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
import de.ipbhalle.metfraglib.score.AutomatedLossFingerprintAnnotationScore;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedLossFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class FingerrintAnnotationMatchingLoss_Test {

	protected Settings settings;
	protected AutomatedLossFingerprintAnnotationScoreInitialiser initLoss;
	protected Double lossMass;
	protected ICandidate candidate;
	protected DefaultPeakList peakList;
	
	@Before
	public void setUp() throws AtomTypeNotKnownFromInputListException, Exception {
		this.lossMass = new Double(132.042);
		this.initLoss = new AutomatedLossFingerprintAnnotationScoreInitialiser();
		this.settings = new MetFragGlobalSettings();
		String loss_annotation_file = ClassLoader.getSystemResource("training_values_loss_pos.txt").getFile();
		this.settings.set(VariableNames.FINGERPRINT_LOSS_ANNOTATION_FILE_NAME, loss_annotation_file);
		this.settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		this.settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.005);
		this.settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, 1);
		this.settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 406.126424);
		
		this.peakList = new DefaultPeakList();
		this.peakList.addElement(new TandemMassPeak(275.0919, 4862.9));

		this.settings.set(VariableNames.PEAK_LIST_NAME, this.peakList);

		this.initLoss.initScoreParameters(this.settings);
		
		this.candidate = this.createCandidate();
		this.settings.set(VariableNames.LOG_LEVEL_NAME, Level.INFO);
		this.candidate.setMatchList(this.createMatchList());
		this.settings.set(VariableNames.CANDIDATE_NAME, this.candidate);
		CombinedSingleCandidateMetFragProcess process = new CombinedSingleCandidateMetFragProcess(this.settings, this.candidate);
		process.setWasSuccessful(true);
		this.settings.set(VariableNames.METFRAG_PROCESSES_NAME, new CombinedSingleCandidateMetFragProcess[] {process});
	}

	@Test
	public void test1() throws Exception {
		double mzppm = (double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		double mzabs = (double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
			
		MassToFingerprintGroupListCollection lossToFingerprintGroupListCollection = (MassToFingerprintGroupListCollection)settings.get(VariableNames.LOSS_TO_FINGERPRINT_GROUP_LIST_COLLECTION_NAME);
		MassToFingerprintGroupList lossToFingerprintGroupList = lossToFingerprintGroupListCollection.getElementByPeak(this.lossMass, mzppm, mzabs);
		assertNotNull("Did not find any matching annotation for " + this.lossMass, lossToFingerprintGroupList);
		
		assertEquals("Incorrect number fingerprints found", lossToFingerprintGroupList.getNumberElements(), 7);
		
		this.initLoss.postProcessScoreParameters(this.settings);
		
		MassFingerprintMatch mfm = ((MassFingerprintMatch)((java.util.ArrayList<?>)this.candidate.getProperty("LossMatchList")).get(0));
		assertEquals("Incorrect fingerprint", mfm.getFingerprint().toString(), "000000000000000000000000000000000000000000000000000011001000000000000001000000000110000011100001000000010000100000000000000000111011000011110010010001011010101000011000000000000000000000000000");
		AutomatedLossFingerprintAnnotationScore score = new AutomatedLossFingerprintAnnotationScore(this.settings);
		score.calculate();
		score.singlePostCalculate();										 
		assertEquals("Incorrect number score value found", score.getValue(), -0.2934606670014012, 0.000000001);
		assertEquals("Incorrect number fingerprint matches found", this.candidate.getProperty("AutomatedLossFingerprintAnnotationScore_Matches"), 1);
		assertEquals("Incorrect number fingerprint prob types found", this.candidate.getProperty("AutomatedLossFingerprintAnnotationScore_Probtypes"), "1:0.7456785520607372:132.042349");
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
		int[] toSet = {0,1,2,3,4,5,6,8,9,10,11,12,13,14,18,21,22,23,26,27};
		FastBitArray atomsBitArray = new FastBitArray(29);
		for(int id : toSet) atomsBitArray.set(id);
		return atomsBitArray;
	}

	protected FastBitArray createBondsFastBitArray() {
		int[] toSet = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,17,18,21,22,26,28,29};
		FastBitArray bondsBitArray = new FastBitArray(32);
		for(int id : toSet) bondsBitArray.set(id);
		return bondsBitArray;
	}

	protected FastBitArray createBrokenBondsFastBitArray() {
		int[] toSet = {27};
		FastBitArray bondsBitArray = new FastBitArray(32);
		for(int id : toSet) bondsBitArray.set(id);
		return bondsBitArray;
	}
	
	protected ICandidate createCandidate() throws AtomTypeNotKnownFromInputListException, Exception {
		ICandidate precursorCandidate = new TopDownPrecursorCandidate("InChI=1S/C20H22O9/c21-8-16-17(25)18(26)20(29-16)27-11-5-13(23)12-7-14(24)19(28-15(12)6-11)9-1-3-10(22)4-2-9/h1-6,14,16-26H,7-8H2/t14-,16+,17-,18+,19-,20+/m1/s1", "124796410");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, "XWFQRXGYJJOFCO");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, "UFGPKIGGSA");
		precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, "XWFQRXGYJJOFCO-UFGPKIGGSA-N");
		precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, "C20H22O9");
		precursorCandidate.setProperty(VariableNames.COMPOUND_NAME_NAME, "ZINC247888785");
		precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, 406.126);	
		precursorCandidate.initialisePrecursorCandidate();
		return precursorCandidate;
	}
}
