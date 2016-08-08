package de.ipbhalle.metfraglib.fragment;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;
import de.ipbhalle.metfraglib.precursor.TopDownBitArrayPrecursor;

public class TopDownBitArrayFragment extends de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment {
	
	/**
	 * constructor setting all bits of atomsBitArray and bondsBitArray to true
	 * entire structure is represented
	 * 
	 * @param precursor
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public TopDownBitArrayFragment(TopDownBitArrayPrecursor precursor) {
		super(precursor);
	}

	public TopDownBitArrayFragment clone() {
		TopDownBitArrayFragment clone = null;
		try {
			clone = new TopDownBitArrayFragment(
					(TopDownBitArrayPrecursor)this.precursorMolecule, 
					this.atomsBitArray.clone(), 
					this.bondsBitArray.clone(), 
					this.brokenBondsBitArray.clone(), 
					this.getNumberHydrogens());
		} catch (AtomTypeNotKnownFromInputListException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		clone.setID(this.ID);
		clone.setTreeDepth(this.treeDepth);
		if(clone.hasMatched) clone.setHasMatched();
		try {
			clone.initialiseMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		return clone;
	}
	
	/**
	 * constructor setting bits of atomsBitArray and bondsBitArray by given ones
	 * 
	 * @param precursor
	 * @param atomsBitArray
	 * @param bondsBitArray
	 * @param brokenBondsBitArray
	 * @param numberHydrogens
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public TopDownBitArrayFragment(
			TopDownBitArrayPrecursor precursor,
			de.ipbhalle.metfraglib.BitArray atomsBitArray, 
			de.ipbhalle.metfraglib.BitArray bondsBitArray, 
			de.ipbhalle.metfraglib.BitArray brokenBondsBitArray, 
			int numberHydrogens) throws AtomTypeNotKnownFromInputListException
	{
		super(precursor, atomsBitArray, bondsBitArray, brokenBondsBitArray);
		this.initialiseMolecularFormula();
		this.setNumberHydrogens(numberHydrogens);
	}
	
	public double getMonoisotopicMass() {
		return this.molecularFormula.getMonoisotopicMass();
	}

	public boolean equals(Object topDownBitArrayFragment) {
		if(this.atomsBitArray.equals(((TopDownBitArrayFragment)topDownBitArrayFragment).getAtomsBitArray())) return true;
		return false;
	}
	
	/**
	 * main function of fragment generation
	 * traverse the given fragment and return two new fragments by removing bond with bondIndexToRemove
	 * 
	 * @param fragment
	 * @param bondNumber
	 * @param bondAtoms
	 * @return
	 */
	public AbstractTopDownBitArrayFragment[] traverseMolecule(short bondIndexToRemove, short[] indecesOfBondConnectedAtoms) {

		/*
		 * generate first fragment
		 */
		de.ipbhalle.metfraglib.BitArray atomArrayOfNewFragment_1 = new de.ipbhalle.metfraglib.BitArray(this.precursorMolecule.getNonHydrogenAtomCount());
		de.ipbhalle.metfraglib.BitArray bondArrayOfNewFragment_1 = new de.ipbhalle.metfraglib.BitArray(this.precursorMolecule.getNonHydrogenBondCount());
		de.ipbhalle.metfraglib.BitArray brokenBondArrayOfNewFragment_1 = this.getBrokenBondsBitArray().clone();
		int[] numberHydrogensOfNewFragment = new int[1];
		
		/*
		 * traverse to first direction from atomIndex connected by broken bond
		 */
		boolean stillOneFragment = this.traverseSingleDirection(indecesOfBondConnectedAtoms[0], indecesOfBondConnectedAtoms[1], bondIndexToRemove, 
				atomArrayOfNewFragment_1, bondArrayOfNewFragment_1, brokenBondArrayOfNewFragment_1, numberHydrogensOfNewFragment);
		
		TopDownBitArrayFragment firstNewGeneratedFragment = null;
		try {
			firstNewGeneratedFragment = new TopDownBitArrayFragment((TopDownBitArrayPrecursor)this.precursorMolecule, atomArrayOfNewFragment_1, bondArrayOfNewFragment_1, 
				brokenBondArrayOfNewFragment_1, numberHydrogensOfNewFragment[0]);
		} catch (AtomTypeNotKnownFromInputListException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		try {
			firstNewGeneratedFragment.initialiseMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e1) {
			e1.printStackTrace();
		}
		
		/*
		 * only one fragment is generated when a ring bond was broken
		 */
		if (stillOneFragment) {
			firstNewGeneratedFragment.setTreeDepth(this.getTreeDepth());
			firstNewGeneratedFragment.setAddedToQueueCounts((byte)(this.getAddedToQueueCounts() + 1));
			TopDownBitArrayFragment[] newFrags = { firstNewGeneratedFragment };
			return newFrags;
		}
		/*
		 * generate second fragment
		 */
		de.ipbhalle.metfraglib.BitArray atomArrayOfNewFragment_2 = new de.ipbhalle.metfraglib.BitArray(this.precursorMolecule.getNonHydrogenAtomCount());
		de.ipbhalle.metfraglib.BitArray bondArrayOfNewFragment_2 = new de.ipbhalle.metfraglib.BitArray(this.precursorMolecule.getNonHydrogenBondCount());
		de.ipbhalle.metfraglib.BitArray brokenBondArrayOfNewFragment_2 = this.getBrokenBondsBitArray().clone();
		numberHydrogensOfNewFragment[0] = 0;

		/*
		 * traverse the second direction from atomIndex connected by broken bond
		 */
		this.traverseSingleDirection(indecesOfBondConnectedAtoms[1], indecesOfBondConnectedAtoms[0], bondIndexToRemove, 
				atomArrayOfNewFragment_2, bondArrayOfNewFragment_2, brokenBondArrayOfNewFragment_2, numberHydrogensOfNewFragment);

		TopDownBitArrayFragment secondNewGeneratedFragment = null;
		try {
			secondNewGeneratedFragment = new TopDownBitArrayFragment((TopDownBitArrayPrecursor)this.precursorMolecule, atomArrayOfNewFragment_2, 
				bondArrayOfNewFragment_2, brokenBondArrayOfNewFragment_2, numberHydrogensOfNewFragment[0]);
		} catch (AtomTypeNotKnownFromInputListException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			secondNewGeneratedFragment.initialiseMolecularFormula();
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		
		firstNewGeneratedFragment.setTreeDepth((byte)(this.getTreeDepth() + 1));
	//	firstNewGeneratedFragment.setAddedToQueueCounts((byte)1);
		
		secondNewGeneratedFragment.setTreeDepth((byte)(this.getTreeDepth() + 1));
	//	secondNewGeneratedFragment.setAddedToQueueCounts((byte)1);

		TopDownBitArrayFragment[] newFrags = { firstNewGeneratedFragment, secondNewGeneratedFragment };
		
		/*
		newFrags[0].setPrecursorFragment(this);
		newFrags[1].setPrecursorFragment(this);
		
		this.addChild(newFrags[0]);
		this.addChild(newFrags[1]);
		*/
		
		return newFrags;

	}
	
	/**
	 * traverse the fragment to one direction starting from startAtomIndex to set BitArrays of new fragment
	 * 
	 * @param startAtomIndex
	 * @param fragment
	 * @param bondIndexToRemove
	 * @param atomArrayOfNewFragment
	 * @param bondArrayOfNewFragment
	 * @param brokenBondArrayOfNewFragment
	 * @param numberHydrogensOfNewFragment
	 * @return
	 */
	protected boolean traverseSingleDirection(short startAtomIndex, short endAtomIndex, short bondIndexToRemove, 
		de.ipbhalle.metfraglib.BitArray atomArrayOfNewFragment, de.ipbhalle.metfraglib.BitArray bondArrayOfNewFragment, 
		de.ipbhalle.metfraglib.BitArray brokenBondArrayOfNewFragment, int[] numberHydrogensOfNewFragment) 
	{
		de.ipbhalle.metfraglib.BitArray bondBitArrayOfCurrentFragment = this.getBondsBitArray();
		/* when traversing the fragment graph then we want to know if we already
		 * visited a node (atom)
		 * need to be done for checking of ringed structures
		 * if traversed an already visited atom, then no new fragment was
		 * generated
		 */
		de.ipbhalle.metfraglib.BitArray visited = new de.ipbhalle.metfraglib.BitArray(this.precursorMolecule.getNonHydrogenAtomCount());
		numberHydrogensOfNewFragment[0] = 0;

		/*
		 *  traverse molecule in the first direction
		 */
		java.util.Stack<short[]> toProcessConnectedAtoms = new java.util.Stack<short[]>();
		java.util.Stack<Short> toProcessAtom = new java.util.Stack<Short>();
		toProcessConnectedAtoms.push(((BitArrayPrecursor)this.precursorMolecule).getConnectedAtomIndecesOfAtomIndex(startAtomIndex));
		toProcessAtom.push(startAtomIndex);
		visited.set(startAtomIndex);
		boolean stillOneFragment = false;
		/*
		 *  set the first atom of possible new fragment
		 * atom is of the one direction of cutted bond
		 */
		atomArrayOfNewFragment.set(startAtomIndex);
		numberHydrogensOfNewFragment[0] += ((BitArrayPrecursor)this.precursorMolecule).getNumberHydrogensConnectedToAtomIndex(startAtomIndex);
		while (!toProcessConnectedAtoms.isEmpty()) {
			short[] nextAtoms = toProcessConnectedAtoms.pop();
			short midAtom = toProcessAtom.pop();
			for (int i = 0; i < nextAtoms.length; i++) {
				/*
				 *  did we visit the current atom already?
				 */
				short currentBondNumber = (short)(((BitArrayPrecursor)this.precursorMolecule).getBondIndexFromAtomAdjacencyList(nextAtoms[i], midAtom) - 1);
				
				if (!bondBitArrayOfCurrentFragment.get(currentBondNumber) || currentBondNumber == bondIndexToRemove) {
					continue;
				}
				/*
				 * if we visited the current atom already then we do not have to
				 * check it again
				 */
				if (visited.get(nextAtoms[i])) {
					bondArrayOfNewFragment.set(currentBondNumber);
					continue;
				}
				/*
				 * if we reach the second atom of the cleaved bond then still one
				 * fragment is present
				 */
				if (nextAtoms[i] == endAtomIndex) {
					stillOneFragment = true;
				}
				
				visited.set(nextAtoms[i]);
				atomArrayOfNewFragment.set(nextAtoms[i]);
				/*
				 * add number of hydrogens of current atom
				 */
				numberHydrogensOfNewFragment[0] += ((BitArrayPrecursor)this.precursorMolecule).getNumberHydrogensConnectedToAtomIndex(nextAtoms[i]);
				bondArrayOfNewFragment.set(((BitArrayPrecursor)this.precursorMolecule).getBondIndexFromAtomAdjacencyList(midAtom, nextAtoms[i]) - 1);
				toProcessConnectedAtoms.push(((BitArrayPrecursor)this.precursorMolecule).getConnectedAtomIndecesOfAtomIndex(nextAtoms[i]));
				toProcessAtom.push(nextAtoms[i]);
			}

		}

		brokenBondArrayOfNewFragment.set(bondIndexToRemove);
		bondArrayOfNewFragment.set(bondIndexToRemove, false);
		
		return stillOneFragment;
	}
	
	
}
