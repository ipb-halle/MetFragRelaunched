package de.ipbhalle.metfragweb.container;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;

import org.apache.log4j.Level;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;
import de.ipbhalle.metfraglib.exceptions.RetentionTimeNotFoundException;
import de.ipbhalle.metfraglib.exceptions.TooFewCandidatesException;
import de.ipbhalle.metfraglib.functions.HelperFunctions;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.ParameterDataTypes;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfragweb.datatype.AvailableScore;
import de.ipbhalle.metfragweb.datatype.SuspectListFileContainer;
import de.ipbhalle.metfragweb.datatype.UploadedSuspectListFile;
import de.ipbhalle.metfragweb.datatype.Weight;
import de.ipbhalle.metfragweb.helper.ProcessCompoundsThreadRunner;
import de.ipbhalle.metfragweb.helper.SettingsInitialiser;
import de.ipbhalle.metfragweb.helper.UserInputDataHandler;
import de.ipbhalle.metfragweb.helper.UserOutputDataHandler;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;

public class BeanSettingsContainer {
	
	/*
	 * candidate filters
	 */
	protected HashMap<String, Boolean> filterEnabledMap;
	protected HashMap<String, Boolean> filterValidMap;
	protected HashMap<String, String> filterNamesMap;
	
	/*
	 * candidate scores
	 */
	protected HashMap<String, Boolean> scoreEnabledMap;
	protected HashMap<String, Boolean> scoreValidMap;
	protected HashMap<String, String> scoreNamesMap;
	

	protected UserInputDataHandler userInputDataHandler;
	protected UserOutputDataHandler userOutputDataHandler;

	protected AvailableParameters availableParameters;
	protected String rootSeesionDir;
	
	/*
	 * ##################
	 * 	parameters needed
	 * ##################
	 */
	/*
	 * database search parameters
	 */
	protected boolean isDatabaseInitialise = false;
	protected String database = "KEGG";
	protected String formula = "";
	protected String identifiers = "";
	protected boolean includeReferences = false;
	protected String databaseRelativeMassDeviation = "5";
	protected String neutralMonoisotopicMass = "253.966126";
	protected String measuredMass = "";
	protected Integer measuredMassMode = 1;
	protected Boolean positiveChargeMeasured = true;
	protected String candidateFilePath = "";
	//availability of search properties
	protected boolean isLocalDatabaseDefined = false;
	protected boolean massSearchAvailable = false;
	protected boolean formulaSearchAvailable = false;
	protected boolean identifierSearchAvailable = false;
	protected boolean candidateProcessingFinished = false;
	//retrieved candidates list
	protected CandidateList retrievedCandidateList;
	
	protected String bondEnergyLipidMapsFilePath = "";
	//for local databases
	protected boolean localPubChemDatabase = false;
	protected boolean localKeggDatabase = false;
	
	//filter parameters
	protected int suspectListFilterFileIdentifier; 
	protected String includedElements = "C,H,N,O,P,S";
	protected String excludedElements = "Cl,Br";
	protected String includedMinimumElements = "NH20";
	protected String includedMaximumElements = "N3O4"; 
	protected String[] smartsFilterInclusion = new String[] {"[cR1]1[cR1][cR1][cR1][cR1][cR1]1"};
	protected String additionalSmartsFilterInclusion = "";
	protected String[] smartsFilterExclusion = new String[] {"[cR1]1[cR1][cR1][cR1][cR1][cR1]1"};
	protected String additionalSmartsFilterExclusion = "";
	protected boolean elementInclusionExclusiveFilterEnabled;
	protected boolean elementInclusionFilterValid = true;
	
	protected String substructureInformationFilterExpression = "( not c1ccccc1 and not C1CCCCC1 ) or [CX3](=O)[OX2H1]";
	protected String selectedInformationSmarts = "";
	
	protected SuspectListFileContainer suspectListFilterFileContainer;
	
	protected boolean forIdentSuspectListFilterEnabled = false;
	protected boolean dsstoxSuspectListFilterEnabled = false;
	
	/*
	 * score parameters
	 */
	protected int suspectListScoreFileIdentifier; 
	protected String[] smartsScoreInclusion = new String[] {"[cR1]1[cR1][cR1][cR1][cR1][cR1]1"};
	protected String additionalSmartsScoreInclusion = "";
	protected String[] smartsScoreExclusion = new String[] {"[cR1]1[cR1][cR1][cR1][cR1][cR1]1"};
	protected String additionalSmartsScoreExclusion = "";
	
	//retention time score parameters
	protected String retentionTimeScoreTrainingFilePath = "";
	protected String experimentalRetentionTimeValue = "0.0";
	protected String partitioningCoefficientColumnName;
	protected List<AvailableScore> availableCandidatePartitioningCoefficients;
	protected List<SelectItem> availablePartitioningCoefficients;
	
	protected List<AvailableScore> availableDatabaseScores;
	protected SuspectListFileContainer suspectListScoreFileContainer;

	protected boolean forIdentSuspectListScoreEnabled = false;
	protected boolean dsstoxSuspectListScoreEnabled = false;
	
	/*
	 * fragmentation parameters
	 */
	protected String relativeMassDeviation = "5";
	protected String absoluteMassDeviation = "0.001";
	protected Integer mode = 1;

	protected DefaultPeakList peaklistObject; 
	protected String peakList = "90.97445 681\n" +
			"106.94476 274\n" +
			"110.02750 110\n" +
			"115.98965 95\n" +
			"117.98540 384\n" +
			"124.93547 613\n" +
			"124.99015 146\n" +
			"125.99793 207\n" +
			"133.95592 777\n" +
			"143.98846 478\n" +
			"144.99625 352\n" +
			"146.00410 999\n" +
			"151.94641 962\n" +
			"160.96668 387\n" +
			"163.00682 782\n" +
			"172.99055 17\n" +
			"178.95724 678\n" +
			"178.97725 391\n" +
			"180.97293 999\n" +
			"196.96778 720\n" +
			"208.96780 999\n" +
			"236.96245 999\n" +
			"254.97312 999";
	protected LineChartModel spectrumModel;
	
	protected Boolean positiveCharge = true;
	
	protected boolean compoundsRetrieved = false;
	protected String retrieveCompoundsButtonLabel;
	protected String numberCompoundsLabel;
	
	/*
	 * process and settings object
	 */
	//used to override settings by an external file (settings.properties)
	protected MetFragGlobalSettings metFragSettingsFile = null;
	//the settings used to process
	protected MetFragGlobalSettings metFragSettings = null;
	protected CombinedMetFragProcess combinedMetFragProcess;
	protected String processCompoundsDialogHeader = "";
	
	/*
	 * results
	 */
	protected List<Weight> weights;
	
	/*
	 * initialization
	 */
	public BeanSettingsContainer(String rootSessionDir) {
		System.out.println("init BeanSettingsContainer");
		this.rootSeesionDir = rootSessionDir;
		this.init();
	}
	
	public void init() {
		this.availableParameters = new AvailableParameters();
		this.userInputDataHandler = new UserInputDataHandler(this);
		this.userOutputDataHandler = new UserOutputDataHandler(this);
		this.retrieveCompoundsButtonLabel = "Retrieve Candidates";
		this.numberCompoundsLabel = "";
		this.spectrumModel = new LineChartModel();
		/*
		 * read settings from external file
		 */
		this.metFragSettingsFile = this.readDatabaseConfigFromFile();

		//init filters
		this.initFilterEnabled();
		this.initFilterValid();
		this.initFilterNames();
		//init scores
		this.initScoreEnabled();
		this.initScoreValid();
		this.initScoreNames();
		//set default database
		this.setDatabase(this.database);
	}

	public UserInputDataHandler getUserInputDataHandler() {
		return this.userInputDataHandler;
	}
	
	public UserOutputDataHandler getUserOutputDataHandler() {
		return this.userOutputDataHandler;
	}
	
	public String getRootSessionFolder() {
		return this.rootSeesionDir;
	}
	
	protected void initScoreEnabled() {
		this.scoreEnabledMap = new HashMap<String, Boolean>();
		this.scoreEnabledMap.put("fragmenterScore", true);
		this.scoreEnabledMap.put("simScore", false);
		this.scoreEnabledMap.put("includedScoreSmarts", false);
		this.scoreEnabledMap.put("excludedScoreSmarts", false);
		this.scoreEnabledMap.put("retentionTimeTrainingFile", false);
		this.scoreEnabledMap.put("suspectListsScore", false);
		this.scoreEnabledMap.put("spectralSimilarity", false);
		this.scoreEnabledMap.put("exactSpectralSimilarity", false);
	}

	protected void initScoreNames() {
		this.scoreNamesMap = new HashMap<String, String>();
		this.scoreNamesMap.put("includedScoreSmarts", "Substructure Inclusion");
		this.scoreNamesMap.put("excludedScoreSmarts", "Substructure Exclusion");
		this.scoreNamesMap.put("retentionTimeTrainingFile", "Retention Time");
		this.scoreNamesMap.put("suspectListsScore", "Suspect Inclusion Lists");
		this.scoreNamesMap.put("fragmenterScore", "Fragmenter Score");
		this.scoreNamesMap.put("spectralSimilarity", "Spectral Similarity");
		this.scoreNamesMap.put("exactSpectralSimilarity", "Exact Spectral Similarity");
	}

