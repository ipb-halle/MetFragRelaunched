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

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.renderer.RendererModel;
import org.openscience.cdk.renderer.SymbolVisibility;
import org.openscience.cdk.renderer.color.CDK2DAtomColors;
import org.openscience.cdk.renderer.generators.standard.StandardGenerator;
import org.openscience.cdk.renderer.visitor.AWTDrawVisitor;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.layout.StructureDiagramGenerator;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;

public class AnnotatedStandardSingleStructureImageGenerator extends StandardSingleStructureImageGenerator {

	public AnnotatedStandardSingleStructureImageGenerator(Font font) {
		super(font);
	}
	
	/**
	 * draw image and return it as RenderedImage
	 * 
	 * @param structure
	 * @return
	 * @throws CDKException
	 */
	public RenderedImage generateImage(final IAtomContainer structure) {
		Image image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D) image.getGraphics();
		g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		g2.fillRect(0, 0, this.imageWidth, this.imageHeight);
		try {
			IAtomContainer moleculeToDraw = AtomContainerManipulator.removeHydrogens(structure);

			Rectangle drawArea = new Rectangle(this.imageWidth, this.imageHeight);

			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(moleculeToDraw);
			for(int i = 0; i < moleculeToDraw.getAtomCount(); i++) 
				moleculeToDraw.getAtom(i).setProperty(StandardGenerator.ANNOTATION_LABEL, i + "");
			for(int i = 0; i < moleculeToDraw.getBondCount(); i++) 
				moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, i + "");
			
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule(moleculeToDraw);
            sdg.generateCoordinates();
			this.renderer.setup(sdg.getMolecule(), drawArea);
			RendererModel rendererModel = this.renderer.getRenderer2DModel();
			rendererModel.set(StandardGenerator.Visibility.class, SymbolVisibility.iupacRecommendations());
    		rendererModel.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors()); 
    		rendererModel.set(StandardGenerator.AnnotationColor.class, new Color(0x455FFF));
    		rendererModel.set(StandardGenerator.StrokeRatio.class, this.strokeRatio);
			Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.imageWidth, this.imageHeight);
			
			this.renderer.paint(sdg.getMolecule(), new AWTDrawVisitor(g2), bounds, true);

		} catch (Exception e) {
			return (RenderedImage) image;
		}
		return (RenderedImage) image;
	}

	public RenderedImage generateImage(final IAtomContainer structure, String id) {
		Rectangle drawArea = new Rectangle(this.imageWidth, this.imageHeight);
		Image image = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_ARGB);
		try {
			IAtomContainer moleculeToDraw = AtomContainerManipulator.removeHydrogens(structure);
			AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(moleculeToDraw);
			StructureDiagramGenerator sdg = new StructureDiagramGenerator();
			for(int i = 0; i < moleculeToDraw.getAtomCount(); i++) 
				moleculeToDraw.getAtom(i).setProperty(StandardGenerator.ANNOTATION_LABEL, i + "");
			for(int i = 0; i < moleculeToDraw.getBondCount(); i++) 
				moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, i + "");
            sdg.setMolecule(moleculeToDraw);
            sdg.generateCoordinates();
            this.renderer.setup(sdg.getMolecule(), drawArea);
			
			RendererModel rendererModel = this.renderer.getRenderer2DModel();
			rendererModel.set(StandardGenerator.Visibility.class, SymbolVisibility.iupacRecommendations());
    		rendererModel.set(StandardGenerator.AtomColor.class, new CDK2DAtomColors());
    		rendererModel.set(StandardGenerator.AnnotationColor.class, new Color(0x455FFF));
    		rendererModel.set(StandardGenerator.StrokeRatio.class, this.strokeRatio);
			Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.imageWidth, this.imageHeight);
			
			Graphics2D g2 = (Graphics2D) image.getGraphics();
			g2.setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
			g2.fillRect(0, 0, this.imageWidth, this.imageHeight);
			this.renderer.paint(sdg.getMolecule(), new AWTDrawVisitor(g2), bounds, true);
		} catch (Exception e) {
			System.err.println("could not draw structure " + id);
			return (RenderedImage) image;
		}
		return (RenderedImage) image;
	}
	
	public static void main(String[] args) {
	    IAtomContainer m = null;
		
	    try {
			m = MoleculeFunctions.getAtomContainerFromInChI("InChI=1S/C11H15N5O4/c1-19-8-7(18)5(2-17)20-11(8)16-4-15-6-9(12)13-3-14-10(6)16/h3-5,7-8,11,17-18H,2H2,1H3,(H2,12,13,14)/t5-,7-,8-,11?/m1/s1");
			MoleculeFunctions.prepareAtomContainer(m, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    //m = MoleculeFunctions.parseSmiles("O=C1C=CC(=O)C2=C1C=CC=C2");
	    AnnotatedStandardSingleStructureImageGenerator s = new AnnotatedStandardSingleStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
	    s.setImageHeight(500);
	    s.setImageWidth(500);
	    s.setStrokeRation(1.2);
	    RenderedImage img = s.generateImage(m, "1");
	    try {
			ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
