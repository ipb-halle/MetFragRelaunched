package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.match.HDFragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * score created for the
 * combines peakScore and bond energy score in one single summand
 * used for HD matches
 */
public class HDNewFragmenterScore extends NewFragmenterScore {
	
	public HDNewFragmenterScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
		this.measuredPrecursorMass = (Double)settings.get(VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME);
		this.calculationFinished = true;
		this.usesPiecewiseCalculation = true;
	}
	
	/**
	 * 
	 */
	public Double[] calculateSingleMatch(IMatch currentMatch) {
		if(!(currentMatch instanceof HDFragmentMassToPeakMatch)) return new Double[] {0.0, null};
		FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
		double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
		double peakScore = 0.0;
		try {
			peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
					* Math.pow(currentMatch.getMatchedPeak().getIntensity(), this.BETA);
		} catch (RelativeIntensityNotDefinedException e) {
			e.printStackTrace();
		}
		int indexOfBestFragment = -1;
		for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
			IFragment currentFragment = currentFragmentList.getElement(ii);
			/*
			 * check if current fragment is valid based on the tree depth
			 */
//			if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
			int[] brokenBondIndeces = ((AbstractTopDownBitArrayFragment)currentFragment).getBrokenBondsBitArray().getSetIndeces();
			double energyOfFragment = 0.0;
			for(int bondIndex : brokenBondIndeces) {
				energyOfFragment += this.bondEnergies[bondIndex];
			}
			energyOfFragment += Math.abs(currentMatch.getNumberOfOverallHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
			/*
			 * assign optimal bondenergy and store best fragment
			 */
			if(energyOfFragment < minimumEnergyPerMatch) {
				minimumEnergyPerMatch = energyOfFragment;
				indexOfBestFragment = ii;
			}
		}
		if(indexOfBestFragment != -1) currentMatch.initialiseBestMatchedFragment(indexOfBestFragment);
		return new Double[] {peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA), minimumEnergyPerMatch};
	}
	
}
