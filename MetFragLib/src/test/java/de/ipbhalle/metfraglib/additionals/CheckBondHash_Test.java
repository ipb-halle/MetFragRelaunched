package de.ipbhalle.metfraglib.additionals;

import static org.junit.Assert.*;

import org.junit.Test;

public class CheckBondHash_Test {

	@Test
	public void test() {
		Bond bond1 = new Bond("P", "S", '=');
		Bond bond2 = new Bond("P", "S", '-');
		Bond bond3 = new Bond("S", "P", '-');
		Bond bond4 = new Bond("H", "C", '-');
		Bond bond5 = new Bond("H", "C", '~');
		Bond bond6 = new Bond("Ge", "Ge", '~');
		Bond bond7 = new Bond("C", "Ge", '~');
		
		assertTrue(bond1 + " are equal " + bond2 + " " + bond1.hashCode() + " " + bond2.hashCode(), bond1.hashCode() != bond2.hashCode());
		assertTrue(bond1 + " are equal " + bond3 + " " + bond1.hashCode() + " " + bond3.hashCode(), bond1.hashCode() != bond3.hashCode());
		assertEquals(bond2.hashCode(), bond3.hashCode());

		assertTrue(bond4 + " are equal " + bond5 + " " + bond4.hashCode() + " " + bond5.hashCode(), bond4.hashCode() != bond5.hashCode());
		assertTrue(bond6 + " are equal " + bond7 + " " + bond6.hashCode() + " " + bond7.hashCode(), bond6.hashCode() != bond7.hashCode());
	}

}
