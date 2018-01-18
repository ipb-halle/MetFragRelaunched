package de.ipbhalle.metfraglib.scoreinitialisation4;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fingerprint.Fingerprint;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.process.CombinedSingleCandidateMetFragProcess;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintsHashMap;

public class AutomatedLossFingerprintAnnotationScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		if(!settings.containsKey(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) || settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) == null) {
			MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = new MassToFingerprintGroupListCollection();
			String filename = (String)settings.get(VariableNames.FINGERPRINT_LOSS_ANNOTATION_FILE_NAME);
			DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
			Double neutralPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
			Double adductMass = Constants.getIonisationTypeMassCorrection(Constants.ADDUCT_NOMINAL_MASSES.indexOf((Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME)), (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME));
			
			java.util.ArrayList<Double> massDifferences = this.calculatePeakDifferences(peakList, neutralPrecursorMass, adductMass);
			java.util.ArrayList<Double> uniqueMassDifferences = this.calculateUniquePeakDifferences(massDifferences, mzppm, mzabs);
			java.util.LinkedList<Double> lossMassesFound = new java.util.LinkedList<Double>();
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
					
					Double matchedMass = this.containsMass(newMass, uniqueMassDifferences, mzabs, mzppm);
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
					settings.set(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_COUNT_NAME, Double.parseDouble(tmp[2]) + numNonMatchOccurrences);
					// number different peak pairs matched
					settings.set(VariableNames.LOSS_FINGERPRINT_MATCHED_TUPLE_COUNT_NAME, Double.parseDouble(tmp[1]) - numMatchedObservationsMerged);
					// number different peak pairs non-matched
					settings.set(VariableNames.LOSS_FINGERPRINT_NON_MATCHED_TUPLE_COUNT_NAME, (double)numNonMatchElements);
					continue;
				}
				String[] tmp = line.split("\\s+");
				Double loss = Double.parseDouble(tmp[0]);
				Double matchedMass = this.containsMass(loss, uniqueMassDifferences, mzabs, mzppm);
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
				lossToFingerprintGroupListCollection.addElementSorted(mergedFingerprintGroupLists.get(it.next()));
			}
			for(int i = 0; i < massDifferences.size(); i++) {
				if(lossToFingerprintGroupListCollection.getElementByPeak(massDifferences.get(i), mzppm, mzabs) != null)
					lossMassesFound.add(massDifferences.get(i));
			}
			breader.close();
			settings.set(VariableNames.LOSS_MASSES_FOUND_NAME, lossMassesFound);
			settings.set(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, lossToFingerprintGroupListCollection);
		}
	}

	public void postProcessScoreParameters(Settings settings) throws AtomTypeNotKnownFromInputListException, Exception {
		CombinedSingleCandidateMetFragProcess[] processes = (CombinedSingleCandidateMetFragProcess[])settings.get(VariableNames.METFRAG_PROCESSES_NAME);
		
		MassToFingerprintsHashMap lossMassToFingerprints = new MassToFingerprintsHashMap();
		MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
		int ionmode = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		boolean ispositive = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		
		double adductMass = Constants.getIonisationTypeMassCorrection(Constants.ADDUCT_NOMINAL_MASSES.indexOf(ionmode), ispositive);
		double precursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		double ionmass = MathTools.round(precursorMass + adductMass);				
		
		Fingerprint fingerprint = new Fingerprint((String)settings.get(VariableNames.FINGERPRINT_TYPE_NAME));
		
		for(CombinedSingleCandidateMetFragProcess scmfp : processes) {
			/*
			 * check whether the single run was successful
			 */
			if(scmfp.wasSuccessful()) {
				ICandidate candidate = scmfp.getScoredPrecursorCandidates()[0];
				java.util.ArrayList<MassFingerprintMatch> lossMatchlist = new java.util.ArrayList<MassFingerprintMatch>();
				MatchList matchlist = candidate.getMatchList();
				if(matchlist == null || matchlist.getNumberElements() == 0) {
					candidate.setProperty("LossMatchList", lossMatchlist);
					continue;
				}
				candidate.initialisePrecursorCandidate();
				for(int i = 0; i < matchlist.getNumberElements(); i++) {
					IMatch matchI = matchlist.getElement(i);
					IFragment fragmentI = matchI.getBestMatchedFragment();
					double peakMassI = matchI.getMatchedPeak().getMass();
					for(int j = i + 1; j < matchlist.getNumberElements(); j++) {
						IMatch matchJ = matchlist.getElement(j);
						double peakMassJ = matchJ.getMatchedPeak().getMass();
						IFragment fragmentJ = matchJ.getBestMatchedFragment();
						if(fragmentJ.isRealSubStructure(fragmentI)) {
							double diff = MathTools.round(peakMassJ - peakMassI);
							MassToFingerprintGroupList matchingLossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(diff, mzppm, mzabs);
							if(matchingLossToFingerprintGroupList == null) continue;
							IFragment diffFragment = fragmentJ.getDifferenceFragment(candidate.getPrecursorMolecule(), fragmentI);
							if(diffFragment == null) continue;
							IAtomContainer con = fingerprint.getNormalizedAtomContainer(candidate.getPrecursorMolecule(), diffFragment);	
							lossMatchlist.add(new MassFingerprintMatch(diff, fingerprint.getNormalizedFastBitArrayFingerprint(con)));
						}
					}
					//do the same for the precursor ion
					double diff = MathTools.round(ionmass - peakMassI);	
					MassToFingerprintGroupList matchingLossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(diff, mzppm, mzabs);
					if(matchingLossToFingerprintGroupList == null) continue;
					IFragment diffFragment = fragmentI.getDifferenceFragment(candidate.getPrecursorMolecule());
					if(diffFragment == null) continue;
					IAtomContainer con = fingerprint.getNormalizedAtomContainer(candidate.getPrecursorMolecule(), diffFragment);
					lossMatchlist.add(new MassFingerprintMatch(diff, fingerprint.getNormalizedFastBitArrayFingerprint(con)));
				}
				candidate.setProperty("LossMatchList", lossMatchlist);
				for(int j = 0; j < lossMatchlist.size(); j++) {
					MassFingerprintMatch lossMatch = lossMatchlist.get(j);
					MassToFingerprintGroupList lossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(lossMatch.getMass(), mzppm, mzabs);
					if(lossToFingerprintGroupList == null) continue;
					//lossMatch.setMass(lossToFingerprintGroupList.getPeakmz());
					FastBitArray currentFingerprint = lossMatch.getFingerprint();
					// check whether fingerprint was observed for current peak mass in the training data
					if (!lossToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
						// if not add the fingerprint to background by addFingerprint function
						// addFingerprint checks also whether fingerprint was already added
						lossMassToFingerprints.addFingerprint(lossMatch.getMass(), currentFingerprint);
					}
				}
			}
		}
		
		double alpha = (double)settings.get(VariableNames.LOSS_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME);					// alpha
		double beta = (double)settings.get(VariableNames.LOSS_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME);						// beta
		
		double f_seen = (double)settings.get(VariableNames.LOSS_FINGERPRINT_TUPLE_COUNT_NAME);								// f_s
		double f_unseen = lossMassToFingerprints.getOverallSize();																// f_u
		double sumFingerprintFrequencies = (double)settings.get(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_COUNT_NAME);		// \sum_N \sum_Ln 1
		
		// set value for denominator of P(f,m)
		double denominatorValue = sumFingerprintFrequencies + alpha * f_seen + alpha * f_unseen + beta;

		settings.set(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_VALUE_NAME, denominatorValue);
		
		double alphaProbability = alpha / denominatorValue; // P(f,m) F_u
		double betaProbability = beta / denominatorValue;	// p(f,m) not annotated
		
		for(int i = 0; i < lossToFingerprintGroupListCollection.getNumberElements(); i++) {
			MassToFingerprintGroupList groupList = lossToFingerprintGroupListCollection.getElement(i);
			
			// sum_f P(f,m)
			// calculate sum of MF_s (including the alpha count) and the joint probabilities
			// at this stage getProbability() returns the absolute counts from the annotation files
			double sum_f = 0.0;
			double sumFsProbabilities = 0.0;
			for(int ii = 0; ii < groupList.getNumberElements(); ii++) {
				// first calculate P(f,m)
				groupList.getElement(ii).setJointProbability((groupList.getElement(ii).getProbability() + alpha) / denominatorValue);
				// sum_f P(f,m) -> for F_s
				sumFsProbabilities += groupList.getElement(ii).getJointProbability();
			}
			
			// calculate the sum of probabilities for un-observed fingerprints for the current mass
			double sumFuProbabilities = alphaProbability * lossMassToFingerprints.getSizeOverall(groupList.getPeakmz());
			
			sum_f += sumFsProbabilities;
			sum_f += sumFuProbabilities;
			sum_f += betaProbability;
		
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
	
	protected Double containsMass(double mass, java.util.ArrayList<Double> massArrayList, double mzabs, double mzppm) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		double bestDev = Integer.MAX_VALUE;
		int bestPeakIndex = -1;
		
		for(int i = 0; i < massArrayList.size(); i++) 
		{
			double currentMass = massArrayList.get(i);
			if((currentMass - dev) <= mass && mass <= (currentMass + dev)) {
				double currentDev = Math.abs(currentMass - mass);
				if(currentDev < bestDev) {
					bestPeakIndex = i;
					bestDev = currentDev;
				}
			}
			else if(currentMass > mass) break;
		}
		if(bestPeakIndex != -1) return massArrayList.get(bestPeakIndex);
		else return null;
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
	 * 
	 * @param peakList
	 * @param neutralPrecursorMass
	 * @param adductMass
	 * @param mzppm
	 * @param mzabs
	 * @return
	 */
	private java.util.ArrayList<Double> calculatePeakDifferences(DefaultPeakList peakList, double neutralPrecursorMass, double adductMass) {
		java.util.ArrayList<Double> peakDifferences = new java.util.ArrayList<Double>();
		
		double ionmass = MathTools.round(neutralPrecursorMass + adductMass);
		// calculate mass differences between mass peaks
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			Double currentMass1 = ((TandemMassPeak)peakList.getElement(i)).getMass();
			if(ionmass <= currentMass1) continue;
			for(int j = i + 1; j < peakList.getNumberElements(); j++) {
				Double currentMass2 = ((TandemMassPeak)peakList.getElement(j)).getMass();
				if(currentMass2 <= currentMass1) continue;
				double massDifference = MathTools.round(currentMass2 - currentMass1);
				peakDifferences.add(massDifference);
			}
			double diff = MathTools.round(ionmass - currentMass1);
			peakDifferences.add(diff);
		}

		java.util.ArrayList<Double> peakDifferencesSorted = new java.util.ArrayList<Double>();
		for(int i = 0; i < peakDifferences.size(); i++) {
			int index = 0; 
			double currentPeakDiff = peakDifferences.get(i);
			while(index < peakDifferencesSorted.size()) {
				if(peakDifferencesSorted.get(index) > currentPeakDiff) break;
				else index++;
			}
			peakDifferencesSorted.add(index, MathTools.round(currentPeakDiff));
		}
		
		return peakDifferencesSorted;
	}
	
	private java.util.ArrayList<Double> calculateUniquePeakDifferences(java.util.ArrayList<Double> peakDifferencesSorted, double mzppm, double mzabs) {
		java.util.ArrayList<Double> peakDifferencesCombined = new java.util.ArrayList<Double>();
		int index = 0;
		while(index < peakDifferencesSorted.size()) {
			double currentMass = peakDifferencesSorted.get(index);
			double maxMassLimit = currentMass + (mzabs + MathTools.calculateAbsoluteDeviation(currentMass, mzppm) * 2.0);
			double massSum = currentMass;
			index++;
			int massesAdded = 1;
			while(index < peakDifferencesSorted.size() && peakDifferencesSorted.get(index) < maxMassLimit) {
				massSum += peakDifferencesSorted.get(index);
				massesAdded++;
				index++;
			}
			peakDifferencesCombined.add(MathTools.round(massSum / (double)massesAdded));
		}
		return peakDifferencesCombined;
	}
	
}
