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
public class PreProcessingCandidateElementInclusionFilter extends AbstractPreProcessingCandidateFilter {
	
	private String[] includedElements;
	private byte[] atomsAsIndeces;
	
	public PreProcessingCandidateElementInclusionFilter(Settings settings) {
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
			for(int j = 0; j < this.atomsAsIndeces.length; j++) {
				boolean isIn = false;
				if(Constants.H_INDEX == this.atomsAsIndeces[j] && formula.getNumberHydrogens() > 0) continue;
				else if(Constants.H_INDEX == this.atomsAsIndeces[j] && formula.getNumberHydrogens() == 0) return false;
				for(int i = 0; i < atomIndeces.length; i++) {
					if(this.atomsAsIndeces[j] == atomIndeces[i]) {
						isIn = true;
						break;
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
