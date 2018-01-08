package de.ipbhalle.metfragweb.controller;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.primefaces.component.organigram.OrganigramHelper;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CloseEvent;
import org.primefaces.event.ItemSelectEvent;
import org.primefaces.event.SlideEndEvent;
import org.primefaces.event.organigram.OrganigramNodeCollapseEvent;
import org.primefaces.event.organigram.OrganigramNodeExpandEvent;
import org.primefaces.event.organigram.OrganigramNodeSelectEvent;
import org.primefaces.model.OrganigramNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.LineChartModel;
import org.primefaces.model.chart.LineChartSeries;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfragweb.compoundCluster.ClusterCompoundsThreadRunner;
import de.ipbhalle.metfragweb.compoundCluster.INode;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.CandidateStatistics;
import de.ipbhalle.metfragweb.datatype.Fragment;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.datatype.AvailableScore;
import de.ipbhalle.metfragweb.datatype.Parameter;
import de.ipbhalle.metfragweb.datatype.ScoreSummary;
import de.ipbhalle.metfragweb.datatype.SuspectListFileContainer;
import de.ipbhalle.metfragweb.datatype.Weight;
import de.ipbhalle.metfragweb.helper.FileStorer;
import de.ipbhalle.metfragweb.helper.ProcessCompoundsThreadRunner;
import de.ipbhalle.metfragweb.helper.RetrieveCompoundsThreadRunner;
import de.ipbhalle.metfragweb.validator.ElementsValidator;
import de.ipbhalle.metfragweb.validator.FormulaValidator;
import de.ipbhalle.metfragweb.validator.PeakListValidator;
import de.ipbhalle.metfragweb.validator.PositiveDoubleValueValidator;
import de.ipbhalle.metfragweb.validator.SmartsExpressionValidator;
import de.ipbhalle.metfragweb.validator.SmartsValidator;

@ManagedBean
@SessionScoped
public class MetFragWebBean {

	private final String version = "v2.0.10";
	/*
	 * combines all the settings
	 */
	protected BeanSettingsContainer beanSettingsContainer;
	
	protected boolean compoundClusteringEnabled = false;
	protected Thread clusterCompoundsThread;
	protected ClusterCompoundsThreadRunner clusterCompoundsThreadRunner;
	private OrganigramNode selectedNode;
	private boolean clusterImageTooltipRendered = false;
	private boolean mergedCandidateResultsByInChIKey1 = true;
//	protected TreeNode[] selectedClusterNodes;
//	protected TreeNode selectedContextMenuClusterNode;
	protected Boolean clusterCompoundsThreadStarted;
	
	protected Messages errorMessages;
	protected Messages infoMessages;

	protected List<Weight> weights;

	/*
	 * some infos
	 */
	protected int numberMatchPeaksOfSelectedMolecule;
	
	/*
	 * processing
	 */
	protected boolean isDatabaseProcessing = false;
	protected boolean isCandidateProcessing = false;
	protected boolean isSpectrumViewActive = false;
	protected boolean isFragmentsViewActive = false;
	protected boolean isScoresViewActive = false;
	
	protected LineChartModel fragmentsModel;
	
	protected boolean validMolecularFormulaDefined = false;
	protected boolean threadExecutionStarted = false;
	protected Thread thread;
	protected ProcessCompoundsThreadRunner processCompoundsThreadRunner;
	protected RetrieveCompoundsThreadRunner retrieveCompoundsThreadRunner;

	/*
	 * statistics
	 */
	protected CandidateStatistics candidateStatistics;
	
	/*
	 * results
	 */
	protected MetFragResultsContainer metFragResultsContainer;
	protected MetFragResultsContainer filteredMetFragResultsContainer;
	protected DefaultPeakList processedPeaklistObject; 
	protected java.util.Vector<Fragment> currentFragments;
	protected ScoreSummary[] currentCandidateScores;
	protected MetFragResult currentScoreCandidate;
	protected Integer[] explainedPeaksFilter;
	
	/*
	 * bean initialisation
	 */
	public MetFragWebBean() {
		System.out.println("MetFragWebBean");
	}

	@PostConstruct
	public void init() {
		this.disableLogger();
		this.generateFolders();
		//set to their defaults
		this.isCandidateProcessing = false;
		this.isSpectrumViewActive = false;
		this.isFragmentsViewActive = false;
		this.isScoresViewActive = false;
		this.validMolecularFormulaDefined = false;
		this.threadExecutionStarted = false;
		this.isFeedbackDialogVisible = false;
		this.dataStorePermission = "no";
		this.candidateStatistics = new CandidateStatistics();
		this.feedbackComment = "";
		this.feedbackEmail = "";
		this.feedbackType = "issue";
		
		this.metFragResultsContainer = new MetFragResultsContainer();
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();
		this.errorMessages = new Messages();
		this.infoMessages = new Messages();
		this.beanSettingsContainer = new BeanSettingsContainer(this.getRootSessionFolder());
		this.fragmentsModel = new LineChartModel();
		this.explainedPeaksFilter = new Integer[0];
		
		this.getParametersLandingBean();
	}
	
	private void getParametersLandingBean()
    {
    	java.util.Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        if(sessionMap.containsKey("landingBean"))
        {
        	System.out.println("updating values");
            MetFragLandingBean landingBean = (MetFragLandingBean) sessionMap.get("landingBean");
            java.util.List<Parameter> params = landingBean.getParameters();
            for(Parameter param : params) {
            	if(param.getKey().equals(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME))
            		this.setAbsoluteMassDeviation(param.getValue());
            	else if(param.getKey().equals(VariableNames.RELATIVE_MASS_DEVIATION_NAME))
            		this.setRelativeMassDeviation(param.getValue());
            	else if(param.getKey().equals(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME))
            		this.setDatabaseRelativeMassDeviation(param.getValue());
            	else if(param.getKey().equals(VariableNames.PRECURSOR_DATABASE_IDS_NAME))
            		this.setIdentifiers(param.getValue());
            	else if(param.getKey().equals(VariableNames.PRECURSOR_ION_MASS_NAME))
            		this.setMeasuredMass(param.getValue());
            	else if(param.getKey().equals(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME))
            		this.setNeutralMonoisotopicMass(param.getValue());
            	else if(param.getKey().equals(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME))
            		this.setFormula(param.getValue());
            	else if(param.getKey().equals(VariableNames.PRECURSOR_ION_MODE_NAME)) {
            		this.setMode(Integer.parseInt(param.getValue()));
            		this.setMeasuredMassMode(Integer.parseInt(param.getValue()));
            	}
            	else if(param.getKey().equals(VariableNames.PEAK_LIST_NAME))
            		this.setPeakList(param.getValue());
            	else if(param.getKey().equals(VariableNames.METFRAG_DATABASE_TYPE_NAME))
            		this.setDatabase(param.getValue());
            }
        }
    }
	
	private void generateFolders() {
		String root = this.getRootSessionFolder();
		java.io.File rootFolder = new java.io.File(root);
		java.io.File[] files = rootFolder.listFiles();
		try {
			//first delete all files in the root folder
			if(files != null) {
				for(int i = 0; i < files.length; i++) {
					if(files[i].isDirectory()) FileUtils.deleteDirectory(files[i]);
					else files[i].delete();
				}
			}
			//generate new folder structure
			System.out.println("generate session folders");
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsscore").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "suspectlistsfilter").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "retentiontimescore").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "candidatefiles").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "downloads").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "uploads").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "images/candidates").mkdirs();
			new java.io.File(root + Constants.OS_SPECIFIC_FILE_SEPARATOR + "images/fragments").mkdirs();
		}
		catch(Exception e) {
			e.printStackTrace();
			System.err.println("error generating session folders");
			return;
		}
	}
	
