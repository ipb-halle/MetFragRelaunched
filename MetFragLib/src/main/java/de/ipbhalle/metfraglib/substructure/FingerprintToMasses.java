package de.ipbhalle.metfraglib.substructure;

import java.util.Vector;

import de.ipbhalle.metfraglib.BitArray;

public class FingerprintToMasses {

	private Vector<Vector<Double>> fingerprintToMasses; 
	private Vector<BitArray> fingerprints;
	
	public FingerprintToMasses() {
		this.fingerprintToMasses = new Vector<Vector<Double>>();
		this.fingerprints = new Vector<BitArray>();
	}
	
	public void addMass(BitArray fingerprint, Double mass) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.addToMasses(mass, this.fingerprintToMasses.get(i));
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new Vector<Double>());
				this.fingerprintToMasses.get(i).add(mass);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new Vector<Double>());
		this.fingerprintToMasses.get(this.fingerprintToMasses.size() - 1).add(mass);
	}
	
	public void addMass(String fingerprint, Double mass) {
		this.addMass(new BitArray(fingerprint), mass);
		new Integer(1).compareTo(new Integer(1));
	}
	
	protected void addToMasses(Double mass, Vector<Double> masses) {
		for(int i = 0; i < masses.size(); i++) {
			int compareResult = masses.get(i).compareTo(mass);
			if(compareResult < 0) {
				masses.add(i, mass);
				return;
			}
			if(compareResult == 0) return;
		}
		masses.add(mass);
	}
	
	public Vector<Double> getFingerprints(BitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return new Vector<Double>();
		return this.fingerprintToMasses.get(index);
	}
	
	public boolean containsFingerprint(BitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return true;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return false;
		}
		return false;
	}
	
	public boolean containsMass(Double mass, Vector<Double> masses) {
		for(int i = 0; i < masses.size(); i++) {
			int compareResult = masses.get(i).compareTo(mass);
			if(compareResult < 0) return false;
			if(compareResult == 0) return true;
		}
		return false;
	}
	
	public int getIndexOfFingerprint(BitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return i;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return -1;
		}
		return -1;
	}
	
	public boolean contains(BitArray fingerprint, Double mass) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return false;
		return this.containsMass(mass, this.fingerprintToMasses.get(index));
	}
	
	public int getSize(BitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0;
		return this.fingerprintToMasses.get(index).size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(Vector<Double> masses : this.fingerprintToMasses) {
			size += masses.size();
		}
		return size;
	}
	
	public void print(BitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		Vector<Double> masses = this.fingerprintToMasses.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i));
		}
	}
}
