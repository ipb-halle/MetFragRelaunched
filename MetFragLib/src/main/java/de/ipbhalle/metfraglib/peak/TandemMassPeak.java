package de.ipbhalle.metfraglib.peak;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.MassLimitsNotDefinedException;

public class TandemMassPeak extends Peak {
	
	/**
	 * mass range based on relative and absolute mass deviation defined
	 * later used for fragment matching
	 */
	private double lowerMassLimit;
	private double upperMassLimit;
	private boolean massLimitsDefined;
	
	public TandemMassPeak(double mass, double absoluteIntensity) {
		super(mass, absoluteIntensity);
	}

	public TandemMassPeak(double mass, double absoluteIntensity, double relativeIntensity) {
		super(mass, absoluteIntensity, relativeIntensity);
	}

	public TandemMassPeak(double mass, double absoluteIntensity, double relativeIntensity, double relativeMassDeviation, double absoluteMassDeviation) {
		super(mass, absoluteIntensity, relativeIntensity);
		this.initialiseMassLimits(relativeMassDeviation, absoluteMassDeviation);
	}
	
	/**
	 * returns 0 if massToCompare matches interval
	 * returns -1 if massToCompare is smaller than the lower bound
	 * returns 1 if massToCompare is greater than the upper bound
	 * 
	 * @param massToCompare
	 * @return
	 */
	public byte matchesToMass(double massToCompare) {
		if(!this.massLimitsDefined)
			try {
				throw new MassLimitsNotDefinedException();
			} catch (MassLimitsNotDefinedException e) {
				e.printStackTrace();
			}
		if(massToCompare >= lowerMassLimit && massToCompare <= upperMassLimit) return 0;
		if(massToCompare < lowerMassLimit) return -1;
		return 1;
	}
	
	public double getUpperMassLimit() {
		return this.upperMassLimit;
	}
	
	public double getLowerMassLimit() {
		return this.lowerMassLimit;
	}
	
	private void initialiseMassLimits(double relativeMassDeviation, double absoluteMassDeviation) {
		this.lowerMassLimit = this.mass - absoluteMassDeviation - MathTools.calculateAbsoluteDeviation(this.mass, relativeMassDeviation);
		this.upperMassLimit = this.mass + absoluteMassDeviation + MathTools.calculateAbsoluteDeviation(this.mass, relativeMassDeviation);
		this.massLimitsDefined = true;
	}
	
	public void setMassLimitsByMassDeviations(double relativeMassDeviation, double absoluteMassDeviation) {
		this.initialiseMassLimits(relativeMassDeviation, absoluteMassDeviation);
	}
}
