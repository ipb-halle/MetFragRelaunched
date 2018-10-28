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

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class ConvertMSPtoMetFragRecord {

	public static boolean EI_mode = false;
	public static boolean WITH_FINGERPRINT = false;
	public static Hashtable<String, String> parameterConversion;
	public static Hashtable<String, String> adductTypes;
	public static Hashtable<String, String> chargeTypes;
	
	static {
		parameterConversion = new Hashtable<String, String>();
		parameterConversion.put("Name:", "# Name = ");
		parameterConversion.put("NAME:", "# Name = ");
		parameterConversion.put("InChI:", "# InChI = ");
		parameterConversion.put("InChIKey:", "# InChIKey = ");
		parameterConversion.put("ms level:", "# MSLevel = ");
		parameterConversion.put("INCHIKEY:", "# InChIKey = ");
		parameterConversion.put("precursor m/z:", "# IonizedPrecursorMass = ");
		parameterConversion.put("PRECURSORMZ:", "# IonizedPrecursorMass = ");
		parameterConversion.put("retention time:", "# RetentionTime = ");
		parameterConversion.put("RETENTIONTIME:", "# RetentionTime = ");
		parameterConversion.put("exact mass:", "# NeutralPrecursorMass = ");
		parameterConversion.put("ExactMass:", "# NeutralPrecursorMass = ");
		parameterConversion.put("EXACTMASS:", "# NeutralPrecursorMass = ");
		parameterConversion.put("ion mode:", "# IsPositiveIonMode = ");
		parameterConversion.put("IONMODE:", "# IsPositiveIonMode = ");
		parameterConversion.put("mass error:", "# MassError = ");
		parameterConversion.put("precursor type:", "# PrecursorIonMode = ");
		parameterConversion.put("PRECURSORTYPE:", "# PrecursorIonMode = ");
		parameterConversion.put("Num Peaks:", "# NumPeaks = ");
		parameterConversion.put("Num peaks:", "# NumPeaks = ");
		parameterConversion.put("formula:", "# NeutralPrecursorMolecularFormula = ");
		parameterConversion.put("FORMULA:", "# NeutralPrecursorMolecularFormula = ");
		parameterConversion.put("Formula:", "# NeutralPrecursorMolecularFormula = ");
		parameterConversion.put("LINKS:", "# SampleName = ");
		parameterConversion.put("MASSBANKACCESSION:", "# SampleName = ");
		
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
		if(args.length != 4 && args.length != 3 && args.length != 2) {
			System.err.println("MSP input file needed. MetFrag record output file needed.");
			System.exit(1);
		}
		String mspfilename = args[0];
		String metfragfilename = args[1];
		if(args.length >= 3) EI_mode = Boolean.parseBoolean(args[2]);
		if(args.length == 4) WITH_FINGERPRINT = Boolean.parseBoolean(args[3]);
		File mspfile = new File(mspfilename);
		File metfragfile = new File(metfragfilename);
		if(!mspfile.exists()) {
			System.err.println("Haven't found " + mspfilename + ". Check path.");
			System.exit(2);
		}
		if(!mspfile.canRead()) {
			System.err.println("Cannot read " + mspfilename + ". Check permissions.");
			System.exit(2);
		}
		java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(metfragfile));
			BufferedReader breader = new BufferedReader(new FileReader(mspfile));
			String line = "";
			int numberPeaks = 0;
			java.util.ArrayList<String> parametersFound = new java.util.ArrayList<String>();
			String lastInChI = "";
			String lastSmiles = "";
			String lastInChIKey = "";
			String lastFormula = "";
			String lastSampleName = "";
			String lastMZ = "";
			String lastMass = "";
			Entry currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
			while((line = breader.readLine()) != null) {
				//get param name and value
				line = line.trim();
				if(line.length() == 0) continue;
				String[] tmp = line.split("\\s+");
				String paramname = "";
				String value = "";
				boolean entryFine = true;
				boolean assignValue = false;
				if(numberPeaks <= 0) {
					for(int i = 0; i < tmp.length; i++) {
						if(!assignValue) paramname += tmp[i].trim() + " ";
						else value += tmp[i].trim() + " ";
						if(tmp[i].trim().endsWith(":")) assignValue = true;
					}
					paramname = paramname.trim();
					value = value.trim();
					if(paramname.equals("InChI:")) {
						lastInChI = value;
						currentEntry.inchi = lastInChI;
					}
					if(paramname.equals("SMILES:")) {
						lastSmiles = value;
						currentEntry.smiles = lastSmiles;
					}
					if(paramname.equals("formula:") || paramname.equals("FORMULA:") || paramname.equals("Formula:")) {
						lastFormula = value;
						currentEntry.formula = lastFormula;
					}
					if(paramname.equals("precursor m/z:") || paramname.equals("PRECURSORMZ:")) {
						lastMZ = value;
						currentEntry.ionizedmass = lastMZ;
					}
					if(paramname.equals("ExactMass:") || paramname.equals("Exact Mass:") || paramname.equals("EXACTMASS:")) {
						lastMass = value;
						currentEntry.mass = lastMass;
					}
					if(paramname.equals("InChIKey:") || paramname.equals("INCHIKEY:")) {
						lastInChIKey = value;
						currentEntry.inchikey = lastInChIKey;
					}
					if(paramname.equals("LINKS:") || paramname.equals("Links:")) {
						if(currentEntry.sampleName == null) { 
							lastSampleName = value;
							currentEntry.sampleName = lastSampleName;
						}
					}
					if(paramname.equals("MASSBANKACCESSION:")) {
						lastSampleName = value;
						currentEntry.sampleName = lastSampleName;
					}
					//start check param name
					if(parametersFound.contains(paramname)) continue;
					if(parameterConversion.containsKey(paramname)) {
						parametersFound.add(paramname);
						if(paramname.equals("ion mode:") || paramname.equals("IONMODE:")) {
							String ionmode = chargeTypes.get(value);
							lines.add(parameterConversion.get(paramname) + ionmode);
							currentEntry.ionmode = ionmode;
						}
						else if(paramname.equals("precursor type:") || paramname.equals("PRECURSORTYPE:")) {
							if(adductTypes.containsKey(value)) {
								String adductType = adductTypes.get(value);
								lines.add(parameterConversion.get(paramname) + adductType);
								currentEntry.adducttype = adductType;
							}
							else {
								lines.add(parameterConversion.get(paramname) + "0");
								currentEntry.adducttype = "0";
								currentEntry.ionmode = "True";
							}
						}
						else if(paramname.equals("Num Peaks:") || paramname.equals("Num peaks:")) {
							numberPeaks = Integer.parseInt(value);
							lines.add(parameterConversion.get(paramname) + value);
							currentEntry.numpeaks = value;
							if(lastInChI == null || lastInChI.equals("")) {
								try {
									lastInChI = MoleculeFunctions.getInChIFromSmiles(lastSmiles);
								} catch(Exception e) {
									entryFine = false;
									continue;
								}
							}
							if(numberPeaks == 0) {
								lines.add("");
								entryFine = checkInChIFormula(lastInChI, lastFormula, lastInChIKey);
								lastInChI = "";
								lastFormula = "";
								lastInChIKey = "";
								if(entryFine) {
									if(currentEntry.ionmode == null) {
										if(Constants.ADDUCT_CHARGES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(Integer.parseInt(currentEntry.adducttype))))
											currentEntry.ionmode = "True";
										else
											currentEntry.ionmode = "False";
									}
									writeEntryToFile(bwriter, currentEntry);
								}
								currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
							}
						}
						else {
							lines.add(parameterConversion.get(paramname) + value);
							if(paramname.equals("Name:")) currentEntry.sampleName = value;
							else if(paramname.equals("NAME:")) currentEntry.sampleName = value;
							else if(paramname.equals("ms level:")) currentEntry.mslevel = value;
							else if(paramname.equals("mass error:")) currentEntry.masserror = value;
						}
					}
				}
				else {
					//read peak
					String[] peaks = line.split("\\s+");
					lines.add(peaks[0] + " " + peaks[1]);
					currentEntry.addPeak(peaks[0], peaks[1]);
					numberPeaks--;
					if(numberPeaks == 0) {
						parametersFound = new java.util.ArrayList<String>();
						lines.add("");
						
						lastInChI = "";
						lastFormula = "";
						lastInChIKey = "";
						if(entryFine) {
							if(currentEntry.ionmode == null && currentEntry.adducttype != null) {
								if(Constants.ADDUCT_CHARGES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(Integer.parseInt(currentEntry.adducttype))))
									currentEntry.ionmode = "True";
								else
									currentEntry.ionmode = "False";
							}
							writeEntryToFile(bwriter, currentEntry);
						}
						currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
					}
				}
			}
			breader.close();
			bwriter.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void writeEntryToFile(BufferedWriter bwriter,  Entry entry) throws Exception {
		if(WITH_FINGERPRINT && entry.inchi == null) {
			if(entry.smiles != null) {
				try {
					entry.inchi = MoleculeFunctions.getInChIFromSmiles(entry.smiles);
				} catch(Exception e) {
					System.out.println("Error: " + entry.sampleName);
				}
			}
		}
		if(WITH_FINGERPRINT && entry.inchi != null) {
			IAtomContainer con = MoleculeFunctions.getAtomContainerFromInChI(entry.inchi);
			MoleculeFunctions.prepareAtomContainer(con, true);
			String fingerprint = MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(con));
			entry.fingerprint = fingerprint;
		}
		
		bwriter.write(entry.toString());
		bwriter.newLine();
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
		public String name;
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
			if(sampleName != null && sampleName.length() != 0) string += "# SampleName = " + sampleName + "\n";
			if(inchi != null && inchi.length() != 0) string += "# InChI = " + inchi + "\n";
			if(smiles != null && smiles.length() != 0) string += "# Smiles = " + smiles + "\n";
			if(inchikey != null && inchikey.length() != 0) string += "# InChIKey = " + inchikey + "\n";
			if(rt != null && rt.length() != 0) string += "# RetentionTime = " + rt + "\n";
			if(!EI_mode && ionmode != null && ionmode.length() != 0) string += "# IsPositiveIonMode = " + ionmode + "\n";
			if(!EI_mode && adducttype != null && adducttype.length() != 0) string += "# PrecursorIonMode = " + adducttype + "\n";
			if(EI_mode) {
				string += "# IsPositiveIonMode = True\n";
				string += "# PrecursorIonMode = 0\n";
			}
			if(mass != null && mass.length() != 0) string += "# NeutralPrecursorMass = " + mass + "\n";
			if(formula != null && formula.length() != 0) string += "# NeutralPrecursorMolecularFormula = " + formula + "\n";
			if(masserror != null && masserror.length() != 0) string += "# MassError = " + masserror + "\n";
			if(mslevel != null && mslevel.length() != 0) string += "# MSLevel = " + mslevel + "\n";
			if(ionizedmass != null && ionizedmass.length() != 0) string += "# IonizedPrecursorMass = " + ionizedmass + "\n";
			else if(mass != null && EI_mode && mass.length() != 0) {
				string += "# IonizedPrecursorMass = " + MathTools.round((Double.parseDouble(mass) - Constants.ELECTRON_MASS)) + "\n";
			}
			if(numpeaks != null && numpeaks.length() != 0) string += "# NumPeaks = " + numpeaks + "\n";
			if(fingerprint != null && fingerprint.length() != 0) string += "# MolecularFingerPrint = " + fingerprint + "\n";
			if(mzs != null && ints != null) {
				for(int i = 0; i < ints.size(); i++) {
					string += mzs.get(i) + " " + ints.get(i) + "\n";
				}
			}
			return string;
		}
	}
}
