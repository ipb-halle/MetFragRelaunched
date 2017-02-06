package de.ipbhalle.metfraglib.parameter;

public class VariableNames {

	/*
	 * global settings
	 */
	public static final String PEAK_LIST_NAME 									= 	"PeakList";
	public static final String DATABASE_RELATIVE_MASS_DEVIATION_NAME 				= 	"DatabaseSearchRelativeMassDeviation";
	public static final String ABSOLUTE_MASS_DEVIATION_NAME 						= 	"FragmentPeakMatchAbsoluteMassDeviation";
	public static final String RELATIVE_MASS_DEVIATION_NAME 						= 	"FragmentPeakMatchRelativeMassDeviation";
	public static final String MAXIMUM_TREE_DEPTH_NAME 							= 	"MaximumTreeDepth";
	public static final String PRECURSOR_ION_MODE_NAME 							= 	"PrecursorIonMode";
	public static final String PRECURSOR_ION_MODE_STRING_NAME 					= 	"PrecursorIonType";
	public static final String IS_POSITIVE_ION_MODE_NAME							= 	"IsPositiveIonMode";
	public static final String PRECURSOR_NEUTRAL_MASS_NAME 						= 	"NeutralPrecursorMass";
	public static final String PRECURSOR_ION_MASS_NAME 							= 	"IonizedPrecursorMass";
	public static final String CONSIDER_HYDROGEN_SHIFTS_NAME 						= 	"ConsiderHydrogenShifts";
	public static final String PRECURSOR_MOLECULAR_FORMULA_NAME 					= 	"NeutralPrecursorMolecularFormula";
	public static final String PRECURSOR_DATABASE_IDS_NAME 						= 	"PrecursorCompoundIDs";
	public static final String PEAK_LIST_PATH_NAME 								= 	"PeakListPath";
	public static final String LOCAL_DATABASE_PATH_NAME 							= 	"LocalDatabasePath";
	public static final String NUMBER_OF_DIGITS_AFTER_ROUNDING_NAME				= 	"NumberOfDigitsAfterRounding";
	public static final String PARAMETER_FILE_NAME								= 	"ParameterFile";
	public static final String PROCESS_CANDIDATES									=	"ProcessCandidates";
	
	public static final String METFRAG_DATABASE_TYPE_NAME 						= 	"MetFragDatabaseType";
	public static final String METFRAG_FRAGMENTER_TYPE_NAME 						= 	"MetFragFragmenterType";
	public static final String METFRAG_CANDIDATE_TYPE_NAME 						= 	"MetFragCandidateType";
	public static final String METFRAG_ASSIGNER_TYPE_NAME 						=	"MetFragAssignerType";
	public static final String METFRAG_SCORE_TYPES_NAME 							= 	"MetFragScoreTypes";
	public static final String METFRAG_ASSIGNER_SCORER_NAME 						=	"MetFragAssignerScorer";
	public static final String METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME 		= 	"MetFragPreProcessingCandidateFilter";
	public static final String PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME		=	"FilterExcludedElements";	
	public static final String PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME		=	"FilterIncludedElements";	
	public static final String PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME			=	"FilterMaximumElements";	
	public static final String PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME			=	"FilterMinimumElements";	
	public static final String METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME 		= 	"MetFragPostProcessingCandidateFilter";
	
	public static final String METFRAG_FRAGMENTER_SCORE_NAME						=	"FragmenterScore";
	
	public static final String MAXIMUM_CANDIDATE_LIMIT_TO_STOP_NAME 				= 	"MaxCandidateLimitToStop"; 
	
	public static final String METFRAG_PEAK_LIST_READER_NAME 				= 	"MetFragPeakListReader";
	public static final String METFRAG_PEAK_LIST_READER_HD_NAME 			= 	"MetFragPeakListReaderHD";
	public static final String METFRAG_SCORE_WEIGHTS_NAME 					= 	"MetFragScoreWeights";
	public static final String METFRAG_CANDIDATE_WRITER_NAME				= 	"MetFragCandidateWriter";
	public static final String METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME		= 	"MetFragCandidateFragmentWriter";
	public static final String METFRAG_CANDIDATE_IMAGE_GENERATOR_NAME		=	"MetFragCandidateImageGenerator";
	public static final String METFRAG_FRAGMENT_IMAGE_GENERATOR_NAME		=	"MetFragFragmentImageGenerator";
	public static final String METFRAG_IMAGE_WRITER_NAME					=	"MetFragImageWriter";
	//minimum intensity filter for FilteredTandemMassPeakListReader
	public static final String MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME			=	"MinimumAbsolutePeakIntensity";
	public static final String MOLECULES_IN_MEMORY							=	"MoleculeInMemory";
	public static final String COMBINED_REFERENCE_SCORE_VALUES				=	"CombinedReferenceScoreValues";
	
