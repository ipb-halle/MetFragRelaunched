package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;

public interface IPeak {

	public Double getMass();
	
	public double getIntensity() throws RelativeIntensityNotDefinedException;
	
	public void setMass(Double mass);

	public double getAbsoluteIntensity();

	public void setAbsoluteIntensity(double absoluteIntensity);
	
	public void setIntensity(double intensity);
	
	public int getID();
	
	public void setID(int id);
	
	/**
	 * delete all objects
	 */
	public void nullify();
}
