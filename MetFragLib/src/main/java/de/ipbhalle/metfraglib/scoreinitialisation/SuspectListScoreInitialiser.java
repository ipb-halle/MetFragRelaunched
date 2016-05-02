package de.ipbhalle.metfraglib.scoreinitialisation;

import de.ipbhalle.metfraglib.interfaces.IScoreInitialiser;
import de.ipbhalle.metfraglib.list.SuspectList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * 
 * initialises suspect lists
 * 
 */
public class SuspectListScoreInitialiser implements IScoreInitialiser {

	@Override
	public void initScoreParameters(Settings settings) {
		String[] suspectListFileNames = (String[])settings.get(VariableNames.SCORE_SUSPECT_LISTS_NAME);
		SuspectList[] suspectLists = new SuspectList[suspectListFileNames.length];
		for(int i = 0; i < suspectListFileNames.length; i++) {
			suspectLists[i] = new SuspectList(suspectListFileNames[i]);
		}
		settings.set(VariableNames.SUSPECTLIST_SCORE_LIST_NAME, suspectLists);
	}

}