	public static final String LOG_LEVEL_NAME 								= 	"LogLevel";
	public static final String SAMPLE_NAME 									= 	"SampleName";
	public static final String STORE_RESULTS_PATH_NAME 						= 	"ResultsPath";
	public static final String STORE_RESULTS_FILE_NAME 						= 	"ResultsFile";
	public static final String MINIMUM_FRAGMENT_MASS_LIMIT_NAME 				= 	"MinimumFragmentMassLimit";
	public static final String NUMBER_THREADS_NAME 							= 	"NumberThreads";
	public static final String MAXIMUM_NUMBER_OF_TOPDOWN_FRAGMENT_ADDED_TO_QUEUE = "MaximumNumberOfAFragmentAddedToQueue";
	public static final String BOND_ENERGY_FILE_PATH_NAME 					= 	"BondEnergyFilePath"; 	
	public static final String EXPERIMENTAL_RETENTION_TIME_VALUE_NAME		= 	"ExperimentalRetentionTimeValue"; 	
	public static final String RETENTION_TIME_SCORE_LINEAR_MODEL_NAME		= 	"RetentionTimeScoreLinearModel"; 	

	public static final String SMARTS_SUBSTRUCTURE_EXCLUSION_SCORE_LIST_NAME = "SmartsSubstructureExclusionScoreSmartsList";
	public static final String SMARTS_SUBSTRUCTURE_INCLUSION_SCORE_LIST_NAME = "SmartsSubstructureInclusionScoreSmartsList";
	
	public static final String PRE_CANDIDATE_FILTER_SMARTS_LIST_NAME 			= "SmartsSubstructureFilterSmartsList";
	public static final String PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME		= "SmartsSubstructureFilterSmartsFormula";
	public static final String PRE_CANDIDATE_FILTER_SMARTS_STRING_NAME 		= "SmartsSubstructureFilterSmartsString";
	
	public static final String SUSPECTLIST_SCORE_LIST_NAME 		= "SuspectListScoreList";
	
	public static final String CHEMSPIDER_TOKEN_NAME					= 	"ChemSpiderToken";
	
	/*
	 * file containing values for model prediction
	 */
	public static final String RETENTION_TIME_TRAINING_FILE_NAME 		= 	"RetentionTimeTrainingFile";
	
	/*
	 * substructure training
	 */
	public static final String SMARTS_PEAK_ANNOTATION_FILE_NAME				= "SmartsPeakAnnotationFile";
	public static final String SMARTS_LOSS_ANNOTATION_FILE_NAME				= "SmartsLossAnnotationFile";
	public static final String PEAK_TO_SMARTS_GROUP_LIST_COLLECTION_NAME		= "peakToSmartGroupListCollection";
	public static final String LOSS_TO_SMARTS_GROUP_LIST_COLLECTION_NAME		= "lossToSmartGroupListCollection";
	
	/*
	 * single process thread settings
	 */
	public static final String MATCH_LIST_NAME 				= 	"matchList";
	public static final String CANDIDATE_NAME				=	"candidate";			
	public static final String BOND_ENERGY_OBJECT_NAME		=	"BondEnergyObject";
	
	/*
	 * processing values
	 */
	public static final String PROCESS_STATUS_OBJECT_NAME		=	"ProcessStatusObject";
	
