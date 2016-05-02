package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.IPreProcessingCandidateFilter;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractPreProcessingCandidateFilter implements IPreProcessingCandidateFilter {

	public AbstractPreProcessingCandidateFilter(Settings settings) {
		
	}

	public abstract void nullify();
	
}
