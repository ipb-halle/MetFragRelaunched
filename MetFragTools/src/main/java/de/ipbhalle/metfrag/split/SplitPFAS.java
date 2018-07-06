package de.ipbhalle.metfrag.split;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.parameter.Constants;

public class SplitPFAS {

	public static String[] extendedChainSmarts = {""}; 
	public static java.util.Hashtable<String, String> argsHash;
	
	public static void main(String[] args) throws AtomTypeNotKnownFromInputListException, Exception {
		if(!getArgs(args)) {
			System.out.println("Error: Could not read arguments properly.");
			System.out.println("Usage:");
			System.out.println("\tjava -cp NAME.jar de.ipbhalle.metfrag.split.SplitPFAS smiles='SMILES' [smartspath='FILEPATH']");
			System.out.println("");
			System.out.println("\tsmiles    \t- SMILES of input PFAS");
			System.out.println("");
			System.out.println("\tsmartspath\t- file containing SMARTS (one per line)");
			System.out.println("\t          \t- for empty SMARTS just include empty line");
			System.out.println("\t          \t- order marks priotity");
			System.out.println("\t          \t- if not given, default \"\" is used");
			System.out.println("");
			System.exit(1);
		}
		if(argsHash.containsKey("smartspath")) 
			if(!readSmarts(argsHash.get("smartspath"))) {
				System.err.println("Error: Could't read " + argsHash.get("smartspath"));
				System.exit(2);
			}
		if(extendedChainSmarts.length == 0) {
			System.err.println("Error: No values found in " + argsHash.get("smartspath"));
			System.exit(3);
		}
		
		String smiles = argsHash.get("smiles");
		PFAS pfas = new PFAS(smiles);
		
		int[] endChainCarbonIndexes = pfas.findEndChainCarbons();

		List<List<Integer>> bondIndexesToSplitForAllSmarts = new ArrayList<List<Integer>>();
		for(int ii = 0; ii < extendedChainSmarts.length; ii++) {
			bondIndexesToSplitForAllSmarts.add(pfas.getBondIndexAfterEndChain(endChainCarbonIndexes, extendedChainSmarts[ii]));
		}
		int numberFoundToSplit = 0;
		for(int ii = 0; ii < bondIndexesToSplitForAllSmarts.size(); ii++) {
			// favor smarts unequal "" in case of multiple matches
			if(bondIndexesToSplitForAllSmarts.get(ii).size() == 0) continue;
			
			if(numberFoundToSplit == 0) {
				File file = File.createTempFile("pref", ".png", new File(Constants.OS_TEMP_DIR));
				pfas.saveHighlightedBondsImage(bondIndexesToSplitForAllSmarts.get(ii), file.getAbsolutePath());
				System.out.println("stored bond(s) to break highlighted in " + file.getAbsolutePath());
				
				String fragmentSmiles = pfas.getSplitResult(bondIndexesToSplitForAllSmarts.get(ii), endChainCarbonIndexes);
				
				System.out.println(fragmentSmiles);
				
				if(!fragmentSmiles.equals("")) numberFoundToSplit++;
			}
		}
	}	
	
	public static boolean readSmarts(String filename) throws IOException {
		File file = new File(filename);
		if(!file.canRead()) {
			System.err.println("Error: Can't read " + filename);
			return false;
		}
		
		List<String> smartsValues = new ArrayList<String>();
		BufferedReader breader = new BufferedReader(new FileReader(file));
		
		String line = "";
		while((line = breader.readLine()) != null) { 
			line = line.trim();
			if(!smartsValues.contains(line)) smartsValues.add(line);
		}
		breader.close();
		extendedChainSmarts = new String[smartsValues.size()];
		for(int i = 0; i < extendedChainSmarts.length; i++) {
			extendedChainSmarts[i] = smartsValues.get(i);
		}
		if(extendedChainSmarts.length != 0) System.out.println(extendedChainSmarts.length + " SMARTS value(s) found");
		
		return true;
	}
	
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("smiles") && !tmp[0].equals("smartspath")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], combineStringArray(tmp));
		}
		
		if (!argsHash.containsKey("smiles")) {
			System.err.println("Error: No smiles defined");
			return false;
		}
		return true;
	}
	
	public static String combineStringArray(String[] array) {
		StringBuilder stringBuilder = new StringBuilder();
		if(array.length > 1) stringBuilder.append(array[1]);
		for(int i = 2; i < array.length; i++) {
			stringBuilder.append("=");
			stringBuilder.append(array[i]);
		}
		return stringBuilder.toString();
	}
}
