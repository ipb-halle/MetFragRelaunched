package de.ipbhalle.metfraglib.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;

public class ConvertMSPtoMetFragRecord {

	public static Hashtable<String, String> parameterConversion;
	public static Hashtable<String, String> adductTypes;
	public static Hashtable<String, String> chargeTypes;
	
	static {
		parameterConversion = new Hashtable<String, String>();
		parameterConversion.put("Name:", "# SampleName = ");
		parameterConversion.put("InChI:", "# InChI = ");
		parameterConversion.put("InChIKey:", "# InChIKey = ");
		parameterConversion.put("ms level:", "# MSLevel = ");
		parameterConversion.put("precursor m/z:", "# IonizedPrecursorMass = ");
		parameterConversion.put("retention time:", "# RetentionTime = ");
		parameterConversion.put("exact mass:", "# NeutralPrecursorMass = ");
		parameterConversion.put("ion mode:", "# IsPositiveIonMode = ");
		parameterConversion.put("mass error:", "# MassError = ");
		parameterConversion.put("precursor type:", "# PrecursorIonMode = ");
		parameterConversion.put("Num Peaks:", "# NumPeaks = ");
		parameterConversion.put("formula:", "# NeutralPrecursorMolecularFormula = ");
		
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
	
	public static void main(String[] args) throws AtomTypeNotKnownFromInputListException {
		if(args.length != 2) {
			System.err.println("MSP input file needed. MetFrag record output file needed.");
			System.exit(1);
		}
		String mspfilename = args[0];
		String metfragfilename = args[1];
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
		java.util.Vector<String> lines = new java.util.Vector<String>();
		java.util.Vector<Entry> entries = new java.util.Vector<Entry>();
		try {
			BufferedReader breader = new BufferedReader(new FileReader(mspfile));
			String line = "";
			int numberPeaks = 0;
			java.util.Vector<String> parametersFound = new java.util.Vector<String>();
			String lastInChI = "";
			String lastInChIKey = "";
			String lastFormula = "";
			Entry currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
			while((line = breader.readLine()) != null) {
				//get param name and value
				line = line.trim();
				if(line.length() == 0) continue;
				String[] tmp = line.split("\\s+");
				String paramname = "";
				String value = "";
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
					if(paramname.equals("formula:")) {
						lastFormula = value;
						currentEntry.formula = lastFormula;
					}
					if(paramname.equals("InChIKey:")) {
						lastInChIKey = value;
						currentEntry.inchikey = lastInChIKey;
					}
					//start check param name
					if(parametersFound.contains(paramname)) continue;
					if(parameterConversion.containsKey(paramname)) {
						parametersFound.add(paramname);
						if(paramname.equals("ion mode:")) {
							String ionmode = chargeTypes.get(value);
							lines.add(parameterConversion.get(paramname) + ionmode);
							currentEntry.ionmode = ionmode;
						}
						else if(paramname.equals("precursor type:")) {
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
						else if(paramname.equals("Num Peaks:")) {
							numberPeaks = Integer.parseInt(value);
							lines.add(parameterConversion.get(paramname) + value);
							currentEntry.numpeaks = value;
							if(numberPeaks == 0) {
								lines.add("");
								boolean entryFine = checkInChIFormula(lastInChI, lastFormula, lastInChIKey);
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
									entries.add(currentEntry);
								}
								currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
							}
						}
						else {
							lines.add(parameterConversion.get(paramname) + value);
							if(paramname.equals("Name:")) currentEntry.sampleName = value;
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
						parametersFound = new java.util.Vector<String>();
						lines.add("");
						boolean entryFine = checkInChIFormula(lastInChI, lastFormula, lastInChIKey);
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
							entries.add(currentEntry);
						}
						currentEntry = new ConvertMSPtoMetFragRecord().new Entry();
					}
				}
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		/*
		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(metfragfile));
			for(int i = 0; i < lines.size(); i++) {
				bwriter.write(lines.get(i));
				bwriter.newLine();
			}
			
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		try {
			BufferedWriter bwriter = new BufferedWriter(new FileWriter(metfragfile));
			for(int i = 0; i < entries.size(); i++) {
				bwriter.write(entries.get(i).toString());
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
		public String ionizedmass;
		public String numpeaks;
		public String ionmode;
		public String masserror;
		public String mslevel;
		public java.util.Vector<String> mzs;
		public java.util.Vector<String> ints;
		
		public void addPeak(String mz, String intensity) {
			if(mzs == null) mzs = new java.util.Vector<String>();
			if(ints == null) ints = new java.util.Vector<String>();
			mzs.add(mz);
			ints.add(intensity);
		}
		
		public String toString() {
			String string = "# SampleName = " + sampleName + "\n";
			if(inchi != null) string += "# InChI = " + inchi + "\n";
			if(inchikey != null) string += "# InChIKey = " + inchikey + "\n";
			if(rt != null) string += "# RetentionTime = " + rt + "\n";
			if(ionmode != null) string += "# IsPositiveIonMode = " + ionmode + "\n";
			if(adducttype != null) string += "# PrecursorIonMode = " + adducttype + "\n";
			if(mass != null) string += "# NeutralPrecursorMass = " + mass + "\n";
			if(masserror != null) string += "# MassError = " + masserror + "\n";
			if(mslevel != null) string += "# MSLevel = " + mslevel + "\n";
			if(ionizedmass != null) string += "# IonizedPrecursorMass = " + ionizedmass + "\n";
			if(numpeaks != null) string += "# NumPeaks = " + numpeaks + "\n";
			if(mzs != null && ints != null) {
				for(int i = 0; i < ints.size(); i++) {
					string += mzs.get(i) + " " + ints.get(i) + "\n";
				}
			}
			return string;
		}
	}
}
