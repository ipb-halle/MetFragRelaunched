package de.ipbhalle.metfraglib.fingerprintlearning;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class FingerrintAnnotationMatchingPeak_Test {

	protected Settings settings;
	protected AutomatedPeakFingerprintAnnotationScoreInitialiser initPeak;
	protected Double peakMass;
	
	@Before
	public void setUp() {
		this.peakMass = new Double(166.0230);
		this.initPeak = new AutomatedPeakFingerprintAnnotationScoreInitialiser();
		this.settings = new MetFragGlobalSettings();
		String peak_annotation_file = ClassLoader.getSystemResource("training_values_peak_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME, peak_annotation_file);
		this.settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		this.settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.005);
		
		DefaultPeakList peakList = new DefaultPeakList();
		peakList.addElement(new TandemMassPeak(this.peakMass, 4180.9));
		
		this.settings.set(VariableNames.PEAK_LIST_NAME, peakList);
	}

	@Test
	public void test1() throws Exception {
		this.initPeak.initScoreParameters(this.settings);
		double mzppm = (double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		double mzabs = (double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(166.0230, mzppm, mzabs);
		assertNotNull("Did not find any matching annotation for " + this.peakMass, peakToFingerprintGroupList);
		
		System.out.println(peakToFingerprintGroupList.getNumberElements());
		System.out.println(peakToFingerprintGroupList.getPeakmz());
		
	}
}
