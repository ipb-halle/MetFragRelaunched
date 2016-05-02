package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.IPostProcessingCandidateFilter;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractPostProcessingCandidateFilter implements IPostProcessingCandidateFilter {

	protected int numberPostFilteredCandidates;
	
	public AbstractPostProcessingCandidateFilter(Settings settings) {
	}
	
	public int getNumberPostFilteredCandidates() {
		return numberPostFilteredCandidates;
	}

	public abstract void nullify();
	
}
