package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.peak.Peak;

public class DefaultPeakList extends AbstractPeakList {

	/**
	 * return minimum mass value of the current peak list
	 * 
	 * @return
	 */
	public double getMinimumMassValue() {
		double minimumMassValue = (double)Integer.MAX_VALUE;
		for(int i = 0; i < this.list.size(); i++) {
			double currentMassValue = ((IPeak)this.list.get(i)).getMass();
			if(currentMassValue < minimumMassValue) minimumMassValue = currentMassValue;
		}
		return minimumMassValue;
	}
	
	public void initialiseMassLimits(double relativeMassDeviation, double absoluteMassDeviation) {
		for(int i = 0; i < this.list.size(); i++) 
			((TandemMassPeak)this.list.get(i)).setMassLimitsByMassDeviations(relativeMassDeviation, absoluteMassDeviation);
	}
	
	public void calculateRelativeIntensities(double maximumRelativeIntensity) {
		double maximumAbsoluteIntensity = 0.0;
		for(int i = 0; i < this.list.size(); i++) {
			Peak currentTandemMassPeak = (Peak)this.list.get(i);
			if(currentTandemMassPeak.getAbsoluteIntensity() > maximumAbsoluteIntensity) 
				maximumAbsoluteIntensity = currentTandemMassPeak.getAbsoluteIntensity();
		}
		double weightFactor = maximumRelativeIntensity / maximumAbsoluteIntensity;
		for(int i = 0; i < this.list.size(); i++) {
			Peak currentTandemMassPeak = (Peak)this.list.get(i);
			currentTandemMassPeak.setRelativeIntensity(MathTools.round(currentTandemMassPeak.getAbsoluteIntensity() * weightFactor, 1));
		}
	}
	
	public int getNumberPeaksUsed() {
		return this.list.size();
	}

	@Override
	public boolean containsMass(double mass, double mzppm, double mzabs) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		
		for(int i = 0; i < this.list.size(); i++) 
		{
			double currentMass = ((Peak)this.list.get(i)).getMass();
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				return true;
			}
			
		}
		return false;
	}
	
}
