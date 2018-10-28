package de.ipbhalle.metfrag.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import de.ipbhalle.metfraglib.substructure.FingerprintToMasses;

public class ConvertMassToFingerprintFile {

	public static void main(String[] args) throws NumberFormatException, IOException {
		String filename = args[0];
		String output = args[1];
		FingerprintToMasses fingerprintToMasses = new FingerprintToMasses();
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0) continue;
			if(line.startsWith("#")) continue;
			if(line.startsWith("SUMMARY")) continue;
			String[] tmp = line.split("\\s+");
			Double peak = Double.parseDouble(tmp[0]);
			for(int i = 1; i < tmp.length; i++) {
				if((i % 2) == 1) {
					double count = Double.parseDouble(tmp[i]);
					fingerprintToMasses.addMass(tmp[i+1], peak, count);
				}
			}
		}
		breader.close();
		
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
		for(int i = 0; i < fingerprintToMasses.getFingerprintSize(); i++) {
			bwriter.write(fingerprintToMasses.toString(i));
			bwriter.newLine();
		}
		bwriter.close();
	}

}
