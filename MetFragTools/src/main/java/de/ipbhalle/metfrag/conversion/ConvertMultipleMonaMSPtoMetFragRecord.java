package de.ipbhalle.metfrag.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalMetChemDatabase;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class ConvertMultipleMonaMSPtoMetFragRecord {

	public static java.util.Hashtable<String, String> argsHash;
	public static boolean endOfFile = false;
	public static HashMap<String, Double[]> prefixToMassDeviation;
	static {
		prefixToMassDeviation = new HashMap<String, Double[]>();
		prefixToMassDeviation.put("BM", new Double[] {0.01, 10.0});
		prefixToMassDeviation.put("FI", new Double[] {0.01, 10.0});
		prefixToMassDeviation.put("PB", new Double[] {0.01, 10.0});
		prefixToMassDeviation.put("JE", new Double[] {0.01, 10.0});
		prefixToMassDeviation.put("PR", new Double[] {0.01, 10.0});
		prefixToMassDeviation.put("TY", new Double[] {0.01, 10.0});
	}
	
	
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("inputfile") && !tmp[0].equals("outputfolder") &&
					!tmp[0].equals("addparamsfile") && !tmp[0].equals("sampleNameFilter")
					 && !tmp[0].equals("outputfolderdata")) {
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
		if (!argsHash.containsKey("addparamsfile")) {
			System.err.println("no addparamsfile defined");
			return false;
		}
		if (!argsHash.containsKey("outputfolder")) {
			System.err.println("no outputfolder defined");
			return false;
		}
		if (!argsHash.containsKey("outputfolderdata")) {
			argsHash.put("outputfolderdata", "");
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
		String outputfolderdata = argsHash.get("outputfolderdata");
		
		java.util.Vector<Entry> entries = new java.util.Vector<Entry>();
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(inputfile)));
			while(!endOfFile) {
				Entry entry = getNextEntry(breader, sampleNameFilter == null ? "" : sampleNameFilter, addparamsfile);
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
		
		entries = checkEntries(entries, addparamsfile);
		
		for(int i = 0; i < entries.size(); i++) {
			Entry entry = entries.get(i);
			String filename = entry.sampleName;
			if(entry.charge.equals("positive")) filename += "-01";
			else filename += "-02";
			entry.sampleName = filename;
			filename += ".txt";
			try {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputfolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename)));
				bwriter.write(entry.toMetFragString(additionalParams, outputfolderdata));
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static java.util.Vector<Entry> checkEntries(java.util.Vector<Entry> entries, String addparamsfile) {
		if(entries.size() == 0) return entries;
		MetFragGlobalSettings settings;
		try {
			boolean[] inchikey_id_missmatches = new boolean[entries.size()];
			String[] alternative_pubchem_id = new String[entries.size()];
			settings = MetFragGlobalSettings.readSettings(new File(addparamsfile), null);
			LocalMetChemDatabase lmcd = new LocalMetChemDatabase(settings);
			// check inchikeys
			String inchikeyset = "'" + entries.get(0).inchikey.split("-")[0] + "'";
			for(int i = 1; i < entries.size(); i++) {
				inchikeyset += ",'" + entries.get(i).inchikey.split("-")[0] + "'";
			}
			CandidateList candidates = lmcd.getCandidateListByQuery("2", "c.inchi_key_1 in (" + inchikeyset + ")");
			for(int i = 0; i < entries.size(); i++) {
				inchikey_id_missmatches[i] = true;
				boolean inchikeyfound = false;
				String inchikey1 = entries.get(i).inchikey.split("-")[0];
				LinkedList<String> alternative_pubchem_ids = new LinkedList<String>();
				for(int j = 0; j < candidates.getNumberElements(); j++) {
					if(inchikey1.equals((String)candidates.getElement(j).getProperty(VariableNames.INCHI_KEY_1_NAME))) {
						inchikeyfound = true;
						alternative_pubchem_ids.add(candidates.getElement(j).getIdentifier() + "|" + candidates.getElement(j).getProperty(VariableNames.INCHI_KEY_3_NAME));
						if(candidates.getElement(j).getIdentifier().equals(entries.get(i).pubchemcid)) inchikey_id_missmatches[i] = false;
					}
				}
				if(!inchikeyfound) inchikey_id_missmatches[i] = true;
				if(inchikey_id_missmatches[i] && alternative_pubchem_ids.size() != 0) {
					java.util.Iterator<String> it = alternative_pubchem_ids.iterator();
					while(it.hasNext()) { 
						String currentid = it.next();
						if(currentid.endsWith("N")) alternative_pubchem_id[i] = currentid.split("\\|")[0];
					}
					if(alternative_pubchem_id[i] == null || alternative_pubchem_id[i].equals("")) 
						alternative_pubchem_id[i] = alternative_pubchem_ids.getFirst().split("\\|")[0];
				}
			}
			String cidString = "";
			for(int i = 0; i < inchikey_id_missmatches.length; i++) {
				if(inchikey_id_missmatches[i]) {
					cidString += "'" + entries.get(i).pubchemcid + "',";
				}
			}
			if(cidString.length() == 0) return entries;
			cidString = cidString.substring(0, cidString.length() - 1);
			candidates = lmcd.getCandidateListByQuery("2", "s.accession in (" + cidString + ")");
			for(int i = 0; i < inchikey_id_missmatches.length; i++) {
				if(inchikey_id_missmatches[i]) {
					System.out.println("inchikey pubchemid mismatch found: " + entries.get(i).sampleName + ": " + entries.get(i).inchikey + " " + entries.get(i).pubchemcid);
					boolean cid_found = false;
					for(int j = 0; j < candidates.getNumberElements(); j++) {
						if(candidates.getElement(j).getIdentifier().equals(entries.get(i))) {
							entries.get(i).inchikey = (String)candidates.getElement(j).getProperty(VariableNames.INCHI_KEY_1_NAME);
							cid_found = true;
							break;
						}
					}
					if(!cid_found) {
						if(alternative_pubchem_id[i] != null && !alternative_pubchem_id[i].equals("")) {
							entries.get(i).pubchemcid = alternative_pubchem_id[i];
							cid_found = true;
						}
					}
					if(cid_found) {
						System.out.println(" corrected " + entries.get(i).inchikey + " " + entries.get(i).pubchemcid);
						inchikey_id_missmatches[i] = false;
					}
				}
			}
			// prepare new entry vector
			java.util.Vector<Entry> newentries = new java.util.Vector<Entry>();
			for(int i = 0; i < inchikey_id_missmatches.length; i++) {
				if(!inchikey_id_missmatches[i])
					newentries.add(entries.get(i));
			}
			return newentries;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static Entry getNextEntry(BufferedReader breader, String sampleNameFilter, String addparamsfile) throws IOException {
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
				
				if(entry.ionmode == null) {
					if(entry.formula == null || entry.ionizedmass == null) return null;
					Integer ionmodeDiff = Integer.MAX_VALUE;
					try {
						ionmodeDiff = (int)MathTools.round(Double.parseDouble(entry.ionizedmass) 
								- new ByteMolecularFormula(entry.formula).getMonoisotopicMass(), 0);
						if(!Constants.checkIonisationNominalMass(ionmodeDiff)) {
							return null;
						}
						entry.ionmode = Constants.getIonisationTypeByNominalMassDifference(ionmodeDiff);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AtomTypeNotKnownFromInputListException e) {
						System.err.println("Error at " + entry.sampleName + " Formula " + entry.formula + "contains at least one unknown element.");
						return null;
					} catch(Exception e) {
						System.err.println("Error at " + entry.sampleName + " " + entry.ionizedmass + " " + ionmodeDiff + " " + entry.formula);
						e.printStackTrace();
						System.exit(1);
					}
				}
				
				if(entry.charge == null) {
					try {
						entry.charge = Constants.getIonisationChargeByType(entry.ionmode) ? "positive" : "negative";
					} catch(Exception e) {
						System.err.println("Error " + entry.ionmode + " not known.");
						System.exit(1);
					}
				}
				
				String errorType = entry.hasMinimumRequirements();
				if(!errorType.equals("")) {
					System.out.println("Error at " + entry.sampleName + " " + errorType);
					return null;
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

		public String hasMinimumRequirements() {
			if(this.sampleName == null || this.sampleName.equals("")) return "No samplename defined";
			if(this.inchi == null || this.inchi.equals("")) return "No inchi defined";
			if(this.smiles == null || this.smiles.equals("")) return "No smiles defined";
			if(this.inchikey == null || this.inchikey.equals("")) return "No inchikey defined";
			if(this.ionmode == null || this.ionmode.equals("")) return "No ionmode defined";
			if(this.inchikey == null || this.inchikey.equals("")) return "No inchikey defined";
			return "";
		}
		
		private String getMzIntValuesToDataString() {
			String string = "";
			if(mzs.length > 0) string = mzs[0] + " " + ints[0];
			for(int i = 1; i < mzs.length; i++) {
				string += "\n" + mzs[i] + " " + ints[i];
			}
			return string + "\n";
		}

		public boolean checkIdentifiers(String addparamsfile) {
			MetFragGlobalSettings settings;
			try {
				settings = MetFragGlobalSettings.readSettings(new File(addparamsfile), null);
				LocalMetChemDatabase lmcd = new LocalMetChemDatabase(settings);
				// check inchikey
				boolean inchikey_id_missmatch = false;
				String alternative_pubchem_id = "";
				if(this.inchikey != null && !this.inchikey.equals("")) {
					String inchikey1 = this.inchikey.split("-")[0];
					CandidateList candidates = lmcd.getCandidateListByQuery("2", "c.inchi_key_1='" + inchikey1 + "'");
					if(candidates.getNumberElements() == 0) inchikey_id_missmatch = true;
					else if(this.pubchemcid != null && !this.pubchemcid.equals("")) {
						inchikey_id_missmatch = true;
						for(int i = 0; i < candidates.getNumberElements(); i++) {
							String identifier = candidates.getElement(i).getIdentifier();
							if(identifier.equals(this.pubchemcid)) inchikey_id_missmatch = false;
						}
					}
					if(inchikey_id_missmatch && candidates.getNumberElements() != 0) 
						alternative_pubchem_id = candidates.getElement(0).getIdentifier();
				}
				if(inchikey_id_missmatch) 
					System.out.println("inchikey pubchemid mismatch found: " + this.sampleName + ": " + this.inchikey + " " + this.pubchemcid);
				if(inchikey_id_missmatch) {
					CandidateList candidates = lmcd.getCandidateListByQuery("2", "s.accession='" + this.pubchemcid + "'");
					if(candidates.getNumberElements() == 0 && !alternative_pubchem_id.equals("")) this.pubchemcid = alternative_pubchem_id;
					if(candidates.getNumberElements() == 0) return false;
					this.inchikey = (String)candidates.getElement(0).getProperty(VariableNames.INCHI_KEY_1_NAME);
					if(inchikey_id_missmatch) 
						System.out.println(" corrected " + this.inchikey + " " + this.pubchemcid);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		
		public String toMetFragString(java.util.Vector<String> additionalLines, String outputfolderdata) throws IOException {
			String lines = "";
			if(outputfolderdata.equals("")) {
				lines += "PeakListString = " + this.getMzIntValuesToString() + "\n";
				lines += "MetFragPeakListReader = de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader" + "\n";
			} else {
				lines += "PeakListPath = " + outputfolderdata + Constants.OS_SPECIFIC_FILE_SEPARATOR + this.sampleName + ".mb\n";
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputfolderdata + Constants.OS_SPECIFIC_FILE_SEPARATOR + this.sampleName + ".mb")));
				bwriter.write(this.getMzIntValuesToDataString());
				bwriter.close();
			}
			lines += "SampleName = " + this.sampleName + "\n";
			lines += "UseSmiles = True\n";
			lines += "LocalDatabasePath = /scratch/cruttkie/fingerprint_training/casmi/results/candidates/" + this.sampleName + ".psv\n";
			lines += "IonizedPrecursorMass = " + this.ionizedmass + "\n";
			lines += "NeutralPrecursorMolecularFormula = " + this.formula + "\n";
			String ionType = this.ionmode;
			lines += "PrecursorIonType = " + ionType + "\n";
			if(charge != null) lines += "IsPositiveIonMode = " + (charge.toLowerCase().equals("positive") ? "True" + "\n" : "False" + "\n");
			lines += "PrecursorCompoundIDs = " + this.pubchemcid + "\n";
			
			Double[] massdevs = null;
			if(prefixToMassDeviation != null) {
				if(prefixToMassDeviation.containsKey(this.sampleName.substring(0, 2))) {
					massdevs = prefixToMassDeviation.get(this.sampleName.substring(0, 2));
				}
			}
			
			for(int i = 0; i < additionalLines.size(); i++) {
				if(massdevs != null && additionalLines.get(i).startsWith("FragmentPeakMatchAbsoluteMassDeviation")) lines += "FragmentPeakMatchAbsoluteMassDeviation = " + massdevs[0] + "\n";
				else if(massdevs != null && additionalLines.get(i).startsWith("FragmentPeakMatchRelativeMassDeviation")) lines += "FragmentPeakMatchRelativeMassDeviation = " + massdevs[1] + "\n";
				else lines += additionalLines.get(i) + "\n";
			}
			
			lines += "# " + this.inchikey.split("-")[0] + "\n";
		
			return lines;
		}
		
		
	}
}
