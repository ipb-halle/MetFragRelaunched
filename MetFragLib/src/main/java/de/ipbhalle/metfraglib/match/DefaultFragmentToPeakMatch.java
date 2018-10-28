package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.parameter.Constants;

/**
 * a fragment peak match that is calculated by an IAssigner
 * 
 * @author cruttkie
 *
 */
public class DefaultFragmentToPeakMatch implements IMatch {

	/**
	 * reference to peak of a peak list matched by a fragment
	 */
	protected final IPeak matchedPeak;
	/**
	 * list of grouped fragment lists matched to the peak
	 */
	protected boolean isPositiveCharge;
	protected FragmentList matchedFragmentsList;
	protected IFragment bestMatchedFragment;
	protected byte bestMatchedFragmentAdductTypeIndex;
	protected byte hydrogenDifferenceOfBestFragment;
	protected Double[] bestAttachedFragmentScores;
	protected Double[] bestAttachedOptimalValues;
	protected java.util.ArrayList<Byte> numberOfHydrogensDifferToPeakMass;
	protected java.util.ArrayList<Byte> fragmentAdductTypeIndeces;
	
	public DefaultFragmentToPeakMatch(IPeak matchedPeak) {
		this.matchedPeak = matchedPeak;
		this.matchedFragmentsList = new FragmentList();
		this.numberOfHydrogensDifferToPeakMass = new java.util.ArrayList<Byte>();
		this.fragmentAdductTypeIndeces = new java.util.ArrayList<Byte>();
	}

	public String getModifiedFormulaStringOfBestMatchedFragment(IMolecularStructure precursorMolecule) {
		String formula = "[" + this.bestMatchedFragment.getMolecularFormula(precursorMolecule).toString();
		if(this.hydrogenDifferenceOfBestFragment == 1) formula += "+H]";
		else if(this.hydrogenDifferenceOfBestFragment > 1) formula += "+" + this.hydrogenDifferenceOfBestFragment + "H]";
		else if(this.hydrogenDifferenceOfBestFragment == -1) formula +=  "-H]";
		else if(this.hydrogenDifferenceOfBestFragment < -1) formula += this.hydrogenDifferenceOfBestFragment + "H]";
		else formula += "]";
		if(this.bestMatchedFragmentAdductTypeIndex != 0)
			formula += Constants.ADDUCT_NAMES.get(this.bestMatchedFragmentAdductTypeIndex);
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		return formula;
	}

	public String getModifiedFormulaHtmlStringOfBestMatchedFragment(IMolecularStructure precursorMolecule) {
		String formula = "[" + this.bestMatchedFragment.getMolecularFormula(precursorMolecule).toString();
		formula = formula.replaceAll("([0-9]+)", "<sub>$1</sub>");
		if(this.hydrogenDifferenceOfBestFragment == 1) formula += "+H]";
		else if(this.hydrogenDifferenceOfBestFragment > 1) formula += "+" + this.hydrogenDifferenceOfBestFragment + "H]";
		else if(this.hydrogenDifferenceOfBestFragment == -1) formula +=  "-H]";
		else if(this.hydrogenDifferenceOfBestFragment < -1) formula += this.hydrogenDifferenceOfBestFragment + "H]";
		else formula += "]";
		if(this.bestMatchedFragmentAdductTypeIndex != 0)
			formula += Constants.ADDUCT_NAMES.get(this.bestMatchedFragmentAdductTypeIndex);
		if(this.isPositiveCharge)
			formula += "<sup>+</sup>";
		else
			formula += "<sup>-</sup>";
		return formula;
	}
	
