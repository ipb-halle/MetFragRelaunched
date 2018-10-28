package de.ipbhalle.metfraglib.fragment;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;
import de.ipbhalle.metfraglib.precursor.TopDownBitArrayPrecursor;

public class TopDownBitArrayFragment extends de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment {
	
	/**
	 * constructor setting all bits of atomsFastBitArray and bondsFastBitArray to true
	 * entire structure is represented
	 * 
	 * @param precursor
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public TopDownBitArrayFragment(TopDownBitArrayPrecursor precursor) {
		super(precursor);
	}

	public TopDownBitArrayFragment clone(IMolecularStructure precursorMolecule) {
		TopDownBitArrayFragment clone = null;
		try {
			clone = new TopDownBitArrayFragment(
					(TopDownBitArrayPrecursor)precursorMolecule, 
					this.atomsFastBitArray.clone(), 
					this.bondsFastBitArray.clone(), 
					this.brokenBondsFastBitArray.clone(), 
					this.getNumberHydrogens());
		} catch (AtomTypeNotKnownFromInputListException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		clone.setID(this.ID);
		clone.setTreeDepth(this.treeDepth);
		if(clone.hasMatched) clone.setHasMatched();
		clone.setNumberHydrogens(this.getNumberHydrogens());
		return clone;
	}
	
	/**
	 * constructor setting bits of atomsFastBitArray and bondsFastBitArray by given ones
	 * 
	 * @param precursor
	 * @param atomsFastBitArray
	 * @param bondsFastBitArray
	 * @param brokenBondsFastBitArray
	 * @param numberHydrogens
	 * @throws AtomTypeNotKnownFromInputListException 
	 */
	public TopDownBitArrayFragment(
			TopDownBitArrayPrecursor precursor,
			de.ipbhalle.metfraglib.FastBitArray atomsFastBitArray, 
			de.ipbhalle.metfraglib.FastBitArray bondsFastBitArray, 
			de.ipbhalle.metfraglib.FastBitArray brokenBondsFastBitArray, 
			int numberHydrogens) throws AtomTypeNotKnownFromInputListException
	{
		super(precursor, atomsFastBitArray, bondsFastBitArray, brokenBondsFastBitArray);
		this.setNumberHydrogens(numberHydrogens);
	}

