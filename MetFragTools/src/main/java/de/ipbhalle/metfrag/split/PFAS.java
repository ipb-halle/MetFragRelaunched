package de.ipbhalle.metfrag.split;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;
import de.ipbhalle.metfraglib.imagegenerator.AnnotatedStandardSingleStructureImageGenerator;
import de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.precursor.AbstractTopDownBitArrayPrecursor;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;

public class PFAS {

	protected TopDownPrecursorCandidate pfasStructure;
	
	public PFAS(String smiles) throws AtomTypeNotKnownFromInputListException, Exception {
		this.pfasStructure = new TopDownPrecursorCandidate("", "1", smiles);
		System.out.println("Reading " + smiles);
		this.pfasStructure.setUseSmiles(true);
		this.pfasStructure.initialisePrecursorCandidate();
	}
	
	/**
	 * 
	 * @param endChainCarbonSm
	 * @param debugFolder
	 * @return
	 * @throws Exception
	 */
	public int[] findEndChainCarbons(String endChainCarbonSm, String debugFolder) throws Exception {
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		List<List<Integer>> matchAtomIndeces = this.getMatchesBySmarts(endChainCarbonSm, con);
		// generate images with highlighted matches
		if(debugFolder != null) {
			System.out.println("Found " + matchAtomIndeces.size() + " match(es) using end chain SMARTS (pacs)");
			HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
			for (int i = 0; i < matchAtomIndeces.size(); i++) {
				FastBitArray bitArrayAtoms = this.generateAndSetBitString(con.getAtomCount(), 
						matchAtomIndeces.get(i));
				FastBitArray bitArrayBonds = new FastBitArray(con.getBondCount());
				s.setHighlightColor(new Color(0x6495ED));
				s.setImageHeight(1500);
				s.setImageWidth(1500);
				s.setStrokeRation(1.2);
				RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, con);
				ImageIO.write((RenderedImage) img, "PNG", 
					new java.io.File(debugFolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "01a-pacs-match-" + (i+1) + ".png"));
			}
		}
		
		int[] matches = new int[matchAtomIndeces.size()];
		
		for(int i = 0; i < matches.length; i++) 
			matches[i] = this.findEndChainCarbonFromMatch(matchAtomIndeces.get(i), con);
		
		// count matches after filtering
		if(debugFolder != null) {
			int validMatches = 0;
			for(int i = 0; i < matches.length; i++)
				if(matches[i] != -1) validMatches++;
			System.out.println(validMatches + " match(es) remain after filtering");
		}	
		