	protected void initScoreValid() {
		this.scoreValidMap = new HashMap<String, Boolean>();
		this.scoreValidMap.put("includedScoreSmarts", true);
		this.scoreValidMap.put("excludedScoreSmarts", true);
		this.scoreValidMap.put("retentionTimeTrainingFile", false);
		this.scoreValidMap.put("suspectListsScore", false);
		this.scoreValidMap.put("fragmenterScore", true);
		this.scoreValidMap.put("simScore", true);
		this.scoreValidMap.put("spectralSimilarity", true);
		this.scoreValidMap.put("exactSpectralSimilarity", true);
	}
	
	protected void initFilterEnabled() {
		this.filterEnabledMap = new HashMap<String, Boolean>();
		this.filterEnabledMap.put("includedFilterElements", false);
		this.filterEnabledMap.put("excludedFilterElements", false);
		this.filterEnabledMap.put("includedFilterMaximumElements", false);
		this.filterEnabledMap.put("includedFilterMinimumElements", false);
		this.filterEnabledMap.put("includedFilterSmarts", false);
		this.filterEnabledMap.put("excludedFilterSmarts", false);
		this.filterEnabledMap.put("suspectListsFilter", false);
		this.filterEnabledMap.put("substructureInformationFilterExpression", false);
		this.filterEnabledMap.put("unconnectedStructureExclusionFilter", true);
		this.filterEnabledMap.put("isotopeFilter", true);
	}

	protected void initFilterNames() {
		this.filterNamesMap = new HashMap<String, String>();
		this.filterNamesMap.put("includedFilterElements", "Element Inclusion");
		this.filterNamesMap.put("excludedFilterElements", "Element Exclusion");
		this.filterNamesMap.put("includedFilterMaximumElements", "Maximum Number Elements");
		this.filterNamesMap.put("includedFilterMinimumElements", "Minimum Number Elements");
		this.filterNamesMap.put("includedFilterSmarts", "SMARTS Inclusion");
		this.filterNamesMap.put("excludedFilterSmarts", "SMARTS Exclusion");
		this.filterNamesMap.put("suspectListsFilter", "Suspect Inclusion Lists");
		this.filterNamesMap.put("substructureInformationFilterExpression", "Substructure Information");
		this.filterNamesMap.put("unconnectedStructureExclusionFilter", "Unconnected Structure Exclusion");
		this.filterNamesMap.put("isotopeFilter", "Isotope");
	}

	protected void initFilterValid() {
		this.filterValidMap = new HashMap<String, Boolean>();
		this.filterValidMap.put("includedFilterElements", true);
		this.filterValidMap.put("excludedFilterElements", true);
		this.filterValidMap.put("includedFilterMaximumElements", true);
		this.filterValidMap.put("includedFilterMinimumElements", true);
		this.filterValidMap.put("includedFilterSmarts", true);
		this.filterValidMap.put("excludedFilterSmarts", true);
		this.filterValidMap.put("substructureInformationFilterExpression", true);
		this.filterValidMap.put("suspectListsFilter", false);
		this.filterValidMap.put("unconnectedStructureExclusionFilter", true);
		this.filterValidMap.put("isotopeFilter", true);
	}
	
	public AvailableParameters getAvailableParameters() {
		return availableParameters;
	}

