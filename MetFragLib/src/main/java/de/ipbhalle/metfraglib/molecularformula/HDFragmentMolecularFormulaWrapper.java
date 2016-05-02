package de.ipbhalle.metfraglib.molecularformula;

import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class HDFragmentMolecularFormulaWrapper {
	
	protected BitArrayFragmentMolecularFormula molecularFormula;
	protected byte numberDeuteriums;
	
	public HDFragmentMolecularFormulaWrapper(BitArrayFragmentMolecularFormula molecularFormula, byte numberDeuteriums) {
		this.molecularFormula = molecularFormula;
		this.numberDeuteriums = numberDeuteriums;
	}

	public IMolecularFormula getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(BitArrayFragmentMolecularFormula molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public byte getNumberDeuteriums() {
		return numberDeuteriums;
	}

	public void setNumberDeuteriums(byte numberDeuteriums) {
		this.numberDeuteriums = numberDeuteriums;
	}
	
	public String toString() {
		String formula = "";
		/*
		 * build non-standardised molecular formula string from index array 
		 */
		int hydrogenIndex = Constants.H_INDEX;
		int carbonIndex = Constants.C_INDEX;
		int deuteriumIndex = Constants.D_INDEX;
		int maxIndex = Math.max(Math.max(0, (this.molecularFormula.getNumberHydrogens() - this.numberDeuteriums) == 0 ? 0 : hydrogenIndex), this.numberDeuteriums == 0 ? 0 : deuteriumIndex);
		
		byte[] atomsAsIndeces = this.molecularFormula.getAtomIndeces();
		short[] numberOfAtoms = this.molecularFormula.getNumberOfAtoms();
		
		for(int i = 0; i < atomsAsIndeces.length; i++)
			if(atomsAsIndeces[i] > maxIndex) 
				maxIndex = atomsAsIndeces[i];
			
		short[] buckets = new short[maxIndex + 1];
		for(int i = 0; i < atomsAsIndeces.length; i++)
			buckets[atomsAsIndeces[i]] = numberOfAtoms[i];
		if(this.molecularFormula.getNumberHydrogens() != 0) buckets[hydrogenIndex] = (short)(this.molecularFormula.getNumberHydrogens() - this.numberDeuteriums);
		if(this.numberDeuteriums != 0) buckets[deuteriumIndex] = (short)this.numberDeuteriums;
		
		boolean includedHydrogen = false;
		for(int i = 0; i < buckets.length; i++) {
			if(i > carbonIndex && this.molecularFormula.isContainsC() && !includedHydrogen) 
			{
				if((this.molecularFormula.getNumberHydrogens() - this.numberDeuteriums)!= 0) formula += "H" + (buckets[hydrogenIndex] == 1 ? "" : buckets[hydrogenIndex] + "");
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
}
