package de.ipbhalle.metfraglib.candidate;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfraglib.interfaces.ICandidate;

public class CheckPrecursorCandidateFunctions {

	private ICandidate candidate;
	
	@Before
	public void setUp() throws Exception {
		this.candidate = new PrecursorCandidate("InChI=1S/C12H19NO5S2/c1-12(2,3)18-11(14)13-9(8-17-20(4,15)16)10-6-5-7-19-10/h5-7,9H,8H2,1-4H3,(H,13,14)/t9-/m0/s1", "13877939");
	}

	@Test
	public void testReferenceObjects() {
		assertNotSame(this.candidate, this.candidate.clone());
	}

	@Test
	public void testAtomContainerConversion() {
		try {
			assertTrue("Equal number of atoms in implicit/explicit-hydrogen atomcontainers", this.candidate.getAtomContainer().getAtomCount() != this.candidate.getImplicitHydrogenAtomContainer().getAtomCount());
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}

}
