package de.ipbhalle.metfraglib.substructure;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultList;

public class PeakToSmartsGroupList extends DefaultList {

	private Double peakmz;
	
	public PeakToSmartsGroupList(Double peakmz) {
		super();
		this.peakmz = peakmz;
	}
	
	public void filterByOccurence(int minimumNumberOccurences) {
		java.util.Vector<Object> filteredList = new java.util.Vector<Object>();
		for(int i = 0; i < this.getNumberElements(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.getElement(i);
			if(smartsGroup.getNumberElements() >= minimumNumberOccurences)
				filteredList.add(smartsGroup);
		}
		this.list = filteredList;
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

	public double getMaximalMatchingProbabilitySorted(ICandidate candidate) {
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			if(smartsGroup.smartsMatches(candidate)) {
				//if already matched you can discard all others (if sorted!!)
				return smartsGroup.getProbability();
			}
		}
		return 0.0;
	}
	
	public void sortElementsByProbability() {
		DefaultList newlist = new DefaultList();
		for(int i = 0; i < this.list.size(); i++) {
			int index = 0;
			while(index < newlist.getNumberElements() && ((SmartsGroup)newlist.getElement(index)).getProbability() > this.getElement(i).getProbability()) {
				index++;
			}
			newlist.addElement(index, this.getElement(i));
		}
		this.list.clear();
		for(int i = 0; i < newlist.getNumberElements(); i++) this.addElement((SmartsGroup)newlist.getElement(i));
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

	public void setProbabilityToJointProbability() {
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			smartsGroup.setProbabilityToJointProbability();
		}
	}

	public void setProbabilityToConditionalProbability_sp() {
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			smartsGroup.setProbabilityToConditionalProbability_sp();
		}
	}
	
	public void setProbabilityToConditionalProbability_ps() {
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			smartsGroup.setProbabilityToConditionalProbability_ps();
		}
	}
	
	public Double getPeakmz() {
		return peakmz;
	}

	public void setPeakmz(Double peakmz) {
		this.peakmz = peakmz;
	}

	// P ( s | p )
	public void updateConditionalProbabilities() {
		// numberSubstructures = how often we have seen the peak peakmz
		int numberSubstructures = 0;
		for(int i = 0; i < this.list.size(); i++) {
			numberSubstructures += ((SmartsGroup)this.list.get(i)).getNumberElements();
		}
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			// P ( substructure | peakmz ) = H( substructure , peakmz ) / H ( peakmz ) 
			smartsGroup.setConditionalProbability_sp((double)smartsGroup.getNumberElements() / (double)numberSubstructures);
		}
	}

	// P ( p | s )
	public void updateConditionalProbabilities(int[] substructureAbsoluteProbabilities) {
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s))
			smartsGroup.setConditionalProbability_ps((double)smartsGroup.getNumberElements() / (double)substructureAbsoluteProbabilities[smartsGroup.getId()]);
		}
	}

	// P ( p , s ) - type 3
	public void updateJointProbabilitiesWithSubstructures(int[] substructureAbsoluteProbabilities, double[] substructureRelativeProbabilities) {
		// now calculate P ( s, p ) 
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s)) = P( p | s )
			double p_given_s = (double)smartsGroup.getNumberElements() / (double)substructureAbsoluteProbabilities[smartsGroup.getId()];
			smartsGroup.setJointProbability(p_given_s * substructureRelativeProbabilities[smartsGroup.getId()]);
		}
	}

	// P ( p , s ) - type 4
	public void updateJointProbabilitiesWithPeaks(int[] peakAbsoluteProbabilities, double[] peakRelativeProbabilities, int peakIndex) {
		// now calculate P ( s, p ) 
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s)) = P( p | s )
			double s_given_p = (double)smartsGroup.getNumberElements() / (double)peakAbsoluteProbabilities[peakIndex];
			smartsGroup.setJointProbability(s_given_p * peakRelativeProbabilities[peakIndex]);
		}
	}
	
	public void updateJointProbabilities(int numberN) {
		// numberSubstructures = how often we have seen the peak peakmz
		for(int i = 0; i < this.list.size(); i++) {
			SmartsGroup smartsGroup = (SmartsGroup)this.list.get(i);
			// P ( substructure | peakmz ) = H( substructure , peakmz ) / H ( peakmz ) 
			smartsGroup.setJointProbability((double)smartsGroup.getNumberElements() / (double)numberN);
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

	public String toStringDetail() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toString() + ":" + this.getElement(0).getId();
		for(int i = 1; i < this.list.size(); i++) {
			SmartsGroup smartGroup = this.getElement(i);
			string += " " + smartGroup.toString() + ":" + smartGroup.getId();
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
	
	public void removeDuplicates() {
		for(int i = 0; i < this.list.size(); i++) {
			((SmartsGroup)this.list.get(i)).removeDuplicates();
		}
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
