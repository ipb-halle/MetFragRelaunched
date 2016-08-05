package de.ipbhalle.metfragweb.helper;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;

import de.ipbhalle.metfraglib.imagegenerator.StandardSingleStructureImageGenerator;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.datatype.Molecule;
import de.ipbhalle.metfragweb.datatype.ScoreSummary;
import de.ipbhalle.metfragweb.datatype.Weight;

public class ProcessCompoundsThreadRunner extends ThreadRunner {

	protected MetFragResultsContainer metFragResultsContainer;
	protected String sessionId;
	protected String rootSessionPath;
	protected ArrayList<Weight> weights;
	protected ArrayList<SelectItem> availableScoreNames;
	protected ArrayList<SelectItem> availableScoreNamesForScoreGraph;
	protected int renderedMoleculesNextPercentageValue;
	protected int renderedMoleculesPercentageValue;
	protected boolean renderingMolecules;
	
	public ProcessCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages, String sessionId, String rootSessionPath) {
		super(beanSettingsContainer, infoMessages, errorMessages);
		this.sessionId = sessionId;
		this.rootSessionPath = rootSessionPath;
		this.renderedMoleculesNextPercentageValue = 1;
		this.renderedMoleculesPercentageValue = 0;
		this.renderingMolecules = false;
	}
	
	@Override
	public void run() {
		System.out.println("ProcessCompoundsThreadRunner run");
		this.infoMessages.removeKey("processingErrorCandidatesInfo");
		this.infoMessages.removeKey("processingFilteredCandidatesInfo");
		this.infoMessages.removeKey("processingProcessedCandidatesInfo");
		//prepare settings object
		try {
			this.beanSettingsContainer.prepareSettingsObject();
		} catch (Exception e) {
			e.printStackTrace();
			this.errorMessages.setMessage("buttonProcessCompoundsError", "Error: Setting parameters failed.");
			return;
		}
		String[] scoresNotToScale = (String[])this.beanSettingsContainer.getMetFragSettings().get(VariableNames.SCORE_NAMES_NOT_TO_SCALE);
		this.errorMessages.removeKey("buttonProcessCompoundsError");
		this.beanSettingsContainer.getMetFragSettings().set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, "de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		this.beanSettingsContainer.renewMetFragProcessSettings(this.beanSettingsContainer.getMetFragSettings());
		
		//start process in a new thread
		try {
			this.beanSettingsContainer.startCandidateProcessing();
		} catch (Exception e) {
			e.printStackTrace();
			this.errorMessages.setMessage("buttonProcessCompoundsError", "Error: Processing of candidates failed.");
			return;
		}
		
		if(this.interrupted) return; 
		
		//wait until poll is triggered to display 100% in the progress bar for candidate processing
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		this.beanSettingsContainer.setProcessCompoundsDialogHeader("Rendering Results");
		this.renderingMolecules = true;
		SortedScoredCandidateList scoredCandidateList = null; 
		try {
			scoredCandidateList = (SortedScoredCandidateList)this.beanSettingsContainer.getCurrentCandidateList();
		} catch(Exception e) {
			return;
		}
		/*
		 * setting infos
		 */
		
		int numberErrors = this.beanSettingsContainer.getNumberErrorCandidates();
		int numberFiltered = this.beanSettingsContainer.getNumberPreFilteredCandidates();
		String candidates = numberErrors == 1 ? "Candidate" : "Candidates";
		if(numberErrors != 0) 
			this.infoMessages.setMessage("processingErrorCandidatesInfo", numberErrors + " " + candidates + " with errors");
		candidates = numberFiltered == 1 ? "Candidate" : "Candidates";
		if(numberFiltered != 0) 
			this.infoMessages.setMessage("processingFilteredCandidatesInfo", numberFiltered + " " + candidates + " filtered out");
		candidates = scoredCandidateList.getNumberElements() == 1 ? "Candidate" : "Candidates";
		this.infoMessages.setMessage("processingProcessedCandidatesInfo", scoredCandidateList.getNumberElements()  + " " + candidates + " processed");
		
		/*
		 * settings weights and scores
		 */
		String[] scoreNames = (String[])this.beanSettingsContainer.getMetFragSettings().get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		double[] maxScore = new double[scoreNames.length]; 
		java.util.Vector<String> additionalScoreNames = new java.util.Vector<String>();

		this.metFragResultsContainer = new MetFragResultsContainer();
		//prepare initial weights
		this.weights = new ArrayList<Weight>();
		this.availableScoreNames = new ArrayList<SelectItem>();
		this.availableScoreNamesForScoreGraph = new ArrayList<SelectItem>();
		boolean[] enableScoreScalling = new boolean[scoreNames.length];
		Integer simScoreIndex = null;
		int scoreForGraphNumber = 0;
		for (int i = 0; i < scoreNames.length; i++) {
			String end = "th";
    		if(scoreForGraphNumber == 0) end = "st";
    		if(scoreForGraphNumber == 1) end = "nd";
    		if(scoreForGraphNumber == 2) end = "rd";
    		String name = this.getWeightDisplayName(scoreNames[i]);
			if(this.isScoreAvailableForGraph(scoreNames[i])) {
				this.availableScoreNamesForScoreGraph.add(new SelectItem(i, name));
			}
			if(scoreNames[i].equals("SimScore")) {
				this.metFragResultsContainer.setSimScoreAvailable(true);
				simScoreIndex = new Integer(i);
			}
			else {
	    		this.weights.add(new Weight(name + " (" + (scoreForGraphNumber + 1) + end + ")" , 100));
	    		this.availableScoreNames.add(new SelectItem(i, name));
				scoreForGraphNumber++;
			}
			enableScoreScalling[i] = true;
    		for(int k = 0; k < scoresNotToScale.length; k++) {
    			if(scoresNotToScale[k].equals(scoreNames[i])) {
    				enableScoreScalling[i] = false;
    				break;
    			}
    		}
		}

		//get maximum scores
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++)
			for(int j = 0; j < scoreNames.length; j++) {
				Double currentScore = 0.0;
				try {
					currentScore = (Double)scoredCandidateList.getElement(i).getProperty(scoreNames[j]);
				} catch(Exception e1) {
					try {
						currentScore = Double.parseDouble((String)scoredCandidateList.getElement(i).getProperty(scoreNames[j]));
						scoredCandidateList.getElement(i).setProperty(scoreNames[j], currentScore);
					}
					catch(Exception e2) {
						//leave score as zero
					}
				}
				if(currentScore > maxScore[j]) 
					maxScore[j] = currentScore;
			}
		for(int j = 0; j < maxScore.length; j++) 
			if(maxScore[j] == 0 || !enableScoreScalling[j]) maxScore[j] = 1.0;
		//generate necessary folders
		java.io.File imageFolderCandidates = new java.io.File(this.rootSessionPath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "images/candidates");
		java.io.File imageFolderFragments = new java.io.File(this.rootSessionPath + Constants.OS_SPECIFIC_FILE_SEPARATOR + "images/fragments");
		try {
			if(imageFolderCandidates.exists()) FileUtils.deleteDirectory(imageFolderCandidates);
			if(imageFolderFragments.exists()) FileUtils.deleteDirectory(imageFolderFragments);
		} 
		catch(Exception e) {
			this.metFragResultsContainer = new MetFragResultsContainer();
			this.errorMessages.setMessage("buttonProcessCompoundsError", "Error: Rendering of candidates failed.");
			return;
		}
		imageFolderCandidates.mkdirs();
		imageFolderFragments.mkdirs();
		
		//define image generator
		StandardSingleStructureImageGenerator imageGenerator = new StandardSingleStructureImageGenerator();
		imageGenerator.setImageHeight(300);
		imageGenerator.setImageWidth(300);
		java.util.HashMap<String, MetFragResult> metFragResults = new java.util.HashMap<String, MetFragResult>();
		int currentKey = 1;
		if(scoredCandidateList != null)
			this.metFragResultsContainer.setNumberPeaksUsed(scoredCandidateList.getNumberPeaksUsed());
		int index = 0;
		for(int i = 0; i < scoredCandidateList.getNumberElements(); i++) {
			if(this.interrupted) {
				this.renderingMolecules = false;
				return; 
			}
			this.checkMoleculeRenderingStatus(i, scoredCandidateList.getNumberElements());
			RenderedImage renderedImage = imageGenerator.generateImage(scoredCandidateList.getElement(i));
			ICandidate candidate = scoredCandidateList.getElement(i);
			java.io.File imageFile = new java.io.File(imageFolderCandidates.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + candidate.getIdentifier() + ".png");
			try {
				ImageIO.write(renderedImage, "png", imageFile);
			} catch (IOException e1) {
				System.err.println("Could not generate image of id " + candidate.getIdentifier());
			}
			ScoreSummary[] scoreSummaries = new ScoreSummary[scoreNames.length];
			for(int j = 0; j < scoreSummaries.length; j++) {
				double score = 0.0;
				double rawScore = 0.0;
				try {
					score = (Double)candidate.getProperty(scoreNames[j]); 
					rawScore = score;
					score /= maxScore[j];
				}
				catch(Exception e) {
					score = 0.0;
					rawScore = 0.0;
				}
				scoreSummaries[j] = new ScoreSummary(this.getWeightDisplayName(scoreNames[j]), score, rawScore);
				if(!this.isScoreAvailableForScore(scoreNames[j])) scoreSummaries[j].setUsedForScoring(false);
				if(!this.isScoreAvailableForGraph(scoreNames[j])) scoreSummaries[j].setUsedForGraph(false);
			}
			double[] additionalScores = new double[additionalScoreNames.size()];
			for(int j = 0; j < additionalScoreNames.size(); j++) {
				additionalScores[j] = (Double)candidate.getProperty(additionalScoreNames.get(j)); 
			}
			String identifier = candidate.getIdentifier();
			Double mass = (Double)candidate.getProperty(VariableNames.MONOISOTOPIC_MASS_NAME);
			String formula = (String)candidate.getProperty(VariableNames.MOLECULAR_FORMULA_NAME);
			Molecule mol = new Molecule(identifier, mass, formula, 
					this.weights, "/files/" + this.sessionId + "/images/candidates/" + candidate.getIdentifier() + ".png", 
					scoreSummaries, candidate.getInChI());
			if(simScoreIndex != null) mol.setSimScoreIndex(simScoreIndex);
			mol.setDatabaseName(this.beanSettingsContainer.getDatabase());
			if(candidate.getProperties().containsKey(VariableNames.COMPOUND_NAME_NAME)) {
				mol.setName((String)candidate.getProperty(VariableNames.COMPOUND_NAME_NAME));
				if(!this.metFragResultsContainer.isCompoundNameAvailable()) this.metFragResultsContainer.setCompoundNameAvailable(true);
			} else {
				mol.setName("");
			}
			//save matches
			int countExplainedPeaks = 0;
			if(candidate.getMatchList() != null) {
				MatchList matchList = candidate.getMatchList();
				for(int l = 0; l < matchList.getNumberElements(); l++) {
					try {
						matchList.getElement(l).getMatchedPeak().getIntensity();
					}
					catch(Exception e1) {
						continue;
					}
					countExplainedPeaks++;
				}
				mol.setMatchList(matchList);
			}
			mol.setNumberPeaksExplained(countExplainedPeaks);
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_1_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null) {
				String currentInChI = (String)candidate.getProperties().get(VariableNames.INCHI_KEY_1_NAME);
				if(metFragResults.containsKey(currentInChI)) 
					metFragResults.get(currentInChI).addMolecule(mol);
				else {
					metFragResults.put(currentInChI, new MetFragResult(mol, currentInChI, index));
					index++;
				}
			}
			else {
				metFragResults.put(String.valueOf(currentKey), new MetFragResult(mol, String.valueOf(currentKey), index));
				index++;
				currentKey++;
			}
		}
		java.util.Iterator<String> it = metFragResults.keySet().iterator();
		while(it.hasNext())
			this.metFragResultsContainer.addMetFragResultScoreSorted(metFragResults.get(it.next()));
		//wait until poll is triggered to display 100% in the progress bar for image rendering
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.renderingMolecules = false;
	}
	
	protected void checkMoleculeRenderingStatus(int numberRenderedMolecules, int numberCandidates) {
		if(((double)(++numberRenderedMolecules) / numberCandidates) * 10.0 >= (double)this.renderedMoleculesNextPercentageValue) {
			int old = this.renderedMoleculesNextPercentageValue;
			this.renderedMoleculesNextPercentageValue = (int)Math.ceil(((double)numberRenderedMolecules / (double)numberCandidates) * 10.0);
			if(old == this.renderedMoleculesNextPercentageValue) this.renderedMoleculesNextPercentageValue++;
			this.renderedMoleculesPercentageValue = ((int)Math.round((((double)numberRenderedMolecules / (double)numberCandidates) * 10.0)) * 10);
		}
	}
	
	public boolean isScoreAvailableForScore(String name) {
		if(name.equals("SimScore")) return false;
		return true;
	}
	
	public boolean isScoreAvailableForGraph(String name) {
		return true;
	}
	
	public String getWeightDisplayName(String realName) {
		if(realName.equals(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME))
			return "MetFrag";
		else if(realName.equals("MetFusionMoNAScore") || realName.equals("OfflineMetFusionScore") || realName.equals("ExactMoNAScore") || realName.equals("IndividualMoNAScore"))
			return "SpectralSimilarityScore";
		else if(realName.equals("ChemSpiderRSCCount"))
			return "RSCCount";
		else if(realName.equals("ChemSpiderReferenceCount"))
			return "ReferenceCount";
		else if(realName.equals("ChemSpiderDataSourceCount"))
			return "DataSourceCount";
		else if(realName.equals("ChemSpiderNumberExternalReferences"))
			return "ExternalReferenceCount";
		else if(realName.equals("ChemSpiderNumberPubMedReferences"))
			return "PubMedReferenceCount";
		else if(realName.equals("PubChemNumberPatents"))
			return "PatentsCount";
		else if(realName.equals("PubChemNumberPubMedReferences"))
			return "PubMedReferenceCount";
		else if(realName.equals("SmartsSubstructureInclusionScore"))
			return "SubstructureInclusionScore";
		else if(realName.equals("SmartsSubstructureExclusionScore"))
			return "SubstructureExclusionScore";
		else if(realName.equals("MatchSpectrumCosineSimilarityScore"))
			return "SimScore";
		return realName;
	}
	
	public boolean isAdditonalScore(String scoreName) {
		if(scoreName.equals("SimScore"))
			return true;
		return false;
	}
	
	public ArrayList<SelectItem> getAvaiableScoreNames() {
		return this.availableScoreNames;
	}

	public ArrayList<SelectItem> getAvaiableScoreNamesForScoreGraph() {
		return this.availableScoreNamesForScoreGraph;
	}
	
	public ArrayList<Weight> getWeights() {
		return this.weights;
	}
	
	public MetFragResultsContainer getResultsContainer() {
		return this.metFragResultsContainer;
	}

	public int getRenderedMoleculesNextPercentageValue() {
		return this.renderedMoleculesNextPercentageValue;
	}

	public int getRenderedMoleculesPercentageValue() {
		return this.renderedMoleculesPercentageValue;
	}

	public boolean isRenderingMolecules() {
		return renderingMolecules;
	}
}
