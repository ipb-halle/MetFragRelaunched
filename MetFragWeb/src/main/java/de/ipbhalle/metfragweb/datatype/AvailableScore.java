package de.ipbhalle.metfragweb.datatype;

import java.io.Serializable;

import jakarta.faces.model.SelectItem;

public class AvailableScore extends SelectItem implements Serializable, Comparable<AvailableScore> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7476077911439905923L;
	protected boolean isSelected;

	public AvailableScore(String scoreName) {
		this.setLabel(scoreName);
		this.setValue(scoreName);
		this.isSelected = false;
	}
	
	public AvailableScore(String scoreName, String label) {
		this.setLabel(label);
		this.setValue(scoreName);
		this.isSelected = false;
	}

	public String getScoreName() {
		return (String)this.getValue();
	}

	public void setScoreName(String scoreName) {
		this.setValue(scoreName);
	}

	public boolean isSelected() {
		return this.isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	@Override
	public int compareTo(AvailableScore o) {
		return this.getLabel().compareTo(o.getLabel());
	}


}