	/*
	 * parameters for local structure database connection
	 */
	public static final String LOCAL_DATABASE_NAME						= 	"LocalDatabase";
	public static final String LOCAL_DATABASE_COMPOUND_TABLE_NAME		= 	"LocalDatabaseCompoundsTable";
	public static final String LOCAL_DATABASE_PORT_NUMBER_NAME			= 	"LocalDatabasePortNumber";
	public static final String LOCAL_DATABASE_SERVER_IP_NAME			= 	"LocalDatabaseServerIp";
	public static final String LOCAL_DATABASE_MASS_COLUMN_NAME			= 	"LocalDatabaseMassColumn";
	public static final String LOCAL_DATABASE_FORMULA_COLUMN_NAME		= 	"LocalDatabaseFormulaColumn";
	public static final String LOCAL_DATABASE_INCHI_COLUMN_NAME			= 	"LocalDatabaseInChIColumn";
	public static final String LOCAL_DATABASE_INCHIKEY1_COLUMN_NAME		= 	"LocalDatabaseInChIKey1Column";
	public static final String LOCAL_DATABASE_INCHIKEY2_COLUMN_NAME		= 	"LocalDatabaseInChIKey2Column";
	public static final String LOCAL_DATABASE_CID_COLUMN_NAME			= 	"LocalDatabaseCidColumn";
	public static final String LOCAL_DATABASE_SMILES_COLUMN_NAME		= 	"LocalDatabaseSmilesColumn";
	public static final String LOCAL_DATABASE_USER_NAME					= 	"LocalDatabaseUser";
	public static final String LOCAL_DATABASE_PASSWORD_NAME						= 	"LocalDatabasePassword";
	public static final String LOCAL_DATABASE_COMPOUND_NAME_COLUMN_NAME			= 	"LocalDatabaseCompoundNameColumn";
	

	public static final String LOCAL_PUBCHEM_DATABASE_NAME						= 	"LocalPubChemDatabase";
	public static final String LOCAL_PUBCHEM_DATABASE_COMPOUND_TABLE_NAME		= 	"LocalPubChemDatabaseCompoundsTable";
	public static final String LOCAL_PUBCHEM_DATABASE_PORT_NUMBER_NAME			= 	"LocalPubChemDatabasePortNumber";
	public static final String LOCAL_PUBCHEM_DATABASE_SERVER_IP_NAME			= 	"LocalPubChemDatabaseServerIp";
	public static final String LOCAL_PUBCHEM_DATABASE_MASS_COLUMN_NAME			= 	"LocalPubChemDatabaseMassColumn";
	public static final String LOCAL_PUBCHEM_DATABASE_FORMULA_COLUMN_NAME		= 	"LocalPubChemDatabaseFormulaColumn";
	public static final String LOCAL_PUBCHEM_DATABASE_INCHI_COLUMN_NAME			= 	"LocalPubChemDatabaseInChIColumn";
	public static final String LOCAL_PUBCHEM_DATABASE_INCHIKEY1_COLUMN_NAME		= 	"LocalPubChemDatabaseInChIKey1Column";
	public static final String LOCAL_PUBCHEM_DATABASE_INCHIKEY2_COLUMN_NAME		= 	"LocalPubChemDatabaseInChIKey2Column";
	public static final String LOCAL_PUBCHEM_DATABASE_CID_COLUMN_NAME			= 	"LocalPubChemDatabaseCidColumn";
	public static final String LOCAL_PUBCHEM_DATABASE_SMILES_COLUMN_NAME		= 	"LocalPubChemDatabaseSmilesColumn";
	public static final String LOCAL_PUBCHEM_DATABASE_USER_NAME					= 	"LocalPubChemDatabaseUser";
	public static final String LOCAL_PUBCHEM_DATABASE_PASSWORD_NAME				= 	"LocalPubChemDatabasePassword";
	public static final String LOCAL_PUBCHEM_DATABASE_COMPOUND_NAME_COLUMN_NAME		= 	"LocalPubChemDatabaseCompoundNameColumn";

	public static final String LOCAL_METCHEM_DATABASE_NAME						= 	"LocalMetChemDatabase";
	public static final String LOCAL_METCHEM_DATABASE_PORT_NUMBER_NAME			= 	"LocalMetChemDatabasePortNumber";
	public static final String LOCAL_METCHEM_DATABASE_SERVER_IP_NAME				= 	"LocalMetChemDatabaseServerIp";
	public static final String LOCAL_METCHEM_DATABASE_USER_NAME					= 	"LocalMetChemDatabaseUser";
	public static final String LOCAL_METCHEM_DATABASE_PASSWORD_NAME				= 	"LocalMetChemDatabasePassword";
	public static final String LOCAL_METCHEM_DATABASE_LIBRARY_NAME				= 	"LocalMetChemDatabaseLibrary";
	
