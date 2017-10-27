package de.ipbhalle.metfraglib.fingerprintlearning;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedLossFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class FingerrintAnnotationMatchingLoss_Test {

	protected Settings settings;
	protected AutomatedLossFingerprintAnnotationScoreInitialiser initLoss;
	protected Double lossMass;
	
	@Before
	public void setUp() {
		this.lossMass = new Double(132.042);
		this.initLoss = new AutomatedLossFingerprintAnnotationScoreInitialiser();
		this.settings = new MetFragGlobalSettings();
		String loss_annotation_file = ClassLoader.getSystemResource("training_values_loss_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_LOSS_ANNOTATION_FILE_NAME, loss_annotation_file);
		this.settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		this.settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.005);
		this.settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, 1);
		this.settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 406.126424);
		
		DefaultPeakList peakList = new DefaultPeakList();
		peakList.addElement(new TandemMassPeak(275.0919, 4862.9));
		
		this.settings.set(VariableNames.PEAK_LIST_NAME, peakList);
	}

	@Test
	public void test1() throws Exception {
		this.initLoss.initScoreParameters(this.settings);
		double mzppm = (double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		double mzabs = (double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
		MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MassToFingerprintGroupList lossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(this.lossMass, mzppm, mzabs);
		assertNotNull("Did not find any matching annotation for " + this.lossMass, lossToFingerprintGroupList);
		
		System.out.println(lossToFingerprintGroupList.getNumberElements());
		System.out.println(lossToFingerprintGroupList.getPeakmz());
		
	}
}
