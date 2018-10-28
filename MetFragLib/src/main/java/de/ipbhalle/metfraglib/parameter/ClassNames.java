package de.ipbhalle.metfraglib.parameter;

import de.ipbhalle.metfraglib.candidatefilter.PostProcessingCandidateHDGroupFlagFilter;
import de.ipbhalle.metfraglib.candidatefilter.PostProcessingCandidateInChIKeyFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementExclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementInclusionExclusiveFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementInclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementInclusionOptionalFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateIsotopeFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateMaximalElementFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateMinimalElementFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsExclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsInclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSuspectListFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateUnconnectedStructureFilter;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalChEBIDatabase;
import de.ipbhalle.metfraglib.database.LocalDerivatisedKeggDatabase;
import de.ipbhalle.metfraglib.database.LocalExtendedMetChemDatabase;
import de.ipbhalle.metfraglib.database.LocalExtendedPubChemDatabase;
import de.ipbhalle.metfraglib.database.LocalInMemoryDatabase;
import de.ipbhalle.metfraglib.database.LocalMetChemDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.database.LocalKeggDatabase;
import de.ipbhalle.metfraglib.database.LocalLipidMapsDatabase;
import de.ipbhalle.metfraglib.database.LocalMySQLDatabase;
import de.ipbhalle.metfraglib.database.LocalPostgresDatabase;
import de.ipbhalle.metfraglib.database.LocalPubChemDatabase;
import de.ipbhalle.metfraglib.database.LocalPropertyFileDatabase;
import de.ipbhalle.metfraglib.database.LocalSDFDatabase;
import de.ipbhalle.metfraglib.database.OnlineChemSpiderDatabase;
import de.ipbhalle.metfraglib.database.OnlineChemSpiderRestDatabase;
import de.ipbhalle.metfraglib.database.OnlineExtendedPubChemDatabase;
import de.ipbhalle.metfraglib.database.OnlineForIdentDatabase;
import de.ipbhalle.metfraglib.database.OnlineKeggDatabase;
import de.ipbhalle.metfraglib.database.OnlineMetaCycDatabase;
import de.ipbhalle.metfraglib.database.OnlinePubChemDatabase;
import de.ipbhalle.metfraglib.score.AutomatedPeakFingerprintAnnotationScore;
import de.ipbhalle.metfraglib.score.AutomatedLossFingerprintAnnotationScore;
import de.ipbhalle.metfraglib.score.AutomatedLossSubstructureAnnotationScore;
import de.ipbhalle.metfraglib.score.AutomatedPeakSubstructureAnnotationScore;
import de.ipbhalle.metfraglib.score.CandidatePropertyScore;
import de.ipbhalle.metfraglib.score.CombinedAutomatedAnnotationScore;
import de.ipbhalle.metfraglib.score.CombinedReferenceScore;
import de.ipbhalle.metfraglib.score.HDExchangedHydrogendsScore;
import de.ipbhalle.metfraglib.score.HDFragmentPairScore;
import de.ipbhalle.metfraglib.score.HDNewFragmenterScore;
import de.ipbhalle.metfraglib.score.IndividualMoNASpectralSimilarity;
import de.ipbhalle.metfraglib.score.MatchSpectrumCosineSimilarityScore;
import de.ipbhalle.metfraglib.score.NewFragmenterScore;
import de.ipbhalle.metfraglib.score.NewFragmenterLipidScore;
import de.ipbhalle.metfraglib.score.NewFragmenterUniqueFormulaScore;
import de.ipbhalle.metfraglib.score.OfflineIndividualMoNASpectralSimilarity;
import de.ipbhalle.metfraglib.score.OfflineMetFusionSpectralSimilarityScore;
import de.ipbhalle.metfraglib.score.RetentionTimeScore;
import de.ipbhalle.metfraglib.score.SmartsSubstructureExclusionScore;
import de.ipbhalle.metfraglib.score.SmartsSubstructureInclusionScore;
import de.ipbhalle.metfraglib.score.SuspectListScore;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedPeakFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.AutomatedLossFingerprintAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.CombinedAutomatedAnnotationScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.DefaultScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.IndividualMoNASpectralSimilarityInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.OfflineIndividualMoNASpectralSimilarityInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.OfflineMetFusionSpectralSimilarityScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.RetentionTimeScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SmartsSubstructureExclusionScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SmartsSubstructureInclusionScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SuspectListScoreInitialiser;
import de.ipbhalle.metfraglib.writer.CandidateListWriterLossFragmentSmilesPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedFragmentsXLS;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedXLS;
import de.ipbhalle.metfraglib.writer.CandidateListWriterFragmentSmilesPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterLossFragmentSmilesCompletePSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterLossFragmentSmilesExtendedPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterSDF;
import de.ipbhalle.metfraglib.writer.CandidateListWriterXLS;
import de.ipbhalle.metfraglib.writer.FragmentListWriterSDF;
import de.ipbhalle.metfraglib.writer.HDCandidateListWriterExtendedPSV;
import de.ipbhalle.metfraglib.writer.HDCandidateListWriterPSV;

