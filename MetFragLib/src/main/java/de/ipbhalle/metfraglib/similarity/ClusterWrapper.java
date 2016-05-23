package de.ipbhalle.metfraglib.similarity;

import com.apporiented.algorithm.clustering.Cluster;

public class ClusterWrapper {

	private Cluster cluster;
	
	public ClusterWrapper(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public ClusterWrapper[] getChildren() {
		if(this.cluster.getChildren() == null) 
			return new ClusterWrapper[0];
		ClusterWrapper[] children = new ClusterWrapper[this.cluster.getChildren().size()];
		for(int i = 0; i < children.length; i++)
			children[i] = new ClusterWrapper(this.cluster.getChildren().get(i));
		return children;
	}
	
	public String getName() {
		return this.cluster.getName();
	}
	
	public Cluster getCluster() {
		return this.cluster;
	}
}
