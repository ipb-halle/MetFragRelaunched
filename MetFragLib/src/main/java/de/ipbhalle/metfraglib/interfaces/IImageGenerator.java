package de.ipbhalle.metfraglib.interfaces;

import java.awt.Color;
import java.awt.image.RenderedImage;

import org.openscience.cdk.interfaces.IAtomContainer;

public interface IImageGenerator {

	public RenderedImage generateImage(IMolecularStructure precursorMolecule, IFragment structure) throws Exception;
	
	public RenderedImage generateImage(IMolecularStructure structure) throws Exception;
	
	public RenderedImage generateImage(IAtomContainer structure) throws Exception;
	
	public RenderedImage generateImage(ICandidate candidate) throws Exception;
	
	public void setImageWidth(int imageWidth);
	
	public void setImageHeight(int imageHeight);
	
	public int getImageWidth();
	
	public int getImageHeight();
	
	public void setBackgroundColor(Color color);

	/**
	 * delete all objects
	 */
	public void nullify();
}
