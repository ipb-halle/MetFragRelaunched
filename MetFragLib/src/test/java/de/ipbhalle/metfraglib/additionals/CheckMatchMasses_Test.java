package de.ipbhalle.metfraglib.additionals;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckMatchMasses_Test {

	@Test
	public void test() {
		//check that mass matches
		assertTrue("Masses: 100.0 100.1; mzppm:0 	 mzabs:0.1 	=> don't match", 
				MathTools.matchMasses(100.0, 100.1, 0, 0.1));
		assertTrue("Masses: 100.0 100.1; mzppm:100  mzabs:0 	=> don't match", 
				MathTools.matchMasses(100.0, 100.01, 100, 0));
		assertTrue("Masses: 100.0 100.1; mzppm:100  mzabs:0.09	=> don't match", 
				MathTools.matchMasses(100.0, 100.1, 100, 0.09));
		
		//check that mass doesn't match
		assertFalse("Masses: 100.0 100.1; mzppm:100  mzabs:0.05 	=> match", 
				MathTools.matchMasses(100.0, 100.1, 0, 0));
		assertFalse("Masses: 100.0 100.1; mzppm:100  mzabs:0.05 	=> match", 
				MathTools.matchMasses(100.0, 100.00001, 0, 0));
	}

}
