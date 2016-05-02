package de.ipbhalle.metfraglib.interfaces;

public interface IPreProcessingCandidateFilter {

	public boolean passesFilter(ICandidate candidate) throws Exception;
	
	public void nullify();
	
}
