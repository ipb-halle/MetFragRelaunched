package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.molecularformula.BitArrayFragmentMolecularFormula;
import de.ipbhalle.metfraglib.molecularformula.HDFragmentMolecularFormulaWrapper;
import de.ipbhalle.metfraglib.parameter.Constants;

public class HDFragmentMassToPeakMatch extends FragmentMassToPeakMatch {
	
	protected java.util.ArrayList<Byte> numberOfDeuteriumsDifferToPeakMass;
	protected java.util.ArrayList<Byte> numberOfDeuteriumsOfMatchedFragment;
	protected java.util.ArrayList<Byte> numberOfVariableDeuteriumsDifferToPeakMass;
	
	public java.util.ArrayList<Byte> getNumberOfVariableDeuteriumsDifferToPeakMass() {
		return numberOfVariableDeuteriumsDifferToPeakMass;
	}

	public void setNumberOfVariableDeuteriumsDifferToPeakMass(
			java.util.ArrayList<Byte> numberOfVariableDeuteriumsDifferToPeakMass) {
		this.numberOfVariableDeuteriumsDifferToPeakMass = numberOfVariableDeuteriumsDifferToPeakMass;
	}

	public byte getBestMatchedFragmentDeuteriumDifference() {
		return this.deuteriumDifferenceOfBestFragment;
	}

	public byte getBestMatchedFragmentVariableDeuteriumDifference() {
		return this.deuteriumDifferenceOfBestFragment;
	}
	
	public byte getVariableDeuteriumDifferenceOfBestFragment() {
		return this.variableDeuteriumDifferenceOfBestFragment;
	}

	public void setVariableDeuteriumDifferenceOfBestFragment(
			byte variableDeuteriumDifferenceOfBestFragment) {
		this.variableDeuteriumDifferenceOfBestFragment = variableDeuteriumDifferenceOfBestFragment;
	}

	protected byte deuteriumDifferenceOfBestFragment; 
	protected byte variableDeuteriumDifferenceOfBestFragment; 
	protected byte numberOfDeuteriumsOfBestFragment;
	
	public byte getNumberOfDeuteriumsOfBestFragment() {
		return numberOfDeuteriumsOfBestFragment;
	}

	public void setNumberOfDeuteriumsOfBestFragment(byte numberOfDeuteriumsOfBestFragment) {
		this.numberOfDeuteriumsOfBestFragment = numberOfDeuteriumsOfBestFragment;
	}

	public HDFragmentMassToPeakMatch(IPeak matchedPeak) {
		super(matchedPeak);
		this.matchedFragmentMassesToTandemMassPeak = new java.util.ArrayList<Double>();
		this.numberOfHydrogensDifferToPeakMass = new java.util.ArrayList<Byte>();
		this.numberOfDeuteriumsDifferToPeakMass = new java.util.ArrayList<Byte>();
		this.numberOfVariableDeuteriumsDifferToPeakMass = new java.util.ArrayList<Byte>();
		this.numberOfDeuteriumsOfMatchedFragment = new java.util.ArrayList<Byte>();
	}
	
	public void shallowNullify() {
		super.shallowNullify();
		this.numberOfDeuteriumsDifferToPeakMass = null; 
		this.numberOfVariableDeuteriumsDifferToPeakMass = null;
		this.numberOfDeuteriumsOfMatchedFragment = null;
	}

	public void nullify() {
		super.nullify();
		this.numberOfDeuteriumsDifferToPeakMass = null; 
		this.numberOfVariableDeuteriumsDifferToPeakMass = null;
		this.numberOfDeuteriumsOfMatchedFragment = null;
	}
	
