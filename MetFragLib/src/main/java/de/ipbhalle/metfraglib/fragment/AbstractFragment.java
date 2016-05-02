package de.ipbhalle.metfraglib.fragment;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IPeak;

public abstract class AbstractFragment implements IFragment {

	protected Integer ID;
	protected Byte treeDepth;
	protected final IMolecularStructure precursorMolecule;
	protected Boolean hasMatched;
	protected Boolean isBestMatchedFragment;
	protected Boolean isValidFragment;
	protected Boolean discardedForFragmentation;

	/**
	 * constructor setting precursor molecule of fragment
	 * 
	 * @param precursor
	 */
	public AbstractFragment(IMolecularStructure precursorMolecule) {
		this.precursorMolecule = precursorMolecule;
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
	 * returns structure as IAtomContainer object
	 * 
	 * @return
	 */
	public abstract IAtomContainer getStructureAsIAtomContainer();
	
	/**
	 * get smiles string of the fragment structure
	 * 
	 * @return
	 */
	public abstract String getSmiles();
	
	/**
	 * get neutral monoisotopic mass of precursor
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public abstract double getMonoisotopicMass();
	
	/**
	 * get number non-hydrogen atoms
	 * 
	 * @return
	 */
	public abstract int getNonHydrogenAtomCount();
	
	/**
	 * get number non-hydrogen connecting bonds
	 * 
	 * @return
	 */
	public abstract int getNonHydrogenBondCount();

	/**
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public abstract IMolecularFormula getMolecularFormula();
	
	
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
	public abstract int[] getBrokenBondIndeces();
	
	/**
	 * returns 0 if given and the current structure have certain properties in common
	 * possible properties can be mass, formula etc. 
	 * returns several other values dependent on the needs
	 * e.g. if comparing the mass of the structures -1 can indicate that the mass of the given structure is smaller than the mass
	 * of the current one 
	 * whereas a return value of 1 can indicate that the mass of the given structure is greater than the mass of the current one
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public abstract byte shareEqualProperties(IFragment molecularStructure) throws AtomTypeNotKnownFromInputListException;

	public abstract boolean isRealSubStructure(IFragment molecularStructure);
	
	public abstract boolean isSubStructure(IFragment molecularStructure);
	
	public abstract int[] getUniqueBrokenBondIndeces(IFragment molecularStructure);
	
	public abstract byte matchToPeak(IPeak peak, int precursorIonType, boolean isPositive, IMatch[] match);
	
	/**
	 * 
	 * @return
	 */
	public abstract IFragment clone();
	
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
