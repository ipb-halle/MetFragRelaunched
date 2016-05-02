package de.ipbhalle.metfraglib.similarity;

import com.apporiented.algorithm.clustering.Cluster;

public class TanimotoClusterWrapper {

	private Cluster cluster;
	
	public TanimotoClusterWrapper(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public TanimotoClusterWrapper[] getChildren() {
		if(this.cluster.getChildren() == null) 
			return new TanimotoClusterWrapper[0];
		TanimotoClusterWrapper[] children = new TanimotoClusterWrapper[this.cluster.getChildren().size()];
		for(int i = 0; i < children.length; i++)
			children[i] = new TanimotoClusterWrapper(this.cluster.getChildren().get(i));
		return children;
	}
	
	public String getName() {
		return this.cluster.getName();
	}
	
	public Cluster getCluster() {
		return this.cluster;
	}
}
