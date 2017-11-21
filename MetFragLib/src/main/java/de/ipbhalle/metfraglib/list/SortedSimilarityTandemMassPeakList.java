package de.ipbhalle.metfraglib.list;

import org.openscience.cdk.fingerprint.IBitFingerprint;

public class SortedSimilarityTandemMassPeakList extends SortedTandemMassPeakList {

	protected String inchi;
	protected String inchikey1;
	protected Boolean isPositiveCharge;
	protected String sampleName;
	protected IBitFingerprint fingerprint;
	
	public SortedSimilarityTandemMassPeakList(Double measuredPrecursorMass) {
		super(measuredPrecursorMass);
	}

	public void setMeasuredPrecursorMass(Double measuredPrecursorMass) {
		this.measuredPrecursorMass = measuredPrecursorMass;
	}
	
	public String getInchi() {
		return inchi;
	}

	public void setInchi(String inchi) {
		this.inchi = inchi;
	}

	public IBitFingerprint getFingerprint() {
		return fingerprint;
	}

	public void setFingerprint(IBitFingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}

	public String getInchikey1() {
		return inchikey1;
	}

	public void setInchikey1(String inchikey1) {
		this.inchikey1 = inchikey1;
	}
	
	public Boolean getIsPositiveCharge() {
		return isPositiveCharge;
	}

	public void setIsPositiveCharge(Boolean isPositiveCharge) {
		this.isPositiveCharge = isPositiveCharge;
	}

	public String getSampleName() {
		return sampleName;
	}

	public void setSampleName(String sampleName) {
		this.sampleName = sampleName;
	}

	public boolean isPartlyInitialised() {
		return this.inchikey1 != null 
				&& this.isPositiveCharge != null;
	}
	
	public boolean isFullyInitialised() {
		return this.inchi != null && this.inchikey1 != null 
				&& this.isPositiveCharge != null && this.measuredPrecursorMass != null && this.measuredPrecursorMass != 0.0;
	}
}
