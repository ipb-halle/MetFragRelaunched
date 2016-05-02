package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.list.SuspectList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class SuspectListScore extends AbstractScore {

	protected ICandidate candidate;
	
	public SuspectListScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		this.value = 0.0;
		SuspectList[] suspectLists = (SuspectList[])settings.get(VariableNames.SUSPECTLIST_SCORE_LIST_NAME);
		for(int i = 0; i < suspectLists.length; i++) {
			if(candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null) {
				String inchikey1 = (String)candidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
				if(inchikey1.length() != 0) {
					if(suspectLists[i].containsElement(inchikey1)) {
						this.value = 1.0;
						break;
					}
				}
			}
		}
		this.calculationFinished = true;
	}
	
	public void setOptimalValues(double[] values) {
		this.optimalValues[0] = values[0];
	}
	
	public Double[] calculateSingleMatch(IMatch match) {
		return new Double[] {0.0, null};
	}
	
	@Override
	public void nullify() {
		super.nullify();
	}

	public boolean isBetterValue(double value) {
		return value > this.value ? true : false;
	}
}
