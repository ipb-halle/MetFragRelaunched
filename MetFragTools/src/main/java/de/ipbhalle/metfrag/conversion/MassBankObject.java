package de.ipbhalle.metfrag.conversion;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MassBankObject {

	private String accession;
	private String name;
	
	private String instrument;
	private String instrumentType;
	private String ionMode;
	
	// database ids
	private String pubchemID;
	private String chemspiderID;
	private String keggID;
	private String chebiID;
	private String lipidmapsID;
	
	// chemical identifiers
	private String inchikey;
	private String inchi;
	private String smiles;
	private String formula;
	
	// precursor information
	private double precursorMz;
	private String precursorType;
	private double exactMass;
	
	// mz values
	private double[][] mz_int_values;
	private Integer numberPeaks;
	
	private boolean properlyInitialized = false;
	
	public MassBankObject(String filename) {
		this.readValuesFromFile(filename);
	}
	
	private void readValuesFromFile(String filename) {
		java.io.BufferedReader breader;
		String line = "";
		try {
			breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			while((line = breader.readLine()) != null) {
				if(line.startsWith("ACCESSION:")) this.accession = connectValuesSpace(line, 1);
				else if(line.startsWith("CH$NAME:") && nameFound()) this.name = connectValuesSpace(line, 1);
				// chemical identifiers
				else if(line.startsWith("CH$FORMULA:")) this.formula = connectValuesSpace(line, 1);
				else if(line.startsWith("CH$SMILES:")) this.smiles = connectValuesSpace(line, 1);
				else if(line.startsWith("CH$IUPAC: InChI=")) this.inchi = connectValuesSpace(line, 1);
				else if(line.startsWith("CH$LINK: INCHIKEY")) this.inchikey = connectValuesSpace(line, 2);
				// precursor information
				else if(line.startsWith("CH$EXACT_MASS:")) this.exactMass = Double.parseDouble(connectValuesSpace(line, 1));
				else if(line.startsWith("MS$FOCUSED_ION: PRECURSOR_M/Z")) this.precursorMz = Double.parseDouble(connectValuesSpace(line, 2));
				else if(line.startsWith("MS$FOCUSED_ION: PRECURSOR_TYPE")) this.precursorType = connectValuesSpace(line, 2);
				// database ids
				else if(line.startsWith("CH$LINK: CHEBI")) this.chebiID = connectValuesSpace(line, 2);
				else if(line.startsWith("CH$LINK: KEGG")) this.keggID = connectValuesSpace(line, 2);
				else if(line.startsWith("CH$LINK: LIPIDMAPS")) this.lipidmapsID = connectValuesSpace(line, 2);
				else if(line.startsWith("CH$LINK: PUBCHEM")) this.pubchemID = connectValuesSpace(line, 2).replaceAll("^CID:", "");
				else if(line.startsWith("CH$LINK: CHEMSPIDER")) this.chemspiderID = connectValuesSpace(line, 2);
				// instrument
				else if(line.startsWith("AC$INSTRUMENT:")) this.instrument = connectValuesSpace(line, 2);
				else if(line.startsWith("AC$INSTRUMENT_TYPE:")) this.instrumentType = connectValuesSpace(line, 1);
				else if(line.startsWith("AC$MASS_SPECTROMETRY: ION_MODE")) this.ionMode = connectValuesSpace(line, 2);
				else if(line.startsWith("PK$NUM_PEAK:")) this.numberPeaks = Integer.parseInt(connectValuesSpace(line, 1));
				else if(line.startsWith("PK$PEAK:")) {
					if(!peakNumFound()) {
						breader.close();
						throw new Exception("No PK$NUM_PEAK found before PK$PEAK");
					}
					this.mz_int_values = new double[this.numberPeaks][2];
					for(int k = 0; k < this.numberPeaks; k++) {
						line = breader.readLine();
						if(line == null) {
							breader.close();
							throw new Exception("No valid peak found at " + line + " (" + (k+1) + "/" + this.numberPeaks + ")");
						}
						this.mz_int_values[k] = readPeak(line);
					}
				}
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("Could not read MassBank record.");
			System.err.println("Error at: " + line);
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Could not read MassBank record.");
			System.err.println("Error at: " + line);
			return;
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not read MassBank record.");
			System.err.println("Error at: " + line);
			return;
		} 
		this.properlyInitialized = true;
	}
	
	private double[] readPeak(String line) {
		line = line.trim().replaceAll("^\\s+", "").replaceAll("\\t+", "");
		String[] tmp = line.split("\\s+");
		return new double[] {Double.parseDouble(tmp[0]), Double.parseDouble(tmp[1])};
	}
	
	private String connectValuesSpace(String string, int startingFrom) {
		String[] tmp = string.split("\\s+");
		String returnString = tmp[startingFrom];
		for(int i = (startingFrom + 1); i < tmp.length; i++) {
			returnString += " " + tmp[i].trim();
		}
		return returnString;
	}
	
	public String getAccession() {
		return accession;
	}

	public String getName() {
		return name;
	}

	public String getInstrument() {
		return instrument;
	}

	public String getInstrumentType() {
		return instrumentType;
	}

	public String getIonMode() {
		return ionMode;
	}

	public String getPubchemID() {
		return pubchemID;
	}

	public String getChemspiderID() {
		return chemspiderID;
	}

	public String getKeggID() {
		return keggID;
	}

	public String getChebiID() {
		return chebiID;
	}

	public String getLipidmapsID() {
		return lipidmapsID;
	}

	public String getInchikey() {
		return inchikey;
	}

	public String getInchi() {
		return inchi;
	}

	public String getSmiles() {
		return smiles;
	}

	public String getFormula() {
		return formula;
	}

	public double getPrecursorMz() {
		return precursorMz;
	}

	public String getPrecursorType() {
		return precursorType;
	}

	public double getExactMass() {
		return exactMass;
	}

	public double[][] getMzIntValues() {
		return mz_int_values;
	}

	public String getMzIntValuesToString() {
		String peakString = mz_int_values[0][0] + "_" + mz_int_values[0][1];
		for(int i = 1; i < this.numberPeaks; i++) {
			peakString = ";" + mz_int_values[i][0] + "_" + mz_int_values[i][1];
		}
		return peakString;
	}

	public Integer getNumberPeaks() {
		return numberPeaks;
	}

	public boolean isProperlyInitialized() {
		return properlyInitialized;
	}

	private boolean nameFound() {
		return this.name != null;
	}
	
	private boolean peakNumFound() {
		return this.numberPeaks != null;
	}
	
	public static void main(String[] args) {
		MassBankObject mo = new MassBankObject("/scratch/cruttkie/fingerprint_training/casmi/parameters/additional_mb_records/UF418404.txt");
		System.out.println(mo.accession);
	}
	
}
