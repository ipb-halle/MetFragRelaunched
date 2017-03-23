package de.ipbhalle.metfraglib.fragment;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.match.FragmentMassToPeakMatch;
import de.ipbhalle.metfraglib.molecularformula.BitArrayFragmentMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;

/**
 * BitArrayFragment is an memory efficient way to store a fragment.
 * It is always related to a CDK AtomContainer object.
 * 
 * @author c-ruttkies
 *
 */
public class DefaultBitArrayFragment extends AbstractFragment {

	protected IMolecularFormula molecularFormula;

	/**
	 * atoms represented as BitArray object 
	 * 
	 */
	protected de.ipbhalle.metfraglib.BitArray atomsBitArray;
	protected de.ipbhalle.metfraglib.BitArray bondsBitArray;
	protected de.ipbhalle.metfraglib.BitArray brokenBondsBitArray;
	
	/**
	 * constructor setting precursor molecule of fragment
	 * 
	 * @param precursor
	 */
	public DefaultBitArrayFragment(IMolecularStructure precursorMolecule) {
		super(precursorMolecule);
		this.atomsBitArray = new BitArray(precursorMolecule.getNonHydrogenAtomCount(), true);
		this.bondsBitArray = new BitArray(precursorMolecule.getNonHydrogenBondCount(), true);
		this.brokenBondsBitArray = new BitArray(precursorMolecule.getNonHydrogenBondCount());
		this.treeDepth = 0;
	}
	
	/**
	 * 
	 * @param atomsBitArray
	 * @param bondsBitArray
	 */
	public DefaultBitArrayFragment(BitArrayPrecursor precursorMolecule, de.ipbhalle.metfraglib.BitArray atomsBitArray, de.ipbhalle.metfraglib.BitArray bondsBitArray, de.ipbhalle.metfraglib.BitArray brokenBondsBitArray) {
		super(precursorMolecule);
		this.atomsBitArray = atomsBitArray;
		this.bondsBitArray = bondsBitArray;
		this.brokenBondsBitArray = brokenBondsBitArray;
		this.treeDepth = 0;
	}
	
	public DefaultBitArrayFragment(BitArrayPrecursor precursorMolecule, de.ipbhalle.metfraglib.BitArray atomsBitArray) {
		super(precursorMolecule);
		this.atomsBitArray = atomsBitArray;
		this.bondsBitArray = new BitArray(precursorMolecule.getNonHydrogenBondCount(), false);
		this.brokenBondsBitArray = new BitArray(precursorMolecule.getNonHydrogenBondCount(), false);
		for(int i = 0; i < this.atomsBitArray.getSize(); i++) {
			short[] atomIndeces = precursorMolecule.getConnectedAtomIndecesOfAtomIndex((byte)i);
			if(this.atomsBitArray.get(i)) {
				for(int k = 0; k < atomIndeces.length; k++) {
					if(this.atomsBitArray.get(atomIndeces[k])) 
						this.bondsBitArray.set(precursorMolecule.getBondIndexFromAtomAdjacencyList((short)i, atomIndeces[k]) - 1);
					else 
						this.brokenBondsBitArray.set(precursorMolecule.getBondIndexFromAtomAdjacencyList((short)i, atomIndeces[k]) - 1);
				}
			}
			else {
				for(int k = 0; k < atomIndeces.length; k++) {
					if(this.atomsBitArray.get(atomIndeces[k])) 
						this.brokenBondsBitArray.set(precursorMolecule.getBondIndexFromAtomAdjacencyList((short)i, atomIndeces[k]) - 1);
				}
			}
		}
		
		this.treeDepth = 0;
	}

	public void initialiseMolecularFormula() throws AtomTypeNotKnownFromInputListException {
		this.molecularFormula = new BitArrayFragmentMolecularFormula((BitArrayPrecursor)this.precursorMolecule, this.atomsBitArray);
	}
	
