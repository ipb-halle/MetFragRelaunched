package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.peak.MergedTandemMassPeak;

public class SortedMergedTandemMassPeakList extends SortedTandemMassPeakList {

	public SortedMergedTandemMassPeakList(Double measuredPrecursorMass) {
		super(measuredPrecursorMass);
	}

	public MergedTandemMassPeak getElement(int index) {
		return (MergedTandemMassPeak)this.list.get(index);
	}
	
	public void addElement(MergedTandemMassPeak tandemMassPeak) {
		int index = 0;
		double mass = tandemMassPeak.getMass();
		while(index < this.list.size() && mass > ((MergedTandemMassPeak)this.list.get(index)).getMass()) index++;
		this.list.add(index, tandemMassPeak);
	}
	
	public int getNumberPeaksUsed() {
		int countUsedPeaks = 0;
		for(int i = 0; i < this.list.size(); i++) {
			try {
				this.getElement(i).getIntensity();
			}
			catch(RelativeIntensityNotDefinedException e) {
				continue;
			}
			countUsedPeaks++;
		}
		return countUsedPeaks;
	}
}
