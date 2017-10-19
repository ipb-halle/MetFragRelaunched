package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

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
			
			java.util.ArrayList<Double> massDifferences = calculatePeakDifferences(peakList, neutralPrecursorMass, adductMass, mzppm, mzabs);
			
			double maxLossMass = massDifferences.get(massDifferences.size() - 1);
			double minLossMass = massDifferences.get(0);
			
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				if(line.startsWith("SUMMARY")) {
					String[] tmp = line.split("\\s+");
					settings.set(VariableNames.LOSS_FINGERPRINT_DENOMINATOR_COUNT_NAME, Double.parseDouble(tmp[2]));
					settings.set(VariableNames.LOSS_FINGERPRINT_TUPLE_COUNT_NAME, Double.parseDouble(tmp[1]));
					continue;
				}
				String[] tmp = line.split("\\s+");
				Double loss = Double.parseDouble(tmp[0]);
				//if(!this.containsMass(loss, massDifferences, mzabs, mzppm)) continue;
				if(loss > (maxLossMass + 5.0) || loss < (minLossMass - 5.0)) continue;
				
				MassToFingerprintGroupList lossToFingerprintGroupList = new MassToFingerprintGroupList(loss);
				FingerprintGroup fingerprintGroup = null;
				for(int i = 1; i < tmp.length; i++) {
					if((i % 2) == 1) {
						if(fingerprintGroup != null) 
							lossToFingerprintGroupList.addElement(fingerprintGroup);
						double count = Double.parseDouble(tmp[i]);
						fingerprintGroup = new FingerprintGroup(count);
						fingerprintGroup.setNumberObserved((int)count);
					}
					else {
						fingerprintGroup.setFingerprint(tmp[i]);
					}
					if(i == (tmp.length - 1)) {
						lossToFingerprintGroupList.addElement(fingerprintGroup);
						lossToFingerprintGroupListCollection.addElement(lossToFingerprintGroupList);
					}
				}
			}
			breader.close();
			settings.set(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, lossToFingerprintGroupListCollection);
		}
	}

	public void postProcessScoreParameters(Settings settings) {
		return;
	}
	
	protected boolean containsMass(double mass, java.util.ArrayList<Double> massArrayList, double mzabs, double mzppm) {
		double dev = MathTools.calculateAbsoluteDeviation(mass, mzppm);
		dev += mzabs;
		
		for(int i = 0; i < massArrayList.size(); i++) 
		{
			double currentMass = massArrayList.get(i);
			if(currentMass - dev <= mass && mass <= currentMass + dev) {
				return true;
			}
			
		}
		return false;
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
