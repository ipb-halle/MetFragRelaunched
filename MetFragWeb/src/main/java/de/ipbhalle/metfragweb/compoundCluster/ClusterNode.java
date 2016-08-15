package de.ipbhalle.metfragweb.compoundCluster;

import de.ipbhalle.metfragweb.datatype.MetFragResult;

public class ClusterNode implements INode {
	
	protected double maxScore;
	
	public ClusterNode(double maxScore) {
		this.maxScore = maxScore;
	}

	public void resetMaxScore() {
		this.maxScore = 0.0;
	}
	
	public double getMaxScore() {
		return this.maxScore;
	}

	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}
	
	public boolean hasResult() {
		return false;
	}

	public MetFragResult getResult() {
		return null;
	}
	
}

