package de.ipbhalle.metfraglib.additionals;

import de.ipbhalle.metfraglib.parameter.Constants;

public class MathTools {
	
	public static double round(double val, double digits) {
		double multiplier = Math.pow(10.0, digits);
		return (double)Math.round(val * multiplier) / multiplier;
	}
	
	public static double round(double val) {
		return round(val, Constants.DEFAULT_NUMBER_OF_DIGITS_AFTER_ROUNDING);
	}
	
	public static double calculateAbsoluteDeviation(double peak, double ppm)
	{
		return (peak / 1000000.0) * ppm;
	}
	
	public static double getNormalDistributionDensity(double value, double mean, double sd) {
		return (1.0 / Math.sqrt(2.0 * Math.PI * sd * sd)) * Math.exp(-1.0 * (Math.pow(value - mean, 2.0) / (2.0 * sd * sd)));
	}
	
	public static boolean matchMasses(double mass1, double mass2, double ppm, double abs) {
		double mzabs = ((mass1 / 1000000.0) * ppm) + abs;
		if(mass1 - mzabs <= mass2 && (mass2 <= mass1 + mzabs)) return true;
		return false;
	}
}
