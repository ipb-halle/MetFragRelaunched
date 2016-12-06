package de.ipbhalle.metfraglib.additionals;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.Bond;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import de.ipbhalle.metfraglib.exceptions.ExplicitHydrogenRepresentationException;

public class CheckMoleculeConversions_Test {

	//hexane
	private IAtomContainer con;
	
	@Before
	public void setUp() {
		/*
		 * build atomcontainer
		 */
		this.con = new AtomContainer();
		IAtom[] atoms = new IAtom[6];
		IBond[] bonds = new IBond[5];
		
		for(int i = 0; i < atoms.length; i++) {
			atoms[i] = new Atom("C");
			this.con.addAtom(atoms[i]);
		}
		for(int i = 0; i < atoms.length - 1; i++) {
			bonds[i] = new Bond(atoms[i], atoms[i+1]);
			this.con.addBond(bonds[i]);
		}
		MoleculeFunctions.prepareAtomContainer(this.con, false);
		this.con = MoleculeFunctions.convertImplicitToExplicitHydrogens(this.con);
	}	
		
	@Test
	public void testSmiles() {
		//smiles
		String smiles = MoleculeFunctions.generateSmiles(this.con);
		assertEquals("C(C(C(C(C(C([H])([H])[H])([H])[H])([H])[H])([H])[H])([H])[H])([H])([H])[H]", smiles);
		
	}

	@Test
	public void testInChI() {
		//inchi
		String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(this.con);
		assertEquals("InChI=1S/C6H14/c1-3-5-6-4-2/h3-6H2,1-2H3", inchiInfo[0]);
		assertEquals("VLKZOEOYAKHREP-UHFFFAOYSA-N", inchiInfo[1]);
	}
	
	@Test
	public void testNumberNonHydrogenAtoms() {
		//non-hydrogen atoms
		assertTrue("Number non-hydrogen atom missmatch", MoleculeFunctions.countNonHydrogenAtoms(this.con) == 6);
	}

	@Test
	public void testMass() throws ExplicitHydrogenRepresentationException {
		//non-hydrogen atoms
		IAtomContainer conImplicitHydrogen = MoleculeFunctions.convertExplicitToImplicitHydrogens(this.con);
		assertTrue("Number non-hydrogen atom missmatch", MoleculeFunctions.countNonHydrogenAtoms(this.con) == 6);
		assertTrue("Number implicit-hydrogen atom missmatch", conImplicitHydrogen.getAtomCount() == 6);
		assertEquals(86.10955, MoleculeFunctions.calculateMonoIsotopicMassImplicitHydrogens(conImplicitHydrogen), 0.000001);
	}
	
}
