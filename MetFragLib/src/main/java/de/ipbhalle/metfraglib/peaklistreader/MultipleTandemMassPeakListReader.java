package de.ipbhalle.metfraglib.peaklistreader;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedSimilarityTandemMassPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class MultipleTandemMassPeakListReader extends AbstractPeakListReader {
	
	public MultipleTandemMassPeakListReader(Settings settings) {
		super(settings);
	}
	
	public SpectralPeakListCollection readMultiple(java.io.InputStream stream) throws Exception {
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.InputStreamReader(stream));
		String line = "";
		SpectralPeakListCollection spectralPeakListCollection = 
				new SpectralPeakListCollection((Boolean)this.settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME), (Double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME), (Double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME));
		SortedSimilarityTandemMassPeakList peakList = null;
		int precursorIonTypeIndex = 0;
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0 && peakList != null) {
				if(peakList.isFullyInitialised()) {
					peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
					spectralPeakListCollection.addPeakList(peakList);
				}
				peakList = null;
			}
			else if(line.length() == 0) continue;
			else if(line.startsWith("#")) { //comment
				if(peakList == null) peakList = new SortedSimilarityTandemMassPeakList(0.0);
				line = line.replaceFirst("^#\\s*", "");
				String[] tmp = line.split("=");
				String paramname = tmp[0].trim();
				String valuename = tmp.length >= 1 ? tmp[1] : "";
				for(int i = 2; i < tmp.length; i++) {
					valuename += "=" + tmp[i];
				}
				valuename = valuename.trim();
				try {
					if(paramname.equals(VariableNames.PRECURSOR_ION_MASS_NAME)) peakList.setMeasuredPrecursorMass(Double.parseDouble(valuename));
					if(paramname.equals(VariableNames.PRECURSOR_ION_MODE_NAME)) precursorIonTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(Integer.parseInt(valuename));
					if(paramname.equals(VariableNames.INCHI_NAME)) peakList.setInchi(valuename);
					if(paramname.equals(VariableNames.INCHI_KEY_NAME)) peakList.setInchikey1(valuename.split("-")[0]);
					if(paramname.equals(VariableNames.IS_POSITIVE_ION_MODE_NAME)) peakList.setIsPositiveCharge(valuename.toLowerCase().equals("true"));
					if(paramname.equals(VariableNames.SAMPLE_NAME)) peakList.setSampleName(valuename);
					if(paramname.equals(VariableNames.MOLECULAR_FINGERPRINT_NAME)) peakList.setFingerprint(MoleculeFunctions.stringToFingerPrint(valuename));
				}
				catch(Exception e) {
					System.err.println("Error with spectrum " + peakList.getSampleName());
					e.printStackTrace();
					System.exit(1);
				}
			}
			else { //peak
				String[] tmp = line.split("\\s+");
				double currentMass = Double.parseDouble(tmp[0].trim());
				double currentIntensity = Double.parseDouble(tmp[1].trim());
				if(currentMass >= peakList.getMeasuredPrecursorMass() - 5.0 + Constants.ADDUCT_MASSES.get(precursorIonTypeIndex))
					continue;
				peakList.addElement(new TandemMassPeak(currentMass, currentIntensity));
			}
		}
		if(peakList != null) {
			peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
			spectralPeakListCollection.addPeakList(peakList);
		}
		breader.close();
		return spectralPeakListCollection;
	}
	
	public SpectralPeakListCollection readMultiple(String path) throws Exception {
		java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(path)));
		String line = "";
		SpectralPeakListCollection spectralPeakListCollection = 
				new SpectralPeakListCollection(
						(Boolean)this.settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME), 
						(Double)this.settings.get(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME), 
						(Double)this.settings.get(VariableNames.RELATIVE_MASS_DEVIATION_NAME),
						(Double)this.settings.get(VariableNames.MINIMUM_COSINE_SIMILARITY_LIMIT_NAME));
		SortedSimilarityTandemMassPeakList peakList = null;
		int precursorIonTypeIndex = 0;
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0 && peakList != null) {
				if(peakList.isFullyInitialised()) {
					peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
					spectralPeakListCollection.addPeakList(peakList);
				}
				peakList = null;
			}
			else if(line.length() == 0) continue;
			else if(line.startsWith("#")) { //comment
				if(peakList == null) peakList = new SortedSimilarityTandemMassPeakList(0.0);
				line = line.replaceFirst("^#\\s*", "");
				String[] tmp = line.split("=");
				String paramname = tmp[0].trim();
				String valuename = tmp.length >= 1 ? tmp[1] : "";
				for(int i = 2; i < tmp.length; i++) {
					valuename += "=" + tmp[i];
				}
				valuename = valuename.trim();
				try {
					if(paramname.equals(VariableNames.PRECURSOR_ION_MASS_NAME)) peakList.setMeasuredPrecursorMass(Double.parseDouble(valuename));
					if(paramname.equals(VariableNames.PRECURSOR_ION_MODE_NAME)) precursorIonTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(Integer.parseInt(valuename));
					if(paramname.equals(VariableNames.INCHI_NAME)) peakList.setInchi(valuename);
					if(paramname.equals(VariableNames.INCHI_KEY_NAME)) peakList.setInchikey1(valuename.split("-")[0]);
					if(paramname.equals(VariableNames.IS_POSITIVE_ION_MODE_NAME)) peakList.setIsPositiveCharge(valuename.toLowerCase().equals("true"));
					if(paramname.equals(VariableNames.SAMPLE_NAME)) peakList.setSampleName(valuename);
					if(paramname.equals(VariableNames.MOLECULAR_FINGERPRINT_NAME)) peakList.setFingerprint(MoleculeFunctions.stringToFingerPrint(valuename));
				}
				catch(Exception e) {
					System.err.println("Error with spectrum " + peakList.getSampleName());
					e.printStackTrace();
					System.exit(1);
				}
			}
			else { //peak
				String[] tmp = line.split("\\s+");
				double currentMass = Double.parseDouble(tmp[0].trim());
				double currentIntensity = Double.parseDouble(tmp[1].trim());
				if(currentMass >= peakList.getMeasuredPrecursorMass() - 5.0 + Constants.ADDUCT_MASSES.get(precursorIonTypeIndex))
					continue;
				peakList.addElement(new TandemMassPeak(currentMass, currentIntensity));
			}
		}
		if(peakList != null) {
			peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
			spectralPeakListCollection.addPeakList(peakList);
		}
		breader.close();
		return spectralPeakListCollection;
	}
	
	public DefaultPeakList read() {
		SortedTandemMassPeakList peakList = null;
		String filename = (String)this.settings.get(VariableNames.PEAK_LIST_PATH_NAME);
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			peakList = new SortedTandemMassPeakList((Double)this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#") || line.length() == 0) continue;
				String[] tmp = line.split("\\s+");
				peakList.addElement(new TandemMassPeak(Double.parseDouble(tmp[0].trim()), Double.parseDouble(tmp[1].trim())));
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < peakList.getNumberElements(); i++)
			peakList.getElement(i).setID(i);
		peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
		return peakList;
	}

}
