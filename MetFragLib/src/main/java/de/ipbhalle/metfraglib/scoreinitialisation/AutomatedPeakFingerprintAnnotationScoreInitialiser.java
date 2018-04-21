package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.fingerprint.Fingerprint;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedSingleCandidateMetFragProcess;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintsHashMap;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

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
public class AutomatedPeakFingerprintAnnotationScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		if(!settings.containsKey(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) || settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) == null) {
			MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new MassToFingerprintGroupListCollection();
			String filename = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME);
			DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			int numMatchedObservationsMerged = 0;
			java.util.HashMap<Double, MassToFingerprintGroupList> mergedFingerprintGroupLists = new java.util.HashMap<Double, MassToFingerprintGroupList>();
			
			String nonMatchedMassesString = breader.readLine().trim();
			int numNonMatchElements = 0;
			int numNonMatchOccurrences = 0;
			if(!nonMatchedMassesString.equals("NA")) {
				String[] tmp = nonMatchedMassesString.split(";");
				numNonMatchElements = tmp.length;
				for(int k = 0; k < tmp.length; k++) {
					String[] tmp2 = tmp[k].split(":");
					int count = 1;
					Double newMass = Double.parseDouble(tmp2[0]);
					if(tmp2.length == 2) count = Integer.parseInt(tmp2[1]);
					numNonMatchOccurrences += count;
					Double matchedMass = peakList.getBestMatchingMass(newMass, mzppm, mzabs);
					if(matchedMass == null && newMass > peakList.getMaximumMassValue()) break;
					if(matchedMass != null) {
						FingerprintGroup group = new FingerprintGroup(1.0);
						group.setNumberObserved(count);
						group.setFingerprint("0");
						if(mergedFingerprintGroupLists.containsKey(matchedMass)) {
							MassToFingerprintGroupList currentGroupList = mergedFingerprintGroupLists.get(matchedMass);
							FingerprintGroup curGroup = currentGroupList.getElementByFingerprint(group.getFingerprint());
							if(curGroup == null) currentGroupList.addElement(group.getFingerprint());
							else {
								numNonMatchElements--;
								curGroup.setNumberObserved(curGroup.getNumberObserved() + group.getNumberObserved());
								curGroup.setProbability(curGroup.getProbability() + group.getProbability());
							}
						} else {
							MassToFingerprintGroupList currentGroupList = new MassToFingerprintGroupList(matchedMass);
							currentGroupList.addElement(group);
							mergedFingerprintGroupLists.put(matchedMass, currentGroupList);
						}
					}
				}
			}
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				if(line.startsWith("SUMMARY")) {
					String[] tmp = line.split("\\s+");
					// sum overall occurrences
					settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME, Double.parseDouble(tmp[2]) + numNonMatchOccurrences);
					// number different peak pairs matched
					settings.set(VariableNames.PEAK_FINGERPRINT_MATCHED_TUPLE_COUNT_NAME, Double.parseDouble(tmp[1]) - numMatchedObservationsMerged);
					// number different peak pairs non-matched
					settings.set(VariableNames.PEAK_FINGERPRINT_NON_MATCHED_TUPLE_COUNT_NAME, (double)numNonMatchElements);
					continue;
				}
				String[] tmp = line.split("\\s+");
				Double peak = Double.parseDouble(tmp[0]);
				// find matching peak in spectrum
				Double matchedMass = peakList.getBestMatchingMass(peak, mzppm, mzabs);
				if(matchedMass != null) {
					FingerprintGroup[] groups = this.getFingerprintGroup(tmp);
					if(mergedFingerprintGroupLists.containsKey(matchedMass)) {
						MassToFingerprintGroupList currentGroupList = mergedFingerprintGroupLists.get(matchedMass);
						for(int i = 0; i < groups.length; i++) {
							FingerprintGroup curGroup = currentGroupList.getElementByFingerprint(groups[i].getFingerprint());
							if(curGroup == null) currentGroupList.addElement(groups[i]);
							else {
								if(curGroup.getFingerprint().getSize() != 1) numMatchedObservationsMerged++;
								curGroup.setNumberObserved(curGroup.getNumberObserved() + groups[i].getNumberObserved());
								curGroup.setProbability(curGroup.getProbability() + groups[i].getProbability());
							}
						}
					} else {
						MassToFingerprintGroupList currentGroupList = new MassToFingerprintGroupList(matchedMass);
						for(int i = 0; i < groups.length; i++)
							currentGroupList.addElement(groups[i]);
						mergedFingerprintGroupLists.put(matchedMass, currentGroupList);
					}
				}
			}
			
			java.util.Iterator<Double> it = mergedFingerprintGroupLists.keySet().iterator();
			while(it.hasNext()) {
				peakToFingerprintGroupListCollection.addElementSorted(mergedFingerprintGroupLists.get(it.next()));
			}
			breader.close();
			settings.set(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, peakToFingerprintGroupListCollection);
		}
	}

	public void postProcessScoreParameters(Settings settings) throws Exception {
		CombinedSingleCandidateMetFragProcess[] processes = (CombinedSingleCandidateMetFragProcess[])settings.get(VariableNames.METFRAG_PROCESSES_NAME);
		
		// to determine F_u
		MassToFingerprintsHashMap peakMassToFingerprints = new MassToFingerprintsHashMap();
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		Fingerprint fingerprint = new Fingerprint((String)settings.get(VariableNames.FINGERPRINT_TYPE_NAME));
		DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
		
		for(CombinedSingleCandidateMetFragProcess scmfp : processes) {
			/*
			 * check whether the single run was successful
			 */
			if(scmfp.wasSuccessful()) {
				ICandidate candidate = scmfp.getScoredPrecursorCandidates()[0];
				MatchList matchlist = candidate.getMatchList();
				
				java.util.LinkedList<Double> nonExplainedPeaks = this.getNonExplainedPeaks(peakList, matchlist);
				
				java.util.ArrayList<MassFingerprintMatch> peakMatchlist = new java.util.ArrayList<MassFingerprintMatch>();
				if(matchlist == null || matchlist.getNumberElements() == 0) {
					candidate.setProperty("PeakMatchList", peakMatchlist);
					continue;
				}
				candidate.initialisePrecursorCandidate();
				for(int j = 0; j < matchlist.getNumberElements() + nonExplainedPeaks.size(); j++) {
					FastBitArray currentFingerprint = null;
					Double mass = null;
					// check if it's a valid match (peak-fragment assignment)
					if(j < matchlist.getNumberElements()) {
						IMatch match = matchlist.getElement(j);
						mass = match.getMatchedPeak().getMass();
						IFragment frag = match.getBestMatchedFragment();
						try {
							IAtomContainer con = fingerprint.getNormalizedAtomContainer(candidate.getPrecursorMolecule(), frag);
							currentFingerprint = fingerprint.getNormalizedFastBitArrayFingerprint(con);
						} catch (InvalidSmilesException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (CDKException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else { // else it must be a non-matched peak
						mass = nonExplainedPeaks.get(j - matchlist.getNumberElements());
						currentFingerprint = new FastBitArray("0");
					}
					MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(mass);
					if(peakToFingerprintGroupList == null) continue;
					peakMatchlist.add(new MassFingerprintMatch(mass, currentFingerprint));
					// check whether fingerprint was observed for current peak mass in the training data
					if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
						// if not add the fingerprint to background by addFingerprint function
						// addFingerprint checks also whether fingerprint was already added
						peakMassToFingerprints.addFingerprint(mass, currentFingerprint);
					}
				}
				candidate.setProperty("PeakMatchList", peakMatchlist);
			}
		}

		double f_seen_matched = (double) settings.get(VariableNames.PEAK_FINGERPRINT_MATCHED_TUPLE_COUNT_NAME); // f_s
		double f_seen_non_matched = (double) settings.get(VariableNames.PEAK_FINGERPRINT_NON_MATCHED_TUPLE_COUNT_NAME); // f_s
		double f_unseen_matched = peakMassToFingerprints.getOverallMatchedSize(); // f_u
		double f_unseen_non_matched = peakMassToFingerprints.getOverallNonMatchedSize(); // f_u
		
		double alpha = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		double beta = (double)settings.get(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta

		double sumFingerprintFrequencies = (double)settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		// set value for denominator of P(f,m)
		double denominatorValue = sumFingerprintFrequencies + alpha * (f_seen_matched + f_unseen_matched) + beta * (f_seen_non_matched + f_unseen_non_matched);
		settings.set(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
		
		double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
		double betaProbability = beta / denominatorValue;	// p(f,m) not annotated

		for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
			MassToFingerprintGroupList groupList = peakToFingerprintGroupListCollection.getElement(i);
			// sum_f P(f,m)
			// calculate sum of MF_s (including the alpha count) and the joint probabilities
			// at this stage getProbability() returns the absolute counts from the annotation files
			double sum_f = 0.0;
			double sumFsProbabilities = 0.0;
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// first calculate P(f,m)
				if(groupList.getElement(ii).getFingerprint().getSize() != 1) 
					groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
				else 
					groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + beta) / denominatorValue);
				// sum_f P(f,m) -> for F_s
				sumFsProbabilities += groupList.getElement(ii).getJointProbability();
			}
			
			// calculate the sum of probabilities for un-observed fingerprints for the current mass
			double sumFuProbabilities = alphaProbability * peakMassToFingerprints.getSizeMatched(groupList.getPeakmz());
			sumFuProbabilities += betaProbability * peakMassToFingerprints.getSizeNonMatched(groupList.getPeakmz());
			
			sum_f += sumFsProbabilities;
			sum_f += sumFuProbabilities;

			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// second calculate P(f|m)
				groupList.getElement(ii).setConditionalProbability_sp(groupList.getElement(ii).getJointProbability() / sum_f);
			}
			groupList.setAlphaProb(alphaProbability / sum_f);
			groupList.setBetaProb(betaProbability / sum_f);
			groupList.setProbabilityToConditionalProbability_sp();
			groupList.calculateSumProbabilites();
		}
		return;
	}
	
	protected int matchesToPeak(Double mass, DefaultPeakList peaklist, int peakPointer, double mzabs, double mzppm) {
		IPeak peak = (IPeak)peaklist.getElement(peakPointer);
		boolean matches = MathTools.matchMasses(peak.getMass(), mass, mzppm, mzabs);
		if(matches) return 0;
		if(mass < peak.getMass()) return -1;
		return 1;
	}
	
	protected FingerprintGroup[] getFingerprintGroup(String[] splittedLine) {
		FingerprintGroup[] groups = new FingerprintGroup[(splittedLine.length - 1) / 2];
		int index = 0;
		for(int i = 1; i < splittedLine.length; i+=2) {
			double count = Double.parseDouble(splittedLine[i]);
			FingerprintGroup newGroup = new FingerprintGroup(count);
			newGroup.setNumberObserved((int)count);
			newGroup.setFingerprint(splittedLine[i + 1]);
			groups[index] = newGroup;
			index++;
		}
		return groups;
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
	
	protected java.util.LinkedList<Double> getNonExplainedPeaks(DefaultPeakList peaklist, MatchList matchList) {
		java.util.LinkedList<Double> nonExplPeaks = new java.util.LinkedList<Double>();
		for(int k = 0; k < peaklist.getNumberElements(); k++) {
			if(matchList.getNumberElements() == 0) {
				nonExplPeaks.add(((IPeak)peaklist.getElement(k)).getMass());
			} else if(!this.isContained(((IPeak)peaklist.getElement(k)).getMass(), matchList)) {
				nonExplPeaks.add(((IPeak)peaklist.getElement(k)).getMass());
			}
		}
		return nonExplPeaks;
	}
	
	private boolean isContained(Double mass, MatchList matchList) {
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			if(matchList.getElement(i).getMatchedPeak().getMass().equals(mass)) return true;
		}
		return false;
	}
}
