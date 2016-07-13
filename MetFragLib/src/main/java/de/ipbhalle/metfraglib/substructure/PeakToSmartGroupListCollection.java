package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartGroupListCollection extends DefaultList {
	
	// P ( p )
	double[] peakProbabilities;
	
	public PeakToSmartGroupListCollection() {
		super();
	}

	public void addElement(PeakToSmartGroupList obj) {
		this.list.add(obj);
	}

	public void addElementSorted(PeakToSmartGroupList obj) {
		int index = 0;
		while(index < this.list.size()) {
			double peakMz = ((PeakToSmartGroupList)this.list.get(index)).getPeakmz();
			if(peakMz < obj.getPeakmz()) index++;
			else break;
		}
		this.list.add(index, obj);
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
			System.out.print(peakToSmartGroupList.getPeakmz());
			for(int j = 0; j < peakToSmartGroupList.getNumberElements(); j++) {
				System.out.print(" ");
				peakToSmartGroupList.getElement(j).print();
			}
		}
	}
	
	public String toString() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList peakToSmartGroupList = this.getElement(i);
			string += peakToSmartGroupList.getPeakmz() + " " + peakToSmartGroupList.toString();
		}
		return string;
	}

	public String toStringSmiles() {
		String string = "";
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList peakToSmartGroupList = this.getElement(i);
			string += peakToSmartGroupList.getPeakmz() + " " + peakToSmartGroupList.toStringSmiles();
		}
		return string;
	}
	
	/**
	 * calculate P ( p ) for every peak p
	 * 
	 */
	public void calculatePeakProbabilities() {
		this.peakProbabilities = new double[this.list.size()];
		int totalNumber = 0;
		for(int i = 0; i < this.list.size(); i++) {
			totalNumber += this.getElement(i).getAbsolutePeakFrequency();
			this.peakProbabilities[i] = (double)totalNumber;
		}
		for(int i = 0; i < this.peakProbabilities.length; i++) {
			this.peakProbabilities[i] /= (double)totalNumber;
		}
	}
	
	public void calculatePosteriorProbabilites() {
		double sumJointProbabilities = 0.0;
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList currentPeakToSmartGroupList = this.getElement(i);
			for(int j = 0; j < currentPeakToSmartGroupList.getNumberElements(); j++) {
				SmartsGroup currentSmartsGroup = currentPeakToSmartGroupList.getElement(j);
				// P ( s | p ) 
				double currentLikelihood = currentSmartsGroup.getProbability();
				// P ( s | p ) * P ( p )
				double currentJointProbability = currentLikelihood * this.peakProbabilities[i];
				currentSmartsGroup.setProbability(currentJointProbability);
				sumJointProbabilities += currentJointProbability;
			}
		}
		for(int i = 0; i < this.list.size(); i++) {
			PeakToSmartGroupList currentPeakToSmartGroupList = this.getElement(i);
			for(int j = 0; j < currentPeakToSmartGroupList.getNumberElements(); j++) {
				SmartsGroup currentSmartsGroup = currentPeakToSmartGroupList.getElement(j);
				// now P ( s , p ) 
				double currentJointProbability = currentSmartsGroup.getProbability();
				// P ( s , p ) / sum_p P ( s , p ) = P ( s , p ) / P ( s )
				double currentPosteriorProbability = currentJointProbability / sumJointProbabilities;
				currentSmartsGroup.setProbability(currentPosteriorProbability);
			}
		}
	}
	
	public void updateProbabilities() {
		for(int i = 0; i < this.list.size(); i++) {
			this.getElement(i).updateProbabilities();
		}
	}
}
