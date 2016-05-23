package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.additionals.MathTools;
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
		return (PeakToSmartGroupList)this.list.get(index);
	}
	
	public PeakToSmartGroupList getElementByPeak(Double mzValue, Double mzppm, Double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mzValue, mzppm) + mzabs;
		double minDev = Integer.MAX_VALUE;
		PeakToSmartGroupList bestMatch = null;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList peakToSmartGroupList = (PeakToSmartGroupList)this.list.get(i);
			double currentDev = Math.abs(peakToSmartGroupList.getPeakmz() - mzValue);
			if(currentDev <= dev) {
				if(currentDev < minDev) {
					minDev = currentDev;
					bestMatch = peakToSmartGroupList;
				}
			}
		}
		return bestMatch;
	}
	
	public void print() {
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList peakToSmartGroupList = this.getElement(i);
			System.out.println(peakToSmartGroupList.getPeakmz());
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				System.out.print("\t");
				peakToSmartGroupList.getElement(j).print();
			}
		}
	}
}
