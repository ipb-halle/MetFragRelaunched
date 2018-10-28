package de.ipbhalle.metfraglib.molecularformula;

import org.openscience.cdk.ChemObject;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.HDTopDownBitArrayPrecursor;

/**
 * 
 * efficient representation of a molecular formula
 * 
 * @author cruttkie
 *
 */
public class HDByteMolecularFormula extends ByteMolecularFormula {

	protected short numberDeuterium;

	public HDByteMolecularFormula() {}
	
	public HDByteMolecularFormula(HDTopDownBitArrayPrecursor precursorMolecule) throws AtomTypeNotKnownFromInputListException {
		super(precursorMolecule);
		this.numberDeuterium = precursorMolecule.getNumberOverallDeuteriums();
	}

	public HDByteMolecularFormula(String molecularFormula) throws AtomTypeNotKnownFromInputListException {
		this.initialise(molecularFormula);
	}
	
	public HDByteMolecularFormula clone() {
		HDByteMolecularFormula clone = new HDByteMolecularFormula();
		clone.setAtomsAsIndeces(this.atomsAsIndeces.clone());
		clone.setNumberOfAtoms(this.numberOfAtoms.clone());
		clone.setNumberHydrogens(this.numberHydrogens);
		clone.setNumberDeuterium(this.numberDeuterium);
		return clone;
	}

	public void setNumberDeuterium(short numberDeuterium) {
		this.numberDeuterium = numberDeuterium;
	}
	
	public short getNumberDeuterium() {
		return this.numberDeuterium;
	}

	public short getNumberElementsFromByte(byte atomIndex) {
		if(Constants.ELEMENTS.indexOf("D") == atomIndex)
			return this.numberDeuterium;
		if(Constants.ELEMENTS.indexOf("H") == atomIndex)
			return (short)(this.numberHydrogens - this.numberDeuterium);
		for (int i = 0; i < this.atomsAsIndeces.length; i++) {
			if(this.atomsAsIndeces[i] == atomIndex) return this.numberOfAtoms[i];
		}
		return 0;
	}
	
	@Override
	public void setNumberHydrogens(short numberHydrogens) {
		if(numberHydrogens < 0) return;
		if(numberHydrogens >= this.numberDeuterium) this.numberHydrogens = numberHydrogens;
		else if(this.numberDeuterium - numberHydrogens >= 0) {
			this.numberDeuterium = (short)(this.numberDeuterium - numberHydrogens);
		}
	}
	
	public boolean contains(String element) {
		if(element.equals("H")) {
			if(this.numberHydrogens > 0) return true;
			else return false;
		}
		if(element.equals("D")) {
			if(this.numberDeuterium > 0) return true;
			else return false;
		}
		byte byteToAtomSymbol = (byte)Constants.ELEMENTS.indexOf(element);
		if(byteToAtomSymbol == -1) return false;
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			if(this.atomsAsIndeces[i] == byteToAtomSymbol) return true;
		return false;
	}
	
	public double getMonoisotopicMass() {
		double monoisotopicMass = 0.0;
		/*
		 * calculate mass based on the number of present atom indeces
		 */
		for(int i = 0; i < atomsAsIndeces.length; i++) {
			monoisotopicMass += Constants.getMonoisotopicMassOfAtom(this.atomsAsIndeces[i]) * this.numberOfAtoms[i];
		}
		monoisotopicMass += Constants.getMonoisotopicMassOfAtom("H") * (this.numberHydrogens - this.numberDeuterium);
		monoisotopicMass += Constants.getMonoisotopicMassOfAtom("D") * (this.numberDeuterium);
		return MathTools.round(monoisotopicMass);
	}
	
	/**
	 * initialise molecular formula by sum formula string
	 * 
	 * @param molecularFormula
	 */
	protected void initialise(String molecularFormula) throws de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException {
		org.openscience.cdk.interfaces.IMolecularFormula cdkMolecularFormula = MolecularFormulaManipulator.getMolecularFormula(molecularFormula, new ChemObject().getBuilder());
		java.util.Iterator<org.openscience.cdk.interfaces.IIsotope> elements = cdkMolecularFormula.isotopes().iterator();
		java.util.ArrayList<String> elementSymbols = new java.util.ArrayList<String>();
		java.util.ArrayList<Short> elementSymbolNumbers = new java.util.ArrayList<Short>();
		this.numberHydrogens = 0;
		while(elements.hasNext()) {
			org.openscience.cdk.interfaces.IIsotope currentIsotope = elements.next();
			String symbol = this.getAtomSymbol(currentIsotope);
			if(symbol.equals("H")) this.numberHydrogens += (short)cdkMolecularFormula.getIsotopeCount(currentIsotope);
			else if(symbol.equals("D")) {
				this.numberDeuterium = (short)cdkMolecularFormula.getIsotopeCount(currentIsotope);
				this.numberHydrogens += this.numberDeuterium;
			}
			else {
				short number = (short)cdkMolecularFormula.getIsotopeCount(currentIsotope);
				elementSymbols.add(symbol);
				elementSymbolNumbers.add(number);
			}
		}
		this.atomsAsIndeces = new byte[elementSymbols.size()];
		this.numberOfAtoms = new short[elementSymbols.size()];
		for(int i = 0; i < this.atomsAsIndeces.length; i++) {
			this.atomsAsIndeces[i] = (byte)Constants.ELEMENTS.indexOf(elementSymbols.get(i));
			if(this.atomsAsIndeces[i] == 0 || this.atomsAsIndeces[i] == 1) this.containsC = true;
			if(this.atomsAsIndeces[i] == -1)
				throw new de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException(elementSymbols.get(i) + " not found");
			this.numberOfAtoms[i] = elementSymbolNumbers.get(i);
		}
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
		int deuteriumIndex = Constants.D_INDEX;
		int maxIndex = Math.max(Math.max(0, (this.numberHydrogens - this.numberDeuterium) == 0 ? 0 : hydrogenIndex), this.numberDeuterium == 0 ? 0 : deuteriumIndex);
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			if(this.atomsAsIndeces[i] > maxIndex) 
				maxIndex = this.atomsAsIndeces[i];
			
		short[] buckets = new short[maxIndex + 1];
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			buckets[this.atomsAsIndeces[i]] = this.numberOfAtoms[i];
		if(this.numberHydrogens != 0) buckets[hydrogenIndex] = (short)(this.numberHydrogens - this.numberDeuterium);
		if(this.numberDeuterium != 0) buckets[deuteriumIndex] = (short)this.numberDeuterium;
		
		boolean includedHydrogen = false;
		for(int i = 0; i < buckets.length; i++) {
			if(i > carbonIndex && this.containsC && !includedHydrogen) 
			{
				if((this.numberHydrogens - this.numberDeuterium) != 0) formula += "H" + (buckets[hydrogenIndex] == 1 ? "" : buckets[hydrogenIndex] + "");
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
