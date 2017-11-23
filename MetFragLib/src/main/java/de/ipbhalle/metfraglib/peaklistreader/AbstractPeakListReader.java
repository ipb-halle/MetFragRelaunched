package de.ipbhalle.metfraglib.peaklistreader;

import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.interfaces.IPeakListReader;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractPeakListReader implements IPeakListReader {

	protected Settings settings;
	
	public AbstractPeakListReader(Settings settings) {
		this.settings = settings;
	}

	protected void deleteByMaximumNumberPeaksUsed(int peakLimit, DefaultPeakList peaklist) {
		if(peakLimit < 0) return;
		java.util.Vector<Integer> sortedIntensityIndexes = new java.util.Vector<Integer>();
		for(int i = 0; i < peaklist.getNumberElements(); i++) {
			IPeak currentPeak = ((IPeak)peaklist.getElement(i));
			double currentIntensity = currentPeak.getAbsoluteIntensity();
			int index = 0;
			while(index < sortedIntensityIndexes.size() 
					&& currentIntensity > ((IPeak)peaklist.getElement(sortedIntensityIndexes.get(index))).getAbsoluteIntensity()) 
				index++;
			sortedIntensityIndexes.add(index, i);
		}
		
		java.util.ArrayList<Object> toRemovePeaks = new java.util.ArrayList<Object>();
		int numberOfKeptPeaks = 0;
		for(int k = sortedIntensityIndexes.size() - 1; k >= 0; k--) {
			if(numberOfKeptPeaks >= peakLimit) toRemovePeaks.add(peaklist.getElement(sortedIntensityIndexes.get(k)));
			else numberOfKeptPeaks++;
		}
		peaklist.removeAll(toRemovePeaks);
	}
	
}
