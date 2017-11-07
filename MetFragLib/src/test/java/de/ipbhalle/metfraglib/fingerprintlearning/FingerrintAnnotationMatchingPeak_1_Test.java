package de.ipbhalle.metfraglib.fingerprintlearning;

import static org.junit.Assert.*;

import org.apache.log4j.Level;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragment.TopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.match.MassFingerprintMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.precursor.TopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.process.CombinedSingleCandidateMetFragProcess;
import de.ipbhalle.metfraglib.score.AutomatedPeakFingerprintAnnotationScore;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class FingerrintAnnotationMatchingPeak_1_Test {

	protected Settings settings;
	protected AutomatedPeakFingerprintAnnotationScoreInitialiser initPeak;
	protected Double peakMass;
	protected ICandidate candidate;
	protected DefaultPeakList peakList;
	
	@Before
	public void setUp1() throws Exception {
		this.peakMass = new Double(166.0230);
		this.initPeak = new AutomatedPeakFingerprintAnnotationScoreInitialiser();
		this.settings = new MetFragGlobalSettings();
		String peak_annotation_file = ClassLoader.getSystemResource("training_values_peak_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_PEAK_ANNOTATION_FILE_NAME, peak_annotation_file);
		this.settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		this.settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.005);
		
		this.peakList = new DefaultPeakList();
		this.peakList.addElement(new TandemMassPeak(this.peakMass, 4180.9));
		
		this.settings.set(VariableNames.PEAK_LIST_NAME, this.peakList);
		
		this.initPeak.initScoreParameters(this.settings);

		this.settings.set(VariableNames.LOG_LEVEL_NAME, Level.INFO);
		this.candidate = this.createCandidate();
		this.candidate.setMatchList(this.createMatchList());
		this.settings.set(VariableNames.CANDIDATE_NAME, this.candidate);
		CombinedSingleCandidateMetFragProcess process = new CombinedSingleCandidateMetFragProcess(this.settings, this.candidate);
		process.setWasSuccessful(true);
		this.settings.set(VariableNames.METFRAG_PROCESSES_NAME, new CombinedSingleCandidateMetFragProcess[] {process});
	}
	
	@Test
	public void test1() throws Exception {
		
		assertEquals("Incorrect PeakFingerprintDenominatorCount", (double)this.settings.get(VariableNames.PEAK_FINGERPRINT_DENOMINATOR_COUNT_NAME), 97947.0, 0.000001);
		assertEquals("Incorrect PeakFingerprintTupleCount", (double)this.settings.get(VariableNames.PEAK_FINGERPRINT_TUPLE_COUNT_NAME), 47409.0, 0.000001);
		
		double mzppm = (double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		double mzabs = (double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.PEAK_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeak(166.0230, mzppm, mzabs);
		assertNotNull("Did not find any matching annotation for " + this.peakMass, peakToFingerprintGroupList);
		
		assertEquals("Incorrect number fingerprints found", peakToFingerprintGroupList.getNumberElements(), 14);

		this.initPeak.postProcessScoreParameters(this.settings);
		
		MassFingerprintMatch mfm = ((MassFingerprintMatch)((java.util.ArrayList<?>)this.candidate.getProperty("PeakMatchList")).get(0));
		assertEquals("Incorrect fingerprint", mfm.getFingerprint().toString(), "000000000000000000000000000000000000000000000000000010000000000000000000000000000000000010000000000000000000000010000000000000100010000000110011010001010100101101111000000000000000000000000000");
		AutomatedPeakFingerprintAnnotationScore score = new AutomatedPeakFingerprintAnnotationScore(this.settings);
		score.calculate();
		score.singlePostCalculate();
		assertEquals("Incorrect number score value found", score.getValue(), -2.2337945145590177, 0.000000001);
		assertEquals("Incorrect number fingerprint matches found", this.candidate.getProperty("AutomatedPeakFingerprintAnnotationScore_Matches"), 1);
		assertEquals("Incorrect number fingerprint prob types found", this.candidate.getProperty("AutomatedPeakFingerprintAnnotationScore_Probtypes"), "1:0.10712118507942177:166.023");
	}
	
	/*
	 * 
	 * add helper functions
	 * 
	 */
	
	protected MatchList createMatchList() throws AtomTypeNotKnownFromInputListException {
		MatchList matchList = new MatchList();
		FragmentMassToPeakMatch match = new FragmentMassToPeakMatch((IPeak)this.peakList.getElement(0));
		IFragment fragment = new TopDownBitArrayFragment((TopDownBitArrayPrecursor)this.candidate.getPrecursorMolecule(),
				this.createAtomsFastBitArray(), 
				this.createBondsFastBitArray(), 
				this.createBrokenBondsFastBitArray(), 5);
		match.addMatchedFragment(fragment, (byte)0, 1.0);
		match.setBestMatchedFragment(fragment);
		matchList.addElement(match);
		return matchList;
	}
	
	protected FastBitArray createAtomsFastBitArray() {
		int[] toSet = {8,9,14,15,16,18,19,20,21,22,23,25};
		FastBitArray atomsBitArray = new FastBitArray(26);
		for(int id : toSet) atomsBitArray.set(id);
		return atomsBitArray;
	}

	protected FastBitArray createBondsFastBitArray() {
		int[] toSet = {10,11,12,16,17,18,19,20,21,22,23,27};
		FastBitArray bondsBitArray = new FastBitArray(28);
		for(int id : toSet) bondsBitArray.set(id);
		return bondsBitArray;
	}

	protected FastBitArray createBrokenBondsFastBitArray() {
		int[] toSet = {2,13,26};
		FastBitArray bondsBitArray = new FastBitArray(28);
		for(int id : toSet) bondsBitArray.set(id);
		return bondsBitArray;
	}
	
	protected ICandidate createCandidate() throws AtomTypeNotKnownFromInputListException, Exception {
		ICandidate precursorCandidate = new TopDownPrecursorCandidate("InChI=1S/C21H22O5/c1-12(2)4-9-15-16(22)10-19-20(21(15)24)17(23)11-18(26-19)13-5-7-14(25-3)8-6-13/h4-8,10,18,22,24H,9,11H2,1-3H3/t18-/m0/s1", "35756082");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, "ZTDRTMVBTLHQII");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, "UHFFFAOYSA");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, "ZTDRTMVBTLHQII-UHFFFAOYSA-N");
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, "C21H22O5");
		precursorCandidate.setProperty(VariableNames.COMPOUND_NAME_NAME, "ZINC28540272");
		precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, 354.147);	
		precursorCandidate.initialisePrecursorCandidate();
		return precursorCandidate;
	}
}