	public double getMatchedFragmentMassToTandemMassPeak(int matchedFragmentIndex) {
		return this.matchedFragmentMassesToTandemMassPeak.get(matchedFragmentIndex);
	}

	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass, double matchedFragmentMassToPeak, byte numberOfDeuteriumsOfMatchedFragment) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.matchedFragmentMassesToTandemMassPeak.add(matchedFragmentMassToPeak);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
		this.numberOfDeuteriumsOfMatchedFragment.add(numberOfDeuteriumsOfMatchedFragment);
		this.numberOfDeuteriumsDifferToPeakMass.add((byte)0);
	}
	
	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass, byte numberDeuteriumsDifferToPeakMass, double matchedFragmentMassToPeak, byte numberOfDeuteriumsOfMatchedFragment) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.matchedFragmentMassesToTandemMassPeak.add(matchedFragmentMassToPeak);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
		this.numberOfDeuteriumsOfMatchedFragment.add(numberOfDeuteriumsOfMatchedFragment);
		this.numberOfDeuteriumsDifferToPeakMass.add(numberDeuteriumsDifferToPeakMass);
	}

	public void addMatchedFragment(IFragment matchedFragment, byte numberHydrogensDifferToPeakMass, byte numberDeuteriumsDifferToPeakMass, double matchedFragmentMassToPeak, byte adductTypeIndex, byte numberVariableDeuteriumsDifferToPeakMass, byte numberOfDeuteriumsOfMatchedFragment) {
		this.matchedFragmentsList.addElement(matchedFragment);
		this.matchedFragmentMassesToTandemMassPeak.add(matchedFragmentMassToPeak);
		this.numberOfHydrogensDifferToPeakMass.add(numberHydrogensDifferToPeakMass);
		this.numberOfDeuteriumsDifferToPeakMass.add(numberDeuteriumsDifferToPeakMass);
		this.numberOfDeuteriumsOfMatchedFragment.add(numberOfDeuteriumsOfMatchedFragment);
		this.numberOfVariableDeuteriumsDifferToPeakMass.add(numberVariableDeuteriumsDifferToPeakMass);
		this.fragmentAdductTypeIndeces.add(adductTypeIndex);
	}

	public byte getNumberOfDeuteriumDifferToPeakMass(int matchedFragmentIndex) {
		return this.numberOfDeuteriumsDifferToPeakMass.get(matchedFragmentIndex);
	}

	public byte getNumberOfVariableDeuteriumDifferToPeakMass(int matchedFragmentIndex) {
		return this.numberOfVariableDeuteriumsDifferToPeakMass.get(matchedFragmentIndex);
	}

	public byte getNumberOfOverallHydrogensDifferToPeakMass(int matchedFragmentIndex) {
		return (byte)(Math.abs(this.numberOfHydrogensDifferToPeakMass.get(matchedFragmentIndex)) + Math.abs(this.numberOfDeuteriumsDifferToPeakMass.get(matchedFragmentIndex)));
	}

	public byte getNumberOfDeuteriumsOfMatchedFragment(int matchedFragmentIndex) {
		return this.numberOfDeuteriumsOfMatchedFragment.get(matchedFragmentIndex);
	}
	
	public void addToMatch(IMatch match) {
		HDFragmentMassToPeakMatch currentMatch = (HDFragmentMassToPeakMatch)match;
		FastBitArray atomsFastBitArrayOfCurrentFragment = ((AbstractTopDownBitArrayFragment)currentMatch.getMatchedFragmentList().getElement(0)).getAtomsFastBitArray();
		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++) {
			AbstractTopDownBitArrayFragment tmpFragment = (AbstractTopDownBitArrayFragment)this.matchedFragmentsList.getElement(i);
			if(tmpFragment.getAtomsFastBitArray().equals(atomsFastBitArrayOfCurrentFragment))
					return;
		}
		if(match.getBestMatchedFragment() != null) {
				this.addMatchedFragment(
					currentMatch.getBestMatchedFragment(), 
					currentMatch.getBestMatchedFragmentHydrogenDifference(),
					currentMatch.getBestMatchedFragmentDeuteriumDifference(),
					currentMatch.getBestMatchFragmentMass(),
					currentMatch.getBestMatchedFragmentAdductTypeIndex(),
					currentMatch.getVariableDeuteriumDifferenceOfBestFragment(), 
					currentMatch.getNumberOfDeuteriumsOfBestFragment());
		}
		else {
			this.addMatchedFragment(
				currentMatch.getMatchedFragmentList().getElement(0), 
				currentMatch.getNumberOfHydrogensDifferToPeakMass(0),
				currentMatch.getNumberOfDeuteriumDifferToPeakMass(0),
				currentMatch.getMatchedFragmentMassToTandemMassPeak(0),
				currentMatch.getFragmentsAdductTypeIndeces().get(0),
				currentMatch.getNumberOfVariableDeuteriumDifferToPeakMass(0),
				currentMatch.getNumberOfDeuteriumsOfMatchedFragment(0));
		}
	}
	
	public void initialiseBestMatchedFragment(int index) {
		super.initialiseBestMatchedFragment(index);
		this.deuteriumDifferenceOfBestFragment = this.numberOfDeuteriumsDifferToPeakMass.get(index);
		this.variableDeuteriumDifferenceOfBestFragment = this.numberOfVariableDeuteriumsDifferToPeakMass.get(index);
		this.numberOfDeuteriumsOfBestFragment = this.numberOfDeuteriumsOfMatchedFragment.get(index);
	}
	
	public String getModifiedFormulaStringOfBestMatchedFragment(IMolecularStructure precursorMolecule) {
		String formula = "";
		BitArrayFragmentMolecularFormula form = (BitArrayFragmentMolecularFormula)this.bestMatchedFragment.getMolecularFormula(precursorMolecule);
		HDFragmentMolecularFormulaWrapper formWrapper = new HDFragmentMolecularFormulaWrapper(form, (byte)(this.numberOfDeuteriumsOfBestFragment + this.variableDeuteriumDifferenceOfBestFragment));
		
		formula = "[" + formWrapper.toString();
		
		if(this.hydrogenDifferenceOfBestFragment == 1) formula += "+H";
		else if(this.hydrogenDifferenceOfBestFragment > 1) formula += "+" + this.hydrogenDifferenceOfBestFragment + "H";
		else if(this.hydrogenDifferenceOfBestFragment == -1) formula +=  "-H";
		else if(this.hydrogenDifferenceOfBestFragment < -1) formula += this.hydrogenDifferenceOfBestFragment + "H";
		if((this.deuteriumDifferenceOfBestFragment) == 1) formula += "+D";
		else if((this.deuteriumDifferenceOfBestFragment) > 1) formula += "+" + (this.deuteriumDifferenceOfBestFragment) + "D";
		else if((this.deuteriumDifferenceOfBestFragment) == -1) formula +=  "-D";
		else if((this.deuteriumDifferenceOfBestFragment) < -1) formula += (this.deuteriumDifferenceOfBestFragment) + "D";
		formula += "]";
		if(this.bestMatchedFragmentAdductTypeIndex != 0)
			formula += Constants.ADDUCT_NAMES.get(this.bestMatchedFragmentAdductTypeIndex);
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		return formula;
	}

	public String getModifiedFormulaStringOfMatchedFragment(IMolecularStructure precursorMolecule, int index) {
		/*
		 * in case there is a number of variable deuteriums we have to reduce the number of hydrogens
		 */
		BitArrayFragmentMolecularFormula form = (BitArrayFragmentMolecularFormula)this.matchedFragmentsList.getElement(index).getMolecularFormula(precursorMolecule);
		HDFragmentMolecularFormulaWrapper formWrapper = new HDFragmentMolecularFormulaWrapper(form, (byte)(this.getNumberOfDeuteriumsOfMatchedFragment(index) + this.getNumberOfVariableDeuteriumDifferToPeakMass(index)));
		
		String formula = "[" + formWrapper.toString();
		
		if(this.getNumberOfHydrogensDifferToPeakMass(index) == 1) formula += "+H";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) > 1) formula += "+" + this.getNumberOfHydrogensDifferToPeakMass(index) + "H";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) == -1) formula +=  "-H";
		else if(this.getNumberOfHydrogensDifferToPeakMass(index) < -1) formula += this.getNumberOfHydrogensDifferToPeakMass(index) + "H";
		if(this.getNumberOfDeuteriumDifferToPeakMass(index) == 1) formula += "+D";
		else if(this.getNumberOfDeuteriumDifferToPeakMass(index) > 1) formula += "+" + (this.getNumberOfDeuteriumDifferToPeakMass(index)) + "D";
		else if(this.getNumberOfDeuteriumDifferToPeakMass(index) == -1) formula +=  "-D";
		else if(this.getNumberOfDeuteriumDifferToPeakMass(index) < -1) formula += (this.getNumberOfDeuteriumDifferToPeakMass(index)) + "D";
		formula += "]";
		if(this.getFragmentsAdductTypeIndex(index) != 0) {
			formula += Constants.ADDUCT_NAMES.get(this.getFragmentsAdductTypeIndex(index));
		}
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		return formula;
		
	}
	/**
	 * 
	 */
	public String getModifiedFormulasStringOfBestMatchedFragment(IMolecularStructure precursorMolecule) {
		String formulas = "";
		String formula = "";
		

		BitArrayFragmentMolecularFormula form = (BitArrayFragmentMolecularFormula)this.bestMatchedFragment.getMolecularFormula(precursorMolecule);
		HDFragmentMolecularFormulaWrapper formWrapper = new HDFragmentMolecularFormulaWrapper(form, (byte)(this.numberOfDeuteriumsOfBestFragment + this.variableDeuteriumDifferenceOfBestFragment));
		
		formula = "[" + form.toString();
		
		if(this.hydrogenDifferenceOfBestFragment == 1) formula += "+H";
		else if(this.hydrogenDifferenceOfBestFragment > 1) formula += "+" + this.hydrogenDifferenceOfBestFragment + "H";
		else if(this.hydrogenDifferenceOfBestFragment == -1) formula +=  "-H";
		else if(this.hydrogenDifferenceOfBestFragment < -1) formula += this.hydrogenDifferenceOfBestFragment + "H";
		if((this.deuteriumDifferenceOfBestFragment) == 1) formula += "+D";
		else if((this.deuteriumDifferenceOfBestFragment) > 1) formula += "+" + (this.deuteriumDifferenceOfBestFragment) + "D";
		else if((this.deuteriumDifferenceOfBestFragment) == -1) formula +=  "-D";
		else if((this.deuteriumDifferenceOfBestFragment) < -1) formula += (this.deuteriumDifferenceOfBestFragment) + "D";
		formula += "]";
		if(this.bestMatchedFragmentAdductTypeIndex != 0)
			formula += Constants.ADDUCT_NAMES.get(this.bestMatchedFragmentAdductTypeIndex);
		if(this.isPositiveCharge)
			formula += "+";
		else
			formula += "-";
		formulas = formula;

		for(int i = 0; i < this.matchedFragmentsList.getNumberElements(); i++) {
			formula = "";
			if(this.getNumberOfVariableDeuteriumDifferToPeakMass(i) == 0) {
				formula = "[" + this.matchedFragmentsList.getElement(i).getMolecularFormula(precursorMolecule).toString();
			}
			else {
				/*
				 * in case there is a number of variable deuteriums we have to reduce the number of hydrogens
				 */
				form = (BitArrayFragmentMolecularFormula)this.matchedFragmentsList.getElement(i).getMolecularFormula(precursorMolecule);
				formWrapper = new HDFragmentMolecularFormulaWrapper(form, (byte)(this.getNumberOfDeuteriumsOfMatchedFragment(i) + this.getNumberOfVariableDeuteriumDifferToPeakMass(i)));
				
				formula = "[" + formWrapper.toString();
			}
			if(this.getNumberOfHydrogensDifferToPeakMass(i) == 1) formula += "+H";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) > 1) formula += "+" + this.getNumberOfHydrogensDifferToPeakMass(i) + "H";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) == -1) formula +=  "-H";
			else if(this.getNumberOfHydrogensDifferToPeakMass(i) < -1) formula += this.getNumberOfHydrogensDifferToPeakMass(i) + "H";
			if(this.getNumberOfDeuteriumDifferToPeakMass(i) == 1) formula += "+D";
			else if(this.getNumberOfDeuteriumDifferToPeakMass(i) > 1) formula += "+" + (this.getNumberOfDeuteriumDifferToPeakMass(i)) + "D";
			else if(this.getNumberOfDeuteriumDifferToPeakMass(i) == -1) formula +=  "-D";
			else if(this.getNumberOfDeuteriumDifferToPeakMass(i) < -1) formula += (this.getNumberOfDeuteriumDifferToPeakMass(i)) + "D";
			formula += "]";
			if(this.getFragmentsAdductTypeIndex(i) != 0) {
				formula += Constants.ADDUCT_NAMES.get(this.getFragmentsAdductTypeIndex(i));
			}
			if(this.isPositiveCharge)
				formula += "+";
			else
				formula += "-";
			formulas += "/" + formula;
		}
		return formulas;
	}
}
