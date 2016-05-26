package de.ipbhalle.metfraglib.interfaces;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;

public interface IFragment {
	
	public IMolecularStructure getPrecursorMolecule();
	
	/**
	 * returns structure as IAtomContainer object
	 * 
	 * @return
	 */
	public IAtomContainer getStructureAsIAtomContainer();
	
	/**
	 * get smiles string of the fragment structure
	 * 
	 * @return
	 */
	public String getSmiles();
	
	/**
	 * get smiles string of the fragment structure
	 * 
	 * @return
	 */
	public String getAromaticSmiles();
	
	/**
	 * get neutral monoisotopic mass of precursor
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public double getMonoisotopicMass();
	
	/**
	 * get number non-hydrogen atoms
	 * 
	 * @return
	 */
	public int getNonHydrogenAtomCount();
	
	/**
	 * get number non-hydrogen connecting bonds
	 * 
	 * @return
	 */
	public int getNonHydrogenBondCount();

	/**
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public IMolecularFormula getMolecularFormula();
	
	public void setID(int id);
	
	public int getID();
	
	/**
	 * default zero
	 * 
	 * @return
	 */
	public byte getTreeDepth();
	
	public byte matchToPeak(IPeak peak, int precursorIonType, boolean isPositive, IMatch[] match);
	
	/**
	 * 
	 * @return
	 */
	public int[] getBrokenBondIndeces();
	
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
	public byte shareEqualProperties(IFragment molecularStructure) throws AtomTypeNotKnownFromInputListException;

	public boolean isRealSubStructure(IFragment molecularStructure);
	
	public boolean isSubStructure(IFragment molecularStructure);
	
	public IFragment getDifferenceFragment(IFragment molecularStructure);

	public IFragment getDifferenceFragment();
	
	public int[] getUniqueBrokenBondIndeces(IFragment molecularStructure);
	
	public boolean isConnected();
	
	public boolean hasMatched();
	
	public void setHasMatched();
	
	public boolean equals(IFragment fragment);
	
	public int hashCode();
	
	public void setIsBestMatchedFragment(boolean value);
	
	/**
	 * 
	 * @return
	 */
	public IFragment clone();
	
	public String getAtomsInfo();
	public String getBondsInfo();
	public String getBrokenBondsInfo();
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public void shallowNullify();
}
