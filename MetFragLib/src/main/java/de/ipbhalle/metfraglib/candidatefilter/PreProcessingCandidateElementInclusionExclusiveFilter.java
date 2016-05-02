package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * candidate filter that filters candidates that have only included the elements of includedElements array
 * other elements are NOT allowed
 * not all elements in the includedElements must be included in the candidate
 */
public class PreProcessingCandidateElementInclusionExclusiveFilter extends AbstractPreProcessingCandidateFilter {
	
	private String[] includedElements;
	
	public PreProcessingCandidateElementInclusionExclusiveFilter(Settings settings) {
		super(settings);
		try {
			this.includedElements = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME);
		}
		catch(NullPointerException e) {
			this.includedElements = null;
		}
		catch(ClassCastException e) {
			this.includedElements = null;
		}
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.includedElements == null) return true;
		IMolecularFormula molecularFormula = null;
		try {
			molecularFormula = candidate.getMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		if(molecularFormula.getNumberElements() > this.includedElements.length) return false;
		String[] elementsArray = molecularFormula.getElementsAsStringArray();
		boolean[] atomsIsContained = new boolean[this.includedElements.length];
		for(int i = 0; i < elementsArray.length; i++) {
			boolean isContained = false;
			for(int j = 0; j < this.includedElements.length; j++) {
				if(this.includedElements[j].equals(elementsArray[i])) { 
					isContained = true;
					atomsIsContained[i] = true;
					break;
				}
			}
			if(!isContained) return false;
		}
		for(Boolean contained : atomsIsContained)
			if(!contained) return false;
		return true;
	}

	public void nullify() {
		this.includedElements = null;
	}
}
