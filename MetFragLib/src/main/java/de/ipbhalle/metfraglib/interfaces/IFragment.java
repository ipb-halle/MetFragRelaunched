package de.ipbhalle.metfraglib.interfaces;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;

public interface IFragment {
	
	/**
	 * returns structure as IAtomContainer object
	 * 
	 * @return
	 */
	public IAtomContainer getStructureAsIAtomContainer(IMolecularStructure precursorMolecule);
	
	/**
	 * get smiles string of the fragment structure
	 * 
	 * @return
	 */
	public String getSmiles(IMolecularStructure precursorMolecule);
	
	/**
	 * get smiles string of the fragment structure
	 * 
	 * @return
	 */
	//public String getAromaticSmiles(IMolecularStructure precursorMolecule);
	
	/**
	 * get neutral monoisotopic mass of precursor
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public double getMonoisotopicMass(IMolecularStructure precursorMolecule);
	
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
//	public IMolecularFormula getMolecularFormula();
	public IMolecularFormula getMolecularFormula(IMolecularStructure precursorMolecule);
	
	public void setID(int id);
	
	public int getID();
	
	/**
	 * default zero
	 * 
	 * @return
	 */
	public byte getTreeDepth();
	
	public byte matchToPeak(IMolecularStructure precursorMolecule, IPeak peak, int precursorIonType, boolean isPositive, IMatch[] match);
	
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
	public byte shareEqualProperties(IMolecularStructure precursorMolecule, IFragment fragment) throws AtomTypeNotKnownFromInputListException;

	public boolean isRealSubStructure(IFragment molecularStructure);
	
	public boolean isSubStructure(IFragment molecularStructure);
	
	public IFragment getDifferenceFragment(IMolecularStructure precursorMolecule, IFragment molecularStructure);

	public IFragment getDifferenceFragment(IMolecularStructure precursorMolecule);
	
	public int[] getUniqueBrokenBondIndeces(IFragment molecularStructure);
	
	public boolean isConnected(IMolecularStructure precursorMolecule);
	
	public boolean hasMatched();
	
	public void setHasMatched();
	
	public boolean equals(IFragment fragment);
	
	public int hashCode();
	
	public void setIsBestMatchedFragment(boolean value);
	
	/**
	 * 
	 * @return
	 */
	public IFragment clone(IMolecularStructure precursorMolecule);
	
	public String getAtomsInfo();
	public String getBondsInfo();
	public String getBrokenBondsInfo();
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public void shallowNullify();
}
