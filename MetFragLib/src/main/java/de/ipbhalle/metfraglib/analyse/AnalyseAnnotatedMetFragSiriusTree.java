package de.ipbhalle.metfraglib.analyse;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SiriusNodePeakList;
import de.ipbhalle.metfraglib.match.FragmentFormulaToPeakMatch;
import de.ipbhalle.metfraglib.peak.SiriusNodePeak;

public class AnalyseAnnotatedMetFragSiriusTree {

	private SiriusNodePeakList peakList;
	
	public AnalyseAnnotatedMetFragSiriusTree(SiriusNodePeakList peakList) {
		this.peakList = peakList;
	}
	
	/**
	 * 
	 * @param candidateNumber
	 * @return
	 */
	public String getAnnotatedDotTree(ICandidate candidate, MatchList matchList) {
		String dotString = "strict digraph {\n";
		for(int i = 0; i < this.peakList.getNumberElements(); i++) {
			SiriusNodePeak currentPeak = (SiriusNodePeak)this.peakList.getElement(i);
			FragmentFormulaToPeakMatch peakMatch = getMatchToPeak(matchList, currentPeak);
			if(peakMatch != null) {
				try {
					dotString += currentPeak.getDotEntry("\\nInt: " + currentPeak.getIntensity() + "\\nMetFrag:\\n" 
									+ peakMatch.getBestMatchedFragment().getMolecularFormula(candidate.getPrecursorMolecule())
									+ "\\n" + peakMatch.getBestMatchedFragment().getSmiles(candidate.getPrecursorMolecule()), "red", "grey") + "\n";
				} catch (RelativeIntensityNotDefinedException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					dotString += currentPeak.getDotEntry("Int: " + currentPeak.getIntensity()) + "\n";
				} catch (RelativeIntensityNotDefinedException e) {
					e.printStackTrace();
				}
			}
		}
		dotString += "}\n";
		return dotString;
	}
	
	/**
	 * 
	 * @param matchList
	 * @param peak
	 * @return
	 */
	public FragmentFormulaToPeakMatch getMatchToPeak(MatchList matchList, SiriusNodePeak peak) {
		for(int ii = 0; ii < matchList.getNumberElements(); ii++) {
			FragmentFormulaToPeakMatch peakMatch = (FragmentFormulaToPeakMatch)matchList.getElement(ii);
			if(peakMatch.getMatchedPeak().getSiriusID().equals(peak.getSiriusID())) return peakMatch;
		}
		return null;
	}
	
	public double getRelativeAmountMatchedPeaks(MatchList matchList) {
		return (double)matchList.getNumberElements() / (this.peakList.getNumberElements());
	}
	
	public boolean detectReplacementRearrangement(MatchList matchList) {
		java.util.ArrayList<SiriusNodePeak> leaves = this.peakList.getLeaves();
		for(int i = 0; i < leaves.size(); i++) {
			SiriusNodePeak currentNode = leaves.get(i);
			boolean hasMatch = false;
			while(!currentNode.isRoot()) {
				FragmentFormulaToPeakMatch peakMatch = getMatchToPeak(matchList, currentNode);
				if(peakMatch != null) {
					hasMatch = true;
					break;
				}
				currentNode = currentNode.getFather();
			}
			if(!hasMatch) return true;
		}
		return false;
	}
}
