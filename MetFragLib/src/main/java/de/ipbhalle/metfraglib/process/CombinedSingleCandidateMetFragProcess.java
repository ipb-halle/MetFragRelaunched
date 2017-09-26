package de.ipbhalle.metfraglib.process;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.collection.PreProcessingCandidateFilterCollection;
import de.ipbhalle.metfraglib.fragmenterassignerscorer.AbstractFragmenterAssignerScorer;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class CombinedSingleCandidateMetFragProcess implements Runnable {
	
	//usually this array is of size 1
	//for special cases additional candidates may be generated out of one
	private ICandidate[] scoredPrecursorCandidates;
	//pre-processing candidate filter collection
	//used to check before the processing of a candidate whether it fulfills all criteria to be processed 
	private PreProcessingCandidateFilterCollection preProcessingCandidateFilterCollection;
	//if true candidate was processed successfully if not an error might have occured
	private boolean wasSuccessful;
	//reference to settings object
	private Settings settings;
	
	private Logger logger = Logger.getLogger(CombinedSingleCandidateMetFragProcess.class);
	
	//fragments the candidate, assignes fragments to m/z peaks and scores
	private AbstractFragmenterAssignerScorer fas;
	
	/**
	 * each candidate processing thread has its own settings object and the candidate to process
	 * 
	 * @param settings
	 * @param candidate
	 */
	public CombinedSingleCandidateMetFragProcess(Settings settings, ICandidate candidate) {
		this.settings = settings;
		this.logger.setLevel((Level)this.settings.get(VariableNames.LOG_LEVEL_NAME));
		this.scoredPrecursorCandidates = new ICandidate[] {candidate};
	}
	
	/**
	 * runs the single candidate metfrag process
	 * the actual processing is done with the AbstractFragmenterAssignerScorer object
	 */
	public void run() {
		this.settings.set(VariableNames.CANDIDATE_NAME, this.scoredPrecursorCandidates[0]);
		
		try {
			//define the fragmenterAssignerScorer
			this.fas = (AbstractFragmenterAssignerScorer) Class.forName((String)this.settings.get(VariableNames.METFRAG_ASSIGNER_SCORER_NAME)).getConstructor(Settings.class, ICandidate.class).newInstance(this.settings, this.scoredPrecursorCandidates[0]);
			//sets the candidate to be processed
			this.fas.setCandidate(this.scoredPrecursorCandidates[0]);
			//inits the candidate, fragmenter, scores objects
			this.fas.initialise();
		} catch (Exception e) {
			//if there's an error processing fails
			String errorMessage = e.getMessage();
			if(errorMessage == null && e.getCause() != null) 
				errorMessage = e.getCause().getMessage();
			if(logger.isDebugEnabled()) {
				logger.debug(this.scoredPrecursorCandidates[0].getIdentifier() + " discarded reasoned by -> " + errorMessage);
			}
			if(logger.isTraceEnabled()) {
				e.printStackTrace();
			}
			ProcessingStatus processCandidates = (ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
			processCandidates.increaseNumberErrorCandidates();
			processCandidates.checkNumberFinishedCandidates();
			this.shallowNullify();
			return;
		}
		/*
		 * check pre-filter
		 */
		boolean passesFilter = true;
		try {
			passesFilter = this.preProcessingCandidateFilterCollection.passesFilter(this.scoredPrecursorCandidates[0]);
		} catch(Exception e) {
			e.printStackTrace();
			passesFilter = false;
		}
		if(!passesFilter) {
			if(logger.isDebugEnabled()) {
				logger.debug(this.scoredPrecursorCandidates[0].getIdentifier() + " discarded due to pre-filtering");
			}
			ProcessingStatus processCandidates = (ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME);
			processCandidates.increaseNumberPreFilteredCandidates();
			processCandidates.checkNumberFinishedCandidates();
			this.shallowNullify();
			return;
		}
		
		/*
		 * generate fragments
		 */
		if(logger.isTraceEnabled()) {
			logger.trace(this.getClass().getName());
			logger.trace("\tprocessing " + this.scoredPrecursorCandidates[0].getIdentifier());
			logger.trace("\t\tgenerating fragments");
		}
		/*
		 * do the actual work
		 * fragment candidate, assign fragments and score
		 */
		this.fas.calculate();
		//removing score assignment and shifted to CombinedMetFragProcess after postCalculating scores
		this.fas.assignInteremScoresResults();
		//set the reference to the scored candidate(s)
		this.scoredPrecursorCandidates = this.fas.getCandidates();
		
		if(logger.isTraceEnabled()) {
			logger.trace("\t\tcleaning candidates");
		}
		
		if(logger.isDebugEnabled()) {
			logger.debug(this.scoredPrecursorCandidates[0].getIdentifier() + " finished");
		}

		((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).checkNumberFinishedCandidates();
		//this.shallowNullify();
		//this.scoredPrecursorCandidates[0].nullify();
		this.fas.shallowNullify();
		this.scoredPrecursorCandidates[0].resetPrecursorMolecule();
		this.wasSuccessful = true;
	}
	
	public void singlePostCalculateScores() throws Exception {
		this.fas.singlePostCalculateScore();
	}
	
	public void assignScores() {
		this.fas.assignScores();
	}
	
	public AbstractFragmenterAssignerScorer getFragmenterAssignerScorer() {
		return this.fas;
	}

	public ICandidate[] getScoredPrecursorCandidates() {
		return this.scoredPrecursorCandidates;
	}
	
	public ICandidate getScoredPrecursorCandidate() {
		return this.scoredPrecursorCandidates[0];
	}
	
	public boolean wasSuccessful() {
		return this.wasSuccessful;
	}
	
	public void setPreProcessingCandidateFilterCollection(PreProcessingCandidateFilterCollection preProcessingCandidateFilterCollection) {
		this.preProcessingCandidateFilterCollection = preProcessingCandidateFilterCollection;
	}

	public void shallowNullify() {
		if(this.scoredPrecursorCandidates != null) {
			for(int i = 0; i < this.scoredPrecursorCandidates.length; i++) {
				if(this.scoredPrecursorCandidates[i] != null) {
					this.scoredPrecursorCandidates[i].shallowNullify();
					this.scoredPrecursorCandidates[i] = null;
				}
			}
		}
		if(this.fas != null) this.fas.shallowNullify();
	}
	
	public void nullify() {
		if(this.scoredPrecursorCandidates != null) {
			for(int i = 0; i < this.scoredPrecursorCandidates.length; i++) {
				if(this.scoredPrecursorCandidates[i] != null) {
					this.scoredPrecursorCandidates[i].nullify();
					this.scoredPrecursorCandidates[i] = null;
				}
			}
		}
		if(this.fas != null) this.fas.nullify();
	}
	
}
