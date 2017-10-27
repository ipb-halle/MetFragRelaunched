package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
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
			Double adductMass = Constants.getIonisationMassByNominalMassDifference((Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME));
			
			java.util.ArrayList<Double> massDifferences = this.calculatePeakDifferences(peakList, neutralPrecursorMass, adductMass, mzppm, mzabs);
			
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			int numObservationsMerged = 0;
			java.util.HashMap<Double, MassToFingerprintGroupList> mergedFingerprintGroupLists = new java.util.HashMap<Double, MassToFingerprintGroupList>();
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				if(line.startsWith("SUMMARY")) {
					String[] tmp = line.split("\\s+");
					settings.set(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_COUNT_NAME, Double.parseDouble(tmp[2]));
					settings.set(VariableNames.LOSS_FINGERPRINT_TUPLE_COUNT_NAME, Double.parseDouble(tmp[1]) - numObservationsMerged);
					continue;
				}
				String[] tmp = line.split("\\s+");
				Double loss = Double.parseDouble(tmp[0]);
				Double matchedMass = this.containsMass(loss, massDifferences, mzabs, mzppm);
				if(matchedMass != null) {
					FingerprintGroup[] groups = this.getFingerprintGroup(tmp);
					if(mergedFingerprintGroupLists.containsKey(matchedMass)) {
						numObservationsMerged++;
						MassToFingerprintGroupList currentGroupList = mergedFingerprintGroupLists.get(matchedMass);
						for(int i = 0; i < groups.length; i++) {
							FingerprintGroup curGroup = currentGroupList.getElementByFingerprint(groups[i].getFingerprint());
							if(curGroup == null) currentGroupList.addElement(groups[i]);
							else {
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
			breader.close();
			settings.set(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, lossToFingerprintGroupListCollection);
		}
	}

	public void postProcessScoreParameters(Settings settings) {
		CombinedSingleCandidateMetFragProcess[] processes = (CombinedSingleCandidateMetFragProcess[])settings.get(VariableNames.METFRAG_PROCESSES_NAME);
		
		MassToFingerprintsHashMap lossMassToFingerprints = new MassToFingerprintsHashMap();
		MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		
		int ionmode = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		boolean ispositive = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		
		double adductMass = Constants.getIonisationTypeMassCorrection(Constants.ADDUCT_NOMINAL_MASSES.indexOf(ionmode), ispositive);
		double precursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		
		double ionmass = precursorMass + adductMass ;
		
		for(CombinedSingleCandidateMetFragProcess scmfp : processes) {
			/*
			 * check whether the single run was successful
			 */
			if(scmfp.wasSuccessful()) {
				ICandidate[] candidates = scmfp.getScoredPrecursorCandidates();
				for(int ii = 0; ii < candidates.length; ii++) {
					MatchList matchlist = candidates[ii].getMatchList();
					for(int i = 0; i < matchlist.getNumberElements(); i++) {
						IMatch matchI = matchlist.getElement(i);
						double peakMassI = matchI.getMatchedPeak().getMass();
						for(int j = i + 1; j < matchlist.getNumberElements(); j++) {
							IMatch matchJ = matchlist.getElement(i);
							double peakMassJ = matchJ.getMatchedPeak().getMass();
							IFragment fragmentJ = matchJ.getBestMatchedFragment();
							if(fragmentJ.isRealSubStructure(fragmentI)) {
								double diff = peakMassJ - peakMassI;
								IFragment diffFragment = fragmentJ.getDifferenceFragment(precursorMolecule, fragmentI);
								if(diffFragment == null) continue;

								IAtomContainer con = fingerprint.getNormalizedAtomContainer(precursorMolecule, diffFragment);
									
								lossFingerprint.add(fingerprint.getNormalizedFingerprint(con));
								lossMassDiff.add(diff);
							}
						}
						
						
						
						MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(match.getMatchedPeak().getMass());
						if(peakToFingerprintGroupList == null) continue;
						IFragment frag = match.getBestMatchedFragment();
						FastBitArray currentFingerprint = null;
						try {
							IAtomContainer con = fingerprint.getNormalizedAtomContainer(candidates[i].getPrecursorMolecule(), frag);
							currentFingerprint = fingerprint.getNormalizedFastBitArrayFingerprint(con);
						} catch (InvalidSmilesException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (CDKException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						//	if(match.getMatchedPeak().getMass() < 60) System.out.println(match.getMatchedPeak().getMass() + " " + currentFingerprint + " " + fragSmiles);
						// check whether fingerprint was observed for current peak mass in the training data
						if (!peakToFingerprintGroupList.containsFingerprint(currentFingerprint)) {
							// if not add the fingerprint to background by addFingerprint function
							// addFingerprint checks also whether fingerprint was already added
							peakMassToFingerprints.addFingerprint(match.getMatchedPeak().getMass(), currentFingerprint);
						}
					}
				}
			}
		}
	}
	
	protected Double containsMass(double mass, java.util.ArrayList<Double> massArrayList, double mzabs, double mzppm) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		double bestDev = Integer.MAX_VALUE;
		int bestPeakIndex = -1;
		
		for(int i = 0; i < massArrayList.size(); i++) 
		{
			double currentMass = massArrayList.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				double currentDev = Math.abs(currentMass - mass);
				if(currentDev < bestDev) {
					bestPeakIndex = i;
					bestDev = currentDev;
				}
			}
			
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
	private java.util.ArrayList<Double> calculatePeakDifferences(DefaultPeakList peakList, double neutralPrecursorMass, double adductMass, double mzppm, double mzabs) {
		java.util.ArrayList<Double> peakDifferences = new java.util.ArrayList<Double>();
		
		double ionmass = neutralPrecursorMass + adductMass;
		
		// calculate mass differences between mass peaks
		for(int i = 0; i < peakList.getNumberElements(); i++) {
			Double currentMass1 = ((TandemMassPeak)peakList.getElement(i)).getMass();
			if(ionmass <= currentMass1) continue;
			for(int j = i + 1; j < peakList.getNumberElements(); j++) {
				Double currentMass2 = ((TandemMassPeak)peakList.getElement(j)).getMass();
				if(currentMass2 <= currentMass1) continue;
				double massDifference = currentMass2 - currentMass1;
				peakDifferences.add(massDifference);
			}
			peakDifferences.add(ionmass - currentMass1);
		}

		java.util.ArrayList<Double> peakDifferencesSorted = new java.util.ArrayList<Double>();
		for(int i = 0; i < peakDifferences.size(); i++) {
			int index = 0; 
			double currentPeakDiff = peakDifferences.get(i);
			while(index < peakDifferencesSorted.size()) {
				if(peakDifferencesSorted.get(index) > currentPeakDiff) break;
				else index++;
			}
			peakDifferencesSorted.add(index, currentPeakDiff);
		}
		
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
			peakDifferencesCombined.add(massSum / (double)massesAdded);
		}
		
		return peakDifferencesCombined;
	}
	
}
