package de.ipbhalle.metfraglib.fragment;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.match.HDFragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.molecularformula.BitArrayFragmentMolecularFormula;
import de.ipbhalle.metfraglib.molecularformula.HDFragmentMolecularFormulaWrapper;

public class HDTopDownBitArrayFragmentWrapper extends AbstractTopDownBitArrayFragmentWrapper {

	protected int[] currentPeakIndexPointerHD;
	//0 - native precursor
	//1,2,.. - deuterated precursors
	//more than 1 only in case we have less exchanged hydrogens than expected
	protected int precursorIndex;
	
	public HDTopDownBitArrayFragmentWrapper(AbstractTopDownBitArrayFragment wrappedFragment) {
		super(wrappedFragment);
		this.currentPeakIndexPointerHD= new int[] {0}; 
		this.precursorIndex = 0;
	}

	public HDTopDownBitArrayFragmentWrapper(
			AbstractTopDownBitArrayFragment wrappedFragment, int currentPeakIndexPointer, int currentPeakIndexPointerHD) {
		super(wrappedFragment, currentPeakIndexPointer);
		this.currentPeakIndexPointerHD = new int[] {currentPeakIndexPointer};
		this.precursorIndex = 0;
	}

	public HDTopDownBitArrayFragmentWrapper(
			AbstractTopDownBitArrayFragment wrappedFragment, int currentPeakIndexPointer, int[] currentPeakIndexPointerArrayHD) {
		super(wrappedFragment, currentPeakIndexPointer);
		this.currentPeakIndexPointerHD = currentPeakIndexPointerArrayHD;
		this.precursorIndex = 0;
	}

	public int getCurrentPeakIndexPointerHD() {
		return this.currentPeakIndexPointerHD[this.precursorIndex];
	}

	public void setCurrentPeakIndexPointerHD(Integer currentPeakIndexPointerHD) {
		this.currentPeakIndexPointerHD[this.precursorIndex] = currentPeakIndexPointerHD;
	}

	public void setCurrentPeakIndexPointerHD(int precursorIndex, int currentPeakIndexPointerHD) {
		this.currentPeakIndexPointerHD[precursorIndex] = currentPeakIndexPointerHD;
	}

	public int getCurrentPeakIndexPointerHD(int precursorIndex) {
		return this.currentPeakIndexPointerHD[precursorIndex];
	}
	
	public int[] getCurrentPeakIndexPointerArrayHD() {
		return this.currentPeakIndexPointerHD;
	}
	
