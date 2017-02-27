package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.settings.Settings;

public interface IScore {

	public Double getValue();
	
	public void setValue(double value);

	public boolean calculationFinished();
	
	public boolean hasInterimResults();
	
	public boolean isInterimResultsCalculated();
	
	public boolean isUsesPiecewiseCalculation();
	
	public boolean isUserDefinedPropertyScore();
	
	/**
	 * calculates the values of the score
	 * 
	 * @param object
	 */
	public void calculate() throws Exception;

	/**
	 * re-calculated score after all candidates have been processed
	 * performed in CombinedMetFragProcess
	 * 
	 * @throws Exception
	 */
	public void postCalculate() throws Exception;
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public void shallowNullify();
	
	public String toString();
	
	public double[] getOptimalValues();
	
	public void setOptimalValues(double[] optimalValues);
	
	public String getOptimalValuesToString();
	
	public boolean isBetterValue(double value);
	
	/**
	 * set metfrag settings
	 */
	public void setSettings(Settings settings);

	/*
	 * return calculated score value and the attached optimal value
	 */
	public Double[] calculateSingleMatch(IMatch match);
	
	/**
	 * normalises scores of the same type and returns normalised values as double array
	 * 
	 * @param scores
	 * @return
	 */
	public double[] normalise(Double[] scores);
	
}
