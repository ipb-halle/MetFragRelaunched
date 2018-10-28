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
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class FingerprintAnnotationMatchingPeak_2_Test {

	protected MetFragGlobalSettings settings;
	protected AutomatedPeakFingerprintAnnotationScoreInitialiser initPeak;
	protected ICandidate candidate;
	protected DefaultPeakList peakList;
	protected org.apache.log4j.Logger logger;
	protected CombinedMetFragProcess mp;
	
	@Before
	public void setUp() throws AtomTypeNotKnownFromInputListException, Exception {
		this.initPeak = new AutomatedPeakFingerprintAnnotationScoreInitialiser();
		String parameter_file = ClassLoader.getSystemResource("parameter_file_example_2.txt").getFile();
		this.settings = MetFragGlobalSettings.readSettings(new File(parameter_file), logger);
		String peak_annotation_file = ClassLoader.getSystemResource("training_values_peak_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME, peak_annotation_file);
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
		Object prop1 = scoredCandidateList.getElement(0).getProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes");
		Object prop2 = scoredCandidateList.getElement(1).getProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes");
		assertNotNull("PeakScore Propery null in candidate " + scoredCandidateList.getElement(0).getIdentifier(), prop1);
		assertNotNull("PeakScore Propery null in candidate " + scoredCandidateList.getElement(1).getIdentifier(), prop2);
		
		String[] probTypes1 = ((String)prop1).split(";");
		String[] probTypes2 = ((String)prop2).split(";");
		
		for(int i = 0; i < probTypes1.length; i++)
			System.out.println(probTypes1[i] + " " + probTypes2[i]);
		
		assertEquals("Number probability types not as expected", probTypes1.length, 16);
		assertEquals("Number probability types not as expected", probTypes2.length, 16);
		
		Double peakScore1 = (Double)scoredCandidateList.getElement(0).getProperty("AutomatedPeakFingerprintAnnotationScore");
		Double peakScore2 = (Double)scoredCandidateList.getElement(1).getProperty("AutomatedPeakFingerprintAnnotationScore");
		
		System.out.println(peakScore1);
		System.out.println(peakScore2);
		
		assertEquals("PeakScore not as expected for candidate " + scoredCandidateList.getElement(0).getIdentifier(), -141.846834822782, peakScore1, 0.00000001);
		assertEquals("PeakScore not as expected for candidate " + scoredCandidateList.getElement(1).getIdentifier(), -159.966937375654, peakScore2, 0.00000001);
	
		int peakMatch1 = (Integer)scoredCandidateList.getElement(0).getProperty("AutomatedPeakFingerprintAnnotationScore_Matches");
		int peakMatch2 = (Integer)scoredCandidateList.getElement(1).getProperty("AutomatedPeakFingerprintAnnotationScore_Matches");
		
		assertEquals("PeakMatchNumber not as expected for candidate " + scoredCandidateList.getElement(0).getIdentifier(), 3, peakMatch1);
		assertEquals("PeakMatchNumber not as expected for candidate " + scoredCandidateList.getElement(1).getIdentifier(), 1, peakMatch2);
	}
}
