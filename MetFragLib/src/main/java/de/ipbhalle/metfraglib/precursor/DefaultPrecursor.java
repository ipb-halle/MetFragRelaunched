package de.ipbhalle.metfraglib.precursor;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;


public class DefaultPrecursor implements IMolecularStructure {

	/**
	 * precursor molecule 
	 * Important: hydrogens should be represented implicitly
	 * 
	 */
	protected final IAtomContainer precursorMolecule;
	protected double neutralMonoisotopicMass;
	protected IMolecularFormula molecularFormula;
	
	public DefaultPrecursor(IAtomContainer precursorMolecule) {
		this.precursorMolecule = precursorMolecule;
	}
	
	public final IAtomContainer getStructureAsIAtomContainer() {
		return this.precursorMolecule;
	}

	public short getNumberHydrogens() {
		return this.molecularFormula.getNumberHydrogens();
	}
	
	public double getNeutralMonoisotopicMass() {
		return this.neutralMonoisotopicMass;
	}

	public int getNonHydrogenAtomCount() {
		return this.precursorMolecule.getAtomCount();
	}

	public int getNonHydrogenBondCount() {
		return this.precursorMolecule.getBondCount();
	}
	
	public void preprocessPrecursor() throws AtomTypeNotKnownFromInputListException, Exception {
		this.molecularFormula = new ByteMolecularFormula(this);
		this.neutralMonoisotopicMass = this.molecularFormula.getMonoisotopicMass();
	}
	
	public DefaultBitArrayFragment toFragment() {
		return new DefaultBitArrayFragment(this);
	}

	public int getNumNodeDegreeOne() {
		int numDegreeOne = 0;
		for(int i = 0; i < this.precursorMolecule.getAtomCount(); i++) {
			numDegreeOne += this.precursorMolecule.getConnectedAtomsCount(this.precursorMolecule.getAtom(i)) == 1 ? 1 : 0;
		}
		return numDegreeOne;
	}
	
	public double getMeanNodeDegree() {
		double meanDegree = 0.0;
		for(int i = 0; i < this.precursorMolecule.getAtomCount(); i++) {
			meanDegree += this.precursorMolecule.getConnectedAtomsCount(this.precursorMolecule.getAtom(i));
		}
		meanDegree /= this.precursorMolecule.getAtomCount();
		return meanDegree;
	}
	
	/**
	 * get number of connected hydrogens of atom with atomIndex
	 * 
	 * @param atomIndex
	 * @return
	 */
	public int getNumberHydrogensConnectedToAtomIndex(int atomIndex) {
		return this.precursorMolecule.getAtom(atomIndex).getImplicitHydrogenCount();
	}

	public IMolecularFormula getMolecularFormula() {
		return this.molecularFormula;
	}

	public String getAtomSymbol(int index) {
		return this.precursorMolecule.getAtom(index).getSymbol();
	}
	
	public boolean isAromaticBond(int index) {
		return false;
	}
	
	public void nullify() {
		// TODO Auto-generated method stub
		
	}

}
