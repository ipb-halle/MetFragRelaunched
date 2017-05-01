package de.ipbhalle.metfraglib.substructure;

import java.util.LinkedList;

import de.ipbhalle.metfraglib.FastBitArray;

public class FingerprintObservations {

	private LinkedList<FastBitArray> fingerprints;
	private LinkedList<Integer> observations;
	private Double[] sumProbabilities; // sum_m p(f,m) -> f fixed
	
	public FingerprintObservations() {
		this.fingerprints = new LinkedList<FastBitArray>();
		this.observations = new LinkedList<Integer>();
	}

	public void calculateSumProbabilities(PeakToFingerprintGroupListCollection collection) {
		if(this.fingerprints.size() == 0) return;
		this.sumProbabilities = new Double[this.fingerprints.size()];
		for(int i = 0; i < collection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = collection.getElement(i);
			for(int j = 0; j < groupList.getNumberElements(); j++) {
				FingerprintGroup group = groupList.getElement(j);
				int currentIndex = getIndexOfFingerprint(group.getFingerprint());
				if(currentIndex == -1) {
					System.err.println("fingerprint not found: " + group.getFingerprint().toStringIDs());
					continue;
				}
				this.sumProbabilities[currentIndex] += group.getProbability();
			}
		}
	}
	
	public double getSumProbabilities(FastBitArray fingerprint) {
		int currentIndex = getIndexOfFingerprint(fingerprint);
		if(currentIndex == -1) {
			return 0.0;
		}
		return this.sumProbabilities[currentIndex];
	}
	
	protected int getIndexOfFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				return i;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				return -1;
			}
		}
		return -1;
	}
	
	public void addFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.observations.set(i, this.observations.get(i) + 1);
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.observations.add(i, new Integer(1));
				return;
			}
		}
	}
	
	public int getNumberObservations(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			int res = this.fingerprints.get(i).compareTo(fingerprint);
			if(res == 0) return this.observations.get(i);
			else if(res > 0) return 0;
		}
		return 0;
	}
	
	public void addFingerprint(String fingerprint) {
		this.addFingerprint(new FastBitArray(fingerprint));
	}
}
