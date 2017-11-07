package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.FastBitArray;

public class MassFingerprintMatch {

	private Double mass;
	private FastBitArray fingerprint;
	
	
	public MassFingerprintMatch(Double mass, FastBitArray fingerprint) {
		super();
		this.mass = mass;
		this.fingerprint = fingerprint;
	}
	
	public Double getMass() {
		return this.mass;
	}
	public void setMass(Double mass) {
		this.mass = mass;
	}
	public FastBitArray getFingerprint() {
		return this.fingerprint;
	}
	public void setFingerprint(FastBitArray fingerprint) {
		this.fingerprint = fingerprint;
	} 
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.mass);
		builder.append(":");
		builder.append(this.fingerprint.toString());
		return builder.toString();
	}
}
