package de.ipbhalle.metfraglib.interfaces;

public interface IBondEnergy {
	
	public double getValue();
	
	public void setValue(double value);

	/**
	 * delete all objects
	 */
	public void nullify();
}
