package de.ipbhalle.metfragweb.helper;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.similarity.ClusterWrapper;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.MetFragResult;

public class ClusterCompoundsThreadRunner extends ThreadRunner {
	
	private MetFragResultsContainer filteredMetFragResultsContainer; 
	private TreeNode treeRoot;
	
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
		java.util.List<MetFragResult> metfragResult = this.filteredMetFragResultsContainer.getMetFragResults();
		for(int i = 0; i < metfragResult.size(); i++) {
			ICandidate candidate = ((MetFragResult)metfragResult.get(i)).getRoot().getCandidate();
			candidates.addElement(candidate);
		}
		
		
		try {
			ClusterWrapper cwRoot = TanimotoSimilarity.generateCluster(candidates);
			DefaultTreeNode tnRoot = null;
			
			java.util.Stack<ClusterWrapper> clusterWrapperStack = new java.util.Stack<ClusterWrapper>();
			java.util.Stack<DefaultTreeNode> treeNodeStack = new java.util.Stack<DefaultTreeNode>();
			
			if(cwRoot.isLeaf()) tnRoot = new DefaultTreeNode(new ClusterLeaf(cwRoot.getName()), null);
			else tnRoot = new DefaultTreeNode("default", null);
				
			clusterWrapperStack.push(cwRoot);
			treeNodeStack.push(tnRoot);
			
			while(!clusterWrapperStack.isEmpty()) {
				ClusterWrapper cwCurrent = clusterWrapperStack.pop();
				DefaultTreeNode tnCurrent = treeNodeStack.pop();
				ClusterWrapper[] children = cwCurrent.getChildren();
				for(ClusterWrapper child : children) {
					DefaultTreeNode tnNext = null;
					if(child.isLeaf()) tnNext = new DefaultTreeNode("leaf", new ClusterLeaf(child.getName()), tnCurrent);
					else tnNext = new DefaultTreeNode("default", tnCurrent);
					//tnCurrent.getChildren().add(tnNext);
					clusterWrapperStack.push(child);
					treeNodeStack.push(tnNext);
				}
			}
			
			this.treeRoot = tnRoot;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("finished clustering");
	}
	
	public boolean isReady() {
		return this.treeRoot != null;
	}
	
	public void reset() {
		this.filteredMetFragResultsContainer = null;
		this.treeRoot = null;
	}
	
	public TreeNode getTreeRoot() {
		return treeRoot;
	}

	public static void main(String[] args) {
		String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
		double[][] distances = new double[][] { 
		    { 0, 1, 9, 7, 11, 14 },
		    { 1, 0, 4, 3, 8, 10 }, 
		    { 9, 4, 0, 9, 2, 8 },
		    { 7, 3, 9, 0, 6, 13 }, 
		    { 11, 8, 2, 6, 0, 10 },
		    { 14, 10, 8, 13, 10, 0 }};

		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering(distances, names, new AverageLinkageStrategy());
		
		for(int i = 0; i < cluster.getChildren().size(); i++)
			System.out.println(cluster.getChildren().get(i).getChildren().size());
		
		
	}
	
	public class ClusterLeaf {
		
		private String name;
		
		public ClusterLeaf(String name) {
			this.name = name;
		}
		
		public String getName() {
			return this.name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
	}
}