	public String getModifiedFormulasStringOfBestMatchedFragment(IMolecularStructure precursorMolecule) {
		System.out.println("here");
		String formula = "[" + this.bestMatchedFragment.getMolecularFormula(precursorMolecule).toString();
		if(this.hydrogenDifferenceOfBestFragment == 1) formula += "+H]";
		else if(this.hydrogenDifferenceOfBestFragment > 1) formula += "+" + this.hydrogenDifferenceOfBestFragment + "H]";
		else if(this.hydrogenDifferenceOfBestFragment == -1) formula +=  "-H]";
		else if(this.hydrogenDifferenceOfBestFragment < -1) formula += this.hydrogenDifferenceOfBestFragment + "H]";
		else formula += "]";
		if(this.bestMatchedFragmentAdductTypeIndex != 0)
			formula += Constants.ADDUCT_NAMES.get(this.bestMatchedFragmentAdductTypeIndex);
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		String formulas = formula;
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++) {
			formula = "[" + this.matchedFragmentsList.getElement(i).getMolecularFormula(precursorMolecule).toString();
			if(this.getNumberOfHydrogensDifferToPeakMass(i) == 1) formula += "+H]";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) > 1) formula += "+" + this.getNumberOfHydrogensDifferToPeakMass(i) + "H]";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) == -1) formula +=  "-H]";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) < -1) formula += this.getNumberOfHydrogensDifferToPeakMass(i) + "H]";
			else formula += "]";
			if(this.getFragmentsAdductTypeIndex(i) != 0)
				formula += Constants.ADDUCT_NAMES.get(this.getFragmentsAdductTypeIndex(i));
			if(this.isPositiveCharge)
				formula += "+";
			else
				formula += "-";
			formulas += "/" + formula;
		}
		return formulas;
	}
	
	public String getModifiedFormulaStringOfMatchedFragment(IMolecularStructure precursorMolecule, int index) {
		String formula = "[" + this.matchedFragmentsList.getElement(index).getMolecularFormula(precursorMolecule).toString();
		if(this.getNumberOfHydrogensDifferToPeakMass(index) == 1) formula += "+H]";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) > 1) formula += "+" + this.getNumberOfHydrogensDifferToPeakMass(index) + "H]";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) == -1) formula +=  "-H]";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) < -1) formula += this.getNumberOfHydrogensDifferToPeakMass(index) + "H]";
		else formula += "]";
		if(this.getFragmentsAdductTypeIndex(index) != 0)
			formula += Constants.ADDUCT_NAMES.get(this.getFragmentsAdductTypeIndex(index));
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		return formula;
	}
	
	
	public String getMatchFragmentsAtomsInfo() {
		String string = this.bestMatchedFragment.getAtomsInfo();
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++)
			string += "/" + this.matchedFragmentsList.getElement(i).getAtomsInfo();
		return string;
	}

	public String getMatchFragmentsBondsInfo() {
		String string = this.bestMatchedFragment.getBondsInfo();
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++)
			string += "/" + this.matchedFragmentsList.getElement(i).getBondsInfo();
		return string;
	}

	public String getMatchFragmentsBrokenBondsInfo() {
		String string = this.bestMatchedFragment.getBrokenBondsInfo();
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++)
			string += "/" + this.matchedFragmentsList.getElement(i).getBrokenBondsInfo();
		return string;
	}
	
	public IPeak getMatchedPeak() {
		return this.matchedPeak;
	}

	public FragmentList getMatchedFragmentList() {
		return this.matchedFragmentsList;
	}

	public java.util.ArrayList<Byte> getFragmentsAdductTypeIndeces() {
		return this.fragmentAdductTypeIndeces;
	}
	
	public byte getNumberOfHydrogensDifferToPeakMass(int matchedFragmentIndex) {
		return this.numberOfHydrogensDifferToPeakMass.get(matchedFragmentIndex);
	}

	public byte getNumberOfOverallHydrogensDifferToPeakMass(int matchedFragmentIndex) {
		return this.numberOfHydrogensDifferToPeakMass.get(matchedFragmentIndex);
	}
	
	public byte getFragmentsAdductTypeIndex(int matchedFragmentIndex) {
		return this.fragmentAdductTypeIndeces.get(matchedFragmentIndex);
	}
	
	public void addToMatch(IMatch match) {
		DefaultFragmentToPeakMatch currentMatch = (DefaultFragmentToPeakMatch)match;
		if(currentMatch.getBestMatchedFragment() != null) {
			this.addMatchedFragment((DefaultBitArrayFragment)currentMatch.getBestMatchedFragment(), (byte)currentMatch.getBestMatchedFragmentHydrogenDifference(), (byte)currentMatch.getBestMatchedFragmentAdductTypeIndex());
		}
		else
			this.addMatchedFragment((DefaultBitArrayFragment)currentMatch.getMatchedFragmentList().getElement(0), (byte)currentMatch.getNumberOfHydrogensDifferToPeakMass(0), (byte)currentMatch.getFragmentsAdductTypeIndex(0));
	}
	
	/**
	 * adds a FragmentList whose mass matches to the peak of the current Peak
	 */
	public void addMatchedFragment(DefaultBitArrayFragment matchedFragment, byte hydrogenDifference, byte fragmentAdductType) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.numberOfHydrogensDifferToPeakMass.add(hydrogenDifference);
		this.fragmentAdductTypeIndeces.add(fragmentAdductType);
	}

	public void shallowNullify() {
		this.matchedFragmentsList.shallowNullify();
	//	this.numberOfHydrogensDifferToPeakMass = null;
	//	this.fragmentAdductTypeIndeces = null;
	}
	
	public void nullify() {
		this.matchedFragmentsList.nullify();
		this.numberOfHydrogensDifferToPeakMass = null;
		this.bestMatchedFragment.shallowNullify();
		this.fragmentAdductTypeIndeces = null;
	}
	
	public void setBestMatchedFragment(IFragment bestMatchedFragment) {
		this.bestMatchedFragment = bestMatchedFragment;
	}
	
	public IFragment getBestMatchedFragment() {
		return this.bestMatchedFragment;
	}
	
	public byte getBestMatchedFragmentHydrogenDifference() {
		return this.hydrogenDifferenceOfBestFragment;
	}

	public byte getBestMatchedFragmentAdductTypeIndex() {
		return this.bestMatchedFragmentAdductTypeIndex;
	}
	
	public void setHydrogenDifferenceOfBestFragment(byte hydrogenDifferenceOfBestFragment) {
		this.hydrogenDifferenceOfBestFragment = hydrogenDifferenceOfBestFragment;
	}
	
	public void initialiseBestMatchedFragment(int index) {
		this.bestMatchedFragment = this.matchedFragmentsList.getElement(index);
		this.hydrogenDifferenceOfBestFragment = this.numberOfHydrogensDifferToPeakMass.get(index);
		this.bestMatchedFragmentAdductTypeIndex = this.fragmentAdductTypeIndeces.get(index);
	}
	
	public void initialiseBestMatchedFragmentByFragmentID(int fragmentID) {
		int index = 0;
		for(index = 0; index < this.matchedFragmentsList.getNumberElements(); index++) {
			if(this.matchedFragmentsList.getElement(index).getID() == fragmentID) break;
		}
		this.bestMatchedFragment = this.matchedFragmentsList.getElement(index);
		this.hydrogenDifferenceOfBestFragment = this.numberOfHydrogensDifferToPeakMass.get(index);
		this.bestMatchedFragmentAdductTypeIndex = this.fragmentAdductTypeIndeces.get(index);
	}
	
	public String toString() {
		try {
			return this.matchedPeak.getMass() + "_" + this.matchedPeak.getIntensity();
		} catch (RelativeIntensityNotDefinedException e) {
			e.printStackTrace();
		}
		return this.matchedPeak.getMass() + " NaN";
	}

	public Double[] getBestAttachedFragmentScores() {
		return this.bestAttachedFragmentScores;
	}

	public void setBestAttachedFragmentScores(Double[] scores) {
		this.bestAttachedFragmentScores = scores;
	}

	@Override
	public Double[] getBestAttachedOptimalValues() {
		return this.bestAttachedOptimalValues;
	}

	@Override
	public void setBestAttachedOptimalValues(Double[] scores) {
		this.bestAttachedOptimalValues = scores;
	}
	
	@Override
	public boolean isPositiveCharge() {
		return this.isPositiveCharge;
	}

	@Override
	public void setIsPositiveCharge(boolean isPositiveCharge) {
		this.isPositiveCharge = isPositiveCharge;
	}
	
	public int getMatchedFragmentsSize() {
		return this.matchedFragmentsList.getNumberElements();
	}
}