	@Override
	public byte matchToPeak(IPeak peak, int precursorIonTypeIndex, boolean isPositive, IMatch[] fragmentPeakMatch) {
		if(fragmentPeakMatch == null || fragmentPeakMatch.length != 1) return -1;
		double[] ionisationTypeMassCorrection = new double [] {
			Constants.getIonisationTypeMassCorrection(precursorIonTypeIndex, isPositive),
			Constants.getIonisationTypeMassCorrection(0, isPositive)
		};
		byte[] signs = {1, -1};
		byte[] compareResultValuesToChangeHydrogenCheck = {1, -1};
		byte numberCompareResultsEqualPlusOne = 0;
		byte numberComparisons = 2;
		boolean matched = false;
		
		short numberHydrogens = this.molecularFormula.getNumberHydrogens();
		
		for(int i = 0; i < ionisationTypeMassCorrection.length; i++) {
			int substractHydrogenFromCharge = 0;
			if(i == 0 && precursorIonTypeIndex == 1) substractHydrogenFromCharge = 1;
			boolean[] toCheckHydrogenShiftType = {true, true};
			double currentFragmentMass = this.getMonoisotopicMass() + ionisationTypeMassCorrection[i];
			byte compareResult = ((TandemMassPeak)peak).matchesToMass(currentFragmentMass);
			if(compareResult == 0 && substractHydrogenFromCharge <= numberHydrogens) {
				/*
				 * if a former fragment has matched already then add the current fragment list to the match object
				 */
				if(fragmentPeakMatch[0] != null) {
					((FragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this, (byte)0, currentFragmentMass, i == 0 ? (byte)precursorIonTypeIndex : (byte)0);
				}
				/*
				 * if no former fragment matched before initialise the match object and add the fragment list
				 */
				else {
					fragmentPeakMatch[0] = new FragmentMassToPeakMatch(peak);
					fragmentPeakMatch[0].setIsPositiveCharge(isPositive);
					((FragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this, (byte)0, currentFragmentMass, i == 0 ? (byte)precursorIonTypeIndex : (byte)0);
				}
				matched = true;
			}
			else if(compareResultValuesToChangeHydrogenCheck[0] == compareResult) 
				toCheckHydrogenShiftType[0] = false;
			else if(compareResultValuesToChangeHydrogenCheck[1] == compareResult) 
				toCheckHydrogenShiftType[1] = false;
			if(compareResult == 1) numberCompareResultsEqualPlusOne++;
			/*
			 * iteration of hydrogenShift numbers to calculate adapted fragment masses
			 */
			int maximalHydrogenShift = this.getTreeDepth();
			for(byte hydrogenShift = 1; 
				hydrogenShift <= maximalHydrogenShift; 
				hydrogenShift++) 
			{
				/*
				 * check all signs to model hydrogen loss (-1) and hydrogen addition (+1)
				 */
				for(byte signIndex = 0; signIndex < signs.length; signIndex++) {
					if(!toCheckHydrogenShiftType[signIndex]) {
						continue;
					}
					if(numberHydrogens - (signs[signIndex] * hydrogenShift - substractHydrogenFromCharge) < 0) {
						continue;
					}
					/*
					 * calculate and check adapted fragment mass 
					 */
					double currentFragmentMassIonModeCorrected = currentFragmentMass + signs[signIndex] * hydrogenShift * Constants.HYDROGEN_MASS;
					compareResult = ((TandemMassPeak)peak).matchesToMass(currentFragmentMassIonModeCorrected);
					numberComparisons++;
					/*
					 * in case fragment matches to peak create match or add the fragment to the match
					 * if result is 0 then the fragment mass matched to the peak
					 */
					if(compareResult == 0) {
						/*
						 * if a former fragment has matched already then add the current fragment list to the match object
						 */
						if(fragmentPeakMatch[0] != null) {
							((FragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this, (byte)(signs[signIndex] * hydrogenShift), currentFragmentMassIonModeCorrected, i == 0 ? (byte)precursorIonTypeIndex : (byte)0);
						}
						/*
						 * if no former fragment matched before initialise the match object and add the fragment list
						 */
						else {
							fragmentPeakMatch[0] = new FragmentMassToPeakMatch(peak);
							fragmentPeakMatch[0].setIsPositiveCharge(isPositive);
							((FragmentMassToPeakMatch)fragmentPeakMatch[0]).addMatchedFragment(this, (byte)(signs[signIndex] * hydrogenShift), currentFragmentMassIonModeCorrected, i == 0 ? (byte)precursorIonTypeIndex : (byte)0);
						}
						matched = true;
					}
					/*
					 * if hydrogen removed/added from fragment mass and fragment mass smaller/greater than peak mass 
					 * there is no need to check further by removing/adding hydrogens
					 */
					else if(compareResultValuesToChangeHydrogenCheck[signIndex] == compareResult) 
						toCheckHydrogenShiftType[signIndex] = false;
					if(compareResult == 1) numberCompareResultsEqualPlusOne++;
				}
			}
		}
		if(matched == true) {
			this.hasMatched = true;
			return 0;
		}
		else if(numberCompareResultsEqualPlusOne == numberComparisons) return 1;
		return -1;
	}	

	/**
	 * 
	 * @param atomIndex
	 * @return
	 */
	public boolean getAtomBit(int atomIndex) {
		return this.atomsBitArray.get(atomIndex);
	}
	
	/**
	 * 
	 * @param atomIndex
	 * @param value
	 */
	public void setAtomBit(int atomIndex, boolean value) {
		this.atomsBitArray.set(atomIndex, value);
	}

	public de.ipbhalle.metfraglib.interfaces.IMolecularStructure getPrecursor() {
		return this.precursorMolecule;
	}

	public de.ipbhalle.metfraglib.BitArray getAtomsBitArray() {
		return this.atomsBitArray;
	}

	public void setAtomsBitArray(de.ipbhalle.metfraglib.BitArray atomsBitArray) {
		this.atomsBitArray = atomsBitArray;
	}

	public de.ipbhalle.metfraglib.BitArray getBondsBitArray() {
		return this.bondsBitArray;
	}

	public de.ipbhalle.metfraglib.BitArray getBrokenBondsBitArray() {
		return this.brokenBondsBitArray;
	}
	
	public void setBondsBitArray(de.ipbhalle.metfraglib.BitArray bondsBitArray) {
		this.bondsBitArray = bondsBitArray;
	}

	public void setBrokenBondsBitArray(de.ipbhalle.metfraglib.BitArray brokenBondsBitArray) {
		this.brokenBondsBitArray = brokenBondsBitArray;
	}

	public IAtomContainer getPrecursorAsIAtomContainer() {
		return this.precursorMolecule.getStructureAsIAtomContainer();
	}
	
	public boolean equals(IFragment fragment) {
		DefaultBitArrayFragment curFragment = (DefaultBitArrayFragment)fragment;
		return this.atomsBitArray.equals(curFragment.getAtomsBitArray());
	}
	
	public void setNumberHydrogens(int numberHydrogens) {
		this.molecularFormula.setNumberHydrogens((short)numberHydrogens);
	}
	
	public int getNumberHydrogens() {
		return this.molecularFormula.getNumberHydrogens();
	}
	
	public IMolecularFormula getMolecularFormula() {
		try {
			return new BitArrayFragmentMolecularFormula((BitArrayPrecursor)this.precursorMolecule, this.atomsBitArray);
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getSmiles() {
		IAtomContainer molecule = this.getStructureAsIAtomContainer();
		MoleculeFunctions.prepareAtomContainer(molecule, false);
		SmilesGenerator sg = new SmilesGenerator();
		String smiles = null;
		try {
			smiles = sg.create(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return smiles;
	}

	public String getAromaticSmiles() {
		IAtomContainer molecule = this.getStructureAsAromaticIAtomContainer();
		SmilesGenerator sg = SmilesGenerator.generic().aromatic();
		String smiles = null;
		try {
			smiles = sg.create(molecule);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		return smiles;
	}
	
	public IMolecularStructure getPrecursorMolecule() {
		return this.precursorMolecule;
	}

	public IAtomContainer getStructureAsAromaticIAtomContainer() {
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IAtomContainer fragmentStructure = builder.newInstance(IAtomContainer.class);
		if(this.atomsBitArray.cardinality() == 1) {
			fragmentStructure.addAtom(this.precursorMolecule.getStructureAsIAtomContainer().getAtom(this.atomsBitArray.getFirstSetBit()));
			if(this.precursorMolecule.getStructureAsIAtomContainer().getAtom(this.atomsBitArray.getFirstSetBit()).isAromatic())
				fragmentStructure.getAtom(0).setIsAromatic(true);
			else 
				fragmentStructure.getAtom(0).setIsAromatic(false);
			return fragmentStructure;
		}
		for(int i = 0; i < this.bondsBitArray.getSize(); i++) {
			if(this.bondsBitArray.get(i)) {
				IBond curBond = this.precursorMolecule.getStructureAsIAtomContainer().getBond(i);
				if(this.precursorMolecule.isAromaticBond(i)) curBond.setIsAromatic(true);
				for(IAtom atom : curBond.atoms()) {
					atom.setImplicitHydrogenCount(0);
					if(this.precursorMolecule.isAromaticBond(i)) atom.setIsAromatic(true);
					fragmentStructure.addAtom(atom);
				}
				fragmentStructure.addBond(curBond);
			}
		}
	//	loss of hydrogens
	//	MoleculeFunctions.prepareAtomContainer(fragmentStructure);
		
		return fragmentStructure;
	}
	
	public IAtomContainer getStructureAsIAtomContainer() {
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		IAtomContainer fragmentStructure = builder.newInstance(IAtomContainer.class);
		if(this.atomsBitArray.cardinality() == 1) {
			fragmentStructure.addAtom(this.precursorMolecule.getStructureAsIAtomContainer().getAtom(this.atomsBitArray.getFirstSetBit()));
			return fragmentStructure;
		}
		for(int i = 0; i < this.bondsBitArray.getSize(); i++) {
			if(this.bondsBitArray.get(i)) {
				IBond curBond = this.precursorMolecule.getStructureAsIAtomContainer().getBond(i);
				for(IAtom atom : curBond.atoms()) {
					fragmentStructure.addAtom(atom);
				}
				fragmentStructure.addBond(curBond);
			}
		}
	//	loss of hydrogens
	//	MoleculeFunctions.prepareAtomContainer(fragmentStructure);
		
		return fragmentStructure;
	}

	public double getMonoisotopicMass() {
		return this.getMolecularFormula().getMonoisotopicMass();
	}

	public int getNonHydrogenAtomCount() {
		return this.atomsBitArray.cardinality();
	}

	public int getNonHydrogenBondCount() {
		return this.bondsBitArray.cardinality();
	}

	public void setTreeDepth(byte treeDepth) {
		this.treeDepth = treeDepth;
	}
	
	public byte getTreeDepth() {
		return this.treeDepth;
	}

	public boolean equals(Object object) {
		if(this.atomsBitArray.equals(((DefaultBitArrayFragment)object).getAtomsBitArray())) return true;
		return false;
	}
	
	/**
	 * returns 0 if the mass of the current is equal to the mass of the argument fragment
	 * returns -1 if the mass of the current is smaller to the mass of the argument fragment
	 * returns 1 if the mass of the current is greater to the mass of the argument fragment
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public byte shareEqualProperties(IFragment fragment) {
		if(this.getMonoisotopicMass() == fragment.getMonoisotopicMass()) return 0;
		if(this.getMonoisotopicMass() > fragment.getMonoisotopicMass()) return 1;
		return -1;
	}

	public int[] getBrokenBondIndeces() {
		return this.brokenBondsBitArray.getSetIndeces();
	}
	
	public DefaultBitArrayFragment clone() {
		DefaultBitArrayFragment clone = new DefaultBitArrayFragment((BitArrayPrecursor)this.precursorMolecule, this.atomsBitArray.clone(), this.bondsBitArray.clone(), this.brokenBondsBitArray.clone());
	//	clone.setID(this.ID);
		clone.setNumberHydrogens(this.getNumberHydrogens());
		clone.setTreeDepth(this.treeDepth);
		return clone;
	}
	
	public void nullify() {
		super.nullify();
		this.atomsBitArray.nullify();
		this.bondsBitArray.nullify();
		this.brokenBondsBitArray.nullify();
	}

	/**
	 * is the argument fragment a real substructure of the current fragment
	 */
	public boolean isRealSubStructure(IFragment molecularStructure) {
		BitArray currentAtomsBitArray = ((DefaultBitArrayFragment)molecularStructure).getAtomsBitArray();
		if(currentAtomsBitArray.getSize() != this.atomsBitArray.getSize()) return false; 
		int currentAtomsBitArrayCardinality = 0;
		int thisAtomsBitArrayCardinality = 0;
		for(int i = 0; i < this.atomsBitArray.getSize(); i++) {
			boolean currentAtomsBitArrayValue = currentAtomsBitArray.get(i);
			boolean thisAtomsBitArrayValue = this.atomsBitArray.get(i);
			if(thisAtomsBitArrayValue) thisAtomsBitArrayCardinality++;
			if(currentAtomsBitArrayValue) currentAtomsBitArrayCardinality++;
			if(!thisAtomsBitArrayValue && currentAtomsBitArrayValue) return false;
		}
		if(thisAtomsBitArrayCardinality == currentAtomsBitArrayCardinality) return false;
		return true;
	}

	public boolean isSubStructure(IFragment molecularStructure) {
		BitArray currentAtomsBitArray = ((DefaultBitArrayFragment)molecularStructure).getAtomsBitArray();
		if(currentAtomsBitArray.getSize() != this.atomsBitArray.getSize()) return false; 
		for(int i = 0; i < this.atomsBitArray.getSize(); i++) {
			boolean currentAtomsBitArrayValue = currentAtomsBitArray.get(i);
			boolean thisAtomsBitArrayValue = this.atomsBitArray.get(i);
			if(!thisAtomsBitArrayValue && currentAtomsBitArrayValue) return false;
		}
		return true;
	}
	
	public int[] getUniqueBrokenBondIndeces(IFragment molecularStructure) {
		BitArray currentBrokenBondBitArray = ((DefaultBitArrayFragment)molecularStructure).getBrokenBondsBitArray();
		int numUniqueBondsSet = 0;
		for(int i = 0; i < currentBrokenBondBitArray.getSize(); i++) {
			if(currentBrokenBondBitArray.get(i) && !this.brokenBondsBitArray.get(i))
				numUniqueBondsSet++;
		}
		int[] uniqueBrokenBondIndeces = new int[numUniqueBondsSet];
		for(int i = 0; i < currentBrokenBondBitArray.getSize(); i++) {
			int index = 0;
			if(currentBrokenBondBitArray.get(i) && !this.brokenBondsBitArray.get(i)) {
				uniqueBrokenBondIndeces[index] = i;
			}	
		}
		return uniqueBrokenBondIndeces;
	}

	@Override
	public String getAtomsInfo() {
		return this.atomsBitArray.toString();
	}

	public IFragment getDifferenceFragment(IFragment molecularStructure) {
		BitArray diffBitArray = this.getAtomsBitArray().getDiff(((DefaultBitArrayFragment)molecularStructure).getAtomsBitArray());
		DefaultBitArrayFragment diffFragment = new DefaultBitArrayFragment((BitArrayPrecursor)this.precursorMolecule, diffBitArray);
		if(!diffFragment.isConnected()) return null;
		return diffFragment;
	}

	public IFragment getDifferenceFragment() {
		BitArray complete = new BitArray(this.getAtomsBitArray().getSize(), true);
		BitArray diffBitArray = complete.getDiff(this.getAtomsBitArray());
		DefaultBitArrayFragment diffFragment = new DefaultBitArrayFragment((BitArrayPrecursor)this.precursorMolecule, diffBitArray);
		if(!diffFragment.isConnected()) return null;
		return diffFragment;
	}
	
	public boolean isConnected() {
		if(this.atomsBitArray.cardinality() == 1) return true;
		BitArrayPrecursor pre = (BitArrayPrecursor)this.precursorMolecule;
		BitArray foundAtoms = new BitArray(this.atomsBitArray.getSize(), false);
		java.util.LinkedList<Integer> toCheck = new java.util.LinkedList<Integer>();
		toCheck.add(this.atomsBitArray.getFirstSetBit());
		while(toCheck.size() != 0) {
			int currentAtomIndex = toCheck.poll();
			short[] neighbors = pre.getConnectedAtomIndecesOfAtomIndex((short)currentAtomIndex);
			for(int k = 0; k < neighbors.length; k++) {
				if(this.atomsBitArray.get(neighbors[k]) && !foundAtoms.get(neighbors[k])) { 
					foundAtoms.set(neighbors[k]);
					toCheck.add((int)neighbors[k]);
				}
			}
		}
		if(foundAtoms.equals(this.atomsBitArray)) return true;
		return false;
	}
	
	@Override
	public String getBondsInfo() {
		return this.bondsBitArray.toString();
	}

	@Override
	public String getBrokenBondsInfo() {
		return this.brokenBondsBitArray.toString();
	}

}
