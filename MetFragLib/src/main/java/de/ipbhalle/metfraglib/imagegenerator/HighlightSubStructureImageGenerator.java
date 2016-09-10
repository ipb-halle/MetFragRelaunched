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
			m = MoleculeFunctions.getAtomContainerFromInChI("InChI=1S/C17H14O5/c1-20-11-5-3-4-10(8-11)17-16(19)15(18)13-7-6-12(21-2)9-14(13)22-17/h3-9,19H,1-2H3");
			MoleculeFunctions.prepareAtomContainer(m, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		HighlightSubStructureImageGenerator s = new HighlightSubStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
		s.setImageHeight(1500);
		s.setImageWidth(1500);
		s.setStrokeRation(1.2);
		
		BitArray bitArrayAtoms = new BitArray(m.getAtomCount());
		BitArray bitArrayBonds = new BitArray(m.getBondCount());
		bitArrayAtoms.set(0, false);
		bitArrayAtoms.set(1, true);
		bitArrayAtoms.set(2, true);
		bitArrayAtoms.set(3, true);
		bitArrayAtoms.set(4, true);
		bitArrayAtoms.set(5, true);
		bitArrayAtoms.set(6, true);
		bitArrayAtoms.set(7, true);
		bitArrayAtoms.set(8, true);
		bitArrayAtoms.set(9, true);
		bitArrayAtoms.set(10, true);
		bitArrayAtoms.set(11, true);
		bitArrayAtoms.set(12, true);
		bitArrayAtoms.set(13, true);
		bitArrayAtoms.set(14, true);
		bitArrayAtoms.set(15, true);
		bitArrayAtoms.set(16, true);
		bitArrayAtoms.set(17, true);
		bitArrayAtoms.set(18, false);
		bitArrayAtoms.set(19, false);
		bitArrayAtoms.set(20, true);
		bitArrayAtoms.set(21, true);
		
		bitArrayBonds.set(0, true);
		bitArrayBonds.set(1, true);
		bitArrayBonds.set(2, true);
		bitArrayBonds.set(3, true);
		bitArrayBonds.set(4, true);
		bitArrayBonds.set(5, true);
		bitArrayBonds.set(6, true);
		bitArrayBonds.set(7, true);
		bitArrayBonds.set(8, true);
		bitArrayBonds.set(9, true);
		bitArrayBonds.set(10, true);
		bitArrayBonds.set(11, true);
		bitArrayBonds.set(12, true);
		bitArrayBonds.set(13, true);
		bitArrayBonds.set(14, true);
		bitArrayBonds.set(15, true);
		bitArrayBonds.set(16, true);
		bitArrayBonds.set(17, false);
		bitArrayBonds.set(18, false);
		bitArrayBonds.set(19, false);
		bitArrayBonds.set(20, true);
		bitArrayBonds.set(21, true);
		bitArrayBonds.set(22, true);
		bitArrayBonds.set(23, true);

		RenderedImage img = s.generateImage(bitArrayAtoms, bitArrayBonds, m);
		ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file2.png"));
	}
}
