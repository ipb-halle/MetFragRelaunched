package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
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
		return new Double[] {0.0, null};
	}
	
	@Override
	public void singlePostCalculate() {
		//this.value = 0.0;
		this.value = 1.0;
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		double alphaProbability = (double)this.settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);
		double betaProbability = (double)this.settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);
		
		MatchList matchList = this.candidate.getMatchList();
	
		int matches = 0;
		// get foreground fingerprint observations (m_f_observed)
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			// get f_m_observed
			PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			IMatch currentMatch = matchList.getMatchByMass(currentMass);

			//(fingerprintToMasses.getSize(currentFingerprint));
			if(currentMatch == null) {
				this.value *= betaProbability;
			} else {
				FastBitArray currentFingerprint = new FastBitArray(MoleculeFunctions.getNormalizedFingerprint(currentMatch.getBestMatchedFragment()));
				// ToDo: at this stage try to check all fragments not only the best one
				matches++;
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
				// |F|
				if(matching_prob != 0.0) this.value *= matching_prob;
				else this.value *= alphaProbability;
			}
		}
		if(peakToFingerprintGroupListCollection.getNumberElements() == 0) this.value = 0.0;
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
