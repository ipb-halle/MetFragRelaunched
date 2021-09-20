package de.ipbhalle.metfrag.r;

import static org.junit.Assert.assertEquals;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class FingerprintR_Test {

	private File smilesInput;
	private File smilesOutTarget;
	
	private final List<String> testSmiles = Arrays.asList(
		"CN(C(=O)NC1=CC=C(C=C1)Cl)OC",
		"C1CCCCC1"
	);
	
	private List<String> smilesFingerprints = Arrays.asList(
		"000000000000100000000001000000000000100000000000000000000000000000000011000010000000001000001110000001100110010100001000010101000000111000000101000111100101111111111000000000000000000000000000", 
		"000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000100000100000000011000000000000000001001000010000000101000000000000000000000000000"
	);
	
	
	@Before
	public void initFiles() throws IOException {
		this.smilesInput = File.createTempFile("smiles-input-", ".csv");
		this.smilesOutTarget = File.createTempFile("smiles-fp-output-", ".csv");
		this.smilesInput.deleteOnExit();
		this.smilesOutTarget.deleteOnExit();
		this.writeSmileToFile();
	}
	
	@Test
	public void testSingleFingerprintCalculation() {
		final String fp1 = FingerPrintR.calculateFingerprintFromSmiles(this.testSmiles.get(0));
		final String fp2 = FingerPrintR.calculateFingerprintFromSmiles(this.testSmiles.get(1));
		assertEquals(fp1, this.smilesFingerprints.get(0));
		assertEquals(fp2, this.smilesFingerprints.get(1));
	}
	
	@Test
	public void testWriteToCSV() {
		FingerPrintR.calculateFingerprintFromSmilesAndWriteToCSV(this.smilesInput.getAbsolutePath(), this.smilesOutTarget.getAbsolutePath());
	}
	
	private void writeSmileToFile() {
		try (BufferedWriter bwriter = new BufferedWriter(new FileWriter(this.smilesInput))) {
			for(String smiles : this.testSmiles) { 
				bwriter.write(smiles);
				bwriter.newLine();
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write file: " + e.getMessage());
		}
	}
}
