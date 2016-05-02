package de.ipbhalle.metfraglib.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;

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
	
	public static void main(String[] args) {
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
		try {
			BufferedReader breader = new BufferedReader(new FileReader(mspfile));
			String line = "";
			int numberPeaks = 0;
			java.util.Vector<String> parametersFound = new java.util.Vector<String>();
			String lastInChI = "";
			String lastInChIKey = "";
			String lastFormula = "";
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
					if(paramname.equals("InChI:")) lastInChI = value;
					if(paramname.equals("formula:")) lastFormula = value;
					if(paramname.equals("InChIKey:")) lastInChIKey = value;
					//start check param name
					if(parametersFound.contains(paramname)) continue;
					if(parameterConversion.containsKey(paramname)) {
						parametersFound.add(paramname);
						if(paramname.equals("ion mode:")) {
							lines.add(parameterConversion.get(paramname) + chargeTypes.get(value));
						}
						else if(paramname.equals("precursor type:")) {
							if(adductTypes.containsKey(value))
								lines.add(parameterConversion.get(paramname) + adductTypes.get(value));
							else {
								lines.add(parameterConversion.get(paramname) + "0");
							}
						}
						else if(paramname.equals("Num Peaks:")) {
							numberPeaks = Integer.parseInt(value);
							lines.add(parameterConversion.get(paramname) + value);
							if(numberPeaks == 0) {
								lines.add("");
								checkInChIFormula(lastInChI, lastFormula, lastInChIKey);
								lastInChI = "";
								lastFormula = "";
								lastInChIKey = "";
							}
						}
						else {
							lines.add(parameterConversion.get(paramname) + value);
						}
					}
				}
				else {
					//read peak
					String[] peaks = line.split("\\s+");
					lines.add(peaks[0] + " " + peaks[1]);
					numberPeaks--;
					if(numberPeaks == 0) {
						parametersFound = new java.util.Vector<String>();
						lines.add("");
						checkInChIFormula(lastInChI, lastFormula, lastInChIKey);
						lastInChI = "";
						lastFormula = "";
						lastInChIKey = "";
					}
				}
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
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
	}
	
	public static void checkInChIFormula(String inchi, String formula, String inchikey) {
		String inchiFormula = inchi.split("/")[1];
		if(!inchiFormula.equals(formula))
			System.out.println("Mismatch " + inchi + " " + formula + " " + inchikey);
	}
	
}
