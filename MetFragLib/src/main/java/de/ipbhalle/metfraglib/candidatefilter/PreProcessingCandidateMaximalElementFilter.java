package de.ipbhalle.metfraglib.candidatefilter;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/*
 * candidate filter that filters candidates based on maximal numbers of elements
 * e.g. N3Cl2 does only allow upto 3 nitrogens and 2 chlorines
 */
public class PreProcessingCandidateMaximalElementFilter extends AbstractPreProcessingCandidateFilter {
	
	private ByteMolecularFormula maximalFormulaElements;
	
	public PreProcessingCandidateMaximalElementFilter(Settings settings) {
		super(settings);
		try {
			String maximumIncludedElements = (String)settings.get(VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME);
			try {
				this.maximalFormulaElements = new ByteMolecularFormula(maximumIncludedElements);
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
		}
		catch(NullPointerException e) {
			this.maximalFormulaElements = null;
		}
		catch(ClassCastException e) {
			this.maximalFormulaElements = null;
		}
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.maximalFormulaElements == null) return true;
		IMolecularFormula molecularFormula = null;
		try {
			molecularFormula = candidate.getMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		
		byte[] atomIndeces = this.maximalFormulaElements.getAtomsAsIndeces();
		short[] numberAtoms = this.maximalFormulaElements.getNumberOfAtoms();
		
		for(int i = 0; i < atomIndeces.length; i++) {
			if(molecularFormula.getNumberElementsFromByte(atomIndeces[i]) > numberAtoms[i])
				return false;
		}
		return true;
	}

	public void nullify() {
		this.maximalFormulaElements = null;
	}
}
