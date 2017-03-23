package de.ipbhalle.metfraglib.scoreinitialisation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.MassToFingerprints;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;

public class AutomatedFingerprintSubstructureAnnotationScoreInitialiser  implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) throws Exception {
		settings.set(VariableNames.PEAK_TO_BACKGROUND_FINGERPRINTS_NAME, new MassToFingerprints());
		if(!settings.containsKey(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) || settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME) == null) {
			PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new PeakToFingerprintGroupListCollection();
			String filename = (String)settings.get(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME);
			DefaultPeakList peakList = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
			Double mzppm = (Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
			Double mzabs = (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
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
						fingerprintGroup = new FingerprintGroup(Double.parseDouble(tmp[i]));
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
		
			System.out.println(peakToFingerprintGroupListCollection.getNumberElements() + " peaks found");
			for(int i = 0; i < peakToFingerprintGroupListCollection.getNumberElements(); i++) {
				System.out.println(peakToFingerprintGroupListCollection.getElement(i).getPeakmz());
			}
			breader.close();
			peakToFingerprintGroupListCollection.calculateSumProbabilities();
			// calculate pseudo count for a non-annotated peak
			double numberPseudoCounts = (double)this.calculateNumberBackgroundPeaks(0.0, 1000.0, mzppm, mzabs, peakToFingerprintGroupListCollection.getNumberElements()) + 1.0;
			double beta = 1.0 / numberPseudoCounts;
			settings.set(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, beta);
			settings.set(VariableNames.BETA_PSEUDO_COUNT_DENOMINATOR_VALUE_NAME, beta * numberPseudoCounts);
			settings.set(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME, peakToFingerprintGroupListCollection);
		}
	}
	
	/**
	 * calculates the number of peaks in the background based on the number of intervals between a start and an end mass value
	 * 
	 * @param startMass
	 * @param endMass
	 * @param mzppm
	 * @param mzabs
	 */
	private int calculateNumberBackgroundPeaks(double startMass, double endMass, double mzppm, double mzabs, int numberForeGroundPeaks) {
		double currentMass = startMass;
		int numberIntervals = 0;
		while(currentMass < endMass) {
			numberIntervals++;
			currentMass = currentMass + ((currentMass / 1000000.0) * mzppm) + mzabs;
		}
		return numberIntervals - numberForeGroundPeaks;
	}

}
