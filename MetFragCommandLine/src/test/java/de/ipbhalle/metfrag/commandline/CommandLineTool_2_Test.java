package de.ipbhalle.metfrag.commandline;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import de.ipbhalle.metfrag.commandline.CommandLineTool;

public class CommandLineTool_2_Test {

	private String finalParameterFilePath;
	
	@Before
	public void setUp() {
		java.io.File parameterFilePath = new java.io.File(ClassLoader.getSystemResource("parameter_file_example_2.txt").getFile());
		String peakListFilePath = ClassLoader.getSystemResource("peaklist_file_example_2.txt").getFile();
		String inchiFilePath = ClassLoader.getSystemResource("inchi_file_example_2.txt").getFile();
		String bondEnergyFilePath = ClassLoader.getSystemResource("bondenergy_file_example_2.txt").getFile();
		/*
		 * read file parameters and add additional ones into a new temporary parameter file
		 * - peaklist file location
		 * - temp folder as result storage
		 */
		String tempDir = System.getProperty("java.io.tmpdir");
		java.io.File tempFile = null;
		try {
			tempFile = java.io.File.createTempFile("temp-file-name", ".tmp");
			tempFile.deleteOnExit(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(tempFile == null) {
			throw new NullPointerException();
		}
		java.io.BufferedWriter bwriter;
		try {
			bwriter = new java.io.BufferedWriter(new java.io.FileWriter(tempFile));
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(parameterFilePath));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				bwriter.write(line);
				bwriter.newLine();
			}
			breader.close();
			/*
			 * add results store path
			 */
			bwriter.write("ResultsPath = " + tempDir);
			bwriter.newLine();
			/*
			 * add peaklist path
			 */
			bwriter.write("PeakListPath = " + peakListFilePath);
			bwriter.newLine();
			/*
			 * add inchi list path
			 */
			bwriter.write("LocalDatabasePath = " + inchiFilePath);
			bwriter.newLine();
			/*
			 * add bond energies list path
			 */
			bwriter.write("BondEnergyFilePath = " + bondEnergyFilePath);
			bwriter.newLine();
			bwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.finalParameterFilePath = tempFile.getAbsolutePath();
	}

	@Test
	public void test() {
		String[] arguments = {this.finalParameterFilePath};
		/*
		 * run the commandline tool
		 */
		CommandLineTool.main(arguments);
	}

}
