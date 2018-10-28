package de.ipbhalle.metfragweb.compoundCluster;

import de.ipbhalle.metfragweb.datatype.MetFragResult;

public class ClusterNode implements INode {
	
	protected double maxScore;
	protected int leafsUnderneath;
	
	public ClusterNode(double maxScore) {
		this.maxScore = maxScore;
		this.leafsUnderneath = 0;
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

	@Override
	public int getLeafsUnderneath() {
		return this.leafsUnderneath;
	}

	@Override
	public void setLeafsUnderneath(int leafsUnderneath) {
		this.leafsUnderneath = leafsUnderneath;
	}

	public boolean hasResult() {
		return false;
	}

	public MetFragResult getResult() {
		return null;
	}

	public String getNodeColorCSS(double maximumScore) {
		if(maximumScore < 0.00000001) return "background: hsl(250, 100%, 35%)";
		double light = 1.0 - (1.0 * ((this.maxScore) / (maximumScore - 0.0)));
		double scaled_light = (((0.75 - 0.35) * (light - 0)) / (maximumScore - 0)) + 0.35;
		return "background: hsl(250, 100%, " + (scaled_light * 100) + "%)";
	}
}

