package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.match.HDFragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.AbstractPeakList;
import de.ipbhalle.metfraglib.match.MatchFragmentList;
import de.ipbhalle.metfraglib.match.MatchFragmentNode;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import edu.emory.mathcs.backport.java.util.Arrays;

public class HDFragmentPairScore extends AbstractScore {

	public HDFragmentPairScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		java.util.HashMap<?, ?> peakIndexToPeakMatch = (java.util.HashMap<?, ?>)this.settings.get(VariableNames.PEAK_INDEX_TO_PEAK_MATCH_NAME);
		java.util.HashMap<?, ?> peakIndexToPeakMatchHD = (java.util.HashMap<?, ?>)this.settings.get(VariableNames.HD_PEAK_INDEX_TO_PEAK_MATCH_NAME);
		//define parameters needed for score calculation
		
		int numberPeaksUsed = ((AbstractPeakList)this.settings.get(VariableNames.PEAK_LIST_NAME)).getNumberPeaksUsed();
		
		int countedPairs = 0;
		byte maximalNumberDeuteriumsExchanged = (Byte)this.settings.get(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS);
		if(((Integer)this.settings.get(VariableNames.HD_PRECURSOR_ION_MODE_NAME)) == 2) maximalNumberDeuteriumsExchanged++;
		
		int[] peakIndeces = new int[peakIndexToPeakMatch.size()];
		int[] peakIndecesHD = new int[peakIndexToPeakMatchHD.size()];
		
		java.util.Iterator<?> it = peakIndexToPeakMatch.keySet().iterator();
		java.util.Iterator<?> itHD = peakIndexToPeakMatchHD.keySet().iterator();
		//fill integer arrays with assigned peak indexes for later search
		int index = 0; while(it.hasNext()) {peakIndeces[index] = (Integer)it.next(); index++;} 
		index = 0; while(itHD.hasNext()) {peakIndecesHD[index] = (Integer)itHD.next(); index++;} 
		//sort arrays as peak lists are sorted by mass
		Arrays.sort(peakIndeces);
		Arrays.sort(peakIndecesHD);
		
		double mzppm = (Double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		double mzabs = (Double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		//store whether native peak was already used within a pair
		//a peak can only once be used
		BitArray peaksPaired = new BitArray(peakIndeces.length);
		peaksPaired.setAll(false);
		
		//now search for peak pairs
		//each hd assigned peak must have a corresponding assigned native peak
		//fragments of pairs must match as well
		for(int i = 0; i < peakIndecesHD.length; i++) {
			double massHD = ((MatchFragmentList)peakIndexToPeakMatchHD.get(peakIndecesHD[i])).getRootNode().getMatch().getMatchedPeak().getMass();
			//System.out.println("checking " + massHD);
			for(index = 0; index < peakIndeces.length; index++) {
				if(peaksPaired.get(index)) continue;
				double mass = ((MatchFragmentList)peakIndexToPeakMatch.get(peakIndeces[index])).getRootNode().getMatch().getMatchedPeak().getMass();
				//System.out.println(" => " + mass);
				boolean matchedPeaks = false;
				boolean matchFragments = false;
				for(double numDs = 0.0; numDs <= maximalNumberDeuteriumsExchanged; numDs++) 
				{
					//check for peak pairs based on their mass and the shift of deuterium
					matchedPeaks = MathTools.matchMasses(massHD, mass - ((numDs * Constants.getMonoisotopicMassOfAtom("H"))) + (numDs * Constants.getMonoisotopicMassOfAtom("D")), mzppm, mzabs);
					//if matched check for matching fragments
					if(matchedPeaks) {
						if(this.checkForMatchingFragments((MatchFragmentList)peakIndexToPeakMatch.get(peakIndeces[index]), (MatchFragmentList)peakIndexToPeakMatchHD.get(peakIndecesHD[i]), (int)numDs)) {
							countedPairs++;
							matchFragments = true;
							peaksPaired.set(index, true);
							break;
						}
					}
				}
				if(matchedPeaks && matchFragments) break;
				if(mass > massHD) break;
			}
		
		}
		if(numberPeaksUsed == 0) this.value = 0.0; 
		else this.value = (double)countedPairs / (double)numberPeaksUsed;
	}

	/**
	 * 
	 * @param fragmentList
	 * @param fragmentListHD
	 * @param deuteriumShift
	 * @return
	 */
	private boolean checkForMatchingFragments(MatchFragmentList fragmentList, MatchFragmentList fragmentListHD, int deuteriumShift) {
		MatchFragmentNode node = fragmentList.getRootNode();
		MatchFragmentNode nodeHD = fragmentListHD.getRootNode();

		while(node != null) {
			while(nodeHD != null) {
				for(int i = 0; i < node.getMatch().getMatchedFragmentsSize(); i++) {
					HDFragmentMassToPeakMatch matchHD = ((HDFragmentMassToPeakMatch)nodeHD.getMatch());
					int numberMatchedFragmentsHD = matchHD.getMatchedFragmentsSize();
					for(int j = 0; j < numberMatchedFragmentsHD; j++) 
					{
						int numberDeuteriumsOfMatchedFragment = 0;
						int deuteriumsOfMatchedFragment = matchHD.getNumberOfDeuteriumsOfMatchedFragment(j);
						int deuteriumDifferenceOfMatchedFragment = matchHD.getNumberOfDeuteriumDifferToPeakMass(j);
						int deuteriumFromCharge = 0;
						if(Constants.ADDUCT_NOMINAL_MASSES.get(matchHD.getFragmentsAdductTypeIndex(j)) == 2) deuteriumFromCharge = 1;
						else if(Constants.ADDUCT_NOMINAL_MASSES.get(matchHD.getFragmentsAdductTypeIndex(j)) == -2) deuteriumFromCharge = -1;
						numberDeuteriumsOfMatchedFragment = deuteriumsOfMatchedFragment + deuteriumDifferenceOfMatchedFragment + deuteriumFromCharge;
						IFragment fragment = node.getMatch().getMatchedFragmentList().getElement(i);
						IFragment fragmentHD = nodeHD.getMatch().getMatchedFragmentList().getElement(j);
						if(fragment.equals(fragmentHD) && numberDeuteriumsOfMatchedFragment == deuteriumShift) return true;
					}
				}
				nodeHD = nodeHD.getNext();
			}
			node = node.getNext();
		}
		return false;
	}
	
	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}

	public Double getValue() {
		return this.value;
	}
	
	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
