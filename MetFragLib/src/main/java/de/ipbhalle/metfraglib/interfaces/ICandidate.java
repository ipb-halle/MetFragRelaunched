package de.ipbhalle.metfraglib.interfaces;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.list.MatchList;

public interface ICandidate {

	public String getIdentifier();

	/**
	 * retrieve the cdk atomcontainer
	 * 
	 * @return
	 * @throws Exception 
	 */
	public IAtomContainer getAtomContainer() throws Exception;
	
	public IAtomContainer getImplicitHydrogenAtomContainer() throws Exception;
	
	/**
	 * 
	 * @return
	 */
	public String getInChI();
	
	/**
	 * 
	 * @return
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public IMolecularFormula getMolecularFormula() throws AtomTypeNotKnownFromInputListException;
	
	public void setProperty(String key, Object value);
	
	public Object getProperty(String key);
	
	public void removeProperty(String key);
	
	public java.util.Hashtable<String, Object> getProperties();
	
	public void setMatchList(MatchList matchList);
	
	public MatchList getMatchList();
	
	public int getNumberPeaksExplained();
	
	public void setPrecursorMolecule(IMolecularStructure molecularStructure);
	
	public void initialisePrecursorCandidate() throws AtomTypeNotKnownFromInputListException, Exception;
	
	public IMolecularStructure getPrecursorMolecule();
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public void shallowNullify();
	
	public ICandidate clone();

	public String[] getPropertyNames();
	
}
