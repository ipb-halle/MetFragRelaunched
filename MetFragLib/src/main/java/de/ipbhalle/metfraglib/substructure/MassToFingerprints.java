package de.ipbhalle.metfraglib.substructure;

import java.util.Vector;

public class MassToFingerprints {

	private Vector<Vector<String>> massesToFingerprints; 
	private Vector<Double> masses;
	
	public MassToFingerprints() {
		this.massesToFingerprints = new Vector<Vector<String>>();
		this.masses = new Vector<Double>();
	}
	
	public void addFingerprint(Double mass, String fingerprint) {
		for(int i = 0; i < this.masses.size(); i++) {
			if(this.masses.get(i).equals(mass)) {
				this.addToFingerprints(fingerprint, this.massesToFingerprints.get(i));
				return;
			}
			if(this.masses.get(i) > mass) {
				this.masses.add(i, mass);
				this.massesToFingerprints.add(i, new Vector<String>());
				this.massesToFingerprints.get(i).add(fingerprint);
				return;
			}
		}
		this.masses.add(mass);
		this.massesToFingerprints.add(new Vector<String>());
		this.massesToFingerprints.get(this.massesToFingerprints.size() - 1).add(fingerprint);
	}
	
	protected void addToFingerprints(String fingerprint, Vector<String> fingerprints) {
		for(int i = 0; i < fingerprints.size(); i++) {
			int compareResult = fingerprints.get(i).compareTo(fingerprint);
			if(compareResult < 0) {
				fingerprints.add(i, fingerprint);
				return;
			}
			if(compareResult == 0) return;
		}
		fingerprints.add(fingerprint);
	}
	
	public Vector<String> getFingerprints(Double mass) {
		int index = this.getIndexOfMass(mass);
		if(index == -1) return new Vector<String>();
		return this.massesToFingerprints.get(index);
	}
	
	public boolean containsMass(Double mass) {
		for(int i = 0; i < this.masses.size(); i++) {
			if(this.masses.get(i).equals(mass)) return true;
			if(this.masses.get(i) > mass) return false;
		}
		return false;
	}
	
	public boolean containsFingerprint(String fingerprint, Vector<String> fingerprints) {
		for(int i = 0; i < fingerprints.size(); i++) {
			int compareResult = fingerprints.get(i).compareTo(fingerprint);
			if(compareResult < 0) return false;
			if(compareResult == 0) return true;
		}
		return false;
	}
	
	public int getIndexOfMass(Double mass) {
		for(int i = 0; i < this.masses.size(); i++) {
			if(this.masses.get(i).equals(mass)) return i;
			if(this.masses.get(i) > mass) return -1;
		}
		return -1;
	}
	
	public boolean contains(Double mass, String fingerprint) {
		int index = this.getIndexOfMass(mass);
		if(index == -1) return false;
		return this.containsFingerprint(fingerprint, this.massesToFingerprints.get(index));
	}
	
	public int getSize(Double mass) {
		int index = this.getIndexOfMass(mass);
		if(index == -1) return 0;
		return this.massesToFingerprints.get(index).size();
	}
	
	public int getOverallSize() {
		int size = 0;
		for(Vector<String> fingerprints : massesToFingerprints) {
			size += fingerprints.size();
		}
		return size;
	}
	
	public void print(Double mass) {
		int index = this.getIndexOfMass(mass);
		Vector<String> fingerprints = this.massesToFingerprints.get(index);
		System.out.println(mass + ":");
		for(int i = 0; i < fingerprints.size(); i++) {
			System.out.println("-> " + fingerprints.get(i));
		}
	}
}
