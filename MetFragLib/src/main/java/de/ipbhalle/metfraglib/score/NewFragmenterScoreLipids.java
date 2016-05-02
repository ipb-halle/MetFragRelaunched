package de.ipbhalle.metfraglib.score;

import java.util.Arrays;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.DefaultFragmentToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentFormulaToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * score created for the new community fragmenter
 * combines peakScore and bond energy score in one single summand
 */
public class NewFragmenterScoreLipids extends NewFragmenterScore {

	protected java.util.HashMap<IPeak, Integer> rankedPeaksByIntensity;
	
	public NewFragmenterScoreLipids(Settings settings) {
		super(settings);
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
//		this.maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.measuredPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		this.interimResultsCalculated = false;
		this.usesPiecewiseCalculation = true;
		this.initialise((IList)settings.get(VariableNames.PEAK_LIST_NAME));
	} 

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	public void calculate() {
		this.matchList = (MatchList)settings.get(VariableNames.MATCH_LIST_NAME);
		this.value = new Double(0);
		this.bestFragmentIndeces = new int[this.matchList.getNumberElements()];
		this.optimalValues = new double[this.matchList.getNumberElements()];
		for(int i = 0; i < this.matchList.getNumberElements(); i++) {
			DefaultFragmentToPeakMatch currentMatch = null;
			if(this.matchList.getElement(i) instanceof FragmentMassToPeakMatch) 
				currentMatch = (FragmentMassToPeakMatch)this.matchList.getElement(i);
			else 
				currentMatch = (FragmentFormulaToPeakMatch)this.matchList.getElement(i);
			FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
			double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
			double peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
					* Math.pow(this.rankedPeaksByIntensity.get(currentMatch.getMatchedPeak()), this.BETA);
			for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
				IFragment currentFragment = currentFragmentList.getElement(ii);
				/*
				 * check if current fragment is valid based on the tree depth
				 */
//				if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
				int[] brokenBondIndeces = currentFragment.getBrokenBondIndeces();
				double energyOfFragment = 0.0;
				for(int bondIndex : brokenBondIndeces) {
					energyOfFragment += this.bondEnergies[bondIndex];
				}
				energyOfFragment += Math.abs(currentMatch.getNumberOfHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
				/*
				 * assign optimal bondenergy and store best fragment
				 */
				if(energyOfFragment < minimumEnergyPerMatch) {
					minimumEnergyPerMatch = energyOfFragment;
					this.bestFragmentIndeces[i] = ii;
				}
			}
			currentMatch.initialiseBestMatchedFragment(this.bestFragmentIndeces[i]);
			this.value += peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA);
			this.optimalValues[i] = minimumEnergyPerMatch;
		}
		this.calculationFinished = true;
	}

	public Double[] calculateSingleMatch(IMatch match) {
		DefaultFragmentToPeakMatch currentMatch = null;
		if(match instanceof FragmentMassToPeakMatch) 
			currentMatch = (FragmentMassToPeakMatch)match;
		else 
			currentMatch = (FragmentFormulaToPeakMatch)match;
		FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
		double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
		double peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
				* Math.pow(this.rankedPeaksByIntensity.get(currentMatch.getMatchedPeak()), this.BETA);
		for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
			IFragment currentFragment = currentFragmentList.getElement(ii);
			/*
			 * check if current fragment is valid based on the tree depth
			 */
//			if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
			int[] brokenBondIndeces = currentFragment.getBrokenBondIndeces();
			double energyOfFragment = 0.0;
			for(int bondIndex : brokenBondIndeces) {
				energyOfFragment += this.bondEnergies[bondIndex];
			}
			energyOfFragment += Math.abs(currentMatch.getNumberOfHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
			/*
			 * assign optimal bondenergy and store best fragment
			 */
			if(energyOfFragment < minimumEnergyPerMatch) {
				minimumEnergyPerMatch = energyOfFragment;
			}
		}
		this.calculationFinished = true;
		return new Double[]{peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA), minimumEnergyPerMatch};
	}
	
	private void initialise(IList peakList) {
		this.rankedPeaksByIntensity = new java.util.HashMap<IPeak, Integer>();
		double[] peakIntensities = new double[peakList.getNumberElements()];
		for(int i = 0; i < peakIntensities.length; i++) {
			try {
				peakIntensities[i] = ((IPeak)peakList.getElement(i)).getIntensity();
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
		}
		Arrays.sort(peakIntensities);
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			IPeak currentpeak = (IPeak)peakList.getElement(i);
			double intensity = 0.0;
			try {
				intensity = currentpeak.getIntensity();
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
			for(int j = peakIntensities.length - 1; j >= 0; j--) {
				if(peakIntensities[j] == intensity) {
					this.rankedPeaksByIntensity.put(currentpeak, j + 1);
					break;
				}
			}
		}
		
		
	}
	
}
