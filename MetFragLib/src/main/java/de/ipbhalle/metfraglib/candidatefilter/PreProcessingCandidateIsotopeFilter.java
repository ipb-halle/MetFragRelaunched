package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * candidate filter that filters candidates that are not connected
 */
public class PreProcessingCandidateIsotopeFilter extends AbstractPreProcessingCandidateFilter {
	
	public PreProcessingCandidateIsotopeFilter(Settings settings) {
		super(settings);
	}

	public boolean passesFilter(ICandidate candidate) {
		if(candidate.getInChI().contains("/i")) return false;
		return true;
	}

	public void nullify()  {
		
	}
}
