package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;

/*
 * candidate filter that filters candidates that have not included all elements of includedElements array
 * other elements are allowed
 */
public class PreProcessingCandidateElementInclusionOptionalFilter extends AbstractPreProcessingCandidateFilter {
	
	private String[] includedElements;
	private byte[] atomsAsIndeces;
	
	public PreProcessingCandidateElementInclusionOptionalFilter(Settings settings) {
		super(settings);
		try {
			this.includedElements = (String[])settings.get(VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME);
			this.atomsAsIndeces = new byte[this.includedElements.length];
			for(int i = 0; i < this.includedElements.length; i++) {
				this.atomsAsIndeces[i] = (byte)Constants.ELEMENTS.indexOf(this.includedElements[i]);
				if(this.atomsAsIndeces[i] == -1) throw new AtomTypeNotKnownFromInputListException();
			}
		}
		catch(NullPointerException e) {
			this.includedElements = null;
		}
		catch(ClassCastException e) {
			this.includedElements = null;
		}
		catch(AtomTypeNotKnownFromInputListException e) {
			this.includedElements = null;
		}
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.includedElements == null) return true;
		try {
			ByteMolecularFormula formula = (ByteMolecularFormula)candidate.getMolecularFormula();
			byte[] atomIndeces = formula.getAtomIndeces();
			for(int j = 0; j < atomIndeces.length; j++) {
				boolean isIn = false;
				for(int i = 0; i < this.atomsAsIndeces.length; i++) {
					if(this.atomsAsIndeces[i] == atomIndeces[j]) {
						isIn = true;
						break;
					}
				}
				if(!isIn) return false;
			}
			if(formula.getNumberHydrogens() > 0) {
				boolean isIn = false;
				for(int i = 0; i < this.atomsAsIndeces.length; i++) {
					if(this.atomsAsIndeces[i] == Constants.H_INDEX) {
						isIn = true;
					}
				}
				if(!isIn) return false;
			}
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	public void nullify() {
		this.includedElements = null;
	}
}
