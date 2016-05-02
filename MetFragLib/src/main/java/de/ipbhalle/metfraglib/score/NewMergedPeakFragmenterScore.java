package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.DefaultFragmentToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentFormulaToPeakMatch;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.MergedTandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * score created for the new community fragmenter
 * combines peakScore and bond energy score in one single summand
 */
public class NewMergedPeakFragmenterScore extends NewFragmenterScore {

	protected double[] scoresRandomSpectra;
	
	public NewMergedPeakFragmenterScore(Settings settings) {
		super(settings);
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
//		this.maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.measuredPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		this.scoresRandomSpectra = new double[(Integer)this.settings.get(VariableNames.NUMBER_RANDOM_SPECTRA_NAME)];
		this.interimResultsCalculated = false;
		this.usesPiecewiseCalculation = true;
	}
	
	public void calculate() {
		this.matchList = (MatchList)settings.get(VariableNames.MATCH_LIST_NAME);
		this.value = new Double(0);
		this.bestFragmentIndeces = new int[this.matchList.getNumberElements()];
		this.optimalValues = new double[this.matchList.getNumberElements()];
		for(int i = 0; i < this.matchList.getNumberElements(); i++) {
			DefaultFragmentToPeakMatch currentMatch = null;
			boolean isMatchOfQuerySpectrum = true;
			if(this.matchList.getElement(i) instanceof FragmentMassToPeakMatch) 
				currentMatch = (FragmentMassToPeakMatch)this.matchList.getElement(i);
			else 
				currentMatch = (FragmentFormulaToPeakMatch)this.matchList.getElement(i);
			double intensity = 0.0;
			try {
				intensity = currentMatch.getMatchedPeak().getIntensity();
			}
			catch(RelativeIntensityNotDefinedException e) {
				//if the match is not caused by a peak of the query spectrum
				isMatchOfQuerySpectrum = false;
				intensity = 0.0;
			}
			
			MergedTandemMassPeak matchedPeak = (MergedTandemMassPeak)currentMatch.getMatchedPeak();
			FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
			double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
			
			/*
			 * get all intensities of random spectra of this peak 
			 */
			java.util.Vector<Double> intensities = matchedPeak.getIntensities();
			java.util.Vector<Integer> spectraIDs = matchedPeak.getSpectraIDs();
			
			double peakScore = Math.pow((matchedPeak.getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
					* Math.pow(intensity, this.BETA);
			double[] peakScores = new double[intensities.size()];
			for(int k = 0; k < intensities.size(); k++) {
				peakScores[k] =  Math.pow((matchedPeak.getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
						* Math.pow(intensities.get(k), this.BETA);
			}

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
					if(isMatchOfQuerySpectrum) this.bestFragmentIndeces[i] = ii;
				}
			}
			if(isMatchOfQuerySpectrum) { 
				currentMatch.initialiseBestMatchedFragment(this.bestFragmentIndeces[i]);
				this.value += peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA);
				this.optimalValues[i] = minimumEnergyPerMatch;
			}
			for(int k = 0; k < spectraIDs.size(); k++) {
				this.scoresRandomSpectra[spectraIDs.get(k)] += peakScores[k] / Math.pow(minimumEnergyPerMatch, this.GAMMA);
			}
		}
		int betterEqualScores = 0;
		for(int i = 0; i < this.scoresRandomSpectra.length; i++) {
			if(this.scoresRandomSpectra[i] >= this.value) betterEqualScores++;
		}
		this.scoredCandidate.setProperty("p-value", (double)betterEqualScores / (double)this.scoresRandomSpectra.length);
		this.calculationFinished = true;
	}

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	public double[] getScoresRandomSpectra() {
		return this.scoresRandomSpectra;
	}
}
