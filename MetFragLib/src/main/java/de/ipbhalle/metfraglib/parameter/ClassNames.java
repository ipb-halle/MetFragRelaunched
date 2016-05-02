package de.ipbhalle.metfraglib.parameter;

import de.ipbhalle.metfraglib.candidatefilter.PostProcessingCandidateHDGroupFlagFilter;
import de.ipbhalle.metfraglib.candidatefilter.PostProcessingCandidateInChIKeyFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementExclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementInclusionExclusiveFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateElementInclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateIsotopeFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateMaximalElementFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateMinimalElementFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsExclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSmartsInclusionFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateSuspectListFilter;
import de.ipbhalle.metfraglib.candidatefilter.PreProcessingCandidateUnconnectedStructureFilter;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalDerivatisedKeggDatabase;
import de.ipbhalle.metfraglib.database.LocalExtendedPubChemDatabase;
import de.ipbhalle.metfraglib.database.LocalInMemoryDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.database.LocalKeggDatabase;
import de.ipbhalle.metfraglib.database.LocalLipidMapsDatabase;
import de.ipbhalle.metfraglib.database.LocalMySQLDatabase;
import de.ipbhalle.metfraglib.database.LocalPostgresDatabase;
import de.ipbhalle.metfraglib.database.LocalPubChemDatabase;
import de.ipbhalle.metfraglib.database.LocalPropertyFileDatabase;
import de.ipbhalle.metfraglib.database.LocalSDFDatabase;
import de.ipbhalle.metfraglib.database.OnlineChemSpiderDatabase;
import de.ipbhalle.metfraglib.database.OnlineExtendedPubChemDatabase;
import de.ipbhalle.metfraglib.database.OnlineForIdentDatabase;
import de.ipbhalle.metfraglib.database.OnlineKeggDatabase;
import de.ipbhalle.metfraglib.database.OnlineMetaCycDatabase;
import de.ipbhalle.metfraglib.database.OnlinePubChemDatabase;
import de.ipbhalle.metfraglib.score.CandidatePropertyScore;
import de.ipbhalle.metfraglib.score.CombinedReferenceScore;
import de.ipbhalle.metfraglib.score.IndividualMoNASpectralSimilarity;
import de.ipbhalle.metfraglib.score.MatchSpectrumCosineSimilarityScore;
import de.ipbhalle.metfraglib.score.NewFragmenterHierarchicalScore;
import de.ipbhalle.metfraglib.score.NewFragmenterScore;
import de.ipbhalle.metfraglib.score.NewFragmenterScoreLipids;
import de.ipbhalle.metfraglib.score.NewFragmenterUniqueFormulaScore;
import de.ipbhalle.metfraglib.score.OfflineMetFusionSpectralSimilarityScore;
import de.ipbhalle.metfraglib.score.RetentionTimeScore;
import de.ipbhalle.metfraglib.score.SmartsSubstructureExclusionScore;
import de.ipbhalle.metfraglib.score.SmartsSubstructureInclusionScore;
import de.ipbhalle.metfraglib.score.SuspectListScore;
import de.ipbhalle.metfraglib.scoreinitialisation.DefaultScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.IndividualMoNASpectralSimilarityInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.OfflineMetFusionSpectralSimilarityScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.RetentionTimeScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SmartsSubstructureExclusionScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SmartsSubstructureInclusionScoreInitialiser;
import de.ipbhalle.metfraglib.scoreinitialisation.SuspectListScoreInitialiser;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedFragmentsXLS;
import de.ipbhalle.metfraglib.writer.CandidateListWriterExtendedXLS;
import de.ipbhalle.metfraglib.writer.CandidateListWriterFragmentSmilesPSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterSDF;
import de.ipbhalle.metfraglib.writer.CandidateListWriterXLS;
import de.ipbhalle.metfraglib.writer.FragmentListWriterSDF;

public class ClassNames {

	private static final java.util.HashMap<String, String> scoreNameToClassName;
	private static final java.util.HashMap<String, String> scoreNameToScoreInitialiserClassName;
	private static final java.util.HashMap<String, String> databaseNameToClassName;
	private static final java.util.HashMap<String, String> candidateListWriterNameToClassName;
	private static final java.util.HashMap<String, String> fragmentListWriterNameToClassName;
	private static final java.util.HashMap<String, String> preProcessingCandidateFilterNameToClassName;
	private static final java.util.HashMap<String, String> postProcessingCandidateFilterNameToClassName;
	
