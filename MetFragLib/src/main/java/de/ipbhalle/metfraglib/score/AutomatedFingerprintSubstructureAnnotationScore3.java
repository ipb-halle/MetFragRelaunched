package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintToMasses;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;

public class AutomatedFingerprintSubstructureAnnotationScore3 extends AbstractScore {

	protected ICandidate candidate;
	
	public AutomatedFingerprintSubstructureAnnotationScore3(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		this.value = 0.0;
		this.calculationFinished = true;
	}

	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	/**
	 * collects the background fingerprints
	 */
	public Double[] calculateSingleMatch(IMatch match) {
		//add fragments to background data
		// 1. get the peak fingerprint collection from the training
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		// 2. extract fingerprint list for current matched mass
		// the mass object can be used directly as it was used for initialisation of the list 
		PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMatchedPeak().getMass());
		// get foregound fingerprint observations
		if(peakToFingerprintGroupList != null) {
			// 3. use this list to filter background fingerprints
			FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)this.settings.get(VariableNames.PEAK_TO_BACKGROUND_MASSES_NAME);
			FragmentList fragmentList = match.getMatchedFragmentList();
			for(int i = 0; i < fragmentList.getNumberElements(); i++) {
				BitArray currentFingerprint = new BitArray(MoleculeFunctions.getNormalizedFingerprint(fragmentList.getElement(i)));
			//	if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
				// check whether fingerprint was observed for current peak mass in the training data
				if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
					// if not add the fingerprint to background by addFingerprint function
					// addFingerprint checks also whether fingerprint was already added
					synchronized(fingerprintToMasses) {
						fingerprintToMasses.addMass(currentFingerprint, match.getMatchedPeak().getMass());
					}
				}
			}
		}
		return new Double[] {0.0, null};
	}
	
	@Override
	public void singlePostCalculate() {
		//this.value = 0.0;
		this.value = 1.0;
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MatchList matchList = this.candidate.getMatchList();
		FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)this.settings.get(VariableNames.PEAK_TO_BACKGROUND_MASSES_NAME);
		int matches = 0;
		// get foreground fingerprint observations (m_f_observed)
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			// get f_m_observed
			PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			IMatch currentMatch = matchList.getMatchByMass(currentMass);
			double alphaValue = (Double)this.settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);
			double betaValue = (Double)this.settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);
			double toMultiply = 1.0;
			// todo
			double f_m_unseen = 0.0;
			//(fingerprintToMasses.getSize(currentFingerprint));
			if(currentMatch == null) {
				toMultiply = betaValue;
			} else {
				BitArray currentFingerprint = new BitArray(MoleculeFunctions.getNormalizedFingerprint(currentMatch.getBestMatchedFragment()));
				// ToDo: at this stage try to check all fragments not only the best one
				matches++;
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
				// |F|
				toMultiply = matching_prob + alphaValue;
			}
			this.value *= (toMultiply / (peakToFingerprintGroupList.getSumProbabilites() + (alphaValue * (f_m_unseen + peakToFingerprintGroupList.getNumberElements()))));
		}
		if(this.value == 1.0) this.value = 0.0;
		this.candidate.setProperty("AutomatedFingerprintSubstructureAnnotationScore_Matches", matches);
 	}
	
	@Override
	public void nullify() {
		super.nullify();
	}

	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
