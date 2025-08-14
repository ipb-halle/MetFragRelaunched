package de.ipbhalle.metfraglib.fragmenterassignerscorer;

import de.ipbhalle.metfraglib.database.OnlinePubChemDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import de.ipbhalle.metfraglib.collection.ScoreCollection;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragmenter.AbstractTopDownFragmenter;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragmenterAssignerScorer;
import de.ipbhalle.metfraglib.interfaces.IScore;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.parameter.ClassNames;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractFragmenterAssignerScorer implements IFragmenterAssignerScorer {

	//matchlist contains all fragment peak matches 
	protected MatchList matchList;
	//settings object
	protected Settings settings;
	//candidate(s) to be processed
	protected ICandidate[] candidates;
	//fragmenter performing in silico fragmention
	protected AbstractTopDownFragmenter fragmenter;
	//score collection containg all scores
	protected ScoreCollection scoreCollection;
	//final scores is weighted sum of all scorecollection scores
	protected double finalScore;

    protected static final Logger logger = LogManager.getLogger();
	
	/**
	 * 
	 * @param settings
	 * @param candidate
	 */
	public AbstractFragmenterAssignerScorer(Settings settings, ICandidate candidate) {
		this.settings = settings;
		this.finalScore = 0;
        Configurator.setLevel(logger.getName(), (Level)this.settings.get(VariableNames.LOG_LEVEL_NAME));
		this.candidates = new ICandidate[] {candidate};
	}
	
	public void nullify() {
		if(this.fragmenter != null) this.fragmenter.nullify();
		if(this.matchList != null) this.matchList.nullify();
		this.fragmenter = null;
		this.matchList = null;
		this.nullifyScoresCollection();
	}

	public void shallowNullify() {
		if(this.fragmenter != null) this.fragmenter.nullify();
		this.fragmenter = null;
		if(this.matchList != null) this.matchList.shallowNullify();
		this.shallowNullifyScoresCollection();
		//this.matchList = null;
	}

	public void shallowNullifyScoresCollection() {
		if(this.scoreCollection != null) this.scoreCollection.shallowNullify();
	}
	
	public void nullifyScoresCollection() {
		if(this.scoreCollection != null) {
			this.scoreCollection.nullify();
		}
		this.scoreCollection = null;
	}
	
	/**
	 * init candidate, fragmenter, scores
	 * 
	 * @throws AtomTypeNotKnownFromInputListException
	 * @throws Exception
	 */
	public void initialise() throws AtomTypeNotKnownFromInputListException, Exception {
		/*
		 * initialise candidate
		 */
		this.candidates[0].initialisePrecursorCandidate();
		/*
		 * initialise fragmenter
		 */
		this.fragmenter = (AbstractTopDownFragmenter) Class.forName((String)settings.get(VariableNames.METFRAG_FRAGMENTER_TYPE_NAME)).getConstructor(Settings.class).newInstance(settings);
		
		/*
		 * initialise score
		 */
		String[] score_types = (String[])this.settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		IScore[] scores = new IScore[score_types.length];
		for(int i = 0; i < score_types.length; i++) {
			logger.debug("\t\tinitialising " + score_types[i]);
			scores[i] = (IScore) Class.forName(ClassNames.getClassNameOfScore(score_types[i])).getConstructor(Settings.class).newInstance(this.settings);
		}
		this.scoreCollection = new ScoreCollection(scores);
	}
	
	public void setCandidate(ICandidate candidate) {		
		this.candidates[0] = candidate;
	}
	
	public abstract void calculate();

	public abstract FragmentList getFragments();

	public void calculateScore() throws Exception {
		this.scoreCollection.calculate();
	}

	public void singlePostCalculateScore() throws Exception {
		this.scoreCollection.singlePostCalculate();
	}
	
	public ScoreCollection getScoreCollection() {
		return this.scoreCollection;
	}

	public ICandidate[] getCandidates() {
		return this.candidates;
	}
	
	public MatchList getMatchList() {
		return this.matchList;
	}

	public void assignScores() {
	//	this.settings.set(VariableNames.MATCH_LIST_NAME, this.matchList);
	//	this.candidates[0].setMatchList(matchList);
		
		/*
		 * generate the result as scored candidate and set the scores as candidate property
		 */
		String[] score_types = (String[])this.settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		for(int i = 0; i < score_types.length; i++) {
			if(scoreCollection.getScore(i).getValue() != null) {
				if(!scoreCollection.getScore(i).isUserDefinedPropertyScore()) {
					this.candidates[0].setProperty(score_types[i], scoreCollection.getScore(i).getValue());
				}
			}
		}
	}
	
	public void assignInterimScoresResults() {
			/*
			 * generate the result as scored candidate and set the scores as candidate property
			 */
			String[] score_types = (String[])this.settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
			for(int i = 0; i < score_types.length; i++) {
				if(scoreCollection.getScore(i).getValue() != null) {
					if(scoreCollection.getScore(i).hasInterimResults() && !scoreCollection.getScore(i).isCandidatePropertyScore()) {
						this.candidates[0].setProperty(score_types[i] + "_Values", scoreCollection.getScore(i).getOptimalValuesToString());
					}
				}
			}
		}
		
	
}
