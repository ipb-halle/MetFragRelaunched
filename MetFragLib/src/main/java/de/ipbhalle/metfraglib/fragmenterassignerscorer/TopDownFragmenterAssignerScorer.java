package de.ipbhalle.metfraglib.fragmenterassignerscorer;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.match.MatchFragmentList;
import de.ipbhalle.metfraglib.match.MatchFragmentNode;
import de.ipbhalle.metfraglib.match.MatchPeakList;
import de.ipbhalle.metfraglib.match.MatchPeakNode;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.AbstractTopDownBitArrayPrecursor;

import java.io.IOException;
import java.util.Vector;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragmentWrapper;

public class TopDownFragmenterAssignerScorer extends AbstractFragmenterAssignerScorer {

	protected boolean uniqueFragmentMatches;
	/*
	 * workaround
	 */
	protected java.util.Hashtable<String, Integer> bitArrayToFragment;
	
	public TopDownFragmenterAssignerScorer(Settings settings, ICandidate candidate) {
		super(settings, candidate);
		this.bitArrayToFragment = new java.util.Hashtable<String, Integer>();
		this.uniqueFragmentMatches = (Boolean)this.settings.get(VariableNames.METFRAG_UNIQUE_FRAGMENT_MATCHES);
	}

	@Override
	public void calculate() {
		AbstractTopDownBitArrayPrecursor candidatePrecursor = (AbstractTopDownBitArrayPrecursor)(this.candidates[0]).getPrecursorMolecule();
		//generate root fragment to start fragmentation
		AbstractTopDownBitArrayFragment root = candidatePrecursor.toFragment();
		Byte maximumTreeDepth = (Byte)settings.get(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		if(maximumTreeDepth == 0) {
			maximumTreeDepth = candidatePrecursor.getNumNodeDegreeOne() >= 4 ? (byte)3 : (byte)2;
		}
		this.candidates[0].setProperty(VariableNames.MAXIMUM_TREE_DEPTH_NAME, maximumTreeDepth);
		//read peaklist
		SortedTandemMassPeakList tandemMassPeakList = (SortedTandemMassPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
		tandemMassPeakList.initialiseMassLimits((Double)settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME), (Double)settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
		Integer precursorIonType = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		Boolean positiveMode = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		int precursorIonTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(precursorIonType);
		
		this.fragmenter.setMinimumFragmentMassLimit(this.fragmenter.getMinimumFragmentMassLimit() - Constants.ADDUCT_MASSES.get(precursorIonTypeIndex));
		
		/*
		 * prepare the processing
		 */
		java.util.Queue<AbstractTopDownBitArrayFragmentWrapper> toProcessFragments = new java.util.LinkedList<AbstractTopDownBitArrayFragmentWrapper>();
		/*
		 * wrap the root fragment
		 */
		AbstractTopDownBitArrayFragmentWrapper rootFragmentWrapper = new AbstractTopDownBitArrayFragmentWrapper(root, tandemMassPeakList.getNumberElements() - 1);
		toProcessFragments.add(rootFragmentWrapper);
		java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch = new java.util.HashMap<Integer, MatchFragmentList>();
		java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch = new java.util.HashMap<Integer, MatchPeakList>();
		MatchPeakList sortedScoredPeaks = null;
		
		Vector<String> fingerprints = new Vector<String>();
		Vector<IBitFingerprint> fps = new Vector<IBitFingerprint>();
		
		/*
		 * iterate over the maximal allowed tree depth
		 */
		for(int k = 1; k <= maximumTreeDepth; k++) {
			java.util.Queue<AbstractTopDownBitArrayFragmentWrapper> newToProcessFragments = new java.util.LinkedList<AbstractTopDownBitArrayFragmentWrapper>();
			/*
			 * use each fragment that is marked as to be processed
			 */
			while(!toProcessFragments.isEmpty()) {
				/*
				 * generate fragments of new tree depth
				 */
				AbstractTopDownBitArrayFragmentWrapper wrappedPrecursorFragment = toProcessFragments.poll();
				
				if(wrappedPrecursorFragment.getWrappedFragment().isDiscardedForFragmentation()) {
					AbstractTopDownBitArrayFragment clonedFragment = (AbstractTopDownBitArrayFragment)wrappedPrecursorFragment.getWrappedFragment().clone();
					clonedFragment.setAsDiscardedForFragmentation();
					newToProcessFragments.add(new AbstractTopDownBitArrayFragmentWrapper(clonedFragment, wrappedPrecursorFragment.getCurrentPeakIndexPointer()));
					continue;
				}
				/*
				 * generate fragments of next tree depth
				 */
				java.util.Vector<AbstractTopDownBitArrayFragment> fragmentsOfCurrentTreeDepth = this.fragmenter.getFragmentsOfNextTreeDepth(wrappedPrecursorFragment.getWrappedFragment());
				this.addFingerPrintsToVector(fragmentsOfCurrentTreeDepth, fingerprints, fps);
				
				/*
				 * get peak pointer of current precursor fragment
				 */
				int currentPeakPointer = wrappedPrecursorFragment.getCurrentPeakIndexPointer();
				/*
				 * start loop over all child fragments from precursor fragment
				 * to try assigning them to the current peak
				 */
				for(int l = 0; l < fragmentsOfCurrentTreeDepth.size(); l++) {
					AbstractTopDownBitArrayFragment currentFragment = fragmentsOfCurrentTreeDepth.get(l);
					
					if(!fragmentsOfCurrentTreeDepth.get(l).isValidFragment()) {
						newToProcessFragments.add(new AbstractTopDownBitArrayFragmentWrapper(fragmentsOfCurrentTreeDepth.get(l), currentPeakPointer));
						continue;
					}
					/*
					 * needs to be set
					 * otherwise you get fragments generated by multiple cleavage in one chain
					 */
					
					if(this.wasAlreadyGeneratedByHashtable(currentFragment)) {
						currentFragment.setAsDiscardedForFragmentation();
						newToProcessFragments.add(new AbstractTopDownBitArrayFragmentWrapper(currentFragment, currentPeakPointer));
						continue;
					}

					byte matched = -1;
					int tempPeakPointer = currentPeakPointer;
					while(matched != 1 && tempPeakPointer >= 0) {
						IMatch[] match = new IMatch[1];
						/*
						 * calculate match
						 */
						matched = currentFragment.matchToPeak(tandemMassPeakList.getElement(tempPeakPointer), precursorIonTypeIndex, positiveMode, match);
						/*
						 * check whether match has occurred
						 */
						if(matched == 0) {
							currentFragment.setPrecursorFragments(true);
							Double[][] currentScores = this.scoreCollection.calculateSingleMatch(match[0]);
							if(sortedScoredPeaks == null) 
							{
								sortedScoredPeaks = new MatchPeakList(tandemMassPeakList.getElement(tempPeakPointer), currentScores[0][0], tempPeakPointer);
							}
							else {
								/*
								 * gives score and id of peak
								 */
								Double[] oldPeakValues = sortedScoredPeaks.contains(tempPeakPointer);
								if(oldPeakValues != null && oldPeakValues[0] < currentScores[0][0]) {
									sortedScoredPeaks.removeElementByID((int)Math.floor(oldPeakValues[1]));
									sortedScoredPeaks.insert(tandemMassPeakList.getElement(tempPeakPointer), currentScores[0][0], tempPeakPointer);
								}
								else if(oldPeakValues == null) {
									sortedScoredPeaks.insert(tandemMassPeakList.getElement(tempPeakPointer), currentScores[0][0], tempPeakPointer);
								}
							}	
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

						/*	MatchFragmentNode newNode = new MatchFragmentNode(currentFragment);
							newNode.setScore(currentScores[0][0]);
							newNode.setFragmentScores(currentScores[0]);
							newNode.setOptimalValues(currentScores[1]);
							newNode.setHydrogenDifference(((DefaultFragmentToPeakMatch)match[0]).getNumberOfHydrogensDifferToPeakMass(0));
							newNode.setFragmentAdductTypeIndex(((DefaultFragmentToPeakMatch)match[0]).getFragmentsAdductTypeIndex(0));
							*/
							/*
							 * find correct location in the fragment list
							 */
							boolean similarFragmentFound = false;
							if(peakIndexToPeakMatch.containsKey(tempPeakPointer)) {
								Double[] values = peakIndexToPeakMatch.get(tempPeakPointer).containsByFingerprint(currentFragment.getAtomsBitArray());
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
								if(fragmentIndexToPeakMatch.containsKey(currentFragment.getID())) {
									fragmentIndexToPeakMatch.get(currentFragment.getID()).insert(tandemMassPeakList.getElement(tempPeakPointer), currentScores[0][0], tempPeakPointer);
								}
								else {
									MatchPeakList newPeakList = new MatchPeakList(tandemMassPeakList.getElement(tempPeakPointer), currentScores[0][0], tempPeakPointer);
									fragmentIndexToPeakMatch.put(currentFragment.getID(), newPeakList);
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
							newToProcessFragments.add(new AbstractTopDownBitArrayFragmentWrapper(currentFragment, tempPeakPointer));
						}
						/*
						 * if the current fragment has matched to the current peak then set the current peak index to the next peak as the current fragment can 
						 * also match to the next peak
						 * if the current fragment mass was smaller than that of the current peak then set the current peak index to the next peak (reduce the index) 
						 * as the next peak mass is smaller and could match the current smaller fragment mass 
						 */
						if(matched == 0 || matched == -1) tempPeakPointer--;
					}
				}
			}
			toProcessFragments = newToProcessFragments;
		}
		
		this.writeFingerPrintsToFile(fingerprints, Constants.OS_TEMP_DIR + Constants.OS_SPECIFIC_FILE_SEPARATOR + this.candidates[0].getIdentifier() + ".txt");
		
		this.matchList = new MatchList();
		
		if(this.uniqueFragmentMatches) this.cleanMatchLists(sortedScoredPeaks, peakIndexToPeakMatch, fragmentIndexToPeakMatch);

		/*
		 * collect score of all scores over all matches
		 */
		double[][] singleScores = new double[this.scoreCollection.getNumberScores()][peakIndexToPeakMatch.size()];
		/*
		 * collect the sum of all scores over all matches
		 */
		double[] summedScores = new double[this.scoreCollection.getNumberScores()];

		java.util.Iterator<Integer> it = peakIndexToPeakMatch.keySet().iterator();
		int index = 0;
		/*
		 * go over peak matches
		 */
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
				System.exit(1);
			}
			Double[] optimalValuesSingleMatch = bestFragment.getOptimalValues();
			for(int k = 1; k < scoreValuesSingleMatch.length; k++) {
				if(optimalValuesSingleMatch[k] != null) singleScores[k-1][index] = optimalValuesSingleMatch[k];
				summedScores[k-1] += scoreValuesSingleMatch[k];
			}
			
			if(bestFragment != null) {
				bestFragment.getFragment().setIsBestMatchedFragment(true);
				//match.initialiseBestMatchedFragmentByFragmentID(bestFragment.getFragment().getID());
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
		
		this.settings.set(VariableNames.MATCH_LIST_NAME, this.matchList);
		this.candidates[0].setMatchList(matchList);
		
		if(this.scoreCollection == null) return;
		try {
			for(int i = 0; i < this.scoreCollection.getNumberScores(); i++) {
				if(!this.scoreCollection.getScore(i).calculationFinished()) {
					this.scoreCollection.getScore(i).calculate();
				}
				else 
					this.scoreCollection.getScore(i).setValue(summedScores[i]);
				if(singleScores[i].length != 0 && this.scoreCollection.getScore(i).hasInterimResults() && !this.scoreCollection.getScore(i).isInterimResultsCalculated()) {
					this.scoreCollection.getScore(i).setOptimalValues(singleScores[i]);
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			logger.warn("candidate score calculation interrupted");
			return;
		}
	}
	
	/**
	 * 
	 * @param sortedScoredPeaks
	 * @param peakIndexToPeakMatch
	 * @param fragmentIndexToPeakMatch
	 */
	public void cleanMatchLists(MatchPeakList sortedScoredPeaks, java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch, java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch) {
		if(sortedScoredPeaks == null) return;
		//	System.out.println(sortedScoredPeaks.countElements());
		/*
		 * traverse matched peak list
		 * 69 -> f1 -> f2 -> f3
		 * 68 -> f1 -> f2 -> f3
		 * 67 -> f1 -> f2 -> f3
		 */
		
	//	sortedScoredPeaks.printElements();
		
		//	this.printHashMapInfo(peakIndexToPeakMatch, fragmentIndexToPeakMatch);
		
		while(sortedScoredPeaks.getRootNode() != null) {
			/*
			 * get current peak index
			 * 69 -> f1 -> f2 -> f3
			 */
			int currentPeakIndex = sortedScoredPeaks.getRootNode().getId();
			//		System.out.println("best peak guy " + currentPeakIndex + " " + sortedScoredPeaks.getRootNode().getPeak().getMass());
			sortedScoredPeaks.removeFirst();
			/*
			 * get fragment list
			 * f1 -> f2 -> f3
			 */
			MatchFragmentList currentFragmentList = peakIndexToPeakMatch.get(currentPeakIndex);
			//	System.out.println("best fragment guy " + currentFragmentList.getRootNode().getElement().getID() + " " + currentFragmentList.getRootNode().getElement().getMonoisotopicMass());
			/*
			 * get root node
			 * f1
			 */
			boolean processed = false;
			while(!processed) {
				MatchFragmentNode currentFragmentNode = currentFragmentList.getRootNode();
				if(currentFragmentNode == null) {
					//		System.out.println("removing peak index " + currentPeakIndex);
					peakIndexToPeakMatch.remove(currentPeakIndex);
					processed = true;
					continue;
				}
				//	System.out.println("\t" + currentFragmentNode.getElement().getID() + " pointing to " + fragmentIndexToPeakMatch.get(currentFragmentNode.getElement().getID()).getRootNode().getId());
				/*
				 * check whether the best peak's best fragment also points to this peak
				 */
				if(fragmentIndexToPeakMatch.get(currentFragmentNode.getFragment().getID()).getRootNode().getId() == currentPeakIndex) {
					//		System.out.println("processed");
					processed = true;
					/*
					 * traverse over fragment list from root
					 */
					MatchFragmentNode fragmentNodeToRemoveFrom = currentFragmentNode.getNext();
					while(fragmentNodeToRemoveFrom != null) {
						/*
						 * remove peak index from fragment list
						 */
						//			System.out.println("removing " + fragmentNodeToRemoveFrom.getElement().getID() + " from " + currentPeakIndex);
						fragmentIndexToPeakMatch.get(fragmentNodeToRemoveFrom.getFragment().getID()).removeElementByID(currentPeakIndex);		
						fragmentNodeToRemoveFrom = fragmentNodeToRemoveFrom.getNext();
					}
					/*
					 * get root peak from peak list of the current matched fragment
					 * f1 -> 69 -> 68 -> 67
					 * 69
					 */
					MatchPeakNode currentPeakNode = fragmentIndexToPeakMatch.get(currentFragmentList.getRootNode().getFragment().getID()).getRootNode();
					//		System.out.println(currentFragmentList.getRootNode().getElement().getID() + " currentPeakNode: " + currentPeakNode.getId());
					//		if(!currentPeakNode.hasNext()) System.out.println("nothing next");
					while(currentPeakNode.hasNext()) {
						/*
						 * get neighbour of peak node
						 */
						currentPeakNode = currentPeakNode.getNext();
						//			System.out.println("\tremoving from list " + currentPeakNode.getId());
						/*
						 * remove fragment index from peak list
						 * 68 -> f1 -> f2 -> f3
						 * 67 -> f1 -> f2 -> f3
						 * removed:
						 * 68 -> f2 -> f3
						 * 67 -> f2 -> f3
						 */
						try {
							//	sortedScoredPeaks.printElements();
							boolean toUpdate = false;
							try {
								peakIndexToPeakMatch.get(currentPeakNode.getId()).getRootNode().getFragment().getID();
							}
							catch(Exception e) {
								System.out.println("fail " + this.candidates[0].getIdentifier());
								System.out.println(currentFragmentNode);
								System.out.println(currentFragmentNode.getFragment());
								System.out.println(currentFragmentNode.getFragment().getID());
								System.out.println();
								System.out.println(peakIndexToPeakMatch.get(currentPeakNode.getId()));
								System.out.println(currentPeakNode.getId());
								System.exit(1);
							}
							if(peakIndexToPeakMatch.get(currentPeakNode.getId()).getRootNode().getFragment().getID() == currentFragmentNode.getFragment().getID()) {
								toUpdate = true;
							}
							if(toUpdate) {
								//		System.out.println("update1");
								//		sortedScoredPeaks.printElements();
									//		System.out.println("remove peak from sortedScoredPeaks by id " + currentPeakNode.getId());
									sortedScoredPeaks.removeElementByID(currentPeakNode.getId());
									//		sortedScoredPeaks.printElements();
							}
							//sortedScoredPeaks.printElements();
							//	System.out.println("removing from " + currentPeakNode.getId() + " " + currentFragmentNode.getElement().getID());
							peakIndexToPeakMatch.get(currentPeakNode.getId()).removeElementByID(currentFragmentNode.getFragment().getID());
					
							if(toUpdate) {
								MatchFragmentNode fragmentRoot = peakIndexToPeakMatch.get(currentPeakNode.getId()).getRootNode();
								//		System.out.println("update2 " + currentPeakNode.getId() + " " + fragmentRoot);
								if(fragmentRoot != null) {
									MatchPeakNode peakRoot = fragmentIndexToPeakMatch.get(fragmentRoot.getFragment().getID()).getElementById(currentPeakNode.getId());
									//		System.out.println("\tpeakRoot " + peakRoot + " " + fragmentRoot.getElement().getID() + " " + currentPeakNode.getId());
									if(peakRoot != null) {
										//			System.out.print("\there2 " + peakRoot.getId() + " "); sortedScoredPeaks.printElements();
										sortedScoredPeaks.insert(peakRoot.clone());
									}
								}
								else {
									//		System.out.println("\tremoving from peak hashmap " + currentPeakNode.getId());
									peakIndexToPeakMatch.remove(currentPeakNode.getId());
								}
							}
							//	sortedScoredPeaks.printElements();
						}
						catch(Exception e) {
							e.printStackTrace();
							System.out.println(this.candidates[0].getIdentifier() + " " + currentPeakNode.getId() + " " + peakIndexToPeakMatch.get(currentPeakNode.getId()) + " " + peakIndexToPeakMatch.size());
							System.exit(1);
						}
					}
					//	currentFragmentList.removeAfterRoot();
				}
				else {
					//	System.out.println("remove " + currentFragmentList.getRootNode().getElement().getID());
					currentFragmentList.removeFirst();
				}
			}
			//	sortedScoredPeaks.printElements();
			//	this.printHashMapInfo(peakIndexToPeakMatch, fragmentIndexToPeakMatch);
		}
	}
	
	protected void printHashMapInfo(java.util.HashMap<Integer, MatchFragmentList> peakIndexToPeakMatch, java.util.HashMap<Integer, MatchPeakList> fragmentIndexToPeakMatch) {
		java.util.Iterator<Integer> it = peakIndexToPeakMatch.keySet().iterator();
		while(it.hasNext()) {
			int key = it.next();
			System.out.print(key + " -> ");
			peakIndexToPeakMatch.get(key).printElements();
			/*
			 * peakID -> fragments
			0 -> 188:104.05002:C7H6N:9.148574830115374	202:104.05002:C7H6N:9.148574830115374	139:105.05784:C7H7N:8.339671410655502	
			1 -> 38:102.04695:C8H6:14.281494870955974	211:101.03913:C8H5:12.476472398768284	
			3 -> 18:105.05784:C7H7N:14.135045638767528	22:105.05784:C7H7N:14.135045638767528	188:104.05002:C7H6N:13.03922782363598	202:104.05002:C7H6N:13.03922782363598	
			4 -> 38:102.04695:C8H6:31.232063005948184	
			5 -> 18:105.05784:C7H7N:15.469563444506283	22:105.05784:C7H7N:15.469563444506283	188:104.05002:C7H6N:13.360240498468544	202:104.05002:C7H6N:13.360240498468544	
			6 -> 18:105.05784:C7H7N:11.41574962148274	22:105.05784:C7H7N:11.41574962148274	188:104.05002:C7H6N:9.954371408167367	202:104.05002:C7H6N:9.954371408167367	
			8 -> 139:105.05784:C7H7N:8.94381924374472	
			*/
		}
		System.out.println("##### " + fragmentIndexToPeakMatch.size());
		java.util.Iterator<Integer> it1 = fragmentIndexToPeakMatch.keySet().iterator();
		while(it1.hasNext()) {
			int key = it1.next();
			System.out.print(key + " -> ");
			fragmentIndexToPeakMatch.get(key).printElements();
			/*
			 * fragmentID -> peaks
				139 -> 8:108.080575:19.0:8.94381924374472 0:104.0495:19.0:8.339671410655502 
				18 -> 5:106.0652:34.0:15.469563444506283 3:105.05745:32.0:14.135045638767528 6:107.072925:21.0:11.41574962148274 
				38 -> 4:105.06985:128.0:31.232063005948184 1:104.062025:33.0:14.281494870955974 
				188 -> 5:106.0652:34.0:13.360240498468544 3:105.05745:32.0:13.03922782363598 6:107.072925:21.0:9.954371408167367 0:104.0495:19.0:9.148574830115374 
				202 -> 5:106.0652:34.0:13.360240498468544 3:105.05745:32.0:13.03922782363598 6:107.072925:21.0:9.954371408167367 0:104.0495:19.0:9.148574830115374 
				22 -> 5:106.0652:34.0:15.469563444506283 3:105.05745:32.0:14.135045638767528 6:107.072925:21.0:11.41574962148274 
				211 -> 1:104.062025:33.0:12.476472398768284
			 */
		}
		System.out.println("#####");
	}
	
	/**
	 * 
	 * @param currentFragment
	 * @return
	 */
	protected boolean wasAlreadyGenerated(AbstractTopDownBitArrayFragment currentFragment) {
		AbstractTopDownBitArrayFragment precursorOfFragment = currentFragment.getPrecursorFragment();
		if(precursorOfFragment == null) return false;
			java.util.Vector<AbstractTopDownBitArrayFragment> children = precursorOfFragment.getChildren();
		
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getID() < currentFragment.getID() && children.get(i).getAtomsBitArray().equals(currentFragment.getAtomsBitArray())) 
			{
				return true;
			}
		}
		return false;
	}

	protected boolean wasAlreadyGeneratedByHashtable(AbstractTopDownBitArrayFragment currentFragment) {
		String currentHash = currentFragment.getAtomsBitArray().toString();
		Integer minimalTreeDepth = this.bitArrayToFragment.get(currentHash);
		if(minimalTreeDepth == null) {
			this.bitArrayToFragment.put(currentHash, (int)currentFragment.getTreeDepth());
			return false;
		}
		//if(minimalTreeDepth.equals(currentFragment.getTreeDepth()))
		if(minimalTreeDepth >= currentFragment.getTreeDepth())
			return false;
		else return true;
	}
	
	@Override
	public FragmentList getFragments() {
		return null;
	}
	
	@Override
	public MatchList getMatchList() {
		return this.matchList;
	}
	
	@Override
	public void nullify() {
		super.nullify();
		this.bitArrayToFragment = null;
	}

	@Override
	public void shallowNullify() {
		super.shallowNullify();
		this.bitArrayToFragment = null;
	}
	
	protected void addFingerPrintsToVector(java.util.Vector<AbstractTopDownBitArrayFragment> fragments, Vector<String> fingerprints, Vector<IBitFingerprint> fps) {
		for(int i = 0; i < fragments.size(); i++) {
			int index = 0;
			IBitFingerprint fp = TanimotoSimilarity.calculateFingerPrint(fragments.get(i).getStructureAsIAtomContainer());
			String fingerprint = MoleculeFunctions.fingerPrintToString(fp);
			int compareResult = -1;
			while(index < fingerprints.size()) {
				compareResult = fingerprints.get(index).compareTo(fingerprint);
				if(compareResult < 0) index++;
				else break; 
			}
			if(compareResult != 0) {
				fingerprints.add(index, fingerprint);
				fps.add(index, fp);
			}
		}
	}
	
	protected void writeFingerPrintsToFile(Vector<String> fps, String filename) {
		try {
			java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
			for(int i = 0; i < fps.size(); i++) {
				bwriter.write(fps.get(i));
				bwriter.newLine();
			}
			bwriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
