package de.ipbhalle.metfraglib.score;

import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.interfaces.ICandidate;

public class HDExchangedHydrogendsScore extends AbstractScore {
	
	public HDExchangedHydrogendsScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		byte numberHydrogensExperimentallyExchanged = (Byte)this.settings.get(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS);
		HDTopDownBitArrayPrecursor candidatePrecursor = (HDTopDownBitArrayPrecursor)((ICandidate)this.settings.get(VariableNames.CANDIDATE_NAME)).getPrecursorMolecule();
		
		double numberExchangedHydrogensInMolecule = candidatePrecursor.getNumberExchangedHydrogens();
		double numberVariableHydrogens = candidatePrecursor.getNumberVariableDeuteriums();
		double numberExchangeableHydrogens = candidatePrecursor.getNumberExchangeableHydrogens();
		double denominator = numberExchangedHydrogensInMolecule - numberHydrogensExperimentallyExchanged + 1;
		if(numberVariableHydrogens > 0) denominator = numberVariableHydrogens + 1;
		if(numberExchangeableHydrogens > numberExchangedHydrogensInMolecule) denominator = (numberExchangeableHydrogens - numberExchangedHydrogensInMolecule) + 1;
		this.value = 1.0 / (double)denominator;
		this.calculationFinished = true;
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
