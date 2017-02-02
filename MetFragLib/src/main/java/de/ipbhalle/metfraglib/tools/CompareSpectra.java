package de.ipbhalle.metfraglib.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;

/**
 * calculate cosine similarity of two spectra given in a file, respectively
 * 
 * input:
 * 
 * spectrum file 1
 * spectrum file 2
 * mzabs
 * mzppm
 * 
 * @author chrisr
 *
 */
public class CompareSpectra {

	public static void main(String[] args) {
		String spectrumFile1 = args[0];
		String spectrumFile2 = args[1];
		
		double mzabs = Double.parseDouble(args[2]);
		double mzppm = Double.parseDouble(args[3]);

		double maximumMass = Integer.MAX_VALUE; 
		if(args.length > 4) maximumMass = Double.parseDouble(args[4]);
		
		DefaultPeakList peakList1 = null;
		DefaultPeakList peakList2 = null;
		try {
			peakList1 = StringTandemMassPeakListReader.readSingle(getPeakString(spectrumFile1), 1000, 1.0, maximumMass);
			peakList2 = StringTandemMassPeakListReader.readSingle(getPeakString(spectrumFile2), 1000, 1.0, maximumMass);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double sim = ((SortedTandemMassPeakList)peakList1).cosineSimilarity((SortedTandemMassPeakList)peakList2, mzppm, mzabs);
		
		System.out.println(sim);
	}

	public static String getPeakString(String filename) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
		String line = "";
		String peakString = "";
		while((line = breader.readLine()) != null) {
			peakString += line + "\n";
		}
		breader.close();
		return peakString;
	}
	
}
