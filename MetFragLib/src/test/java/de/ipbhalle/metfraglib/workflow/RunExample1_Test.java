package de.ipbhalle.metfraglib.workflow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class RunExample1_Test {

	private final int EXPECTED_NUMBER_CANDIDATES = 167; 
	private final int EXPECTED_NUMBER_PEAKS_USED = 22; 
	private final int EXPECTED_NUMBER_PEAKS_EXPLAINED = 18; 
	private final int EXPECTED_RANK = 1; 
	private final double EXPECTED_SCORE_CORRECT = 1.0;
	private final double EXPECTED_FRAGMENTER_SCORE_CORRECT = 1197.32678016043;
	private final String INCHIKEY1_CORRECT = "MEFQWPUMEMWTJP"; 
	
	private CombinedMetFragProcess metfragProcess;
	
	@Before
	public void setUp() throws Exception {
		String peakListFilePath = ClassLoader.getSystemResource("peaklist_file_example_1.txt").getFile();
		String candidateListFilePath = ClassLoader.getSystemResource("candidate_file_example_1.txt").getFile();
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		//set peaklist path and candidate list path
		settings.set(VariableNames.PEAK_LIST_PATH_NAME, peakListFilePath);
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, candidateListFilePath);
		//set needed parameters
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.001);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 253.966126);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalCSV");
		
		this.metfragProcess = new CombinedMetFragProcess(settings);
	}

	@Test
	public void test() {
		try {
			this.metfragProcess.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.metfragProcess.run();
		
		ScoredCandidateList scoredCandidateList = (ScoredCandidateList)this.metfragProcess.getCandidateList();
		//check number candidates
		assertEquals("Error: Expected " + EXPECTED_NUMBER_CANDIDATES + " candidates. Found " + scoredCandidateList.getNumberElements(), EXPECTED_NUMBER_CANDIDATES, scoredCandidateList.getNumberElements());
		ICandidate correctCandidate = null;
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			String inchikey1 = (String)scoredCandidateList.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
			if(inchikey1.equals(INCHIKEY1_CORRECT)) correctCandidate = scoredCandidateList.getElement(i);
		}
		assertNotNull("Error: No candidate found with InChIKey part one equals " + INCHIKEY1_CORRECT, correctCandidate);
		
		int numberPeaksUsed = scoredCandidateList.getNumberPeaksUsed();
		int numberPeaksExplained = correctCandidate.getMatchList().getNumberElements();
		double fragmenterScore = (Double)correctCandidate.getProperty(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME);
		double score = (Double)correctCandidate.getProperty(VariableNames.FINAL_SCORE_COLUMN_NAME);
		
		assertEquals("Error: Expected " + EXPECTED_NUMBER_PEAKS_USED + " peaks used from peak list. Found " + numberPeaksUsed, EXPECTED_NUMBER_PEAKS_USED, numberPeaksUsed);
		assertEquals("Error: Expected " + EXPECTED_NUMBER_PEAKS_EXPLAINED + " explained peaks. Found " + numberPeaksExplained, EXPECTED_NUMBER_PEAKS_EXPLAINED, numberPeaksExplained);
		assertEquals("Error: Expected " + EXPECTED_FRAGMENTER_SCORE_CORRECT + " as FragmenterScore. Found " + fragmenterScore, EXPECTED_FRAGMENTER_SCORE_CORRECT, fragmenterScore, 0.0001);
		assertEquals("Error: Expected " + EXPECTED_SCORE_CORRECT + " as Score. Found " + score, EXPECTED_SCORE_CORRECT, score, 0.0000001);

		int rank = 0;
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			if((Double)scoredCandidateList.getElement(i).getProperty(VariableNames.FINAL_SCORE_COLUMN_NAME) >= score)
				rank++;
		}
		assertEquals("Error: Expected " + EXPECTED_RANK + " as rank. Found " + rank, EXPECTED_RANK, rank);

	}

}
