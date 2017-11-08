package de.ipbhalle.metfraglib.fingerprintlearning;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedLossFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class FingerprintAnnotationMatchingLoss_2_Test {

	protected MetFragGlobalSettings settings;
	protected AutomatedLossFingerprintAnnotationScoreInitialiser initLoss;
	protected ICandidate candidate;
	protected DefaultPeakList peakList;
	protected org.apache.log4j.Logger logger;
	protected CombinedMetFragProcess mp;
	
	@Before
	public void setUp() throws AtomTypeNotKnownFromInputListException, Exception {
		this.initLoss = new AutomatedLossFingerprintAnnotationScoreInitialiser();
		String parameter_file = ClassLoader.getSystemResource("parameter_file_example_1.txt").getFile();
		this.settings = MetFragGlobalSettings.readSettings(new File(parameter_file), logger);
		String loss_annotation_file = ClassLoader.getSystemResource("training_values_loss_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_LOSS_ANNOTATION_FILE_NAME, loss_annotation_file);
		this.settings.set(VariableNames.STORE_RESULTS_PATH_NAME, Constants.OS_TEMP_DIR);
		String candidate_file = ClassLoader.getSystemResource("candidate_file_example_2.psv").getFile();
		this.settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, candidate_file);
		//check settings with SettingsChecker	
		SettingsChecker settingsChecker = new SettingsChecker();
		assertTrue("Error checking settings", settingsChecker.check(this.settings));
	
		this.settings.set(VariableNames.LOG_LEVEL_NAME, Level.INFO);
		this.mp = new CombinedMetFragProcess(this.settings);
	}

	@Test
	public void test1() throws Exception {
		assertTrue("Error fetching candidates.", this.mp.retrieveCompounds());
		try {
			this.mp.run();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		CandidateList scoredCandidateList = this.mp.getCandidateList();
		assertEquals("Number candidates not as expected", 2, scoredCandidateList.getNumberElements());
		Object prop1 = scoredCandidateList.getElement(0).getProperty("AutomatedLossFingerprintAnnotationScore_Probtypes");
		System.out.println(prop1);
		Object prop2 = scoredCandidateList.getElement(1).getProperty("AutomatedLossFingerprintAnnotationScore_Probtypes");
		assertNotNull("LossScore Propery null in candidate " + scoredCandidateList.getElement(0).getIdentifier(), prop1);
		assertNotNull("LossScore Propery null in candidate " + scoredCandidateList.getElement(1).getIdentifier(), prop2);
	
		String[] probTypes1 = ((String)prop1).split(";");
		String[] probTypes2 = ((String)prop2).split(";");
		
		for(int i = 0; i < probTypes1.length; i++)
			System.out.println(probTypes1[i] + " " + probTypes2[i]);
		
		assertEquals("Number probability types not as expected", 171, probTypes1.length);
		assertEquals("Number probability types not as expected", 171, probTypes2.length);
		
		Double lossScore1 = (Double)scoredCandidateList.getElement(0).getProperty("AutomatedLossFingerprintAnnotationScore");
		Double lossScore2 = (Double)scoredCandidateList.getElement(1).getProperty("AutomatedLossFingerprintAnnotationScore");

		System.out.println(lossScore1);
		System.out.println(lossScore2);
		
		assertEquals("LossScore not as expected for candidate " + scoredCandidateList.getElement(0).getIdentifier(), -1464.97939337479, lossScore1, 0.00000001);
		assertEquals("LossScore not as expected for candidate " + scoredCandidateList.getElement(1).getIdentifier(), -1615.84107652482, lossScore2, 0.00000001);
	
		int lossMatch1 = (Integer)scoredCandidateList.getElement(0).getProperty("AutomatedLossFingerprintAnnotationScore_Matches");
		int lossMatch2 = (Integer)scoredCandidateList.getElement(1).getProperty("AutomatedLossFingerprintAnnotationScore_Matches");
		
		assertEquals("LossMatchNumber not as expected for candidate " + scoredCandidateList.getElement(0).getIdentifier(), 14, lossMatch1);
		assertEquals("LossMatchNumber not as expected for candidate " + scoredCandidateList.getElement(1).getIdentifier(), 1, lossMatch2);
		
	}
}
