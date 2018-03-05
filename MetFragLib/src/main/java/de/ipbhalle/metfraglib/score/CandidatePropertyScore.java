package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidatePropertyScore extends AbstractScore {
	
	public CandidatePropertyScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.calculationFinished = true;
		this.isUserDefinedPropertyScore = true;
	}
	
	public void calculate() {
		
	}

	public boolean isCandidatePropertyScore() {
		return true;
	}
	
	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}
	
	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
