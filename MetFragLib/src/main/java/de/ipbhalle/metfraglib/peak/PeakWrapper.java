package de.ipbhalle.metfraglib.peak;

public class PeakWrapper {

	private final Peak peak;
	private double energy;
	
	public PeakWrapper(Peak peak) {
		this.peak = peak;
	}
	
	public void setEnergy(double energy) {
		this.energy = energy;
	}
	
	public double getEnergy() {
		return this.energy;
	}
	
	public Peak getPeak() {
		return this.peak;
	}
}
