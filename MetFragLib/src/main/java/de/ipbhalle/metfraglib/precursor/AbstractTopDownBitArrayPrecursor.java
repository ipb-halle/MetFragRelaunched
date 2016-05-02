package de.ipbhalle.metfraglib.precursor;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;

public abstract class AbstractTopDownBitArrayPrecursor extends BitArrayPrecursor {
	
	public AbstractTopDownBitArrayPrecursor(IAtomContainer precursorMolecule) {
		super(precursorMolecule);
	}

	public abstract AbstractTopDownBitArrayFragment toFragment();
	
}
