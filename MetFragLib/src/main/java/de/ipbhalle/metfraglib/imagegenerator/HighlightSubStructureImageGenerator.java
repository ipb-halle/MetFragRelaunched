package de.ipbhalle.metfraglib.imagegenerator;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.RenderedImage;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.generators.standard.SelectionVisibility;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.layout.StructureDiagramGenerator;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.TopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;

public class HighlightSubStructureImageGenerator extends StandardSingleStructureImageGenerator {

	protected Color highlightColor = new Color(0x98F08E);
	
	public HighlightSubStructureImageGenerator() {
		super();
	}

	public HighlightSubStructureImageGenerator(Font font) {
		super(font);
	}

	public Color getHighlightColor() {
		return highlightColor;
	}

	public void setHighlightColor(Color highlightColor) {
		this.highlightColor = highlightColor;
	}

	public RenderedImage generateImage(final ICandidate candidate) {
		RenderedImage image = null;
		try {
			image = this.generateImage(candidate.getAtomContainer());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return image;
	}

	public RenderedImage generateImage(IMolecularStructure precursorMolecule, final IFragment structure) throws Exception {
		if (structure instanceof DefaultBitArrayFragment || structure instanceof TopDownBitArrayFragment)
			return generateImage(precursorMolecule, (DefaultBitArrayFragment) structure);
		return super.generateImage(precursorMolecule, structure);
	}

	public RenderedImage generateImage(IMolecularStructure precursorMolecule, final DefaultBitArrayFragment structure) {
		Image image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		try {
			IAtomContainer molecule = new AtomContainer(precursorMolecule.getStructureAsIAtomContainer());
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.setMolecule(molecule);
			sdg.generateCoordinates();

			Rectangle drawArea = new Rectangle(this.imageWidth, this.imageHeight);

			IAtomContainer moleculeToDraw = sdg.getMolecule();
			this.renderer.setup(sdg.getMolecule(), drawArea);

			RendererModel rendererModel = this.renderer.getRenderer2DModel();
			//rendererModel.set(StandardGenerator.Visibility.class, SelectionVisibility.iupacRecommendations());
			//rendererModel.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors());
			rendererModel.set(StandardGenerator.Highlighting.class, StandardGenerator.HighlightStyle.OuterGlow);
    		rendererModel.set(StandardGenerator.StrokeRatio.class, this.strokeRatio);

			FastBitArray atoms = structure.getAtomsFastBitArray();
			for (int i = 0; i < atoms.getSize(); i++) {
				if(atoms.get(i)) moleculeToDraw.getAtom(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, this.highlightColor);
				else moleculeToDraw.getAtom(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			FastBitArray bonds = structure.getBondsFastBitArray();
			for (int i = 0; i < bonds.getSize(); i++) {
				if(bonds.get(i)) moleculeToDraw.getBond(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, this.highlightColor);
				else moleculeToDraw.getBond(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.imageWidth, this.imageHeight);
			
			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(this.backgroundColor);
			g2.fillRect(0, 0, this.imageWidth, this.imageHeight);
			this.renderer.paint(moleculeToDraw, new AWTDrawVisitor(g2), bounds, true);
		} catch (Exception e) {
			return (RenderedImage) image;
		}
		return (RenderedImage) image;
	}

	public RenderedImage generateImage(final FastBitArray toHighlightAtoms, final FastBitArray toHighlightBonds, final IAtomContainer molecule) {
		Image image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		try {
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			sdg.setMolecule(molecule);
			sdg.generateCoordinates();

			Rectangle drawArea = new Rectangle(this.imageWidth, this.imageHeight);

			IAtomContainer structure = sdg.getMolecule();
			this.renderer.setup(structure, drawArea);

			RendererModel rendererModel = this.renderer.getRenderer2DModel();
			rendererModel.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors());
    		rendererModel.set(StandardGenerator.StrokeRatio.class, this.strokeRatio);
			rendererModel.set(StandardGenerator.Highlighting.class, StandardGenerator.HighlightStyle.OuterGlow);
			rendererModel.set(StandardGenerator.Visibility.class, SelectionVisibility.iupacRecommendations());
			Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.imageWidth, this.imageHeight);

			for (int i = 0; i < toHighlightAtoms.getSize(); i++) {
				if (toHighlightAtoms.get(i)) {
					structure.getAtom(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, this.highlightColor);
				}
				else structure.getAtom(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}
			for (int i = 0; i < toHighlightBonds.getSize(); i++) {
				if (toHighlightBonds.get(i)) {
					structure.getBond(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, this.highlightColor);
				}
				else structure.getBond(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(this.backgroundColor);
			g2.fillRect(0, 0, this.imageWidth, this.imageHeight);
			this.renderer.paint(structure, new AWTDrawVisitor(g2), bounds, true);
		} catch (Exception e) {
			return (RenderedImage) image;
		}
		return (RenderedImage) image;
	}

	public static void main(String[] args) throws InvalidSmilesException, IOException {
		//SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		//IAtomContainer m = sp.parseSmiles("CC(C)c1ccc(cc1)S(=O)(=O)O");
		IAtomContainer m = null;
		try {
			m = MoleculeFunctions.getAtomContainerFromInChI("InChI=1S/C15H14O6/c16-8-4-11(18)9-6-13(20)15(21-14(9)5-8)7-1-2-10(17)12(19)3-7/h1-5,13,15-20H,6H2/t13-,15-/m1/s1");
			//m = MoleculeFunctions.getAtomContainerFromSMILES("C1CN(C(=N1)N)CC2=CN=C(C=C2)Cl");
			MoleculeFunctions.removeHydrogens(m);
			MoleculeFunctions.prepareAtomContainer(m, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
		s.setHighlightColor(new Color(0x6495ED));
		s.setImageHeight(1500);
		s.setImageWidth(1500);
		s.setStrokeRation(1.2);
		
		//1111101110001100001
		FastBitArray bitArrayAtoms = generateAndSetBistString(21, new int[] {19,12,5});
		FastBitArray bitArrayBonds = generateAndSetBistString(23, new int[] {11,20});
		
		
		RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, m);
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file2.png"));
	}
	
	public static FastBitArray convertBitString(String bitString) {
		FastBitArray bitArray = new FastBitArray(bitString.length());
		for(int i = 0; i < bitString.length(); i++) {
			char pos = bitString.charAt(i);
			if(pos == '1') bitArray.set(i, true);
			else bitArray.set(i, false);
		}
		return bitArray;
	}
	
	public static void convertBitString(String bitString, String type) {
		for(int i = 0; i < bitString.length(); i++) {
			char pos = bitString.charAt(i);
			if(pos == '1') System.out.println(type + ".set(" + i + ", true);");
			else System.out.println(type + ".set(" + i + ", false);");
		}
	}
	
	public static FastBitArray generateAndSetBistString(int size, int[] toSet) {
		FastBitArray bitArray = new FastBitArray(size, false);
		for(int i = 0; i < toSet.length; i++) {
			bitArray.set(toSet[i]);
		}
		return bitArray;
	}
	
	public static FastBitArray generateAndUnSetBistString(int size, int[] toUnSet) {
		FastBitArray bitArray = new FastBitArray(size, true);
		for(int i = 0; i < toUnSet.length; i++) {
			bitArray.set(toUnSet[i], false);
		}
		return bitArray;
	}
}
