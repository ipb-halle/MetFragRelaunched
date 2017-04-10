package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.model.LinearRetentionTimeModel;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * 
 * initialises the rt logp model
 * 
 */
public class RetentionTimeScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) {
		LinearRetentionTimeModel linearModel = new LinearRetentionTimeModel(settings);
		
		settings.set(VariableNames.RETENTION_TIME_SCORE_LINEAR_MODEL_NAME, linearModel);
	}

	public void postProcessScoreParameters(Settings settings) {
		return;
	}
	
}
