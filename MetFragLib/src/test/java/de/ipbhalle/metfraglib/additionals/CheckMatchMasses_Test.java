package de.ipbhalle.metfraglib.additionals;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckMatchMasses_Test {

	@Test
	public void test() {
		assertTrue("100.0 100.1 0 0.1 don't match", MathTools.matchMasses(100.0, 100.1, 0, 0.1));
		assertTrue("100.0 100.1 100 0 don't match", MathTools.matchMasses(100.0, 100.01, 100, 0));
		assertTrue("100.0 100.1 100 0.05 don't match", MathTools.matchMasses(100.0, 100.1, 100, 0.09));
	
		assertFalse("100.0 100.1 100 0.05 match", MathTools.matchMasses(100.0, 100.1, 0, 0));
		assertFalse("100.0 100.1 100 0.05 match", MathTools.matchMasses(100.0, 100.00001, 0, 0));
		
	}

}
