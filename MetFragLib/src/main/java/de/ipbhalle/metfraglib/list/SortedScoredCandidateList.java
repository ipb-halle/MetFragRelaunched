package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.interfaces.ICandidate;

public class SortedScoredCandidateList extends ScoredCandidateList {

	/**
	 * 
	 * @param candidate
	 */
	public void addElement(ICandidate candidate) {
		double score = (Double)candidate.getProperty("Score");
		int index = 0; 
		while(index < this.list.size() && score < (Double)((ICandidate)this.list.get(index)).getProperty("Score")) 
			index++;
		this.list.add(index, candidate);
	}
}
