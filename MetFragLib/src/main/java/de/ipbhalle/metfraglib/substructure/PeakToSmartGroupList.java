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
			if(smartsGroup.getProbability() > maxProbability && smartsGroup.smartsMatches(candidate)) {
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

	public void updateProbabilities() {
		// numberSubstructures = how often we have seen the peak peakmz
		int numberSubstructures = 0;
		for(int i = 0; i < this.list.size(); i++) {
			numberSubstructures += ((SmartsGroup)this.list.get(i)).getNumberElements();
		}
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			// P ( substructure | peakmz ) = H( substructure , peakmz ) / H ( peakmz ) 
			smartsGroup.setProbability((double)smartsGroup.getNumberElements() / (double)numberSubstructures);
		}
	}
	
	public String toString() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toString();
		for(int i = 1; i < this.list.size(); i++) {
			SmartsGroup smartGroup = this.getElement(i);
			string += " " + smartGroup.toString();
		}
		return string + "\n";
	}
	
	/**
	 * how often we have seen peakmz => H( peakmz )
	 * 
	 * @return
	 */
	public int getAbsolutePeakFrequency() {
		int numberSubstructures = 0;
		for(int i = 0; i < this.list.size(); i++) {
			numberSubstructures += ((SmartsGroup)this.list.get(i)).getNumberElements();
		}
		return numberSubstructures;
	}
	
	public String toStringSmiles() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toStringSmiles();
		for(int i = 1; i < this.list.size(); i++) {
			SmartsGroup smartGroup = this.getElement(i);
			string += " " + smartGroup.toStringSmiles();
		}
		return string + "\n";
	}
}
