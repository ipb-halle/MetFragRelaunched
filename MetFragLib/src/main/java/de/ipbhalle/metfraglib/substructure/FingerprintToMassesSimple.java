package de.ipbhalle.metfraglib.substructure;

import java.util.ArrayList;
import java.util.LinkedList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;

public class FingerprintToMassesSimple {

	private ArrayList<LinkedList<Double>> fingerprintToMasses;
	private ArrayList<FastBitArray> fingerprints;
	
	public FingerprintToMassesSimple() {
		this.fingerprintToMasses = new ArrayList<LinkedList<Double>>();
		this.fingerprints = new ArrayList<FastBitArray>();
	}
	
	public void addMass(FastBitArray fingerprint, Double mass) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.addToMasses(mass, this.fingerprintToMasses.get(i));
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new LinkedList<Double>());
				this.fingerprintToMasses.get(i).add(mass);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new LinkedList<Double>());
		this.fingerprintToMasses.get(this.fingerprintToMasses.size() - 1).add(mass);
	}

	public void addFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new LinkedList<Double>());
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new LinkedList<Double>());
	}

	public void addMass(FastBitArray fingerprint, Double mass, double mzppm, double mzabs) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) {
				this.addToMasses(mass, this.fingerprintToMasses.get(i),	mzppm, mzabs);
				return;
			}
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) {
				this.fingerprints.add(i, fingerprint);
				this.fingerprintToMasses.add(i, new LinkedList<Double>());
				this.fingerprintToMasses.get(i).add(mass);
				return;
			}
		}
		this.fingerprints.add(fingerprint);
		this.fingerprintToMasses.add(new LinkedList<Double>());
		this.fingerprintToMasses.get(this.fingerprintToMasses.size() - 1).add(mass);
	}
	
	public void addMass(String fingerprint, Double mass) {
		this.addMass(new FastBitArray(fingerprint), mass);
	}

	protected void addToMasses(Double mass, LinkedList<Double> masses, double mzppm, double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		double bestDev = Integer.MAX_VALUE;	
		
		for(int i = 0; i < masses.size(); i++) {
			double currentMass = masses.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				double currentDev = Math.abs(currentMass - mass);
				if(currentDev < bestDev) {
					bestDev = currentDev;
				}
			}
		}
		// mass already included
		if(bestDev <= dev) return;
		
		masses.add(mass);
	}
	
	protected void addToMasses(Double mass, LinkedList<Double> masses) {
		if(!masses.contains(mass)) masses.add(mass);
	}
	
	public LinkedList<Double> getMasses(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return new LinkedList<Double>();
		return this.fingerprintToMasses.get(index);
	}

	public LinkedList<Double> getMasses(int index) {
		if(index == -1) return new LinkedList<Double>();
		return this.fingerprintToMasses.get(index);
	}
	
	public boolean containsFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return true;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return false;
		}
		return false;
	}
	
	public boolean containsMass(Double mass, LinkedList<Double> masses) {
		return masses.contains(mass);
	}
	
	public int getIndexOfFingerprint(FastBitArray fingerprint) {
		for(int i = 0; i < this.fingerprints.size(); i++) {
			if(this.fingerprints.get(i).equals(fingerprint)) return i;
			if(this.fingerprints.get(i).compareTo(fingerprint) > 0) return -1;
		}
		return -1;
	}
	
	public boolean contains(FastBitArray fingerprint, Double mass) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return false;
		return this.containsMass(mass, this.fingerprintToMasses.get(index));
	}
	
	public int getSize(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		if(index == -1) return 0;
		return this.fingerprintToMasses.get(index).size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(LinkedList<Double> masses : this.fingerprintToMasses) {
			size += masses.size();
		}
		return size;
	}
	
	public FastBitArray getFingerprint(int index) {
		return this.fingerprints.get(index);
	}
	
	public int getFingerprintSize() {
		return this.fingerprints.size();
	}
	
	public void print(FastBitArray fingerprint) {
		int index = this.getIndexOfFingerprint(fingerprint);
		LinkedList<Double> masses = this.fingerprintToMasses.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i));
		}
	}
	
	public void print(String fp) {
		FastBitArray fingerprint = new FastBitArray(fp);
		int index = this.getIndexOfFingerprint(fingerprint);
		LinkedList<Double> masses = this.fingerprintToMasses.get(index);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i));
		}
	}
	
	public String toString(FastBitArray fingerprint) {
		String string = fingerprint.toString();
		int index = this.getIndexOfFingerprint(fingerprint);
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
	
	public String toString(int index) {
		String string = this.fingerprints.get(index).toString();
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
	
	public String toStringIDs(int index) {
		String string = this.fingerprints.get(index).toStringIDs();
		for(int i = 0; i < this.fingerprintToMasses.get(index).size(); i++) {
			string += " " + this.fingerprintToMasses.get(index).get(i);
		}
		return string;
	}
}
