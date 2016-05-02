package de.ipbhalle.metfraglib.collection;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IPreProcessingCandidateFilter;

public class PreProcessingCandidateFilterCollection {

	private IPreProcessingCandidateFilter[] preProcessingCandidateFilter;
	
	public PreProcessingCandidateFilterCollection(IPreProcessingCandidateFilter[] preProcessingCandidateFilter) {
		this.preProcessingCandidateFilter = preProcessingCandidateFilter;
	}
	
	public boolean passesFilter(ICandidate candidate) throws Exception {
		if(this.preProcessingCandidateFilter == null) return true;
		for(int i = 0; i < this.preProcessingCandidateFilter.length; i++) {
			if(!this.preProcessingCandidateFilter[i].passesFilter(candidate)) return false;
		}
		return true;
	}
	
	public void nullify() {
		for(int i = 0; i < this.preProcessingCandidateFilter.length; i++)
			this.preProcessingCandidateFilter[i].nullify();
	}
}
