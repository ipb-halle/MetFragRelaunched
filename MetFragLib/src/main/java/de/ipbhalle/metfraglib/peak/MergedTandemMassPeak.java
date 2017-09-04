package de.ipbhalle.metfraglib.peak;

public class MergedTandemMassPeak extends TandemMassPeak {

	protected java.util.ArrayList<Double> intensities;
	protected java.util.ArrayList<Integer> spectraIDs;
	
	public MergedTandemMassPeak(double mass, double absoluteIntensity) {
		super(mass, absoluteIntensity);
		this.intensities = new java.util.ArrayList<Double>();
		this.spectraIDs = new java.util.ArrayList<Integer>();
	}

	public java.util.ArrayList<Double> getIntensities() {
		return intensities;
	}

	public void setIntensities(java.util.ArrayList<Double> intensities) {
		this.intensities = intensities;
	}

	public java.util.ArrayList<Integer> getSpectraIDs() {
		return spectraIDs;
	}

	public void setSpectraIDs(java.util.ArrayList<Integer> spectraIDs) {
		this.spectraIDs = spectraIDs;
	}
	
}
