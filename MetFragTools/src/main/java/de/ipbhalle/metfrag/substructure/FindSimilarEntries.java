package de.ipbhalle.metfrag.substructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class FindSimilarEntries {

	public static void main(String[] args) throws InvalidSmilesException, IOException {
		String input_mf_file = args[0];
		String input_smiles = args[1];
		
		IAtomContainer con = MoleculeFunctions.parseSmiles(input_smiles);
		String fingerprint = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(con));
	
		BufferedReader breader = new BufferedReader(new FileReader(new File(input_mf_file)));
		String line = "";
		String lastID = "";
		String lastFingerprint = "";
		while((line = breader.readLine()) != null) {
			if(line.startsWith("# SampleName")) lastID = line.split("\\s+")[3];
			if(line.startsWith("# MolecularFingerPrint")) {
				lastFingerprint = line.split("\\s+")[3];
				double sim = TanimotoSimilarity.calculateSimilarity(MoleculeFunctions.stringToFingerPrint(lastFingerprint), MoleculeFunctions.stringToFingerPrint(fingerprint));
				if(sim >= 0.8) System.out.println(lastID + " " + sim);
			}
		}
		breader.close();
		
	}

}
