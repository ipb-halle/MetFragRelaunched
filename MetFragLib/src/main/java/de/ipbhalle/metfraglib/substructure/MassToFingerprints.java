package de.ipbhalle.metfraglib.substructure;

import java.util.Hashtable;
import java.util.Vector;

public class MassToFingerprints {

	private Hashtable<Double, Vector<String>> massesToFingerprints; 
	
	public MassToFingerprints() {
		this.massesToFingerprints = new Hashtable<Double, Vector<String>>();
	}
	
	public void addFingerprint(Double mass, String fingerprint) {
		if(!this.massesToFingerprints.containsKey(mass)) {
			this.massesToFingerprints.put(mass, new Vector<String>());
		}
		Vector<String> fingerprints = this.massesToFingerprints.get(mass);
		if(!fingerprints.contains(fingerprint)) fingerprints.add(fingerprint);
	}
	
	public Vector<String> getFingerprints(Double mass) {
		if(!this.massesToFingerprints.containsKey(mass)) return new Vector<String>();
		return this.massesToFingerprints.get(mass);
	}
	
	public boolean contains(Double mass, String fingerprint) {
		try {
			return this.massesToFingerprints.get(mass).contains(fingerprint);
		} catch(Exception e) {
			return false;
		}
	}
	
	public int getSize(Double mass) {
		try {
			return this.massesToFingerprints.get(mass).size();
		} catch(Exception e) {
			return 0;
		}
	}
}
