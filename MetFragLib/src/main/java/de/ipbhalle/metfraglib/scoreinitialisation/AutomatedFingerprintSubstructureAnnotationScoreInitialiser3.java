package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedSingleCandidateMetFragProcess;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.FingerprintToMasses;
import de.ipbhalle.metfraglib.substructure.MassToFingerprints;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;

/**
 * 1 - read absolute frequencies of (f,m) 
 * 2 - calculate joint probabilities with knowledge of F_u calculated by fragment matching of the candidate list
 * 3 - calculate conditional probabilities
 * 
 * steps 2 and 3 need to be performed within the post processing function when F_u is known
 * 
 * @author cruttkie
 *
 */
public class AutomatedFingerprintSubstructureAnnotationScoreInitialiser3  implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		settings.set(VariableNames.PEAK_TO_BACKGROUND_MASSES_NAME, new FingerprintToMasses());
		if(!settings.containsKey(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) || settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) == null) {
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new PeakToFingerprintGroupListCollection();
			String filename = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME);
			DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
			
			double denominatorCount = 0;
			double numberSeenTuples = 0;  //F_s
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				String[] tmp = line.split("\\s+");
				Double peak = Double.parseDouble(tmp[0]);
				Double matchedMass = peakList.getBestMatchingMass(peak, mzppm, mzabs);
				if(matchedMass == null) continue;
				PeakToFingerprintGroupList peakToFingerprintGroupList = new PeakToFingerprintGroupList(matchedMass);
				FingerprintGroup fingerprintGroup = null;
				for(int i = 1; i < tmp.length; i++) {
					if((i % 2) == 1) {
						if(fingerprintGroup != null) 
							peakToFingerprintGroupList.addElement(fingerprintGroup);
						double count = Double.parseDouble(tmp[i]);
						fingerprintGroup = new FingerprintGroup(count);
						denominatorCount += count;
						numberSeenTuples++;
					}
					else {
						fingerprintGroup.setFingerprint(tmp[i]);
					}
					if(i == (tmp.length - 1)) {
						peakToFingerprintGroupList.addElement(fingerprintGroup);
						peakToFingerprintGroupListCollection.addElement(peakToFingerprintGroupList);
					}
				}
			}
			breader.close();
			
			settings.set(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, peakToFingerprintGroupListCollection);
			settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME, denominatorCount);
			settings.set(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME, numberSeenTuples);
		}
	}

	public void postProcessScoreParameters(Settings settings) {
		CombinedSingleCandidateMetFragProcess[] processes = (CombinedSingleCandidateMetFragProcess[])settings.get(VariableNames.METFRAG_PROCESSES_NAME);
		
		// to determin F_u
		MassToFingerprints massToFingerprints = new MassToFingerprints();
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);

		for(CombinedSingleCandidateMetFragProcess scmfp : processes) {
			/*
			 * check whether the single run was successful
			 */
			if(scmfp.wasSuccessful()) {
				ICandidate[] candidates = scmfp.getScoredPrecursorCandidates();
				for(int i = 0; i < candidates.length; i++) {
					MatchList matchlist = candidates[i].getMatchList();
					for(int j = 0; j < matchlist.getNumberElements(); j++) {
						IMatch match = matchlist.getElement(j);
						PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMatchedPeak().getMass());
						IFragment frag = match.getBestMatchedFragment();
						FastBitArray currentFingerprint = new FastBitArray(MoleculeFunctions.getNormalizedFingerprint(frag));
						//	if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
						// check whether fingerprint was observed for current peak mass in the training data
						if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
							// if not add the fingerprint to background by addFingerprint function
							// addFingerprint checks also whether fingerprint was already added
							massToFingerprints.addFingerprint(match.getMatchedPeak().getMass(), currentFingerprint);
						}
					}
				}
			}
		}

		double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
		double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
		double f_unseen = massToFingerprints.getOverallSize();																// f_u
		double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		// set value for denominator of P(f,m)
		double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;
		settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta);
		
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				groupList.getElement(ii).setConditionalProbability_sp((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
			}
		}
		
		peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_sp();
		peakToFingerprintGroupListCollection.calculateSumProbabilities();
		
		return;
	}
	
	/**
	 * calculates the number of peaks in the background based on the number of intervals between a start and an end mass value
	 * 
	 * @param startMass
	 * @param endMass
	 * @param mzppm
	 * @param mzabs
	 */
	protected int calculateNumberBackgroundPeaks(double startMass, double endMass, double mzppm, double mzabs, int numberForeGroundPeaks) {
		double currentMass = startMass;
		int numberIntervals = 0;
		while(currentMass < endMass) {
			numberIntervals++;
			currentMass = currentMass + ((currentMass / 1000000.0) * mzppm) + mzabs;
		}
		return numberIntervals - numberForeGroundPeaks;
	}

}
