package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
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
public class NewFragmenterUniqueFormulaScore extends NewFragmenterScore {
	
	public NewFragmenterUniqueFormulaScore(Settings settings) {
		super(settings);
		this.scoredCandidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
//		this.maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.measuredPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		this.interimResultsCalculated = false;
		this.usesPiecewiseCalculation = true;
	}
	
	public void calculate() {
		this.matchList = (MatchList)settings.get(VariableNames.MATCH_LIST_NAME);
		this.value = Double.valueOf(0);
		
		java.util.ArrayList<Double> scores = new java.util.ArrayList<Double>();
		java.util.ArrayList<Integer> fragmentIds = new java.util.ArrayList<Integer>();
		java.util.ArrayList<Integer> fragmentIndeces = new java.util.ArrayList<Integer>();
		java.util.ArrayList<IFragment> fragments = new java.util.ArrayList<IFragment>();
		java.util.ArrayList<DefaultFragmentToPeakMatch> matches = new java.util.ArrayList<DefaultFragmentToPeakMatch>();
		java.util.ArrayList<Byte> hydrogenDifferences = new java.util.ArrayList<Byte>();
		
		for(int i = 0; i < this.matchList.getNumberElements(); i++) {
			DefaultFragmentToPeakMatch currentMatch = null;
			if(this.matchList.getElement(i) instanceof FragmentMassToPeakMatch) 
				currentMatch = (FragmentMassToPeakMatch)this.matchList.getElement(i);
			else 
				currentMatch = (FragmentFormulaToPeakMatch)this.matchList.getElement(i);
			FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
			double peakScore = 0.0;
			try {
				peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
						* Math.pow(currentMatch.getMatchedPeak().getIntensity(), this.BETA);
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
			for(int ii = 0; ii < currentFragmentList.getNumberElements(); ii++) {
				IFragment currentFragment = currentFragmentList.getElement(ii);
//				if(currentFragment.getTreeDepth() > this.maximumTreeDepth) continue;
				int[] brokenBondIndeces = currentFragment.getBrokenBondIndeces();
				double energyOfFragment = 0.0;
				for(int bondIndex : brokenBondIndeces) {
					energyOfFragment += this.bondEnergies[bondIndex];
				}
				energyOfFragment += Math.abs(currentMatch.getNumberOfHydrogensDifferToPeakMass(ii)) * this.WEIGHT_HYDROGEN_PENALTY;
				double currentScore = peakScore / Math.pow(energyOfFragment, this.GAMMA);
				int index = fragmentIds.indexOf(currentFragment.getID());
				if(index != -1) {
					double lastScore = scores.get(index);
					if(lastScore < currentScore) {
						scores.remove(index);
						fragmentIds.remove(index);
						fragments.remove(index);
						matches.remove(index);
						hydrogenDifferences.remove(index);
						fragmentIndeces.remove(index);
						index = 0;
						while(scores.size() > index && scores.get(index) > currentScore) index++;
						scores.add(index, currentScore);
						fragmentIds.add(index, currentFragment.getID());
						fragments.add(index, currentFragment);
						matches.add(index, currentMatch);
						hydrogenDifferences.add(index, currentMatch.getNumberOfHydrogensDifferToPeakMass(ii));
						fragmentIndeces.add(index, ii);
					}
				}
				else {
					index = 0;
					while(scores.size() > index && scores.get(index) > currentScore) index++;
					scores.add(index, currentScore);
					fragmentIds.add(index, currentFragment.getID());
					fragments.add(index, currentFragment);
					matches.add(index, currentMatch);
					hydrogenDifferences.add(index, currentMatch.getNumberOfHydrogensDifferToPeakMass(ii));
					fragmentIndeces.add(index, ii);
				}
			}
		}

		java.util.ArrayList<Integer> validAndBestFragmentMatches = new java.util.ArrayList<Integer>();
		for(int i = 0; i < scores.size(); i++) {
			boolean validAndBestMatch = true;
			for(int k = 0; k < validAndBestFragmentMatches.size(); k++) {
				if(fragmentIds.get(validAndBestFragmentMatches.get(k)) == fragmentIds.get(i)) {
					validAndBestMatch = false;
				}
				if(matches.get(validAndBestFragmentMatches.get(k)).getMatchedPeak().getMass() == matches.get(i).getMatchedPeak().getMass()) {
					validAndBestMatch = false;
				}
			}
			if(!validAndBestMatch) continue;
			int index = 0;
			/*
			 * insert the valid and best match sorted by mass of the peak
			 */
			while(index < validAndBestFragmentMatches.size() && matches.get(i).getMatchedPeak().getMass() > matches.get(validAndBestFragmentMatches.get(index)).getMatchedPeak().getMass())
				index++;
			validAndBestFragmentMatches.add(index, i);
		}
		
		MatchList cleanedMatchList = new MatchList();
		this.bestFragmentIndeces = new int[validAndBestFragmentMatches.size()];
		this.optimalValues = new double[validAndBestFragmentMatches.size()];
		for(int i = 0; i < validAndBestFragmentMatches.size(); i++) {
			matches.get(validAndBestFragmentMatches.get(i)).initialiseBestMatchedFragment(fragmentIndeces.get(validAndBestFragmentMatches.get(i)));
			this.value += scores.get(validAndBestFragmentMatches.get(i));
			this.optimalValues[i] = scores.get(validAndBestFragmentMatches.get(i));
			cleanedMatchList.addElement(matches.get(validAndBestFragmentMatches.get(i)));
			/*
			System.out.println(
				scores.get(validAndBestFragmentMatches.get(i)) + "\t" + 
				fragmentIds.get(validAndBestFragmentMatches.get(i)) + "\t" + 
				matches.get(validAndBestFragmentMatches.get(i)).getMatchedPeak().getMass() + "\t\t" +
				fragments.get(validAndBestFragmentMatches.get(i)).getMolecularFormula() + "\t" +
				hydrogenDifferences.get(validAndBestFragmentMatches.get(i))
			);
			*/
		}
		this.settings.set(VariableNames.MATCH_LIST_NAME, cleanedMatchList);
		this.calculationFinished = true;
	}

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		DefaultFragmentToPeakMatch currentMatch = null;
		if(match instanceof FragmentMassToPeakMatch) 
			currentMatch = (FragmentMassToPeakMatch)match;
		else 
			currentMatch = (FragmentFormulaToPeakMatch)match;
		FragmentList currentFragmentList = currentMatch.getMatchedFragmentList();
		double minimumEnergyPerMatch = (double)Integer.MAX_VALUE;
		double peakScore = 0.0;
		try {
			peakScore = Math.pow((currentMatch.getMatchedPeak().getMass() / this.measuredPrecursorMass) * 10.0, this.ALPHA) 
					* Math.pow(currentMatch.getMatchedPeak().getIntensity(), this.BETA);
		} catch (RelativeIntensityNotDefinedException e) {
			e.printStackTrace();
		}
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
		return new Double[] {peakScore / Math.pow(minimumEnergyPerMatch, this.GAMMA), minimumEnergyPerMatch};
	}
}
