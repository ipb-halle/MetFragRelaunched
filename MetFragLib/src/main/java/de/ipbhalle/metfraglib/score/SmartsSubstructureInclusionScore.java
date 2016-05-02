package de.ipbhalle.metfraglib.score;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class SmartsSubstructureInclusionScore extends AbstractScore {

	protected ICandidate candidate;
	
	public SmartsSubstructureInclusionScore(Settings settings) {
		super(settings);
		this.optimalValues = new double[1];
		this.optimalValues[0] = 0.0;
		this.candidate = (ICandidate)settings.get(VariableNames.CANDIDATE_NAME);
		this.hasInterimResults = false;
	}
	
	public void calculate() {
		this.value = 0.0;
		SMARTSQueryTool[] smartsQuerytools = (SMARTSQueryTool[])this.settings.get(VariableNames.SMARTS_SUBSTRUCTURE_INCLUSION_SCORE_LIST_NAME);
		if(smartsQuerytools == null) return;

 		for(int i = 0; i < smartsQuerytools.length; i++) {
			try {
				if(smartsQuerytools[i].matches(candidate.getPrecursorMolecule().getStructureAsIAtomContainer())) {
					this.value++;
				}
			} catch (CDKException e) {
				e.printStackTrace();
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
