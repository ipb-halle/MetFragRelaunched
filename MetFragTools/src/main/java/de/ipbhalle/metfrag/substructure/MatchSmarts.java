package de.ipbhalle.metfrag.substructure;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

public class MatchSmarts {

	public static void main(String[] args) throws Exception {
		
		String smarts1 = "C=CC";
		String smarts2 = "cccN";
		String smiles = "c1ccccc1N";
		
		SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
		IAtomContainer con = sp.parseSmiles(smiles);
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(con);
		
		SMARTSQueryTool sqt = new SMARTSQueryTool(smarts1, DefaultChemObjectBuilder.getInstance());
		
		System.out.println(smarts1 + " in " + smiles + ": " + sqt.matches(con));
		
		sqt.setSmarts(smarts2);
		
		System.out.println(smarts2 + " in " + smiles + ": "+ sqt.matches(con));
	}
	
}