		// generate images with highlighted matches after filtering
		if(debugFolder != null) {
			HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
			for(int i = 0; i < matches.length; i++) {
				if(matches[i] == -1) continue;
				// store end chain carbon image
				FastBitArray bitArrayAtoms = 
						this.generateAndSetBitString(con.getAtomCount(), new int[] {matches[i]});
				FastBitArray bitArrayBonds = new FastBitArray(con.getBondCount());
				s.setHighlightColor(new Color(0x6495ED));
				s.setImageHeight(1500);
				s.setImageWidth(1500);
				s.setStrokeRation(1.2);
				RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, con);
				ImageIO.write((RenderedImage) img, "PNG", 
					new java.io.File(debugFolder + Constants.OS_SPECIFIC_FILE_SEPARATOR 
						+ "01b-pfas-alpha-carbon-" + (i+1) + ".png"));
				
				
			}
		}
		
		return matches;
	}
	
	public List<List<List<Integer>>> getBondIndexAfterEndChain(int[] endChainCarbonIndexes, String smarts, 
			Integer smartsIndex, 
			String debugFolder) throws Exception {
		List<List<Integer>> toBreakBondIndexes = new LinkedList<List<Integer>>();
		List<List<Integer>> toBreakBondIndexesXR = new LinkedList<List<Integer>>();
		
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();

		List<List<Integer>>	matchedAtomIndexes = this.getMatchesBySmarts(smarts, con);
		if(debugFolder != null) System.out.println("Found " + matchedAtomIndexes.size() + " match(es) with " + smarts);
		
		for(int endChainCarbonIndex : endChainCarbonIndexes) {	
			if(endChainCarbonIndex == -1) continue;
				// generate structure image with matched atoms by given smarts
			if(debugFolder != null) {
				HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
				FastBitArray bitArrayAtoms = this.generateAndSetBitStringMultiple(con.getAtomCount(), matchedAtomIndexes);
				FastBitArray bitArrayBonds = new FastBitArray(con.getBondCount());
				s.setHighlightColor(new Color(0x6495ED));
				s.setImageHeight(1500);
				s.setImageWidth(1500);
				s.setStrokeRation(1.2);
				RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, con);
				ImageIO.write((RenderedImage) img, "PNG", 
					new java.io.File(debugFolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + "02a-atoms-matched-by-smarts-" + (smartsIndex+1) + ".png"));
			}
			
			for(List<Integer> matchedAtomIndex : matchedAtomIndexes) {
				List<Integer> bondsAfterMatch = this.findBondsAfterMatch(matchedAtomIndex, endChainCarbonIndex, con);
				List<Integer> bondsNextMatch = this.findBondsNextToMatch(matchedAtomIndex, endChainCarbonIndex, con);
				toBreakBondIndexes.add(bondsAfterMatch);
				toBreakBondIndexesXR.add(bondsNextMatch);
			}
		}
		
		List<List<List<Integer>>> toReturn = new LinkedList<List<List<Integer>>>();
		toReturn.add(toBreakBondIndexes);
		toReturn.add(toBreakBondIndexesXR);

		return toReturn;
	}
	
	protected List<Integer> findBondsAfterMatch(List<Integer> matchedAtomIndexes, int endChainCarbonIndex, 
			IAtomContainer con) throws Exception {
		List<Integer> bondIndexesAfterMatch = new ArrayList<Integer>();
		IAtom endChainCarbon = con.getAtom(endChainCarbonIndex);
		List<IAtom> connectedAtoms = con.getConnectedAtomsList(endChainCarbon);
		if(matchedAtomIndexes == null) {
			for(IAtom connectedAtom : connectedAtoms) { 
				if((connectedAtom.getSymbol().equals("C") && this.countConnectedFluors(connectedAtom, con) >= 1) || connectedAtom.getSymbol().equals("F")) continue;
				bondIndexesAfterMatch.add(new Integer(con.indexOf(con.getBond(connectedAtom, endChainCarbon))));
			}
		} else {
			// check whether end chain carbon is connected to the current Step-02 match 
			// or whether the end chain carbon is part of that match 
			if(this.isDisjunct(connectedAtoms, matchedAtomIndexes, con) || matchedAtomIndexes.contains(endChainCarbonIndex)) 
				return bondIndexesAfterMatch;
			// for each atom of the current Step-02 match
			for(Integer matchedAtomIndex : matchedAtomIndexes) {
				// get all connected atoms
				List<IAtom> atoms = con.getConnectedAtomsList(con.getAtom(matchedAtomIndex));
				for(IAtom atom : atoms) {
					int indexOfAtom = con.indexOf(atom);
					if(!matchedAtomIndexes.contains(indexOfAtom) && indexOfAtom != endChainCarbonIndex 
							&& (!atom.getSymbol().equals("C") || this.countConnectedFluors(atom, con) == 0) 
							&& !atom.getSymbol().equals("F")) { 
						bondIndexesAfterMatch.add(
							con.indexOf(con.getBond(atom, con.getAtom(matchedAtomIndex)))
						);
					}
				}
			}
		}
		return bondIndexesAfterMatch;				
	}
	
	/**
	 * find bond that connects SMARTS match with PFAS alpha carbon
	 * 
	 * @param matchedAtomIndexes
	 * @param endChainCarbonIndex
	 * @param con
	 * @return
	 * @throws Exception
	 */
	protected List<Integer> findBondsNextToMatch(List<Integer> matchedAtomIndexes, int endChainCarbonIndex, 
			IAtomContainer con) throws Exception {
		IAtom endChainCarbon = con.getAtom(endChainCarbonIndex);
		List<IAtom> connectedAtoms = con.getConnectedAtomsList(endChainCarbon);
		List<Integer> bondsNextToAlphaCarbon = null;
		try {
			bondsNextToAlphaCarbon = connectedAtoms.stream().filter(connectedAtom -> 
				matchedAtomIndexes.contains(con.indexOf(connectedAtom))
			).map(atom -> con.indexOf(con.getBond(endChainCarbon, atom))).collect(Collectors.toList());
		} catch(Exception e) {
			return new LinkedList<Integer>();
		}
		
		return bondsNextToAlphaCarbon;
	}
	
	protected boolean isDisjunct(List<IAtom> connectedAtoms, List<Integer> matchedAtomIndexes, IAtomContainer con) {
		for(IAtom atom : connectedAtoms) {
			if(matchedAtomIndexes.contains(con.indexOf(atom))) return false;
		}
		return true;
	}
	
	protected List<List<Integer>> getMatchesBySmarts(String smarts, IAtomContainer con) throws Exception {
		SMARTSQueryTool smartsQuerytools = new SMARTSQueryTool(smarts, DefaultChemObjectBuilder.getInstance());
		smartsQuerytools.matches(con);
		return smartsQuerytools.getUniqueMatchingAtoms();
	}
	
	protected int findEndChainCarbonFromMatch(List<Integer> matchIndexes, IAtomContainer con) {
		// first find the carbon
		for(int i = 0; i < matchIndexes.size(); i++) 
			if(con.getAtom(matchIndexes.get(i)).getSymbol().equals("C") && this.isFinalCarbonInChain(matchIndexes.get(i), con)) 
				return matchIndexes.get(i);
		return -1;
	}
	
	/**
	 * check if current carbon is tconnectedFluorsCounthe final carbon in the PFAS chain
	 * 
	 * @param index
	 * @param con
	 * @return
	 */
	protected boolean isFinalCarbonInChain(int index, IAtomContainer con) {
		int connectedFluorsCount = this.countConnectedFluors(con.getAtom(index), con);
		if(connectedFluorsCount < 2) return false;
		int numberOfConnectedCarbonsWithFluor = 0;
		List<IAtom> atoms = con.getConnectedAtomsList(con.getAtom(index));
		for(IAtom connectedAtom : atoms) {
			int connectedFluorineAtoms = this.countConnectedFluors(connectedAtom, con);
			if(connectedAtom.getSymbol().equals("C") && connectedFluorineAtoms >= 1) 
				numberOfConnectedCarbonsWithFluor++;
		}
		if(numberOfConnectedCarbonsWithFluor > 1) return false;
		return true;
	}
	
	protected int countConnectedFluors(IAtom carbonAtom, IAtomContainer con) {
		List<IAtom> atoms = con.getConnectedAtomsList(carbonAtom);
		int connectedFluorsCount = 0;
		for(IAtom atom : atoms) {
			if(atom.getSymbol().equals("F")) connectedFluorsCount++;
		}
		return connectedFluorsCount;
	}
	
	public FragmentPFAS getSplitResult(List<Integer> bondIndexes, int[] endChainCarbonIndexes) {
		LinkedList<AbstractTopDownBitArrayFragment> abstractTopDownBitArrayFragments = new LinkedList<AbstractTopDownBitArrayFragment>();
		LinkedList<AbstractTopDownBitArrayFragment> newAbstractTopDownBitArrayFragments = null;
		AbstractTopDownBitArrayFragment root = ((AbstractTopDownBitArrayPrecursor)this.pfasStructure.getPrecursorMolecule()).toFragment();
		abstractTopDownBitArrayFragments.add(root);
		int bondsBroken = 0;
		List<AbstractTopDownBitArrayFragment> createdFragments = new ArrayList<AbstractTopDownBitArrayFragment>();
		// store atoms 
		List<short[]> indecesOfBondConnectedAtomsList = new ArrayList<short[]>();
		for(int i = 0; i < bondIndexes.size(); i++) {
			int bondIndex = bondIndexes.get(i);
			newAbstractTopDownBitArrayFragments = new LinkedList<AbstractTopDownBitArrayFragment>();
			while(!abstractTopDownBitArrayFragments.isEmpty()) {
				AbstractTopDownBitArrayFragment currentFragment = abstractTopDownBitArrayFragments.remove();
				if(currentFragment.getBondsFastBitArray().get(bondIndex)) {
					short[] indecesOfBondConnectedAtoms = new short[2];
					AbstractTopDownBitArrayFragment[] fragments = this.split((short)bondIndex, currentFragment, indecesOfBondConnectedAtoms);
					if(fragments.length != 1) bondsBroken++;
					for(int ii = 0; ii < fragments.length; ii++) {
						newAbstractTopDownBitArrayFragments.add(fragments[ii]);
						createdFragments.add(fragments[ii]);
						indecesOfBondConnectedAtomsList.add(indecesOfBondConnectedAtoms);
					}
				}
			}
			abstractTopDownBitArrayFragments = newAbstractTopDownBitArrayFragments;
		}
		
		List<short[]> cleanedIndecesOfBondConnectedAtomsList = new ArrayList<short[]>();
		List<AbstractTopDownBitArrayFragment> cleanedCreatedFragments = new ArrayList<AbstractTopDownBitArrayFragment>();
		for(int i = 0; i < createdFragments.size(); i++) {	
			boolean bondFound = false;
			for(int j = 0; j < bondIndexes.size(); j++) {
				if(createdFragments.get(i).getBondsFastBitArray().get(bondIndexes.get(j))) {
					bondFound = true;
				}
			}
			if(!bondFound) {
				cleanedCreatedFragments.add(createdFragments.get(i));
				cleanedIndecesOfBondConnectedAtomsList.add(indecesOfBondConnectedAtomsList.get(i));
			}
		}

		boolean[] containsEndChainCarbon = new boolean[cleanedCreatedFragments.size()];
		for(int i = 0; i < cleanedCreatedFragments.size(); i++) {
			for(int ii = 0; ii < endChainCarbonIndexes.length; ii++) {
				if(cleanedCreatedFragments.get(i).getAtomsFastBitArray().get(endChainCarbonIndexes[ii])) 
					containsEndChainCarbon[i] = true;
			}
		}
		
		int fragmentsWithEndChainCarbonCount = 0;
		for(boolean fragmentsWithEndChainCarbon : containsEndChainCarbon)
			if(fragmentsWithEndChainCarbon) fragmentsWithEndChainCarbonCount++;
		if(fragmentsWithEndChainCarbonCount == 0) {
			System.err.println("Error: Problem occured. Check input molecule. No PFAS left after split.");
			return null;
		}
		
		for(int i = 0; i < containsEndChainCarbon.length; i++)
			if(!containsEndChainCarbon[i]) 
				return new FragmentPFAS(cleanedCreatedFragments, 
					cleanedIndecesOfBondConnectedAtomsList, 
					containsEndChainCarbon, bondsBroken, this.pfasStructure);
	
		return null;
	}
	
	protected AbstractTopDownBitArrayFragment[] split(short bondIndex, AbstractTopDownBitArrayFragment fragment, short[] indecesOfBondConnectedAtoms) {
		short[] curIndecesOfBondConnectedAtoms = ((BitArrayPrecursor)this.pfasStructure.getPrecursorMolecule())
			.getConnectedAtomIndecesOfBondIndex(bondIndex);
		indecesOfBondConnectedAtoms[0] = curIndecesOfBondConnectedAtoms[0];
		indecesOfBondConnectedAtoms[1] = curIndecesOfBondConnectedAtoms[1];
		AbstractTopDownBitArrayFragment[] fragments = 
			fragment.traverseMolecule(this.pfasStructure.getPrecursorMolecule(), bondIndex, indecesOfBondConnectedAtoms);
		
		return fragments;
	}

	public void saveHighlightedAtomsImage(List<Integer> toHighlightAtoms, String filename) throws Exception {
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		HighlightSubStructureImageGenerator imageGen = new HighlightSubStructureImageGenerator();
		imageGen.setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		
		FastBitArray atoms = new FastBitArray(con.getAtomCount());
		FastBitArray bonds = new FastBitArray(con.getBondCount());

		for(int i = 0; i < toHighlightAtoms.size(); i++) atoms.set(toHighlightAtoms.get(i));
		
		imageGen.setImageHeight(1500);
		imageGen.setImageWidth(1500);
		imageGen.setStrokeRation(1.2);
		
		RenderedImage img = imageGen.generateImage(atoms, bonds, con);
		
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File(filename));
	}
	
	public void saveHighlightedBondsImage(int[] bondIndexes, String filename) throws Exception {
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		HighlightSubStructureImageGenerator imageGen = new HighlightSubStructureImageGenerator();
		imageGen.setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		
		FastBitArray atoms = new FastBitArray(con.getAtomCount());
		FastBitArray bonds = new FastBitArray(con.getBondCount());
		
		for(int i = 0; i < bondIndexes.length; i++) atoms.set(bondIndexes[i]);
		
		imageGen.setImageHeight(1500);
		imageGen.setImageWidth(1500);
		imageGen.setStrokeRation(1.2);
		
		RenderedImage img = imageGen.generateImage(atoms, bonds, con);
		
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File(filename));
	}

	public void saveHighlightedBondsImage(List<Integer> bondIndexes, String filename) throws Exception {
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		HighlightSubStructureImageGenerator imageGen = new HighlightSubStructureImageGenerator();
		imageGen.setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		
		FastBitArray atoms = new FastBitArray(con.getAtomCount());
		FastBitArray bonds = new FastBitArray(con.getBondCount());
		
		for(int i = 0; i < bondIndexes.size(); i++) bonds.set(bondIndexes.get(i));
		
		imageGen.setImageHeight(1500);
		imageGen.setImageWidth(1500);
		imageGen.setStrokeRation(1.2);
		
		RenderedImage img = imageGen.generateImage(atoms, bonds, con);
		
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File(filename));
	}
	
	public void saveAnnotatedImage(String filename) throws Exception {
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		
		AnnotatedStandardSingleStructureImageGenerator imageGen = new AnnotatedStandardSingleStructureImageGenerator();
		imageGen.setBackgroundColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));

		imageGen.setImageHeight(1500);
		imageGen.setImageWidth(1500);
		imageGen.setStrokeRation(1.2);
		
		RenderedImage img = imageGen.generateImage(con);
		
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File(filename));
	}

	protected FastBitArray generateAndSetBitString(int size, int[] toSet) {
		FastBitArray bitArray = new FastBitArray(size, false);
		for(int i = 0; i < toSet.length; i++) {
			bitArray.set(toSet[i]);
		}
		return bitArray;
	}

	protected FastBitArray generateAndSetBitString(int size, List<Integer> toSet) {
		FastBitArray bitArray = new FastBitArray(size, false);
		for(int i = 0; i < toSet.size(); i++) {
			bitArray.set(toSet.get(i));
		}
		return bitArray;
	}

	protected FastBitArray generateAndSetBitStringMultiple(int size, List<List<Integer>> toSet) {
		FastBitArray bitArray = new FastBitArray(size, false);
		for(int i = 0; i < toSet.size(); i++) {
			for(int ii = 0; ii < toSet.size(); ii++)
				bitArray.set(toSet.get(i).get(ii));
		}
		return bitArray;
	}
}
