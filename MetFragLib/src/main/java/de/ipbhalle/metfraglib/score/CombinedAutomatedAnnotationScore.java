package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartsGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartsGroupListCollection;

public class CombinedAutomatedAnnotationScore extends AbstractScore {

	protected ICandidate candidate;
	
	public CombinedAutomatedAnnotationScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		this.value = 0.0;
		PeakToSmartsGroupListCollection peakToSmartGroupListCollection = (PeakToSmartsGroupListCollection)this.settings.get(VariableNames.PEAK_TO_SMARTS_GROUP_LIST_COLLECTION_NAME);
		PeakToSmartsGroupListCollection lossToSmartGroupListCollection = (PeakToSmartsGroupListCollection)this.settings.get(VariableNames.LOSS_TO_SMARTS_GROUP_LIST_COLLECTION_NAME);
		for(int i = 0; i < peakToSmartGroupListCollection.getNumberElements(); i++) {
			PeakToSmartsGroupList peakToSmartsGroupList = peakToSmartGroupListCollection.getElement(i);
			this.value += peakToSmartsGroupList.getMaximalMatchingProbabilitySorted(this.candidate);
		}
		for(int i = 0; i < peakToSmartGroupListCollection.getNumberElements(); i++) {
			PeakToSmartsGroupList peakToSmartsGroupList = lossToSmartGroupListCollection.getElement(i);
			this.value += peakToSmartsGroupList.getMaximalMatchingProbabilitySorted(this.candidate);
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
