package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScore;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractScore implements IScore {

	protected Double value;
	protected double[] optimalValues;
	protected Settings settings;
	protected boolean hasInterimResults;
	protected boolean interimResultsCalculated;
	protected boolean calculationFinished;
	protected boolean isUserDefinedPropertyScore;
	//if score is substantially calculated by 'calculate()' or piecewise by 'calculateSingleMatch(IMatch match)' 
	protected boolean usesPiecewiseCalculation;
	
	public AbstractScore(Settings settings) {
		this.settings = settings;
		this.optimalValues = new double[0];
		this.hasInterimResults = true;
		this.interimResultsCalculated = true;
		this.calculationFinished = false;
		this.isUserDefinedPropertyScore = false;
		this.usesPiecewiseCalculation = false;
	}
	
	public boolean hasInterimResults() {
		return this.hasInterimResults;
	}
	
	public boolean calculationFinished() {
		return this.calculationFinished;
	}
	
	public boolean isUserDefinedPropertyScore() {
		return this.isUserDefinedPropertyScore;
	}
	
	public boolean isUsesPiecewiseCalculation() {
		return this.usesPiecewiseCalculation;
	}
	
	public void setCalculationFinished() {
		this.calculationFinished = true;
	}
	
	public String toString() {
		return this.value + "";
	}

	public void setValue(double value) {
		this.value = value;
	}
	
	public Double getValue() {
		return this.value;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public double[] getOptimalValues() {
		return this.optimalValues;
	}

	public boolean isInterimResultsCalculated() {
		return interimResultsCalculated;
	}

	public void setInterimResultsCalculated(boolean interimResultsCalculated) {
		this.interimResultsCalculated = interimResultsCalculated;
	}

	public void setOptimalValues(double[] optimalValues) {
		this.optimalValues = optimalValues;
	}
	
	public String getOptimalValuesToString() {
		if(this.optimalValues != null && this.optimalValues.length != 0) {
			String optimalValueString = "";
			if(this.optimalValues[0] != 0.0) optimalValueString = this.optimalValues[0] + "";
			for(int iii = 1; iii < this.optimalValues.length; iii++) {
				if(this.optimalValues[iii] != 0.0)
					optimalValueString += ";" + this.optimalValues[iii];
			}
			if(optimalValueString.startsWith(";")) optimalValueString = optimalValueString.substring(1);
			return optimalValueString;
		}
		return "NA";
	}
	
	public abstract Double[] calculateSingleMatch(IMatch match);
	
	public abstract boolean isBetterValue(double value);
	
	public double[] normalise(Double[] values) {
		double[] normalisedScores = new double[values.length];
		Double maxValue = values[0];
		for(int i = 1; i < values.length; i++)
			if(maxValue < values[i]) maxValue = values[i];
		for(int i = 0; i < values.length; i++) {
			if(maxValue != 0.0) normalisedScores[i] = values[i] / maxValue;
			else normalisedScores[i] = 0.0;
		}
		return normalisedScores;
	}
	
	public void nullify() {
		this.optimalValues = null;
		this.value = null;
	}

	public void shallowNullify() {
		this.optimalValues = null;
	}
	
	@Override
	public void postCalculate() {
		return;
	}
}
