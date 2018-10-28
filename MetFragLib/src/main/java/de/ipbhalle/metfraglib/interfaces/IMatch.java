package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.list.FragmentList;

public interface IMatch {

	public IPeak getMatchedPeak();
	
	public FragmentList getMatchedFragmentList();

	public IFragment getBestMatchedFragment();
	
	public byte getBestMatchedFragmentHydrogenDifference();
	
	public Double[] getBestAttachedFragmentScores();
	
	public Double[] getBestAttachedOptimalValues();

	public void setBestAttachedFragmentScores(Double[] scores);
	
	public byte getNumberOfOverallHydrogensDifferToPeakMass(int index);
	
	public void setBestAttachedOptimalValues(Double[] scores);
	
	public boolean isPositiveCharge();
	
	public void setIsPositiveCharge(boolean isPositiveCharge);
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	public void shallowNullify();

	public String toString();
	
	public void addToMatch(IMatch match);
	
	public String getModifiedFormulaStringOfBestMatchedFragment(IMolecularStructure precursorMolecule);

	public String getModifiedFormulaStringOfMatchedFragment(IMolecularStructure precursorMolecule, int index);
	
	public String getModifiedFormulasStringOfBestMatchedFragment(IMolecularStructure precursorMolecule);
	
	public void initialiseBestMatchedFragment(int index);
	
	public void initialiseBestMatchedFragmentByFragmentID(int fragmentID);
	
	public String getMatchFragmentsBrokenBondsInfo();
	
	public String getMatchFragmentsBondsInfo();
	
	public String getMatchFragmentsAtomsInfo();
	
	public int getMatchedFragmentsSize();
}
