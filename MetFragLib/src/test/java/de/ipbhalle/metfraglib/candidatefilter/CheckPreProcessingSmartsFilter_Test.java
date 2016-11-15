package de.ipbhalle.metfraglib.candidatefilter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class CheckPreProcessingSmartsFilter_Test {

	private PreProcessingCandidateSmartsFilter candidateFilter;
	private Settings settings;
	private ICandidate[] toTestCandidates = new ICandidate[4];
	
	@Before
	public void setUp() {
		//init candidate objects
		toTestCandidates[0] = new PrecursorCandidate("InChI=1S/C12H19NO5S2/c1-12(2,3)18-11(14)13-9(8-17-20(4,15)16)10-6-5-7-19-10/h5-7,9H,8H2,1-4H3,(H,13,14)/t9-/m0/s1", "13877939");
		toTestCandidates[0].setProperty(VariableNames.INCHI_KEY_1_NAME, "LGXPPQPBQCFQPU");
		toTestCandidates[1] = new PrecursorCandidate("InChI=1S/C12H19NO5S2/c1-13(19(2,14)15)12-8-6-11(7-9-12)5-4-10-18-20(3,16)17/h6-9H,4-5,10H2,1-3H3", "59122053");
		toTestCandidates[1].setProperty(VariableNames.INCHI_KEY_1_NAME, "CNMCSJSREOGBCU");
		toTestCandidates[2] = new PrecursorCandidate("InChI=1S/C12H19NO5S2/c1-19(14,15)13-12-8-6-11(7-9-12)5-3-4-10-18-20(2,16)17/h6-9,13H,3-5,10H2,1-2H3", "54525898");
		toTestCandidates[2].setProperty(VariableNames.INCHI_KEY_1_NAME, "YTBWCCSOEYFHGQ");
		toTestCandidates[3] = new PrecursorCandidate("InChI=1S/C15H12O5/c16-9-3-1-8(2-4-9)13-7-12(19)15-11(18)5-10(17)6-14(15)20-13/h1-6,13,16-18H,7H2", "932");
		toTestCandidates[3].setProperty(VariableNames.INCHI_KEY_1_NAME, "FTVWIRXFELQLPI");
		
		this.settings = new Settings();
	}
	
	@Test
	public void test1() {
		this.settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, "CCC and c1ccccc1");
		this.candidateFilter = new PreProcessingCandidateSmartsFilter(this.settings);
		
		assertFalse("LGXPPQPBQCFQPU matches 'CCC and c1ccccc1'", this.candidateFilter.passesFilter(this.toTestCandidates[0]));
		assertTrue("CNMCSJSREOGBCU does not match 'CCC and c1ccccc1'", this.candidateFilter.passesFilter(this.toTestCandidates[1]));
		assertTrue("YTBWCCSOEYFHGQ does not match 'CCC and c1ccccc1'", this.candidateFilter.passesFilter(this.toTestCandidates[2]));
		assertTrue("FTVWIRXFELQLPI does not match 'CCC and c1ccccc1'", this.candidateFilter.passesFilter(this.toTestCandidates[3]));
	}

	@Test
	public void test2() {
		this.settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, "( CCC or c1ccccc1 ) and S(=O)=O");
		this.candidateFilter = new PreProcessingCandidateSmartsFilter(this.settings);
		
		assertTrue("LGXPPQPBQCFQPU does not match '( CCC or c1ccccc1 ) and S(=O)=O'", this.candidateFilter.passesFilter(this.toTestCandidates[0]));
		assertTrue("CNMCSJSREOGBCU does not match '( CCC or c1ccccc1 ) and S(=O)=O'", this.candidateFilter.passesFilter(this.toTestCandidates[1]));
		assertTrue("YTBWCCSOEYFHGQ does not match '( CCC or c1ccccc1 ) and S(=O)=O'", this.candidateFilter.passesFilter(this.toTestCandidates[2]));
		assertFalse("FTVWIRXFELQLPI matches '( CCC or c1ccccc1 ) and S(=O)=O'", this.candidateFilter.passesFilter(this.toTestCandidates[3]));
	}

	@Test
	public void test3() {
		this.settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, "not (( CCC or c1ccccc1 ) and S(=O)=O )");
		this.candidateFilter = new PreProcessingCandidateSmartsFilter(this.settings);
		
		assertFalse("LGXPPQPBQCFQPU matches 'not (( CCC or c1ccccc1 ) and S(=O)=O)'", this.candidateFilter.passesFilter(this.toTestCandidates[0]));
		assertFalse("CNMCSJSREOGBCU matches 'not (( CCC or c1ccccc1 ) and S(=O)=O)'", this.candidateFilter.passesFilter(this.toTestCandidates[1]));
		assertFalse("YTBWCCSOEYFHGQ matches 'not (( CCC or c1ccccc1 ) and S(=O)=O)'", this.candidateFilter.passesFilter(this.toTestCandidates[2]));
		assertTrue("FTVWIRXFELQLPI does not match 'not (( CCC or c1ccccc1 ) and S(=O)=O)'", this.candidateFilter.passesFilter(this.toTestCandidates[3]));
	}

	@Test (expected=IllegalArgumentException.class)
	public void testIllegalArgumentException_Brackets() {
		//check correct brackets
		this.settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, "not ( S(=O)=O)");
		this.candidateFilter = new PreProcessingCandidateSmartsFilter(this.settings);
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testIllegalArgumentException_Smarts() {
		//check correct smarts
		this.settings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, "S(=O)=O=");
		this.candidateFilter = new PreProcessingCandidateSmartsFilter(this.settings);
	}

}