	static {
		/*
		 * new implemented scores have to be added here
		 */
		scoreNameToClassName = new java.util.HashMap<String, String>();
		scoreNameToClassName.put(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME, NewFragmenterScore.class.getName());
		scoreNameToClassName.put("FragmenterHierarchicalScore", NewFragmenterHierarchicalScore.class.getName());
		scoreNameToClassName.put("FragmenterScoreLipids", NewFragmenterScoreLipids.class.getName());
		scoreNameToClassName.put("FragmenterUniqueFormulaScore", NewFragmenterUniqueFormulaScore.class.getName());
		scoreNameToClassName.put("SmartsSubstructureInclusionScore", SmartsSubstructureInclusionScore.class.getName());
		scoreNameToClassName.put("SmartsSubstructureExclusionScore", SmartsSubstructureExclusionScore.class.getName());
		scoreNameToClassName.put("SuspectListScore", SuspectListScore.class.getName());
		scoreNameToClassName.put("RetentionTimeScore", RetentionTimeScore.class.getName());
		scoreNameToClassName.put("CombinedReferenceScore", CombinedReferenceScore.class.getName());
		scoreNameToClassName.put("SimScore", MatchSpectrumCosineSimilarityScore.class.getName());
		
		scoreNameToClassName.put("IndividualMoNAScore", IndividualMoNASpectralSimilarity.class.getName());
		scoreNameToClassName.put("OfflineMetFusionScore", OfflineMetFusionSpectralSimilarityScore.class.getName());
		
		/*
		 * each score needs its global init class to set shared objects once
		 */
		scoreNameToScoreInitialiserClassName = new java.util.HashMap<String, String>();
		scoreNameToScoreInitialiserClassName.put(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME, DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterHierarchicalScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterScoreLipids", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("FragmenterUniqueFormulaScore", DefaultScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SmartsSubstructureInclusionScore", SmartsSubstructureInclusionScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SmartsSubstructureExclusionScore", SmartsSubstructureExclusionScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("SuspectListScore", SuspectListScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("RetentionTimeScore", RetentionTimeScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("CombinedReferenceScore", DefaultScoreInitialiser.class.getName());
		
		scoreNameToScoreInitialiserClassName.put("IndividualMoNAScore", IndividualMoNASpectralSimilarityInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("OfflineMetFusionScore", OfflineMetFusionSpectralSimilarityScoreInitialiser.class.getName());
		scoreNameToScoreInitialiserClassName.put("MatchSpectrumCosineSimilarityScore", DefaultScoreInitialiser.class.getName());
		
		/*
		 * new implemented databases have to be added here
		 */
		databaseNameToClassName = new java.util.HashMap<String, String>();
		databaseNameToClassName.put("KEGG", OnlineKeggDatabase.class.getName());
		databaseNameToClassName.put("MetaCyc", OnlineMetaCycDatabase.class.getName());
		databaseNameToClassName.put("PubChem", OnlinePubChemDatabase.class.getName());
		databaseNameToClassName.put("ExtendedPubChem", OnlineExtendedPubChemDatabase.class.getName());
		databaseNameToClassName.put("ChemSpider", OnlineChemSpiderDatabase.class.getName());
		databaseNameToClassName.put("LocalPSV", LocalPSVDatabase.class.getName());
		databaseNameToClassName.put("LocalCSV", LocalCSVDatabase.class.getName());
		databaseNameToClassName.put("LocalProperty", LocalPropertyFileDatabase.class.getName());
		databaseNameToClassName.put("ForIdent", OnlineForIdentDatabase.class.getName());
		databaseNameToClassName.put("LocalSDF", LocalSDFDatabase.class.getName());
		databaseNameToClassName.put("LipidMaps", LocalLipidMapsDatabase.class.getName());
		databaseNameToClassName.put("PostgresSQL", LocalPostgresDatabase.class.getName());
		databaseNameToClassName.put("MySQL", LocalMySQLDatabase.class.getName());
		databaseNameToClassName.put("LocalKegg", LocalKeggDatabase.class.getName());
		databaseNameToClassName.put("LocalDerivatisedKegg", LocalDerivatisedKeggDatabase.class.getName());
		databaseNameToClassName.put("LocalPubChem", LocalPubChemDatabase.class.getName());
		databaseNameToClassName.put("LocalExtendedPubChem", LocalExtendedPubChemDatabase.class.getName());
		databaseNameToClassName.put("LocalInMemoryDatabase", LocalInMemoryDatabase.class.getName());
		
		/*
		 * new implemented candidateListWriter have to be added here
		 */
		candidateListWriterNameToClassName = new java.util.HashMap<String, String>();
		candidateListWriterNameToClassName.put("CSV", CandidateListWriterCSV.class.getName());
		candidateListWriterNameToClassName.put("PSV", CandidateListWriterPSV.class.getName());
		candidateListWriterNameToClassName.put("ExtendedPSV", CandidateListWriterExtendedPSV.class.getName());
		candidateListWriterNameToClassName.put("FragmentSmilesPSV", CandidateListWriterFragmentSmilesPSV.class.getName());
		candidateListWriterNameToClassName.put("SDF", CandidateListWriterSDF.class.getName());
		candidateListWriterNameToClassName.put("XLS", CandidateListWriterXLS.class.getName());
		candidateListWriterNameToClassName.put("ExtendedXLS", CandidateListWriterExtendedXLS.class.getName());
		candidateListWriterNameToClassName.put("ExtendedFragmentsXLS", CandidateListWriterExtendedFragmentsXLS.class.getName());
		/*
		 * new implemented preProcessingCandidateFilter have to be added here
		 */
		preProcessingCandidateFilterNameToClassName = new java.util.HashMap<String, String>();
		preProcessingCandidateFilterNameToClassName.put("UnconnectedCompoundFilter", PreProcessingCandidateUnconnectedStructureFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("IsotopeFilter", PreProcessingCandidateIsotopeFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementExclusionFilter", PreProcessingCandidateElementExclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementInclusionExclusiveFilter", PreProcessingCandidateElementInclusionExclusiveFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("ElementInclusionFilter", PreProcessingCandidateElementInclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SmartsSubstructureExclusionFilter", PreProcessingCandidateSmartsExclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SmartsSubstructureInclusionFilter", PreProcessingCandidateSmartsInclusionFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("SuspectListFilter", PreProcessingCandidateSuspectListFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("MaximumElementsFilter", PreProcessingCandidateMaximalElementFilter.class.getName());
		preProcessingCandidateFilterNameToClassName.put("MinimumElementsFilter", PreProcessingCandidateMinimalElementFilter.class.getName());
		/*
		 * new implemented preProcessingCandidateFilter have to be added here
		 */
		postProcessingCandidateFilterNameToClassName = new java.util.HashMap<String, String>();
		postProcessingCandidateFilterNameToClassName.put("InChIKeyFilter", PostProcessingCandidateInChIKeyFilter.class.getName());
		
		fragmentListWriterNameToClassName = new java.util.HashMap<String, String>();
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
}
