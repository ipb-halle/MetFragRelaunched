package de.ipbhalle.metfraglib.peak;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.IPeak;

public class Peak implements IPeak, Comparable<IPeak> {

	protected Double mass;
	protected double absoluteIntensity;
	protected double relativeIntensity;
	protected boolean relativeIntensityDefined;
	protected int id;
	
	public Peak() {
		
	}
	
	public int getID() {
		return this.id;
	}

	public void setID(int id) {
		this.id = id;
	}
	
	public Peak(double mass, double absoluteIntensity) {
		this.mass = mass;
		this.absoluteIntensity = absoluteIntensity;
	}

	public Peak(double mass, double absoluteIntensity, double relativeIntensity) {
		this.mass = mass;
		this.absoluteIntensity = absoluteIntensity;
		this.relativeIntensity = relativeIntensity;
		this.relativeIntensityDefined = true;
	}
	
	public Double getMass() {
		return this.mass;
	}

	public double getAbsoluteIntensity() {
		return this.absoluteIntensity;
	}

	public void setAbsoluteIntensity(double absoluteIntensity) {
		this.absoluteIntensity = absoluteIntensity;
	}
	
	public double getRelativeIntensity() {
		if(!this.relativeIntensityDefined)
			try {
				throw new RelativeIntensityNotDefinedException();
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
		return this.relativeIntensity;
	}

	public void setMass(Double mass) {
		this.mass = mass;
	}
	
	public double getIntensity() throws RelativeIntensityNotDefinedException {
		if(!this.relativeIntensityDefined)
			throw new RelativeIntensityNotDefinedException();
		return this.relativeIntensity;
	}
	
	public void setIntensity(double intensity) {
		this.relativeIntensity = intensity;
		this.relativeIntensityDefined = true;
	}

	public void setRelativeIntensity(double relativeIntensity) {
		this.relativeIntensity = relativeIntensity;
		this.relativeIntensityDefined = true;
	}
	
	public int compareTo(IPeak o) {
		if(this.mass < o.getMass()) return -1;
		if(this.mass == o.getMass()) return 0;
		return 1;
	}
	
	public void nullify() {
		// TODO Auto-generated method stub
		
	}
}
