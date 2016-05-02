package de.ipbhalle.metfraglib.interfaces;

public interface IMolecularFormula {

	public String toString();
	
	public boolean containsC();
	
	public short getNumberHydrogens();
	
	public double getMonoisotopicMass();
	
	public boolean compareTo(IMolecularFormula molecularFormula);

	public boolean compareToWithoutHydrogen(IMolecularFormula molecularFormula);

	public boolean compareTo(IMolecularFormula molecularFormula, short hydrogenDifference);
	
	public short getHydrogenDifference(IMolecularFormula molecularFormula); 
	
	public byte getNumberElements();
	
	public boolean contains(String element);
	
	public String[] getElementsAsStringArray();
	
	public void setNumberHydrogens(short numberHydrogens);
	
	public short getNumberElementsFromByte(byte atomIndex);
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public IMolecularFormula clone();
}
