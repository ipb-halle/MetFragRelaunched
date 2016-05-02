package de.ipbhalle.metfraglib.parameter;

import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;

public class SettingsChecker {

	protected Logger logger = Logger.getLogger(this.getClass());
	
	public boolean check(Settings settings) {
		
		if(!checkPeakListFileSettings(settings)) return false;
		if(!checkDatabaseSettings(settings)) return false;
		if(!checkFragmenterSettings(settings)) return false;
		if(!candidateFilterSettings(settings)) return false;
		if(!scoringTypesSettings(settings)) return false;
		if(!checkOutputSettings(settings)) return false;
		
		return true;
	}

	public boolean check(Settings settings, boolean checkOutput) {
		
		if(!checkPeakListFileSettings(settings)) return false;
		if(!checkDatabaseSettings(settings)) return false;
		if(!checkFragmenterSettings(settings)) return false;
		if(!candidateFilterSettings(settings)) return false;
		if(!scoringTypesSettings(settings)) return false;
		if(checkOutput && !checkOutputSettings(settings)) return false;
		
		return true;
	}
	
	/**
	 * 
	 * @param settings
	 * @return
	 */
	private boolean checkPeakListFileSettings(Settings settings) {
		
		boolean checkPositive = true;
		
		/**
		 * check peak list file
		 */
		if(settings.get(VariableNames.PEAK_LIST_STRING_NAME) != null 
				&& ((String)settings.get(VariableNames.METFRAG_PEAK_LIST_READER_NAME)).equals(FilteredStringTandemMassPeakListReader.class.getName())) {
			//nothing to do here
		}
		else if(settings.get(VariableNames.PEAK_LIST_PATH_NAME) != null) {
			String peakListName = (String)settings.get(VariableNames.PEAK_LIST_PATH_NAME);
			checkPositive = checkFile(VariableNames.PEAK_LIST_PATH_NAME, peakListName);
		}
		else {
			this.logger.error(VariableNames.PEAK_LIST_PATH_NAME + " is not defined!");
			checkPositive = false;
		}
		
		return checkPositive;
	}
	
	private boolean checkOutputSettings(Settings settings) {
		
		boolean checkPositive = true;
		
		Object SampleName = settings.get(VariableNames.SAMPLE_NAME);
		Object ResultsPath = settings.get(VariableNames.STORE_RESULTS_PATH_NAME);
		Object MetFragCandidateWriter = settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME);
		
		/**
		 * check peak list file
		 */
		
