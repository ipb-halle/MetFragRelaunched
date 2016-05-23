package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartGroupList extends DefaultList {

	private Double peakmz;
	
	public PeakToSmartGroupList(Double peakmz) {
		super();
		this.peakmz = peakmz;
	}
	
	public double getMaximalMatchingProbability(ICandidate candidate) {
		double maxProbability = 0.0;
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			if(smartsGroup.smartsMatches(candidate) && smartsGroup.getProbability() > maxProbability) {
				maxProbability = smartsGroup.getProbability();
			}
		}
		return maxProbability;
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

	public SmartsGroup getElementBySmiles(String smiles, double similarityThreshold) {
		double maxSimilarity = 0.0;
		SmartsGroup bestMatch = null;
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			double currentSimilarity = smartsGroup.getBestSimilarity(smiles);
			if(currentSimilarity > maxSimilarity) {
				maxSimilarity = currentSimilarity;
				if(currentSimilarity >= similarityThreshold) bestMatch = smartsGroup;
			}
		}
		return bestMatch;
	}
	
	public Double getPeakmz() {
		return peakmz;
	}

	public void setPeakmz(Double peakmz) {
		this.peakmz = peakmz;
	}
	
}
