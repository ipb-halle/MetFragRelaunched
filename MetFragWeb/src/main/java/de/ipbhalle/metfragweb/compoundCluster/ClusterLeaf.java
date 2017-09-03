package de.ipbhalle.metfragweb.compoundCluster;

import de.ipbhalle.metfragweb.datatype.MetFragResult;

public class ClusterLeaf extends ClusterNode {
	
	private MetFragResult metfragResult;
	
	public ClusterLeaf(MetFragResult metfragResult, double maxScore) {
		super(maxScore);
		this.metfragResult = metfragResult;
	}
	
	public MetFragResult getMetfragResult() {
		return metfragResult;
	}

	public void resetMaxScore() {
		if(this.metfragResult != null) this.maxScore = this.metfragResult.getScore();
		else this.maxScore = 0.0;
	}
	
	public void setMetfragResult(MetFragResult metfragResult) {
		this.metfragResult = metfragResult;
	}

	public String getImageAddress() {
		return this.metfragResult.getImageAddress();
	}
	
	public String getName() {
		return this.metfragResult.getRoot().getIdentifier();
	}

	public boolean hasResult() {
		return this.metfragResult != null;
	}

	public MetFragResult getResult() {
		return this.metfragResult;
	}

	@Override
	public int getLeafsUnderneath() {
		return 1;
	}

	@Override
	public void setLeafsUnderneath(int leafsUnderneath) {
		return;
	}

}
