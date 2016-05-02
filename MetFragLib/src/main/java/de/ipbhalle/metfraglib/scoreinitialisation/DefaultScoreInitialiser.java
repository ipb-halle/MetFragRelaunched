package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * 
 * the default score initialiser is used for scores that don't need 
 * additional data or parameters
 * 
 */
public class DefaultScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) {
		//nothing to do here
	}

}
