package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PostProcessingCandidateInChIKeyFilter extends AbstractPostProcessingCandidateFilter {

	public PostProcessingCandidateInChIKeyFilter(Settings settings) {
		super(settings);
	}
	 
	public CandidateList filter(CandidateList candidateList) {
		this.numberPostFilteredCandidates = 0;
		if(candidateList.getNumberElements() == 0) return candidateList;
		CandidateList filteredCandidateList = new SortedScoredCandidateList();
		java.util.Vector<String> seenInChIKeys = new java.util.Vector<String>();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate currentCandidate = candidateList.getElement(i);
			String inchiKey1 = (String)currentCandidate.getProperty(VariableNames.INCHI_KEY_1_NAME);
			if(inchiKey1.length() != 0) {
				if(!seenInChIKeys.contains(inchiKey1)) {
					filteredCandidateList.addElement(currentCandidate);
					seenInChIKeys.add(inchiKey1);
				}
				else {
					currentCandidate.nullify();
					currentCandidate = null;
					this.numberPostFilteredCandidates++;
				}
			}
		}
		return filteredCandidateList;
	}
	
	public void nullify() {
		
	}
	
}
