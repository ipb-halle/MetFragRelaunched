package de.ipbhalle.metfraglib.tools;

import java.util.Vector;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;

public class GenerateCandidateFileFromInChIs {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));
		Vector<String> lines = new Vector<String>();
		String line = "";
		int id = 1;
		lines.add("Identifier|MonoisotopicMass|InChI|InChIKey1|InChIKey2|MolecularFormula");
		while((line = breader.readLine()) != null) {
			line = line.trim();
			String[] tmp = line.split("\\|");
			String inchikey1 = tmp[1];
			String inchikey2 = tmp[2];
			String formula = tmp[0].split("/")[1];
			ByteMolecularFormula byteMolecularFormula = new ByteMolecularFormula(formula);
			double mass = byteMolecularFormula.getMonoisotopicMass();
			lines.add(id + "|" + mass + "|" + tmp[0] + "|" + inchikey1 + "|" + inchikey2 + "|" + formula);
			id++;
		}
		breader.close();
		
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[1])));
		
		for(int i = 0; i < lines.size(); i++) {
			bwriter.write(lines.get(i));
			bwriter.newLine();
		}
		
		bwriter.close();
	}

}