	public static final String LOCAL_KEGG_DATABASE_NAME					= 	"LocalKeggDatabase";
	public static final String LOCAL_KEGG_DATABASE_COMPOUND_TABLE_NAME	= 	"LocalKeggDatabaseCompoundsTable";
	public static final String LOCAL_KEGG_DATABASE_PORT_NUMBER_NAME		= 	"LocalKeggDatabasePortNumber";
	public static final String LOCAL_KEGG_DATABASE_SERVER_IP_NAME			= 	"LocalKeggDatabaseServerIp";
	public static final String LOCAL_KEGG_DATABASE_MASS_COLUMN_NAME		= 	"LocalKeggDatabaseMassColumn";
	public static final String LOCAL_KEGG_DATABASE_FORMULA_COLUMN_NAME	= 	"LocalKeggDatabaseFormulaColumn";
	public static final String LOCAL_KEGG_DATABASE_INCHI_COLUMN_NAME		= 	"LocalKeggDatabaseInChIColumn";
	public static final String LOCAL_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME	= 	"LocalKeggDatabaseInChIKey1Column";
	public static final String LOCAL_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME	= 	"LocalKeggDatabaseInChIKey2Column";
	public static final String LOCAL_KEGG_DATABASE_CID_COLUMN_NAME		= 	"LocalKeggDatabaseCidColumn";
	public static final String LOCAL_KEGG_DATABASE_SMILES_COLUMN_NAME		= 	"LocalKeggDatabaseSmilesColumn";
	public static final String LOCAL_KEGG_DATABASE_USER_NAME				= 	"LocalKeggDatabaseUser";
	public static final String LOCAL_KEGG_DATABASE_PASSWORD_NAME			= 	"LocalKeggDatabasePassword";
	public static final String LOCAL_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME		= 	"LocalKeggDatabaseCompoundNameColumn";
	
	public static final String LOCAL_LIPIDMAPS_DATABASE_NAME							= 	"LocalLipidMapsDatabase";
	public static final String LOCAL_LIPIDMAPS_DATABASE_COMPOUND_TABLE_NAME			= 	"LocalLipidMapsDatabaseCompoundsTable";
	public static final String LOCAL_LIPIDMAPS_DATABASE_PORT_NUMBER_NAME				= 	"LocalLipidMapsDatabasePortNumber";
	public static final String LOCAL_LIPIDMAPS_DATABASE_SERVER_IP_NAME				= 	"LocalLipidMapsDatabaseServerIp";
	public static final String LOCAL_LIPIDMAPS_DATABASE_MASS_COLUMN_NAME				= 	"LocalLipidMapsDatabaseMassColumn";
	public static final String LOCAL_LIPIDMAPS_DATABASE_FORMULA_COLUMN_NAME			= 	"LocalLipidMapsDatabaseFormulaColumn";
	public static final String LOCAL_LIPIDMAPS_DATABASE_INCHI_COLUMN_NAME				= 	"LocalLipidMapsDatabaseInChIColumn";
	public static final String LOCAL_LIPIDMAPS_DATABASE_INCHIKEY1_COLUMN_NAME			= 	"LocalLipidMapsDatabaseInChIKey1Column";
	public static final String LOCAL_LIPIDMAPS_DATABASE_INCHIKEY2_COLUMN_NAME			= 	"LocalLipidMapsDatabaseInChIKey2Column";
	public static final String LOCAL_LIPIDMAPS_DATABASE_CID_COLUMN_NAME				= 	"LocalLipidMapsDatabaseCidColumn";
	public static final String LOCAL_LIPIDMAPS_DATABASE_SMILES_COLUMN_NAME			= 	"LocalLipidMapsDatabaseSmilesColumn";
	public static final String LOCAL_LIPIDMAPS_DATABASE_USER_NAME						= 	"LocalLipidMapsDatabaseUser";
	public static final String LOCAL_LIPIDMAPS_DATABASE_PASSWORD_NAME					= 	"LocalLipidMapsDatabasePassword";
	public static final String LOCAL_LIPIDMAPS_DATABASE_COMPOUND_NAME_COLUMN_NAME		= 	"LocalLipidMapsDatabaseCompoundNameColumn";
	
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_NAME							= 	"LocalDerivatisedKeggDatabase";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_TABLE_NAME			= 	"LocalDerivatisedKeggDatabaseCompoundsTable";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_PORT_NUMBER_NAME				= 	"LocalDerivatisedKeggDatabasePortNumber";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_SERVER_IP_NAME					= 	"LocalDerivatisedKeggDatabaseServerIp";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_MASS_COLUMN_NAME				= 	"LocalDerivatisedKeggDatabaseMassColumn";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_FORMULA_COLUMN_NAME			= 	"LocalDerivatisedKeggDatabaseFormulaColumn";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_INCHI_COLUMN_NAME				= 	"LocalDerivatisedKeggDatabaseInChIColumn";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME			= 	"LocalDerivatisedKeggDatabaseInChIKey1Column";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME			= 	"LocalDerivatisedKeggDatabaseInChIKey2Column";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_CID_COLUMN_NAME				= 	"LocalDerivatisedKeggDatabaseCidColumn";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_SMILES_COLUMN_NAME				= 	"LocalDerivatisedKeggDatabaseSmilesColumn";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_USER_NAME						= 	"LocalDerivatisedKeggDatabaseUser";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_PASSWORD_NAME					= 	"LocalDerivatisedKeggDatabasePassword";
	public static final String LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME		= 	"LocalDerivatisedKeggDatabaseCompoundNameColumn";
	

