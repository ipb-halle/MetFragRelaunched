package de.ipbhalle.metfraglib.collection;

import de.ipbhalle.metfraglib.interfaces.IPostProcessingCandidateFilter;
import de.ipbhalle.metfraglib.list.CandidateList;

public class PostProcessingCandidateFilterCollection {

	private IPostProcessingCandidateFilter[] postProcessingCandidateFilter;
	private int numberPostFilteredCandidates;
	
	public PostProcessingCandidateFilterCollection(IPostProcessingCandidateFilter[] postProcessingCandidateFilter) {
		this.postProcessingCandidateFilter = postProcessingCandidateFilter;
	}

	public CandidateList filter(CandidateList candidateList) {
		if(this.postProcessingCandidateFilter == null) return candidateList;
		for(int i = 0; i < this.postProcessingCandidateFilter.length; i++) 
			candidateList = this.postProcessingCandidateFilter[i].filter(candidateList);
		for(int i = 0; i < this.postProcessingCandidateFilter.length; i++)
			this.numberPostFilteredCandidates += this.postProcessingCandidateFilter[i].getNumberPostFilteredCandidates();
		return candidateList;
	}
	
	public int getNumberPostFilteredCandidates() {
		return numberPostFilteredCandidates;
	}

	public void nullfiy() {
		for(int i = 0; i < this.postProcessingCandidateFilter.length; i++) 
			this.postProcessingCandidateFilter[i].nullify();
	}	
	
}
