package de.ipbhalle.metfraglib.precursor;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragment.TopDownBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;

public class TopDownBitArrayPrecursor extends AbstractTopDownBitArrayPrecursor {
	
	public TopDownBitArrayPrecursor(IAtomContainer precursorMolecule) {
		super(precursorMolecule);
	}
	
	public AbstractTopDownBitArrayFragment toFragment() {
		TopDownBitArrayFragment fragment = new TopDownBitArrayFragment(this);
		try {
			fragment.initialiseMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		return fragment;
	}
	
}