	public static final String LOCAL_CHEBI_DATABASE_NAME						= 	"LocalChEBIDatabase";
	public static final String LOCAL_CHEBI_DATABASE_COMPOUND_TABLE_NAME		= 	"LocalChEBIDatabaseCompoundsTable";
	public static final String LOCAL_CHEBI_DATABASE_PORT_NUMBER_NAME			= 	"LocalChEBIDatabasePortNumber";
	public static final String LOCAL_CHEBI_DATABASE_SERVER_IP_NAME			= 	"LocalChEBIDatabaseServerIp";
	public static final String LOCAL_CHEBI_DATABASE_MASS_COLUMN_NAME			= 	"LocalChEBIDatabaseMassColumn";
	public static final String LOCAL_CHEBI_DATABASE_FORMULA_COLUMN_NAME		= 	"LocalChEBIDatabaseFormulaColumn";
	public static final String LOCAL_CHEBI_DATABASE_INCHI_COLUMN_NAME			= 	"LocalChEBIDatabaseInChIColumn";
	public static final String LOCAL_CHEBI_DATABASE_INCHIKEY1_COLUMN_NAME		= 	"LocalChEBIDatabaseInChIKey1Column";
	public static final String LOCAL_CHEBI_DATABASE_INCHIKEY2_COLUMN_NAME		= 	"LocalChEBIDatabaseInChIKey2Column";
	public static final String LOCAL_CHEBI_DATABASE_CID_COLUMN_NAME			= 	"LocalChEBIDatabaseCidColumn";
	public static final String LOCAL_CHEBI_DATABASE_SMILES_COLUMN_NAME		= 	"LocalChEBIDatabaseSmilesColumn";
	public static final String LOCAL_CHEBI_DATABASE_USER_NAME					= 	"LocalChEBIDatabaseUser";
	public static final String LOCAL_CHEBI_DATABASE_PASSWORD_NAME				= 	"LocalChEBIDatabasePassword";
	public static final String LOCAL_CHEBI_DATABASE_COMPOUND_NAME_COLUMN_NAME	= 	"LocalChEBIDatabaseCompoundNameColumn";

	
	/*
	 * for MoNA
	 */
	public static final String NUMBER_RANDOM_SPECTRA_NAME					=	"NumberRandomSpectra";
	public static final String MASSBANK_URL								=	"MassbankURL";
	public static final String MASSBANK_RECORD_CACHE_DIRECTORY			=	"MassbankRecordCacheDirectory";
	public static final String MONA_PRESENT_INCHIKEYS_FILE_NAME 			= 	"MoNAPresentInChIKeysFile";
	public static final String OFFLINE_SPECTRAL_DATABASE_FILE_NAME 		= 	"OfflineSpectralDatabaseFile";
	
