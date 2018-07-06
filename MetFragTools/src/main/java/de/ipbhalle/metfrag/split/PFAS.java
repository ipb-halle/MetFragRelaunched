package de.ipbhalle.metfrag.split;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
	
	public int[] findEndChainCarbons() throws Exception {
		String smarts = "FC(F)([C,F])[!$(C(F)(F));!$(F)]";
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		List<List<Integer>> matchAtomIndeces = this.getMatchesBySmarts(smarts, con);
		
		int[] matches = new int[matchAtomIndeces.size()];
		
		for(int i = 0; i < matches.length; i++) 
			matches[i] = this.findEndChainCarbonFromMatch(matchAtomIndeces.get(i), con);
			
		return matches;
	}
	
	public List<Integer> getBondIndexAfterEndChain(int[] endChainCarbonIndexes, String smarts) throws Exception {
		List<Integer> toBreakBondIndexes = new ArrayList<Integer>();
		IAtomContainer con = this.pfasStructure.getImplicitHydrogenAtomContainer();
		for(int endChainCarbonIndex : endChainCarbonIndexes) {	
			if(smarts.equals("")) {
				List<Integer> bondsAfterMatch = findBondsAfterMatch(null, endChainCarbonIndex, con);
				for(Integer bondAfterMatch : bondsAfterMatch)
					toBreakBondIndexes.add(bondAfterMatch);
			} else {
				List<List<Integer>> matchedAtomIndexes = this.getMatchesBySmarts(smarts, con);
				for(List<Integer> matchedAtomIndex : matchedAtomIndexes) {
					List<Integer> bondsAfterMatch = this.findBondsAfterMatch(matchedAtomIndex, endChainCarbonIndex, con);
					for(Integer bondAfterMatch : bondsAfterMatch)
						toBreakBondIndexes.add(bondAfterMatch);
				}
			}
		}
		return toBreakBondIndexes;
	}
	
	
	protected List<Integer> findBondsAfterMatch(List<Integer> matchedAtomIndexes, int endChainCarbonIndex, IAtomContainer con) {
		List<Integer> bondIndexesAfterMatch = new ArrayList<Integer>();
		IAtom endChainCarbon = con.getAtom(endChainCarbonIndex);
		List<IAtom> connectedAtoms = con.getConnectedAtomsList(endChainCarbon);
		if(matchedAtomIndexes == null) {
			for(IAtom connectedAtom : connectedAtoms) { 
				if((connectedAtom.getSymbol().equals("C") && this.countConnectedFluors(connectedAtom, con) >= 1) || connectedAtom.getSymbol().equals("F")) continue;
				bondIndexesAfterMatch.add(con.indexOf(con.getBond(connectedAtom, endChainCarbon)));
			}
		} else {
			if(this.isDisjunct(connectedAtoms, matchedAtomIndexes, con) || matchedAtomIndexes.contains(endChainCarbonIndex)) return bondIndexesAfterMatch;
			for(Integer matchedAtomIndex : matchedAtomIndexes) {
				List<IAtom> atoms = con.getConnectedAtomsList(con.getAtom(matchedAtomIndex));
				for(IAtom atom : atoms) {
					int indexOfAtom = con.indexOf(atom);
					if(!matchedAtomIndexes.contains(indexOfAtom) && indexOfAtom != endChainCarbonIndex 
							&& (!atom.getSymbol().equals("C") || this.countConnectedFluors(atom, con) == 0) && !atom.getSymbol().equals("F")) { 
						bondIndexesAfterMatch.add(con.indexOf(con.getBond(atom, con.getAtom(matchedAtomIndex))));
					}
				}
			}
		}
		return bondIndexesAfterMatch;				
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
			if(con.getAtom(matchIndexes.get(i)).getSymbol().equals("C") && this.isFinalCarbonInChain(matchIndexes.get(i), con)) return matchIndexes.get(i);
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
			if(connectedAtom.getSymbol().equals("C") && this.countConnectedFluors(connectedAtom, con) >= 1) numberOfConnectedCarbonsWithFluor++;
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
	
	public String getSplitResult(List<Integer> bondIndexes, int[] endChainCarbonIndexes) {
		LinkedList<AbstractTopDownBitArrayFragment> abstractTopDownBitArrayFragments = new LinkedList<AbstractTopDownBitArrayFragment>();
		LinkedList<AbstractTopDownBitArrayFragment> newAbstractTopDownBitArrayFragments = null;
		AbstractTopDownBitArrayFragment root = ((AbstractTopDownBitArrayPrecursor)this.pfasStructure.getPrecursorMolecule()).toFragment();
		abstractTopDownBitArrayFragments.add(root);
		for(int bondIndex : bondIndexes) {
			newAbstractTopDownBitArrayFragments = new LinkedList<AbstractTopDownBitArrayFragment>();
			while(!abstractTopDownBitArrayFragments.isEmpty()) {
				AbstractTopDownBitArrayFragment currentFragment = abstractTopDownBitArrayFragments.remove();
				if(currentFragment.getBondsFastBitArray().get(bondIndex)) {
					AbstractTopDownBitArrayFragment[] fragments = this.split((short)bondIndex, currentFragment);
					for(int i = 0; i < fragments.length; i++) newAbstractTopDownBitArrayFragments.add(fragments[i]);
				}
			}
			abstractTopDownBitArrayFragments = newAbstractTopDownBitArrayFragments;
		}

		boolean[] containsEndChainCarbon = new boolean[newAbstractTopDownBitArrayFragments.size()];
		for(int i = 0; i < newAbstractTopDownBitArrayFragments.size(); i++) {
			for(int ii = 0; ii < endChainCarbonIndexes.length; ii++) {
				if(newAbstractTopDownBitArrayFragments.get(i).getAtomsFastBitArray().get(endChainCarbonIndexes[ii])) containsEndChainCarbon[i] = true;
			}
		}
		
		int fragmentsWithEndChainCarbonCount = 0;
		for(boolean fragmentsWithEndChainCarbon : containsEndChainCarbon)
			if(fragmentsWithEndChainCarbon) fragmentsWithEndChainCarbonCount++;
		if(fragmentsWithEndChainCarbonCount == 0) {
			System.err.println("Error: Problem occured. Check input molecule. No PFAS left after split.");
			return "";
		}
		if(fragmentsWithEndChainCarbonCount >= 2) {
			System.err.println("Error: Problem occured. Check input molecule. More than one functional group left after split.");
			return "";
		}
		
		for(int i = 0; i < containsEndChainCarbon.length; i++)
			if(!containsEndChainCarbon[i]) return newAbstractTopDownBitArrayFragments.get(i).getSmiles(this.pfasStructure.getPrecursorMolecule());
	
		return "";
	}
	
	protected AbstractTopDownBitArrayFragment[] split(short bondIndex, AbstractTopDownBitArrayFragment fragment) {
		short[] indecesOfBondConnectedAtoms = ((BitArrayPrecursor)this.pfasStructure.getPrecursorMolecule()).getConnectedAtomIndecesOfBondIndex(bondIndex);
		AbstractTopDownBitArrayFragment[] fragments = fragment.traverseMolecule(this.pfasStructure.getPrecursorMolecule(), bondIndex, indecesOfBondConnectedAtoms);
	
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
}
