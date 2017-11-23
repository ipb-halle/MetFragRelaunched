package de.ipbhalle.metfrag.spectral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.collection.SpectralPeakListCollection;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.MultipleTandemMassPeakListReader;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class MatchSpectralLibrary {

	public static ArrayList<DefaultPeakList> querySpectra;
	public static ArrayList<String> querySpectraNames;
	
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String querySpectrumFile = args[0];
		String spectralDatabaseFile = args[1];
		String charge = args[2].toLowerCase();
		Double mzabs = Double.parseDouble(args[3]);
		Double mzppm = Double.parseDouble(args[4]);
		Double threshold = Double.parseDouble(args[5]);
		String resultFile = args[6];
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.IS_POSITIVE_ION_MODE_NAME, charge.startsWith("neg") ? false : true);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzabs);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, mzppm);
		
		MultipleTandemMassPeakListReader multiplePeakListReader = new MultipleTandemMassPeakListReader(settings);
		SpectralPeakListCollection spectralPeakLists = null;
		try {
			System.out.println("reading spectral database");
			spectralPeakLists = multiplePeakListReader.readMultiple(spectralDatabaseFile);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		try {
			System.out.println("reading query spectra");
			readQuerySpectra(querySpectrumFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(2);
		}
		System.out.println("read " + spectralPeakLists.getSize() + " library spectra");
		
		ArrayList<String> data = new ArrayList<String>();
		
		for(int i = 0; i < querySpectra.size(); i++) {
			if(querySpectra.get(i).getNumberElements() <= 4) continue;
			for(int j = 0; j < spectralPeakLists.getSize(); j++) {
				double sim = spectralPeakLists.getPeakList(j).cosineSimilarity((SortedTandemMassPeakList)querySpectra.get(i), mzppm, mzabs);
				if(sim >= threshold) {
					data.add(sim + " " + querySpectraNames.get(i) + " " + spectralPeakLists.getPeakList(j).getInchikey1() + " \"" + spectralPeakLists.getPeakList(j).getSampleName().replaceFirst("Name:\\s+", "") + "\" " + spectralPeakLists.getPeakList(j).getInchi());
				}
			}
		}
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(resultFile)));
		bwriter.write("Similarity Query InChIKey1 SampleName InChI");
		bwriter.newLine();
		for(int i = 0; i < data.size(); i++) {
			bwriter.write(data.get(i));
			bwriter.newLine();
		}
		bwriter.close();
	}

	public static void readQuerySpectra(String querySpectrumFile) throws Exception {
		querySpectra = new ArrayList<DefaultPeakList>();
		querySpectraNames = new ArrayList<String>();
		BufferedReader breader = new BufferedReader(new FileReader(new File(querySpectrumFile)));
		String line = "";
		String peakString = "";
		String lastSpectrumName = "";
		while((line = breader.readLine()) != null) {
			line = line.trim();
			if(line.length() == 0) continue;
			if(line.startsWith("Name")) {
				if(peakString.length() != 0) {
					try {
						querySpectra.add(StringTandemMassPeakListReader.readSingle(peakString, 1000));
					}
					catch(Exception e) {
						System.out.println(lastSpectrumName);
						System.out.println(peakString);
						System.exit(3);
					}
					querySpectraNames.add(lastSpectrumName);
					peakString = "";
				}
				lastSpectrumName = line.replaceAll("^Name\\s*", "");
			}
			else {
				peakString += line + "\n";
			}
		}
		if(peakString.length() != 0) {
			querySpectra.add(StringTandemMassPeakListReader.readSingle(peakString, 1000));
			querySpectraNames.add(lastSpectrumName);
		}
		breader.close();
	}
	
}
