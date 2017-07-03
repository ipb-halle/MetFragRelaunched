package de.ipbhalle.metfrag.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

public class ConvertMassBankToMetFragRecord {

	public static java.util.Hashtable<String, String> argsHash;
	
	public static void main(String[] args) throws IOException {
		boolean correctArgs = getArgs(args);
		if(!correctArgs) {
			return;
		}
		
		MassBankObject mo = new MassBankObject(argsHash.get("infile"));
		if(!mo.isProperlyInitialized()) {
			System.err.println("Skipping " + argsHash.get("infile"));
			return;
		}
		
		Vector<String> lines = new Vector<String>();
		
		lines.add("PeakListString = " + mo.getMzIntValuesToString());
		lines.add("SampleName = " + mo.getAccession());
		lines.add("MetFragPeakListReader = de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader");
		lines.add("IonizedPrecursorMass = " + mo.getPrecursorMz());
		
		String ionType = mo.getPrecursorType();
		
		if(!de.ipbhalle.metfraglib.parameter.Constants.ADDUCT_TYPES.contains(ionType)) {
			System.err.println("precursor type " + ionType + " for " + mo.getAccession() + " not known.");
			return;
		}
		lines.add("PrecursorIonType = " + ionType);
		lines.add("PrecursorCompoundIDs = " + mo.getPubchemID());
		
		BufferedReader breader = new BufferedReader(new FileReader(new File(argsHash.get("paramfile"))));
		
		String line = "";
		while((line = breader.readLine()) != null) {
			lines.add(line);
		}
		
		lines.add("# " + mo.getInchikey().split("-")[0]);
		
		breader.close();
		
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(argsHash.get("outfile"))));
		for(int i = 0; i < lines.size(); i++) {
			bwriter.write(lines.get(i));
			bwriter.newLine();
		}
		
		bwriter.close();
	}

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("infile") && !tmp[0].equals("outfile") && !tmp[0].equals("resultpath")
					&& !tmp[0].equals("paramfile")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("infile")) {
			System.err.println("no infile defined");
			return false;
		}

		if (!argsHash.containsKey("outfile")) {
			System.err.println("no outfile defined");
			return false;
		}

		if (!argsHash.containsKey("paramfile")) {
			System.err.println("no paramfile defined");
			return false;
		}
		
		return true;
	}
	
}