	//disable talkative axis logger
	protected void disableLogger() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
	}
	
	/*
	 * uncategorized
	 */
	public String getRootSessionFolder() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		String sessionId = session.getId();
		String rootPath = ((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext()).getRealPath("/");
		return rootPath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "files" + Constants.OS_SPECIFIC_FILE_SEPARATOR + sessionId;
	}

	public String getSessionId() {
		FacesContext fCtx = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) fCtx.getExternalContext().getSession(false);
		return session.getId();
	}
	
	/**
	 * 
	 * @param event
	 */
	public void parametersUploadListener(org.primefaces.event.FileUploadEvent event) {
		System.out.println("parametersUploadListener");
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("metFragWebBean");
		this.errorMessages.removeKey("updateParametersError");
		this.errorMessages.removeKey("buttonDownloadCompoundsError");
		this.infoMessages.removeKey("updateParametersInfo");
		//do the work
		this.init();
		this.beanSettingsContainer.getUserInputDataHandler().handleParametersZipFile(event.getFile(), this.infoMessages, 
			this.errorMessages, this.beanSettingsContainer);
	}
	
	/*
	 * database search settings
	 */
	public java.util.List<javax.faces.model.SelectItem> getAvailableDatabases() {
		return this.beanSettingsContainer.getAvailableParameters().getDatabases();
	}
	
	public String getMeasuredMass() {
		if (this.beanSettingsContainer.getMeasuredMass() == null)
			return "";
		return this.beanSettingsContainer.getMeasuredMass();
	}

	public void setMeasuredMass(String measuredMass) {
		try {
			if (measuredMass != null && measuredMass.trim().length() != 0) {
				if(PositiveDoubleValueValidator.check(measuredMass)) 
					this.errorMessages.removeKey("inputMeasuredMassError");
				else 
					throw new Exception();
			}
			else this.errorMessages.removeKey("inputMeasuredMassError");
		} catch (Exception e) {
			this.errorMessages.setMessage("inputMeasuredMassError", "Error: Invalid mass value.");
		} finally {
			this.beanSettingsContainer.setMeasuredMass(measuredMass);
		}
	}

	public Integer getMeasuredMassMode() {
		return this.beanSettingsContainer.getMeasuredMassMode();
	}

	public void setMeasuredMassMode(Integer measuredMassMode) {
		this.beanSettingsContainer.setMeasuredMassMode(measuredMassMode);
	}
	
	public void calculateNeutralMonoisotopicMass() {
		if(this.validMolecularFormulaDefined) {
			this.errorMessages.setMessage("inputMeasuredMassError", "Error: Molecular formula is defined.");
			return;
		}
		if(this.beanSettingsContainer.getMeasuredMass() == null || this.beanSettingsContainer.getMeasuredMass().length() == 0) {
			this.errorMessages.setMessage("inputMeasuredMassError", "Error: Mass value required.");
			return;
		}
		else if(!PositiveDoubleValueValidator.check(this.beanSettingsContainer.getMeasuredMass())) {
			this.errorMessages.setMessage("inputMeasuredMassError", "Error: Invalid mass value.");
			return;
		}
		this.errorMessages.removeKey("inputMeasuredMassError");
		this.errorMessages.removeKey("buttonDownloadCompoundsError");
		try {
			int mode = this.beanSettingsContainer.getMeasuredMassMode();
			if(mode == 1000 || mode == -1000) mode = 0;
			double value = Double.parseDouble(this.beanSettingsContainer.getMeasuredMass()) - Constants.ADDUCT_MASSES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(mode)) 
					- Constants.POSITIVE_IONISATION_MASS_DIFFERENCE.get(Constants.POSITIVE_IONISATION.indexOf(this.beanSettingsContainer.isPositiveChargeMeasured()));
			if(value < 0.0) {
				this.errorMessages.setMessage("inputMeasuredMassError", "Error: Input results in a negative neutral mass.");
				return;
			}
			this.beanSettingsContainer.setNeutralMonoisotopicMass(String.valueOf(MathTools.round(value)));
		} catch(Exception e) {
			e.printStackTrace();
			this.errorMessages.setMessage("inputMeasuredMassError", "Error: Error calculating mass value.");
			return;
		}
	}
	
	public boolean isCompoundsRetrieved() {
		return this.beanSettingsContainer.isCompoundsRetrieved();
	}

	public void setCompoundsRetrieved(boolean compoundsRetrieved) {
		this.beanSettingsContainer.setCompoundsRetrieved(compoundsRetrieved);
	}
	
	public String getDatabase() {
		return this.beanSettingsContainer.getDatabase();
	}

	public void setDatabase(String database) {
		if(this.beanSettingsContainer.getDatabase().equals(database) && this.beanSettingsContainer.isDatabaseInitialise()) return;
		this.metFragResultsContainer = new MetFragResultsContainer();
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();
		this.errorMessages = new Messages();
		this.infoMessages = new Messages();
		this.processedPeaklistObject = null;
		this.beanSettingsContainer.setDatabase(database);
		this.candidateStatistics.setShowPointLabels(false);
		this.candidateStatistics.setSelectedCandidate(0);
		RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(1)");
	}
	
	public boolean isValidMolecularFormulaDefined() {
		return this.validMolecularFormulaDefined;
	}
	
	public String getFormula() {
		return this.beanSettingsContainer.getFormula();
	}

	public void setFormula(String formula) {
		try {
			if (formula != null && formula.trim().length() != 0) {
				if(FormulaValidator.check(formula)) {
					this.validMolecularFormulaDefined = true;
					this.errorMessages.removeKey("formulaError");
					this.beanSettingsContainer.setNeutralMonoisotopicMass(String.valueOf(new ByteMolecularFormula(formula).getMonoisotopicMass()));
				}
				else {
					throw new Exception();
				}
			}
			else {
				this.errorMessages.removeKey("formulaError");
				this.validMolecularFormulaDefined = false;
			}
		} catch(Exception e) {
			this.errorMessages.setMessage("formulaError", "Error: Invalid formula value.");
			this.validMolecularFormulaDefined = false;
		}
		finally {
			this.beanSettingsContainer.setFormula(formula);
		}
	}
	
	public String getDatabaseRelativeMassDeviation() {
		if (this.beanSettingsContainer.getDatabaseRelativeMassDeviation() == null)
			return "";
		return this.beanSettingsContainer.getDatabaseRelativeMassDeviation();
	}

	public void setDatabaseRelativeMassDeviation(String databaseRelativeMassDeviation) {
		try {
			if (databaseRelativeMassDeviation != null && databaseRelativeMassDeviation.trim().length() != 0) {
				if(PositiveDoubleValueValidator.check(databaseRelativeMassDeviation)) 
					this.errorMessages.removeKey("inputDatabaseRelativeMassDeviationError");
				else 
					throw new Exception();
			}
			else this.errorMessages.removeKey("inputDatabaseRelativeMassDeviationError");
		} catch (Exception e) {
			this.errorMessages.setMessage("inputDatabaseRelativeMassDeviationError", "Error: Invalid search ppm value.");
		} finally {
			this.beanSettingsContainer.setDatabaseRelativeMassDeviation(databaseRelativeMassDeviation);
		}
	}
	
	public String getNeutralMonoisotopicMass() {
		if (this.beanSettingsContainer.getNeutralMonoisotopicMass() == null)
			return "";
		return this.beanSettingsContainer.getNeutralMonoisotopicMass();
	}

	public void setNeutralMonoisotopicMass(String neutralMonoisotopicMass) {
		try {
			if (neutralMonoisotopicMass != null && neutralMonoisotopicMass.trim().length() != 0) {
				if(PositiveDoubleValueValidator.check(neutralMonoisotopicMass)) 
					this.errorMessages.removeKey("inputNeutralMonoisotopicMassError");
				else 
					throw new Exception();
			}
			else this.errorMessages.removeKey("inputNeutralMonoisotopicMassError");
		} catch (Exception e) {
			this.errorMessages.setMessage("inputNeutralMonoisotopicMassError", "Error: Invalid mass value.");
		} finally {
			this.beanSettingsContainer.setNeutralMonoisotopicMass(neutralMonoisotopicMass);
		}
	}
	
	public String getIdentifiers() {
		return this.beanSettingsContainer.getIdentifiers();
	}

	public void setIdentifiers(String identifiers) {
		try {
			if(identifiers != null && identifiers.trim().length() != 0) {
				identifiers = identifiers.trim();
				if(identifiers.startsWith(",") || identifiers.matches(",,") || identifiers.endsWith(",") || !identifiers.replaceAll(",", "").replaceAll(":", "").matches("\\w*"))
					throw new Exception();
				else
					this.errorMessages.removeKey("identifierError");
			}	
			else this.errorMessages.removeKey("identifierError");
		}
		catch(Exception e) {
			this.errorMessages.setMessage("identifierError", "Error: Invalid identifiers value.");
		}
		finally {
			this.beanSettingsContainer.setIdentifiers(identifiers);
		}
	}
		
	public void candidateFileUploadListener(org.primefaces.event.FileUploadEvent event) {
		this.errorMessages.removeKey("candidateFileError");
		//do the work
		this.beanSettingsContainer.getUserInputDataHandler().handleLocalCandidateFile(event.getFile(), this.infoMessages, this.errorMessages, 
				this.beanSettingsContainer);
    }
	
	public String getCandidateFileName() {
		return this.beanSettingsContainer.getCandidateFileName();
	}

	public String getCandidateFileNamePart() {
		if (this.beanSettingsContainer.getCandidateFileName() == null || this.beanSettingsContainer.getCandidateFileName().length() == 0)
			return "";
		String value = this.beanSettingsContainer.getCandidateFileName().substring(0, Math.min(40, this.beanSettingsContainer.getCandidateFileName().length()));
		if (value.length() < this.beanSettingsContainer.getCandidateFileName().length())
			value += "...";
		return value;
	}
	
	public boolean isIncludeReferences() {
		return this.beanSettingsContainer.isIncludeReferences();
	}

	public void setIncludeReferences(boolean includeReferences) {
		this.beanSettingsContainer.setIncludeReferences(includeReferences);
	}
	
	public boolean isCandidateFilePathDefined() {
		return !this.beanSettingsContainer.getCandidateFilePath().equals("");
	}
	
	public boolean isLocalDatabaseDefined() {
		return this.beanSettingsContainer.isLocalDatabaseDefined();
	}

	public boolean isMassSearchAvailable() {
		return this.beanSettingsContainer.isMassSearchAvailable();
	}

	public boolean isIdentifierSearchAvailable() {
		return this.beanSettingsContainer.isIdentifierSearchAvailable();
	}

	public boolean isFormulaSearchAvailable() {
		return this.beanSettingsContainer.isFormulaSearchAvailable();
	}
	
	private boolean checkDatabaseSettings() {
		boolean checksFine = true;
		boolean formulaIsGiven = false;
		boolean identifierIsGiven = false;
		boolean massIsGiven = false;
		boolean formulaCheckIsFine = true;
		boolean databaseRelativeMassDeviation = false;
		// check mass
		if (this.beanSettingsContainer.getNeutralMonoisotopicMass() != null && this.beanSettingsContainer.getNeutralMonoisotopicMass().length() != 0) {
			massIsGiven = true;
			this.errorMessages.removeKey("buttonDownloadCompoundsError");
			try {
				double value = Double.parseDouble(this.beanSettingsContainer.getNeutralMonoisotopicMass());
				if (value <= 0.0 || value > 1250)
					throw new Exception();
				else
					this.errorMessages.removeKey("inputNeutralMonoisotopicMassError");
			} catch (Exception e) {
				checksFine = false;
				this.errorMessages.setMessage("inputNeutralMonoisotopicMassError", "Error: Invalid mass value.");
			}
		} else
			this.errorMessages.removeKey("inputNeutralMonoisotopicMassError");
		if (this.beanSettingsContainer.getIdentifiers() != null && this.beanSettingsContainer.getIdentifiers().trim().length() != 0) {
			identifierIsGiven = true;
			if (this.beanSettingsContainer.getIdentifiers().startsWith(",") || this.beanSettingsContainer.getIdentifiers().endsWith(",") || !this.beanSettingsContainer.getIdentifiers().replaceAll(",", "").replaceAll(":", "").matches("[A-Z0-9]*")) {
				checksFine = false;
				this.errorMessages.setMessage("inputIdentifiersError", "Error: Invalid identifiers value. Example: 932,2730,2519");
			} else
				this.errorMessages.removeKey("inputIdentifiersError");
		} else
			this.errorMessages.removeKey("inputIdentifiersError");
		if (this.beanSettingsContainer.getFormula() != null && this.beanSettingsContainer.getFormula().trim().length() != 0) {
			formulaIsGiven = true;
			if (!FormulaValidator.check(this.beanSettingsContainer.getFormula())) {
				checksFine = false;
				this.errorMessages.setMessage("inputFormulaError", "Error: Invalid molecular formula value.");
			} else {
				formulaCheckIsFine = true;
				this.errorMessages.removeKey("inputFormulaError");
			}
		} else
			this.errorMessages.removeKey("inputFormulaError");
		if (this.beanSettingsContainer.getDatabaseRelativeMassDeviation() != null && this.beanSettingsContainer.getDatabaseRelativeMassDeviation().length() != 0) {
			databaseRelativeMassDeviation = true;
			try {
				if (Double.parseDouble(this.beanSettingsContainer.getDatabaseRelativeMassDeviation()) < 0.0)
					throw new Exception();
				else
					this.errorMessages.removeKey("inputDatabaseRelativeMassDeviationError");
			} catch (Exception e) {
				checksFine = false;
				this.errorMessages.setMessage("inputDatabaseRelativeMassDeviationError", "Error: Invalid search ppm value.");
			}
		}
		// check that at least mass or formula is given
		if (!formulaIsGiven && !massIsGiven) {
			checksFine = false;
			this.errorMessages.setMessage("inputNeutralMonoisotopicMassError", "Error: Neutral mass or molecular formula required.");
			this.errorMessages.setMessage("inputFormulaError", "Error: Neutral mass or molecular formula required.");
		}
		// check that if only mass with online database is given then the
		// seearch ppm has to be provided
		if (!formulaIsGiven && !identifierIsGiven && !databaseRelativeMassDeviation && massIsGiven && !this.beanSettingsContainer.isLocalDatabaseDefined()) {
			checksFine = false;
			this.errorMessages.setMessage("inputDatabaseRelativeMassDeviationError", "Error: Search ppm required.");
		}
		// if formula but no mass is given then calculate mass
		if (formulaIsGiven && formulaCheckIsFine) {
			try {
				this.beanSettingsContainer.setNeutralMonoisotopicMass(String.valueOf(new ByteMolecularFormula(this.beanSettingsContainer.getFormula()).getMonoisotopicMass()));
			} catch (AtomTypeNotKnownFromInputListException e) {
				checksFine = false;
				this.errorMessages.setMessage("inputFormulaError", "Invalid molecular formula value.");
			}
		}
		return checksFine;
	}
	
	public int getNumberCompoundsRetrieved() {
		if (this.beanSettingsContainer.getRetrievedCandidateList() == null)
			return 0;
		return this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements();
	}
	
	public String getRetrieveCompoundsButtonLabel() {
		return this.beanSettingsContainer.getRetrieveCompoundsButtonLabel();
	}

	public void setRetrieveCompoundsButtonLabel(String retrieveCompoundsButtonLabel) {
		this.beanSettingsContainer.setRetrieveCompoundsButtonLabel(retrieveCompoundsButtonLabel);
	}
	
	public String getNumberCompoundsLabel() {
		return this.beanSettingsContainer.getNumberCompoundsLabel();
	}

	public boolean isDatabaseProcessing() {
		return this.isDatabaseProcessing;
	}
	
	public void retrieveCompounds(ActionEvent event) {
		RequestContext.getCurrentInstance().execute("PF('retrieveCandidatesProgressDialog').show();");
		
		this.isDatabaseProcessing = true;
		/*
		 * stop compounds cluster thread
		 */
		if(this.clusterCompoundsThread != null)
			try {
				this.clusterCompoundsThread.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		/*
		 * check database settings before retrieving compounds
		 */
		if (!this.checkDatabaseSettings()) {
			RequestContext.getCurrentInstance().execute("PF('retrieveCandidatesProgressDialog').hide();");
			this.isDatabaseProcessing = false;
			System.out.println(this.errorMessages.getMessage("inputFormulaError"));
			return;
		}
		RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(1)");
		RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(5)");
		this.beanSettingsContainer.setCompoundsRetrieved(false);
		this.metFragResultsContainer = new MetFragResultsContainer();
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();
		this.infoMessages.removeKey("processingProcessedCandidatesInfo");
		/*
		 * starting database thread
		 */
		this.threadExecutionStarted = true;
		this.retrieveCompoundsThreadRunner = new RetrieveCompoundsThreadRunner(this.beanSettingsContainer, 
			this.infoMessages, this.errorMessages);
		this.thread = new Thread(this.retrieveCompoundsThreadRunner);
		/*
		  * start the metfrag processing
		  */
		if(this.thread != null) this.thread.start();
		
	}	
	
	public String getRetrieveCandidatesDialogHeader() {
		return this.beanSettingsContainer.getRetrieveCompoundsDialogHeader();
	}
	
	public void checkDatabaseThread() {
		if(!this.isDatabaseProcessing) return;
		if(this.thread == null) return;
		if(!this.threadExecutionStarted) return;
		if(!this.thread.isAlive() && this.retrieveCompoundsThreadRunner != null && !this.retrieveCompoundsThreadRunner.isInterrupted()) {
			try {
				this.isDatabaseProcessing = false;
				System.out.println("checkDatabaseThread thread is dead");
				this.threadExecutionStarted = false;
				
				if (this.beanSettingsContainer.getRetrievedCandidateList() != null) {
					if(this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements() != 1) 
						FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Candidate retrieval finished", "Got " + this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements() + " candidates"));
					else 
						FacesContext.getCurrentInstance().addMessage("databaseGrowl", new FacesMessage("Candidate retrieval finished", "Got " + this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements() + " candidate"));
					System.out.println(this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements() + " compound(s)");
					if(this.beanSettingsContainer.getRetrievedCandidateList().getNumberElements() == 0) 
						RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(1)");
				}
				else {
					this.beanSettingsContainer.setNumberCompoundsLabel("");
					this.errorMessages.setMessage("retrieveCompoundsError", "Error fetching candidates.");
					FacesContext.getCurrentInstance().addMessage("databaseGrowl", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database error",  "Failed to retrieve candidates"));
					System.out.println("0 candidates (Error)");
					RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
					return;
				}
				RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
			}
			catch(Exception e) {
				//error occured
				this.isDatabaseProcessing = false;
				this.beanSettingsContainer.setNumberCompoundsLabel("");
				this.errorMessages.setMessage("retrieveCompoundsError", "Error fetching candidates.");
				FacesContext.getCurrentInstance().addMessage("databaseGrowl", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Database error",  "Failed to retrieve candidates"));
				System.out.println("0 candidates (Error)");
				RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
				return;
			}
		}
	}
		
	//candidate download
	public void downloadCandidatesListenerCSV(ActionEvent actionEvent) {
		this.createCandidatesToDownload("csv");
	}

	public void downloadCandidatesListenerSDF(ActionEvent actionEvent) {
		this.createCandidatesToDownload("sdf");
	}

	public void downloadCandidatesListenerXLS(ActionEvent actionEvent) {
		this.createCandidatesToDownload("xls");
	}
	
	public org.primefaces.model.StreamedContent createCandidatesToDownload(String format) {
		return this.beanSettingsContainer.getUserOutputDataHandler().createCandidatesToDownload(format, this.errorMessages);
	}
	
	/*
	 * filter
	 */
	//unconnected compounds filter (should be on all the time)
	public boolean isUnconnetcedStructureExclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("unconnectedStructureExclusionFilter");
	}

	public void setUnconnetcedStructureExclusionFilterEnabled(boolean unconnetcedStructureExclusionFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(unconnetcedStructureExclusionFilterEnabled, "unconnectedStructureExclusionFilter");
	}
	
	//minimum elements filter
	public String getIncludedMinimumElements() {
		return this.beanSettingsContainer.getIncludedMinimumElements();
	}

	public void setIncludedMinimumElements(String includedMinimumElements) {
		try {
			if (includedMinimumElements == null || includedMinimumElements.trim().length() == 0) {
				this.errorMessages.setMessage("includedFilterMinimumElementsError", "Error: Sub-formula value required.");
			} else {
				if (FormulaValidator.check(includedMinimumElements))
					this.errorMessages.removeKey("includedFilterMinimumElementsError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("includedFilterMinimumElementsError", "Error: Invalid sub-formula value.");
		} finally {
			this.beanSettingsContainer.setIncludedMinimumElements(includedMinimumElements);
		}
	}

	public boolean isElementMinimumElementsFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("includedFilterMinimumElements");
	}

	public void setElementMinimumElementsFilterEnabled(boolean elementMinimumElementsFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(elementMinimumElementsFilterEnabled, "includedFilterMinimumElements");
	}
	
	//maximum elements filter
	public String getIncludedMaximumElements() {
		return this.beanSettingsContainer.getIncludedMaximumElements();
	}

	public void setIncludedMaximumElements(String includedMaximumElements) {
		try {
			if (includedMaximumElements == null || includedMaximumElements.trim().length() == 0) {
				this.errorMessages.setMessage("includedFilterMaximumElementsError", "Error: Sub-formula value required.");
			} else {
				if (FormulaValidator.check(includedMaximumElements))
					this.errorMessages.removeKey("includedFilterMaximumElementsError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("includedFilterMaximumElementsError", "Error: Invalid sub-formula value.");
		} finally {
			this.beanSettingsContainer.setIncludedMaximumElements(includedMaximumElements);
		}
	}

	public boolean isElementMaximumElementsFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("includedFilterMaximumElements");
	}

	public void setElementMaximumElementsFilterEnabled(boolean elementMaximumElementsFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(elementMaximumElementsFilterEnabled, "includedFilterMaximumElements");
	}
	
	//element inclusion filter
	public String getIncludedFilterElements() {
		return this.beanSettingsContainer.getIncludedFilterElements();
	}

	public void setIncludedFilterElements(String includedFilterElements) {
		try {
			if (includedFilterElements == null || includedFilterElements.trim().length() == 0) {
				this.errorMessages.setMessage("includedFilterElementsError", "Error: Elements value required.");
			} else {
				if (ElementsValidator.check(includedFilterElements))
					this.errorMessages.removeKey("includedFilterElementsError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("includedFilterElementsError", "Error: Invalid elements value.");
		} finally {
			this.beanSettingsContainer.setIncludedFilterElements(includedFilterElements);
		}
	}
	
    public String getElementInclusionFilterType() {
        return this.beanSettingsContainer.getElementInclusionFilterType();
    }
 
    public void setElementInclusionFilterType(String elementInclusionFilterType) {
        this.beanSettingsContainer.setElementInclusionFilterType(elementInclusionFilterType);
    }
	
	public boolean isElementInclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("includedFilterElements");
	}

	public void setElementInclusionFilterEnabled(boolean elementInclusionFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(elementInclusionFilterEnabled, "includedFilterElements");
	}
		
	//element exclusion filter
	public boolean isElementExclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("excludedFilterElements");
	}

	public void setElementExclusionFilterEnabled(boolean elementExclusionFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(elementExclusionFilterEnabled, "excludedFilterElements");
	}
	
	public String getExcludedElements() {
		return this.beanSettingsContainer.getExcludedFilterElements();
	}

	public void setExcludedElements(String excludedFilterElements) {
		try {
			if (excludedFilterElements == null || excludedFilterElements.trim().length() == 0) {
				this.errorMessages.setMessage("excludedFilterElementsError", "Error: Elements value required.");
			} else {
				if (ElementsValidator.check(excludedFilterElements))
					this.errorMessages.removeKey("excludedFilterElementsError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("excludedFilterElementsError", "Error: Invalid elements value.");
		} finally {
			this.beanSettingsContainer.setExcludedFilterElements(excludedFilterElements);
		}
	}
	
	//SMARTS inclusion
	public java.util.List<javax.faces.model.SelectItem> getAvailableSubstructureSmarts() {
		return this.beanSettingsContainer.getAvailableParameters().getSubstructureSmarts();
	}
	
	public boolean isSmartsInclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("includedFilterSmarts");
	}
	
	public String getSmartsFilterInclusionLabel() {
		return this.beanSettingsContainer.getSmartsFilterInclusion().length == 1 ? this.beanSettingsContainer.getSmartsFilterInclusion().length + " Substructure selected" : this.beanSettingsContainer.getSmartsFilterInclusion().length + " Substructures selected";
	}
	
	public void setSmartsInclusionFilterEnabled(boolean smartsInclusionFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(smartsInclusionFilterEnabled, "includedFilterSmarts");
	}

	public String[] getSmartsFilterInclusion() {
		return this.beanSettingsContainer.getSmartsFilterInclusion();
	}

	public void setAdditionalSmartsFilterInclusion(String additionalSmartsFilterInclusion) {
		try {
			if ((additionalSmartsFilterInclusion == null || additionalSmartsFilterInclusion.trim().length() == 0) 
					&& (this.beanSettingsContainer.getSmartsFilterInclusion() == null || this.beanSettingsContainer.getSmartsFilterInclusion().length == 0)) {
				System.out.println("setAdditionalSmartsFilterInclusion Error: No substructure set.");
				this.errorMessages.setMessage("includedFilterSmartsError", "Error: No substructure set.");
			} else if(additionalSmartsFilterInclusion != null && additionalSmartsFilterInclusion.trim().length() != 0) {
				if (SmartsValidator.check(additionalSmartsFilterInclusion))
					this.errorMessages.removeKey("includedFilterSmartsError");
				else {
					throw new Exception();
				}
			} else {
				this.errorMessages.removeKey("includedFilterSmartsError");
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("includedFilterSmartsError", "Error: Invalid SMARTS value.");
		} finally {
			this.beanSettingsContainer.setAdditionalSmartsFilterInclusion(additionalSmartsFilterInclusion);
		}
	}
	
	public String getAdditionalSmartsFilterInclusion() {
		return this.beanSettingsContainer.getAdditionalSmartsFilterInclusion();
	}
	
	public void setSmartsFilterInclusion(String[] smartsFilterInclusion) {
		if((smartsFilterInclusion == null || smartsFilterInclusion.length == 0) 
				&& (this.beanSettingsContainer.getAdditionalSmartsFilterInclusion() == null || this.beanSettingsContainer.getAdditionalSmartsFilterInclusion().trim().length() == 0))
			this.errorMessages.setMessage("includedFilterSmartsError", "Error: No substructure set.");
		else if(this.beanSettingsContainer.getAdditionalSmartsFilterInclusion() != null && this.beanSettingsContainer.getAdditionalSmartsFilterInclusion().trim().length() != 0) {
			if (SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsFilterInclusion())) {
				this.errorMessages.removeKey("includedFilterSmartsError");
			}
			else
				this.errorMessages.setMessage("includedFilterSmartsError", "Error: Invalid SMARTS value.");
		}
		else {
			this.errorMessages.removeKey("includedFilterSmartsError");
		}
		this.beanSettingsContainer.setSmartsFilterInclusion(smartsFilterInclusion);
	}
	
	//SMARTS exclusion
	public String getSmartsFilterExclusionLabel() {
		return this.beanSettingsContainer.getSmartsFilterExclusion().length == 1 ? this.beanSettingsContainer.getSmartsFilterExclusion().length + " Substructure selected" : this.beanSettingsContainer.getSmartsFilterExclusion().length + " Substructures selected";
	}

	public boolean isSmartsExclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("excludedFilterSmarts");
	}

	public void setSmartsExclusionFilterEnabled(boolean smartsExclusionFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(smartsExclusionFilterEnabled, "excludedFilterSmarts");
	}

	public void setAdditionalSmartsFilterExclusion(String additionalSmartsFilterExclusion) {
		try {
			if ((additionalSmartsFilterExclusion == null || additionalSmartsFilterExclusion.trim().length() == 0) 
					&& (this.beanSettingsContainer.getSmartsFilterExclusion() == null || this.beanSettingsContainer.getSmartsFilterExclusion().length == 0)) {
				this.errorMessages.setMessage("excludedFilterSmartsError", "Error: No substructure set.");
			} else if(additionalSmartsFilterExclusion != null && additionalSmartsFilterExclusion.trim().length() != 0) {
				if (SmartsValidator.check(additionalSmartsFilterExclusion))
					this.errorMessages.removeKey("excludedFilterSmartsError");
				else {
					throw new Exception();
				}
			} else {
				this.errorMessages.removeKey("excludedFilterSmartsError");
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("excludedFilterSmartsError", "Error: Invalid SMARTS value.");
		} finally {
			this.beanSettingsContainer.setAdditionalSmartsFilterExclusion(additionalSmartsFilterExclusion);
		}
	}
	
	public String getAdditionalSmartsFilterExclusion() {
		return this.beanSettingsContainer.getAdditionalSmartsFilterExclusion();
	}

	public String[] getSmartsFilterExclusion() {
		return this.beanSettingsContainer.getSmartsFilterExclusion();
	}

	public void setSmartsFilterExclusion(String[] smartsFilterExclusion) {
		if((smartsFilterExclusion == null || smartsFilterExclusion.length == 0) 
				&& (this.beanSettingsContainer.getAdditionalSmartsFilterExclusion() == null || this.beanSettingsContainer.getAdditionalSmartsFilterExclusion().trim().length() == 0))
			this.errorMessages.setMessage("excludedFilterSmartsError", "Error: No substructure set.");
		else if(this.beanSettingsContainer.getAdditionalSmartsFilterExclusion() != null && this.beanSettingsContainer.getAdditionalSmartsFilterExclusion().trim().length() != 0) {
			if (SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsFilterExclusion())) {
				this.errorMessages.removeKey("excludedFilterSmartsError");
			}
			else
				this.errorMessages.setMessage("excludedFilterSmartsError", "Error: Invalid SMARTS value.");
		}
		else {
			this.errorMessages.removeKey("excludedFilterSmartsError");
		}
		this.beanSettingsContainer.setSmartsFilterExclusion(smartsFilterExclusion);
	}
	
	//substructure information
	public boolean isSubstructureInformationFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("substructureInformationFilterExpression");
	}

	public void setSubstructureInformationFilterEnabled(boolean substructureInformationFilterEnabled) {
		this.beanSettingsContainer.setFilterEnabled(substructureInformationFilterEnabled, "substructureInformationFilterExpression");
	}

	public String getSubstructureInformationFilterExpression() {
		return this.beanSettingsContainer.getSubstructureInformationFilterExpression();
	}

	public void setSubstructureInformationFilterExpression(String substructureInformationFilterExpression) {
		try {
			if (substructureInformationFilterExpression == null || substructureInformationFilterExpression.trim().length() == 0) {
			//	this.errorMessages.setMessage("substructureInformationFilterExpressionError", "Error: No substructure expression set.");
				this.errorMessages.removeKey("substructureInformationFilterExpressionError");
			} else {
				if (SmartsExpressionValidator.check(substructureInformationFilterExpression))
					this.errorMessages.removeKey("substructureInformationFilterExpressionError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("substructureInformationFilterExpressionError", "Error: Invalid value.");
		} finally {
			this.beanSettingsContainer.setSubstructureInformationFilterExpression(substructureInformationFilterExpression);
		}
	}

	public String getSelectedInformationSmarts() {
		return this.beanSettingsContainer.getSelectedInformationSmarts();
	}
	
	public void setSelectedInformationSmarts(String selectedSmarts) {
		this.errorMessages.removeKey("selectedInformationSmartsError");
		this.beanSettingsContainer.setSelectedInformationSmarts(selectedSmarts);
	}
	
	public java.util.List<javax.faces.model.SelectItem> getAvailableSubstructureInformationSmarts() {
		return this.beanSettingsContainer.getAvailableParameters().getSubstructureInformationSmarts();
	}
	
	public void andSelectedInformationSmarts(ActionEvent action) {
		if(this.beanSettingsContainer.getSelectedInformationSmarts() == null || this.beanSettingsContainer.getSelectedInformationSmarts().length() == 0) {
			return;
		}
		String currentExpression = this.beanSettingsContainer.getSubstructureInformationFilterExpression().trim();
		if(currentExpression.length() != 0) this.beanSettingsContainer.setSubstructureInformationFilterExpression(currentExpression + " and " + this.beanSettingsContainer.getSelectedInformationSmarts());
		else this.beanSettingsContainer.setSubstructureInformationFilterExpression(this.beanSettingsContainer.getSelectedInformationSmarts());
	}

	public void orSelectedInformationSmarts(ActionEvent action) {
		if(this.beanSettingsContainer.getSelectedInformationSmarts() == null || this.beanSettingsContainer.getSelectedInformationSmarts().length() == 0) {
			return;
		}
		String currentExpression = this.beanSettingsContainer.getSubstructureInformationFilterExpression().trim();
		if(currentExpression.length() != 0) this.beanSettingsContainer.setSubstructureInformationFilterExpression(currentExpression + " or " + this.beanSettingsContainer.getSelectedInformationSmarts());
		else this.beanSettingsContainer.setSubstructureInformationFilterExpression(this.beanSettingsContainer.getSelectedInformationSmarts());
	}
	
	public void notSelectedInformationSmarts(ActionEvent action) {
		if(this.beanSettingsContainer.getSelectedInformationSmarts() == null || this.beanSettingsContainer.getSelectedInformationSmarts().length() == 0) {
			return;
		}
		String currentExpression = this.beanSettingsContainer.getSubstructureInformationFilterExpression().trim();
		if(currentExpression.length() != 0) this.beanSettingsContainer.setSubstructureInformationFilterExpression(currentExpression + " and not " + this.beanSettingsContainer.getSelectedInformationSmarts());
		else this.beanSettingsContainer.setSubstructureInformationFilterExpression("not " + this.beanSettingsContainer.getSelectedInformationSmarts());
	}
	
	
	
	//suspect list filter
	public SuspectListFileContainer getSuspectListFilterFileContainer() {
		return this.beanSettingsContainer.getSuspectListFilterFileContainer();
	}

	public String getGlobalSuspectListFilterInfo() {
		if (beanSettingsContainer.getSuspectListFilterFileContainer() == null)
			return "";
		return "Number uploaded suspect lists: " + this.beanSettingsContainer.getSuspectListFilterFileContainer().getTotelFileNumber();
	}
	
	public void removeFromSuspectListFilterContainer(ActionEvent event) {
		if (this.beanSettingsContainer.getSuspectListFilterFileContainer() != null) {
			int suspectListElementName = (Integer) event.getComponent().getAttributes().get("currentSuspectListFilterElementId");
			this.beanSettingsContainer.getSuspectListFilterFileContainer().removeById(suspectListElementName);
			if (this.beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0 
					&& !this.isForIdentSuspectListInclusionFilterEnabled()
					&& !this.isDsstoxSuspectListInclusionFilterEnabled()) {
				this.errorMessages.setMessage("suspectListsFilterError", "Suspect list file(s) required.");
				this.beanSettingsContainer.setScoreValid(false, "suspectListsFilter");
			} else
				this.errorMessages.removeKey("suspectListsFilterError");
		}
	}

	public boolean isSuspectListInclusionFilterEnabled() {
		return this.beanSettingsContainer.isFilterEnabled("suspectListsFilter");
	}
	
	public boolean isForIdentSuspectListInclusionFilterEnabled() {
		return this.beanSettingsContainer.isForIdentSuspectListFilterEnabled();
	}

	public boolean isDsstoxSuspectListInclusionFilterEnabled() {
		return this.beanSettingsContainer.isDsstoxSuspectListFilterEnabled();
	}

	public void setForIdentSuspectListInclusionFilterEnabled(boolean value) {
		this.beanSettingsContainer.setForIdentSuspectListFilterEnabled(value);
		if ((this.beanSettingsContainer.getSuspectListFilterFileContainer() == null 
				|| this.beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0) 
				&& !this.isForIdentSuspectListInclusionFilterEnabled()
				&& !this.isDsstoxSuspectListInclusionFilterEnabled()) 
		{
			this.errorMessages.setMessage("suspectListsFilterError", "Suspect list file(s) required.");
			this.beanSettingsContainer.setScoreValid(false, "suspectListsFilter");
		}
		else 
		{
			this.errorMessages.removeKey("suspectListsFilterError");
			this.beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
		}
	}

	public void setDsstoxSuspectListInclusionFilterEnabled(boolean value) {
		this.beanSettingsContainer.setDsstoxSuspectListFilterEnabled(value);
		if ((this.beanSettingsContainer.getSuspectListFilterFileContainer() == null 
				|| this.beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0) 
				&& !this.isForIdentSuspectListInclusionFilterEnabled()
				&& !this.isDsstoxSuspectListInclusionFilterEnabled()) 
		{
			this.errorMessages.setMessage("suspectListsFilterError", "Suspect list file(s) required.");
			this.beanSettingsContainer.setScoreValid(false, "suspectListsFilter");
		}
		else 
		{
			this.errorMessages.removeKey("suspectListsFilterError");
			this.beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
		}
	}
	
	public void setSuspectListInclusionFilterEnabled(boolean suspectListInclusionFilterEnabled) {
		if (suspectListInclusionFilterEnabled == true) {
			if ((this.beanSettingsContainer.getSuspectListFilterFileContainer() == null 
					|| this.beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0)  
					&& !this.isForIdentSuspectListInclusionFilterEnabled()
					&& !this.isDsstoxSuspectListInclusionFilterEnabled())
				this.beanSettingsContainer.setFilterValid(false, "suspectListsFilter");
			else
				this.beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
		}
		this.beanSettingsContainer.setFilterEnabled(suspectListInclusionFilterEnabled, "suspectListsFilter");
	}
	
	public void suspectListFilterUploadListener(org.primefaces.event.FileUploadEvent event) {
		this.errorMessages.removeKey("suspectListsFilterError");
		//do the work
		this.beanSettingsContainer.getUserInputDataHandler().handleSuspectListFilterFile(event.getFile(), this.infoMessages, 
				this.errorMessages, this.beanSettingsContainer);
	}
	
	//checking init
	private void initFilterSettings() {
		// element inclusion filter
		if (this.beanSettingsContainer.isFilterEnabled("includedFilterElements")) {
			if (this.beanSettingsContainer.getIncludedFilterElements() != null && this.beanSettingsContainer.getIncludedFilterElements().trim().length() != 0) {
				if (!ElementsValidator.check(this.beanSettingsContainer.getIncludedFilterElements())) {
					this.beanSettingsContainer.setFilterValid(false, "includedFilterElements");
					this.errorMessages.setMessage("includedFilterElementsError", "Error: Invalid elements value. Example: Cl,C,H,O");
				} else {
					this.beanSettingsContainer.setFilterValid(true, "includedFilterElements");
					this.errorMessages.removeKey("includedFilterElementsError");
				}
			} else {
				this.beanSettingsContainer.setFilterValid(false, "includedFilterElements");
				this.errorMessages.setMessage("includedFilterElementsError", "Error: Elements value required.");
			}
		}
		// element exclusion filter
		if (this.beanSettingsContainer.isFilterEnabled("excludedFilterElements")) {
			if (this.beanSettingsContainer.getExcludedFilterElements() != null && this.beanSettingsContainer.getExcludedFilterElements().trim().length() != 0) {
				if (!ElementsValidator.check(this.beanSettingsContainer.getExcludedFilterElements())) {
					this.beanSettingsContainer.setFilterValid(false, "excludedFilterElements");
					this.errorMessages.setMessage("excludedFilterElementsError", "Error: Invalid elements value. Example: Cl,C,H,O");
				} else {
					this.beanSettingsContainer.setFilterValid(true, "excludedFilterElements");
					this.errorMessages.removeKey("excludedFilterElementsError");
				}
			} else {
				this.beanSettingsContainer.setFilterValid(false, "excludedFilterElements");
				this.errorMessages.setMessage("excludedFilterElementsError", "Error: Elements value required.");
			}
		}
		// maximum elements filter
		if (this.beanSettingsContainer.isFilterEnabled("includedFilterMaximumElements")) {
			if (this.beanSettingsContainer.getIncludedMaximumElements() != null && this.beanSettingsContainer.getIncludedMaximumElements().trim().length() != 0) {
				if(!FormulaValidator.check(this.beanSettingsContainer.getIncludedMaximumElements())) {
					this.beanSettingsContainer.setFilterValid(false, "includedFilterMaximumElements");
					this.errorMessages.setMessage("includedFilterMaximumElementsError", "Error: Invalid sub-formula value.");
				}  else {
					this.beanSettingsContainer.setFilterValid(true, "includedFilterMaximumElements");
					this.errorMessages.removeKey("includedFilterMaximumElementsError");
				}
			
			} else {
				this.beanSettingsContainer.setFilterValid(false, "includedFilterMaximumElements");
				this.errorMessages.setMessage("includedFilterMaximumElementsError", "Error: Sub-formula value required.");
			}
		}
		// minimum elements filter
		if (this.beanSettingsContainer.isFilterEnabled("includedFilterMinimumElements")) {
			if (this.beanSettingsContainer.getIncludedMinimumElements() != null && this.beanSettingsContainer.getIncludedMinimumElements().trim().length() != 0) {
				if(!FormulaValidator.check(this.beanSettingsContainer.getIncludedMinimumElements())) {
					this.beanSettingsContainer.setFilterValid(false, "includedFilterMinimumElements");
					this.errorMessages.setMessage("includedFilterMinimumElementsError", "Error: Invalid sub-formula value.");
				}  else {
					this.beanSettingsContainer.setFilterValid(true, "includedFilterMinimummElements");
					this.errorMessages.removeKey("includedFilterMinimumElementsError");
				}
			
			} else {
				this.beanSettingsContainer.setFilterValid(false, "includedFilterMinimumElements");
				this.errorMessages.setMessage("includedFilterMinimumElementsError", "Error: Sub-formula value required.");
			}
		}
		// SMARTS inclusion filter
		if (this.beanSettingsContainer.isFilterEnabled("includedFilterSmarts")) {
			if (this.beanSettingsContainer.getSmartsFilterInclusion() != null && this.beanSettingsContainer.getSmartsFilterInclusion().length != 0) 
			{
				if(!SmartsValidator.check(this.beanSettingsContainer.getSmartsFilterInclusion())) {
					this.beanSettingsContainer.setFilterValid(false, "includedFilterSmarts");
					this.errorMessages.setMessage("includedFilterSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setFilterValid(true, "includedFilterSmarts");
					this.errorMessages.removeKey("includedFilterSmartsError");
				}
			} else if(this.beanSettingsContainer.getAdditionalSmartsFilterInclusion() != null && this.beanSettingsContainer.getAdditionalSmartsFilterInclusion().trim().length() != 0) {
				if(!SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsFilterInclusion())) {
					this.beanSettingsContainer.setFilterValid(false, "includedFilterSmarts");
					this.errorMessages.setMessage("includedFilterSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setFilterValid(true, "includedFilterSmarts");
					this.errorMessages.removeKey("includedFilterSmartsError");
				}
			} else {
				this.beanSettingsContainer.setFilterValid(false, "includedFilterSmarts");
				this.errorMessages.setMessage("includedFilterSmartsError", "Error: No substructure set.");
			} 
		}
		// SMARTS exclusion filter
		if (this.beanSettingsContainer.isFilterEnabled("excludedFilterSmarts")) {
			if (this.beanSettingsContainer.getSmartsFilterExclusion() != null && this.beanSettingsContainer.getSmartsFilterExclusion().length != 0) 
			{
				if(!SmartsValidator.check(this.beanSettingsContainer.getSmartsFilterExclusion())) {
					this.beanSettingsContainer.setFilterValid(false, "excludedFilterSmarts");
					this.errorMessages.setMessage("excludedFilterSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setFilterValid(true, "excludedFilterSmarts");
					this.errorMessages.removeKey("excludedFilterSmartsError");
				}
			} else if(this.beanSettingsContainer.getAdditionalSmartsFilterExclusion() != null && this.beanSettingsContainer.getAdditionalSmartsFilterExclusion().trim().length() != 0) {
				if(!SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsFilterExclusion())) {
					this.beanSettingsContainer.setFilterValid(false, "excludedFilterSmarts");
					this.errorMessages.setMessage("excludedFilterSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setFilterValid(true, "excludedFilterSmarts");
					this.errorMessages.removeKey("excludedFilterSmartsError");
				}
			} else {
				this.beanSettingsContainer.setFilterValid(false, "excludedFilterSmarts");
				this.errorMessages.setMessage("excludedFilterSmartsError", "Error: No substructure set.");
			} 
		}
		//substructure information filter
		if (this.beanSettingsContainer.isFilterEnabled("substructureInformationFilterExpression")) {
			if (this.beanSettingsContainer.getSubstructureInformationFilterExpression() != null && this.beanSettingsContainer.getSubstructureInformationFilterExpression().length() != 0) 
			{
				if(!SmartsExpressionValidator.check(this.beanSettingsContainer.getSubstructureInformationFilterExpression())) {
					this.beanSettingsContainer.setFilterValid(false, "substructureInformationFilterExpression");
					this.errorMessages.setMessage("substructureInformationFilterExpressionError", "Error: Invalid SMARTS Expression.");
				}
				else {
					this.beanSettingsContainer.setFilterValid(true, "substructureInformationFilterExpression");
					this.errorMessages.removeKey("substructureInformationFilterExpressionError");
				}
			} else {
				this.beanSettingsContainer.setFilterValid(false, "substructureInformationFilterExpression");
				this.errorMessages.setMessage("substructureInformationFilterExpressionError", "Error: No substructure expression set.");
			} 
		}
		// suspect lists inclusion filter
		if (this.beanSettingsContainer.isFilterEnabled("suspectListsFilter")) {
			if ((this.beanSettingsContainer.getSuspectListFilterFileContainer() == null 
					|| this.beanSettingsContainer.getSuspectListFilterFileContainer().size() == 0) 
					&& !this.isForIdentSuspectListInclusionFilterEnabled() && !this.isDsstoxSuspectListInclusionFilterEnabled()) 
			{
				this.beanSettingsContainer.setFilterValid(false, "suspectListsFilter");
				this.errorMessages.setMessage("suspectListsFilterError", "Suspect list file(s) required.");
			}
			else 
			{
				this.errorMessages.removeKey("suspectListsFilterError");
				this.beanSettingsContainer.setFilterValid(true, "suspectListsFilter");
			}
		}
	}

	private boolean checkFilterSettings() {
		boolean checksFine = true;
		this.errorMessages.removeKey("globalFilterErrors");
		String globalFilterErrors = "";
		java.util.Iterator<String> it = this.beanSettingsContainer.getFilterKeys();
		while (it.hasNext()) {
			String currentKey = it.next();
			if (this.beanSettingsContainer.isFilterEnabled(currentKey) && !this.beanSettingsContainer.isFilterValid(currentKey)) {
				if (globalFilterErrors.length() != 0)
					globalFilterErrors += ", " + this.beanSettingsContainer.getFilterName(currentKey);
				else
					globalFilterErrors += this.beanSettingsContainer.getFilterName(currentKey);
				checksFine = false;
			}
		}
		if (!checksFine)
			this.errorMessages.setMessage("globalFilterErrors" , "Check filters: " + globalFilterErrors);
		return checksFine;
	}
	
	/*
	 * score
	 */
	public boolean isSpectralSimilarityScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("spectralSimilarity");
	}

	public void setSpectralSimilarityScoreEnabled(boolean spectralSimilarityScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(spectralSimilarityScoreEnabled, "spectralSimilarity");
	}

	public boolean isExactSpectralSimilarityScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("exactSpectralSimilarity");
	}

	public void setExactSpectralSimilarityScoreEnabled(boolean exactSpectralSimilarityScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(exactSpectralSimilarityScoreEnabled, "exactSpectralSimilarity");
	}

	// SMARTS scores
	public String getSmartsScoreInclusionLabel() {
		return this.beanSettingsContainer.getSmartsScoreInclusion().length == 1 ? this.beanSettingsContainer.getSmartsScoreInclusion().length + " Substructure selected" : this.beanSettingsContainer.getSmartsScoreInclusion().length + " Substructures selected";
	}
	
	public boolean isSmartsInclusionScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("includedScoreSmarts");
	}

	public void setSmartsInclusionScoreEnabled(boolean smartsInclusionScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(smartsInclusionScoreEnabled, "includedScoreSmarts");
	}

	public String[] getSmartsScoreInclusion() {
		return this.beanSettingsContainer.getSmartsScoreInclusion();
	}
	
	public String getAdditionalSmartsScoreInclusion() {
		return this.beanSettingsContainer.getAdditionalSmartsScoreInclusion();
	}

	public String getAdditionalSmartsScoreExclusion() {
		return this.beanSettingsContainer.getAdditionalSmartsScoreExclusion();
	}
	
	public void setAdditionalSmartsScoreInclusion(String additionalSmartsScoreInclusion) {
		try {
			if ((additionalSmartsScoreInclusion == null || additionalSmartsScoreInclusion.trim().length() == 0) 
					&& (this.beanSettingsContainer.getSmartsScoreInclusion() == null || this.beanSettingsContainer.getSmartsScoreInclusion().length == 0)) {
				this.errorMessages.setMessage("includedScoreSmartsError", "Error: No substructure set.");
			} else if(additionalSmartsScoreInclusion != null && additionalSmartsScoreInclusion.trim().length() != 0) {
				if (SmartsValidator.check(additionalSmartsScoreInclusion))
					this.errorMessages.removeKey("includedScoreSmartsError");
				else {
					throw new Exception();
				}
			} else {
				this.errorMessages.removeKey("includedScoreSmartsError");
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("includedScoreSmartsError", "Error: Invalid SMARTS value.");
		} finally {
			this.beanSettingsContainer.setAdditionalSmartsScoreInclusion(additionalSmartsScoreInclusion);
		}
	}
	
	public void setSmartsScoreInclusion(String[] smartsScoreInclusion) {
		if((smartsScoreInclusion == null || smartsScoreInclusion.length == 0) 
				&& (this.beanSettingsContainer.getAdditionalSmartsScoreInclusion() == null || this.beanSettingsContainer.getAdditionalSmartsScoreInclusion().trim().length() == 0))
			this.errorMessages.setMessage("includedScoreSmartsError", "Error: No substructure set.");
		else if(this.beanSettingsContainer.getAdditionalSmartsScoreInclusion() != null && this.beanSettingsContainer.getAdditionalSmartsScoreInclusion().trim().length() != 0) {
			if (SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsScoreInclusion())) {
				this.errorMessages.removeKey("includedScoreSmartsError");
			}
			else
				this.errorMessages.setMessage("includedScoreSmartsError", "Error: Invalid SMARTS value.");
		}
		else {
			this.errorMessages.removeKey("includedScoreSmartsError");
		}
		this.beanSettingsContainer.setSmartsScoreInclusion(smartsScoreInclusion);
	}

	public boolean isSmartsExclusionScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("excludedScoreSmarts");
	}
	
	public String getSmartsScoreExclusionLabel() {
		return this.beanSettingsContainer.getSmartsScoreExclusion().length == 1 ? this.beanSettingsContainer.getSmartsScoreExclusion().length + " Substructure selected" : this.beanSettingsContainer.getSmartsScoreExclusion().length + " Substructures selected";
	}
	
	public void setSmartsExclusionScoreEnabled(boolean smartsExclusionScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(smartsExclusionScoreEnabled, "excludedScoreSmarts");
	}

	public String[] getSmartsScoreExclusion() {
		return this.beanSettingsContainer.getSmartsScoreExclusion();
	}

	public void setSmartsScoreExclusion(String[] smartsScoreExclusion) {
		if((smartsScoreExclusion == null || smartsScoreExclusion.length == 0) 
				&& (this.beanSettingsContainer.getAdditionalSmartsScoreExclusion() == null || this.beanSettingsContainer.getAdditionalSmartsScoreExclusion().trim().length() == 0))
			this.errorMessages.setMessage("excludedScoreSmartsError", "Error: No substructure set.");
		else if(this.beanSettingsContainer.getAdditionalSmartsScoreExclusion() != null && this.beanSettingsContainer.getAdditionalSmartsScoreExclusion().trim().length() != 0) {
			if (SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsScoreExclusion())) {
				this.errorMessages.removeKey("excludedScoreSmartsError");
			}
			else
				this.errorMessages.setMessage("excludedScoreSmartsError", "Error: Invalid SMARTS value.");
		}
		else {
			this.errorMessages.removeKey("excludedScoreSmartsError");
		}
		this.beanSettingsContainer.setSmartsScoreExclusion(smartsScoreExclusion);
	}

	public void setAdditionalSmartsScoreExclusion(String additionalSmartsScoreExclusion) {
		try {
			if ((additionalSmartsScoreExclusion == null || additionalSmartsScoreExclusion.trim().length() == 0) 
					&& (this.beanSettingsContainer.getSmartsScoreExclusion() == null || this.beanSettingsContainer.getSmartsScoreExclusion().length == 0)) {
				this.errorMessages.setMessage("excludedScoreSmartsError", "Error: No substructure set.");
			} else if(additionalSmartsScoreExclusion != null && additionalSmartsScoreExclusion.trim().length() != 0) {
				if (SmartsValidator.check(additionalSmartsScoreExclusion))
					this.errorMessages.removeKey("excludedScoreSmartsError");
				else {
					throw new Exception();
				}
			} else {
				this.errorMessages.removeKey("excludedScoreSmartsError");
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("excludedScoreSmartsError", "Error: Invalid SMARTS value.");
		} finally {
			this.beanSettingsContainer.setAdditionalSmartsScoreExclusion(additionalSmartsScoreExclusion);
		}
	}
	
	//retention time score
	public List<SelectItem> getAvailablePartitioningCoefficients() {
		return this.beanSettingsContainer.getAvailablePartitioningCoefficients();
	}
	
	public boolean isAvailablePartitioningCoefficientsExist() {
		if (this.getAvailablePartitioningCoefficients() == null || this.getAvailablePartitioningCoefficients().size() == 0)
			return false;
		return true;
	}
	
	public boolean isRetentionTimeScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("retentionTimeTrainingFile");
	}
	
	public void setRetentionTimeScoreEnabled(boolean retentionTimeScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(retentionTimeScoreEnabled, "retentionTimeTrainingFile");
	}

	public String getExperimentalRetentionTimeValue() {
		return this.beanSettingsContainer.getExperimentalRetentionTimeValue();
	}

	public void setExperimentalRetentionTimeValue(String experimentalRetentionTimeValue) {
		try {
			if (experimentalRetentionTimeValue == null || experimentalRetentionTimeValue.trim().length() == 0) {
				this.errorMessages.setMessage("experimentalRetentionTimeValueError", "Error: Experimental retention time value required.");
			}
			else {
				if (PositiveDoubleValueValidator.check(experimentalRetentionTimeValue))
					this.errorMessages.removeKey("experimentalRetentionTimeValueError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("experimentalRetentionTimeValueError", "Error: Invalid experimental retention time value.");
		} finally {
			this.beanSettingsContainer.setExperimentalRetentionTimeValue(experimentalRetentionTimeValue);
		}
	}

	public String getRetentionTimeScoreTrainingFileName() {
		return this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName();
	}

	public String getPartitioningCoefficientColumnName() {
		return this.beanSettingsContainer.getPartitioningCoefficientColumnName();
	}

	public void setPartitioningCoefficientColumnName(String partitioningCoefficientColumnName) {
		this.beanSettingsContainer.setPartitioningCoefficientColumnName(partitioningCoefficientColumnName);
	}
	
	public String getRetentionTimeScoreTrainingFileNamePart() {
		if (this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName() == null)
			return "";
		String value = this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName().substring(0, Math.min(50, this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName().length()));
		if (value.length() < this.beanSettingsContainer.getRetentionTimeScoreTrainingFileName().length())
			value += "...";
		return value;
	}
	
	public void retentionTimeFileUploadListener(org.primefaces.event.FileUploadEvent event) {
		this.errorMessages.removeKey("retentionTimeTrainingFileError"); 
		//do the work
		this.beanSettingsContainer.getUserInputDataHandler().handleRetentionTimeTrainingFile(event.getFile(), this.infoMessages, this.errorMessages, this.beanSettingsContainer);
	}

	//suspect inclusion list score
	public SuspectListFileContainer getSuspectListScoreFileContainer() {
		return this.beanSettingsContainer.getSuspectListScoreFileContainer();
	}

	public String getGlobalSuspectListScoreInfo() {
		if (beanSettingsContainer.getSuspectListScoreFileContainer() == null)
			return "";
		return "Number uploaded suspect lists: " + this.beanSettingsContainer.getSuspectListScoreFileContainer().getTotelFileNumber();
	}
	
	public void removeFromSuspectListScoreContainer(ActionEvent event) {
		if (this.beanSettingsContainer.getSuspectListScoreFileContainer() != null) {
			int suspectListElementName = (Integer) event.getComponent().getAttributes().get("currentSuspectListScoreElementId");
			this.beanSettingsContainer.getSuspectListScoreFileContainer().removeById(suspectListElementName);
			if (this.beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0 
					&& !this.isForIdentSuspectListInclusionScoreEnabled()
					&& !this.isDsstoxSuspectListInclusionScoreEnabled()) {
				this.errorMessages.setMessage("suspectListsScoreError", "Suspect list file(s) required.");
				this.beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			} else
				this.errorMessages.removeKey("suspectListsScoreError");
		}
	}
	
	public void setSuspectListInclusionScoreEnabled(boolean suspectListInclusionScoreEnabled) {
		this.beanSettingsContainer.setScoreEnabled(suspectListInclusionScoreEnabled, "suspectListsScore");
	}

	public boolean isSuspectListInclusionScoreEnabled() {
		return this.beanSettingsContainer.isScoreEnabled("suspectListsScore");
	}

	public void suspectListScoreUploadListener(org.primefaces.event.FileUploadEvent event) {
		this.errorMessages.removeKey("suspectListsScoreError");
		//do the work
		this.beanSettingsContainer.getUserInputDataHandler().handleSuspectListScoreFile(event.getFile(), this.infoMessages, this.errorMessages, this.beanSettingsContainer);
	}
	
	public boolean isForIdentSuspectListInclusionScoreEnabled() {
		return this.beanSettingsContainer.isForIdentSuspectListScoreEnabled();
	}

	public boolean isDsstoxSuspectListInclusionScoreEnabled() {
		return this.beanSettingsContainer.isDsstoxSuspectListScoreEnabled();
	}

	public void setForIdentSuspectListInclusionScoreEnabled(boolean value) {
		this.beanSettingsContainer.setForIdentSuspectListScoreEnabled(value);
		if ((this.beanSettingsContainer.getSuspectListScoreFileContainer() == null 
				|| this.beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) 
				&& !this.isForIdentSuspectListInclusionScoreEnabled() && !this.isDsstoxSuspectListInclusionScoreEnabled()) 
		{
			this.beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			this.errorMessages.setMessage("suspectListsScoreError", "Suspect list file(s) required.");
		}
		else 
		{
			this.errorMessages.removeKey("suspectListsScoreError");
			this.beanSettingsContainer.setScoreValid(true, "suspectListsScore");
		}
	}

	public void setDsstoxSuspectListInclusionScoreEnabled(boolean value) {
		this.beanSettingsContainer.setDsstoxSuspectListScoreEnabled(value);
		if ((this.beanSettingsContainer.getSuspectListScoreFileContainer() == null || 
				this.beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) && 
				!this.isForIdentSuspectListInclusionScoreEnabled() && !this.isDsstoxSuspectListInclusionScoreEnabled()
			) 
		{
			this.beanSettingsContainer.setScoreValid(false, "suspectListsScore");
			this.errorMessages.setMessage("suspectListsScoreError", "Suspect list file(s) required.");
		}
		else 
		{
			this.errorMessages.removeKey("suspectListsScoreError");
			this.beanSettingsContainer.setScoreValid(true, "suspectListsScore");
		}
	}
	
	public boolean isAvailableDatabaseScoresExist() {
		if (!this.beanSettingsContainer.isCompoundsRetrieved())
			return false;
		if (this.beanSettingsContainer.getRetrievedCandidateList() == null)
			return false;
		if (this.getAvailableDatabaseScores().size() == 0)
			return false;
		return true;
	}

	public List<AvailableScore> getAvailableDatabaseScores() {
		return this.beanSettingsContainer.getAvailableDatabaseScores();
	}
	
	//checking init
	private void initScoreSettings() {
		// SMARTS inclusion score
		if (this.beanSettingsContainer.isScoreEnabled("includedScoreSmarts")) {
			if (this.beanSettingsContainer.getSmartsScoreInclusion() != null && this.beanSettingsContainer.getSmartsScoreInclusion().length != 0) 
			{
				if(!SmartsValidator.check(this.beanSettingsContainer.getSmartsScoreInclusion())) {
					this.beanSettingsContainer.setScoreValid(false, "includedScoreSmarts");
					this.errorMessages.setMessage("includedScoreSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setScoreValid(true, "includedScoreSmarts");
					this.errorMessages.removeKey("includedScoreSmartsError");
				}
			} else if(this.beanSettingsContainer.getAdditionalSmartsScoreInclusion() != null && this.beanSettingsContainer.getAdditionalSmartsScoreInclusion().trim().length() != 0) {
				if(!SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsScoreInclusion())) {
					this.beanSettingsContainer.setScoreValid(false, "includedScoreSmarts");
					this.errorMessages.setMessage("includedScoreSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setScoreValid(true, "includedScoreSmarts");
					this.errorMessages.removeKey("includedScoreSmartsError");
				}
			}
			else {
				this.beanSettingsContainer.setScoreValid(false, "includedScoreSmarts");
				this.errorMessages.setMessage("includedScoreSmartsError", "Error: No substructure set.");
			} 
		}
		// SMARTS exclusion score
		if (this.beanSettingsContainer.isScoreEnabled("excludedScoreSmarts")) {
			if (this.beanSettingsContainer.getSmartsScoreExclusion() != null && this.beanSettingsContainer.getSmartsScoreExclusion().length != 0) 
			{
				if(!SmartsValidator.check(this.beanSettingsContainer.getSmartsScoreExclusion())) {
					this.beanSettingsContainer.setScoreValid(false, "excludedScoreSmarts");
					this.errorMessages.setMessage("excludedScoreSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setScoreValid(true, "excludedScoreSmarts");
					this.errorMessages.removeKey("excludedScoreSmartsError");
				}
			} else if(this.beanSettingsContainer.getAdditionalSmartsScoreExclusion() != null && this.beanSettingsContainer.getAdditionalSmartsScoreExclusion().trim().length() != 0) {
				if(!SmartsValidator.check(this.beanSettingsContainer.getAdditionalSmartsScoreExclusion())) {
					this.beanSettingsContainer.setScoreValid(false, "excludedScoreSmarts");
					this.errorMessages.setMessage("excludedScoreSmartsError", "Error: Invalid SMARTS value.");
				}
				else {
					this.beanSettingsContainer.setScoreValid(true, "exludedScoreSmarts");
					this.errorMessages.removeKey("excludedScoreSmartsError");
				}
			}
			else {
				this.beanSettingsContainer.setScoreValid(false, "excludedScorerSmarts");
				this.errorMessages.setMessage("excludedScoreSmartsError", "Error: No substructure set.");
			} 
		}
		// retention time score
		if (this.beanSettingsContainer.isScoreEnabled("retentionTimeTrainingFile")) {
			boolean retentionTimeFailure = false;
			if (this.beanSettingsContainer.getExperimentalRetentionTimeValue() != null && this.beanSettingsContainer.getExperimentalRetentionTimeValue().trim().length() != 0) { 
				if(!PositiveDoubleValueValidator.check(this.beanSettingsContainer.getExperimentalRetentionTimeValue())) {
					this.beanSettingsContainer.setScoreValid(false, "excludedScoreSmarts");
					this.errorMessages.setMessage("experimentalRetentionTimeValueError", "Error: Invalid experimental retention time value.");
					retentionTimeFailure = true;
				}
				else { 
					this.beanSettingsContainer.setScoreValid(true, "retentionTimeTrainingFile");
					this.errorMessages.removeKey("experimentalRetentionTimeValueError");
				}
			} else {
				retentionTimeFailure = true;
				this.beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
				this.errorMessages.setMessage("experimentalRetentionTimeValueError", "Error: Experimental retention time value required.");
			}
			if (this.beanSettingsContainer.getAvailablePartitioningCoefficients() == null || this.beanSettingsContainer.getAvailablePartitioningCoefficients().size() == 0) 
			{
				this.errorMessages.setMessage("retentionTimeTrainingFileError", "No proper value for a partitioning coefficient found. Check candidate and retention time file.");
				this.beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
			}
			else if(this.beanSettingsContainer.getRetentionTimeScoreTrainingFilePath() == null || this.beanSettingsContainer.getRetentionTimeScoreTrainingFilePath().trim().length() == 0) {
				this.errorMessages.setMessage("retentionTimeTrainingFileError", "Error: Retention time training file needed.");
				this.beanSettingsContainer.setScoreValid(false, "retentionTimeTrainingFile");
			}
			else if(!retentionTimeFailure) { 
				this.beanSettingsContainer.setScoreValid(true, "retentionTimeTrainingFile");
				this.errorMessages.removeKey("retentionTimeTrainingFileError");
			}
		}
		// suspect lists inclusion score
		if (this.beanSettingsContainer.isScoreEnabled("suspectListsScore")) {
			if ((this.beanSettingsContainer.getSuspectListScoreFileContainer() == null 
					|| this.beanSettingsContainer.getSuspectListScoreFileContainer().size() == 0) 
					&& !this.isForIdentSuspectListInclusionScoreEnabled()
					&& !this.isDsstoxSuspectListInclusionScoreEnabled()) 
			{
				this.beanSettingsContainer.setScoreValid(false, "suspectListsScore");
				this.errorMessages.setMessage("suspectListsScoreError", "Suspect list file(s) required.");
			}
			else 
			{
				this.errorMessages.removeKey("suspectListsScoreError");
				this.beanSettingsContainer.setScoreValid(true, "suspectListsScore");
			}
		}
	}
	
	private boolean checkScoreSettings() {
		boolean checksFine = true;
		String globalScoreErrors = "";
		java.util.Iterator<String> it = this.beanSettingsContainer.getScoreKeys();
		while (it.hasNext()) {
			String currentKey = it.next();
			if (this.beanSettingsContainer.isScoreEnabled(currentKey) && !this.beanSettingsContainer.isScoreValid(currentKey)) {
				if (globalScoreErrors.length() != 0)
					globalScoreErrors += ", " + this.beanSettingsContainer.getScoreName(currentKey);
				else
					globalScoreErrors += this.beanSettingsContainer.getScoreName(currentKey);
				checksFine = false;
			}
		}
		if (!checksFine)
			this.errorMessages.setMessage("globalScoreErrors", "Check scores: " + globalScoreErrors);
		return checksFine;
	}
	
	/*
	 * fragmenter settings
	 */	
	public java.util.List<javax.faces.model.SelectItem> getPrecursorModes() {
		return this.beanSettingsContainer.getAvailableParameters().getPrecursorModes();
	}

	public java.util.List<javax.faces.model.SelectItem> getTreeDepths() {
		return this.beanSettingsContainer.getAvailableParameters().getTreeDepths();
	}
	
	public String getRelativeMassDeviation() {
		if (this.beanSettingsContainer.getRelativeMassDeviation() == null)
			return "";
		return this.beanSettingsContainer.getRelativeMassDeviation();
	}

	public void setRelativeMassDeviation(String relativeMassDeviation) {
		try {
			if (relativeMassDeviation == null || relativeMassDeviation.trim().length() == 0) {
				this.errorMessages.setMessage("inputMzPpmError", "Error: Mzppm value required.");
			} else {
				if(PositiveDoubleValueValidator.check(relativeMassDeviation))
					this.errorMessages.removeKey("inputMzPpmError");
				else throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("inputMzPpmError", "Error: Invalid Mzppm value.");
		} finally {
			this.beanSettingsContainer.setRelativeMassDeviation(relativeMassDeviation);
		}
	}

	public String getAbsoluteMassDeviation() {
		if (this.beanSettingsContainer.getAbsoluteMassDeviation() == null)
			return "";
		return String.valueOf(this.beanSettingsContainer.getAbsoluteMassDeviation());
	}

	public void setAbsoluteMassDeviation(String absoluteMassDeviation) {
		try {
			if (absoluteMassDeviation == null || absoluteMassDeviation.trim().length() == 0) {
				this.errorMessages.setMessage("inputMzAbsError", "Error: Mzabs value required.");
			} else {
				if(PositiveDoubleValueValidator.check(absoluteMassDeviation))
					this.errorMessages.removeKey("inputMzAbsError");
				else throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("inputMzAbsError", "Error: Invalid Mzabs value.");
		} finally {
			this.beanSettingsContainer.setAbsoluteMassDeviation(absoluteMassDeviation);
		}
	}
	
	public Integer getMode() {
		return this.beanSettingsContainer.getMode();
	}

	public void setMode(Integer mode) {
		this.beanSettingsContainer.setMode(mode);
	}

	public boolean isGroupCandidatesEnabled() {
		return this.mergedCandidateResultsByInChIKey1;
	}

	public void setGroupCandidatesEnabled(boolean groupCandidates) {
		this.mergedCandidateResultsByInChIKey1 = groupCandidates;
	}
	
	public Byte getTreeDepth() {
		return this.beanSettingsContainer.getTreeDepth();
	}

	public void setTreeDepth(Byte treeDepth) {
		this.beanSettingsContainer.setTreeDepth(treeDepth);
	}
	
	public String getPeakList() {
		if (this.beanSettingsContainer == null)
			return "";
		return this.beanSettingsContainer.getPeakList();
	}

	public void setPeakList(String peakList) {
		try {
			this.beanSettingsContainer.setPeakList(peakList);
			if (peakList == null || peakList.trim().length() == 0)
				this.errorMessages.setMessage("peakListInputError", "Error: Peak list required.");
			else {
				if (PeakListValidator.check(peakList))
					this.errorMessages.removeKey("peakListInputError");
				else
					throw new Exception();
			}
		} catch (Exception e) {
			this.errorMessages.setMessage("peakListInputError", "Error: Invalid peak list value.");
		} finally {
			this.beanSettingsContainer.setPeakList(peakList);
		}
	}
	
	public boolean isPositiveCharge() {
		return this.beanSettingsContainer.isPositiveCharge();
	}

	public void setPositiveCharge(boolean positiveCharge) {
		this.beanSettingsContainer.setPositiveCharge(positiveCharge);
	}
	
	private boolean checkFragmenterSettings() {
		boolean checksFine = true;
		if (this.beanSettingsContainer.getRelativeMassDeviation() != null && this.beanSettingsContainer.getRelativeMassDeviation().length() != 0) {
			try {
				if (Double.parseDouble(this.beanSettingsContainer.getRelativeMassDeviation()) < 0.0)
					throw new Exception();
				else
					this.errorMessages.removeKey("inputMzPpmError");
			} catch (Exception e) {
				checksFine = false;
				this.errorMessages.setMessage("inputMzPpmError", "Error: Invalid mzppm value.");
			}
		} else {
			checksFine = false;
			this.errorMessages.setMessage("inputMzPpmError", "Error: Mzppm required.");
		}

		if (this.beanSettingsContainer.getAbsoluteMassDeviation() != null && this.beanSettingsContainer.getAbsoluteMassDeviation().length() != 0) {
			try {
				if (Double.parseDouble(this.beanSettingsContainer.getAbsoluteMassDeviation()) < 0.0)
					throw new Exception();
				else
					this.errorMessages.removeKey("inputMzAbsError");
			} catch (Exception e) {
				checksFine = false;
				this.errorMessages.setMessage("inputMzAbsError", "Error: Invalid mzabs value.");
			}
		} else {
			checksFine = false;
			this.errorMessages.setMessage("inputMzAbsError", "Error: Mzabs required.");
		}

		if (this.beanSettingsContainer.getPeakList() != null && this.beanSettingsContainer.getPeakList().trim().length() != 0) {
			if (!PeakListValidator.check(this.beanSettingsContainer.getPeakList())) {
				checksFine = false;
				this.errorMessages.setMessage("peakListInputError", "Error: Invalid peak list value.");
			} else
				this.errorMessages.removeKey("peakListInputError");
		} else {
			checksFine = false;
			this.errorMessages.setMessage("peakListInputError", "Error: Peak list required.");
		}
		return checksFine;
	}
	
	public void checkParametersForDownload() {
		this.errorMessages.removeKey("buttonDownloadParametersDatabaseError");
		this.errorMessages.removeKey("buttonProcessCompoundsError");
		this.errorMessages.removeKey("buttonProcessCompoundsFilterError");
		this.errorMessages.removeKey("buttonProcessCompoundsScoreError");
		System.out.println("checking parameters for download");
		boolean allSettingsFine = true;
		this.initFilterSettings();
		this.initScoreSettings();

		if (!this.checkDatabaseSettings()) {
			allSettingsFine = false;
			this.errorMessages.setMessage("buttonDownloadParametersDatabaseError", "Check database settings.");
		} else {
			this.errorMessages.removeKey("buttonDownloadParametersDatabaseError");
		}
		if (!this.checkFilterSettings()) {
			allSettingsFine = false;
			this.errorMessages.setMessage("buttonDownloadParametersFilterError", this.errorMessages.getMessage("globalFilterErrors"));
		} else {
			this.errorMessages.removeKey("buttonDownloadParametersFilterError");
			this.errorMessages.removeKey("buttonProcessCompoundsFilterError");
		}
		if (!this.checkScoreSettings()) {
			allSettingsFine = false;
			this.errorMessages.setMessage("buttonDownloadParametersScoreError", this.errorMessages.getMessage("globalScoreErrors"));
		} else {
			this.errorMessages.removeKey("buttonDownloadParametersScoreError");
			this.errorMessages.removeKey("buttonProcessCompoundsScoreError");
		}
		if (!this.checkFragmenterSettings()) {
			allSettingsFine = false;
		}
		if (!allSettingsFine) {
			return;
		}
		RequestContext.getCurrentInstance().execute("PF('downloadParametersDialog').show();");
	}
	
	public org.primefaces.model.StreamedContent getDownloadParameters() {
		ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
		String pathToProperties = servletContext.getRealPath("/resources/README.txt");
		
		org.primefaces.model.StreamedContent resource = null;
		try {
			resource = this.beanSettingsContainer.getUserOutputDataHandler().getDownloadParameters(this.errorMessages, pathToProperties);
		} catch(Exception e) {
			resource = new org.primefaces.model.DefaultStreamedContent(System.in, "application/zip", "MetFragWeb_Parameters.zip");
		}
		return resource;
	}
	
	public void generateSpectrumModelViewListener(ActionEvent action) {
		if(this.beanSettingsContainer.generateSpectrumModelView(this.errorMessages)) {
			this.isSpectrumViewActive = true;
			RequestContext.getCurrentInstance().update("spectrumViewDialog");
			RequestContext.getCurrentInstance().execute("PF('spectrumViewDialog').show();");
		}
		else {
			this.isSpectrumViewActive = false;
			RequestContext.getCurrentInstance().execute("PF('spectrumViewDialog').hide();");
		}
	}
	
	public void closeSpectrumView(CloseEvent event) {
		this.isSpectrumViewActive = false;
	}
	
	public boolean isSpectrumViewActive() {
		return this.isSpectrumViewActive;
	}
	
	public LineChartModel getSpectrumModelView() {
		return this.beanSettingsContainer.getSpectrumModelView();
	}
	
	/*
	 * candidate processing
	 */
	public String getProcessingStatus() {
		return this.beanSettingsContainer.getProcessingStatus();
	}

	public int getProcessingProgress() {
		if(this.processCompoundsThreadRunner == null)
			return this.beanSettingsContainer.getProcessingProgress();
		else
			return this.beanSettingsContainer.getProcessingProgress(this.processCompoundsThreadRunner);
	}
	
	public void processCompounds(ActionEvent event) {
		RequestContext.getCurrentInstance().execute("PF('processCandidatesProgressDialog').show();");
		this.isCandidateProcessing = true;
		this.errorMessages.removeKey("buttonDownloadParametersFilterError");
		this.errorMessages.removeKey("buttonDownloadParametersScoreError");
		this.errorMessages.removeKey("buttonDownloadParametersError");
		this.errorMessages.removeKey("buttonProcessCompoundsError");
		this.infoMessages.removeKey("processingProcessedCandidatesInfo");
		System.out.println("init scores");
		//initialise parameters
		this.initFilterSettings();
		this.initScoreSettings();
		System.out.println("checks before processing");
		
		//cluster compounds reset
		if(this.clusterCompoundsThread != null)
			try {
				this.clusterCompoundsThread.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		if(this.clusterCompoundsThreadRunner != null) this.clusterCompoundsThreadRunner.reset();
		
		//check all parameters
		boolean allSettingsFine = true;
		//check filter settings
		if (!this.checkFilterSettings()) {
			allSettingsFine = false;
			this.errorMessages.setMessage("buttonProcessCompoundsFilterError", this.errorMessages.getMessage("globalFilterErrors"));
		} else {
			this.errorMessages.removeKey("buttonProcessCompoundsFilterError");
			this.errorMessages.removeKey("buttonProcessCompoundsFilterError");
		}
		//check score settings
		if (!this.checkScoreSettings()) {
			allSettingsFine = false;
			this.errorMessages.setMessage("buttonProcessCompoundsScoreError", this.errorMessages.getMessage("globalScoreErrors"));
		} else {
			this.errorMessages.removeKey("buttonProcessCompoundsScoreError");
			this.errorMessages.removeKey("buttonProcessCompoundsScoreError");
		}
		//check fragmenter settings
		if (!this.checkFragmenterSettings()) {
			allSettingsFine = false;
		}
		if (!allSettingsFine) {
			RequestContext.getCurrentInstance().execute("PF('processCandidatesProgressDialog').hide();");
			this.isCandidateProcessing = false;
			return;
		}
		this.candidateStatistics = new CandidateStatistics();
		this.candidateStatistics.setShowPointLabels(false);
		this.candidateStatistics.setSelectedCandidate(0);
		RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(5)");
		System.out.println("start processing");
		//create a thread that undertakes the processing
		this.threadExecutionStarted = true;
		try {
			this.processedPeaklistObject = this.beanSettingsContainer.generatePeakListObject();
		} catch (Exception e) {
			e.printStackTrace();
			this.processedPeaklistObject = null;
		}
		
		this.processCompoundsThreadRunner = new ProcessCompoundsThreadRunner(this.beanSettingsContainer, 
			this.infoMessages, this.errorMessages, this.getSessionId(), this.getRootSessionFolder(), this.mergedCandidateResultsByInChIKey1);
		this.thread = new Thread(this.processCompoundsThreadRunner);
		this.selectedNode = null;
		/*
		 * start the metfrag processing
		 */
		if(this.thread != null) this.thread.start();
	}

	public int getNumberProcessedCompounds() {
		return this.metFragResultsContainer != null && this.metFragResultsContainer.getMetFragResults() != null ? this.metFragResultsContainer.getMetFragResults().size() : 0;
	}
	
	public void setCandidateProcessingFinished(boolean candidateProcessingFinished) {
		this.beanSettingsContainer.setCandidateProcessingFinished(candidateProcessingFinished);
	}

	public boolean isCandidateProcessingFinished() {
		return this.beanSettingsContainer.isCandidateProcessingFinished();
	}
	
	/**
	 * interrupts processing thread
	 */

	public void stopCompoundRetrievingListener(ActionEvent event) {
		//nothing to do
	}
	
	public void stopCompoundRetrieving() {
		System.out.println("stop retrieving triggered");
		if(this.retrieveCompoundsThreadRunner == null) return;
		try {
			this.retrieveCompoundsThreadRunner.setInterrupted(true);
			this.thread.join();
			while(this.thread.isAlive()) {
				try {
					System.out.println("waiting for thread to be finished");
				    Thread.sleep(2000);                 
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
			System.out.println("Thread is finished");

			this.isDatabaseProcessing = false;
			this.beanSettingsContainer.setCompoundsRetrieved(false);
			this.metFragResultsContainer = new MetFragResultsContainer();
			this.filteredMetFragResultsContainer = new MetFragResultsContainer();
			this.infoMessages.removeKey("processingProcessedCandidatesInfo");

			this.threadExecutionStarted = false;
			if(this.clusterCompoundsThread != null)
				try {
					this.clusterCompoundsThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			if(this.clusterCompoundsThreadRunner != null) this.clusterCompoundsThreadRunner.reset();
			//stop the thread
			RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
			//set the message
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Candidate retrieving stopped.",  ""));
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void stopCompoundProcessingListener(ActionEvent event) {
		this.beanSettingsContainer.setProcessCompoundsDialogHeader("Stopping Process");
		RequestContext.getCurrentInstance().update("mainForm:mainAccordion:processCandidatesProgressDialog");
	}
	
	public void stopCompoundProcessing() {
		System.out.println("stop processing triggered");
		try {
			this.beanSettingsContainer.terminateMetFragProcess();
			this.thread.join();
			//cluster compounds stop
			if(this.clusterCompoundsThread != null) {
				this.clusterCompoundsThread.join();
				this.clusterCompoundsThreadStarted = false;
			}
			while(this.thread.isAlive()) {
				try {
					System.out.println("waiting for thread to be finished");
				    Thread.sleep(2000);                 
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
			this.infoMessages.removeKey("processingErrorCandidatesInfo");
			this.infoMessages.removeKey("processingProcessedCandidatesInfo");
			this.infoMessages.removeKey("processingFilteredCandidatesInfo");
			this.infoMessages.removeKey("filterCompoundsInfo");
			//stop the thread
			this.isCandidateProcessing = false;
			this.metFragResultsContainer = new MetFragResultsContainer();
			this.filteredMetFragResultsContainer = new MetFragResultsContainer();
			RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
			//set the message
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Processing stopped.",  ""));
			this.beanSettingsContainer.setProcessCompoundsDialogHeader("");
			this.beanSettingsContainer.resetProcessStatus();
			this.retrieveCompoundsThreadRunner = null;
			
		} catch (InterruptedException e) {
			return;
		}
	}
	
	public void checkProcessingThread() {
		this.checkClusterThread();
		if(this.thread == null) return;
		if(!this.threadExecutionStarted) return;
		if(!this.isCandidateProcessing) return;
		if(!this.thread.isAlive() && this.processCompoundsThreadRunner != null && !this.processCompoundsThreadRunner.isInterrupted()) {
			try {
				this.isCandidateProcessing = false;
				System.out.println("checkProcessingThread thread is dead");
				this.threadExecutionStarted = false;
				this.explainedPeaksFilter = new Integer[0];
				this.infoMessages.removeKey("filterCompoundsInfo");
			//	RequestContext.getCurrentInstance().execute("PF('MetFragResultsTable').filter()");
				RequestContext.getCurrentInstance().update("mainForm:mainAccordion:MetFragResultsTable");
				RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
				this.weights = this.processCompoundsThreadRunner.getWeights();
				this.metFragResultsContainer = this.processCompoundsThreadRunner.getResultsContainer();
				this.filteredMetFragResultsContainer = new MetFragResultsContainer();
				for(int i = 0; i < this.metFragResultsContainer.getMetFragResults().size(); i++)
					this.filteredMetFragResultsContainer.addMetFragResult(this.metFragResultsContainer.getMetFragResults().get(i));
				this.filteredMetFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
				this.filteredMetFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
				this.filteredMetFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
				this.generateScoreDistributionModelView();
				if(!this.isScoreDistributionModelAvailable()) RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(3)");
				//set processing infos
				String detailedMessage = this.infoMessages.getMessage("processingProcessedCandidatesInfo"); 
				if(this.infoMessages.containsKey("processingErrorCandidatesInfo")) {
					detailedMessage += " " + this.infoMessages.getMessage("processingErrorCandidatesInfo");
				}
				if(this.infoMessages.containsKey("processingFilteredCandidatesInfo"))
					detailedMessage += " " + this.infoMessages.getMessage("processingFilteredCandidatesInfo");
				//set the global message
				FacesContext.getCurrentInstance().addMessage("fragmenterGrowl", new FacesMessage("Processing finished", detailedMessage) );
				//reset the header of the processing dialog
				this.beanSettingsContainer.setProcessCompoundsDialogHeader("");
				this.beanSettingsContainer.resetProcessStatus();
				//start clustering compounds
				if(this.compoundClusteringEnabled && this.getNumberProcessedCompounds() < 1000) {
					if(this.filteredMetFragResultsContainer.getMetFragResults().size() >= 5) {
						this.clusterCompoundsThreadRunner = new ClusterCompoundsThreadRunner(this.beanSettingsContainer, 
								this.infoMessages, this.errorMessages, this.filteredMetFragResultsContainer);
						this.clusterCompoundsThread = new Thread(this.clusterCompoundsThreadRunner);
						if(this.clusterCompoundsThread != null) {
							this.clusterCompoundsThreadStarted = true;
							this.clusterCompoundsThread.start();
						}
					}
				}
			}
			catch(Exception e) {
				//error occured
				e.printStackTrace();
				this.isCandidateProcessing = false;
				System.err.println("checkProcessingThread error");
				this.infoMessages.removeKey("processingProcessedCandidatesInfo");
				this.infoMessages.removeKey("filterCompoundsInfo");
				this.metFragResultsContainer = new MetFragResultsContainer();
				this.filteredMetFragResultsContainer = new MetFragResultsContainer();
				this.beanSettingsContainer.resetProcessStatus();
				FacesContext.getCurrentInstance().addMessage("fragmenterGrowl", new FacesMessage(FacesMessage.SEVERITY_ERROR, "Failed to process candidates", ""));
				this.errorMessages.setMessage("buttonProcessCompoundsError", "Error when processing candidates.");
				return;
			}
			this.errorMessages.removeKey("buttonProcessCompoundsError");
		}
	}
	
	public boolean isCandidateProcessing() {
		return this.isCandidateProcessing;
	}
	
	public String getProcessCompoundsDialogHeader() {
		return this.beanSettingsContainer.getProcessCompoundsDialogHeader();
	}
	
	public String getLabel() {
		return "test";
	}
	
	/*
	 * results cluster
	 */
	public void checkClusterThread() {
		if(this.clusterCompoundsThread == null) return;
		if(this.clusterCompoundsThreadStarted && this.clusterCompoundsThreadRunner != null) {
			if(this.clusterCompoundsThreadRunner.isReady()) {
				RequestContext.getCurrentInstance().update("mainForm:mainAccordion:resultClusterPanel");
				this.clusterCompoundsThreadStarted = false;
			}
		}
	}
	
	public boolean isCompoundsClusterReady() {
		return this.clusterCompoundsThreadRunner == null ? false : this.clusterCompoundsThreadRunner.isReady();
	}
	
	public boolean isCompoundClusteringEnabled() {
		return this.compoundClusteringEnabled;
	}
	
	public OrganigramNode getCompoundsClusterRoot() {
		return this.clusterCompoundsThreadRunner.getTreeRoot();
	}

    public void expandSelectedClusterCompounds() {
    	if(this.selectedNode != null) {
    		OrganigramNode currentSelection = OrganigramHelper.findTreeNode(this.clusterCompoundsThreadRunner.getTreeRoot(), this.selectedNode);
            java.util.Stack<OrganigramNode> stackList = new java.util.Stack<OrganigramNode>();
    		stackList.push(currentSelection);
    		while(!stackList.isEmpty()) {
    			OrganigramNode current = stackList.pop();
    			current.setExpanded(true);
    			if(current.getChildCount() == 0) continue;
     			for(OrganigramNode child : current.getChildren()) {
    				stackList.push(child);
    			}
    		}
    	//	RequestContext.getCurrentInstance().update("mainForm:mainAccordion:compoundClusterTree");
    	}
    }
    
    public void collapseSelectedClusterCompounds() {
    	if(this.selectedNode != null) {
    		OrganigramNode currentSelection = OrganigramHelper.findTreeNode(this.clusterCompoundsThreadRunner.getTreeRoot(), this.selectedNode);
            java.util.Stack<OrganigramNode> stackList = new java.util.Stack<OrganigramNode>();
    		stackList.push(currentSelection);
    		while(!stackList.isEmpty()) {
    			OrganigramNode current = stackList.pop();
    			current.setExpanded(false);
    			if(current.getChildCount() == 0) continue;
     			for(OrganigramNode child : current.getChildren()) {
    				stackList.push(child);
    			}
    		}
    	//	RequestContext.getCurrentInstance().update("mainForm:mainAccordion:compoundClusterTree");
    	}
    }
    
    public void displaySelectedClusterCompounds() {
    	if(this.selectedNode != null) {
    		OrganigramNode currentSelection = OrganigramHelper.findTreeNode(this.clusterCompoundsThreadRunner.getTreeRoot(), this.selectedNode);
           
        	this.filteredMetFragResultsContainer = new MetFragResultsContainer();

    		this.filteredMetFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
    		this.filteredMetFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
    		this.filteredMetFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());

    		java.util.Stack<OrganigramNode> stackList = new java.util.Stack<OrganigramNode>();
    		stackList.push(currentSelection);
    		while(!stackList.isEmpty()) {
    			OrganigramNode current = stackList.pop();
    			if(current == null) continue;
    			if(((INode)current.getData()).hasResult())
    				this.filteredMetFragResultsContainer.addMetFragResultScoreSorted(((INode)current.getData()).getResult());
    			if(current.getChildCount() == 0) continue;
     			for(OrganigramNode child : current.getChildren()) {
    				stackList.push(child);
    			}
    		}
    		this.generateScoreDistributionModelView();
    		
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:MetFragResultsTable");
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:statistics");
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:resultsTablePanel");
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:statisticsPanel");
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:statistics");
    		if(!this.isScoreDistributionModelAvailable()) RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(3)");
    		this.infoMessages.removeKey("filterCompoundsInfo");
    		this.explainedPeaksFilter = new Integer[0];
    		FacesContext.getCurrentInstance().addMessage("resultsClusterGrowl", new FacesMessage("Filtered Candidates", "Filtered " + this.filteredMetFragResultsContainer.getMetFragResults().size() + " candidates remained in result table") );
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:resultTable");
    		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:scoreDistributionPlot");
    	}
    }
    
    public void nodeSelectListener(OrganigramNodeSelectEvent event) {
    	
    }
    
    public boolean getClusterImageTooltipRendered() {
    	return this.clusterImageTooltipRendered;
    }
    
    public OrganigramNode getSelectedNode() {
        return this.selectedNode;
    }
 
    public void setSelectedNode(OrganigramNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
    public void nodeExpand(OrganigramNodeExpandEvent event) {
        event.getOrganigramNode().setExpanded(true); 
    }

    public void nodeCollapse(OrganigramNodeCollapseEvent event) {
        event.getOrganigramNode().setExpanded(false);     
    }

	/*
	 * results statistics 
	 */
	public void generateScoreDistributionModelView() {
		this.candidateStatistics.generateScoreDistributionModelView(
				this.filteredMetFragResultsContainer.getMetFragResults()
		);
	}
	
	public boolean isScoreDistributionModelAvailable() {
		return this.candidateStatistics.getScoreDistributionModel() == null ? false : true;
	}
	
	public LineChartModel getScoreDistributionModelView() {
		return this.candidateStatistics.getScoreDistributionModel();
	}
	
	public String getScoreDistributionModelPointLabels() {
		return this.candidateStatistics.getScoreDistributionModelPointLabels();
	}

	public String getScoreDistributionModelLegendLabels() {
		return this.candidateStatistics.getLegendLabels();
	}
	
	public boolean isShowScoreDistributionPointLabels() {
		return this.candidateStatistics.isShowPointLabels();
	}

	public void setShowScoreDistributionPointLabels(boolean isShowScoreDistributionPointLabels) {
		this.candidateStatistics.setShowPointLabels(isShowScoreDistributionPointLabels);
	}
	
	public void candidatesViewItemSelect(ItemSelectEvent event) {
		if(this.getMetFragResults().size() <= event.getSeriesIndex()) return;
		int candidateIndex = event.getItemIndex();
		int page = (int)Math.floor(candidateIndex / 10);
		int pos = candidateIndex % 10;
		RequestContext.getCurrentInstance().execute(
			"var jobs = 0;" +
			"var span = $('#mainForm').children('div').eq(0).children('div').eq(9).children('span').eq(0);" +
			"var container = span.children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(2);" +
			"var scrollTo;" +
			"function f2() {"
				+ "if(jobs == 2) {"
					+ "setTimeout(function() { "
						+ "container = span.children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(2);"
						+ "scrollTo = container.children('table').eq(0).children('tbody').eq(0).children('tr').eq(" + pos + ");"
						+ "scrollTo.stop().css(\"background-color\", \"#FFFF9C\").animate({ backgroundColor: \"#FFFFFF\"}, 3000);"
					+ "}, 500);"
				+ "}; "
			+ "};" +
			"function job1() { setTimeout(function() { PF('MetFragResultsTable').getPaginator().setPage(" + page + "); jobs+=1; f2(); }, 10);}" +
			"function job2() { " +
				"setTimeout(function() { "
					+ "container = span.children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(0).children('div').eq(2);"
					+ "scrollTo = container.children('table').eq(0).children('tbody').eq(0).children('tr').eq(" + pos + ");"
					+ "container.animate({scrollTop: scrollTo.offset().top - container.offset().top + container.scrollTop()}); jobs+=1; f2(); "
				+ "}, 10); " +
			"}" +
			"job1();" +
			"job2();"
		);
	}
	
	public int getSelectedCandidate() {
		return this.candidateStatistics.getSelectedCandidate();
	}
	
	public String[] getShowScoreGraphs() {
		return this.candidateStatistics.getShowScoreGraphs();
	}
	
	public void setShowScoreGraphs(String[] showScoreGraphs) {
		this.candidateStatistics.setShowScoreGraphs(showScoreGraphs, this.processCompoundsThreadRunner.getAvaiableScoreNamesForScoreGraph());
	}

	public java.util.List<SelectItem> getAvailableScoreNamesForScoreGraph() {
		if(this.processCompoundsThreadRunner != null && this.processCompoundsThreadRunner.getAvaiableScoreNamesForScoreGraph() != null)
			return this.processCompoundsThreadRunner.getAvaiableScoreNamesForScoreGraph();
		else 
			return new java.util.ArrayList<SelectItem>(); 
	}
	
	public java.util.List<SelectItem> getAvailableScoreNames() {
		if(this.processCompoundsThreadRunner != null && this.processCompoundsThreadRunner.getAvaiableScoreNames() != null)
			return this.processCompoundsThreadRunner.getAvaiableScoreNames();
		else 
			return new java.util.ArrayList<SelectItem>(); 
	}
	
	public boolean isScoreNamesAvailable() {
		//check whether more than one score is available
		if(this.processCompoundsThreadRunner != null && this.processCompoundsThreadRunner.getAvaiableScoreNames() != null)
			return true;
		return false; 
	}
	
	public void printSelectedScoreGraphs() {
		String[] strings = this.candidateStatistics.getShowScoreGraphs();
		for(int i = 0; i < strings.length; i++) {
			System.out.println(strings[i]);
		}
	}
	
	/*
	 * results
	 */
	public List<MetFragResult> getMetFragResults() {
		if(this.metFragResultsContainer == null || this.metFragResultsContainer.getMetFragResults() == null
				|| this.metFragResultsContainer.getMetFragResults().size() == 0)
			return new java.util.ArrayList<MetFragResult>();
		return this.metFragResultsContainer.getMetFragResults();
	}

	public List<MetFragResult> getFilteredMetFragResults() {
		if(this.filteredMetFragResultsContainer == null || this.filteredMetFragResultsContainer.getMetFragResults() == null
				|| this.filteredMetFragResultsContainer.getMetFragResults().size() == 0)
			return new java.util.ArrayList<MetFragResult>();
		return this.filteredMetFragResultsContainer.getMetFragResults();
	}
	
	public List<Weight> getWeights() {
		return weights;
	}

	public void setWeights(List<Weight> weights) {
		this.weights = weights;
	}
	
	public boolean isRenderWeights() {
		if(this.weights == null) return false;
		return this.weights.size() > 1;
	}
	
	public void weightsSliderValueChanged(SlideEndEvent event) {
		List<MetFragResult> molecules = this.metFragResultsContainer.getMetFragResults();
		
		MetFragResultsContainer metFragResultsContainer = new MetFragResultsContainer();
		metFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
		metFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
		metFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
		for (MetFragResult molecule : molecules) {
			molecule.recalculateScore(this.weights);
			metFragResultsContainer.addMetFragResultScoreSorted(molecule);
		}
		this.metFragResultsContainer = metFragResultsContainer;
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();

		this.filteredMetFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
		this.filteredMetFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
		this.filteredMetFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
		for(MetFragResult molecule : this.metFragResultsContainer.getMetFragResults()) {
			if(!molecule.isFiltered()) this.filteredMetFragResultsContainer.addMetFragResult(molecule);
		}
		
		this.generateScoreDistributionModelView();
		if(this.clusterCompoundsThreadRunner != null && this.clusterCompoundsThreadRunner.isReady()) {
			this.clusterCompoundsThreadRunner.updateScores();
		}
	}
	
	public void weightsTextInputValueChanged() {
		List<MetFragResult> molecules = this.metFragResultsContainer.getMetFragResults();

		MetFragResultsContainer metFragResultsContainer = new MetFragResultsContainer();
		metFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
		metFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
		metFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
		for (MetFragResult molecule : molecules) {
			molecule.recalculateScore(this.weights);
			metFragResultsContainer.addMetFragResultScoreSorted(molecule);
		}
		this.metFragResultsContainer = metFragResultsContainer;
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();

		this.filteredMetFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
		this.filteredMetFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
		this.filteredMetFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
		
		for(MetFragResult molecule : this.metFragResultsContainer.getMetFragResults()) {
			if(!molecule.isFiltered()) this.filteredMetFragResultsContainer.addMetFragResult(molecule);
		}
		this.generateScoreDistributionModelView();
		if(this.clusterCompoundsThreadRunner != null && this.clusterCompoundsThreadRunner.isReady()) {
			System.out.println("updating scores");
			this.clusterCompoundsThreadRunner.updateScores();
		}
	}
	
	public boolean isResultsAvailable() {
		if(this.metFragResultsContainer == null || this.metFragResultsContainer.getMetFragResults().size() == 0) 
			return false;
		return true;
	}
	
	public boolean isCompoundNameAvailable() {
		return this.metFragResultsContainer.isCompoundNameAvailable();
	}

	public boolean isSimScoreAvailable() {
		return this.metFragResultsContainer.isSimScoreAvailable();
	}
	
	public int getNumberPeaksUsed() {
		return this.metFragResultsContainer.getNumberPeaksUsed();
	}

	public void closeFragmentsView(CloseEvent event) {
		this.isFragmentsViewActive = false;
	}

	public boolean isFragmentsViewActive() {
		return this.isFragmentsViewActive;
	}
	
	public LineChartModel getFragmentModelView() {
		return this.beanSettingsContainer.getSpectrumModelView();
	}

	public java.util.Vector<ScoreSummary> getCandidateScore() {
		java.util.Vector<ScoreSummary> scoreSummaries = new java.util.Vector<ScoreSummary>();
		if(this.currentCandidateScores == null) return scoreSummaries;
		for(int i = 0; i < this.currentCandidateScores.length; i++)
			if(this.currentCandidateScores[i].isUsedForScoring()) scoreSummaries.add(this.currentCandidateScores[i]);
		return scoreSummaries;
	}
	
	public void setCurrentScoreCandidate(MetFragResult metFragResult) {
		this.currentScoreCandidate = metFragResult;
	}
	
	public void generateScoresViewListener() {
		this.isScoresViewActive = true;
		if(this.currentScoreCandidate == null) {
			return;
		}
	//	this.currentScoreCandidate = this.metFragResultsContainer.getMetFragResults().get(candidateScoreIndex);
		this.currentCandidateScores = this.currentScoreCandidate.getScoreSummary();
	}

	public String getCurrentScoreCandidateName() {
		return this.currentScoreCandidate.getName();
	}

	public String getCurrentScoreCandidateIdentifier() {
		return this.currentScoreCandidate.getIdentifier();
	}
	
	public boolean isScoresViewActive() {
		return this.isScoresViewActive;
	}
	
	public void closeScoresView() {
		this.isScoresViewActive = false;
	}

	/**
	 * 
	 * @return
	 */
	public org.primefaces.model.StreamedContent generateCandidateDownloadFile() {
		org.primefaces.model.StreamedContent resource = new org.primefaces.model.DefaultStreamedContent(System.in, "application/vnd.ms-excel", "MetFragWeb_Candidate.xls" );
		try {
			resource = this.beanSettingsContainer.getUserOutputDataHandler().generatedCandidateDownloadFile(this.currentScoreCandidate);
		} catch (Exception e1) {
			e1.printStackTrace();
			return resource;
		}
		if(this.currentScoreCandidate == null) {
			System.out.println("generatedCandidateDownloadFile null");
			return resource;
		}
		return resource;
	}
	
	/**
	 * 
	 */
	public void generateFragmentsModelViewListener() {
		System.out.println("generateFragmentsModelViewListener");
		try {
			if(this.currentScoreCandidate == null) {
				System.out.println("generateFragmentsModelViewListener null");
				return;
			}
			//generate image of annotated fragments of the relating molecule
			System.out.println("generateFragmentImages");
			this.generateFragmentImages(this.currentScoreCandidate);
			//generate spectrum view
			System.out.println("generateFragmentsModelView");
			if(this.generateFragmentsModelView(this.currentScoreCandidate)) 
			{
				this.isFragmentsViewActive = true;
				System.out.println("update fragmentsViewDialog");
				RequestContext.getCurrentInstance().update("fragmentsViewDialog");
			}
			else {
				this.isFragmentsViewActive = false;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	protected void generateFragmentImages(MetFragResult molecule) throws IOException {
		//first remove last generated fragments
		java.io.File imageFolderFragments = new java.io.File(this.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "images/fragments");
		if(imageFolderFragments.exists()) FileUtils.deleteDirectory(imageFolderFragments);
		imageFolderFragments.mkdirs();
		//start creating the fragments
		this.currentFragments = new java.util.Vector<Fragment>();
		String sessionId = this.getSessionId();
		ICandidate candidate = molecule.getRoot().getCandidate();
		try {
			candidate.initialisePrecursorCandidate();
		} catch (Exception e1) {
			System.err.println("error when initialising precursor for fragment generation");
			e1.printStackTrace();
			return;
		}
		for(int i = 0; i < molecule.getMatchList().getNumberElements(); i++) {
			try {
				HighlightSubStructureImageGenerator imageGenerator = new HighlightSubStructureImageGenerator();
				int size = 300;
				if(Double.parseDouble(this.beanSettingsContainer.getNeutralMonoisotopicMass()) > 500) size = 400;
				if(Double.parseDouble(this.beanSettingsContainer.getNeutralMonoisotopicMass()) > 700) size = 500;
				imageGenerator.setImageHeight(size);
				imageGenerator.setImageWidth(size);
				RenderedImage image;
				java.io.File imageFile = new java.io.File(imageFolderFragments.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "fragment_" + i + ".png");
				try {
					image = imageGenerator.generateImage(candidate.getPrecursorMolecule(), molecule.getMatchList().getElement(i).getBestMatchedFragment());
					ImageIO.write(image, "png", imageFile); 
				} catch (Exception e) {
					System.err.println("error generating fragment image");
				}
				FragmentMassToPeakMatch match = (FragmentMassToPeakMatch)molecule.getMatchList().getElement(i);
				match.getBestMatchedFragment();
				this.currentFragments.add(
						new Fragment(match.getModifiedFormulaHtmlStringOfBestMatchedFragment(candidate.getPrecursorMolecule()),
								MathTools.round(match.getBestMatchFragmentMass()),
								"/files/" + sessionId + "/images/fragments/fragment_" + i + ".png",
								match.getMatchedPeak().getMass(), this.currentFragments.size() + 1));
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("error generating fragment image for " + molecule.getIdentifier() + " " + i);
				continue;
			}
		}
		try {
			candidate.resetPrecursorMolecule();
		} catch (Exception e1) {
			System.err.println("error when initialising precursor for fragment generation");
			e1.printStackTrace();
			return;
		}
	}
	
	public boolean isCurrentFragmentsAvailable() {
		return this.currentFragments == null || this.currentFragments.size() == 0 ? false : true;
	}
	
	public java.util.Vector<Fragment> getCurrentFragments() {
		return this.currentFragments;
	}

	protected boolean generateFragmentsModelView(MetFragResult molecule) {
		this.fragmentsModel = new LineChartModel();
		this.numberMatchPeaksOfSelectedMolecule = 0;
		if(this.processedPeaklistObject == null) {
			System.out.println("peaklist object is not initialised");
			return false;
		}
		try {
			if(this.processedPeaklistObject == null) throw new Exception();
			double maxMZ = ((TandemMassPeak)this.processedPeaklistObject.getElement(this.processedPeaklistObject.getNumberElements() - 1)).getMass();
			this.fragmentsModel.getAxis(AxisType.Y).setMin(0);
			this.fragmentsModel.getAxis(AxisType.Y).setMax(1050);
			this.fragmentsModel.getAxis(AxisType.Y).setLabel("Intensity");
			this.fragmentsModel.getAxis(AxisType.Y).setTickInterval("250");
			this.fragmentsModel.getAxis(AxisType.Y).setTickCount(5);
			this.fragmentsModel.getAxis(AxisType.X).setMin(0.0);
			this.fragmentsModel.getAxis(AxisType.X).setLabel("m/z");
			this.fragmentsModel.getAxis(AxisType.X).setTickFormat("%.2f");
			this.fragmentsModel.getAxis(AxisType.X).setTickAngle(-30);
			this.fragmentsModel.setZoom(true);
			this.fragmentsModel.setMouseoverHighlight(true);
			this.fragmentsModel.setShowDatatip(false);
			this.fragmentsModel.setShowPointLabels(false);
			this.fragmentsModel.setExtender("fragmentsViewExtender");
			String xTickInterval = "100.000";
			if(maxMZ <= 400) xTickInterval = "50.000";
			if(maxMZ <= 150) xTickInterval = "10.000"; 
			this.fragmentsModel.getAxis(AxisType.X).setTickInterval(xTickInterval);
			String seriesColors = "";
			java.util.Vector<Integer> explainedPeakIDs = new java.util.Vector<Integer>();
			for(int i = 0; i < molecule.getMatchList().getNumberElements(); i++)
				explainedPeakIDs.add(molecule.getMatchList().getElement(i).getMatchedPeak().getID());
			java.util.Vector<LineChartSeries> nonMatchPeaks = new java.util.Vector<LineChartSeries>();
			java.util.Vector<LineChartSeries> nonUsedPeaks = new java.util.Vector<LineChartSeries>();
			for(int i = 0; i < this.processedPeaklistObject.getNumberElements(); i++) 
			{
				TandemMassPeak peak = (TandemMassPeak)this.processedPeaklistObject.getElement(i);
				LineChartSeries newSeries = new LineChartSeries();
				newSeries.set(peak.getMass() + 0.0000001, -10000000.0);
				newSeries.set(peak.getMass(), peak.getRelativeIntensity());
				int mode = this.beanSettingsContainer.getMode() == 1000 || this.beanSettingsContainer.getMode() == -1000 ? 0 : this.beanSettingsContainer.getMode();
				int modeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(mode);
				if(explainedPeakIDs.contains(peak.getID())) {
					this.numberMatchPeaksOfSelectedMolecule++;
					seriesColors += "66cc66,"; 
					this.fragmentsModel.addSeries(newSeries);
				}
				else if(peak.getMass() > Double.parseDouble(this.beanSettingsContainer.getNeutralMonoisotopicMass()) - 5.0 + Constants.ADDUCT_MASSES.get(modeIndex)) 
					nonUsedPeaks.add(newSeries);
				else
					nonMatchPeaks.add(newSeries);
			}
			for(int i = 0; i < nonMatchPeaks.size(); i++) {
				seriesColors += "00749f,";
				this.fragmentsModel.addSeries(nonMatchPeaks.get(i));
			}
			for(int i = 0; i < nonUsedPeaks.size(); i++) {
				seriesColors += "808080,";
				this.fragmentsModel.addSeries(nonUsedPeaks.get(i));
			}
			seriesColors = seriesColors.substring(0, seriesColors.length() - 1);
			this.fragmentsModel.setSeriesColors(seriesColors);
		}
		catch(Exception e) {
			e.printStackTrace();
			this.fragmentsModel = new LineChartModel();
			return false;	
		}

		return true;
	}
	
	public LineChartModel getFragmentsModelView() {
		return this.fragmentsModel;
	}
	
	public void fragmentsViewItemSelect(ItemSelectEvent event) {
		if(this.numberMatchPeaksOfSelectedMolecule <= event.getSeriesIndex()) return;
		RequestContext.getCurrentInstance().execute(
				"var container = $('#fragmentsViewForm').children('span').eq(0).children('div').eq(1).children('div').eq(1);" +
				"var scrollTo = container.children('ul').eq(0).children('li').eq(" + event.getSeriesIndex() + ");" +
				"container.animate({scrollTop: scrollTo.offset().top - container.offset().top + container.scrollTop()});");
	}
	
	public java.util.List<SelectItem> getAvailableExplainedPeaks() {
		java.util.ArrayList<SelectItem> peaks = new java.util.ArrayList<SelectItem>();
		if(this.processedPeaklistObject == null) return peaks;
		for(int i = 0; i < this.processedPeaklistObject.getNumberElements(); i++) {
			Double currentMass = ((TandemMassPeak)this.processedPeaklistObject.getElement(i)).getMass();
			peaks.add(new SelectItem(((TandemMassPeak)this.processedPeaklistObject.getElement(i)).getID(), String.valueOf(MathTools.round(currentMass))));
		}
		return peaks;
	}
	
	public void setExplainedPeaksFilter(Integer[] explainedPeaksFilter) {
		this.explainedPeaksFilter = explainedPeaksFilter;
	}

	public Integer[] getExplainedPeaksFilter() {
		return this.explainedPeaksFilter;
	}
	
	public void filterMetFragResultsByExplainedPeaksListener(ActionEvent event) {
		this.filteredMetFragResultsContainer = new MetFragResultsContainer();

		this.filteredMetFragResultsContainer.setNumberPeaksUsed(this.metFragResultsContainer.getNumberPeaksUsed());
		this.filteredMetFragResultsContainer.setCompoundNameAvailable(this.metFragResultsContainer.isCompoundNameAvailable());
		this.filteredMetFragResultsContainer.setSimScoreAvailable(this.metFragResultsContainer.isSimScoreAvailable());
		
		if(this.explainedPeaksFilter == null) this.explainedPeaksFilter = new Integer[0];
		for(int i = 0; i < this.metFragResultsContainer.getMetFragResults().size(); i++) {
			MatchList matchlist = this.metFragResultsContainer.getMetFragResults().get(i).getMatchList();
			boolean containsPeaks = true;
			for(int j = 0; j < this.explainedPeaksFilter.length; j++) {
				int index = matchlist.getIndexOfPeakID(this.explainedPeaksFilter[j]);
				if(index == -1) {
					containsPeaks = false;
					break;
				}
			}
			if(containsPeaks) 
				this.metFragResultsContainer.getMetFragResults().get(i).setFiltered(false);
			else 
				this.metFragResultsContainer.getMetFragResults().get(i).setFiltered(true);
		}
		for(int i = 0; i < this.metFragResultsContainer.getMetFragResults().size(); i++)
			if(!this.metFragResultsContainer.getMetFragResults().get(i).isFiltered()) 
				this.filteredMetFragResultsContainer.addMetFragResult(this.metFragResultsContainer.getMetFragResults().get(i));
		
		int numberCandidates = this.filteredMetFragResultsContainer.getMetFragResults().size();
		String message = numberCandidates + (numberCandidates == 1 ? " Entry" : " Entries");
		this.infoMessages.setMessage("filterCompoundsInfo", message);
		this.generateScoreDistributionModelView();
		 
		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:MetFragResultsTable");
		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:resultsTablePanel");
		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:statisticsPanel");
		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion:statistics");
		if(!this.isScoreDistributionModelAvailable()) RequestContext.getCurrentInstance().execute("PF('mainAccordion').unselect(3)");
		RequestContext.getCurrentInstance().update("mainForm:mainAccordion:MetFragResultsTable");
		RequestContext.getCurrentInstance().update("mainForm:mainAccordion:peakFilterPanel");
		RequestContext.getCurrentInstance().update("mainForm:mainAccordion:scoreDistributionPlot");
		//RequestContext.getCurrentInstance().update("mainForm:mainAccordion");
	}
	
	//results download
	public void downloadResultsListenerCSV(ActionEvent actionEvent) {
		this.createResultsToDownload("csv");
	}

	public void downloadResultsListenerSDF(ActionEvent actionEvent) {
		this.createResultsToDownload("sdf");
	}

	public void downloadResultsListenerXLS(ActionEvent actionEvent) {
		this.createResultsToDownload("xls");
	}
	
	public org.primefaces.model.StreamedContent createResultsToDownload(String format) {
		return this.beanSettingsContainer.getUserOutputDataHandler().createResultsToDownload(this.filteredMetFragResultsContainer, format, this.errorMessages);
	}
	
	/*
	 * error and info handling
	 */
	public String getErrorMessage(String id) {
		return this.errorMessages.getMessage(id);
	}

	public boolean isErrorMessage(String id) {
		return this.errorMessages.containsKey(id);
	}

	public String getInfoMessage(String id) {
		return this.infoMessages.getMessage(id);
	}

	public boolean isInfoMessage(String id) {
		return this.infoMessages.containsKey(id);
	}
	
	/**
	 * response to idle monitor when session is over
	 */
	public void viewExpiredListener() {
		System.out.println("session expired");
		FacesContext fc = FacesContext.getCurrentInstance();
        java.util.Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
        javax.faces.application.NavigationHandler nav =
                fc.getApplication().getNavigationHandler();
        // Push some useful stuff to the request scope for
        // use in the page
        requestMap.put("currentViewId", FacesContext.getCurrentInstance().getViewRoot().getViewId());

        nav.handleNavigation(fc, null, "viewExpired");
        fc.renderResponse();
        
        RequestContext.getCurrentInstance().execute("PF('expiredSessionDialog').show();");

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        HttpSession session = request.getSession(false);
        session.invalidate();
	}
	
	/*
	 * feedback
	 */
	protected String feedbackComment;
	protected String feedbackEmail;
	protected String dataStorePermission;
    protected String feedbackType;
	protected boolean isFeedbackDialogVisible; 
	
	public String getFeedbackComment() {
		return this.feedbackComment;
	}

	public void setFeedbackComment(String feedbackComment) {
		this.feedbackComment = feedbackComment;
	}

	public String getFeedbackEmail() {
		return this.feedbackEmail;
	}

	public void setFeedbackEmail(String feedbackEmail) {
		this.feedbackEmail = feedbackEmail;
	}

	public String getDataStorePermission() {
		return this.dataStorePermission;
	}

	public void setDataStorePermission(String dataStorePermission) {
		this.dataStorePermission = dataStorePermission;
	}

	public void setFeedbackType(String feedbackType) {
        this.feedbackType = feedbackType;
    }

	public String getFeedbackType() {
        if(this.feedbackType == null || this.feedbackType.length() == 0)
        	this.feedbackType = "issue";
        return this.feedbackType;
    }
	
	public void feedbackButtonListener(ActionEvent actionEvent) {
		this.infoMessages.removeKey("feedbackSubmitButtonInfo");
    	this.errorMessages.removeKey("selectPermitDataStoreError");
    	this.errorMessages.removeKey("selectTypeError");
    	this.errorMessages.removeKey("inputCommentError");
    	this.errorMessages.removeKey("inputEmailError");
		this.isFeedbackDialogVisible = true;
	}

	public void feedbackButton() {
		this.isFeedbackDialogVisible = true;
	}
	
	public boolean isFeedbackDialogVisible() {
		return this.isFeedbackDialogVisible;
	}
	
	public void closeFeedbackDialogListener(ActionEvent event) {
		this.infoMessages.removeKey("feedbackSubmitButtonInfo");
    	this.errorMessages.removeKey("selectPermitDataStoreError");
    	this.errorMessages.removeKey("selectTypeError");
    	this.errorMessages.removeKey("inputCommentError");
    	this.errorMessages.removeKey("inputEmailError");
		this.isFeedbackDialogVisible = false;
	}
	
	protected boolean checkFeedbackSettings() {
		this.infoMessages.removeKey("feedbackSubmitButtonInfo");
		boolean checksFine = true;
		//email
		if(this.feedbackEmail == null || this.feedbackEmail.length() == 0) {
			this.errorMessages.setMessage("inputEmailError", "Please provide a valid email address.");
			checksFine = false;
		}
	    else { 
	    	this.errorMessages.removeKey("inputEmailError");
			if(!Settings.checkEmailAddress(this.feedbackEmail)) {
				this.errorMessages.setMessage("inputEmailError", "Please provide a valid email address.");
		    	checksFine = false;
		    }
		    else 
		    	this.errorMessages.removeKey("inputEmailError");
	    }
	    //comment
	    	//nothing to check
	    //dataStorePermission
			//nothing to check
	    return checksFine;
	}
	
	/**
	 * 
	 * @param actionEvent
	 */
	public void submitFeedbackListener(ActionEvent actionEvent) {
		this.infoMessages.removeKey("feedbackSubmitButtonInfo");
		if(!this.checkFeedbackSettings()) return;

		java.io.File zipFileToAttach = null;
		java.io.File logFileToAttach = null;
		java.io.File sessionFileToAttach = null;
		String ls = Constants.OS_LINE_SEPARATOR;
		String addMsg = "StoreData: " + this.dataStorePermission + ls;
		if(this.dataStorePermission.equals("yes")) {
			java.io.File feedbackFolder = new java.io.File(this.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "feedback");
			if(feedbackFolder.exists())
				try {
					FileUtils.deleteDirectory(feedbackFolder);
				} catch (IOException e1) {
					this.errorMessages.setMessage("feedbackSubmitButtonError", "Error: Feedback could not be sent.");
					return;
				}
			feedbackFolder.mkdirs();
			FileStorer fileStorer = new FileStorer();
			try {
				StreamedContent zipContent = this.beanSettingsContainer.getUserOutputDataHandler().getDownloadParameters(this.errorMessages, null);
				zipFileToAttach = fileStorer.saveUploadedFile(zipContent.getStream(), feedbackFolder.getAbsoluteFile(), "MetFragWeb_Parameters.zip");
				addMsg += "Zip: yes" + ls;
			}
			catch(Exception e) {
				addMsg += "Zip: no " + ls;
			}
			try {
				logFileToAttach = this.beanSettingsContainer.getSettingsLogFile(this.getRootSessionFolder() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "feedback", "log.txt");
				addMsg += "Log: yes" + ls;
			}
			catch(Exception e) {
				addMsg += "Log: no" + ls;
			}
			try {
				sessionFileToAttach = fileStorer.compressFolder(this.getRootSessionFolder(), this.getRootSessionFolder() 
						+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "feedback" 
						+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "MetFragWebSession.zip", new String[] {".*feedback.*", ".*\\.zip$", ".*\\.png$"}, "MetFragWeb" 
						+ Constants.OS_SPECIFIC_FILE_SEPARATOR + "files" + Constants.OS_SPECIFIC_FILE_SEPARATOR);
				addMsg += "Session: yes" + ls;
			} catch(Exception e) {
				e.printStackTrace();
				addMsg += "Session: no" + ls;
			}
		}
		else {
			addMsg += "Zip: no " + ls;
			addMsg += "Log: no" + ls;
			addMsg += "Session: no" + ls;
		}
		try {
			MetFragGlobalSettings emailSettings = this.beanSettingsContainer.readDatabaseConfigFromFile();
			MultiPartEmail email = new MultiPartEmail();
			email.setSubject("MetFrag Feedback");
			if(emailSettings.containsKey(VariableNames.FEEDBACK_EMAIL_HOST)) email.setHostName((String)emailSettings.get(VariableNames.FEEDBACK_EMAIL_HOST));
			if(emailSettings.containsKey(VariableNames.FEEDBACK_EMAIL_PORT)) email.setSmtpPort((Integer)emailSettings.get(VariableNames.FEEDBACK_EMAIL_PORT));
			if(emailSettings.containsKey(VariableNames.FEEDBACK_EMAIL_TO)) {
				email.setFrom((String)emailSettings.get(VariableNames.FEEDBACK_EMAIL_TO));
				email.addTo((String)emailSettings.get(VariableNames.FEEDBACK_EMAIL_TO));
			}
			if(emailSettings.containsKey(VariableNames.FEEDBACK_EMAIL_USER) && emailSettings.containsKey(VariableNames.FEEDBACK_EMAIL_PASS)) {
				email.setAuthentication((String)emailSettings.get(VariableNames.FEEDBACK_EMAIL_USER), (String)emailSettings.get(VariableNames.FEEDBACK_EMAIL_PASS));
			}
			email.setStartTLSRequired(true);
			email.addReplyTo(this.feedbackEmail);
			email.setMsg("From: " + this.feedbackEmail + ls 
					+ "Type: " + this.feedbackType + ls 
					+ addMsg + ls + ls 
					+ this.feedbackComment);
			if(zipFileToAttach != null) {
				EmailAttachment attachment = new EmailAttachment();
				attachment.setPath(zipFileToAttach.getAbsolutePath());
				attachment.setDisposition(EmailAttachment.ATTACHMENT);
				attachment.setDescription("MetFragWeb parameters");
				attachment.setName("MetFragWeb_Parameters.zip");
				email.attach(attachment);
			}
			if(logFileToAttach != null) {
				EmailAttachment attachment = new EmailAttachment();
				attachment.setPath(logFileToAttach.getAbsolutePath());
				attachment.setDisposition(EmailAttachment.ATTACHMENT);
				attachment.setDescription("MetFragWeb log");
				attachment.setName("log.txt");
				email.attach(attachment);
			}
			if(sessionFileToAttach != null) {
				EmailAttachment attachment = new EmailAttachment();
				attachment.setPath(sessionFileToAttach.getAbsolutePath());
				attachment.setDisposition(EmailAttachment.ATTACHMENT);
				attachment.setDescription("Session folder");
				attachment.setName("session.zip");
				email.attach(attachment);
			}
			email.send();
		} catch(Exception e) {
			this.errorMessages.setMessage("feedbackSubmitButtonError", "Error: Feedback could not be sent.");
			return;
		}
		this.infoMessages.setMessage("feedbackSubmitButtonInfo", "Feedback was sent successfully. Thank you!");
		this.feedbackComment = "";
		this.feedbackEmail = "";
		this.feedbackType = "issue";
		this.dataStorePermission = "no"; 
	}
	
	public String getServerName() {
        String hostname = "";
        try {
                hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
                e.printStackTrace();
                return "";
        }
        return hostname;
	}
	
	public String getServerPlusVersion() {
		return this.getServerName() + " " + this.version;
	}
}