		if(SampleName == null) {
			this.logger.error(VariableNames.SAMPLE_NAME + " is not defined!");
			checkPositive = false;
		}
		if(ResultsPath == null) {
			this.logger.error(VariableNames.STORE_RESULTS_PATH_NAME + " is not defined!");
			checkPositive = false;
		}
		else if(checkPositive) {
			checkPositive = this.checkDirectory(VariableNames.STORE_RESULTS_PATH_NAME, (String)ResultsPath);
		}
		if(MetFragCandidateWriter == null) {
			this.logger.error(VariableNames.METFRAG_CANDIDATE_WRITER_NAME + " is not defined!");
			checkPositive = false;
		}
		else {
			String[] names = (String[])MetFragCandidateWriter;
			for(int k = 0; k < names.length; k++) {
				if(ClassNames.getClassNameOfCandidateListWriter(names[k]) == null) {
					this.logger.error(VariableNames.METFRAG_CANDIDATE_WRITER_NAME + " " + names[k] + " is not known!");
					checkPositive = false;
				}
			}
		}
		return checkPositive;
	}
	
	/**
	 * 
	 * @param settings
	 * @return
	 */
	private boolean checkDatabaseSettings(Settings settings) {

		boolean checkPositive = true;
		
		java.util.Vector<String> needsLocalDatabaseFile = new java.util.Vector<String>();
		needsLocalDatabaseFile.add("LocalCSV");
		needsLocalDatabaseFile.add("LocalPSV");
		needsLocalDatabaseFile.add("LocalProperty");
		needsLocalDatabaseFile.add("LocalSDF");
		
		Object DatabaseTypeName = settings.get(VariableNames.METFRAG_DATABASE_TYPE_NAME);
		Object PrecursorCompoundIDs = settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME);
		Object NeutralPrecursorMass = settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		Object NeutralPrecursorMolecularFormula = settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME);
		Object DatabaseSearchRelativeMassDeviation = settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME);
		Object LocalDatabasePath = settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME);
		Object ChemSpiderToken = settings.get(VariableNames.CHEMSPIDER_TOKEN_NAME);
		
		Object MaxCandidateLimitToStop = settings.get(VariableNames.MAXIMUM_CANDIDATE_LIMIT_TO_STOP_NAME);
	
		if(NeutralPrecursorMass == null) {
			if(NeutralPrecursorMolecularFormula != null) {
				try {
					ByteMolecularFormula formula = new ByteMolecularFormula((String)NeutralPrecursorMolecularFormula);
					settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, formula.getMonoisotopicMass());
				}
				catch(Exception e) {
					this.logger.error("Error: Invalid value for " +  VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME + ".");
					checkPositive = false;
				}
			}
			else {
					try {
					Double ionMass = (Double)settings.get(VariableNames.PRECURSOR_ION_MASS_NAME);
					Integer ionMode = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
					Boolean isPositive = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
					double value = ionMass - Constants.ADDUCT_MASSES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(ionMode)) 
							- Constants.POSITIVE_IONISATION_MASS_DIFFERENCE.get(Constants.POSITIVE_IONISATION.indexOf(isPositive));
					settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, value);
				}
				catch(Exception e) {
					this.logger.error("Error: Precursor mass information not sufficient. Define NeutralPrecursorMolecularFormula, NeutralPrecursorMass or IonizedPrecursorMass!");
					checkPositive = false;
				}
			}
		}
		
		/*
		 * check database type
		 */
		if(DatabaseTypeName != null) {
			String databaseName = (String)DatabaseTypeName;
			if(ClassNames.getClassNameOfDatabase(databaseName) == null) {
				this.logger.error(VariableNames.METFRAG_DATABASE_TYPE_NAME + " " + databaseName + " is not known!");
				checkPositive = false;
			}
			if(databaseName.equals("ChemSpider") && ChemSpiderToken == null) {
				this.logger.error(VariableNames.CHEMSPIDER_TOKEN_NAME + " is not defined!");
				checkPositive = false;
			}
			boolean isLocalDatabase = false;
			if(needsLocalDatabaseFile.contains(databaseName)) 
				isLocalDatabase = true;
			
			if(!isLocalDatabase && (PrecursorCompoundIDs == null 
					&& NeutralPrecursorMolecularFormula == null
					&& DatabaseSearchRelativeMassDeviation == null)) 
			{
				this.logger.error("Define at least one parameter: "
						+ VariableNames.PRECURSOR_DATABASE_IDS_NAME + ", "
						+ VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME + ", "
						+ VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME);
				checkPositive = false;
			}
			
			if(isLocalDatabase) {
				if(LocalDatabasePath == null) {
					this.logger.error(VariableNames.LOCAL_DATABASE_PATH_NAME + " is not defined!");
					checkPositive = false;
				}
				else {
					String localDatabasePathName = (String)LocalDatabasePath;
					if(checkPositive) checkPositive = checkFile(VariableNames.LOCAL_DATABASE_PATH_NAME, localDatabasePathName);
				}
			}
		}
		else {
			this.logger.error(VariableNames.METFRAG_DATABASE_TYPE_NAME + " is not defined!");
			checkPositive = false;
		}

		if(MaxCandidateLimitToStop != null) {
			try {
				if((Integer)MaxCandidateLimitToStop < 1) {
					throw new Exception();
				}
			}
			catch(Exception e) {
				this.logger.error("Error: Invalid value for " +  VariableNames.MAXIMUM_CANDIDATE_LIMIT_TO_STOP_NAME + " (" + MaxCandidateLimitToStop + "). Define a positive value!");
				checkPositive = false;
			}
		}
		
		return checkPositive;
	}
	
	/**
	 * 
	 * @param settings
	 * @return
	 */
	private boolean checkFragmenterSettings(Settings settings) {

		boolean checkPositive = true;
		
		Object PrecursorIonMode = settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		Object IsPositiveIonMode = settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		Object FragmentPeakMatchAbsoluteMassDeviation = settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		Object FragmentPeakMatchRelativeMassDeviation = settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		
		if(PrecursorIonMode == null) {
			this.logger.error(VariableNames.PRECURSOR_ION_MODE_NAME + " is not defined!");
			checkPositive = false;
		}
		else if(IsPositiveIonMode == null) {
			this.logger.error(VariableNames.IS_POSITIVE_ION_MODE_NAME + " is not defined!");
			checkPositive = false;
		}
		else {
			int PrecursorIonModeValue = 0;
			try {
				PrecursorIonModeValue = (Integer)PrecursorIonMode;
			}
			catch(Exception e) {
				this.logger.error("No valid value for " + VariableNames.PRECURSOR_ION_MODE_NAME + ": " + PrecursorIonModeValue + "!");
				checkPositive = false;
			}
			Boolean IsPositiveIonModeValue = (Boolean)IsPositiveIonMode;
			if(!Constants.ADDUCT_NOMINAL_MASSES.contains(PrecursorIonModeValue)) {
				this.logger.error(VariableNames.PRECURSOR_ION_MODE_NAME + " " + PrecursorIonModeValue + " is not known!");
				checkPositive = false;
			}
			else if(PrecursorIonModeValue != 0) {
				int precursorTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(PrecursorIonModeValue);
				if((Constants.ADDUCT_CHARGES.get(precursorTypeIndex) && !IsPositiveIonModeValue) || (!Constants.ADDUCT_CHARGES.get(precursorTypeIndex) && IsPositiveIonModeValue)) {
					this.logger.error("Values mismatch: " + VariableNames.PRECURSOR_ION_MODE_NAME + " = " + Constants.ADDUCT_NAMES.get(precursorTypeIndex) + 
							" " + VariableNames.IS_POSITIVE_ION_MODE_NAME + " = " + IsPositiveIonMode);
					checkPositive = false;
				}
			}
 		}
		if(FragmentPeakMatchAbsoluteMassDeviation == null) {
			this.logger.error(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME + " is not defined!");
			checkPositive = false;
		}
		if(FragmentPeakMatchRelativeMassDeviation == null) {
			this.logger.error(VariableNames.RELATIVE_MASS_DEVIATION_NAME + " is not defined!");
			checkPositive = false;
		}
		
		return checkPositive;
	}
	
	/**
	 * 
	 * @param settings
	 * @return
	 */
	private boolean candidateFilterSettings(Settings settings) {
		
		boolean checkPositive = true;

		Object MetFragPreProcessingCandidateFilter = settings.get(VariableNames.METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME);
		Object MetFragPostProcessingCandidateFilter = settings.get(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME);
		Object FilterExcludedElements = settings.get(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME);
		Object FilterIncludedElements = settings.get(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME);
		Object FilterSmartsInclusionList = settings.get(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME);
		Object FilterSmartsExclusionList = settings.get(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME);
		Object FilterSuspectLists = settings.get(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME);
		Object FilterMaximumElements = settings.get(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME);
		Object FilterMinimumElements = settings.get(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME);
		
		/**
		 * check preprocessing filers
		 */
		if(MetFragPreProcessingCandidateFilter != null) {
			java.util.Vector<String> definedFilters = new java.util.Vector<String>();
			String[] MetFragPreProcessingCandidateFilterValue = (String[])MetFragPreProcessingCandidateFilter;
			for(int i = 0; i < MetFragPreProcessingCandidateFilterValue.length; i++) {
				if(definedFilters.contains(MetFragPreProcessingCandidateFilterValue[i])) {
					this.logger.error(MetFragPreProcessingCandidateFilterValue[i] + " in " + VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME + " is defined more than once!");
					checkPositive = false;
				}
				else definedFilters.add(MetFragPreProcessingCandidateFilterValue[i]);
				if(MetFragPreProcessingCandidateFilterValue[i].equals("UnconnectedCompoundFilter")) {
					//nothing to check here
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("IsotopeFilter")) {
					//nothing to check here
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("ElementExclusionFilter")) {
					if(FilterExcludedElements == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("ElementInclusionFilter")) {
					if(FilterIncludedElements == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("ElementInclusionExclusiveFilter")) {
					if(FilterIncludedElements == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("SmartsSubstructureExclusionFilter")) {
					if(FilterSmartsExclusionList == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("SmartsSubstructureInclusionFilter")) {
					if(FilterSmartsInclusionList == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("MaximumElementsFilter")) {
					if(FilterMaximumElements == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("MinimumElementsFilter")) {
					if(FilterMinimumElements == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragPreProcessingCandidateFilterValue[i].equals("SuspectListFilter")) {
					if(FilterSuspectLists == null) {
						this.logger.error(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME + " is not defined!");
						checkPositive = false;
					}
					else if(checkPositive) {
						String[] fileNames = (String[])FilterSuspectLists;
						for(int j = 0; j < fileNames.length; j++) {
							if(!fileNames[j].equals(VariableNames.FORIDENT_SUSPECTLIST_NAME) && !this.checkFile(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME, fileNames[j]))
								checkPositive = false;
						}
					}
				}
				else {
					this.logger.error(VariableNames.METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME + " " + MetFragPreProcessingCandidateFilterValue[i] + " is not known!");
					checkPositive = false;
				}
			}
		}
		/**
		 * check postprocessing filers
		 */
		if(MetFragPostProcessingCandidateFilter != null) {
			String[] MetFragPostProcessingCandidateFilterValue = (String[])MetFragPostProcessingCandidateFilter;
			for(int i = 0; i < MetFragPostProcessingCandidateFilterValue.length; i++) {
				if(MetFragPostProcessingCandidateFilterValue[i].equals("InChIKeyFilter")) {
					//nothing to check here
				}
				else {
					this.logger.error(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME + " " + MetFragPostProcessingCandidateFilterValue[i] + " is not known!");
					checkPositive = false;
				}
			}
		}
		
		return checkPositive;
	}
	
	/**
	 * 
	 * @param settings
	 * @return
	 */
	private boolean scoringTypesSettings(Settings settings) {

		boolean checkPositive = true;

		Object MetFragScoreTypes = settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		Object MetFragScoreWeights = settings.get(VariableNames.METFRAG_SCORE_WEIGHTS_NAME);
		Object ScoreSmartsInclusionList = settings.get(VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME);
		Object ScoreSmartsExclusionList = settings.get(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME);
		Object ScoreSuspectLists = settings.get(VariableNames.SCORE_SUSPECT_LISTS_NAME);
		Object RetentionTimeTrainingFile = settings.get(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME);
		Object ExperimentalRetentionTimeValue = settings.get(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME);
		Object CombinedReferenceScoreValues = settings.get(VariableNames.COMBINED_REFERENCE_SCORE_VALUES);
		Object OfflineSpectralDatabaseFile = settings.get(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME);
		
		if(MetFragScoreTypes != null && MetFragScoreWeights != null) {
			String[] MetFragScoreTypesValue = (String[])MetFragScoreTypes;
			Double[] MetFragScoreWeightsValue = (Double[])MetFragScoreWeights;
			if(MetFragScoreTypesValue.length != MetFragScoreWeightsValue.length) {
				this.logger.error("Numbers of " + VariableNames.METFRAG_SCORE_TYPES_NAME + " and " + VariableNames.METFRAG_SCORE_WEIGHTS_NAME + " differ!");
				checkPositive = false;
			}
			java.util.Vector<String> definedScores = new java.util.Vector<String>();
			for(int i = 0; i < MetFragScoreTypesValue.length; i++) {
				if(definedScores.contains(MetFragScoreTypesValue[i])) {
					this.logger.error(MetFragScoreTypesValue[i] + " in " + VariableNames.METFRAG_SCORE_TYPES_NAME + " is defined more than once!");
					checkPositive = false;
				}
				else definedScores.add(MetFragScoreTypesValue[i]);
				if(MetFragScoreTypesValue[i].equals(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME)) {
					//nothing to check here
				}
				else if(MetFragScoreTypesValue[i].equals("SmartsSubstructureInclusionScore")) {
					if(ScoreSmartsInclusionList == null) {
						this.logger.error(VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragScoreTypesValue[i].equals("SmartsSubstructureExclusionScore")) {
					if(ScoreSmartsExclusionList == null) {
						this.logger.error(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragScoreTypesValue[i].equals("CombinedReferenceScore")) {
					if(CombinedReferenceScoreValues == null) {
						this.logger.error(VariableNames.COMBINED_REFERENCE_SCORE_VALUES + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragScoreTypesValue[i].equals("SuspectListsScore")) {
					if(ScoreSuspectLists == null) {
						this.logger.error(VariableNames.SCORE_SUSPECT_LISTS_NAME + " is not defined!");
						checkPositive = false;
					}
					else if(checkPositive) {
						String[] fileNames = (String[])ScoreSuspectLists;
						for(int j = 0; j < fileNames.length; j++) 
							if(!fileNames[j].equals(VariableNames.FORIDENT_SUSPECTLIST_NAME) && !this.checkFile(VariableNames.SCORE_SUSPECT_LISTS_NAME, fileNames[j]))
								checkPositive = false;
					}
				}
				else if(MetFragScoreTypesValue[i].equals("RetentionTimeScore")) {
					if(RetentionTimeTrainingFile == null) {
						this.logger.error(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME + " is not defined!");
						checkPositive = false;
					}
					else {
						String RetentionTimeTrainingFilePathName = (String)RetentionTimeTrainingFile;
						if(checkPositive) checkPositive = checkFile(VariableNames.LOCAL_DATABASE_PATH_NAME, RetentionTimeTrainingFilePathName);
					}
					if(ExperimentalRetentionTimeValue == null) {
						this.logger.error(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME + " is not defined!");
						checkPositive = false;
					}
				}
				else if(MetFragScoreTypesValue[i].equals("ExactMoNAScore")) {
					//nothing to check here
				}
				else if(MetFragScoreTypesValue[i].equals("MetFusionMoNAScore")) {
					//nothing to check here
				}
				else if(MetFragScoreTypesValue[i].equals("IndividualMoNAScore")) {
					//nothing to check here
				}
				else if(MetFragScoreTypesValue[i].equals("SimScore")) {
					//nothing to check here
				}
				else if(MetFragScoreTypesValue[i].equals("OfflineMetFusionScore") && OfflineSpectralDatabaseFile != null) {
					String OfflineSpectralDatabaseFilePathName = (String)OfflineSpectralDatabaseFile;
					if(checkPositive) checkPositive = this.checkFile(VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME, OfflineSpectralDatabaseFilePathName);
				}
				/*
				else {
					this.logger.error(VariableNames.METFRAG_SCORE_TYPES_NAME + " " + MetFragScoreTypesValue[i] + " is not known!");
					checkPositive = false;
				}
				*/
			}
		}	
		else {
			if(MetFragScoreTypes == null) this.logger.error(VariableNames.METFRAG_SCORE_TYPES_NAME + " is not defined!");
			if(MetFragScoreWeights == null) this.logger.error(VariableNames.METFRAG_SCORE_WEIGHTS_NAME + " is not defined!");
			checkPositive = false;
		}
			
		return checkPositive;
		
	}
		
	/**
	 * 
	 */
	private boolean checkDirectory(String parameterName, String fileName) {
		java.io.File file = new java.io.File(fileName);
		if(!file.isDirectory()) {
			this.logger.error(parameterName + " " + fileName + " is no regular directory!"); 
			return false;
		}
		if(!file.canWrite()) {
			this.logger.error(parameterName + " " + fileName + " has no write permissions!"); 
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 */
	private boolean checkFile(String parameterName, String fileName) {
		java.io.File file = null;
		try {
			file = new java.io.File(fileName);
		}
		catch(Exception e) {
			this.logger.error("Problems reading " + parameterName + " " + fileName + "!"); 
			return false;
		}
		if(!file.isFile()) {
			this.logger.error(parameterName + " " + fileName + " is no regular file!"); 
			return false;
		}
		if(!file.exists()) {
			this.logger.error(parameterName + " " + fileName + " not found!"); 
			return false;
		}
		if(!file.canRead()) {
			this.logger.error(parameterName + " " + fileName + " has no read permissions!"); 
			return false;
		}
		return true;
	}
}
