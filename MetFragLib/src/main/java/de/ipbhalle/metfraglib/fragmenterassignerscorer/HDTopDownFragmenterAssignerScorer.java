package de.ipbhalle.metfraglib.fragmenterassignerscorer;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IScore;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.match.MatchFragmentList;
import de.ipbhalle.metfraglib.match.MatchFragmentNode;
import de.ipbhalle.metfraglib.match.MatchPeakList;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.parameter.ClassNames;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.HDPrecursorCandidateWrapper;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.collection.ScoreCollection;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.HDTopDownBitArrayFragmentWrapper;
import de.ipbhalle.metfraglib.fragmenter.AbstractTopDownFragmenter;

public class HDTopDownFragmenterAssignerScorer extends TopDownFragmenterAssignerScorer {

	protected int precursorIonTypeIndex;
	protected boolean positiveMode;
	protected int precursorIonTypeIndexHD;
	protected boolean positiveModeHD;
	protected HDPrecursorCandidateWrapper candidateWrapper;
	protected boolean extendedWriter = false;

	public HDTopDownFragmenterAssignerScorer(Settings settings, ICandidate candidate) {
		super(settings, candidate);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void calculate() {
		HDTopDownBitArrayPrecursor candidatePrecursor = (HDTopDownBitArrayPrecursor)(this.candidates[0]).getPrecursorMolecule();
		//one native candidate can have multiple deuterated precursors
		int deuteratedCandidateNumber = candidatePrecursor.getNumberDeuteratedCombinations();

		/*
		 * generate root fragment
		 */
		AbstractTopDownBitArrayFragment root = candidatePrecursor.toFragment();
		Byte maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		if(maximumTreeDepth == 0) {
			maximumTreeDepth = candidatePrecursor.getNumNodeDegreeOne() >= 4 ? (byte)3 : (byte)2;
		}
		this.candidates[0].setProperty(VariableNames.MAXIMUM_TREE_DEPTH_NAME, maximumTreeDepth);
		//native spectrum
		SortedTandemMassPeakList tandemMassPeakList = (SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
		tandemMassPeakList.initialiseMassLimits((Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME), (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
		//hd spectrum
		SortedTandemMassPeakList tandemMassPeakListHD = (SortedTandemMassPeakList)settings.get(VariableNames.HD_PEAK_LIST_NAME);
		tandemMassPeakListHD.initialiseMassLimits((Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME), (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
		//native ion type
		Integer precursorIonType = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		this.positiveMode = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		this.precursorIonTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(precursorIonType);
		//hd ion type
		Integer precursorIonTypeHD = (Integer)settings.get(VariableNames.HD_PRECURSOR_ION_MODE_NAME);
		this.precursorIonTypeIndexHD = Constants.ADDUCT_NOMINAL_MASSES.indexOf(precursorIonTypeHD);
		
		//set the minimum mass limit for fragment generation
		double minimumMassNative = tandemMassPeakList.getMinimumMassValue();
		double minimumMassHD = tandemMassPeakListHD.getMinimumMassValue();
		double minimumMass = minimumMassHD < minimumMassNative ? minimumMassHD : minimumMassNative;
		
		this.fragmenter.setMinimumFragmentMassLimit(minimumMass - Math.max(Constants.ADDUCT_MASSES.get(precursorIonTypeIndex), Constants.ADDUCT_MASSES.get(precursorIonTypeIndexHD)));
		
		/*
		 * prepare the processing
		 */
		java.util.Queue<HDTopDownBitArrayFragmentWrapper> toProcessFragments = new java.util.LinkedList<HDTopDownBitArrayFragmentWrapper>();
		/*
		 * wrap the root fragment
		 */
		int[] currentPeakIndexPointerArrayHD = new int[deuteratedCandidateNumber];
		for(int i = 0; i < currentPeakIndexPointerArrayHD.length; i++) currentPeakIndexPointerArrayHD[i] = tandemMassPeakListHD.getNumberElements() - 1;
		HDTopDownBitArrayFragmentWrapper rootFragmentWrapper = new HDTopDownBitArrayFragmentWrapper(root, tandemMassPeakList.getNumberElements() - 1, currentPeakIndexPointerArrayHD);
		toProcessFragments.add(rootFragmentWrapper);
		
		/*
		 * define the match lists
		 */
		java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch = new java.util.HashMap<Integer, MatchFragmentList>();
		java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch = new java.util.HashMap<Integer, MatchPeakList>();
		
		java.util.HashMap<Integer, MatchFragmentList>[] peakIndexToPeakMatchHD = new java.util.HashMap[deuteratedCandidateNumber];
		java.util.HashMap<Integer, MatchPeakList>[] fragmentIndexToPeakMatchHD = new java.util.HashMap[deuteratedCandidateNumber];
		
		for(int i = 0; i < deuteratedCandidateNumber; i++) {
			peakIndexToPeakMatchHD[i] = new java.util.HashMap<Integer, MatchFragmentList>();
			fragmentIndexToPeakMatchHD[i] = new java.util.HashMap<Integer, MatchPeakList>();
		}
		
		/*
		 * iterate over the maximal allowed tree depth
		 */
		for(int k = 1; k <= maximumTreeDepth; k++) {
			java.util.Queue<HDTopDownBitArrayFragmentWrapper> newToProcessFragments = new java.util.LinkedList<HDTopDownBitArrayFragmentWrapper>();
			/*
			 * use each fragment that is marked as to be processed
			 */
			while(!toProcessFragments.isEmpty()) {
				/*
				 * generate fragments of new tree depth
				 */
				HDTopDownBitArrayFragmentWrapper wrappedPrecursorFragment = toProcessFragments.poll();
				if(wrappedPrecursorFragment.getWrappedFragment().isDiscardedForFragmentation()) {
					AbstractTopDownBitArrayFragment clonedFragment = (AbstractTopDownBitArrayFragment)wrappedPrecursorFragment.getWrappedFragment().clone();
					clonedFragment.setAsDiscardedForFragmentation();
					newToProcessFragments.add(new HDTopDownBitArrayFragmentWrapper(clonedFragment, wrappedPrecursorFragment.getCurrentPeakIndexPointer(), wrappedPrecursorFragment.getCurrentPeakIndexPointerHD()));
					continue;
				}
				/*
				 * generate fragments of next tree depth
				 */
				java.util.Vector<AbstractTopDownBitArrayFragment> fragmentsOfCurrentTreeDepth = this.fragmenter.getFragmentsOfNextTreeDepth(wrappedPrecursorFragment.getWrappedFragment());
				/*
				 * get peak pointer of current precursor fragment
				 */
				int currentPeakPointer = wrappedPrecursorFragment.getCurrentPeakIndexPointer();
				int[] currentPeakPointerHD = wrappedPrecursorFragment.getCurrentPeakIndexPointerArrayHD();
				/*
				 * start loop over all child fragments from precursor fragment
				 * to try assigning them to the current peak
				 */
				for(int l = 0; l < fragmentsOfCurrentTreeDepth.size(); l++) {
					AbstractTopDownBitArrayFragment currentFragment = fragmentsOfCurrentTreeDepth.get(l);
					HDTopDownBitArrayFragmentWrapper newFragmentWrapper = new HDTopDownBitArrayFragmentWrapper(currentFragment, currentPeakPointer, currentPeakPointerHD.clone());
					
					if(!fragmentsOfCurrentTreeDepth.get(l).isValidFragment()) {
						newToProcessFragments.add(newFragmentWrapper);
						continue;
					}
					/*
					 * needs to be set
					 * otherwise you get fragments generated by multiple cleavage in one chain
					 */
					if(this.wasAlreadyGeneratedByHashtable(currentFragment)) {
						currentFragment.setAsDiscardedForFragmentation();
						newToProcessFragments.add(newFragmentWrapper);
						continue;
					}
					
					
					int tempPeakPointer = currentPeakPointer;
					this.matchFragment(tempPeakPointer, newFragmentWrapper, tandemMassPeakList, peakIndexToPeakMatch, fragmentIndexToPeakMatch);
					
					//run over all deuterated combinations
					for(int d = 0; d < deuteratedCandidateNumber; d++) {
						newFragmentWrapper.setPrecursorIndex(d);
						int tempPeakPointerHD = newFragmentWrapper.getCurrentPeakIndexPointerHD();
						this.matchFragmentHD(tempPeakPointerHD, newFragmentWrapper, tandemMassPeakListHD, peakIndexToPeakMatchHD[d], fragmentIndexToPeakMatchHD[d]);
					}
					newToProcessFragments.add(newFragmentWrapper);
				}
			}
			toProcessFragments = newToProcessFragments;
		}
		
		this.matchList = new MatchList();
		
		/*
		 * collect score of all scores over all matches
		 */
		double[][] singleScores = new double[this.scoreCollection.getNumberScores()][peakIndexToPeakMatch.size()];
		java.util.Vector<double[][]> singleScoresHD = new java.util.Vector<double[][]>();
		for(int d = 0; d < deuteratedCandidateNumber; d++) singleScoresHD.add(new double[this.scoreCollection.getNumberScores()][peakIndexToPeakMatchHD[d].size()]);
		/*
		 * collect the sum of all scores over all matches
		 */
		double[] summedScores = new double[this.scoreCollection.getNumberScores()];
		double[][] summedScoresHD = new double[deuteratedCandidateNumber][this.scoreCollection.getNumberScores()];
		
		ICandidate[] deuteratedCandidates = new ICandidate[deuteratedCandidateNumber];
		deuteratedCandidates[0] = this.candidates[0];
		for(int i = 1; i < deuteratedCandidateNumber; i++) {
			deuteratedCandidates[i] = new TopDownPrecursorCandidate(this.candidates[0].getInChI(), this.candidates[0].getIdentifier());
		}
		
		this.calculateFragmenterScores(peakIndexToPeakMatch, singleScores, summedScores);
		for(int d = 0; d < deuteratedCandidateNumber; d++) {
			this.calculateFragmenterScoresHD(peakIndexToPeakMatchHD[d], singleScoresHD.get(d), summedScoresHD[d], deuteratedCandidates[d], d);
		}
		
		this.settings.set(VariableNames.PEAK_INDEX_TO_PEAK_MATCH_NAME, peakIndexToPeakMatch);
		this.settings.set(VariableNames.HD_PEAK_INDEX_TO_PEAK_MATCH_NAME, peakIndexToPeakMatchHD[0]);
		
		this.settings.set(VariableNames.CANDIDATE_NAME, this.candidates[0]);
		this.candidates[0].setMatchList(this.matchList);
		
		if(this.scoreCollection == null) return;
		try {
			for(int i = 0; i < this.scoreCollection.getNumberScores(); i++) {
				if(!this.scoreCollection.getScore(i).calculationFinished()) {
					this.scoreCollection.getScore(i).calculate();
				}
				else {
					this.scoreCollection.getScore(i).setValue(summedScores[i] + summedScoresHD[0][i]);
				}
				if(singleScores[i].length != 0 && this.scoreCollection.getScore(i).hasInterimResults() && !this.scoreCollection.getScore(i).isInterimResultsCalculated()) {
					this.scoreCollection.getScore(i).setOptimalValues(singleScores[i]);
				}
			}
			//set score values directly for the first candidate only
			String[] score_types = (String[])this.settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
			for(int i = 0; i < score_types.length; i++) {
				if(scoreCollection.getScore(i).getValue() != null) {
					if(!scoreCollection.getScore(i).isUserDefinedPropertyScore()) {
						this.candidates[0].setProperty(score_types[i], scoreCollection.getScore(i).getValue());
					}
					if(scoreCollection.getScore(i).hasInterimResults()) { 
						this.candidates[0].setProperty(score_types[i] + "_Values", scoreCollection.getScore(i).getOptimalValuesToString());
					}
				}
			}
			//set scores for additional deuterated candidates
			for(int d = 1; d < deuteratedCandidateNumber; d++) {
				//firstly set the HDPeakIndexToPeakMatch needed for HDFragmentPairScore
				this.settings.set(VariableNames.HD_PEAK_INDEX_TO_PEAK_MATCH_NAME, peakIndexToPeakMatchHD[d]);
				for(int i = 0; i < score_types.length; i++) {
					if(this.scoreCollection.getScore(i).getValue() != null) {
						if(this.scoreCollection.getScore(i).isUsesPiecewiseCalculation()) {
							deuteratedCandidates[d].setProperty(score_types[i], summedScores[i] + summedScoresHD[d][i]);
						}
						else if(this.scoreCollection.getScore(i).calculationFinished()) {
							deuteratedCandidates[d].setProperty(score_types[i], this.scoreCollection.getScore(i).getValue());
						}
						else if(!this.scoreCollection.getScore(i).calculationFinished()) {
							this.scoreCollection.getScore(i).calculate();
							deuteratedCandidates[d].setProperty(score_types[i], this.scoreCollection.getScore(i).getValue());
						}
					}
				}
			}
			this.settings.remove(VariableNames.PEAK_INDEX_TO_PEAK_MATCH_NAME);
			this.settings.remove(VariableNames.HD_PEAK_INDEX_TO_PEAK_MATCH_NAME);
			this.candidates = deuteratedCandidates;
			for(int d = 0; d < deuteratedCandidateNumber; d++)
				this.candidates[d].setProperty(VariableNames.HD_GROUP_FLAG_NAME, this.candidates[0].getIdentifier());
		} catch(Exception e) {
			e.printStackTrace();
			logger.warn("candidate score calculation interrupted");
			return;
		}
		this.candidates[0].setProperty(VariableNames.HD_NUMBER_PEAKS_USED_COLUMN, tandemMassPeakListHD.getNumberPeaksUsed());
	}
	
	public void assignScores() {
		
	}
	
	/**
	 * 
	 * @param peakIndexToPeakMatch
	 * @param singleScores
	 * @param summedScores
	 */
	protected void calculateFragmenterScores(java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch, double[][] singleScores, 
			double[] summedScores) {
		
		java.util.Iterator<Integer> it = peakIndexToPeakMatch.keySet().iterator();
		int index = 0;
		while(it.hasNext()) {
			int key = it.next();
			MatchFragmentList matchFragmentList = peakIndexToPeakMatch.get(key);
			MatchFragmentNode bestFragment = matchFragmentList.getRootNode();
			IMatch match = bestFragment.getMatch();
			Double[] scoreValuesSingleMatch = null;
			try {
				scoreValuesSingleMatch = bestFragment.getFragmentScores();
			}
			catch(Exception e) {
				matchFragmentList.printElements();
				System.out.println(this.candidates[0].getIdentifier() + " " + key);
				return;
			}
			Double[] optimalValuesSingleMatch = bestFragment.getOptimalValues();
			for(int k = 1; k < scoreValuesSingleMatch.length; k++) {
				if(optimalValuesSingleMatch[k] != null) singleScores[k-1][index] = optimalValuesSingleMatch[k];
				summedScores[k-1] += scoreValuesSingleMatch[k];
			}
			
			if(bestFragment != null) {
				bestFragment.getFragment().setIsBestMatchedFragment(true);
				//match.initialiseBestMatchedFragment(0);
				this.matchList.addElementSorted(match);
				MatchFragmentNode currentFragment = bestFragment;
				while(currentFragment.hasNext()) {
					MatchFragmentNode node = currentFragment.getNext();
					match.addToMatch(node.getMatch());
					currentFragment = currentFragment.getNext();
				}
			}
			index++;
		}
	}
	
	/**
	 * 
	 * @param peakIndexToPeakMatch
	 * @param singleScores
	 * @param summedScores
	 */
	protected void calculateFragmenterScoresHD(java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatchHD, double[][] singleScores, 
			double[] summedScores, ICandidate candidate, int precursorID) {
		
		java.util.Iterator<Integer> it = peakIndexToPeakMatchHD.keySet().iterator();
		int index = 0;

		String sumFormulasOfFragmentsExplainedPeaks = "";
		String smilesOfFragmentsExplainedPeaks = "";
		
		while(it.hasNext()) {
			int key = it.next();
			MatchFragmentList matchFragmentList = peakIndexToPeakMatchHD.get(key);
			MatchFragmentNode bestFragment = matchFragmentList.getRootNode();
			IMatch match = bestFragment.getMatch();
			
			sumFormulasOfFragmentsExplainedPeaks += match.getMatchedPeak().getMass() + ":" + match.getModifiedFormulaStringOfBestMatchedFragment() + ";";
			// write out fragment smiles of HDX candidates if extended writer is set
			if(this.extendedWriter) {
				try {
					smilesOfFragmentsExplainedPeaks += match.getMatchedPeak().getMass() + ":" + MoleculeFunctions.getFragmentSmilesHD(match.getBestMatchedFragment(), precursorID) + ";";
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Double[] scoreValuesSingleMatch = null;
			try {
				scoreValuesSingleMatch = bestFragment.getFragmentScores();
			}
			catch(Exception e) {
				matchFragmentList.printElements();
				System.out.println(candidate.getIdentifier() + " " + key);
				return;
			}
			Double[] optimalValuesSingleMatch = bestFragment.getOptimalValues();
			for(int k = 1; k < scoreValuesSingleMatch.length; k++) {
				if(optimalValuesSingleMatch[k] != null) singleScores[k-1][index] = optimalValuesSingleMatch[k];
				summedScores[k-1] += scoreValuesSingleMatch[k];
			}
			
			if(bestFragment != null) {
				bestFragment.getFragment().setIsBestMatchedFragment(true);
				//match.initialiseBestMatchedFragment(0);
				MatchFragmentNode currentFragment = bestFragment;
				while(currentFragment.hasNext()) {
					MatchFragmentNode node = currentFragment.getNext();
					match.addToMatch(node.getMatch());
					currentFragment = currentFragment.getNext();
				}
			}
			index++;
		}
		
		if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
		if(smilesOfFragmentsExplainedPeaks.length() != 0) smilesOfFragmentsExplainedPeaks = smilesOfFragmentsExplainedPeaks.substring(0, smilesOfFragmentsExplainedPeaks.length() - 1);

		candidate.setProperty("HDSmilesOfExplPeaks", smilesOfFragmentsExplainedPeaks);
		candidate.setProperty("HDFormulasOfExplPeaks", sumFormulasOfFragmentsExplainedPeaks);
		candidate.setProperty("HDNoExplPeaks", index);
	}
	
	/**
	 * checks the match of the current fragment and returns the index of the next peak
	 * 
	 * @param tempPeakPointer
	 * @param currentFragmentWrapper
	 * @param tandemMassPeakList
	 * @param sortedScoredPeaks
	 * @param newToProcessFragments
	 * @param peakIndexToPeakMatch
	 * @param fragmentIndexToPeakMatch
	 * @return
	 */
	protected boolean matchFragment(int tempPeakPointer, HDTopDownBitArrayFragmentWrapper currentFragmentWrapper, 
			SortedTandemMassPeakList tandemMassPeakList, 
			java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch,
			java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch) {
		byte matched = -1;
		boolean matchedAndAdded = false;
		while(matched != 1 && tempPeakPointer >= 0) {
			IMatch[] match = new IMatch[1];
			TandemMassPeak currentPeak = tandemMassPeakList.getElement(tempPeakPointer);
			/*
			 * calculate match
			 */
			if(tempPeakPointer >= 0) {
				matched = currentFragmentWrapper.getWrappedFragment().matchToPeak(currentPeak, this.precursorIonTypeIndex, this.positiveMode, match);
			}
			/*
			 * check whether match has occurred
			 */
			if(matched == 0) {
				currentFragmentWrapper.getWrappedFragment().setPrecursorFragments(true);
				Double[][] currentScores = this.scoreCollection.calculateSingleMatch(match[0]);
				/*
				 * first generate the new fragment node and set the score values
				 */
				MatchFragmentNode newNode = new MatchFragmentNode(match[0]);
				newNode.setScore(currentScores[0][0]);
				newNode.setFragmentScores(currentScores[0]);
				newNode.setOptimalValues(currentScores[1]);
			
				/*
				 * find correct location in the fragment list
				 */
				boolean similarFragmentFound = false;
				if(peakIndexToPeakMatch.containsKey(tempPeakPointer)) {
					Double[] values = peakIndexToPeakMatch.get(tempPeakPointer).containsByFingerprint(currentFragmentWrapper.getWrappedFragment().getAtomsFastBitArray());
					if(values == null) {
						peakIndexToPeakMatch.get(tempPeakPointer).insert(newNode);
					}
					else {
						if(values[0] < currentScores[0][0]) {
							peakIndexToPeakMatch.get(tempPeakPointer).removeElementByID((int)Math.floor(values[1]));
							fragmentIndexToPeakMatch.get((int)Math.floor(values[1])).removeElementByID(tempPeakPointer);
							if(fragmentIndexToPeakMatch.get((int)Math.floor(values[1])).getRootNode() == null) {
								fragmentIndexToPeakMatch.remove((int)Math.floor(values[1]));
							}
							peakIndexToPeakMatch.get(tempPeakPointer).insert(newNode);
						}
						else similarFragmentFound = true;
					}
				}
				else {
					MatchFragmentList newFragmentList = new MatchFragmentList(newNode);
					peakIndexToPeakMatch.put(tempPeakPointer, newFragmentList);
				}
				/*
				 * insert peak into fragment's peak list 
				 */
				if(!similarFragmentFound) {
					if(fragmentIndexToPeakMatch.containsKey(currentFragmentWrapper.getWrappedFragment().getID())) {
						fragmentIndexToPeakMatch.get(currentFragmentWrapper.getWrappedFragment().getID()).insert(currentPeak, currentScores[0][0], tempPeakPointer);
					}
					else {
						MatchPeakList newPeakList = new MatchPeakList(currentPeak, currentScores[0][0], tempPeakPointer);
						fragmentIndexToPeakMatch.put(currentFragmentWrapper.getWrappedFragment().getID(), newPeakList);
					}
				}
			}
			/*
			 * if the mass of the current fragment was greater than the peak mass then assign the current peak ID to the peak IDs of the
			 * child fragments as they have smaller masses 
			 */
			if(matched == 1 || tempPeakPointer == 0) {
				/*
				 * mark current fragment for further fragmentation
				 */
				currentFragmentWrapper.setCurrentPeakIndexPointer(tempPeakPointer);
			}
			/*
			 * if the current fragment has matched to the current peak then set the current peak index to the next peak as the current fragment can 
			 * also match to the next peak
			 * if the current fragment mass was smaller than that of the current peak then set the current peak index to the next peak (reduce the index) 
			 * as the next peak mass is smaller and could match the current smaller fragment mass 
			 */
			if(matched == 0 || matched == -1) tempPeakPointer--;
		}
		return matchedAndAdded;
	}
	
	/**
	 * 
	 * @param tempPeakPointer
	 * @param currentFragmentWrapper
	 * @param tandemMassPeakList
	 * @param sortedScoredPeaks
	 * @param newToProcessFragments
	 * @param peakIndexToPeakMatch
	 * @param fragmentIndexToPeakMatch
	 * @return
	 */
	protected boolean matchFragmentHD(int tempPeakPointer, HDTopDownBitArrayFragmentWrapper currentFragmentWrapper, 
			SortedTandemMassPeakList tandemMassPeakList, 
			java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch,
			java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch) 
	{
		byte matched = -1;
		boolean matchedAndAdded = false;
		while(matched != 1 && tempPeakPointer >= 0) {
			IMatch[] match = new IMatch[1];
			TandemMassPeak currentPeak = tandemMassPeakList.getElement(tempPeakPointer);
			/*
			 * calculate match
			 */
			if(tempPeakPointer >= 0) 
				matched = currentFragmentWrapper.matchToPeak(currentPeak, this.precursorIonTypeIndexHD, this.positiveMode, match);
			/*
			 * check whether match has occurred
			 */
			if(matched == 0) {
				matchedAndAdded = true;
				currentFragmentWrapper.getWrappedFragment().setPrecursorFragments(true);
				Double[][] currentScores = this.scoreCollection.calculateSingleMatch(match[0]);
				/*
				 * insert fragment into peak's fragment list 
				 */
				/*
				 * first generate the new fragment node and set the score values
				 */
				MatchFragmentNode newNode = new MatchFragmentNode(match[0]);
				newNode.setScore(currentScores[0][0]);
				newNode.setFragmentScores(currentScores[0]);
				newNode.setOptimalValues(currentScores[1]);
			
				/*
				 * find correct location in the fragment list
				 */
				boolean similarFragmentFound = false;
				if(peakIndexToPeakMatch.containsKey(tempPeakPointer)) {
					Double[] values = peakIndexToPeakMatch.get(tempPeakPointer).containsByFingerprint(currentFragmentWrapper.getWrappedFragment().getAtomsFastBitArray());
					if(values == null) {
						peakIndexToPeakMatch.get(tempPeakPointer).insert(newNode);
					}
					else {
						if(values[0] < currentScores[0][0]) {
							peakIndexToPeakMatch.get(tempPeakPointer).removeElementByID((int)Math.floor(values[1]));
							fragmentIndexToPeakMatch.get((int)Math.floor(values[1])).removeElementByID(tempPeakPointer);
							if(fragmentIndexToPeakMatch.get((int)Math.floor(values[1])).getRootNode() == null) {
								fragmentIndexToPeakMatch.remove((int)Math.floor(values[1]));
							}
							peakIndexToPeakMatch.get(tempPeakPointer).insert(newNode);
						}
						else similarFragmentFound = true;
					}
				}
				else {
					MatchFragmentList newFragmentList = new MatchFragmentList(newNode);
					peakIndexToPeakMatch.put(tempPeakPointer, newFragmentList);
				}
				/*
				 * insert peak into fragment's peak list 
				 */
				if(!similarFragmentFound) {
					if(fragmentIndexToPeakMatch.containsKey(currentFragmentWrapper.getWrappedFragment().getID())) {
						fragmentIndexToPeakMatch.get(currentFragmentWrapper.getWrappedFragment().getID()).insert(currentPeak, currentScores[0][0], tempPeakPointer);
					}
					else {
						MatchPeakList newPeakList = new MatchPeakList(currentPeak, currentScores[0][0], tempPeakPointer);
						fragmentIndexToPeakMatch.put(currentFragmentWrapper.getWrappedFragment().getID(), newPeakList);
					}
				}
			}
			/*
			 * if the mass of the current fragment was greater than the peak mass then assign the current peak ID to the peak IDs of the
			 * child fragments as they have smaller masses 
			 */
			if(matched == 1 || tempPeakPointer == 0) {
				/*
				 * mark current fragment for further fragmentation
				 */
				currentFragmentWrapper.setCurrentPeakIndexPointerHD(tempPeakPointer);
			}
			/*
			 * if the current fragment has matched to the current peak then set the current peak index to the next peak as the current fragment can 
			 * also match to the next peak
			 * if the current fragment mass was smaller than that of the current peak then set the current peak index to the next peak (reduce the index) 
			 * as the next peak mass is smaller and could match the current smaller fragment mass 
			 */
			if(matched == 0 || matched == -1) tempPeakPointer--;
		}
		return matchedAndAdded;
	}
	
	@Override
	public void initialise() throws AtomTypeNotKnownFromInputListException, Exception {
		/*
		 * initialise candidate
		 */
		this.candidates[0].setProperty(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS, this.settings.get(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS));
		this.candidateWrapper = new HDPrecursorCandidateWrapper(this.candidates[0]);
		this.candidateWrapper.initialisePrecursorCandidate();
		
		/*
		 * initialise fragmenter
		 */
		this.fragmenter = (AbstractTopDownFragmenter) Class.forName((String)settings.get(VariableNames.METFRAG_FRAGMENTER_TYPE_NAME)).getConstructor(Settings.class).newInstance(settings);

		/*
		 * initialise score
		 */
		String[] score_types = (String[])this.settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		//init scores and add hd scores
		IScore[] scores = new IScore[score_types.length];
		for(int i = 0; i < score_types.length; i++) {
			logger.debug("\t\tinitialising " + score_types[i]);
			scores[i] = (IScore) Class.forName(ClassNames.getClassNameOfScore(score_types[i])).getConstructor(Settings.class).newInstance(this.settings);
		}
		//add hd fragmenter score
		this.scoreCollection = new ScoreCollection(scores);
		
		Object writer = this.settings.get(VariableNames.METFRAG_CANDIDATE_WRITER_NAME);
		
		if(writer != null) {
			String[] writers = (String[])writer;
			for(int i = 0; i < writers.length; i++) {
				if(writers[i].equals("ExtendedHDCSV")) {
					this.extendedWriter = true;
					break;
				}
			}
		}
	}
	
	
}
