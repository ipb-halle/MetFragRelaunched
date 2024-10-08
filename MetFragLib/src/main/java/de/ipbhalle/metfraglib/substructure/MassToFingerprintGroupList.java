package de.ipbhalle.metfraglib.substructure;

import java.util.Hashtable;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.list.DefaultList;

public class MassToFingerprintGroupList extends DefaultList {

	private Double peakmz;
	private Double sumProbabilities;
	private Double alphaProb;
	private Double betaProb;
	private Integer id;
	
	public MassToFingerprintGroupList(Double peakmz) {
		super();
		this.peakmz = peakmz;
	}
	
	public Double getAlphaProb() {
		return alphaProb;
	}

	public void setAlphaProb(Double alphaProb) {
		this.alphaProb = alphaProb;
	}

	public Double getBetaProb() {
		return betaProb;
	}

	public void setBetaProb(Double betaProb) {
		this.betaProb = betaProb;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void filterByOccurence(int minimumNumberOccurences) {
		java.util.ArrayList<Object> filteredList = new java.util.ArrayList<Object>();
		for(int i = 0; i < this.getNumberElements(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.getElement(i);
			if(fingerprintGroup.getNumberObserved() >= minimumNumberOccurences)
				filteredList.add(fingerprintGroup);
		}
		this.list = filteredList;
	}
	
	// sum_f P(m,f) = P(m)
	public void calculateSumProbabilites() {
		this.sumProbabilities = 0.0;
		for(int i = 0; i < this.getNumberElements(); i++) {
			this.sumProbabilities += this.getElement(i).getProbability();
		}
	}
	
	public double getSumProbabilites() {
		return this.sumProbabilities;
	}

	public void setSumProbabilites(double sumProbability) {
		this.sumProbabilities = sumProbability;
	}
	
	public double getMaximalMatchingProbability(FastBitArray fingerprint) {
		double maxProbability = 0.0;
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			if(fingerprintGroup.getProbability() > maxProbability && fingerprintGroup.getFingerprint().equals(fingerprint)) {
				maxProbability = fingerprintGroup.getProbability();
			}
		}
		return maxProbability;
	}

	public double getMatchingProbability(FastBitArray fingerprint) {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			if(fingerprintGroup.getFingerprint().equals(fingerprint)) {
				return fingerprintGroup.getProbability();
			}
		}
		return 0.0;
	}

