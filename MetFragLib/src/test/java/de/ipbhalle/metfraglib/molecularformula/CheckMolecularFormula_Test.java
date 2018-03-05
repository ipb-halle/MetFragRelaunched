package de.ipbhalle.metfraglib.molecularformula;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;

public class CheckMolecularFormula_Test {
	
	private ByteMolecularFormula molForm1;
	private ByteMolecularFormula molForm2;
	private ByteMolecularFormula molForm3;
	private ByteMolecularFormula molForm4;
	
	@Before
	public void setUp() {
		try {
			molForm1 = new ByteMolecularFormula("C12H20");
			molForm2 = new ByteMolecularFormula("C15H12O5");
			molForm3 = new ByteMolecularFormula("C9H11Cl3NO3PS");
			molForm4 = new ByteMolecularFormula("C42H69Cl2N5O10");
		}
		catch(AtomTypeNotKnownFromInputListException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testMonoisotopicMasses() {
		assertEquals("Mass C12H20 mismatch", 164.1566, this.molForm1.getMonoisotopicMass(), 0.0001);
		assertEquals("Mass C15H12O5 mismatch", 272.06851, this.molForm2.getMonoisotopicMass(), 0.0001);
		assertEquals("Mass C9H11Cl3NO3PS mismatch", 348.92631, this.molForm3.getMonoisotopicMass(), 0.0001);
		assertEquals("Mass C42H69Cl2N5O10 mismatch", 873.44242, this.molForm4.getMonoisotopicMass(), 0.0001);
	}
	
	@Test(expected=AtomTypeNotKnownFromInputListException.class)
	public void testUnknownAtomTypeException() throws AtomTypeNotKnownFromInputListException {
		new ByteMolecularFormula("C20A3");
	}
}
