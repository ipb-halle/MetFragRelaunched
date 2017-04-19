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
public class AutomatedFingerprintSubstructureAnnotationScoreInitialiser4  implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		if(!settings.containsKey(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) || settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) == null) {
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new PeakToFingerprintGroupListCollection();
			String filename = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME);
		//	String filename_conv = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_CONV_NAME);
			DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				if(line.startsWith("SUMMARY")) {
					String[] tmp = line.split("\\s+");
					settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME, Double.parseDouble(tmp[2]));
					settings.set(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME, Double.parseDouble(tmp[1]));
					continue;
				}
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
			
			/*
			FingerprintToMasses fingerprintToMasses = new FingerprintToMasses();
			breader = new BufferedReader(new FileReader(new File(filename_conv)));
			while((line = breader.readLine()) != null) {
				line = line.trim();
				String[] tmp = line.split("\\s+");
				FastBitArray currentFingerprint = new FastBitArray(tmp[0]);
				for(int k = 1; k < tmp.length; k += 2) {
					fingerprintToMasses.addMass(currentFingerprint, Double.parseDouble(tmp[k+1]), Double.parseDouble(tmp[k]));
				}
			}
			
			settings.set(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME, fingerprintToMasses);
			*/
			settings.set(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, peakToFingerprintGroupListCollection);
		
			settings.set(VariableNames.NUMBER_BACKGROUND_MASSES_NAME, calculateNumberBackgroundPeaks(0, 1000, mzppm, mzabs, peakList.getNumberElements()));
		}
	}

	/**
	 * 
	 */
	public void postProcessScoreParameters(Settings settings) {
		CombinedSingleCandidateMetFragProcess[] processes = (CombinedSingleCandidateMetFragProcess[])settings.get(VariableNames.METFRAG_PROCESSES_NAME);
		
		// to determine F_u
		MassToFingerprints massToFingerprints = new MassToFingerprints();
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (PeakToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		FingerprintToMasses fingerprintToMasses = (FingerprintToMasses)settings.get(VariableNames.FINGERPRINT_TO_PEAK_GROUP_LIST_COLLECTION_NAME);

		double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
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
						if(peakToFingerprintGroupList == null) continue;
						IFragment frag = match.getBestMatchedFragment();
						FastBitArray currentFingerprint = new FastBitArray(MoleculeFunctions.getNormalizedFingerprint(frag));
						// if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
						// check whether fingerprint was observed for current peak mass in the training data
						if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
							// if not add the fingerprint to background by addFingerprint function
							// addFingerprint checks also whether fingerprint was already added
							massToFingerprints.addFingerprint(match.getMatchedPeak().getMass(), currentFingerprint);
							fingerprintToMasses.addMass(currentFingerprint, match.getMatchedPeak().getMass(), 0.0, mzppm, mzabs);
						}
					}
				}
			}
		}

		double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
		double f_seen = (double)settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
		double f_unseen = massToFingerprints.getOverallSize();																// f_u
		double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		// set value for denominator of P(f,m)
		double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;
		settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
		
		double alphaProbability = alpha / denominatorValue; 	// P(f,m) F_u
		double betaProbability = beta / denominatorValue;		// p(f,m) not annotated
		
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);

			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// first calculate P(f,m)
				groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getNumberObserved() + alpha) / denominatorValue);
				// sum_f P(f,m) -> for F_s
			}

		}
		// normalize probabilities in fingerprintToProbabilities
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.normalizeNumObservations(i, denominatorValue);
		}
		fingerprintToMasses.calculateSumNumObservations();
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			fingerprintToMasses.addAlphaProbability(alphaProbability / fingerprintToMasses.getSumNumObservations(i));
		}
		fingerprintToMasses.addBetaProbability(betaProbability / (double)settings.get(VariableNames.NUMBER_BACKGROUND_MASSES_NAME));
		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			PeakToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// second calculate P(f|m)
				double sum_m = fingerprintToMasses.getSumNumObservations(groupList.getElement(ii).getFingerprint()); 
				if(sum_m == 0) {
					System.err.println("Check " + groupList.getPeakmz() + " " + groupList.getElement(ii).getFingerprint().toStringIDs());
				}
				groupList.getElement(ii).setConditionalProbability_ps(groupList.getElement(ii).getJointProbability() / sum_m);
			}

			groupList.setProbabilityToConditionalProbability_ps();
	
		}
		
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
