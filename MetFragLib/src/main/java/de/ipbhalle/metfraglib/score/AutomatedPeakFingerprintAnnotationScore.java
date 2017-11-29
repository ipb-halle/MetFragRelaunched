package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class AutomatedPeakFingerprintAnnotationScore extends AbstractScore {

	protected ICandidate candidate;
	
	public AutomatedPeakFingerprintAnnotationScore(Settings settings) {
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
		this.value = 0.0;
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)this.settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		
		java.util.ArrayList<?> peakMatchList = (java.util.ArrayList<?>)this.candidate.getProperty("PeakMatchList");
	
		int matches = 0;
		// get foreground fingerprint observations (m_f_observed)
		java.util.ArrayList<Double> matchMasses = new java.util.ArrayList<Double>();
		java.util.ArrayList<Double> matchProb = new java.util.ArrayList<Double>();
		java.util.ArrayList<Integer> matchType = new java.util.ArrayList<Integer>(); // found - 1; alpha - 2; beta - 3
		// get foreground fingerprint observations (m_f_observed)
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			// get f_m_observed
			MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElement(i);
			Double currentMass = peakToFingerprintGroupList.getPeakmz();
			MassFingerprintMatch currentMatch = getMatchByMass(peakMatchList, currentMass);
			if(currentMatch == null) {
				matchProb.add(peakToFingerprintGroupList.getBetaProb());
				matchType.add(3);
				matchMasses.add(currentMass);
				this.value += Math.log(peakToFingerprintGroupList.getBetaProb());
			} else {
				FastBitArray currentFingerprint = new FastBitArray(currentMatch.getFingerprint());
				// ToDo: at this stage try to check all fragments not only the best one
				// (p(m,f) + alpha) / sum_F(p(m,f)) + |F| * alpha
				double matching_prob = peakToFingerprintGroupList.getMatchingProbability(currentFingerprint);
				// |F|
				if(matching_prob != 0.0) {
					matches++;
					this.value += Math.log(matching_prob);
					matchProb.add(matching_prob);
					matchType.add(1);
					matchMasses.add(currentMass);
				}
				else {
					this.value += Math.log(peakToFingerprintGroupList.getAlphaProb());
					matchProb.add(peakToFingerprintGroupList.getAlphaProb());
					matchType.add(2);
					matchMasses.add(currentMass);
				}
			}
		}
		if(peakToFingerprintGroupListCollection.getNumberElements() == 0) this.value = 0.0;

		this.candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Matches", matches);
		this.candidate.setProperty("AutomatedPeakFingerprintAnnotationScore", this.value);
		this.candidate.setProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes", this.getProbTypeString(matchProb, matchType, matchMasses));
		
		this.candidate.removeProperty("PeakMatchList");
 	}
	
	public String getProbTypeString(java.util.ArrayList<Double> matchProb, java.util.ArrayList<Integer> matchType, java.util.ArrayList<Double> matchMasses) {
		if(matchProb.size() == 0) return "NA";
		StringBuilder string = new StringBuilder();
		if(matchProb.size() >= 1) {
			string.append(matchType.get(0));
			string.append(":");
			string.append(matchProb.get(0));
			string.append(":");
			string.append(matchMasses.get(0));
		}
		for(int i = 1; i < matchProb.size(); i++) {
			string.append(";");
			string.append(matchType.get(i));
			string.append(":");
			string.append(matchProb.get(i));
			string.append(":");
			string.append(matchMasses.get(i));
		}
		return string.toString();
	}
	
	public static MassFingerprintMatch getMatchByMass(java.util.ArrayList<?> matches, Double peakMass) {
		for(int i = 0; i < matches.size(); i++) {
			MassFingerprintMatch match = (MassFingerprintMatch)matches.get(i);
			if(match.getMass().equals(peakMass)) 
				return match;
		}
		return null;
	}
	
	@Override
	public void nullify() {
		super.nullify();
	}

	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
