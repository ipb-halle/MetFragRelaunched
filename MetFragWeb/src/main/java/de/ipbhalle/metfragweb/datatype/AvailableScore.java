package de.ipbhalle.metfragweb.datatype;

import java.io.Serializable;

public class AvailableScore implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7476077911439905923L;
	protected String label;
	protected String scoreName;
	protected boolean isSelected;

	public AvailableScore(String scoreName) {
		this.label = scoreName;
		this.scoreName = scoreName;
		this.isSelected = false;
	}
	
	public AvailableScore(String scoreName, String label) {
		this.label = label;
		this.scoreName = scoreName;
		this.isSelected = false;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getScoreName() {
		return this.scoreName;
	}

	public void setScoreName(String scoreName) {
		this.scoreName = scoreName;
	}

	public boolean isSelected() {
		return this.isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}



}
