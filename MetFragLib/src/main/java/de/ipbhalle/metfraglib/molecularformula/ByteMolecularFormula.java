package de.ipbhalle.metfraglib.molecularformula;

import org.openscience.cdk.interfaces.IIsotope;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.parameter.Constants;

/**
 * 
 * efficient representation of a molecular formula
 * 
 * @author cruttkie
 *
 */
public class ByteMolecularFormula implements IMolecularFormula {

	/**
	 * indeces of atoms related to de.ipbhalle.metfrag.additionals.Constans
	 */
	protected byte[] atomsAsIndeces;
	protected short[] numberOfAtoms;
	
	protected short numberHydrogens;
	protected boolean containsC;
	
	public ByteMolecularFormula() {}
	
	/**
	 * generates formula 
	 * 
	 * @param molecule
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public ByteMolecularFormula(IMolecularStructure precursorMolecule) throws AtomTypeNotKnownFromInputListException {
		try {
			this.initialise(precursorMolecule);
		} catch (ExplicitHydrogenRepresentationException e) {
			e.printStackTrace();
		}
	}
	
	public ByteMolecularFormula(String molecularFormula) throws AtomTypeNotKnownFromInputListException {
		this.initialise(molecularFormula);
	}
	
	/**
	 * checks whether the current element is present in the molecular formula
	 * 
	 * @param element
	 * @return
	 */
	public boolean contains(String element) {
		if(element.equals("H")) {
			if(this.numberHydrogens > 0) return true;
			else return false;
		}
		byte byteToAtomSymbol = (byte)Constants.ELEMENTS.indexOf(element);
		if(byteToAtomSymbol == -1) return false;
		for(int i = 0; i < this.atomsAsIndeces.length; i++)
			if(this.atomsAsIndeces[i] == byteToAtomSymbol) return true;
		return false;
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
		buckets[hydrogenIndex] = this.numberHydrogens;
		
		boolean includedHydrogen = false;
		for(int i = 0; i < buckets.length; i++) {
			if(i > carbonIndex && this.containsC && !includedHydrogen) 
			{
				formula += "H" + (buckets[hydrogenIndex] == 1 ? "" : buckets[hydrogenIndex] + "");
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

	/**
	 * 
	 */
	public double getMonoisotopicMass() {
		double monoisotopicMass = 0.0;
		/*
		 * calculate mass based on the number of present atom indeces
		 */
		for(int i = 0; i < atomsAsIndeces.length; i++) {
			monoisotopicMass += Constants.getMonoisotopicMassOfAtom(this.atomsAsIndeces[i]) * this.numberOfAtoms[i];
		}
		monoisotopicMass += Constants.getMonoisotopicMassOfAtom("H") * this.numberHydrogens;
		return MathTools.round(monoisotopicMass, Constants.DEFAULT_NUMBER_OF_DIGITS_AFTER_ROUNDING);
	}
	
	/**
	 * initialise molecular formula by sum formula string
	 * 
	 * @param molecularFormula
	 */
	protected void initialise(String molecularFormula) throws de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException {
		this.parseMolecularFormula(molecularFormula);
		
	}
	
	/**
	 * initialise molecular formula by molecular structure
	 * 
	 * @param molecule
	 * @throws ExplicitHydrogenRepresentationException 
	 */
	protected void initialise(IMolecularStructure precursorMolecule) 
			throws 	de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException, 
					de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException {
		this.numberHydrogens = 0;
		java.util.Map<Byte, Short> elementsToCount = new java.util.HashMap<Byte, Short>();
		int numberElementsPresentInPrecursorMolecule = 0;
		
		for(int i = 0; i < precursorMolecule.getStructureAsIAtomContainer().getAtomCount(); i++) {
			String currentAtomSymbol = this.getAtomSymbol(precursorMolecule.getStructureAsIAtomContainer().getAtom(i));
			byte byteToAtomSymbol = (byte)Constants.ELEMENTS.indexOf(currentAtomSymbol);
			if(byteToAtomSymbol == 0 || byteToAtomSymbol == -1) this.containsC = true;
			if(byteToAtomSymbol == -1) {
				throw new de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException(currentAtomSymbol + " not found");
			}
			if(currentAtomSymbol.equals("H"))
				throw new de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException();
			if(elementsToCount.containsKey(byteToAtomSymbol))
				elementsToCount.put(byteToAtomSymbol, (short)(elementsToCount.get(byteToAtomSymbol) + 1));
			else {
				elementsToCount.put(byteToAtomSymbol, (short)1);
				numberElementsPresentInPrecursorMolecule++;
			}
			this.numberHydrogens += precursorMolecule.getStructureAsIAtomContainer().getAtom(i).getImplicitHydrogenCount();
		}
		
		java.util.Iterator<Byte> keys = elementsToCount.keySet().iterator();
		int index = 0;
		this.atomsAsIndeces = new byte[numberElementsPresentInPrecursorMolecule];
		this.numberOfAtoms = new short[numberElementsPresentInPrecursorMolecule];
		while(keys.hasNext()) {
			byte atomSymbol = keys.next();
			this.atomsAsIndeces[index] = atomSymbol;
			this.numberOfAtoms[index] = elementsToCount.get(atomSymbol);
			index++;
		}
	}

	public boolean compareTo(IMolecularFormula molecularFormula) {
		if(molecularFormula.getNumberHydrogens() != this.numberHydrogens) return false;
		return this.compareToWithoutHydrogen(molecularFormula);
	}
	
	public boolean compareTo(IMolecularFormula molecularFormula, short hydrogenDifference) {
		if(Math.abs(molecularFormula.getNumberHydrogens() - this.numberHydrogens) > hydrogenDifference) return false;
		return this.compareToWithoutHydrogen(molecularFormula);
	}
	
	public short getHydrogenDifference(IMolecularFormula molecularFormula) {
		return (short)Math.abs(molecularFormula.getNumberHydrogens() - this.numberHydrogens);
	}
	
	/**
	 * like compareTo without checking the hydrogen number
	 * 
	 * @param molecularFormula
	 * @return
	 */
	public boolean compareToWithoutHydrogen(IMolecularFormula molecularFormula) {
		byte[] atomsAsIndeces = ((ByteMolecularFormula)molecularFormula).getAtomsAsIndeces();
		short[] numberOfAtoms = ((ByteMolecularFormula)molecularFormula).getNumberOfAtoms();
		byte[] atomsAsIndeces_1 = atomsAsIndeces;
		byte[] atomsAsIndeces_2 = this.atomsAsIndeces;
		short[] numberOfAtoms_1 = numberOfAtoms;
		short[] numberOfAtoms_2 = this.numberOfAtoms;
		if(atomsAsIndeces_1.length < atomsAsIndeces_2.length) {
			atomsAsIndeces_2 = atomsAsIndeces;
			atomsAsIndeces_1 = this.atomsAsIndeces;
			numberOfAtoms_2 = numberOfAtoms;
			numberOfAtoms_1 = this.numberOfAtoms;
		}
		for(int i = 0; i < atomsAsIndeces_1.length; i++) {
			boolean found = false;
			for(int j = 0; j < atomsAsIndeces_2.length; j++) {
				if(atomsAsIndeces_1[i] == atomsAsIndeces_2[j]) {
					found = true;
					if(numberOfAtoms_1[i] != numberOfAtoms_2[j]) {
						return false;
					}
					break;
				}
			}
			if(!found && numberOfAtoms_1[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
	public byte[] getAtomsAsIndeces() {
		return atomsAsIndeces;
	}

	public void setAtomsAsIndeces(byte[] atomsAsIndeces) {
		this.atomsAsIndeces = atomsAsIndeces;
	}

	public short[] getNumberOfAtoms() {
		return numberOfAtoms;
	}

	public void setNumberOfAtoms(short[] numberOfAtoms) {
		this.numberOfAtoms = numberOfAtoms;
	}

	public short getNumberHydrogens() {
		return numberHydrogens;
	}

	@Override
	public void setNumberHydrogens(short numberHydrogens) {
		if(numberHydrogens >= 0) this.numberHydrogens = numberHydrogens;
	}

	public ByteMolecularFormula clone() {
		ByteMolecularFormula clone = new ByteMolecularFormula();
		clone.setAtomsAsIndeces(this.atomsAsIndeces.clone());
		clone.setNumberOfAtoms(this.numberOfAtoms.clone());
		clone.setNumberHydrogens(this.numberHydrogens);
		return clone;
	}
	
	public void nullify() {
		this.atomsAsIndeces = null;
		this.numberOfAtoms = null;
	}
	
	public byte getNumberElements() {
		return (byte)(this.atomsAsIndeces.length + (this.numberHydrogens == 0 ? 0 : 1));
	}

	protected void parseMolecularFormula(String formula) throws AtomTypeNotKnownFromInputListException {
		String splitableFormula = formula.trim().replaceAll("(\\[*[0-9]*[A-Z][a-z]{0,3}\\]*)([0-9]*)", "$1;$2\\|");
		String[] parts = splitableFormula.split("\\|");
		int numberElements = 0;
		for(int i = 0; i < parts.length; i++) 
			if(!parts[i].split(";")[0].equals("H") && !parts[i].split(";")[0].equals("D"))
				numberElements++;
		
		this.atomsAsIndeces = new byte[numberElements];
		this.numberOfAtoms = new short[numberElements];
		this.numberHydrogens = 0;
		
		int index = 0;
		for(int i = 0; i < parts.length; i++) {
			String[] tmp = parts[i].split(";");
			short count = 1;
			if(tmp.length > 1 && tmp[1].length() != 0) count = Short.parseShort(tmp[1]);
			if(tmp[0].equals("H")) this.numberHydrogens = count;
			else if(tmp[0].equals("D")) this.numberHydrogens = count;
			else {
				byte byteToAtomSymbol = (byte)Constants.ELEMENTS.indexOf(tmp[0]);
				if(byteToAtomSymbol == 0 || byteToAtomSymbol == 1) this.containsC = true;
				if(byteToAtomSymbol == -1) {
					throw new de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException(tmp[0] + " not found (" + formula + ")");
				}
				this.atomsAsIndeces[index] = byteToAtomSymbol;
				this.numberOfAtoms[index] = count;
				index++;
			}
		}
		
	}
	
	public boolean containsC() {
		return this.containsC;
	}

	public byte[] getAtomIndeces() {
		return this.atomsAsIndeces;
	}
	
	public String getAtomSymbol(IIsotope atom) {
		String symbol = atom.getSymbol();
		if(atom.getMassNumber() != null)
			symbol = "[" + atom.getMassNumber() + symbol + "]";
		return symbol;
	}
	
	public short getNumberElementsFromByte(byte atomIndex) {
		if(Constants.ELEMENTS.indexOf("H") == atomIndex)
			return this.numberHydrogens;
		for (int i = 0; i < this.atomsAsIndeces.length; i++) {
			if(this.atomsAsIndeces[i] == atomIndex) return this.numberOfAtoms[i];
		}
		return 0;
	}
	
	public boolean isContainsC() {
		return containsC;
	}

	/**
	 * 
	 */
	public String[] getElementsAsStringArray() {
		String[] elementsArray = new String[this.atomsAsIndeces.length + (this.numberHydrogens == 0 ? 0 : 1)];
		for(int i = 0; i < this.atomsAsIndeces.length; i++) {
			elementsArray[i] = Constants.ELEMENTS.get(this.atomsAsIndeces[i]);
		}
		if(this.numberHydrogens != 0) elementsArray[elementsArray.length - 1] = "H";
		return elementsArray;
	}
	
}
