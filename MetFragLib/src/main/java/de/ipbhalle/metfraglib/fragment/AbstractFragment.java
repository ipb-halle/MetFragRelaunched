package de.ipbhalle.metfraglib.fragment;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;

public abstract class AbstractFragment implements IFragment {

	protected Integer ID;
	protected Byte treeDepth;
	//protected final IMolecularStructure precursorMolecule;
	protected Boolean hasMatched;
	protected Boolean isBestMatchedFragment;
	protected Boolean isValidFragment;
	protected Boolean discardedForFragmentation;

	/**
	 * constructor setting precursor molecule of fragment
	 * 
	 * @param precursor
	 */
	public AbstractFragment() {
	//	this.precursorMolecule = precursorMolecule;
		this.treeDepth = 0;
		this.hasMatched = false;
		this.isValidFragment = false;
		this.discardedForFragmentation = false;
		this.isBestMatchedFragment = false;
	}
	
	public void shallowNullify() {
		this.discardedForFragmentation = null;
		this.hasMatched = null;
		this.isValidFragment = null;
		this.ID = null;
		this.isBestMatchedFragment = null;
	}
	
	/**
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	
	public void setID(int ID) {
		this.ID = ID;
	}
	
	public int getID() {
		return this.ID;
	}
	
	/**
	 * default zero
	 * 
	 * @return
	 */
	public byte getTreeDepth() {
		return this.treeDepth;
	}
	

	public boolean hasMatched() {
		return hasMatched;
	}

	public void setHasMatched() {
		this.hasMatched = true;
	}

	public void setIsBestMatchedFragment(boolean value) {
		this.isBestMatchedFragment = value;
	}

	public boolean isBestMatchedFragment() {
		return this.isBestMatchedFragment;
	}
	
	public boolean isValidFragment() {
		return isValidFragment;
	}

	public void setAsValidFragment() {
		this.isValidFragment = true;
	}
	
	public boolean isDiscardedForFragmentation() {
		return this.discardedForFragmentation;
	}

	public void setAsDiscardedForFragmentation() {
		this.discardedForFragmentation = true;
	}
	
	/**
	 * 
	 * @return
	 */
	public abstract IFragment clone(IMolecularStructure precursorMolecule);
	
	/**
	 * delete all objects
	 */
	public void nullify() {
		this.discardedForFragmentation = null;
		this.hasMatched = null;
		this.isValidFragment = null;
		this.ID = null;
		this.isBestMatchedFragment = null;
		this.treeDepth = null;
	}
	
}
