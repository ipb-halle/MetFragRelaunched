package de.ipbhalle.metfrag.spectral;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RetrieveSpectraFromMGF {

	public static String filePathName = 
			"/home/chrisr/Dokumente/PhD/MetFrag/ipb/nadine/new_data/kartoffel/11178-Compounds-20eV.mgf";
	public static String id = "11178_c";
	public static String outputFolder = "/home/chrisr/Dokumente/PhD/MetFrag/ipb/nadine/new_data/extractedData/potato_tuber/11178_c";
	
	public static void main(String[] args) {
		
		if(args != null && args.length == 2) {
			filePathName = args[0];
			id = args[1];
		}
		
		File file = new File(filePathName);
		
		if(!file.exists()) {
			System.err.println("Error: File does not exist.");
			System.exit(1);
		}

		if(!file.canRead()) {
			System.err.println("Error: Cannot read file.");
			System.exit(1);
		}
		
		if(!file.isFile()) {
			System.err.println("Error: File is not valid.");
			System.exit(1);
		}
		
		try {
			BufferedReader breader = new BufferedReader(new FileReader(file));
			
			String line = "";
			boolean readPeaks = false;
			double currentPrecursorMass = 0.0;
			int currentSpectrumNumber = 0;
			
			BufferedWriter bwriter = null;
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 0) continue;
				if(line.startsWith("#")) continue;
				if(line.startsWith("END IONS")) {
					readPeaks = false;
					currentPrecursorMass = 0.0;
					if(bwriter != null) {
						bwriter.close();
					}
				}
				if(readPeaks && currentPrecursorMass != 0.0) {
					String[] tmp = line.split("\\s+");
					double mz = Double.parseDouble(tmp[0]);
					double intensity = Double.parseDouble(tmp[1]);
					bwriter.write(mz + " " + intensity);
					bwriter.newLine();
				}
				else if(readPeaks) {
					if(line.startsWith("PEPMASS")) {
						currentPrecursorMass = Double.parseDouble(line.split("=")[1].split("\\s+")[0]);
						bwriter.write("# NeutralPrecursorMass = " + currentPrecursorMass);
						bwriter.newLine();
					}
				}
				else 
				{
					if(line.startsWith("BEGIN IONS")) {
						readPeaks = true;
						currentSpectrumNumber++;
						String specNum = currentSpectrumNumber + "";
						if(currentSpectrumNumber < 10) specNum = "0" + specNum;
						bwriter = new BufferedWriter(new FileWriter(new File(outputFolder + "/spec_" + id + "_" + specNum + ".txt")));
					}
				}
				
			}
			
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
}
