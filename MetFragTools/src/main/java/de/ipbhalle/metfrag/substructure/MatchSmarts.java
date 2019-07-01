package de.ipbhalle.metfrag.substructure;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.RenderedImage;
import java.util.List;

import javax.imageio.ImageIO;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.imagegenerator.HighlightSubStructureImageGenerator;

public class MatchSmarts {

	public static void main(String[] args) throws Exception {
		
		String smarts1 = "FC(F)([C,F])[!$(C(F)(F));!$(F)]";
		String smiles = "C(=O)(C(C(C(C(C(C(C(F)(F)F)(C(F)(F)F)F)(F)F)(F)F)(F)F)(F)F)(F)F)O";
		
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer con = sp.parseSmiles(smiles);
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(con);
		MoleculeFunctions.removeHydrogens(con);
		MoleculeFunctions.prepareAtomContainer(con, false);
		SMARTSQueryTool sqt = new SMARTSQueryTool(smarts1, DefaultChemObjectBuilder.getInstance());
		
		System.out.println(smarts1 + " in " + smiles + ": " + sqt.matches(con));
		
		HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
		s.setHighlightColor(new Color(0x6495ED));
		s.setImageHeight(1500);
		s.setImageWidth(1500);
		s.setStrokeRation(1.2);
		List<List<Integer>> matchedAtomIndexes = sqt.getUniqueMatchingAtoms();
		
		for (int i = 0; i < matchedAtomIndexes.size(); i++) {
			FastBitArray bitArrayAtoms = generateAndSetBistString(con.getAtomCount(), 
				matchedAtomIndexes.get(i));
			FastBitArray bitArrayBonds = new FastBitArray(con.getBondCount());
			
			RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, con);
			ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file" + (i+1) + ".png"));
		}
	}
	

	public static FastBitArray generateAndSetBistString(int size, List<Integer> toSet) {
		FastBitArray bitArray = new FastBitArray(size, false);
		for(int i = 0; i < toSet.size(); i++) {
			bitArray.set(toSet.get(i));
		}
		return bitArray;
	}
}
