package de.ipbhalle.metfrag.r;

import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;

public class TestMatchingFragmentGeneration {

	double[] masses = {119.051, 123.044, 147.044, 153.019, 179.036, 189.058, 273.076, 274.083}; 
	double exactMass; 
	double mzabs; 
	double mzppm ; 
	boolean posCharge; 
	int mode; 
	byte treeDepth;
	
	@Before
	public void setUp() {
		this.exactMass = 272.06847;
		this.mzabs = 0.01; 
		this.mzppm = 10.0; 
		this.posCharge = true; 
		this.mode = 1; 
		this.treeDepth = 2; 
	}

	@Test
	public void test() {
		IAtomContainer[] matchedFragments = MetfRag.generateMatchingFragments(MoleculeFunctions.parseSmiles("C1C(OC2=CC(=CC(=C2C1=O)O)O)C3=CC=C(C=C3)O"), 
				this.masses, this.exactMass, this.mzabs, this.mzppm, this.posCharge, this.mode, this.treeDepth);
		System.out.println("Generated " + matchedFragments.length + " matching fragments..");
	}

}