	public static final String EXACT_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME 			= "ExactMoNASpectralSimilarityCandidates";
	public static final String INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_INCHIKEY_LIST_NAME 	= "IndividualMoNASpectralSimilarityInChIKeyList";
	public static final String INDIVIDUAL_MONA_SPECTRAL_SIMILARITY_WEB_SERVICE_NAME 		= "IndividualMoNASpectralSimilarityWebService";
	public static final String METFUSION_MONA_SPECTRAL_SIMILARITY_CANDIDATES_NAME 			= "MetFusionMoNASpectralSimilarityCandidates";
	public static final String OFFLINE_METFUSION_MONA_SPECTRAL_SIMILARITY_PEAK_LIST_COLLECTION_NAME = "OfflineMetFusionMonaSpectralSimilarityPeakListCollection";
	public static final String MINIMUM_COSINE_SIMILARITY_LIMIT_NAME		= "MinimumCosineSimilarityLimit";
	public static final String MOLECULAR_FINGERPRINT_NAME					= "MolecularFingerPrint";
	
	/*
	 * candidate property names
	 */
	public static final String INCHI_KEY_1_NAME				=	"InChIKey1";
	public static final String INCHI_KEY_2_NAME				=	"InChIKey2";
	public static final String INCHI_KEY_NAME					=	"InChIKey";
	public static final String MOLECULAR_FORMULA_NAME			=	"MolecularFormula";
	public static final String MONOISOTOPIC_MASS_NAME			=	"MonoisotopicMass";
	public static final String COMPOUND_NAME_NAME				=	"CompoundName";
	public static final String IUPAC_NAME_NAME				=	"IUPACName";
	public static final String INCHI_NAME						=	"InChI";
	public static final String IDENTIFIER_NAME				=	"Identifier";
	public static final String SMILES_NAME					=	"SMILES";
	public static final String FINGERPRINT_NAME_NAME				=	"FingerPrint";
	public static final String DEUTERIUM_COUNT_NAME				=	"DeuteriumCount";
	public static final String VARIABLE_DEUTERIUM_COUNT_NAME		=	"AromaticDeuteriums";
	public static final String ENABLE_DEUTERIUM_NAME				=	"EnableDeuterium";
	public static final String RETENTION_TIME_NAME				=	"RetentionTime";
	public static final String USER_LOG_P_VALUE_NAME				=	"ExternalPartitioningCoefficientColumnName";
	//forident
	public static final String FORIDENT_TONNAGE_NAME					=	"ForIdentTonnage";
	public static final String FORIDENT_CATEGORIES_NAME				=	"ForIdentCategories";
	public static final String FORIDENT_SUSPECTLIST_NAME				=	"FOR-IDENT";
	public static final String DSSTOX_SUSPECTLIST_NAME				=	"DSSTOX";
	public static final String DSSTOX_SUSPECTLIST_FILE_NAME			=	"dsstox_suspect_list_filtered.txt";
	
	public static final String PUBCHEM_NUMBER_PATENTS_NAME 			=	"PubChemNumberPatents";
	public static final String PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME	=	"PubChemNumberPubMedReferences";
	public static final String PUBCHEM_XLOGP_NAME	 					=	"XlogP3";
	
	public static final String CHEMSPIDER_NUMBER_PUBMED_REFERENCES_NAME 		=	"ChemSpiderNumberPubMedReferences";
	public static final String CHEMSPIDER_NUMBER_EXTERNAL_REFERENCES_NAME 		=	"ChemSpiderNumberExternalReferences";
	public static final String CHEMSPIDER_DATA_SOURCE_COUNT 				=	"ChemSpiderDataSourceCount";
	public static final String CHEMSPIDER_REFERENCE_COUNT 				=	"ChemSpiderReferenceCount";
	public static final String CHEMSPIDER_RSC_COUNT						=	"ChemSpiderRSCCount";
	public static final String CHEMSPIDER_XLOGP_NAME						=	"CHEMSPIDER_XLOGP";
	public static final String CHEMSPIDER_ALOGP_NAME						=	"CHEMSPIDER_ALOGP";
	
	public static final String FINAL_SCORE_COLUMN_NAME					=	"Score";
	public static final String NUMBER_EXPLAINED_PEAKS_COLUMN				=	"NoExplPeaks";
	public static final String EXPLAINED_PEAKS_COLUMN						=	"ExplPeaks";
	public static final String FORMULAS_OF_PEAKS_EXPLAINED_COLUMN			=	"FormulasOfExplPeaks";
	public static final String NUMBER_PEAKS_USED_COLUMN					=	"NumberPeaksUsed";
	public static final String PEAK_LIST_STRING_NAME 						= 	"PeakListString";
	
