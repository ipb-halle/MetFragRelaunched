package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * score created for the 
 * combines peakScore and bond energy score in one single summand
 */
public class MatchSpectrumCosineSimilarityScore extends AbstractScore {
	
	public MatchSpectrumCosineSimilarityScore(Settings settings) {
		super(settings);
		this.value = Double.valueOf(0);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		MatchList matchList = ((ICandidate)this.settings.get(VariableNames.CANDIDATE_NAME)).getMatchList();
		//generate matched peak list containing only peaks which have been annotated
		SortedTandemMassPeakList matchPeakList = new SortedTandemMassPeakList((Double)this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			matchPeakList.addElement(matchList.getElement(i).getMatchedPeak());
		}
		this.value = matchPeakList.cosineSimilarity(
				(SortedTandemMassPeakList)this.settings.get(VariableNames.PEAK_LIST_NAME), 
				(Double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME), 
				(Double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
		this.calculationFinished = true;
	}

	public boolean isBetterValue(double value) {
		return this.value < value ? true : false;
	}
	
	/**
	 * 
	 */
	public Double[] calculateSingleMatch(IMatch currentMatch) {
		return new Double[] {0.0, null};
	}
	
}
