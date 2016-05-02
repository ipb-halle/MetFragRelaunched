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
public class PreProcessingCandidateMinimalElementFilter extends AbstractPreProcessingCandidateFilter {
	
	private ByteMolecularFormula minimumFormulaElements;
	
	public PreProcessingCandidateMinimalElementFilter(Settings settings) {
		super(settings);
		try {
			String minimumIncludedElements = (String)settings.get(VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME);
			try {
				this.minimumFormulaElements = new ByteMolecularFormula(minimumIncludedElements);
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
		}
		catch(NullPointerException e) {
			this.minimumFormulaElements = null;
		}
		catch(ClassCastException e) {
			this.minimumFormulaElements = null;
		}
	}

	public boolean passesFilter(ICandidate candidate) throws AtomTypeNotKnownFromInputListException {
		if(this.minimumFormulaElements == null) return true;
		IMolecularFormula molecularFormula = candidate.getMolecularFormula();
		
		byte[] atomIndeces = this.minimumFormulaElements.getAtomsAsIndeces();
		short[] numberAtoms = this.minimumFormulaElements.getNumberOfAtoms();
		
		for(int i = 0; i < atomIndeces.length; i++) {
			if(molecularFormula.getNumberElementsFromByte(atomIndeces[i]) < numberAtoms[i])
				return false;
		}
		return true;
	}

	public void nullify() {
		this.minimumFormulaElements = null;
	}
}
