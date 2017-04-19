package de.ipbhalle.metfraglib.substructure;

import java.util.Vector;

import de.ipbhalle.metfraglib.FastBitArray;

public class FingerprintToProbabilities {

	private Vector<Vector<Double>> fingerprintToProbabilities;
	private Vector<FastBitArray> fingerprints;
	
	public FingerprintToProbabilities() {
		this.fingerprintToProbabilities = new Vector<Vector<Double>>();
		this.fingerprints = new Vector<FastBitArray>();
	}
	
	public void addFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToProbabilities.add(i, new Vector<Double>());
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToProbabilities.add(new Vector<Double>());
	}

	public void addFingerprint(FastBitArray fingerprint, Vector<Integer> probabilities) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				Vector<Double> tmp = new Vector<Double>();
				for(int k = 0; k < probabilities.size(); k++) tmp.add((double)probabilities.get(k));
				this.fingerprintToProbabilities.add(i, tmp);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		Vector<Double> tmp = new Vector<Double>();
		for(int k = 0; k < probabilities.size(); k++) tmp.add((double)probabilities.get(k));
		this.fingerprintToProbabilities.add(tmp);
	}
	
	public void addProbability(FastBitArray fingerprint, Double probability) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.addToProbabilities(probability, this.fingerprintToProbabilities.get(i));
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToProbabilities.add(i, new Vector<Double>());
				this.fingerprintToProbabilities.get(i).add(probability);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToProbabilities.add(new Vector<Double>());
		this.fingerprintToProbabilities.get(this.fingerprintToProbabilities.size() - 1).add(probability);
	}
	
	public void addProbability(String fingerprint, Double probability) {
		this.addProbability(new FastBitArray(fingerprint), probability);
	}
	
	protected void addToProbabilities(Double probability, Vector<Double> probabilities) {
		for(int i = 0; i < probabilities.size(); i++) {
			int compareResult = probabilities.get(i).compareTo(probability);
			if(compareResult <= 0) {
				probabilities.add(i, probability);
				return;
			}
			
		}
		probabilities.add(probability);
	}
	
	public Vector<Double> getProbabilities(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return new Vector<Double>();
		return this.fingerprintToProbabilities.get(index);
	}

	public Vector<Double> getProbabilities(int index) {
		if(index == -1) return new Vector<Double>();
		return this.fingerprintToProbabilities.get(index);
	}

	public boolean containsFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return true;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return false;
		}
		return false;
	}
	
	public int getIndexOfFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return i;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return -1;
		}
		return -1;
	}
	
	public int getSize(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0;
		return this.fingerprintToProbabilities.get(index).size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(Vector<Double> probabilities : this.fingerprintToProbabilities) {
			size += probabilities.size();
		}
		return size;
	}
	
	public void print(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		Vector<Double> probabilities = this.fingerprintToProbabilities.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < probabilities.size(); i++) {
			System.out.println("-> " + probabilities.get(i));
		}
	}
	
	public void print(String fp) {
		FastBitArray fingerprint = new FastBitArray(fp);
		int index = this.getIndexOfFingerprint(fingerprint);
		Vector<Double> probabilities = this.fingerprintToProbabilities.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < probabilities.size(); i++) {
			System.out.println("-> " + probabilities.get(i));
		}
	}
}