	public double getMatchingProbability(FastBitArray fingerprint, boolean debug) {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			if(debug) System.out.println("\t compare " + fingerprint.toStringIDs() + " " + fingerprintGroup.getFingerprint().toStringIDs());
			if(fingerprintGroup.getFingerprint().equals(fingerprint)) {
				return fingerprintGroup.getProbability();
			}
		}
		return 0.0;
	}
	
	public void sortElementsByProbability() {
		DefaultList newlist = new DefaultList();
		for(int i = 0; i < this.list.size(); i++) {
			int index = 0;
			while(index < newlist.getNumberElements() && ((FingerprintGroup)newlist.getElement(index)).getProbability() > this.getElement(i).getProbability()) {
				index++;
			}
			newlist.addElement(index, this.getElement(i));
		}
		this.list.clear();
		for(int i = 0; i < newlist.getNumberElements(); i++) this.addElement((FingerprintGroup)newlist.getElement(i));
	}
	
	public FingerprintGroup getElement(int index) {
		return (FingerprintGroup)this.list.get(index);
	}
	
	public void addElement(FingerprintGroup obj) {
		this.list.add(obj);
	}

	public void addElement(int index, FingerprintGroup obj) {
		this.list.add(index, obj);
	}

	public FingerprintGroup getElementByFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			if(fingerprintGroup.getFingerprint().equals(fingerprint)) {
				return fingerprintGroup;
			}
		}
		return null;
	}

	public void setProbabilityToNumberObserved() {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			fingerprintGroup.setProbabilityToNumberObserved();
		}
	}

	public void setProbabilityToJointProbability() {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			fingerprintGroup.setProbabilityToJointProbability();
		}
	}

	public void setProbabilityToConditionalProbability_sp() {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			fingerprintGroup.setProbabilityToConditionalProbability_sp();
		}
	}
	
	public void setProbabilityToConditionalProbability_ps() {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			fingerprintGroup.setProbabilityToConditionalProbability_ps();
		}
	}
	
	public Double getPeakmz() {
		return peakmz;
	}

	public void setPeakmz(Double peakmz) {
		this.peakmz = peakmz;
	}

	public boolean containsFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.getNumberElements(); i++) {
			if(this.getElement(i).getFingerprint().equals(fingerprint)) return true;
		}
		return false;
	}
	
	// P ( s | p )
	public void updateConditionalProbabilities() {
		// numberSubstructures = how often we have seen the peak peakmz
		int numberSubstructures = 0;
		for(int i = 0; i < this.list.size(); i++) {
			numberSubstructures += ((FingerprintGroup)this.list.get(i)).getNumberObserved();
		}
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			// P ( substructure | peakmz ) = H( substructure , peakmz ) / H ( peakmz ) 
			fingerprintGroup.setConditionalProbability_sp((double)fingerprintGroup.getNumberObserved() / (double)numberSubstructures);
		}
	}

	// P ( p | s )
	public void updateConditionalProbabilities(int[] substructureAbsoluteProbabilities) {
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s))
			fingerprintGroup.setConditionalProbability_ps((double)fingerprintGroup.getNumberObserved() / (double)substructureAbsoluteProbabilities[fingerprintGroup.getId()]);
		}
	}

	// P ( p , s ) - type 3
	public void updateJointProbabilitiesWithSubstructures(int[] substructureAbsoluteProbabilities, double[] substructureRelativeProbabilities) {
		// now calculate P ( s, p ) 
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s)) = P( p | s )
			double p_given_s = (double)fingerprintGroup.getNumberObserved() / (double)substructureAbsoluteProbabilities[fingerprintGroup.getId()];
			fingerprintGroup.setJointProbability(p_given_s * substructureRelativeProbabilities[fingerprintGroup.getId()]);
		}
	}

	// P ( p , s ) - type 4
	public void updateJointProbabilitiesWithPeaks(int[] peakAbsoluteProbabilities, double[] peakRelativeProbabilities, int peakIndex) {
		// now calculate P ( s, p ) 
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			//N_p^(s) / (sum_p N_p^(s)) = P( p | s )
			double s_given_p = (double)fingerprintGroup.getNumberObserved() / (double)peakAbsoluteProbabilities[peakIndex];
			fingerprintGroup.setJointProbability(s_given_p * peakRelativeProbabilities[peakIndex]);
		}
	}
	
	public void updateJointProbabilities(int numberN) {
		// numberSubstructures = how often we have seen the peak peakmz
		for(int i = 0; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = (FingerprintGroup)this.list.get(i);
			// P ( substructure | peakmz ) = H( substructure , peakmz ) / H ( peakmz ) 
			fingerprintGroup.setJointProbability((double)fingerprintGroup.getNumberObserved() / (double)numberN);
		}
	}
	
	public String toString() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toString();
		for(int i = 1; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			string += " " + fingerprintGroup.toString();
		}
		return string + "\n";
	}

	public String toStringSmiles() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toStringSmiles();
		for(int i = 1; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			string += " " + fingerprintGroup.toStringSmiles();
		}
		return string + "\n";
	}

	public String toStringDetail() {
		String string = "";
		if(this.list.size() > 0) string += this.getElement(0).toStringDetail();
		for(int i = 1; i < this.list.size(); i++) {
			FingerprintGroup fingerprintGroup = this.getElement(i);
			string += " " + fingerprintGroup.toStringDetail();
		}
		return string + "\n";
	}
	
	public void updatePeakMass(double mzppm, double mzabs) {
		this.peakmz = this.peakmz + MathTools.calculateAbsoluteDeviation(this.peakmz, mzppm) + mzabs;
	}

	public void updatePeakMass(Hashtable<Integer, ArrayList<Double>> grouplistid_to_masses) {
		if(this.id == null) return;
		if(!grouplistid_to_masses.containsKey(this.id) || grouplistid_to_masses.get(this.id) == null) return;
		ArrayList<Double> masses = grouplistid_to_masses.get(this.id);
		this.peakmz = 0.0;
		for(double mass : masses) this.peakmz += mass;
		this.peakmz /= masses.size();
		this.peakmz = MathTools.round(this.peakmz);
	}
	
	/**
	 * how often we have seen peakmz => H( peakmz )
	 * 
	 * @return
	 */
	public int getAbsolutePeakFrequency() {
		int numberSubstructures = 0;
		for(int i = 0; i < this.list.size(); i++) {
			numberSubstructures += (this.getElement(i)).getNumberObserved();
		}
		return numberSubstructures;
	}
	
}
