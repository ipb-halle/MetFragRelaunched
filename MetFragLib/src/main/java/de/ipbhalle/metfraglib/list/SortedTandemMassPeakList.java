package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.peak.Peak;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;

/**
 * elements of list are of type TandemMassPeakList
 * 
 * @author cruttkie
 *
 */
public class SortedTandemMassPeakList extends DefaultPeakList {
	
	protected Double measuredPrecursorMass;
	
	public SortedTandemMassPeakList(Double measuredPrecursorMass) {
		super();
		this.measuredPrecursorMass = measuredPrecursorMass;
	}
	
	public Double getMeasuredPrecursorMass() {
		return measuredPrecursorMass;
	}

	public TandemMassPeak getElement(int index) {
		return (TandemMassPeak)this.list.get(index);
	}
	
	public void addElement(TandemMassPeak tandemMassPeak) {
		int index = 0;
		double mass = tandemMassPeak.getMass();
		while(index < this.list.size() && mass > ((TandemMassPeak)this.list.get(index)).getMass()) index++;
		this.list.add(index, tandemMassPeak);
	}
	
	public String toString() {
		String sortedTandemMassPeakListAsString = "";
		for(int i = 0; i < this.list.size(); i++) {
			TandemMassPeak currentTandemMassPeak = (TandemMassPeak)this.list.get(i);
			sortedTandemMassPeakListAsString += currentTandemMassPeak.getMass() + " " + currentTandemMassPeak.getAbsoluteIntensity() + " " + currentTandemMassPeak.getRelativeIntensity() + "\n";
		}
		return sortedTandemMassPeakListAsString;
	}
	
	@Override
	public boolean containsMass(double mass, double mzppm, double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;

		for(int i = 0; i < this.list.size(); i++) 
		{
			double currentMass = ((Peak)this.list.get(i)).getMass();
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				return true; 
			}
			else if(currentMass > mass) return false;
			
		}
		return false;
	}
	
	protected int getIndexOfPeakByMass(double mass, double mzppm, double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;

		for(int i = 0; i < this.list.size(); i++) 
		{
			double currentMass = ((Peak)this.list.get(i)).getMass();
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				return i; 
			}
			else if(currentMass > mass) return -1;
			
		}
		return -1;
	}
	
	public double cosineSimilarity(SortedTandemMassPeakList peakList, double mzppm, double mzabs) {
		//http://www.sciencedirect.com/science/article/pii/S1044030501003270
		double numerator = 0.0;
		double asquare = 0.0;
		double bsquare = 0.0;
		BitArray foundPeaks = new BitArray(this.getNumberElements());
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			double mass = peakList.getElement(i).getMass();
			double intensity = peakList.getElement(i).getRelativeIntensity();
			int index = this.getIndexOfPeakByMass(mass, mzppm, mzabs);
			asquare += intensity * intensity;
			if(index != -1) {
				foundPeaks.set(index);
				numerator += intensity * this.getElement(index).getRelativeIntensity();
				bsquare += this.getElement(index).getRelativeIntensity() * this.getElement(index).getRelativeIntensity();
			}
		}
		for(int i = 0; i < foundPeaks.getSize(); i++) {
			if(!foundPeaks.get(i)) {
				bsquare += this.getElement(i).getRelativeIntensity() * this.getElement(i).getRelativeIntensity();
			}
		}
		if(asquare == 0.0 || bsquare == 0.0) return 0.0;
		return MathTools.round(numerator / Math.sqrt(asquare * bsquare), 4.0);
	}
	
	public int getNumberPeaksUsed() {
		return this.list.size();
	}
}
