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

import de.ipbhalle.metfraglib.BitArray;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.fragment.TopDownBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;

public class HighlightSubStructureImageGenerator extends StandardSingleStructureImageGenerator {

	public HighlightSubStructureImageGenerator() {
		super();
	}

	public HighlightSubStructureImageGenerator(Font font) {
		super(font);
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

	public RenderedImage generateImage(final IFragment structure) throws Exception {
		if (structure instanceof DefaultBitArrayFragment)
			return generateImage((DefaultBitArrayFragment) structure);
		if (structure instanceof TopDownBitArrayFragment)
			return generateImage((DefaultBitArrayFragment) structure);
		return super.generateImage(structure);
	}

	public RenderedImage generateImage(final DefaultBitArrayFragment structure) {
		Image image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		try {
			IAtomContainer molecule = new AtomContainer(structure.getPrecursor().getStructureAsIAtomContainer());
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

			BitArray atoms = structure.getAtomsBitArray();
			for (int i = 0; i < atoms.getSize(); i++) {
				if(atoms.get(i)) moleculeToDraw.getAtom(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, new Color(0x98F08E));
				else moleculeToDraw.getAtom(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			BitArray bonds = structure.getBondsBitArray();
			for (int i = 0; i < bonds.getSize(); i++) {
				if(bonds.get(i)) moleculeToDraw.getBond(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, new Color(0x98F08E));
				else moleculeToDraw.getBond(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.imageWidth, this.imageHeight);
			
			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
			g2.fillRect(0, 0, this.imageWidth, this.imageHeight);
			this.renderer.paint(moleculeToDraw, new AWTDrawVisitor(g2), bounds, true);
		} catch (Exception e) {
			return (RenderedImage) image;
		}
		return (RenderedImage) image;
	}

	public RenderedImage generateImage(final BitArray toHighlightAtoms, final BitArray toHighlightBonds, final IAtomContainer molecule) {
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
					structure.getAtom(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, new Color(0x98F08E));
				}
				else structure.getAtom(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}
			for (int i = 0; i < toHighlightBonds.getSize(); i++) {
				if (toHighlightBonds.get(i)) {
					structure.getBond(i).setProperty(StandardGenerator.HIGHLIGHT_COLOR, new Color(0x98F08E));
				}
				else structure.getBond(i).removeProperty(StandardGenerator.HIGHLIGHT_COLOR);
			}

			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
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
			m = MoleculeFunctions.getAtomContainerFromInChI("InChI=1/C28H32O16/c1-9-18(32)21(35)23(37)27(41-9)40-8-16-19(33)22(36)24(38)28(43-16)44-26-20(34)17-13(31)6-11(29)7-15(17)42-25(26)10-3-4-12(30)14(5-10)39-2/h3-7,9,16,18-19,21-24,27-33,35-38H,8H2,1-2H3/t9-,16+,18-,19+,21+,22-,23+,24+,27+,28-/m0/s1");
			//MoleculeFunctions.removeHydrogens(m);
			MoleculeFunctions.prepareAtomContainer(m, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
		s.setImageHeight(1500);
		s.setImageWidth(1500);
		s.setStrokeRation(1.2);
		
		//convertBitString("000000000001001101111000110001101111001111001101", "bitArrayBonds");
		
		BitArray bitArrayAtoms = new BitArray(m.getAtomCount());
		BitArray bitArrayBonds = new BitArray(m.getBondCount());
		bitArrayAtoms.set(0, false);
		bitArrayAtoms.set(1, false);
		bitArrayAtoms.set(2, false);
		bitArrayAtoms.set(3, false);
		bitArrayAtoms.set(4, false);
		bitArrayAtoms.set(5, false);
		bitArrayAtoms.set(6, false);
		bitArrayAtoms.set(7, true);
		bitArrayAtoms.set(8, true);
		bitArrayAtoms.set(9, false);
		bitArrayAtoms.set(10, false);
		bitArrayAtoms.set(11, false);
		bitArrayAtoms.set(12, false);
		bitArrayAtoms.set(13, false);
		bitArrayAtoms.set(14, false);
		bitArrayAtoms.set(15, true);
		bitArrayAtoms.set(16, false);
		bitArrayAtoms.set(17, true);
		bitArrayAtoms.set(18, true);
		bitArrayAtoms.set(19, false);
		bitArrayAtoms.set(20, true);
		bitArrayAtoms.set(21, true);
		bitArrayAtoms.set(22, true);
		bitArrayAtoms.set(23, true);
		bitArrayAtoms.set(24, false);
		bitArrayAtoms.set(25, false);
		bitArrayAtoms.set(26, true);
		bitArrayAtoms.set(27, true);
		bitArrayAtoms.set(28, false);
		bitArrayAtoms.set(29, false);
		bitArrayAtoms.set(30, false);
		bitArrayAtoms.set(31, true);
		bitArrayAtoms.set(32, true);
		bitArrayAtoms.set(33, false);
		bitArrayAtoms.set(34, true);
		bitArrayAtoms.set(35, true);
		bitArrayAtoms.set(36, true);
		bitArrayAtoms.set(37, true);
		bitArrayAtoms.set(38, false);
		bitArrayAtoms.set(39, true);
		bitArrayAtoms.set(40, true);
		bitArrayAtoms.set(41, false);
		bitArrayAtoms.set(42, true);
		bitArrayAtoms.set(43, true);
		
		bitArrayBonds.set(0, false);
		bitArrayBonds.set(1, false);
		bitArrayBonds.set(2, false);
		bitArrayBonds.set(3, false);
		bitArrayBonds.set(4, false);
		bitArrayBonds.set(5, false);
		bitArrayBonds.set(6, false);
		bitArrayBonds.set(7, false);
		bitArrayBonds.set(8, false);
		bitArrayBonds.set(9, false);
		bitArrayBonds.set(10, false);
		bitArrayBonds.set(11, true);
		bitArrayBonds.set(12, false);
		bitArrayBonds.set(13, false);
		bitArrayBonds.set(14, true);
		bitArrayBonds.set(15, true);
		bitArrayBonds.set(16, false);
		bitArrayBonds.set(17, true);
		bitArrayBonds.set(18, true);
		bitArrayBonds.set(19, true);
		bitArrayBonds.set(20, true);
		bitArrayBonds.set(21, false);
		bitArrayBonds.set(22, false);
		bitArrayBonds.set(23, false);
		bitArrayBonds.set(24, true);
		bitArrayBonds.set(25, true);
		bitArrayBonds.set(26, false);
		bitArrayBonds.set(27, false);
		bitArrayBonds.set(28, false);
		bitArrayBonds.set(29, true);
		bitArrayBonds.set(30, true);
		bitArrayBonds.set(31, false);
		bitArrayBonds.set(32, true);
		bitArrayBonds.set(33, true);
		bitArrayBonds.set(34, true);
		bitArrayBonds.set(35, true);
		bitArrayBonds.set(36, false);
		bitArrayBonds.set(37, false);
		bitArrayBonds.set(38, true);
		bitArrayBonds.set(39, true);
		bitArrayBonds.set(40, true);
		bitArrayBonds.set(41, true);
		bitArrayBonds.set(42, false);
		bitArrayBonds.set(43, false);
		bitArrayBonds.set(44, true);
		bitArrayBonds.set(45, true);
		bitArrayBonds.set(46, false);
		bitArrayBonds.set(47, true);

		RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, m);
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file2.png"));
	}
	
	public static void convertBitString(String bitString, String type) {
		for(int i = 0; i < bitString.length(); i++) {
			char pos = bitString.charAt(i);
			if(pos == '1') System.out.println(type + ".set(" + i + ", true);");
			else System.out.println(type + ".set(" + i + ", false);");
		}
	}
}
