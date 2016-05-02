package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PreProcessingCandidateElementExclusionFilter extends AbstractPreProcessingCandidateFilter {
	
	private String[] excludedElements;
	
	public PreProcessingCandidateElementExclusionFilter(Settings settings) {
		super(settings);
		try {
			this.excludedElements = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME);
		}
		catch(NullPointerException e) {
			this.excludedElements = null;
		}
		catch(ClassCastException e) {
			this.excludedElements = null;
		}
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.excludedElements == null) return true;
		IMolecularFormula molecularFormula = null;
		try {
			molecularFormula = candidate.getMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < this.excludedElements.length; i++) {
			if(molecularFormula.contains(this.excludedElements[i])) return false;
		}
		return true;
	}
	
	public void nullify() {
		this.excludedElements = null;
	}

	
}