	public boolean equals(Object topDownFastBitArrayFragment) {
		if(this.atomsFastBitArray.equals(((TopDownBitArrayFragment)topDownFastBitArrayFragment).getAtomsFastBitArray())) return true;
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
	public AbstractTopDownBitArrayFragment[] traverseMolecule(IMolecularStructure precursorMolecule, short bondIndexToRemove, short[] indecesOfBondConnectedAtoms) {

		/*
		 * generate first fragment
		 */
		de.ipbhalle.metfraglib.FastBitArray atomArrayOfNewFragment_1 = new de.ipbhalle.metfraglib.FastBitArray(precursorMolecule.getNonHydrogenAtomCount());
		de.ipbhalle.metfraglib.FastBitArray bondArrayOfNewFragment_1 = new de.ipbhalle.metfraglib.FastBitArray(precursorMolecule.getNonHydrogenBondCount());
		de.ipbhalle.metfraglib.FastBitArray brokenBondArrayOfNewFragment_1 = this.getBrokenBondsFastBitArray().clone();
		int[] numberHydrogensOfNewFragment = new int[1];
		
		/*
		 * traverse to first direction from atomIndex connected by broken bond
		 */
		boolean stillOneFragment = this.traverseSingleDirection(precursorMolecule, indecesOfBondConnectedAtoms[0], indecesOfBondConnectedAtoms[1], bondIndexToRemove, 
				atomArrayOfNewFragment_1, bondArrayOfNewFragment_1, brokenBondArrayOfNewFragment_1, numberHydrogensOfNewFragment);
		
		TopDownBitArrayFragment firstNewGeneratedFragment = null;
		try {
			firstNewGeneratedFragment = new TopDownBitArrayFragment((TopDownBitArrayPrecursor)precursorMolecule, atomArrayOfNewFragment_1, bondArrayOfNewFragment_1, 
				brokenBondArrayOfNewFragment_1, numberHydrogensOfNewFragment[0]);
		} catch (AtomTypeNotKnownFromInputListException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
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
		de.ipbhalle.metfraglib.FastBitArray atomArrayOfNewFragment_2 = new de.ipbhalle.metfraglib.FastBitArray(precursorMolecule.getNonHydrogenAtomCount());
		de.ipbhalle.metfraglib.FastBitArray bondArrayOfNewFragment_2 = new de.ipbhalle.metfraglib.FastBitArray(precursorMolecule.getNonHydrogenBondCount());
		de.ipbhalle.metfraglib.FastBitArray brokenBondArrayOfNewFragment_2 = this.getBrokenBondsFastBitArray().clone();
		numberHydrogensOfNewFragment[0] = 0;

		/*
		 * traverse the second direction from atomIndex connected by broken bond
		 */
		this.traverseSingleDirection(precursorMolecule, indecesOfBondConnectedAtoms[1], indecesOfBondConnectedAtoms[0], bondIndexToRemove, 
				atomArrayOfNewFragment_2, bondArrayOfNewFragment_2, brokenBondArrayOfNewFragment_2, numberHydrogensOfNewFragment);

		TopDownBitArrayFragment secondNewGeneratedFragment = null;
		try {
			secondNewGeneratedFragment = new TopDownBitArrayFragment((TopDownBitArrayPrecursor)precursorMolecule, atomArrayOfNewFragment_2, 
				bondArrayOfNewFragment_2, brokenBondArrayOfNewFragment_2, numberHydrogensOfNewFragment[0]);
		} catch (AtomTypeNotKnownFromInputListException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		firstNewGeneratedFragment.setTreeDepth((byte)(this.getTreeDepth() + 1));
		
		secondNewGeneratedFragment.setTreeDepth((byte)(this.getTreeDepth() + 1));

		TopDownBitArrayFragment[] newFrags = { firstNewGeneratedFragment, secondNewGeneratedFragment };
		
		return newFrags;

	}
	
	/**
	 * traverse the fragment to one direction starting from startAtomIndex to set FastBitArrays of new fragment
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
	protected boolean traverseSingleDirection(IMolecularStructure precursorMolecule, short startAtomIndex, short endAtomIndex, short bondIndexToRemove, 
		de.ipbhalle.metfraglib.FastBitArray atomArrayOfNewFragment, de.ipbhalle.metfraglib.FastBitArray bondArrayOfNewFragment, 
		de.ipbhalle.metfraglib.FastBitArray brokenBondArrayOfNewFragment, int[] numberHydrogensOfNewFragment) 
	{
		de.ipbhalle.metfraglib.FastBitArray bondFastBitArrayOfCurrentFragment = this.getBondsFastBitArray();
		/* when traversing the fragment graph then we want to know if we already
		 * visited a node (atom)
		 * need to be done for checking of ringed structures
		 * if traversed an already visited atom, then no new fragment was
		 * generated
		 */
		de.ipbhalle.metfraglib.FastBitArray visited = new de.ipbhalle.metfraglib.FastBitArray(precursorMolecule.getNonHydrogenAtomCount());
		numberHydrogensOfNewFragment[0] = 0;

		/*
		 *  traverse molecule in the first direction
		 */
		java.util.Stack<short[]> toProcessConnectedAtoms = new java.util.Stack<short[]>();
		java.util.Stack<Short> toProcessAtom = new java.util.Stack<Short>();
		toProcessConnectedAtoms.push(((BitArrayPrecursor)precursorMolecule).getConnectedAtomIndecesOfAtomIndex(startAtomIndex));
		toProcessAtom.push(startAtomIndex);
		visited.set(startAtomIndex);
		boolean stillOneFragment = false;
		/*
		 *  set the first atom of possible new fragment
		 * atom is of the one direction of cutted bond
		 */
		atomArrayOfNewFragment.set(startAtomIndex);
		numberHydrogensOfNewFragment[0] += ((BitArrayPrecursor)precursorMolecule).getNumberHydrogensConnectedToAtomIndex(startAtomIndex);
		while (!toProcessConnectedAtoms.isEmpty()) {
			short[] nextAtoms = toProcessConnectedAtoms.pop();
			short midAtom = toProcessAtom.pop();
			for (int i = 0; i < nextAtoms.length; i++) {
				/*
				 *  did we visit the current atom already?
				 */
				short currentBondNumber = (short)(((BitArrayPrecursor)precursorMolecule).getBondIndexFromAtomAdjacencyList(nextAtoms[i], midAtom) - 1);
				
				if (!bondFastBitArrayOfCurrentFragment.get(currentBondNumber) || currentBondNumber == bondIndexToRemove) {
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
				numberHydrogensOfNewFragment[0] += ((BitArrayPrecursor)precursorMolecule).getNumberHydrogensConnectedToAtomIndex(nextAtoms[i]);
				bondArrayOfNewFragment.set(((BitArrayPrecursor)precursorMolecule).getBondIndexFromAtomAdjacencyList(midAtom, nextAtoms[i]) - 1);
				toProcessConnectedAtoms.push(((BitArrayPrecursor)precursorMolecule).getConnectedAtomIndecesOfAtomIndex(nextAtoms[i]));
				toProcessAtom.push(nextAtoms[i]);
			}

		}

		brokenBondArrayOfNewFragment.set(bondIndexToRemove);
		bondArrayOfNewFragment.set(bondIndexToRemove, false);
		
		return stillOneFragment;
	}
	
	
}
