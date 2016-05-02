package de.ipbhalle.metfraglib.molecularformula;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.DefaultPrecursor;

public class BitArrayFragmentMolecularFormula extends ByteMolecularFormula {

	/*
	 * indeces relate to atom indeces of the relating precursor
	 * values relate to elements of de.ipbhalle.metfrag.additionals.Constans
	 */
	protected final DefaultPrecursor precursorMolecule;
	/*
	 * bit array marks precursor atoms as true that are set in the fragment
	 */
	
	public BitArrayFragmentMolecularFormula(DefaultPrecursor precursorMolecule) {
		this.precursorMolecule = precursorMolecule;
	}
	
	public BitArrayFragmentMolecularFormula(DefaultPrecursor precursorMolecule, BitArray atomsBitArray) throws AtomTypeNotKnownFromInputListException {
		super(precursorMolecule);
		this.precursorMolecule = precursorMolecule;
		this.initialise(atomsBitArray);
	}

	/**
	 * 
	 */
	private void initialise(BitArray atomsBitArray) {
		this.numberHydrogens = 0;
		for(int i = 0; i < this.numberOfAtoms.length; i++)
			this.numberOfAtoms[i] = 0;
		for(int i = 0; i < this.precursorMolecule.getStructureAsIAtomContainer().getAtomCount(); i++) {
			String currentAtomSymbol = this.getAtomSymbol(this.precursorMolecule.getStructureAsIAtomContainer().getAtom(i));
			byte atomNumber = (byte)Constants.ELEMENTS.indexOf(currentAtomSymbol);
			if(atomNumber == 0 || atomNumber == 1) this.containsC = true;
			if(atomsBitArray.get(i)) {
				for(int ii = 0; ii < this.atomsAsIndeces.length; ii++) {
					if(this.atomsAsIndeces[ii] == atomNumber) {
						this.numberOfAtoms[ii]++;
						break;
					}
				}
				this.numberHydrogens += this.precursorMolecule.getNumberHydrogensConnectedToAtomIndex(i);
			}
		}
	}
	
	/**
	 * 
	 * @param atomBitArray
	 * @return
	 */
	public double getMonoisotopicMass() {
		double monoisotopicMass = 0.0;
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			monoisotopicMass += Constants.getMonoisotopicMassOfAtom(this.atomsAsIndeces[i]) * this.numberOfAtoms[i];
		monoisotopicMass += Constants.HYDROGEN_MASS * this.numberHydrogens;
		return MathTools.round(monoisotopicMass, Constants.DEFAULT_NUMBER_OF_DIGITS_AFTER_ROUNDING);
	}
	
	/**
	 * 
	 */
	public String toString() {
		String formula = "";
		/*
		 * build non-standardised molecular formula string from index array 
		 */
		int hydrogenIndex = Constants.H_INDEX;
		int carbonIndex = Constants.C_INDEX;
		int maxIndex = this.numberHydrogens == 0 ? 0 : hydrogenIndex;
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			if(this.atomsAsIndeces[i] > maxIndex) 
				maxIndex = this.atomsAsIndeces[i];
		short[] buckets = new short[maxIndex + 1];
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			buckets[this.atomsAsIndeces[i]] = this.numberOfAtoms[i];
		if(this.numberHydrogens != 0) buckets[hydrogenIndex] = this.numberHydrogens;
		
		boolean includedHydrogen = false;
		for(int i = 0; i < buckets.length; i++) {
			if(i > carbonIndex && this.containsC && !includedHydrogen) 
			{
				if(this.numberHydrogens != 0 && buckets[hydrogenIndex] != 0) formula += "H"; 
				if(this.numberHydrogens != 0 && buckets[hydrogenIndex] > 1) formula += buckets[hydrogenIndex];
				includedHydrogen = true;
			}
			if(buckets[i] != 0) {
				if(includedHydrogen && i == hydrogenIndex) continue;
				formula += Constants.ELEMENTS.get(i);
				if(buckets[i] > 1) formula += buckets[i];
			}
		}

		return formula;
	}
	
	public BitArrayFragmentMolecularFormula clone() {
		BitArrayFragmentMolecularFormula clone = new BitArrayFragmentMolecularFormula(this.precursorMolecule);
		clone.setAtomsAsIndeces(this.atomsAsIndeces.clone());
		clone.setNumberOfAtoms(this.numberOfAtoms.clone());
		clone.setNumberHydrogens(this.numberHydrogens);
		return clone;
	}
}
