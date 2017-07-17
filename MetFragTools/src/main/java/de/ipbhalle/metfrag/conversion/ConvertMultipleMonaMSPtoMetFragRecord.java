package de.ipbhalle.metfrag.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.ipbhalle.metfraglib.parameter.Constants;

public class ConvertMultipleMonaMSPtoMetFragRecord {

	public static java.util.Hashtable<String, String> argsHash;
	public static boolean endOfFile = false;
	
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("inputfile") && !tmp[0].equals("outputfolder") &&
					!tmp[0].equals("addparamsfile") && !tmp[0].equals("sampleNameFilter")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("inputfile")) {
			System.err.println("no inputfile defined");
			return false;
		}
		if (!argsHash.containsKey("outputfolder")) {
			System.err.println("no outputfolder defined");
			return false;
		}
		
		return true;
	}

	public static void main(String[] args) {
		boolean argCorrect = getArgs(args);
		if (!argCorrect) {
			System.err.println(
					"run: progname inputfile='input msp file' "
					+ "outputfolder='output folder' "
					+ "[addparamsfile='file with additional parameters to add'] "
					+ "[sampleNameFilter='filter for accessions to include']");
			System.exit(1);
		}

		String inputfile = argsHash.get("inputfile");
		String outputfolder = argsHash.get("outputfolder");
		String addparamsfile = argsHash.get("addparamsfile");
		String sampleNameFilter = argsHash.get("sampleNameFilter");
		
		java.util.Vector<Entry> entries = new java.util.Vector<Entry>();
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(inputfile)));
			while(!endOfFile) {
				Entry entry = getNextEntry(breader, sampleNameFilter == null ? "" : sampleNameFilter);
				if(entry != null) entries.add(entry);
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		java.util.Vector<String> additionalParams = new java.util.Vector<String>();
		if(addparamsfile != null) {
			BufferedReader breader;
			try {
				breader = new BufferedReader(new FileReader(new File(addparamsfile)));
				String line = "";
				while((line = breader.readLine()) != null) {
					line = line.trim();
					if(!line.equals("")) additionalParams.add(line);
				}
				breader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			String filename = entry.sampleName;
			if(entry.charge.equals("positive")) filename += "-01";
			else filename += "-02";
			entry.sampleName = filename;
			filename += ".txt";
			try {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename)));
				bwriter.write(entry.toMetFragString(additionalParams));
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static Entry getNextEntry(BufferedReader breader, String sampleNameFilter) throws IOException {
		Entry entry = new ConvertMultipleMonaMSPtoMetFragRecord().new Entry();
		String line = "";
		while ((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.equals("")) continue;
			if(line.startsWith("DB#:")) {
				entry.sampleName = line.split("\\s+")[1].trim();
				if(!sampleNameFilter.equals("") && !entry.sampleName.startsWith(sampleNameFilter)) {
					goToNextEmptyLine(breader);
					return null;
				}
			}
			else if(line.startsWith("InChIKey:")) entry.inchikey = line.split("\\s+")[1].trim();
			else if(line.startsWith("Formula:")) entry.formula = line.split("\\s+")[1].trim();
			else if(line.startsWith("PrecursorMZ:")) entry.ionizedmass = line.split("\\s+")[1].trim();
			else if(line.startsWith("Comments:")) {
				line = line.replaceFirst("Comments:\\s+", "");
				String[] tmp = line.split("\"\\s+\"");
				for(int k = 0; k < tmp.length; k++) {
					String cur = tmp[k].replaceAll("\"", "");
					if(cur.startsWith("precursor type=")) {
						entry.ionmode = cur.split("=")[1].trim();
						if(!de.ipbhalle.metfraglib.parameter.Constants.ADDUCT_TYPES.contains(entry.ionmode)) {
							System.out.println(entry.sampleName + " " + entry.ionmode + " not known");
							goToNextEmptyLine(breader);
							return null;
						}
					}
					else if(cur.startsWith("InChI=")) entry.inchi = cur.split("=")[1].trim();
					else if(cur.startsWith("SMILES=")) entry.smiles = cur.split("=")[1].trim();
					else if(cur.startsWith("retention time=")) entry.rt = cur.split("=")[1].trim().split("\\s+")[0];
					else if(cur.startsWith("pubchem cid=")) entry.pubchemcid = cur.split("=")[1].trim();
				}
			}
			else if(line.startsWith("Num Peaks:")) {
				int numPeaks = Integer.parseInt(line.split("\\s+")[2].trim());
				String[] ints = new String[numPeaks];
				String[] mzs = new String[numPeaks];
				for(int l = 0; l < numPeaks; l++) {
					String peak = breader.readLine().trim();
					String[] tmp2 = peak.split("\\s+");
					mzs[l] = tmp2[0].trim();
					ints[l] = tmp2[1].trim();
				}
				entry.ints = ints;
				entry.mzs = mzs;
				
				if(entry.charge == null) {
					entry.charge = Constants.getIonisationChargeByType(entry.ionmode) ? "positive" : "negative";
				}
				
				return entry;
			}
		}
		endOfFile = true;
		return null;
	}
	
	public static void goToNextEmptyLine(BufferedReader breader) {
		String line = "";
		try {
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.equals("")) return;
			}
			endOfFile = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class Entry {
		public String sampleName;
		public String inchi;
		public String inchikey;
		public String adducttype;
		public String rt;
		public String mass;
		public String formula;
		public String smiles;
		public String ionizedmass;
		public String numpeaks;
		public String ionmode;
		public String masserror;
		public String mslevel;
		public String fingerprint;
		public String pubchemcid;
		public String charge;
		public String[] mzs;
		public String[] ints;
		
		public String toString() {
			String string = "";
			if(sampleName != null) string += "# SampleName = " + sampleName + "\n";
			if(inchi != null) string += "# InChI = " + inchi + "\n";
			if(smiles != null) string += "# Smiles = " + smiles + "\n";
			if(inchikey != null) string += "# InChIKey = " + inchikey + "\n";
			if(rt != null) string += "# RetentionTime = " + rt + "\n";
			if(ionmode != null) string += "# IsPositiveIonMode = " + ionmode + "\n";
			if(adducttype != null) string += "# PrecursorIonMode = " + adducttype + "\n";
			if(mass != null) string += "# NeutralPrecursorMass = " + mass + "\n";
			if(masserror != null) string += "# MassError = " + masserror + "\n";
			if(mslevel != null) string += "# MSLevel = " + mslevel + "\n";
			if(ionizedmass != null) string += "# IonizedPrecursorMass = " + ionizedmass + "\n";
			if(numpeaks != null) string += "# NumPeaks = " + numpeaks + "\n";
			if(fingerprint != null) string += "# MolecularFingerPrint = " + fingerprint + "\n";
			if(mzs != null && ints != null) {
				for(int i = 0; i < ints.length; i++) {
					string += mzs[i] + " " + ints[i] + "\n";
				}
			}
			return string;
		}
		
		private String getMzIntValuesToString() {
			String string = "";
			if(mzs.length > 0) string = mzs[0] + "_" + ints[0];
			for(int i = 1; i < mzs.length; i++) {
				string += ";" + mzs[i] + "_" + ints[i];
			}
			return string;
		}
		
		public String toMetFragString(java.util.Vector<String> additionalLines) {
			String lines = "";
			
			lines += "PeakListString = " + this.getMzIntValuesToString() + "\n";
			lines += "SampleName = " + this.sampleName + "\n";
			lines += "MetFragPeakListReader = de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader" + "\n";
			lines += "IonizedPrecursorMass = " + this.ionizedmass + "\n";
			String ionType = this.ionmode;
			lines += "PrecursorIonType = " + ionType + "\n";
			if(charge != null) lines += "IsPositiveIonMode = " + (charge.toLowerCase().equals("positive") ? "True" + "\n" : "False" + "\n");
			lines += "PrecursorCompoundIDs = " + this.pubchemcid + "\n";
			
			for(int i = 0; i < additionalLines.size(); i++) {
				lines += additionalLines.get(i) + "\n";
			}
			
			lines += "# " + this.inchikey.split("-")[0] + "\n";
		
			return lines;
		}
	}
}
