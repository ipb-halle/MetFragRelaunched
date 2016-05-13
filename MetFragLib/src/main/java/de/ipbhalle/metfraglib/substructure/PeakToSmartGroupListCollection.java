package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartGroupListCollection extends DefaultList {
	
	public PeakToSmartGroupListCollection() {
		super();
	}

	public void addElement(PeakToSmartGroupList obj) {
		this.list.add(obj);
	}

	public void addElement(int index, PeakToSmartGroupList obj) {
		this.list.add(index, obj);
	}
	
	public PeakToSmartGroupList getElement(int index) {
		return (PeakToSmartGroupList)this.getElement(index);
	}
}
