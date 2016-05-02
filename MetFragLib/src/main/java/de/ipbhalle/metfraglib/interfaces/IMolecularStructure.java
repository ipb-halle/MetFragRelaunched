package de.ipbhalle.metfraglib.interfaces;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;

public interface IMolecularStructure {

	/**
	 * returns structure as IAtomContainer object
	 * 
	 * @return
	 */
	public IAtomContainer getStructureAsIAtomContainer();
	
	/**
	 * get neutral monoisotopic mass of precursor
	 * 
	 * @return
	 */
	public double getNeutralMonoisotopicMass();
	
	/**
	 * get number non-hydrogen atoms
	 * 
	 * @return
	 */
	public int getNonHydrogenAtomCount();
	
	/**
	 * 
	 * @return
	 */
	public double getMeanNodeDegree();
	
	/**
	 * 
	 * @return
	 */
	public int getNumNodeDegreeOne();
	
	/**
	 * get number non-hydrogen connecting bonds
	 * 
	 * @return
	 */
	public int getNonHydrogenBondCount();

	/**
	 * 
	 * @return
	 */
	public IMolecularFormula getMolecularFormula();

	/**
	 * 
	 * @return
	 */
	public short getNumberHydrogens();
	
	/**
	 * 
	 * @return
	 */
	public IFragment toFragment();
	
	/**
	 * 
	 * @throws AtomTypeNotKnownFromInputListException
	 */
	public void preprocessPrecursor() throws AtomTypeNotKnownFromInputListException, Exception;
	
	public boolean isAromaticBond(int index);
	
	/**
	 * delete all objects
	 */
	public void nullify();
}
