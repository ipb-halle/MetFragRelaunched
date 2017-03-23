package de.ipbhalle.metfraglib.substructure;

import java.util.Vector;

import de.ipbhalle.metfraglib.BitArray;

public class FingerprintGroup {

	private Double jointProbability = null;
	private Double conditionalProbability_ps = null;
	private Double conditionalProbability_sp = null;
	
	private int numberObserved = 0;
	private Double probability = null;
	
	BitArray fingerprint;
	String smiles;
	private Integer id = null;

	public FingerprintGroup(Double probability) {
		super();
		this.probability = probability;
	}

	public FingerprintGroup(Double probability, Double jointProbability, Double conditionalProbability_ps, Double conditionalProbability_sp) {
		super();
		this.probability = probability;
		this.jointProbability = jointProbability;
		this.conditionalProbability_ps = conditionalProbability_ps;
		this.conditionalProbability_sp = conditionalProbability_sp;
	}

	public int getNumberObserved() {
		return this.numberObserved;
	}

	public void setNumberObserved(int numberObserved) {
		this.numberObserved = numberObserved;
	}

	public void incerementNumberObserved() {
		this.numberObserved++;
	}
	
	public void setFingerprint(String fingerprint) {
		this.fingerprint = new BitArray(fingerprint);
	}

	public BitArray getFingerprint() {
		return this.fingerprint;
	}

	public void setSmiles(String smiles) {
		this.smiles = smiles;
	}

	public String getSmiles() {
		return this.smiles;
	}
	
	public void setProbabilityToJointProbability() {
		this.probability = this.jointProbability;
	}

	public void setProbabilityToConditionalProbability_ps() {
		this.probability = this.conditionalProbability_ps;
	}

	public void setProbabilityToConditionalProbability_sp() {
		this.probability = this.conditionalProbability_sp;
	}
	
	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public Double getProbability() {
		return probability;
	}

	public Double getJointProbability() {
		return jointProbability;
	}

	public void setJointProbability(Double jointProbability) {
		this.jointProbability = jointProbability;
	}

	public Double getConditionalProbability_ps() {
		return conditionalProbability_ps;
	}

	public void setConditionalProbability_ps(Double conditionalProbability_ps) {
		this.conditionalProbability_ps = conditionalProbability_ps;
	}

	public Double getConditionalProbability_sp() {
		return conditionalProbability_sp;
	}

	public void setConditionalProbability_sp(Double conditionalProbability_sp) {
		this.conditionalProbability_sp = conditionalProbability_sp;
	}

	public boolean fingerprintMatches(Vector<String> _fingerprints) {
		for(int i = 0; i < _fingerprints.size(); i++) {
			if(_fingerprints.get(i).equals(this.fingerprint)) return true;
		}
		return false;
	}
	
	public void print() {
		System.out.print(this.probability + " " + this.fingerprint);
		System.out.println();
	}
	
	public String toString() {
		String string = this.probability + " " + this.fingerprint;
		return string;
	}

	public String toStringSmiles() {
		String string = this.probability + " " + this.fingerprint + "|" + this.smiles;
		return string;
	}

	public String toStringDetail() {
		String string = this.probability + " " + this.fingerprint + "|" + this.fingerprint.toStringIDs() + "|" + this.smiles;
		return string;
	}

	public void setId(int id) {
		this.id = id;
	}
	public Integer getId() {
		return id;
	}
}
