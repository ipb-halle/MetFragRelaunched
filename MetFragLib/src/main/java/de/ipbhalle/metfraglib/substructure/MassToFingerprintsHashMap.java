package de.ipbhalle.metfraglib.substructure;

import java.util.HashMap;
import java.util.LinkedList;

import de.ipbhalle.metfraglib.FastBitArray;

public class MassToFingerprintsHashMap {

	private HashMap<Double, LinkedList<FastBitArray>> massesToFingerprints; 
	
	public MassToFingerprintsHashMap() {
		this.massesToFingerprints = new HashMap<Double, LinkedList<FastBitArray>>();
	}
	
	public void addFingerprint(Double mass, FastBitArray fingerprint) {
		if(this.massesToFingerprints.containsKey(mass)) {
			this.addToFingerprints(fingerprint, this.massesToFingerprints.get(mass));
		} else {
			LinkedList<FastBitArray> newFingerprintList = new LinkedList<FastBitArray>();
			newFingerprintList.add(fingerprint);
			this.massesToFingerprints.put(mass, newFingerprintList);
		}
	}
	
	public void addFingerprint(Double mass, String fingerprint) {
		this.addFingerprint(mass, new FastBitArray(fingerprint));
	}
	
	protected void addToFingerprints(FastBitArray fingerprint, LinkedList<FastBitArray> fingerprints) {
		if(fingerprints.contains(fingerprint)) return;
		fingerprints.add(fingerprint);
	}
	
	public LinkedList<FastBitArray> getFingerprints(Double mass) {
		return this.massesToFingerprints.get(mass);
	}
	
	public boolean containsMass(Double mass) {
		return this.massesToFingerprints.containsKey(mass);
	}
	
	public boolean containsFingerprint(FastBitArray fingerprint, LinkedList<FastBitArray> fingerprints) {
		if(fingerprints == null) return false;
		return fingerprints.contains(fingerprint);
	}
	
	public boolean contains(Double mass, FastBitArray fingerprint) {
		return this.containsFingerprint(fingerprint, this.massesToFingerprints.get(mass));
	}
	
	public int getSizeOverall(Double mass) {
		LinkedList<FastBitArray> fingerprints = this.massesToFingerprints.get(mass);
		if(fingerprints == null) return 0;
		return fingerprints.size();
	}

	public int getSizeMatched(Double mass) {
		LinkedList<FastBitArray> fingerprints = this.massesToFingerprints.get(mass);
		if(fingerprints == null) return 0;
		int count = 0;
		for(int i = 0; i < fingerprints.size(); i++) {
			if(fingerprints.get(i).getSize() != 1) count++;
		}
		return count;
	}

	public int getSizeNonMatched(Double mass) {
		LinkedList<FastBitArray> fingerprints = this.massesToFingerprints.get(mass);
		if(fingerprints == null) return 0;
		int count = 0;
		for(int i = 0; i < fingerprints.size(); i++) 
			if(fingerprints.get(i).getSize() == 1) count++;
		return count;
	}
	
	public int getOverallSize() {
		int size = 0;
		java.util.Iterator<Double> it = this.massesToFingerprints.keySet().iterator();
		while(it.hasNext()) {
			size += this.massesToFingerprints.get(it.next()).size();
		}
		return size;
	}

	public int getOverallMatchedSize() {
		int size = 0;
		java.util.Iterator<Double> it = this.massesToFingerprints.keySet().iterator();
		while(it.hasNext()) {
			LinkedList<FastBitArray> bitArrays = this.massesToFingerprints.get(it.next());
			java.util.Iterator<FastBitArray> it2 = bitArrays.iterator();
			while(it2.hasNext()) {
				FastBitArray bitArray = it2.next();
				if(bitArray.getSize() != 1) size++;
			}
		}
		return size;
	}

	public int getOverallNonMatchedSize() {
		int size = 0;
		java.util.Iterator<Double> it = this.massesToFingerprints.keySet().iterator();
		while(it.hasNext()) {
			LinkedList<FastBitArray> bitArrays = this.massesToFingerprints.get(it.next());
			java.util.Iterator<FastBitArray> it2 = bitArrays.iterator();
			while(it2.hasNext()) {
				FastBitArray bitArray = it2.next();
				if(bitArray.getSize() == 1) size++;
			}
		}
		return size;
	}
	
	public void print(Double mass) {
		LinkedList<FastBitArray> fingerprints = this.massesToFingerprints.get(mass);
		System.out.println(mass + ":");
		for(int i = 0; i < fingerprints.size(); i++) {
			System.out.println("-> " + fingerprints.get(i).toStringIDs());
		}
	}
}
