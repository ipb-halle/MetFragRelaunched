package de.ipbhalle.metfragweb.compoundCluster;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.OrganigramNode;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.similarity.ClusterWrapper;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.helper.ThreadRunner;

public class ClusterCompoundsThreadRunner extends ThreadRunner implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2601773430970125516L;
	/**
	 * 
	 */
	private MetFragResultsContainer filteredMetFragResultsContainer; 
	private OrganigramNode treeRoot;   
	private java.util.Vector<OrganigramNode> leaves;
	
	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages) {
		super(beanSettingsContainer, infoMessages, errorMessages);
	}

	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages, MetFragResultsContainer filteredMetFragResultsContainer) {
		super(beanSettingsContainer, infoMessages, errorMessages);
		this.filteredMetFragResultsContainer = filteredMetFragResultsContainer;
	}
	
	@PostConstruct
	@Override
	public void run() {
		
		System.out.println("clustering compounds");
		CandidateList candidates = new CandidateList();
		java.util.HashMap<String, MetFragResult> resultsMap = new java.util.HashMap<String, MetFragResult>();
		
		java.util.List<MetFragResult> metfragResult = this.filteredMetFragResultsContainer.getMetFragResults();
		for(int i = 0; i < metfragResult.size(); i++) {
			ICandidate candidate = ((MetFragResult)metfragResult.get(i)).getRoot().getCandidate();
			candidates.addElement(candidate);
			
			resultsMap.put(candidate.getIdentifier(), (MetFragResult)metfragResult.get(i));
		}
		
		try {
			ClusterWrapper cwRoot = TanimotoSimilarity.generateCluster(candidates);
			OrganigramNode tnRoot = null;
			
			java.util.Stack<ClusterWrapper> clusterWrapperStack = new java.util.Stack<ClusterWrapper>();
			java.util.Stack<OrganigramNode> treeNodeStack = new java.util.Stack<OrganigramNode>();
			
			if(cwRoot.isLeaf()) tnRoot = new DefaultOrganigramNode("compound", new ClusterLeaf(resultsMap.get(cwRoot.getName()), 0.0), null);
			else tnRoot = new DefaultOrganigramNode("compoundGroup", new ClusterNode(0.0), null);
			
			this.setNodeAttributes(tnRoot);
			
			clusterWrapperStack.push(cwRoot);
			treeNodeStack.push(tnRoot);
			
			this.leaves = new java.util.Vector<OrganigramNode>();
			
			while(!clusterWrapperStack.isEmpty()) {
				ClusterWrapper cwCurrent = clusterWrapperStack.pop();
				OrganigramNode tnCurrent = treeNodeStack.pop();
				ClusterWrapper[] children = cwCurrent.getChildren();
				for(ClusterWrapper child : children) {
					OrganigramNode tnNext = null;
					if(child.isLeaf()) {
						MetFragResult currentResult = resultsMap.get(child.getName());
						tnNext = new DefaultOrganigramNode("compound", new ClusterLeaf(currentResult, currentResult.getScore()), tnCurrent);
						this.leaves.add(tnNext);
					}
					else tnNext = new DefaultOrganigramNode("compoundGroup", new ClusterNode(0.0), tnCurrent);

					this.setNodeAttributes(tnNext);
					
					clusterWrapperStack.push(child);
					treeNodeStack.push(tnNext);
				}
			}
			this.treeRoot = tnRoot;
			this.calculateLeafsUnderneath();
			this.updateScores();

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("finished clustering");
	}
	
	protected void setNodeAttributes(OrganigramNode node) {
		node.setExpanded(false);
		node.setDroppable(false);
		node.setDraggable(false);
		node.setSelectable(true);
	}
	
	public void calculateLeafsUnderneath() {
		java.util.LinkedList<OrganigramNode> annotateList = new java.util.LinkedList<OrganigramNode>();
		for(int i = 0; i < this.leaves.size(); i++) {
			if(this.leaves.get(i).getParent() == null) continue;
			annotateList.add(this.leaves.get(i).getParent());
		}
		while(!annotateList.isEmpty()) {
			OrganigramNode current = annotateList.poll();
			java.util.List<OrganigramNode> children = current.getChildren();
			int numberLeafsUnderneath = 0;
			for(int i = 0; i < children.size(); i++) {
				numberLeafsUnderneath += ((INode)children.get(i).getData()).getLeafsUnderneath();
			}
			((INode)current.getData()).setLeafsUnderneath(numberLeafsUnderneath);
			if(current.getParent() == null) continue;
			annotateList.add(current.getParent());
		}
	}
	
	/**
	 * 
	 */
	public void updateScores() {
		
		java.util.Stack<OrganigramNode> stackList = new java.util.Stack<OrganigramNode>();
		stackList.push(this.treeRoot);
		while(!stackList.isEmpty()) {
			OrganigramNode current = stackList.pop();
			if(current == null) continue;
			((INode)current.getData()).resetMaxScore();
			for(OrganigramNode child : current.getChildren()) {
				stackList.push(child);
			}
		}
		
		java.util.LinkedList<OrganigramNode> annotateList = new java.util.LinkedList<OrganigramNode>();
		for(int i = 0; i < this.leaves.size(); i++) {
			annotateList.add(this.leaves.get(i));
		}
		
		while(!annotateList.isEmpty()) {
			OrganigramNode current = annotateList.poll();
			OrganigramNode parent = current.getParent();
			if(parent == null) continue;
			
			double currentMaxScore = ((ClusterNode)current.getData()).getMaxScore();
			double parentMaxScore = ((ClusterNode)parent.getData()).getMaxScore();
			if(parentMaxScore < currentMaxScore) {
				((ClusterNode)parent.getData()).setMaxScore(currentMaxScore);
			}
			
			annotateList.add(parent);
		}
		
	}
	
	public boolean isReady() {
		return this.treeRoot != null;
	}
	
	public void reset() {
		this.filteredMetFragResultsContainer = null;
		this.treeRoot = null;
	}
	
	public OrganigramNode getTreeRoot() {
		return this.treeRoot;
	}

}
