package de.ipbhalle.metfraglib.substructure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;

public class FingerprintToMassesSimpleHashMap {

	private HashMap<FastBitArray, LinkedList<Double>> fingerprints;
	
	public FingerprintToMassesSimpleHashMap() {
		this.fingerprints = new HashMap<FastBitArray, LinkedList<Double>>();
	}
	
	public void addMass(FastBitArray fingerprint, Double mass) {
		if(this.fingerprints.containsKey(fingerprint)) {
			this.addToMasses(mass, this.fingerprints.get(fingerprint));
			return;
		}
		LinkedList<Double> newList = new LinkedList<Double>();
		newList.add(mass);
		this.fingerprints.put(fingerprint, newList);
	}

	public void addFingerprint(FastBitArray fingerprint) {
		if(this.fingerprints.containsKey(fingerprint)) {
			return;
		} else {
			LinkedList<Double> newList = new LinkedList<Double>();
			this.fingerprints.put(fingerprint, newList);
		}
	}

	public FastBitArray[] getFingerprints() {
		FastBitArray[] fingerprints = new FastBitArray[this.fingerprints.size()];
		Iterator<?> it = this.fingerprints.keySet().iterator();
		int index = 0;
		while(it.hasNext()) {
			fingerprints[index] = (FastBitArray)it.next();
			index++;
		}
		return fingerprints;
	}
	
	public void addMass(FastBitArray fingerprint, Double mass, double mzppm, double mzabs) {
		if(this.fingerprints.containsKey(fingerprint)) {
			this.addToMasses(mass, this.fingerprints.get(fingerprint), mzppm, mzabs);
		} else {
			LinkedList<Double> newList = new LinkedList<Double>();
			newList.add(mass);
			this.fingerprints.put(fingerprint, newList);
		}
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
		if(this.fingerprints.containsKey(fingerprint)) {
			return this.fingerprints.get(fingerprint);
		} 
		return new LinkedList<Double>();
	}
	
	public boolean containsFingerprint(FastBitArray fingerprint) {
		return this.fingerprints.containsKey(fingerprint);
	}
	
	public boolean containsMass(Double mass, LinkedList<Double> masses) {
		return masses.contains(mass);
	}

	public boolean contains(FastBitArray fingerprint, Double mass) {
		if(this.fingerprints.containsKey(fingerprint)) {
			return this.fingerprints.get(fingerprint).contains(mass);
		} 
		return false;
	}
	
	public int getSize(FastBitArray fingerprint) {
		if(this.fingerprints.containsKey(fingerprint)) {
			return this.fingerprints.get(fingerprint).size();
		} 
		return 0;
	}
	
	public int getOverallSize() {
		int size = 0;
		java.util.Iterator<?> it = (java.util.Iterator<?>)this.fingerprints.keySet().iterator();
	    while (it.hasNext()) {
	    	FastBitArray fingerprint = (FastBitArray)it.next();
	    	size += this.fingerprints.get(fingerprint).size();
	    }
	    return size;
	}
	
	public int getFingerprintSize() {
		return this.fingerprints.size();
	}
	
	public void print(FastBitArray fingerprint) {
		if(!this.fingerprints.containsKey(fingerprint)) return;
		LinkedList<Double> masses = this.fingerprints.get(fingerprint);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i));
		}
	}
	
	public void print(String fp) {
		FastBitArray fingerprint = new FastBitArray(fp);
		if(!this.fingerprints.containsKey(fingerprint)) return;
		LinkedList<Double> masses = this.fingerprints.get(fingerprint);
		System.out.println(fingerprint.toStringIDs() + ":");
		for(int i = 0; i < masses.size(); i++) {
			System.out.println("-> " + masses.get(i));
		}
	}
	
	public String toString(FastBitArray fingerprint) {
		if(!this.fingerprints.containsKey(fingerprint)) return "";
		LinkedList<Double> masses = this.fingerprints.get(fingerprint);
		String string = fingerprint.toString();
		for(Double mass : masses) {
			string += " " + mass;
		}
		return string;
	}
	
	public String toStringIDs(FastBitArray fingerprint) {
		if(!this.fingerprints.containsKey(fingerprint)) return "";
		LinkedList<Double> masses = this.fingerprints.get(fingerprint);
		String string = fingerprint.toStringIDs();
		for(Double mass : masses) {
			string += " " + mass;
		}
		return string;
	}
}