	public boolean readUploadedSettings(java.io.File[] contentFiles, Messages infoMessages, Messages errorMessages, 
			AvailableParameters avalableParameters, String rootDir) {
		errorMessages.removeKey("uploadParametersError");
		if(contentFiles == null) {
			errorMessages.setMessage("uploadParametersError", "Error: Uploading file failed");
			System.err.println("Error: Uploading file failed (zip file null)");
			return false;
		}
		java.io.File configFile = null;
		try {
			int numberConfigFiles = 0;
			for(int i = 0; i < contentFiles.length; i++) 
			{
				String filePath = contentFiles[i].getAbsolutePath();
				if(filePath.endsWith(".cfg")) {
					numberConfigFiles++;
					configFile = new java.io.File(filePath);
				}
			}
			if(numberConfigFiles == 0) {
				errorMessages.setMessage("uploadParametersError", "Error: No config file found (.cfg)");
				System.err.println("Error: No config file found (.cfg)");
				return false;
			}
			if(numberConfigFiles > 1) {
				errorMessages.setMessage("uploadParametersError", "Error: Multiple config files found (.cfg)");
				System.err.println("Error: Multiple config files found (.cfg)");
				return false;
			}
			java.util.HashMap<String, java.io.File> parameterFiles = new java.util.HashMap<String, java.io.File>();
			String parentFolder = configFile.getParent() + Constants.OS_SPECIFIC_FILE_SEPARATOR;
			for(int i = 0; i < contentFiles.length; i++) {
				String key = contentFiles[i].getAbsolutePath().replaceFirst(parentFolder, "");
				System.out.println(key + " " + contentFiles[i].getAbsolutePath());
				parameterFiles.put(key, contentFiles[i]);
			}
			
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(configFile));
			MetFragGlobalSettings settings = MetFragGlobalSettings.readSettings(configFile, null);
			breader.close();
			/*
			 * do the work
			 */
			if(!SettingsInitialiser.initilizeSettings(this, settings, infoMessages, errorMessages, avalableParameters, parameterFiles)) {
				System.err.println("Error when reading settings");
				return false;
			}
		} catch (FileNotFoundException e) {
			errorMessages.setMessage("uploadParametersError", "Error: Could not read ZIP file");
			System.err.println("Error: Could not read ZIP file");
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			errorMessages.setMessage("uploadParametersError", "Error: Could not read ZIP file");
			System.err.println("Error: Uploading file failed");
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			errorMessages.setMessage("uploadParametersError", "Error: Could not read config file");
			System.err.println("Error: Could not read config file " + configFile.getName());
			return false;
		} 
		System.out.println("Uploaded successfully.");
		return true;
	}
	
	/*
	 * database search settings
	 */
	public String getDatabase() {
		return database;
	}
	
	public void setDatabase(String database) {
		if(this.database.equals(database) && this.isDatabaseInitialise) return;
		this.resetDatabaseParameters();
		if(this.availableParameters.isNeedLocalFile(database)) {
			this.isLocalDatabaseDefined = true;
			this.candidateFilePath = "";
		}
		else {
			this.retrieveCompoundsButtonLabel = "Retrieve Candidates";
			this.isLocalDatabaseDefined = false;
		}
		if(!this.isLocalDatabaseDefined) {
			this.massSearchAvailable = true;
			this.formulaSearchAvailable = true;
			this.identifierSearchAvailable = true;
		}
		else {
			this.massSearchAvailable = false;
			this.formulaSearchAvailable = false;
			this.identifierSearchAvailable = false;
		}
		this.database = database;
		this.isDatabaseInitialise = true;
	}

	public boolean isIncludeReferences() {
		return this.includeReferences;
	}

	public void setIncludeReferences(boolean includeReferences) {
		this.includeReferences = includeReferences;
	}
	
	public boolean isLocalDatabaseDefined() {
		return this.isLocalDatabaseDefined;
	}

	public void setLocalDatabaseDefined(boolean isLocalDatabaseDefined) {
		this.isLocalDatabaseDefined = isLocalDatabaseDefined;
	}
	
	public String getCandidateFileName() {
		if(this.candidateFilePath == null || this.candidateFilePath.equals(""))
			return "";
		else {
			try {
				java.io.File file = new java.io.File(this.candidateFilePath);
				if(file.exists()) return file.getName();
			} catch (Exception e) {
				return "";
			}
		}
		return "";
	}
	
	public String getNeutralMonoisotopicMass() {
		return this.neutralMonoisotopicMass;
	}

	public void setNeutralMonoisotopicMass(String neutralMonoisotopicMass) {
		this.neutralMonoisotopicMass = neutralMonoisotopicMass;
	}
	
	public String getDatabaseRelativeMassDeviation() {
		return this.databaseRelativeMassDeviation;
	}

	public void setDatabaseRelativeMassDeviation(
			String databaseRelativeMassDeviation) {
		this.databaseRelativeMassDeviation = databaseRelativeMassDeviation;
	}
	
	public String getFormula() {
		return this.formula;
	}
	
	public void setFormula(String formula) {
		this.formula = formula;
	}
	
	public String getIdentifiers() {
		return this.identifiers;
	}

	public void setIdentifiers(String identifiers) {
		this.identifiers = identifiers;
	}
	
	public void initialiseAvailableCandidatePartitioningCoefficients() {
		java.util.List<AvailableScore> availableCandidatePartitioningCoefficients = new java.util.ArrayList<AvailableScore>();
		if (this.getRetrievedCandidateList() != null) {
			CandidateList candidates = this.getRetrievedCandidateList();
			if (candidates != null && candidates.getNumberElements() != 0) {
				java.util.Enumeration<String> keys = candidates.getElement(0).getProperties().keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (this.availableParameters.isPreservedCompoundPartitioningCoefficientProperty(key)) 
						continue;
					boolean checkFinished = false;
					int index = 0;
					while(!checkFinished) {	
						Object value = candidates.getElement(index).getProperty(key);
						if (key.equals(VariableNames.INCHI_NAME)) {
							availableCandidatePartitioningCoefficients.add(new AvailableScore("CDK"));
							checkFinished = true;
						} else {
							if (value != null) {
								try {
									if(value.getClass().getName().equals("java.lang.String"))
										Double.parseDouble((String) value);
									else if(!value.getClass().getName().equals("java.lang.Double")) {
										checkFinished = true;
										continue;
									}	
									availableCandidatePartitioningCoefficients.add(new AvailableScore(key));
									checkFinished = true;
								} catch (Exception e) {
									if(!value.equals("NA")) checkFinished = true;
									index++;
									if(candidates.getNumberElements() <= index) checkFinished = true;
									continue;
								}
							}
						}
					}
				}
			}
		}
		this.setAvailableCandidatePartitioningCoefficients(availableCandidatePartitioningCoefficients);
	}
	
	public void initialiseAvailableDatabaseScores() {
		java.util.List<AvailableScore> availableScores = new java.util.ArrayList<AvailableScore>();
		if (this.getRetrievedCandidateList() != null) {
			CandidateList candidates = this.getRetrievedCandidateList();
			if (candidates != null && candidates.getNumberElements() != 0) {
				java.util.Enumeration<String> keys = candidates.getElement(0).getProperties().keys();
				while (keys.hasMoreElements()) {
					String key = keys.nextElement();
					if (this.getAvailableParameters().isPreservedCompoundScoreProperty(key))
						continue;
					Object value = candidates.getElement(0).getProperty(key);
					if (value != null) {
						try {
							if (value instanceof Double)
								availableScores.add(new AvailableScore(key));
							else {
								Double.parseDouble((String) value);
								availableScores.add(new AvailableScore(key));
							}
						} catch (Exception e) {
							continue;
						}
					}
				}
			}
		}
		this.setAvailableDatabaseScores(availableScores);
	}
	
	//calculation of neutral monoisotopic mass
	public Integer getMeasuredMassMode() {
		return this.measuredMassMode;
	}

	public void setMeasuredMassMode(Integer measuredMassMode) {
		this.measuredMassMode = measuredMassMode;
		if(measuredMassMode == 1000) this.setPositiveChargeMeasured(true);
		else if(measuredMassMode == -1000) this.setPositiveChargeMeasured(false);
		else this.setPositiveChargeMeasured(Constants.ADDUCT_CHARGES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(this.measuredMassMode)));
	}

	public void setMeasuredMass(String measuredMass) {
		this.measuredMass = measuredMass;
	}

	public String getMeasuredMass() {
		return this.measuredMass;
	}
	
	public String getRetrieveCompoundsDialogHeader() {
		if(this.metFragSettings != null && this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			return ((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getRetrievingStatusString();
		return "Retrieving Candidates";
	}
	
	public void resetProcessStatus() {
		if(this.metFragSettings != null && this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).resetValues();
	}
	
	public void preprocessLocalDatabaseCandidates() throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, this.getDatabase());
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.candidateFilePath);
		CombinedMetFragProcess combinedMetFragProcess = new CombinedMetFragProcess(settings);
		boolean compoundsRetrieved = false;
		try {
			compoundsRetrieved = combinedMetFragProcess.retrieveCompounds();
		} catch (Exception e1) {
			compoundsRetrieved = false;
			throw new Exception();
		}
		if(compoundsRetrieved) {
			CandidateList candidates = combinedMetFragProcess.getCandidateList();
			if(candidates == null || candidates.getNumberElements() == 0) {
				this.massSearchAvailable = false;
				this.formulaSearchAvailable = false;
				this.identifierSearchAvailable = false;
				return;
			}
			this.massSearchAvailable = candidates.getElement(0).getProperties().containsKey(VariableNames.MONOISOTOPIC_MASS_NAME) ? true : false;
			this.formulaSearchAvailable = candidates.getElement(0).getProperties().containsKey(VariableNames.MOLECULAR_FORMULA_NAME) ? true : false;
			this.identifierSearchAvailable = candidates.getElement(0).getProperties().containsKey(VariableNames.IDENTIFIER_NAME) ? true : false;
			try {
				this.retrieveCompoundsButtonLabel = "Retrieve Candidates";
				this.databaseRelativeMassDeviation = null;
			} catch(Exception e) {
				this.retrieveCompoundsButtonLabel = "Retrieve Candidates";
			}
		}
	}

	public String getCandidateFilePath() {
		return this.candidateFilePath;
	}

	public void setCandidateFilePath(String candidateFilePath) {
		this.candidateFilePath = candidateFilePath;
	}

	public boolean isMassSearchAvailable() {
		return this.massSearchAvailable;
	}

	public void setMassSearchAvailable(boolean massSearchAvailable) {
		this.massSearchAvailable = massSearchAvailable;
	}

	public boolean isFormulaSearchAvailable() {
		return this.formulaSearchAvailable;
	}

	public void setFormulaSearchAvailable(boolean formulaSearchAvailable) {
		this.formulaSearchAvailable = formulaSearchAvailable;
	}

	public boolean isIdentifierSearchAvailable() {
		return this.identifierSearchAvailable;
	}

	public void setIdentifierSearchAvailable(boolean identifierSearchAvailable) {
		this.identifierSearchAvailable = identifierSearchAvailable;
	}

	public String getRetrieveCompoundsButtonLabel() {
		return this.retrieveCompoundsButtonLabel;
	}

	public void setRetrieveCompoundsButtonLabel(String retrieveCompoundsButtonLabel) {
		this.retrieveCompoundsButtonLabel = retrieveCompoundsButtonLabel;
	}

	public boolean isCompoundsRetrieved() {
		return this.compoundsRetrieved;
	}

	public void setCompoundsRetrieved(boolean compoundsRetrieved) {
		this.compoundsRetrieved = compoundsRetrieved;
	}
	
	public boolean isDatabaseInitialise() {
		return isDatabaseInitialise;
	}

	public void setDatabaseInitialise(boolean isDatabaseInitialise) {
		this.isDatabaseInitialise = isDatabaseInitialise;
	}
	 
	public CandidateList getRetrievedCandidateList() {
		return this.retrievedCandidateList;
	}
	
	/*
	 * filter settings
	 */
	public String getIncludedFilterElements() {
		return this.includedElements;
	}

	public void setIncludedFilterElements(String includedElements) {
		this.includedElements = includedElements;
	}

	public String getExcludedFilterElements() {
		return this.excludedElements;
	}

	public void setExcludedFilterElements(String excludedElements) {
		this.excludedElements = excludedElements;
	}

	public String getIncludedMinimumElements() {
		return this.includedMinimumElements;
	}

	public void setIncludedMinimumElements(String includedMinimumElements) {
		this.includedMinimumElements = includedMinimumElements;
	}

	public String getIncludedMaximumElements() {
		return this.includedMaximumElements;
	}

	public void setIncludedMaximumElements(String includedMaximumElements) {
		this.includedMaximumElements = includedMaximumElements;
	}

	public String[] getSmartsFilterInclusion() {
		return this.smartsFilterInclusion;
	}

	public String getAdditionalSmartsFilterInclusion() {
		return this.additionalSmartsFilterInclusion;
	}

	public void setSmartsFilterInclusion(String[] smartsFilterInclusion) {
		this.smartsFilterInclusion = smartsFilterInclusion;
	}

	public void setAdditionalSmartsFilterInclusion(String additionalSmartsFilterInclusion) {
		this.additionalSmartsFilterInclusion = additionalSmartsFilterInclusion;
	}

	public String[] getSmartsFilterExclusion() {
		return this.smartsFilterExclusion;
	}

	public String getAdditionalSmartsFilterExclusion() {
		return this.additionalSmartsFilterExclusion;
	}

	public void setSmartsFilterExclusion(String[] smartsFilterExclusion) {
		this.smartsFilterExclusion = smartsFilterExclusion;
	}

	public void setAdditionalSmartsFilterExclusion(String additionalSmartsFilterExclusion) {
		this.additionalSmartsFilterExclusion = additionalSmartsFilterExclusion;
	}
	
	public void setElementInclusionExclusiveFilterEnabled(
			boolean elementInclusionExclusiveFilterEnabled) {
		this.elementInclusionExclusiveFilterEnabled = elementInclusionExclusiveFilterEnabled;
	}
		
	public void setSubstructureInformationFilterExpression(String substructureInformationFilterExpression) {
		this.substructureInformationFilterExpression = substructureInformationFilterExpression;
	}
	
	public String getSubstructureInformationFilterExpression() {
		return this.substructureInformationFilterExpression;
	}
	
	public void setSelectedInformationSmarts(String selectedSmarts) {
		this.selectedInformationSmarts = selectedSmarts;
	}
	
	public String getSelectedInformationSmarts() {
		return this.selectedInformationSmarts;
	}
	
	public void preprocessRetentionTimeTrainingFile() throws Exception {
		if(this.retentionTimeScoreTrainingFilePath == null || this.retentionTimeScoreTrainingFilePath.trim().length() == 0) return;
		if(this.availableCandidatePartitioningCoefficients == null || this.availableCandidatePartitioningCoefficients.size() == 0) return;
		java.io.File retentionTimeTrainingFile = new java.io.File(this.retentionTimeScoreTrainingFilePath);
		if(retentionTimeTrainingFile != null && retentionTimeTrainingFile.exists() && retentionTimeTrainingFile.canRead()) {
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			settings.set(VariableNames.LOG_LEVEL_NAME, Level.DEBUG);
			settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "LocalProperty");
			settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.retentionTimeScoreTrainingFilePath);
			CombinedMetFragProcess combinedMetFragProcess = new CombinedMetFragProcess(settings);
			boolean entriesRetrieved = combinedMetFragProcess.retrieveCompounds();
			if(entriesRetrieved) {
				CandidateList candidates = combinedMetFragProcess.getCandidateList();
				if(candidates == null || candidates.getNumberElements() <= 1) throw new TooFewCandidatesException();
				if(!candidates.getElement(0).getProperties().containsKey(VariableNames.RETENTION_TIME_NAME)) 
					throw new RetentionTimeNotFoundException();
				this.availablePartitioningCoefficients = new java.util.ArrayList<SelectItem>();
				java.util.Enumeration<String> keys = candidates.getElement(0).getProperties().keys();
				while(keys.hasMoreElements()) {
					String currentKey = keys.nextElement().trim();
					if(currentKey.equals(VariableNames.INCHI_NAME)) {
						currentKey = "CDK";
					}
					for(int k = 0; k < this.availableCandidatePartitioningCoefficients.size(); k++) {
						if(this.availableCandidatePartitioningCoefficients.get(k).getLabel().equals(currentKey)) {
							if(currentKey.equals("CDK")) this.availablePartitioningCoefficients.add(new SelectItem(currentKey));
							else {
								try {
									Double.parseDouble((String)candidates.getElement(0).getProperty(currentKey));
									this.availablePartitioningCoefficients.add(new SelectItem(currentKey));
								}
								catch(Exception e) {
									continue;
								}
							}
						}
					}
				}
			}
		}
		else this.availablePartitioningCoefficients = null;
	}
	
	protected void resetDatabaseParameters() {
		this.candidateFilePath = "";
		this.numberCompoundsLabel = "";
		this.retrievedCandidateList = null;
		if(this.combinedMetFragProcess != null) this.combinedMetFragProcess.nullify();
		this.combinedMetFragProcess = null;
		this.compoundsRetrieved = false;
		this.includeReferences = false;
		/*
		 * reset retention time score
		 */
		this.retentionTimeScoreTrainingFilePath = "";
		this.scoreEnabledMap.put("retentionTimeTrainingFile", false);
		this.scoreValidMap.put("retentionTimeTrainingFile", false);
		this.availablePartitioningCoefficients = null;
		this.experimentalRetentionTimeValue = "0.0";
	}
	
	public void retrieveCompounds(Messages infoMessages, Messages errorMessages) throws Exception {
		/*
		 * define the settings
		 */
		infoMessages.removeKey("retrieveCompoundsInfo");
		errorMessages.removeKey("retrieveCompoundsError");
		this.retentionTimeScoreTrainingFilePath = "";
		this.scoreEnabledMap.put("retentionTimeTrainingFile", false);
		this.scoreValidMap.put("retentionTimeTrainingFile", false);
		this.availablePartitioningCoefficients = null;
		this.experimentalRetentionTimeValue = "0.0";
		
		this.retrievedCandidateList = null;
		this.numberCompoundsLabel = "";
		this.setMetFragSettings(new MetFragGlobalSettings());
		this.prepareDatabaseSettings();
		this.metFragSettings.includeSettings(this.metFragSettingsFile, true, this.database.equals("ChemSpider") ? null : new String[] {"ChemSpiderToken"});
		/*
		 * init the new combinedMetFragProcess
		 */
		System.out.println("retrieveCompounds");
		this.combinedMetFragProcess = new CombinedMetFragProcess(this.getMetFragSettings());
		try {
			this.setCompoundsRetrieved(this.combinedMetFragProcess.retrieveCompounds());
		} catch (Exception e) {
			e.printStackTrace();
			errorMessages.setMessage("retrieveCompoundsError", "Error fetching candidates.");
			return;
		}
		/*
		 * set label
		 */
		if(this.combinedMetFragProcess == null || this.combinedMetFragProcess.getCandidateList() == null) this.numberCompoundsLabel = "";
		if(this.combinedMetFragProcess.getCandidateList().getNumberElements() == 1) {
			infoMessages.setMessage("retrieveCompoundsInfo", this.combinedMetFragProcess.getCandidateList().getNumberElements() + " Candidate");
			this.retrievedCandidateList = this.combinedMetFragProcess.getCandidateList().clone();
		}
		else {
			infoMessages.setMessage("retrieveCompoundsInfo", this.combinedMetFragProcess.getCandidateList().getNumberElements() + " Candidates");
			this.retrievedCandidateList = this.combinedMetFragProcess.getCandidateList();
		}
	}
	
	public int getSuspectListFilterFileIdentifier() {
		return this.suspectListFilterFileIdentifier;
	}

	public void incrementSuspectListFilterFileIdentifier() {
		this.suspectListFilterFileIdentifier++;
	}
	
	public void setSuspectListFilterFileIdentifier(int suspectListFilterFileIdentifier) {
		this.suspectListFilterFileIdentifier = suspectListFilterFileIdentifier;
	}

	public SuspectListFileContainer getSuspectListFilterFileContainer() {
		return this.suspectListFilterFileContainer;
	}

	public void setSuspectListFilterFileContainer(
			SuspectListFileContainer suspectListFilterFileContainer) {
		this.suspectListFilterFileContainer = suspectListFilterFileContainer;
	}

	public String addUploadedSuspectListFilterFile(UploadedSuspectListFile file) throws Exception {
		if(this.suspectListFilterFileContainer == null) this.suspectListFilterFileContainer = new SuspectListFileContainer();
		if(this.suspectListFilterFileContainer.contains(file)) {
			System.out.println("A suspect list with name " + file.getName() + " has already bean uploaded.");
			return "A suspect list with name " + file.getName() + " has already bean uploaded.\n";
		}
		if(this.suspectListFilterFileContainer.size() >= 5) {
			System.out.println("Limited to 5 files uploaded concurrantly.");
			return "Limited to 5 files uploaded concurrantly.\n";
		}
		this.suspectListFilterFileContainer.addFile(file);
		return "";
	}

	public boolean isElementInclusionExclusiveFilterEnabled() {
		return this.elementInclusionExclusiveFilterEnabled;
	}
	
	public String getFilterName(String filterName) {
		return this.filterNamesMap.get(filterName);
	}
	
	public boolean isElementInclusionFilterValid() {
		return elementInclusionFilterValid;
	}

	public void setElementInclusionFilterValid(boolean elementInclusionFilterValid) {
		this.elementInclusionFilterValid = elementInclusionFilterValid;
	}
	
	public boolean isForIdentSuspectListFilterEnabled() {
		return forIdentSuspectListFilterEnabled;
	}

	public void setForIdentSuspectListFilterEnabled(boolean forIdentSuspectListFilterEnabled) {
		this.forIdentSuspectListFilterEnabled = forIdentSuspectListFilterEnabled;
	}

	public boolean isDsstoxSuspectListFilterEnabled() {
		return dsstoxSuspectListFilterEnabled;
	}

	public void setDsstoxSuspectListFilterEnabled(boolean dsstoxSuspectListFilterEnabled) {
		this.dsstoxSuspectListFilterEnabled = dsstoxSuspectListFilterEnabled;
	}

	/*
	 * scores
	 */
	public List<AvailableScore> getAvailableDatabaseScores() {
		return this.availableDatabaseScores;
	}

	public void setAvailableDatabaseScores(java.util.List<AvailableScore> availableDatabaseScores) {
		this.availableDatabaseScores = availableDatabaseScores;
	}

	//retention time scores
	public String getRetentionTimeScoreTrainingFileName() {
		if(this.retentionTimeScoreTrainingFilePath == null || this.retentionTimeScoreTrainingFilePath.equals(""))
			return "";
		else {
			try {
				java.io.File file = new java.io.File(this.retentionTimeScoreTrainingFilePath);
				if(file.exists()) return file.getName();
			} catch (Exception e) {
				return "";
			}
		}
		return "";
	}
	
	public List<AvailableScore> getAvailableCandidatePartitioningCoefficients() {
		return this.availableCandidatePartitioningCoefficients;
	}

	public void setAvailableCandidatePartitioningCoefficients(java.util.List<AvailableScore> availableCandidatePartitioningCoefficients) {
		this.availableCandidatePartitioningCoefficients = availableCandidatePartitioningCoefficients;
	}
	
	public List<SelectItem> getAvailablePartitioningCoefficients() {
		return this.availablePartitioningCoefficients;
	}

	public void setAvailablePartitioningCoefficients(java.util.List<SelectItem> availablePartitioningCoefficients) {
		this.availablePartitioningCoefficients = availablePartitioningCoefficients;
	}
	
	public String getRetentionTimeScoreTrainingFilePath() {
		return retentionTimeScoreTrainingFilePath;
	}

	public void setRetentionTimeScoreTrainingFilePath(
			String retentionTimeScoreModelValuePath) {
		this.retentionTimeScoreTrainingFilePath = retentionTimeScoreModelValuePath;
	}
	
	public void setExperimentalRetentionTimeValue(
			String experimentalRetentionTimeValue) {
		this.experimentalRetentionTimeValue = experimentalRetentionTimeValue;
	}

	public String getPartitioningCoefficientColumnName() {
		return partitioningCoefficientColumnName;
	}

	public void setPartitioningCoefficientColumnName(
			String partitioningCoefficientColumnName) {
		this.partitioningCoefficientColumnName = partitioningCoefficientColumnName;
	}
	
	//substructure score
	public void setAdditionalSmartsScoreExclusion(String additionalSmartsScoreExclusion) {
		this.additionalSmartsScoreExclusion = additionalSmartsScoreExclusion;
	}
	
	public void setAdditionalSmartsScoreInclusion(String additionalSmartsScoreInclusion) {
		this.additionalSmartsScoreInclusion = additionalSmartsScoreInclusion;
	}

	public void setSmartsScoreExclusion(String[] smartsScoreExclusion) {
		this.smartsScoreExclusion = smartsScoreExclusion;
	}
	
	public void setSmartsScoreInclusion(String[] smartsScoreInclusion) {
		this.smartsScoreInclusion = smartsScoreInclusion;
	}
	
	public String[] getSmartsScoreInclusion() {
		return this.smartsScoreInclusion;
	}

	public String[] getSmartsScoreExclusion() {
		return this.smartsScoreExclusion;
	}

	public String getAdditionalSmartsScoreInclusion() {
		return this.additionalSmartsScoreInclusion;
	}

	public String getAdditionalSmartsScoreExclusion() {
		return this.additionalSmartsScoreExclusion;
	}

	public String getExperimentalRetentionTimeValue() {
		return experimentalRetentionTimeValue;
	}

	//suspect list inclusion score
	public String addUploadedSuspectListScoreFile(UploadedSuspectListFile file) throws Exception {
		if(this.suspectListScoreFileContainer == null) this.suspectListScoreFileContainer = new SuspectListFileContainer();
		if(this.suspectListScoreFileContainer.contains(file)) {
			System.out.println("A suspect list with name " + file.getName() + " has already bean uploaded.");
			return "A suspect list with name " + file.getName() + " has already bean uploaded.\n";
		}
		if(this.suspectListScoreFileContainer.size() >= 5) {
			System.out.println("Limited to 5 files uploaded concurrantly.");
			return "Limited to 5 files uploaded concurrantly.\n";
		}
		this.suspectListScoreFileContainer.addFile(file);
		return "";
	}
	
	public boolean isForIdentSuspectListScoreEnabled() {
		return forIdentSuspectListScoreEnabled;
	}

	public void setForIdentSuspectListScoreEnabled(boolean forIdentSuspectListScoreEnabled) {
		this.forIdentSuspectListScoreEnabled = forIdentSuspectListScoreEnabled;
	}

	public boolean isDsstoxSuspectListScoreEnabled() {
		return dsstoxSuspectListScoreEnabled;
	}

	public void setDsstoxSuspectListScoreEnabled(boolean dsstoxSuspectListScoreEnabled) {
		this.dsstoxSuspectListScoreEnabled = dsstoxSuspectListScoreEnabled;
	}

	public void incrementSuspectListScoreFileIdentifier() {
		this.suspectListScoreFileIdentifier++;
	}
	
	public int getSuspectListScoreFileIdentifier() {
		return this.suspectListScoreFileIdentifier;
	}

	public void setSuspectListScoreFileIdentifier(int suspectListScoreFileIdentifier) {
		this.suspectListScoreFileIdentifier = suspectListScoreFileIdentifier;
	}

	public SuspectListFileContainer getSuspectListScoreFileContainer() {
		return this.suspectListScoreFileContainer;
	}

	public void setSuspectListScoreFileContainer(
			SuspectListFileContainer suspectListScoreFileContainer) {
		this.suspectListScoreFileContainer = suspectListScoreFileContainer;
	}
	
	/*
	 * fragmenter settings
	 */
	public String getRelativeMassDeviation() {
		return this.relativeMassDeviation;
	}

	public void setRelativeMassDeviation(String relativeMassDeviation) {
		this.relativeMassDeviation = relativeMassDeviation;
	}

	public String getAbsoluteMassDeviation() {
		return this.absoluteMassDeviation;
	}

	public void setAbsoluteMassDeviation(String absoluteMassDeviation) {
		this.absoluteMassDeviation = absoluteMassDeviation;
	}

	public Integer getMode() {
		return this.mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
		if(mode == 1000) this.setPositiveCharge(true);
		else if(mode == -1000) this.setPositiveCharge(false);
		else this.setPositiveCharge(Constants.ADDUCT_CHARGES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(this.mode)));
	}

	public Boolean isPositiveChargeMeasured() {
		return this.positiveChargeMeasured;
	}

	public void setPositiveChargeMeasured(Boolean positiveChargeMeasured) {
		this.positiveChargeMeasured = positiveChargeMeasured;
	}
	
	public DefaultPeakList getPeakListObject() {
		return this.peaklistObject;
	}
	
	public String getPeakList() {
		return this.peakList;
	}

	public void setPeakList(String peakList) {
		this.peakList = peakList;
	}

	public Boolean isPositiveCharge() {
		return this.positiveCharge;
	}

	public void setPositiveCharge(Boolean positiveCharge) {
		this.positiveCharge = positiveCharge;
	}
	
	public DefaultPeakList generatePeakListObject() throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PEAK_LIST_STRING_NAME, this.peakList);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 0.0);
		DefaultPeakList peaklistObject = new StringTandemMassPeakListReader(settings).read();
		return peaklistObject;
	}
	
	/*
	 * overall settings processing
	 */
	private void prepareDatabaseSettings() throws ParameterNotKnownException {
		String database = this.database;
		if(database.equals("PubChem")) {
			if(this.localPubChemDatabase) {
				if(this.includeReferences) database = "LocalExtendedPubChem";
				else database = "LocalPubChem";
			}
			else if(this.includeReferences) database = "ExtendedPubChem";
		}
		else if(database.equals("KEGG") && this.localKeggDatabase) database = "LocalKegg";
		this.getMetFragSettings().set(VariableNames.METFRAG_DATABASE_TYPE_NAME, database);
		this.getMetFragSettings().set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, Double.parseDouble(this.getNeutralMonoisotopicMass()));
		if(this.isFormulaSearchAvailable()) 
			this.getMetFragSettings().set(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME, this.getFormula() == null || this.getFormula().trim().length() == 0 ? null : this.getFormula().trim());
		if(this.isMassSearchAvailable()) this.getMetFragSettings().set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, this.getDatabaseRelativeMassDeviation() == null || this.getDatabaseRelativeMassDeviation().trim().length() == 0 ? null : Double.parseDouble(this.getDatabaseRelativeMassDeviation()));
		if(this.isIdentifierSearchAvailable()) this.getMetFragSettings().set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, this.getIdentifiers() == null || this.getIdentifiers().trim().length() == 0 ? null : (String[])ParameterDataTypes.getParameter(this.getIdentifiers().trim(), VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if(this.isLocalDatabaseDefined()) {
			if(this.getCandidateFilePath() != null && !this.equals("")) 
				this.getMetFragSettings().set(VariableNames.LOCAL_DATABASE_PATH_NAME, this.getCandidateFilePath());
			else return;
		}
	}
	
	private void prepareCompoundFilterSettings() throws Exception {
		java.util.Vector<String> compoundPreFilters = new java.util.Vector<String>();
		//unconnected structure exclusion filter
		if(this.filterEnabledMap.get("unconnectedStructureExclusionFilter") && this.filterValidMap.get("unconnectedStructureExclusionFilter")) 
			compoundPreFilters.add("UnconnectedCompoundFilter");
		//isotope filter
		if(this.filterEnabledMap.get("isotopeFilter") && this.filterValidMap.get("isotopeFilter")) 
			compoundPreFilters.add("IsotopeFilter");
		//element inclusion score
		if(this.elementInclusionExclusiveFilterEnabled && this.filterEnabledMap.get("includedFilterElements") && this.filterValidMap.get("includedFilterElements")) {
			compoundPreFilters.add("ElementInclusionExclusiveFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME, ParameterDataTypes.getParameter(this.includedElements, VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME));
		} 	
		else if(this.filterEnabledMap.get("includedFilterElements") && this.filterValidMap.get("includedFilterElements")) {
			compoundPreFilters.add("ElementInclusionFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME, ParameterDataTypes.getParameter(this.includedElements, VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME));
		} else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME);
		
		//element exclusion filter
		if(this.filterEnabledMap.get("excludedFilterElements") && this.filterValidMap.get("excludedFilterElements")) {
			compoundPreFilters.add("ElementExclusionFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME, ParameterDataTypes.getParameter(this.excludedElements, VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME));
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME);
		//maximum elements filter
		if(this.filterEnabledMap.get("includedFilterMaximumElements") && this.filterValidMap.get("includedFilterMaximumElements")) {
			compoundPreFilters.add("MaximumElementsFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME, ParameterDataTypes.getParameter(this.includedMaximumElements, VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME));				
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME);
		//minimum elements filter
		if(this.filterEnabledMap.get("includedFilterMinimumElements") && this.filterValidMap.get("includedFilterMinimumElements")) {
			compoundPreFilters.add("MinimumElementsFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME, ParameterDataTypes.getParameter(this.includedMinimumElements, VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME));				
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME);
		//SMARTS inclusion filter
		if(this.filterEnabledMap.get("includedFilterSmarts") && this.filterValidMap.get("includedFilterSmarts")) {
			compoundPreFilters.add("SmartsSubstructureInclusionFilter");
			String filterString = HelperFunctions.stringArrayToString(this.smartsFilterInclusion);
			if(filterString.length() == 0) filterString += this.additionalSmartsFilterInclusion;
			else filterString += "," + this.additionalSmartsFilterInclusion;
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME, ParameterDataTypes.getParameter(filterString, VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME));				
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME);
		//SMARTS exclusion filter
		if(this.filterEnabledMap.get("excludedFilterSmarts") && this.filterValidMap.get("excludedFilterSmarts")) {
			compoundPreFilters.add("SmartsSubstructureExclusionFilter");
			String filterString = HelperFunctions.stringArrayToString(this.smartsFilterExclusion);
			if(filterString.length() == 0) filterString += this.additionalSmartsFilterExclusion;
			else filterString += "," + this.additionalSmartsFilterExclusion;
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME, ParameterDataTypes.getParameter(filterString, VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME));				
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME);
		//substructure information
		if(this.filterEnabledMap.get("substructureInformationFilterExpression") && this.filterValidMap.get("substructureInformationFilterExpression")) {
			compoundPreFilters.add("SubstructureInformationFilter");
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, ParameterDataTypes.getParameter(this.substructureInformationFilterExpression, VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME));				
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME);
		//suspect lists inclusion filter
		if(this.filterEnabledMap.get("suspectListsFilter") && this.filterValidMap.get("suspectListsFilter")) {
			compoundPreFilters.add("SuspectListFilter");
			String[] suspectListFilter = null;
			if(this.suspectListFilterFileContainer != null && this.suspectListFilterFileContainer.size() != 0)
				suspectListFilter = this.suspectListFilterFileContainer.getAbsoluteFileNamesAsString();
			if(this.isForIdentSuspectListFilterEnabled()) {
				if(suspectListFilter == null) 
					suspectListFilter = new String[] {VariableNames.FORIDENT_SUSPECTLIST_NAME};
				else {
					String[] newSuspectListFilter = new String[suspectListFilter.length + 1];
					for(int k = 0; k < suspectListFilter.length; k++)
						newSuspectListFilter[k] = suspectListFilter[k];
					newSuspectListFilter[newSuspectListFilter.length - 1] = VariableNames.FORIDENT_SUSPECTLIST_NAME;
					suspectListFilter = newSuspectListFilter;
				}	
			}
			if(this.isDsstoxSuspectListFilterEnabled()) {
				if(suspectListFilter == null) 
					suspectListFilter = new String[] {VariableNames.DSSTOX_SUSPECTLIST_NAME};
				else {
					String[] newSuspectListFilter = new String[suspectListFilter.length + 1];
					for(int k = 0; k < suspectListFilter.length; k++)
						newSuspectListFilter[k] = suspectListFilter[k];
					newSuspectListFilter[newSuspectListFilter.length - 1] = VariableNames.DSSTOX_SUSPECTLIST_NAME;
					suspectListFilter = newSuspectListFilter;
				}	
			}
			this.metFragSettings.set(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME, suspectListFilter);
		}
		else this.metFragSettings.remove(VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME);
		
		if(compoundPreFilters.size() != 0) {
			String[] compoundPreFiltersArray = new String[compoundPreFilters.size()];
			for(int i = 0; i < compoundPreFilters.size(); i++) compoundPreFiltersArray[i] = compoundPreFilters.get(i);
			this.metFragSettings.set(VariableNames.METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME, compoundPreFiltersArray);
		}
	}

	private void prepareCompoundScoreSettings() throws Exception {
		java.util.Vector<String> compoundScores = new java.util.Vector<String>();
		//fragmenter score
		if(this.scoreEnabledMap.get("fragmenterScore") && this.scoreValidMap.get("fragmenterScore")) {
			compoundScores.add(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME);
		}
		//SMARTS inclusion
		if(this.scoreEnabledMap.get("includedScoreSmarts") && this.scoreValidMap.get("includedScoreSmarts")) {
			compoundScores.add("SmartsSubstructureInclusionScore");
			String filterString = HelperFunctions.stringArrayToString(this.smartsScoreInclusion);
			if(filterString.length() == 0) filterString += this.additionalSmartsScoreInclusion;
			else filterString += "," + this.additionalSmartsScoreInclusion;
			this.metFragSettings.set(VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME, ParameterDataTypes.getParameter(filterString, VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME));
		}
		else this.metFragSettings.remove(VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME);
		//SMARTS exclusion
		if(this.scoreEnabledMap.get("excludedScoreSmarts") && this.scoreValidMap.get("excludedScoreSmarts")) {
			compoundScores.add("SmartsSubstructureExclusionScore");
			String filterString = HelperFunctions.stringArrayToString(this.smartsScoreExclusion);
			if(filterString.length() == 0) filterString += this.additionalSmartsScoreExclusion;
			else filterString += "," + this.additionalSmartsScoreExclusion;
			this.metFragSettings.set(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME, ParameterDataTypes.getParameter(filterString, VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME));
		}
		else this.metFragSettings.remove(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME);
		//spectral similarity
		if(this.scoreEnabledMap.get("spectralSimilarity") && this.scoreValidMap.get("spectralSimilarity")) {
			compoundScores.add("OfflineMetFusionScore");
		}
		//exact spectral similarity
		if(this.scoreEnabledMap.get("exactSpectralSimilarity") && this.scoreValidMap.get("exactSpectralSimilarity")) {
			compoundScores.add("OfflineIndividualMoNAScore");
		}
		//suspect list inclusion score
		if(this.scoreEnabledMap.get("suspectListsScore") && this.scoreValidMap.get("suspectListsScore")) {
			compoundScores.add("SuspectListScore");
			String[] suspectListScore = null;
			if(this.suspectListScoreFileContainer != null && this.suspectListScoreFileContainer.size() != 0)
				suspectListScore = this.suspectListScoreFileContainer.getAbsoluteFileNamesAsString();
			if(this.isForIdentSuspectListScoreEnabled()) {
				if(suspectListScore == null)
					suspectListScore = new String[] {VariableNames.FORIDENT_SUSPECTLIST_NAME};
				else {
					String[] newSuspectListScore = new String[suspectListScore.length + 1];
					for(int k = 0; k < suspectListScore.length; k++)
						newSuspectListScore[k] = suspectListScore[k];
					newSuspectListScore[newSuspectListScore.length - 1] = VariableNames.FORIDENT_SUSPECTLIST_NAME;
					suspectListScore = newSuspectListScore;
				}	
			}
			if(this.isDsstoxSuspectListScoreEnabled()) {
				if(suspectListScore == null)
					suspectListScore = new String[] {VariableNames.DSSTOX_SUSPECTLIST_NAME};
				else {
					String[] newSuspectListScore = new String[suspectListScore.length + 1];
					for(int k = 0; k < suspectListScore.length; k++)
						newSuspectListScore[k] = suspectListScore[k];
					newSuspectListScore[newSuspectListScore.length - 1] = VariableNames.DSSTOX_SUSPECTLIST_NAME;
					suspectListScore = newSuspectListScore;
				}	
			}
			this.metFragSettings.set(VariableNames.SCORE_SUSPECT_LISTS_NAME, suspectListScore);
		}
		else this.metFragSettings.remove(VariableNames.SCORE_SUSPECT_LISTS_NAME);
		if(this.scoreEnabledMap.get("retentionTimeTrainingFile") && this.scoreValidMap.get("retentionTimeTrainingFile")) {
			compoundScores.add("RetentionTimeScore");
			this.metFragSettings.set(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME, this.retentionTimeScoreTrainingFilePath);
			this.metFragSettings.set(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME, Double.parseDouble(this.experimentalRetentionTimeValue));
			if(this.partitioningCoefficientColumnName != null && !this.partitioningCoefficientColumnName.equals("CDK")) 
				this.metFragSettings.set(VariableNames.USER_LOG_P_VALUE_NAME, this.partitioningCoefficientColumnName);
			else this.metFragSettings.remove(VariableNames.USER_LOG_P_VALUE_NAME);
		}
		else {
			this.metFragSettings.remove(VariableNames.RETENTION_TIME_TRAINING_FILE_NAME);
			this.metFragSettings.remove(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME);
			this.metFragSettings.remove(VariableNames.USER_LOG_P_VALUE_NAME);
		}
		//database scores
		if(this.availableDatabaseScores != null)
			for(int i = 0; i < this.availableDatabaseScores.size(); i++)
				if(this.availableDatabaseScores.get(i).isSelected()) compoundScores.add(this.availableDatabaseScores.get(i).getLabel());
		//set the sim score
		if(this.scoreEnabledMap.get("simScore") && this.scoreValidMap.get("simScore")) {
			compoundScores.add("SimScore");
		}
		//set the scores
		if(compoundScores.size() != 0) {
			String[] compoundScoresArray = new String[compoundScores.size()];
			for(int i = 0; i < compoundScores.size(); i++) compoundScoresArray[i] = compoundScores.get(i);
			this.metFragSettings.set(VariableNames.METFRAG_SCORE_TYPES_NAME, compoundScoresArray);
		}
		//init the weights
		Double[] weights = new Double[compoundScores.size()];
		for(int i = 0; i < weights.length; i++) weights[i] = 1.0;
		//set the weights
		this.metFragSettings.set(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, weights);
	}
	
	private void prepareFragmenterSettings() throws Exception {
		this.metFragSettings.set(VariableNames.PEAK_LIST_STRING_NAME, this.peakList);
		this.metFragSettings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, Double.parseDouble(this.relativeMassDeviation));
		this.metFragSettings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, Double.parseDouble(this.absoluteMassDeviation));
		this.metFragSettings.set(VariableNames.PEAK_LIST_NAME, new StringTandemMassPeakListReader(this.metFragSettings).read());
		int mode = this.mode;
		if(mode == 1000 || mode == -1000) mode = 0;
		this.metFragSettings.set(VariableNames.PRECURSOR_ION_MODE_NAME, mode);
		this.metFragSettings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, this.isPositiveCharge());
	}

	public boolean generateSpectrumModelView(Messages errorMessages) {
		this.spectrumModel = new LineChartModel();
		if(this.peakList == null) {
			errorMessages.setMessage("peakListInputError", "Error: Peak list required.");
			return false;
		}
		String string = (this.peakList).trim();
		try {
			if(string.length() == 0) throw new Exception();
			this.peaklistObject = this.generatePeakListObject();
			double maxMZ = ((TandemMassPeak)this.peaklistObject.getElement(this.peaklistObject.getNumberElements() - 1)).getMass();
			this.spectrumModel.getAxis(AxisType.Y).setMin(0);
			this.spectrumModel.getAxis(AxisType.Y).setMax(1050);
			this.spectrumModel.getAxis(AxisType.Y).setLabel("Intensity");
			this.spectrumModel.getAxis(AxisType.Y).setTickInterval("250");
			this.spectrumModel.getAxis(AxisType.Y).setTickCount(5);
			this.spectrumModel.getAxis(AxisType.X).setMin(0.0);
			this.spectrumModel.getAxis(AxisType.X).setTickAngle(-30);
			this.spectrumModel.getAxis(AxisType.X).setLabel("m/z");
			this.spectrumModel.getAxis(AxisType.X).setTickFormat("%.2f");
			this.spectrumModel.setZoom(true);
			this.spectrumModel.setMouseoverHighlight(true);
			this.spectrumModel.setShowDatatip(false);
			this.spectrumModel.setShowPointLabels(false);
			this.spectrumModel.setExtender("spectrumViewExtender");
			String xTickInterval = "100.000";
			if(maxMZ <= 400) xTickInterval = "50.000";
			if(maxMZ <= 150) xTickInterval = "10.000"; 
			this.spectrumModel.getAxis(AxisType.X).setTickInterval(xTickInterval);
			for(int i = 0; i < this.peaklistObject.getNumberElements(); i++) 
			{
				TandemMassPeak peak = (TandemMassPeak)this.peaklistObject.getElement(i);
				LineChartSeries newSeries = new LineChartSeries();
				newSeries.set(peak.getMass() + 0.0000001, -10000000.0);
				newSeries.set(peak.getMass(), peak.getRelativeIntensity());
				this.spectrumModel.addSeries(newSeries);
			}
			this.spectrumModel.setSeriesColors("00749f");
		}
		catch(Exception e) {
			this.spectrumModel = new LineChartModel();
			errorMessages.setMessage("peakListInputError", "Error: Invalid peak list value.");
			return false;	
		}
		errorMessages.removeKey("peakListInputError");
		return true;
	}

	public LineChartModel getSpectrumModelView() {
		return this.spectrumModel;
	}
	
	public String getProcessingStatus() {
		if(this.metFragSettings != null && this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			return ((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getProcessStatusString();
		return "Processing";
	}
	
	public int getProcessingProgress() {
		if(this.metFragSettings != null && this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			return (((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getNextPercentageValue().get() - 1) * 10;
		return 100;
	}
	
	public int getProcessingProgress(ProcessCompoundsThreadRunner processRunner) {
		if(this.metFragSettings != null) 
			if(this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME)) {
				ProcessingStatus processingStatus = (ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
				if(processingStatus != null && (processRunner == null || (!processRunner.isRenderingMolecules()))) {
					return processingStatus.getPercentageValue().get();
				}
				else if(processRunner != null && processRunner.isRenderingMolecules()) {
					return processRunner.getRenderedMoleculesPercentageValue();
				}
			}
		return 100;
	}
	
	/**
	 * 
	 * @param errorMessages
	 * @return
	 */
	public MetFragGlobalSettings getMetFragSettings() {
		return this.metFragSettings;
	}

	public void setMetFragSettings(MetFragGlobalSettings metFragSettings) {
		this.metFragSettings = metFragSettings;
	}

	public void prepareSettingsObject() throws Exception {
		this.metFragSettings = new MetFragGlobalSettings();
		System.out.println("prepareDatabaseSettings");
		this.prepareDatabaseSettings();
		System.out.println("prepareCompoundFilterSettings");
		this.prepareCompoundFilterSettings();
		System.out.println("prepareCompoundScoreSettings");
		this.prepareCompoundScoreSettings();
		System.out.println("prepareFragmenterSettings");
		this.prepareFragmenterSettings();
		
		this.metFragSettings.includeSettings(this.metFragSettingsFile, true, this.getExcludeKeys());
		
		if(this.database.equals("LipidMaps") && this.bondEnergyLipidMapsFilePath != null && this.bondEnergyLipidMapsFilePath.length() != 0) {
			System.out.println("bond energy file path set to " + this.bondEnergyLipidMapsFilePath);
			this.metFragSettings.set(VariableNames.BOND_ENERGY_FILE_PATH_NAME, this.bondEnergyLipidMapsFilePath);
		}
	}

	public java.util.Vector<String> getExcludeKeys() {
		java.util.Vector<String> excludeKeys = new java.util.Vector<String>();
		excludeKeys.add(VariableNames.FEEDBACK_EMAIL_HOST);
		excludeKeys.add(VariableNames.FEEDBACK_EMAIL_PASS);
		excludeKeys.add(VariableNames.FEEDBACK_EMAIL_USER);
		excludeKeys.add(VariableNames.FEEDBACK_EMAIL_PORT);
		excludeKeys.add(VariableNames.FEEDBACK_EMAIL_TO);
		excludeKeys.add(VariableNames.MONA_PROXY_PORT);
		excludeKeys.add(VariableNames.MONA_PROXY_SERVER);
		
		excludeKeys.add(VariableNames.MONA_PROXY_SERVER);
		excludeKeys.add(VariableNames.MONA_PROXY_PORT);
		excludeKeys.add(VariableNames.KEGG_PROXY_SERVER);
		excludeKeys.add(VariableNames.KEGG_PROXY_PORT);
		excludeKeys.add(VariableNames.METACYC_PROXY_SERVER);
		excludeKeys.add(VariableNames.METACYC_PROXY_PORT);
		
		if(!this.database.equals("ChemSpider")) excludeKeys.add(VariableNames.CHEMSPIDER_TOKEN_NAME);
		//local database
		excludeKeys.add(VariableNames.LOCAL_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_DATABASE_PASSWORD_NAME);
		//local pubchem database
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_NAME_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_PUBCHEM_DATABASE_PASSWORD_NAME);
		//local kegg database
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_PASSWORD_NAME);
		excludeKeys.add(VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME);
		//local lipidmaps database
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_PASSWORD_NAME);
		excludeKeys.add(VariableNames.LOCAL_LIPIDMAPS_DATABASE_COMPOUND_NAME_COLUMN_NAME);
		//local lipidmaps database
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PASSWORD_NAME);
		excludeKeys.add(VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME);
		//local chebi database
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_COMPOUND_TABLE_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_PORT_NUMBER_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_SERVER_IP_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_MASS_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_FORMULA_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_INCHI_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_INCHIKEY1_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_INCHIKEY2_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_CID_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_SMILES_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_COMPOUND_NAME_COLUMN_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_USER_NAME);
		excludeKeys.add(VariableNames.LOCAL_CHEBI_DATABASE_PASSWORD_NAME);
		
		return excludeKeys;
	}
	
	public void renewMetFragProcessSettings(MetFragGlobalSettings settings) {
		this.combinedMetFragProcess.renewSettings(settings);
	}
	
	/*
	 * processing 
	 */
	public void startCandidateProcessing() throws Exception {
		if(this.retrievedCandidateList == null || this.retrievedCandidateList.getNumberElements() == 0) return;
		this.combinedMetFragProcess.setCandidateList(this.retrievedCandidateList.clone());
		this.combinedMetFragProcess.run();
	}
	
	public boolean isCandidateProcessingFinished() {
		return this.candidateProcessingFinished;
	}

	public void setCandidateProcessingFinished(boolean candidateProcessingFinished) {
		this.candidateProcessingFinished = candidateProcessingFinished;
	}
	
	public void terminateMetFragProcess() {
		if(this.combinedMetFragProcess != null) this.combinedMetFragProcess.terminate();
	}

	public String getProcessCompoundsDialogHeader() {
		if(this.processCompoundsDialogHeader.length() != 0) 
			return this.processCompoundsDialogHeader;
		if(this.metFragSettings != null) {
			if(this.metFragSettings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME)) {
				ProcessingStatus processingStatus = (ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
				if(processingStatus != null) {
					return ((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getProcessStatusString();
				}
			}
		}
		return "Processing";
	}

	public void setProcessCompoundsDialogHeader(String processCompoundsDialogHeader) {
		this.processCompoundsDialogHeader = processCompoundsDialogHeader;
	}
	
	/*
	 * results
	 */
	public List<Weight> getWeights() {
		return this.weights;
	}

	public void setWeights(List<Weight> weights) {
		this.weights = weights;
	}

	public CandidateList getCurrentCandidateList() {
		if(this.combinedMetFragProcess == null || this.combinedMetFragProcess.getCandidateList() == null) return new CandidateList();
		return this.combinedMetFragProcess.getCandidateList();
	}
	
	public int getNumberErrorCandidates() {
		return ((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getNumberErrorCandidates().get();
	}
	
	public int getNumberPreFilteredCandidates() {
		return ((ProcessingStatus)this.metFragSettings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).getNumberPreFilteredCandidates().get();
	}
	
	/*
	 * uncategorized
	 */
	public String getNumberCompoundsLabel() {
		return this.numberCompoundsLabel;
	}

	public void setNumberCompoundsLabel(String label) {
		this.numberCompoundsLabel = label;
	}

	public String getScoreName(String scoreName) {
		return this.scoreNamesMap.get(scoreName);
	}
	
	public boolean isFilterValid(String filterName) {
		if(filterName.equals("includedFilterElements")) {
			return this.isElementInclusionFilterValid();
		}
		else {
			return this.filterValidMap.get(filterName);
		}
	}
	
	public void setFilterValid(boolean value, String filterName) {
		if(filterName.equals("includedFilterElements")) {
			this.setElementInclusionFilterValid(value);
		}
		else if(this.filterValidMap.containsKey(filterName)) {
			this.filterValidMap.put(filterName, value);
		}
	}

	public void setFilterEnabled(boolean value, String filterName) {
		if(this.filterEnabledMap.containsKey(filterName)) {
			this.filterEnabledMap.put(filterName, value);
		}
	}
	
	public boolean isScoreValid(String scoreName) {
		return this.scoreValidMap.get(scoreName);
	}
	
	public void setScoreValid(boolean value, String scoreName) {
		if(this.scoreValidMap.containsKey(scoreName))
			this.scoreValidMap.put(scoreName, value);
	}

	public void setScoreEnabled(boolean value, String scoreName) {
		if(this.scoreEnabledMap.containsKey(scoreName))
			this.scoreEnabledMap.put(scoreName, value);
	}
	
	public boolean isScoreEnabled(String scoreName) {
		return this.scoreEnabledMap.get(scoreName);
	}
	
	public boolean isFilterEnabled(String filterName) {
		return this.filterEnabledMap.get(filterName);
	}
	
	public java.util.Iterator<String> getFilterKeys() {
		return this.filterEnabledMap.keySet().iterator();
	}
	
	public java.util.Iterator<String> getScoreKeys() {
		return this.scoreEnabledMap.keySet().iterator();
	}
	
	public String getRandomString(int length) {
		java.util.Random rand = new java.util.Random();
		String source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		String target = "";
		for(int i = 0; i < length; i++) {
			target += source.charAt(rand.nextInt(source.length()));
		}
		return target;
	}
	
	public byte[] readIntoByteArray(java.io.InputStream in) throws java.io.IOException {
	        byte[] buffer = new byte[4096];
	        int bytesRead;
	        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

	        while ((bytesRead = in.read(buffer)) != -1) {
	            out.write(buffer, 0, bytesRead);
	        }
	        out.flush();

	        return out.toByteArray();
	    }
	 
	public void copyFileUsingStream(java.io.File source, java.io.File dest) throws Exception {
        java.io.InputStream is = null;
        java.io.OutputStream os = null;
        try {
            is = new java.io.FileInputStream(source);
            os = new java.io.FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
	 }
	 
	 /*
	  * read settings from settings.properties
	  */
	public MetFragGlobalSettings readDatabaseConfigFromFile() {
		try {
			ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
			this.bondEnergyLipidMapsFilePath = servletContext.getRealPath("/resources/BondEnergiesLipidMaps.txt");
			String pathToProperties = servletContext.getRealPath("/resources/settings.properties");
			MetFragGlobalSettings settings = MetFragGlobalSettings.readSettings(new java.io.File(pathToProperties), null);
			if(settings.containsKey(VariableNames.LOCAL_KEGG_DATABASE_NAME) && settings.get(VariableNames.LOCAL_KEGG_DATABASE_NAME) != null) this.localKeggDatabase = true;
			if(settings.containsKey(VariableNames.LOCAL_PUBCHEM_DATABASE_NAME) && settings.get(VariableNames.LOCAL_PUBCHEM_DATABASE_NAME) != null) this.localPubChemDatabase = true;
			return settings;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error reading settings.properties");
			return null;
		}
	}

	public java.io.File getSettingsLogFile(String path, String name) throws IOException {
		java.io.File file = new java.io.File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + name);
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(file));
		
		bwriter.write("#### DATABASE SETTINGS ####"); bwriter.newLine();
		bwriter.write(VariableNames.METFRAG_DATABASE_TYPE_NAME + " = " + this.database); bwriter.newLine();
		bwriter.write(VariableNames.MOLECULAR_FORMULA_NAME + " = " + this.formula); bwriter.newLine();
		bwriter.write(VariableNames.IDENTIFIER_NAME + " = " + this.identifiers); bwriter.newLine();
		bwriter.write("IncludeReferences" + " = " + this.includeReferences); bwriter.newLine();
		bwriter.write(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME + " = " + this.databaseRelativeMassDeviation); bwriter.newLine();
		bwriter.write(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME + " = " + this.neutralMonoisotopicMass); bwriter.newLine();
		bwriter.write(VariableNames.PRECURSOR_ION_MASS_NAME + " = " + this.measuredMass); bwriter.newLine();
		bwriter.write(VariableNames.PRECURSOR_ION_MODE_NAME + " = " + this.measuredMassMode); bwriter.newLine();
		bwriter.write("PositiveChargeMeasured" + " = " + this.positiveChargeMeasured); bwriter.newLine();
		bwriter.write(VariableNames.LOCAL_DATABASE_PATH_NAME + " = " + this.candidateFilePath); bwriter.newLine();
		bwriter.write("isLocalDatabaseDefined" + " = " + this.isLocalDatabaseDefined); bwriter.newLine();
		bwriter.write("massSearchAvailable" + " = " + this.massSearchAvailable); bwriter.newLine();
		bwriter.write("formulaSearchAvailable" + " = " + this.formulaSearchAvailable); bwriter.newLine();
		bwriter.write("identifierSearchAvailable" + " = " + this.identifierSearchAvailable); bwriter.newLine();
		bwriter.write("candidateProcessingFinished" + " = " + this.candidateProcessingFinished); bwriter.newLine();
		
		java.util.Iterator<String> it = this.filterEnabledMap.keySet().iterator();
		bwriter.write("#### FILTERS ####");
		bwriter.newLine();
		while(it.hasNext()) {
			String key = it.next();
			bwriter.write(key + " = " + this.filterEnabledMap.get(key) + " " + this.filterValidMap.get(key)); bwriter.newLine();
		}
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME + " = " + this.includedElements); bwriter.newLine();
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME + " = " + this.excludedElements); bwriter.newLine();
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME + " = " + this.includedMinimumElements); bwriter.newLine();
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME + " = " + this.includedMaximumElements); bwriter.newLine();
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME + " = " + this.smartsFilterInclusion); bwriter.newLine();
		bwriter.write(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME + " = " + this.smartsFilterExclusion); bwriter.newLine();
		bwriter.write("elementInclusionExclusiveFilterEnabled" + " = " + this.elementInclusionExclusiveFilterEnabled); bwriter.newLine();
		bwriter.write("elementInclusionFilterValid" + " = " + this.elementInclusionExclusiveFilterEnabled); bwriter.newLine();
		
		bwriter.write("#### SCORES ####");
		bwriter.newLine();
		while(it.hasNext()) {
			String key = it.next();
			bwriter.write(key + " = " + this.filterEnabledMap.get(key) + " " + this.filterValidMap.get(key)); bwriter.newLine();
		}
		bwriter.write(VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME + " = " + this.smartsScoreInclusion); bwriter.newLine();
		bwriter.write(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME + " = " + this.smartsScoreExclusion); bwriter.newLine();
		bwriter.write(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME + " = " + this.experimentalRetentionTimeValue); bwriter.newLine();
		bwriter.write(VariableNames.USER_LOG_P_VALUE_NAME + " = " + this.experimentalRetentionTimeValue); bwriter.newLine();

		bwriter.write("#### FRAGMENTATION SETTINGS ####"); bwriter.newLine();
		bwriter.write(VariableNames.RELATIVE_MASS_DEVIATION_NAME + " = " + this.relativeMassDeviation); bwriter.newLine();
		bwriter.write(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME + " = " + this.absoluteMassDeviation); bwriter.newLine();
		bwriter.write(VariableNames.PRECURSOR_ION_MASS_NAME + " = " + this.mode); bwriter.newLine();
		bwriter.write(VariableNames.PEAK_LIST_NAME + " = " + this.peakList); bwriter.newLine();
		bwriter.write(VariableNames.IS_POSITIVE_ION_MODE_NAME + " = " + this.positiveCharge); bwriter.newLine();
		

		bwriter.write("#### GENERAL STUFF ####"); bwriter.newLine();
		bwriter.write("compoundsRetrieved" + " = " + this.compoundsRetrieved); bwriter.newLine();
		
		bwriter.close();
		return file;
	}
	
	
}
