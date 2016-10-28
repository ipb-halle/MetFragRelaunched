package de.ipbhalle.metfraglib.mzml;

import io.github.msdk.datamodel.msspectra.MsSpectrumType;
import io.github.msdk.datamodel.rawdata.IsolationInfo;
import io.github.msdk.datamodel.rawdata.MsScan;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.google.common.collect.Range;

public class SpectrumImage {

	private int imageWidth;
	private int imageHeight;
	
	private	 int maxIntWidth; 
	private	 int maxIntHeight; 
			
	private	 int startIntWidth;
	private int startIntHeight;
	
	private double maxDoubleMz; 
	private double minDoubleMz; 
	private float maxFloatInt; 
	private float minFloatInt; 
	
	public SpectrumImage(int imageWidth, int imageHeight) {
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		this.maxIntWidth = this.imageWidth - 40;
		this.maxIntHeight = this.imageHeight - 40;
		
		this.startIntWidth = 40; 
		this.startIntHeight = 40; 
		
				
	}
	
	public void drawMS2SpectrumImage(MsScan scan, IsolationInfo isoInfo, String filename, boolean debug) {
		
		double[] mzValues = scan.getMzValues(); 
		float[] intensities = scan.getIntensityValues();
		
		if(mzValues == null || mzValues.length == 0) {
			if(debug) System.out.println("no values found"); 
			return;
		}
		if(intensities == null || intensities.length == 0) {
			if(debug) System.out.println("no values found"); 
			return;
		}
		
		if(debug) System.out.println(mzValues.length + " values found");
		
		
		if(mzValues.length != intensities.length) return;
		
		BufferedImage img = new BufferedImage(this.imageWidth, this.imageHeight, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, this.imageWidth, this.imageHeight);
		
		this.maxDoubleMz = this.getMaximumValue(mzValues);
		this.minDoubleMz = this.getMinimumValue(mzValues);
		
		this.maxFloatInt = this.getMaximumValue(intensities);
		this.minFloatInt = 0;
		
		double[] mzRange = this.getMzRange(scan, isoInfo);
		
		this.maxDoubleMz = mzRange[1];
		this.minDoubleMz = mzRange[0];
		
		// printMaxAbundantMz(mzValues, intensities);
		
		double conversionMultiplierMz = (double)(this.maxIntWidth - this.startIntWidth) / (mzRange[1] - mzRange[0]);
		float conversionMultiplierInt = (float)(this.maxIntHeight - this.startIntHeight) / (this.maxFloatInt - this.minFloatInt);

		int[] xPixelValues = new int[mzValues.length];
		int[] yPixelValues = new int[intensities.length];
		
		for(int i = 0; i < xPixelValues.length; i++) {
			int[] pixelValue = calculateIntegerDot(mzValues[i], intensities[i], conversionMultiplierMz, conversionMultiplierInt);
			xPixelValues[i] = pixelValue[0];
			yPixelValues[i] = this.imageHeight - pixelValue[1];
		}
		
		// draw data
		g.setColor(Color.BLUE);
		if(scan.getSpectrumType() == MsSpectrumType.CENTROIDED) {
			for(int i = 0; i < xPixelValues.length; i++) {
				g.drawLine(xPixelValues[i], yPixelValues[i], xPixelValues[i], this.imageHeight - this.startIntHeight);
			}
		}
		else {
			g.drawPolyline(xPixelValues, yPixelValues, yPixelValues.length);
		}
		if(debug) System.out.println("drawing axis");
		// start drawing axes
		g.setColor(Color.BLACK);
		
		int xSource = (int)Math.round(this.startIntWidth / 1.5);
		int ySource = this.imageHeight - (int)Math.round(this.startIntHeight / 1.5);
		// draw x axis
		g.drawLine(xSource, ySource , this.maxIntWidth, ySource);
		int[] labels = this.getLabelsMz();
		for(int i = 0; i < labels.length; i++) {
			int[] pixelValue = calculateIntegerDot(labels[i], 0, conversionMultiplierMz, conversionMultiplierInt);
			g.drawString(String.valueOf(labels[i]), pixelValue[0], ySource + (int)Math.round(this.startIntHeight / 2));
			g.drawLine(pixelValue[0], ySource, pixelValue[0], ySource + (int)Math.round(this.startIntHeight / 10));
		}	
		// draw y axis
		g.drawLine(xSource, ySource , xSource, this.imageHeight - this.maxIntHeight);
		
		// draw spectrum info
		g.drawString(scan.getScanDefinition() + " / TIC: " + scan.getTIC() + " / RT: " + scan.getChromatographyInfo().getRetentionTime(), xSource, 20);
		
		if(debug) System.out.println("writing image");
		java.io.File outputfile = new java.io.File(filename);
		try {
			ImageIO.write(img, "jpg", outputfile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(debug) System.out.println("finished: wrote to " + filename);
	}
	
	protected int[] getLabelsMz() {
		int end = (int)Math.floor(this.maxDoubleMz / (double)Math.floor(Math.log10(this.maxDoubleMz)) * 10);
		double diff = this.maxDoubleMz - this.minDoubleMz;
		double base = Math.log10(diff);
		int power = (int)Math.round(base);
		int base_unit = (int)Math.pow(10.0, (double)power);
		int step = base_unit / 2;
		java.util.Vector<Integer> labels_vec = new java.util.Vector<Integer>();
		for(int i = 0; i <= end; i += step) {
			if(i >= this.minDoubleMz) labels_vec.add(i);
		}
		int[] labels = new int[labels_vec.size()];
		for(int i = 0; i < labels.length; i++)
			labels[i] = labels_vec.get(i);
		System.out.println("number labels " + labels.length);
		return labels;
	}
	
	protected int[] calculateIntegerDot(double x, float y, double conversionMultiplierMz, float conversionMultiplierInt) {
		int xValue = (int)Math.round((x - this.minDoubleMz) * conversionMultiplierMz) + this.startIntWidth;
		int yValue = (int)Math.round((y - this.minFloatInt) * conversionMultiplierInt) + this.startIntHeight;
		return new int[] {xValue, yValue};
	}

	protected double getMinimumValue(double[] values) {
		double value = Integer.MAX_VALUE;
		for(double currentValue : values) {
			if(value > currentValue) value = currentValue;
		}
		return value;
	}

	protected float getMinimumValue(float[] values) {
		float value = Integer.MAX_VALUE;
		for(float currentValue : values) {
			if(value > currentValue) value = currentValue;
		}
		return value;
	}

	protected double getMaximumValue(double[] values) {
		double value = 0.0;
		for(double currentValue : values) {
			if(value < currentValue) value = currentValue;
		}
		return value;
	}

	private float getMaximumValue(float[] values) {
		float value = 0.0f;
		for(float currentValue : values) {
			if(value < currentValue) value = currentValue;
		}
		return value;
	}
	
	protected double[] getMzRange(MsScan scan, IsolationInfo isoInfo) {
		Range<Double> mzRange = scan.getScanningRange();
		if(mzRange != null) return new double[] {mzRange.lowerEndpoint(), mzRange.upperEndpoint()};
		String scanDefinition = scan.getScanDefinition();
		try {	
			String[] tmp = scanDefinition.replaceAll(".*\\[(.*)\\].*", "$1").split("-");
			return new double[] {Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1])};
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		double min = Math.max(0.0, this.minDoubleMz - 100);
		double max = isoInfo.getPrecursorMz() + 10.0;
		if(max < this.maxDoubleMz) max = this.maxDoubleMz + 10.0;
		return new double[] {min, max};
	}
	
	protected void printMaxAbundantMz(double[] mz, float[] intentsity) {
		int indexOfMaxIntensity = 0; 
		float valueOfMaxIntensity = intentsity[0];
		for(int i = 1; i < intentsity.length; i++) {
			if(intentsity[i] > valueOfMaxIntensity) {
				valueOfMaxIntensity = intentsity[i];
				indexOfMaxIntensity = i;
			}
		}
		System.out.println(valueOfMaxIntensity + " @ " + mz[indexOfMaxIntensity]);
	}
}
