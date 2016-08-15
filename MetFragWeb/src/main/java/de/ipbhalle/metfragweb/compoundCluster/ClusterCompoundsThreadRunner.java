package de.ipbhalle.metfragweb.compoundCluster;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.similarity.ClusterWrapper;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.helper.ThreadRunner;

public class ClusterCompoundsThreadRunner extends ThreadRunner {
	
	private MetFragResultsContainer filteredMetFragResultsContainer; 
	private DefaultTreeNode treeRoot;
	private java.util.Vector<DefaultTreeNode> leaves;
	
	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages) {
		super(beanSettingsContainer, infoMessages, errorMessages);
	}

	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages, MetFragResultsContainer filteredMetFragResultsContainer) {
		super(beanSettingsContainer, infoMessages, errorMessages);
		this.filteredMetFragResultsContainer = filteredMetFragResultsContainer;
	}
	
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
			DefaultTreeNode tnRoot = null;
			
			java.util.Stack<ClusterWrapper> clusterWrapperStack = new java.util.Stack<ClusterWrapper>();
			java.util.Stack<DefaultTreeNode> treeNodeStack = new java.util.Stack<DefaultTreeNode>();
			
			if(cwRoot.isLeaf()) tnRoot = new DefaultTreeNode("leaf", new ClusterLeaf(resultsMap.get(cwRoot.getName()), 0.0), null);
			else tnRoot = new DefaultTreeNode("node", new ClusterNode(0.0), null);
			
				
			clusterWrapperStack.push(cwRoot);
			treeNodeStack.push(tnRoot);
			
			this.leaves = new java.util.Vector<DefaultTreeNode>();
			
			while(!clusterWrapperStack.isEmpty()) {
				ClusterWrapper cwCurrent = clusterWrapperStack.pop();
				DefaultTreeNode tnCurrent = treeNodeStack.pop();
				ClusterWrapper[] children = cwCurrent.getChildren();
				for(ClusterWrapper child : children) {
					DefaultTreeNode tnNext = null;
					if(child.isLeaf()) {
						MetFragResult currentResult = resultsMap.get(child.getName());
						tnNext = new DefaultTreeNode("leaf", new ClusterLeaf(currentResult, currentResult.getScore()), tnCurrent);
						this.leaves.add(tnNext);
					}
					else tnNext = new DefaultTreeNode("node", new ClusterNode(0.0), tnCurrent);
					
					clusterWrapperStack.push(child);
					treeNodeStack.push(tnNext);
				}
			}
			
			this.updateScores();

			this.treeRoot = tnRoot;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("finished clustering");
	}
	
	/**
	 * 
	 */
	public void updateScores() {
		
		java.util.Stack<TreeNode> stackList = new java.util.Stack<TreeNode>();
		stackList.push(this.treeRoot);
		while(!stackList.isEmpty()) {
			TreeNode current = stackList.pop();
			if(current == null) continue;
			((INode)current.getData()).resetMaxScore();
			for(TreeNode child : current.getChildren()) {
				stackList.push(child);
			}
		}
		
		java.util.LinkedList<TreeNode> annotateList = new java.util.LinkedList<TreeNode>();
		for(int i = 0; i < this.leaves.size(); i++) {
			annotateList.add(this.leaves.get(i));
		}
		
		while(!annotateList.isEmpty()) {
			TreeNode current = annotateList.poll();
			TreeNode parent = current.getParent();
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
	
	public DefaultTreeNode getTreeRoot() {
		return treeRoot;
	}

}