	/**
	 * check math for HD fragment
	 * 
	 * @param peak
	 * @param precursorIonTypeIndex
	 * @param isPositive
	 * @param fragmentPeakMatch
	 * @return
	 */
	public byte matchToPeak(IMolecularStructure precursorMolecule, IPeak peak, int precursorIonTypeIndex, boolean isPositive, IMatch[] fragmentPeakMatch) {
		if(fragmentPeakMatch == null || fragmentPeakMatch.length != 1) return -1;
		double[] ionisationTypeMassCorrection = new double [] {
			Constants.getIonisationTypeMassCorrection(precursorIonTypeIndex, isPositive),
			Constants.getIonisationTypeMassCorrection(0, isPositive)
		};
		//count number deuteriums of current fragment
		byte numberDeuteriums = 0;
		//get current precursor 
		HDTopDownBitArrayPrecursor currentPrecursor = (HDTopDownBitArrayPrecursor)precursorMolecule;
		
		for(int i = 0; i < this.wrappedFragment.getAtomsFastBitArray().getSize(); i++) 
			if(this.wrappedFragment.getAtomsFastBitArray().get(i)) 
				numberDeuteriums += currentPrecursor.getNumberDeuteriumsConnectedToAtomIndex(this.precursorIndex, i);
		//correct ionisation type in case no deuterium is attached to fragment
		if(numberDeuteriums == 0 && Constants.ADDUCT_NOMINAL_MASSES.get(precursorIonTypeIndex) == -2)
			ionisationTypeMassCorrection[0] = Constants.getIonisationTypeMassCorrection(Constants.ADDUCT_NOMINAL_MASSES.indexOf(-1), isPositive);
		
		//number variable deuteriums are equal over all fragments
		byte variableDeuteriums = (byte)currentPrecursor.getNumberVariableDeuteriums();
		double[] shifts = new double[(this.wrappedFragment.getTreeDepth() * 2) + 1];
		shifts[0] = 0.0;
		int index = 1;
		//hydrogen/deuterium shift array
		boolean[] toUseForHydrogen = new boolean[(this.wrappedFragment.getTreeDepth() * 2) + 1];
		boolean[] toUseForDeuterium = new boolean[(this.wrappedFragment.getTreeDepth() * 2) + 1];
		/*
		 * set all shifts as possible
		 */
		for(int i = 0; i < toUseForHydrogen.length; i++) {
			toUseForHydrogen[i] = true;
			toUseForDeuterium[i] = true;
		}
		for(int i = 1; i <= this.wrappedFragment.getTreeDepth(); i++) {
			shifts[index++] = i;
			shifts[index++] = -1.0 * (double)i;
		}
		
		/*
		 * find shifts that are not possible
		 */
		for(int i = 1; i <= this.wrappedFragment.getTreeDepth(); i++) 
			if(i + (this.wrappedFragment.getNumberHydrogens() - variableDeuteriums) > currentPrecursor.getNumberHydrogens()) { 
				toUseForHydrogen[2 * i - 1] = false;
				toUseForHydrogen[2 * i] = false;
			}
		for(int i = 1; i <= this.wrappedFragment.getTreeDepth(); i++) {
			if(i + numberDeuteriums > currentPrecursor.getNumberOverallDeuteriums()) {
				toUseForDeuterium[2 * i - 1] = false;
				toUseForDeuterium[2 * i] = false;
			}
		}
		byte numberCompareResultsEqualPlusOne = 0;
		byte numberCompareResultsEqualMinusOne = 0;
		byte numberComparisons = 0;
		boolean matched = false;
		
		short numberDeuteriumsPrecusor = currentPrecursor.getNumberOverallDeuteriums();
		short numberHydrogensPrecusor = currentPrecursor.getNumberHydrogens();
		
		for(int p = 0; p < ionisationTypeMassCorrection.length; p++) {
			int substractDeuteriumFromCharge = 0;
			int substractHydrogenFromCharge = 0;
			if(p == 0 && precursorIonTypeIndex == 1) substractHydrogenFromCharge = 1;
			if(p == 0 && precursorIonTypeIndex == 3) substractDeuteriumFromCharge = 1;
			for(int i = 0; i < toUseForDeuterium.length; i++) {
				if(!toUseForDeuterium[i]) continue;
				for(int j = 0; j < toUseForHydrogen.length; j++) {
					// loop for variable deuteriums
					for(byte k = 0; k <= variableDeuteriums; k++) {
						if(!toUseForHydrogen[i]) {
							continue;
						}
						if(Math.abs(shifts[i]) + Math.abs(shifts[j]) > this.wrappedFragment.getTreeDepth()) {
							continue;
						}
						//check number overall hydrogens smaller zero
						if(shifts[i] + shifts[j] + this.wrappedFragment.getNumberHydrogens() - substractHydrogenFromCharge < 0) {
							continue;
						}
						//check number normal hydrogens smaller zero (shifts[j] -> number shifted hydrogens)
						if(shifts[j] + (this.wrappedFragment.getNumberHydrogens() - numberDeuteriums) - substractHydrogenFromCharge < 0) {
							continue;
						}
						//check number overall hydrogens greater than hydrogens available
						if((int)(shifts[i] + shifts[j] + this.wrappedFragment.getNumberHydrogens()) > numberHydrogensPrecusor) {
							continue;
						}
						//number of overall deuteriums of fragment must not be smaller than 0
						if(shifts[i] + k + numberDeuteriums - substractDeuteriumFromCharge < 0) {
							continue;
						}
						//number of overall deuteriums of fragment including shift, variable, real must not exceed the number
						//of deuteriums of the precursor
						if(shifts[i] + k + numberDeuteriums > numberDeuteriumsPrecusor) {
							continue;
						}
						numberComparisons++;
						//calculate corrected fragment mass
						//includes hydrogen/deuterium shift
						double monoisotopicFragmentMass = this.wrappedFragment.getMonoisotopicMass(precursorMolecule);
						//correct fragment mass by number of exchanged hydrogens
						monoisotopicFragmentMass += Constants.getMonoisotopicMassOfAtom("D") * (double)numberDeuteriums - Constants.getMonoisotopicMassOfAtom("H") * (double)numberDeuteriums;
						
						double currentFragmentMass = monoisotopicFragmentMass + ionisationTypeMassCorrection[p]
							+ (shifts[j]) * Constants.getMonoisotopicMassOfAtom("H") 
							+ (Constants.getMonoisotopicMassOfAtom("D") * (double)k - Constants.getMonoisotopicMassOfAtom("H") * (double)k) 
							+ (shifts[i] * Constants.getMonoisotopicMassOfAtom("D"));
						byte compareResult = ((TandemMassPeak)peak).matchesToMass(currentFragmentMass);
						if(compareResult == 0) {
							if(fragmentPeakMatch[0] != null) {
								((HDFragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this.wrappedFragment, (byte)shifts[j], (byte)shifts[i], currentFragmentMass, p == 0 ? (byte)precursorIonTypeIndex : (byte)0, k, numberDeuteriums);
							}
							else {
								fragmentPeakMatch[0] = new HDFragmentMassToPeakMatch(peak);
								fragmentPeakMatch[0].setIsPositiveCharge(isPositive);
								((HDFragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this.wrappedFragment, (byte)shifts[j], (byte)shifts[i], currentFragmentMass, p == 0 ? (byte)precursorIonTypeIndex : (byte)0, k, numberDeuteriums);
							}
							matched = true;
							((HDFragmentMassToPeakMatch)fragmentPeakMatch[0]).setIsPositiveCharge(isPositive);
						}
						else if(compareResult == 1) numberCompareResultsEqualPlusOne++;
						else if(compareResult == -1) numberCompareResultsEqualMinusOne++;
					}	
				}
			}
		}

		if(matched) {
			this.wrappedFragment.setHasMatched();
			return 0;
		}
		if(numberCompareResultsEqualPlusOne == numberComparisons) return 1;
		if(numberCompareResultsEqualMinusOne == numberComparisons) return -1; 
		return -1;
	}
	
	public int getPrecursorIndex() {
		return precursorIndex;
	}

	public void setPrecursorIndex(int precursorIndex) {
		this.precursorIndex = precursorIndex;
	}

	public HDFragmentMolecularFormulaWrapper getMolecularFormula(IMolecularStructure precursorMolecule) throws AtomTypeNotKnownFromInputListException {
		HDTopDownBitArrayPrecursor currentPrecursor = (HDTopDownBitArrayPrecursor)precursorMolecule;
		BitArrayFragmentMolecularFormula formula = new BitArrayFragmentMolecularFormula(currentPrecursor, this.wrappedFragment.getAtomsFastBitArray());
		byte numberDeuteriums = 0;
		for(int i = 0; i < this.wrappedFragment.getAtomsFastBitArray().getSize(); i++) 
			if(this.wrappedFragment.getAtomsFastBitArray().get(i)) 
				numberDeuteriums += currentPrecursor.getNumberDeuteriumsConnectedToAtomIndex(this.precursorIndex, i);
		return new HDFragmentMolecularFormulaWrapper(formula, numberDeuteriums);
	}
	
	public void shallowNullify() {
		super.shallowNullify();
		this.currentPeakIndexPointerHD = null;
	}
}
