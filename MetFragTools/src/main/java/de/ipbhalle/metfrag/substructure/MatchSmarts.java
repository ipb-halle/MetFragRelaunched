package de.ipbhalle.metfrag.substructure;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;

import java.io.BufferedReader;

public class MatchSmarts {

	public static void main(String[] args) throws Exception {
		String filename = args[0];
		String smarts = args[1];
		Vector<MoleculeEntry> entries = readMolecules(filename);
		
		SMARTSQueryTool sqt = new SMARTSQueryTool(smarts, DefaultChemObjectBuilder.getInstance());
		
		System.out.println(smarts + " " + entries.size());
		for(int i = 0; i < entries.size(); i++) {
			
			IAtomContainer con = null;
			try {
				con = MoleculeFunctions.getAtomContainerFromSMILES(entries.get(i).smiles);
			} catch(Exception e) {
				continue;
			}
			
			if(sqt.matches(con)) {
				System.out.println(entries.get(i).name + " " + entries.get(i).smiles);
			}
		}
	}
	
	public static Vector<MoleculeEntry> readMolecules(String filename) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		Vector<MoleculeEntry> entries = new Vector<MoleculeEntry>();
		while((line = breader.readLine()) != null) {
			String[] tmp = line.split("\\s+");
			MoleculeEntry entry = new MatchSmarts().new MoleculeEntry(tmp[1], tmp[0]);
			entries.add(entry);
		}
		breader.close();
		return entries;
	}
	
	public class MoleculeEntry {
		
		public String smiles;
		public String name;
		
		public MoleculeEntry(String smiles, String name) {
			this.smiles = smiles;
			this.name = name;
		}
		
	}
	
	
}
