package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.list.CandidateList;

public interface IPostProcessingCandidateFilter {

	public CandidateList filter(CandidateList candidateList);
	
	public int getNumberPostFilteredCandidates();
	
	public void nullify();
}
