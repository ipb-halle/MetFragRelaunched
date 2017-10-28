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

	private boolean bondsAsCharacters = false;
	private boolean annotateBonds = true;
	private int atomIndexStart = 0;
	private int bondIndexStart = 0;
	
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
				moleculeToDraw.getAtom(i).setProperty(StandardGenerator.ANNOTATION_LABEL, (i + this.atomIndexStart) + "");
			
			if(this.annotateBonds) {
				boolean useBondCharacters = this.bondsAsCharacters;
				if((moleculeToDraw.getBondCount() + this.bondIndexStart) >= 27) useBondCharacters = false;
				for(int i = 0; i < moleculeToDraw.getBondCount(); i++) 
					if(!useBondCharacters) moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, (i + this.bondIndexStart) + "");
					else moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, ((char)((i + this.bondIndexStart) + 97)) + "");
			}
			
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
				moleculeToDraw.getAtom(i).setProperty(StandardGenerator.ANNOTATION_LABEL, (i + this.atomIndexStart) + "");
			if(this.annotateBonds) {
				boolean useBondCharacters = this.bondsAsCharacters;
				if((moleculeToDraw.getBondCount() + this.bondIndexStart) >= 27) useBondCharacters = false;
				for(int i = 0; i < moleculeToDraw.getBondCount(); i++) 
					if(!useBondCharacters) moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, (i + this.bondIndexStart) + "");
					else moleculeToDraw.getBond(i).setProperty(StandardGenerator.ANNOTATION_LABEL, ((char)((i + this.bondIndexStart) + 97)) + "");
			}
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
	
	public void setAnnotateBonds(boolean annotateBonds) {
		this.annotateBonds = annotateBonds;
	}

	public void setAtomIndexStart(int atomIndexStart) {
		this.atomIndexStart = atomIndexStart;
	}

	public void setBondIndexStart(int bondIndexStart) {
		this.bondIndexStart = bondIndexStart;
	}

	public void setBondAsCharacters(boolean value) {
		this.bondsAsCharacters = value;
	}
	
	public static void main(String[] args) {
	    IAtomContainer m = null;
		
	    try {
			m = MoleculeFunctions.getAtomContainerFromInChI("InChI=1S/C20H22O9/c21-8-16-17(25)18(26)20(29-16)27-11-5-13(23)12-7-14(24)19(28-15(12)6-11)9-1-3-10(22)4-2-9/h1-6,14,16-26H,7-8H2/t14-,16+,17-,18+,19-,20+/m1/s1");
			//m = MoleculeFunctions.getAtomContainerFromSMILES("C1CN(C(=N1)N)CC2=CN=C(C=C2)Cl");
			MoleculeFunctions.prepareAtomContainer(m, true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	    //m = MoleculeFunctions.parseSmiles("O=C1C=CC(=O)C2=C1C=CC=C2");
	    AnnotatedStandardSingleStructureImageGenerator s = new AnnotatedStandardSingleStructureImageGenerator(new Font("Verdana", Font.BOLD, 18));
	    s.setImageHeight(1500);
	    s.setImageWidth(1500);
	    s.setStrokeRation(1.2);
	    s.setBondAsCharacters(false);
	    s.setAtomIndexStart(0);
	    s.setBondIndexStart(0);
	    s.setAnnotateBonds(true);
	    RenderedImage img = s.generateImage(m, "1");
	    try {
			ImageIO.write((RenderedImage) img, "PNG", new java.io.File("/tmp/file.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
