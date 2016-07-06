package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartGroupListCollection;

public class AutomatedLossAnnotationScore extends AbstractScore {

	protected ICandidate candidate;
	
	public AutomatedLossAnnotationScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		this.value = 0.0;
		PeakToSmartGroupListCollection peakToSmartGroupListCollection = (PeakToSmartGroupListCollection)this.settings.get(VariableNames.PEAK_TO_SMARTS_GROUP_LIST_COLLECTION_NAME);
		for(int i = 0; i < peakToSmartGroupListCollection.getNumberElements(); i++) {
			PeakToSmartGroupList peakToSmartsGroupList = peakToSmartGroupListCollection.getElement(i);
			this.value += peakToSmartsGroupList.getMaximalMatchingProbability(this.candidate);
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