	public static final String PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME	=	"FilterSmartsInclusionList";
	public static final String PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME	=	"FilterSmartsExclusionList";
	public static final String PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME				=	"FilterSuspectLists";
	

	public static final String SCORE_SMARTS_INCLUSION_LIST_NAME			=	"ScoreSmartsInclusionList";
	public static final String SCORE_SMARTS_EXCLUSION_LIST_NAME			=	"ScoreSmartsExclusionList";
	public static final String SCORE_SUSPECT_LISTS_NAME					=	"ScoreSuspectLists";
	public static final String METFRAG_UNIQUE_FRAGMENT_MATCHES 			= 	"MetFragUniqueFragmentMatches";
	public static final String SCORE_NAMES_NOT_TO_SCALE		 			= 	"ScoreNamesNotToScale";
	
	/*
	 * email feedback settings
	 */
	public static final String FEEDBACK_EMAIL_HOST			 			= 	"FeedbackEmailHost";
	public static final String FEEDBACK_EMAIL_TO			 				= 	"FeedbackEmailTo";
	public static final String FEEDBACK_EMAIL_PORT			 			= 	"FeedbackEmailPort";
	public static final String FEEDBACK_EMAIL_PASS			 			= 	"FeedbackEmailPass";
	public static final String FEEDBACK_EMAIL_USER			 			= 	"FeedbackEmailUser";

	/*
	 * hd settings
	 */
	public static final String HD_PEAK_LIST_PATH_NAME 						= 	"HDPeakListPath";
	public static final String HD_PRECURSOR_NEUTRAL_MASS_NAME 				= 	"HDNeutralPrecursorMass";
	public static final String HD_PRECURSOR_ION_MASS_NAME 					= 	"HDIonizedPrecursorMass";
	public static final String HD_PRECURSOR_MOLECULAR_FORMULA_NAME 			= 	"HDNeutralPrecursorMolecularFormula";
	public static final String HD_PRECURSOR_ION_MODE_NAME 					= 	"HDPrecursorIonMode";
	public static final String HD_NUMBER_EXCHANGED_HYDROGENS					=	"HDNumberExchangedHydrogens";
	public static final String HD_PEAK_LIST_NAME 								= 	"HDPeakList";
	public static final String HD_GROUP_FLAG_NAME 							= 	"HDGroupFlag";
	public static final String HD_NUMBER_PEAKS_USED_COLUMN					=	"HDNumberPeaksUsed";
	public static final String HD_MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME		=	"HDMinimumAbsolutePeakIntensity";
	
	public static final String PEAK_INDEX_TO_PEAK_MATCH_NAME					=	"PeakIndexToPeakMatch";
	public static final String HD_PEAK_INDEX_TO_PEAK_MATCH_NAME				=	"HDPeakIndexToPeakMatch";
	public static final String HD_NUMBER_EXPLAINED_PEAKS_COLUMN				=	"HDNoExplPeaks";
	public static final String HD_GROUP_FLAG_FILTER_ENABLED_NAME				=	"HDGroupFlagFilterEnabled";
	
	
	/*
	 * proxy settings
	 */
	public static final String MONA_PROXY_SERVER				 			= 	"MoNAProxyServer";
	public static final String MONA_PROXY_PORT				 			= 	"MoNAProxyPort";
	public static final String KEGG_PROXY_SERVER				 			= 	"KeggProxyServer";
	public static final String KEGG_PROXY_PORT				 			= 	"KeggProxyPort";
	public static final String METACYC_PROXY_SERVER				 		= 	"MetaCycProxyServer";
	public static final String METACYC_PROXY_PORT				 			= 	"MetaCycProxyPort";
	public static final String PUBCHEM_PROXY_SERVER				 		= 	"PubChemProxyServer";
	public static final String PUBCHEM_PROXY_PORT				 			= 	"PubChemProxyPort";
	public static final String CHEMSPIDER_PROXY_SERVER				 		= 	"ChemSpiderProxyServer";
	public static final String CHEMSPIDER_PROXY_PORT				 		= 	"ChemSpiderProxyPort";

}
