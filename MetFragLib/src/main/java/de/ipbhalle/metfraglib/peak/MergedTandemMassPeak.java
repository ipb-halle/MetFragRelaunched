package de.ipbhalle.metfraglib.peak;

public class MergedTandemMassPeak extends TandemMassPeak {

	protected java.util.Vector<Double> intensities;
	protected java.util.Vector<Integer> spectraIDs;
	
	public MergedTandemMassPeak(double mass, double absoluteIntensity) {
		super(mass, absoluteIntensity);
		this.intensities = new java.util.Vector<Double>();
		this.spectraIDs = new java.util.Vector<Integer>();
	}

	public java.util.Vector<Double> getIntensities() {
		return intensities;
	}

	public void setIntensities(java.util.Vector<Double> intensities) {
		this.intensities = intensities;
	}

	public java.util.Vector<Integer> getSpectraIDs() {
		return spectraIDs;
	}

	public void setSpectraIDs(java.util.Vector<Integer> spectraIDs) {
		this.spectraIDs = spectraIDs;
	}
	
}