public class ClassNames {

	private static final java.util.HashMap<String, String> scoreNameToClassName;
	private static final java.util.HashMap<String, String> scoreNameToScoreInitialiserClassName;
	private static final java.util.HashMap<String, String> databaseNameToClassName;
	private static final java.util.HashMap<String, String> candidateListWriterNameToClassName;
	private static final java.util.HashMap<String, String> fragmentListWriterNameToClassName;
	private static final java.util.HashMap<String, String> preProcessingCandidateFilterNameToClassName;
	private static final java.util.HashMap<String, String> postProcessingCandidateFilterNameToClassName;
	private static final java.util.HashMap<String, String> fingerprinterNameToClassName;
	
	static {
		/*
		 * new implemented scores have to be added here
		 */
		scoreNameToClassName = new java.util.HashMap<String, String>();
		databaseNameToClassName = new java.util.HashMap<String, String>();
		candidateListWriterNameToClassName = new java.util.HashMap<String, String>();
		postProcessingCandidateFilterNameToClassName = new java.util.HashMap<String, String>();
		scoreNameToScoreInitialiserClassName = new java.util.HashMap<String, String>();
		preProcessingCandidateFilterNameToClassName = new java.util.HashMap<String, String>();
		fragmentListWriterNameToClassName = new java.util.HashMap<String, String>();
		fingerprinterNameToClassName = new java.util.HashMap<String, String>();
		
		fingerprinterNameToClassName.put("MACCSFingerprinter", org.openscience.cdk.fingerprint.MACCSFingerprinter.class.getName());
		fingerprinterNameToClassName.put("CircularFingerprinter", org.openscience.cdk.fingerprint.CircularFingerprinter.class.getName());
		fingerprinterNameToClassName.put("LingoFingerprinter", org.openscience.cdk.fingerprint.LingoFingerprinter.class.getName());
		fingerprinterNameToClassName.put("GraphOnlyFingerprinter", org.openscience.cdk.fingerprint.GraphOnlyFingerprinter.class.getName());
		fingerprinterNameToClassName.put("ShortestPathFingerprinter", org.openscience.cdk.fingerprint.ShortestPathFingerprinter.class.getName());
		
		/*
		 * score to class
		 */
		scoreNameToClassName.put(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME, NewFragmenterScore.class.getName());
		scoreNameToClassName.put("FragmenterLipidScore", NewFragmenterLipidScore.class.getName());
		scoreNameToClassName.put("FragmenterUniqueFormulaScore", NewFragmenterUniqueFormulaScore.class.getName());
		scoreNameToClassName.put("SmartsSubstructureInclusionScore", SmartsSubstructureInclusionScore.class.getName());
		scoreNameToClassName.put("SmartsSubstructureExclusionScore", SmartsSubstructureExclusionScore.class.getName());
		scoreNameToClassName.put("SuspectListScore", SuspectListScore.class.getName());
		scoreNameToClassName.put("RetentionTimeScore", RetentionTimeScore.class.getName());
		scoreNameToClassName.put("CombinedReferenceScore", CombinedReferenceScore.class.getName());
		scoreNameToClassName.put("SimScore", MatchSpectrumCosineSimilarityScore.class.getName());
		scoreNameToClassName.put("OfflineIndividualMoNAScore", OfflineIndividualMoNASpectralSimilarity.class.getName());

		scoreNameToClassName.put("CombinedAutomatedAnnotationScore", CombinedAutomatedAnnotationScore.class.getName());
		scoreNameToClassName.put("AutomatedSubstructureAnnotationScore", AutomatedPeakSubstructureAnnotationScore.class.getName());
		scoreNameToClassName.put("AutomatedPeakFingerprintAnnotationScore", AutomatedPeakFingerprintAnnotationScore.class.getName());
		scoreNameToClassName.put("AutomatedLossFingerprintAnnotationScore", AutomatedLossFingerprintAnnotationScore.class.getName());
		scoreNameToClassName.put("AutomatedLossAnnotationScore", AutomatedLossSubstructureAnnotationScore.class.getName());
		scoreNameToClassName.put("IndividualMoNAScore", IndividualMoNASpectralSimilarity.class.getName());
		scoreNameToClassName.put("OfflineMetFusionScore", OfflineMetFusionSpectralSimilarityScore.class.getName());
		
		/*
		 * each score needs its global init class to set shared objects once
		 */
		scoreNameToScoreInitialiserClassName.put(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME, DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterHierarchicalScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterScoreLipids", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterUniqueFormulaScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SmartsSubstructureInclusionScore", SmartsSubstructureInclusionScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SmartsSubstructureExclusionScore", SmartsSubstructureExclusionScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SuspectListScore", SuspectListScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("RetentionTimeScore", RetentionTimeScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("CombinedReferenceScore", DefaultScoreInitialiser.class.getName());

		scoreNameToScoreInitialiserClassName.put("AutomatedAnnotationScore", CombinedAutomatedAnnotationScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("AutomatedPeakFingerprintAnnotationScore", AutomatedPeakFingerprintAnnotationScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("AutomatedLossFingerprintAnnotationScore", AutomatedLossFingerprintAnnotationScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("IndividualMoNAScore", IndividualMoNASpectralSimilarityInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("OfflineIndividualMoNAScore", OfflineIndividualMoNASpectralSimilarityInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("OfflineMetFusionScore", OfflineMetFusionSpectralSimilarityScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("MatchSpectrumCosineSimilarityScore", DefaultScoreInitialiser.class.getName());
		
		/*
		 * HDX
		 */
		scoreNameToClassName.put("HDFragmenterScore", HDNewFragmenterScore.class.getName());
		scoreNameToClassName.put("HDFragmentPairScore", HDFragmentPairScore.class.getName());
		scoreNameToClassName.put("HDExchangedHydrogendsScore", HDExchangedHydrogendsScore.class.getName());
		
		scoreNameToScoreInitialiserClassName.put("HDFragmenterScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("HDFragmentPairScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("HDFragmentPairScore2", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("HDExchangedHydrogendsScore", DefaultScoreInitialiser.class.getName());
		
		candidateListWriterNameToClassName.put("HDCSV", HDCandidateListWriterPSV.class.getName());
		candidateListWriterNameToClassName.put("ExtendedHDCSV", HDCandidateListWriterExtendedPSV.class.getName());
		
		postProcessingCandidateFilterNameToClassName.put("HDGroupFlagFilter", PostProcessingCandidateHDGroupFlagFilter.class.getName());
		
		/*
		 * new implemented databases have to be added here
		 */
		databaseNameToClassName.put("KEGG", OnlineKeggDatabase.class.getName());
		databaseNameToClassName.put("MetaCyc", OnlineMetaCycDatabase.class.getName());
		databaseNameToClassName.put("PubChem", OnlinePubChemDatabase.class.getName());
		databaseNameToClassName.put("ExtendedPubChem", OnlineExtendedPubChemDatabase.class.getName());
		databaseNameToClassName.put("ChemSpider", OnlineChemSpiderDatabase.class.getName());
		databaseNameToClassName.put("ChemSpiderRest", OnlineChemSpiderRestDatabase.class.getName());
		databaseNameToClassName.put("LocalPSV", LocalPSVDatabase.class.getName());
		databaseNameToClassName.put("LocalCSV", LocalCSVDatabase.class.getName());
		databaseNameToClassName.put("LocalProperty", LocalPropertyFileDatabase.class.getName());
		databaseNameToClassName.put("FOR-IDENT", OnlineForIdentDatabase.class.getName());
		databaseNameToClassName.put("LocalSDF", LocalSDFDatabase.class.getName());
		databaseNameToClassName.put("LipidMaps", LocalLipidMapsDatabase.class.getName());
		databaseNameToClassName.put("Postgres", LocalPostgresDatabase.class.getName());
		databaseNameToClassName.put("MySQL", LocalMySQLDatabase.class.getName());
		databaseNameToClassName.put("LocalKegg", LocalKeggDatabase.class.getName());
		databaseNameToClassName.put("LocalDerivatisedKegg", LocalDerivatisedKeggDatabase.class.getName());
		databaseNameToClassName.put("LocalPubChem", LocalPubChemDatabase.class.getName());
		databaseNameToClassName.put("LocalExtendedPubChem", LocalExtendedPubChemDatabase.class.getName());
		databaseNameToClassName.put("LocalInMemoryDatabase", LocalInMemoryDatabase.class.getName());
		databaseNameToClassName.put("LocalChEBI", LocalChEBIDatabase.class.getName());
		databaseNameToClassName.put("MetChem", LocalMetChemDatabase.class.getName());
		databaseNameToClassName.put("ExtendedMetChem", LocalExtendedMetChemDatabase.class.getName());
		
		/*
		 * new implemented candidateListWriter have to be added here
		 */
		candidateListWriterNameToClassName.put("CSV", CandidateListWriterCSV.class.getName());
		candidateListWriterNameToClassName.put("PSV", CandidateListWriterPSV.class.getName());
		candidateListWriterNameToClassName.put("ExtendedPSV", CandidateListWriterExtendedPSV.class.getName());
		candidateListWriterNameToClassName.put("FragmentSmilesPSV", CandidateListWriterFragmentSmilesPSV.class.getName());
		candidateListWriterNameToClassName.put("LossFragmentSmilesPSV", CandidateListWriterLossFragmentSmilesPSV.class.getName());
		candidateListWriterNameToClassName.put("LossFragmentSmilesExtendedPSV", CandidateListWriterLossFragmentSmilesExtendedPSV.class.getName());
		candidateListWriterNameToClassName.put("LossFragmentSmilesCompletePSV", CandidateListWriterLossFragmentSmilesCompletePSV.class.getName());
		candidateListWriterNameToClassName.put("SDF", CandidateListWriterSDF.class.getName());
		candidateListWriterNameToClassName.put("XLS", CandidateListWriterXLS.class.getName());
		candidateListWriterNameToClassName.put("ExtendedXLS", CandidateListWriterExtendedXLS.class.getName());
		candidateListWriterNameToClassName.put("ExtendedFragmentsXLS", CandidateListWriterExtendedFragmentsXLS.class.getName());
		/*
		 * new implemented preProcessingCandidateFilter have to be added here
		 */
		preProcessingCandidateFilterNameToClassName.put("UnconnectedCompoundFilter", PreProcessingCandidateUnconnectedStructureFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("IsotopeFilter", PreProcessingCandidateIsotopeFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementExclusionFilter", PreProcessingCandidateElementExclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementInclusionExclusiveFilter", PreProcessingCandidateElementInclusionExclusiveFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementInclusionOptionalFilter", PreProcessingCandidateElementInclusionOptionalFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementInclusionFilter", PreProcessingCandidateElementInclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SmartsSubstructureExclusionFilter", PreProcessingCandidateSmartsExclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SmartsSubstructureInclusionFilter", PreProcessingCandidateSmartsInclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SuspectListFilter", PreProcessingCandidateSuspectListFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("MaximumElementsFilter", PreProcessingCandidateMaximalElementFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("MinimumElementsFilter", PreProcessingCandidateMinimalElementFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SubstructureInformationFilter", PreProcessingCandidateSmartsFilter.class.getName());
		/*
		 * new implemented preProcessingCandidateFilter have to be added here
		 */
		postProcessingCandidateFilterNameToClassName.put("InChIKeyFilter", PostProcessingCandidateInChIKeyFilter.class.getName());
		
		fragmentListWriterNameToClassName.put("SDF", FragmentListWriterSDF.class.getName());
	}
	
	public static String getClassNameOfScoreInitialiser(String scoreName) {
		String className = scoreNameToScoreInitialiserClassName.get(scoreName);
		if(className == null) className = DefaultScoreInitialiser.class.getName();
		return className;
	}

	public static String getClassNameOfScore(String scoreName) {
		String className = scoreNameToClassName.get(scoreName);
		if(className == null) className = CandidatePropertyScore.class.getName();
		return className;
	}
	
	public static String getClassNameOfDatabase(String databaseName) {
		return databaseNameToClassName.get(databaseName);
	}
	
	public static String getClassNameOfCandidateListWriter(String candidateListName) {
		return candidateListWriterNameToClassName.get(candidateListName);
	}

	public static String getClassNameOfFragmentListWriter(String candidateListName) {
		return fragmentListWriterNameToClassName.get(candidateListName);
	}
	
	public static String getClassNameOfPreProcessingCandidateFilter(String candidateFilterName) {
		return preProcessingCandidateFilterNameToClassName.get(candidateFilterName);
	}
	
	public static String getClassNameOfPostProcessingCandidateFilter(String candidateFilterName) {
		return postProcessingCandidateFilterNameToClassName.get(candidateFilterName);
	}
	
	public static boolean containsScore(String scoreName) {
		return scoreNameToClassName.containsKey(scoreName);
	}
	
	public static boolean containsFingerprintType(String scoreName) {
		return fingerprinterNameToClassName.containsKey(scoreName);
	}
	
	public static String getClassOfFingerprintClassName(String fingerprintName) {
		return fingerprinterNameToClassName.get(fingerprintName);
	}
	
	public static String[] getFingerprintNames() {
		String[] fingerprintClassNames = new String[fingerprinterNameToClassName.size()];
		java.util.Iterator<?> it = fingerprinterNameToClassName.keySet().iterator();
		int index = 0;
		while(it.hasNext()) {
			fingerprintClassNames[index] = (String)it.next();
			index++;
		}
		return fingerprintClassNames;
	}
}
