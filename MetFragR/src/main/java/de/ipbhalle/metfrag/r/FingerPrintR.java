package de.ipbhalle.metfrag.r;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.fingerprint.TanimotoSimilarity;

public class FingerPrintR {

	public static void calculateFingerprintFromSmilesAndWriteToCSV(String pathToSmilesFile, String pathToFingerprintCSV) {
		final Set<String> smiles = readSmilesFromFile(new File(pathToSmilesFile));
		final Map<String, String> smilesToFingerprint = smiles.stream().collect(Collectors.toMap(smi -> smi, smi -> calculateFingerprintFromSmiles(smi)));
		writeSmilesFinerprintMapToFile(smilesToFingerprint, new File(pathToFingerprintCSV));
	}
	
	public static String calculateFingerprintFromSmiles(String smiles) {
		final IAtomContainer con = getAtomContainerFromSmiles(smiles);
		MoleculeFunctions.prepareAtomContainer(con, true);
		final IBitFingerprint fingerprint = TanimotoSimilarity.calculateFingerPrint(con);
		return toBitString(fingerprint);
	}
	
	private static void writeSmilesFinerprintMapToFile(Map<String, String> smilesToFingerprint, File target) {
		try (BufferedWriter bwriter = new BufferedWriter(new FileWriter(target))) {
			bwriter.write("SMILES,FP");
			bwriter.newLine();
			final Iterator<Entry<String,String>> it = smilesToFingerprint.entrySet().iterator();
			while(it.hasNext()) {
				final Entry<String, String> entry = it.next(); 
				bwriter.write(entry.getKey() + "," + entry.getValue());
				bwriter.newLine();
			};
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not write to " + target.getAbsolutePath() + ": " + e.getMessage());
		}
	}
	
	private static Set<String> readSmilesFromFile(File pathToSmilesFile) {
		try (BufferedReader breader = new BufferedReader(new FileReader(pathToSmilesFile))) {
			final Set<String> smiles = new HashSet<>();
			String line = null;
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#")) continue;
				if(line.length() == 0) continue;
				smiles.add(line);
			}
			return smiles;
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("File " + pathToSmilesFile.getAbsolutePath() + " not found");
		} catch (IOException e) {
			throw new IllegalArgumentException("Could not read " + pathToSmilesFile.getAbsolutePath() + " properly: " + e.getMessage());
		}
	}
	
	private static IAtomContainer getAtomContainerFromSmiles(String smiles) {
		try {
			return MoleculeFunctions.getAtomContainerFromSMILES(smiles);
		} catch (Exception e) {
			throw new IllegalArgumentException("Could not parse SMILES: " + smiles + ": " + e.getMessage());
		}
	}
	
	private static String toBitString(IBitFingerprint fp) {
		final StringBuilder bitStringBuilder = new StringBuilder();
		final BitSet bitSet = fp.asBitSet();
		for(int i = 0; i < bitSet.size(); i++) {
			boolean bit = bitSet.get(i);
			if(bit) bitStringBuilder.append("1");
			else bitStringBuilder.append("0");
		}
		return bitStringBuilder.toString();
	}
}
