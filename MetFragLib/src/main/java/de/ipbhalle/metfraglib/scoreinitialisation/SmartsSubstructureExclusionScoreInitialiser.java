package de.ipbhalle.metfraglib.scoreinitialisation;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * 
 * initialises smarts queries
 * 
 */
public class SmartsSubstructureExclusionScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) {
		String[] excludedSmarts = (String[])settings.get(VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME);
		SMARTSQueryTool[] smartsQuerytools = null;
		if(excludedSmarts != null && excludedSmarts.length != 0) {
			smartsQuerytools = new SMARTSQueryTool[excludedSmarts.length];
			for(int i = 0; i < excludedSmarts.length; i++) {
				smartsQuerytools[i] = new SMARTSQueryTool(excludedSmarts[i], DefaultChemObjectBuilder.getInstance());
			}
			settings.set(VariableNames.SMARTS_SUBSTRUCTURE_EXCLUSION_SCORE_LIST_NAME, smartsQuerytools);
		}
	}

	public void postProcessScoreParameters(Settings settings) {
		return;
	}
	
}
