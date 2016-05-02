package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.list.CandidateList;

public interface IDatabase {

	/**
	 * returns database identifiers of candidates matching mass criteria
	 * 
	 * @param monoisotopicMass
	 * @param ppm
	 * @return
	 * @throws MultipleHeadersFoundInInputDatabaseException 
	 * @throws Exception 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws MultipleHeadersFoundInInputDatabaseException, Exception;
	
	/**
	 * returns database identifiers based on parameters set in the given seetings
	 * 
	 * @param settings
	 * @return
	 * @throws Exception 
	 * @throws MultipleHeadersFoundInInputDatabaseException 
	 */
	public java.util.Vector<String> getCandidateIdentifiers() throws MultipleHeadersFoundInInputDatabaseException, Exception;

	/**
	 * returns database identifiers of candidates matching molecular formula criteria
	 * 
	 * @param molecularFormula
	 * @return
	 * @throws Exception 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(String molecularFormula) throws Exception;
	
	/**
	 * returns database identifiers of candidates matching given identifier criteria
	 * function should check if identifiers are present in the implemented database
	 * 
	 * @param identifiers
	 * @return
	 * @throws Exception 
	 * @throws MultipleHeadersFoundInInputDatabaseException 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(java.util.Vector<String> identifiers) throws MultipleHeadersFoundInInputDatabaseException, Exception;

	/**
	 * returns database identifiers of candidates matching given identifier criteria
	 * function should check if identifiers are present in the implemented database
	 * 
	 * @param identifiers
	 * @return
	 * @throws Exception 
	 */
	public java.util.Vector<String> getCandidateIdentifiers(String[] identifiers) throws Exception;
	
	/**
	 * returns one candidate based on its identifier
	 * 
	 * @param identifier
	 * @return
	 * @throws Exception 
	 */
	public ICandidate getCandidateByIdentifier(String identifier) throws DatabaseIdentifierNotFoundException, Exception;
	
	/**
	 * returns candidates based on their identifiers
	 * 
	 * @param identifiers
	 * @return
	 * @throws Exception 
	 */
	public CandidateList getCandidateByIdentifier(java.util.Vector<String> identifiers) throws Exception;

	/**
	 * delete all objects
	 */
	public void nullify();
}
