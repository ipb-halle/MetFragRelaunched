package de.ipbhalle.metfrag.conversion;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class ConvertMGFtoMetFragRecord {

	public static Hashtable<String, String> parameterConversion;
	public static Hashtable<String, String> adductTypes;
	public static Hashtable<String, String> chargeTypes;

	static {
		parameterConversion = new Hashtable<String, String>();
		parameterConversion.put("MSLEVEL", "# MSLEVEL = ");
		parameterConversion.put("SPECTRUMID", "# SampleName = ");
		parameterConversion.put("Name", "# Name = ");
		parameterConversion.put("NAME", "# Name = ");
		parameterConversion.put("INCHI", "# InChI = ");
		parameterConversion.put("InChIKey", "# InChIKey = ");
		parameterConversion.put("ms level", "# MSLevel = ");
		parameterConversion.put("INCHIKEY", "# InChIKey = ");
		parameterConversion.put("precursor m/z", "# IonizedPrecursorMass = ");
		parameterConversion.put("PRECURSORMZ", "# IonizedPrecursorMass = ");
		parameterConversion.put("retention time", "# RetentionTime = ");
		parameterConversion.put("RETENTIONTIME", "# RetentionTime = ");
		parameterConversion.put("exact mass", "# NeutralPrecursorMass = ");
		parameterConversion.put("ion mode", "# IsPositiveIonMode = ");
		parameterConversion.put("IONMODE", "# IsPositiveIonMode = ");
		parameterConversion.put("mass error", "# MassError = ");
		parameterConversion.put("precursor type", "# PrecursorIonMode = ");
		parameterConversion.put("PRECURSORTYPE", "# PrecursorIonMode = ");
		parameterConversion.put("Num Peaks", "# NumPeaks = ");
		parameterConversion.put("formula", "# NeutralPrecursorMolecularFormula = ");
		parameterConversion.put("FORMULA", "# NeutralPrecursorMolecularFormula = ");
		
		adductTypes = new Hashtable<String, String>();
		adductTypes.put("[2M-H]-", "-1");
		adductTypes.put("[2M+H]+", "1");
		adductTypes.put("[2M+Na]+", "23");
		adductTypes.put("[M]-", "0");
		adductTypes.put("[M]+", "0");
		adductTypes.put("M-", "0");
		adductTypes.put("[M+15]+", "1");
		adductTypes.put("[M-2H]-", "-1");
		adductTypes.put("[M-2H2O+H]+", "1");
		adductTypes.put("[M-2H2O+H]+,[M-H2O+H]+", "1");
		adductTypes.put("[M-2H+H2O]-", "-1");
		adductTypes.put("[M-3]+,[M-H2O+H]+", "1");
		adductTypes.put("[(M+CH3COOH)-H]-", "59");
		adductTypes.put("[M+CH3COO]-/[M-CH3]-", "59");
		adductTypes.put("[M+CH3COO]-", "59");
		adductTypes.put("[M-H]-", "-1");
		adductTypes.put("[M+H]+", "1");
		adductTypes.put("[M-H2O+H]+", "1");
		adductTypes.put("[M-H2O+H]+,[M-2H2O+H]+", "1");
		adductTypes.put("[M+H-C6H10O4]+", "1");
		adductTypes.put("[M-H-C6H10O5]-", "-1");
		adductTypes.put("[M-H-C6H10O5}-", "-1");
		adductTypes.put("[M+H-C6H10O5]+", "1");
		adductTypes.put("[M-H-CO2]-", "-1");
		adductTypes.put("[M+HCOO-]-", "45");
		adductTypes.put("[M+H-H2O]+", "1");
		adductTypes.put("[M+H]+,[M-H2O+H]+", "1");
		adductTypes.put("[M-H+OH]-", "-1");
		adductTypes.put("[M+Na]+", "23");
		adductTypes.put("[(M+NH3)+H]+", "18");
		adductTypes.put("[M+NH4]+", "18");
		
		chargeTypes = new Hashtable<String, String>();
		chargeTypes.put("positive", "True");
		chargeTypes.put("Positive", "True");
		chargeTypes.put("POSITIVE", "True");
		chargeTypes.put("negative", "False");
		chargeTypes.put("Negative", "False");
		chargeTypes.put("NEGATIVE", "False");
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.err.println("MGF input file needed. MetFrag record output folder needed.");
			System.exit(1);
		}
		String mgffilename = args[0];
		String metfragfilename = args[1];
		File mgffile = new File(mgffilename);
		File metfragfolder = new File(metfragfilename);
		
		if(!mgffile.exists()) {
			System.err.println("Haven't found " + mgffilename + ". Check path.");
			System.exit(2);
		}
		if(!mgffile.canRead()) {
			System.err.println("Cannot read " + mgffilename + ". Check permissions.");
			System.exit(2);
		}
		java.util.ArrayList<Entry> entries = new java.util.ArrayList<Entry>();
		
		try {
			BufferedReader breader = new BufferedReader(new FileReader(mgffile));
			String line = "";
			int numberScans = 0;
			String lastInChI = "";
			String lastSmiles = "";
			String lastFormula = "";
			String lastMZ = "";
			String lastName = "";
			String lastIonMode = "";
			String lastPrecursorMode = "";
			String lastSpectrumid = "";
			String lastMslevel = "";
			Entry currentEntry = new ConvertMGFtoMetFragRecord().new Entry();
			boolean entryFine = true;
			while((line = breader.readLine()) != null) {
				//get param name and value
				line = line.trim();
				if(line.length() == 0) continue;
				String[] tmp = line.split("=");
				String paramname = "";
				String value = "";
				if(numberScans <= 0) {
					paramname = tmp[0].trim();
					value = "";
					if(tmp.length >= 2)
						value = tmp[1];
					for(int i = 2; i < tmp.length; i++) {
						value += "=" + tmp[i].trim();
					}
					paramname = paramname.trim();
					value = value.trim();
					if(paramname.equals("INCHI")) {
						lastInChI = value;
						currentEntry.inchi = lastInChI;
					}
					if(paramname.equals("SMILES")) {
						lastSmiles = value;
						currentEntry.smiles = lastSmiles;
					}
					if(paramname.equals("PEPMASS")) {
						lastMZ = value;
						currentEntry.ionizedmass = lastMZ;
					}
					if(paramname.equals("NAME")) {
						lastName = value;
						currentEntry.name = lastName;
					}
					if(paramname.equals("SPECTRUMID")) {
						lastSpectrumid = value;
						currentEntry.spectrumid = lastSpectrumid;
					}
					if(paramname.equals("MSLEVEL")) {
						lastMslevel = value;
						currentEntry.mslevel = lastMslevel;
					}
					if(paramname.equals("IONMODE")) {
						String ionmode = chargeTypes.get(value);
						lastIonMode = ionmode;
						currentEntry.ionmode = lastIonMode;
					}
					if(paramname.equals("SCANS")) {
						numberScans = Integer.parseInt(value);
						if(lastInChI == null || lastInChI.equals("")) {
							if(lastSmiles != null && lastSmiles.length() != 0) 
								try {
									lastInChI = MoleculeFunctions.getInChIFromSmiles(lastSmiles);
								} catch(Exception e) {
									System.out.println("Error: smiles " + currentEntry.spectrumid + " " + lastSmiles);
									continue;
								}
						}
						if(lastInChI == null || lastInChI.length() == 0) continue;
						IAtomContainer con = null;
						try {
							con = MoleculeFunctions.getAtomContainerFromInChI(lastInChI);
						} catch(Exception e) {
							try {
								if(lastSmiles == null || lastSmiles.length() == 0 || lastSmiles.equals("N/A")) {
									System.out.println("Error: " + currentEntry.spectrumid + " no valid inchi, no smiles");
									entryFine = false;
									continue;
								}
								lastInChI = MoleculeFunctions.getInChIFromSmiles(lastSmiles);
								con = MoleculeFunctions.getAtomContainerFromInChI(lastInChI);
							} catch(Exception e1) {
								System.out.println("Error: getInChIFromSmiles " + currentEntry.spectrumid + " " + lastSmiles + " " + lastInChI);
								entryFine = false;
								continue;
							}
						}
						lastFormula = lastInChI.split("/")[1];
						try {
							entryFine = checkInChIFormula(lastInChI, lastFormula, "");
						} catch(Exception e) {
							entryFine = false;
						}
						if(!entryFine) continue;
						if(lastSmiles == null || lastSmiles.length() == 0 || lastSmiles.equals("N/A")) 
							lastSmiles = MoleculeFunctions.generateSmiles(con);
						currentEntry.inchi = lastInChI;
						currentEntry.smiles = lastSmiles;
						
						String[] names_splitted = currentEntry.name.split("\\s+");
						lastPrecursorMode = names_splitted[names_splitted.length - 1];
						if(currentEntry.ionmode.equals("True")) lastPrecursorMode = "[" + lastPrecursorMode + "]+"; 
						if(currentEntry.ionmode.equals("False")) lastPrecursorMode = "[" + lastPrecursorMode + "]-"; 
						currentEntry.adducttype = lastPrecursorMode;
					}
				}
				else {
					//read peak
					if(line.equals("END IONS")) {
						if(entryFine) entries.add(currentEntry);
						currentEntry = new ConvertMGFtoMetFragRecord().new Entry();
						lastInChI = "";
						lastSmiles = "";
						lastFormula = "";
						lastIonMode = "";
						entryFine = true;
						numberScans = 0;
						continue;
					}
					String[] peaks = line.split("\\s+");
					try {
						currentEntry.addPeak(peaks[0], peaks[1]);
					} catch(Exception e) {
						System.out.println("Error addPeak: " + line);
						continue;
					}
				}
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("number entries : " + entries.size());

		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(metfragfolder.getAbsolutePath() + Constants.OS_SPECIFIC_FILE_SEPARATOR + "smiles.txt"));
			for(int i = 0; i < entries.size(); i++) {
				bwriter.write(entries.get(i).spectrumid + " " + entries.get(i).smiles);
				bwriter.newLine();
			}
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public static boolean checkInChIFormula(String inchi, String formula, String inchikey) throws AtomTypeNotKnownFromInputListException {
		ByteMolecularFormula inchiFormula = new ByteMolecularFormula(inchi.split("/")[1]);
		ByteMolecularFormula queryFormula = new ByteMolecularFormula(formula);
		if(!inchiFormula.compareToWithoutHydrogen(queryFormula)) {
			System.out.println("Mismatch " + inchi + " " + formula + " " + inchikey);
			return false;
		}
		return true;
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
		public String name;
		public String samplename;
		public String mslevel;
		public String masserror;
		public String fingerprint;
		public String spectrumid;
		public java.util.ArrayList<String> mzs;
		public java.util.ArrayList<String> ints;
		
		public void addPeak(String mz, String intensity) {
			if(mzs == null) mzs = new java.util.ArrayList<String>();
			if(ints == null) ints = new java.util.ArrayList<String>();
			mzs.add(mz);
			ints.add(intensity);
		}
		
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
				for(int i = 0; i < ints.size(); i++) {
					string += mzs.get(i) + " " + ints.get(i) + "\n";
				}
			}
			return string;
		}
	}
	
}
