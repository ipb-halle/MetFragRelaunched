package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * candidate filter that filters candidates that are not connected
 */
public class PreProcessingCandidateUnconnectedStructureFilter extends AbstractPreProcessingCandidateFilter {
	
	public PreProcessingCandidateUnconnectedStructureFilter(Settings settings) {
		super(settings);
	}

	public boolean passesFilter(ICandidate candidate) {
		String formula = candidate.getInChI().split("/")[1];
		if(formula.contains(".") || formula.matches("^[0-9]+.*")) return false;
		return true;
	}

	public void nullify()  {
		
	}
}
