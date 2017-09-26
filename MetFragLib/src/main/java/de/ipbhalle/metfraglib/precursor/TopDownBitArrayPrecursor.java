package de.ipbhalle.metfraglib.precursor;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.fragment.TopDownBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;

public class TopDownBitArrayPrecursor extends AbstractTopDownBitArrayPrecursor {
	
	public TopDownBitArrayPrecursor(IAtomContainer precursorMolecule) {
		super(precursorMolecule);
	}
	
	public AbstractTopDownBitArrayFragment toFragment() {
		return new TopDownBitArrayFragment(this);
	}
	
}
