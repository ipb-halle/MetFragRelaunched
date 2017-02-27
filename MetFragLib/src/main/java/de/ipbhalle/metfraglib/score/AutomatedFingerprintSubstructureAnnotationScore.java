package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;
import de.ipbhalle.metfraglib.substructure.MassToFingerprints;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;

public class AutomatedFingerprintSubstructureAnnotationScore extends AbstractScore {

	protected ICandidate candidate;
	
	public AutomatedFingerprintSubstructureAnnotationScore(Settings settings) {
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
	
	public Double[] calculateSingleMatch(IMatch match) {
		//add fragments to background data
		// 1. get the peak fingerprint collection from the training
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		// 2. extract fingerprint list for current matched mass
		// the mass object can be used directly as it was used for initialisation of the list 
		PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMatchedPeak().getMass());
		if(peakToFingerprintGroupList != null) {
			// 3. use this list to filter background fingerprints
			MassToFingerprints massToFingerprints = (MassToFingerprints)this.settings.get(VariableNames.PEAK_TO_BACKGROUND_FINGERPRINTS_NAME);
			FragmentList fragmentList = match.getMatchedFragmentList();
			for(int i = 0; i < fragmentList.getNumberElements(); i++) {
				String currentFingerprint = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(fragmentList.getElement(i).getStructureAsIAtomContainer()));
				// check whether fingerprint was observed for current peak mass in the training data
				if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
					// if not add the fingerprint to background by addFingerprint function
					// addFingerprint checks also whether fingerprint was already added
					synchronized(massToFingerprints) {
						massToFingerprints.addFingerprint(match.getMatchedPeak().getMass(), currentFingerprint);
					}
				}
			}
		}
		return new Double[] {0.0, null};
	}
	
	@Override
	public void postCalculate() {
		this.value = 0.0;
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MatchList matchList = this.candidate.getMatchList();
		MassToFingerprints massToFingerprints = (MassToFingerprints)this.settings.get(VariableNames.PEAK_TO_BACKGROUND_FINGERPRINTS_NAME);
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			IMatch currentMatch = matchList.getMatchByMass(currentMass); 
			if(currentMatch == null) {
				this.value += Math.log((Double)this.settings.get(VariableNames.FINGERPRINT_ANNOTATION_BETA_VALUE_NAME) / (Double)this.settings.get(VariableNames.BETA_PSEUDO_COUNT_DENOMINATOR_VALUE_NAME));
			} else {
				String currentFingerprint = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(currentMatch.getBestMatchedFragment().getStructureAsIAtomContainer()));
				// |F|
				double numberPseudoCountsFingerprint = (massToFingerprints.getSize(currentMass) + 1.0);
				double alpha = 1.0 / numberPseudoCountsFingerprint;
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double p_f_given_m = (peakToFingerprintGroupList.getMatchingProbability(currentFingerprint) + alpha) / (peakToFingerprintGroupList.getSumProbabilites() + (alpha * numberPseudoCountsFingerprint)); 
				this.value += Math.log(p_f_given_m);
			}
		}
 	}
	
	@Override
	public void nullify() {
		super.nullify();
	}

	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
