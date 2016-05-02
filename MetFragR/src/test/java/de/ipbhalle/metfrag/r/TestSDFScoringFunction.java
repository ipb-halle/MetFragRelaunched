package de.ipbhalle.metfrag.r;

import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;

public class TestSDFScoringFunction {

	String pathToSDF; 
	double[] masses = {119.051, 123.044, 147.044, 153.019, 179.036, 189.058, 273.076, 274.083}; 
	double[] intensities = {467.616,370.662,6078.145,10000.0,141.192,176.358,10000.000,318.003};
	double exactMass = 272.06847; 
	int numberThreads = 1; 
	double mzabs = 0.01; 
	double mzppm = 10.0; 
	double searchppm = 10.0; 
	boolean posCharge = true; 
	int mode = 1; 
	int treeDepth = 2;
	String[] scoreNames = {"FragmenterScore"};
	double[] scoreWeights = {1.0};
	
	@Before
	public void setUp() {
		java.io.File exampleFileSDF = new java.io.File(ClassLoader.getSystemResource("example_sdf.sdf").getFile());
		this.pathToSDF = exampleFileSDF.getAbsolutePath();
	}

	@Test
	public void test() {
		IAtomContainer[] scoredMolecules = MetfRag.scoreMoleculesAgainstSpectrum(pathToSDF, masses, intensities,
				exactMass, numberThreads, mzabs, mzppm, searchppm, posCharge, mode, treeDepth, scoreNames, scoreWeights);
		System.out.println("Processed " + scoredMolecules.length + " molecule successfully...");
		java.util.Iterator<Object> iterator = scoredMolecules[0].getProperties().keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String)iterator.next();
			System.out.println(key + "\n\t" + scoredMolecules[0].getProperty(key));
		}
	
	}

}
