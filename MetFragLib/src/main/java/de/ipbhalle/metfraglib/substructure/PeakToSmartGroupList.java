package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartGroupList extends DefaultList {

	private Double peakmz;
	
	public PeakToSmartGroupList(Double peakmz) {
		super();
		this.peakmz = peakmz;
	}
	
	public SmartsGroup getElement(int index) {
		return (SmartsGroup)this.list.get(index);
	}
	
	public void addElement(SmartsGroup obj) {
		this.list.add(obj);
	}

	public void addElement(int index, SmartsGroup obj) {
		this.list.add(index, obj);
	}

	public Double getPeakmz() {
		return peakmz;
	}

	public void setPeakmz(Double peakmz) {
		this.peakmz = peakmz;
	}
	
}
