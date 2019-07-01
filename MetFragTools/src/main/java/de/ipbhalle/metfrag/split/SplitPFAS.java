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
	public static String endChainCarbonSmarts = "FC(F)([C,F])[!$(C(F)(F));!$(F)]";
	public static String debugFolder = null;
	
	public static void main(String[] args) throws AtomTypeNotKnownFromInputListException, Exception {
		if(!getArgs(args)) {
			System.out.println("Error: Could not read arguments properly.");
			System.out.println("Usage:");
			System.out.println("\tjava -cp NAME.jar de.ipbhalle.metfrag.split.SplitPFAS smiles='SMILES' "
					+ "[smartspath='FILEPATH'] [image='yes or no'] [eccs='SMARTS'] [df='FOLDERPATH']");
			System.out.println("");
			System.out.println("\tsmiles    \t- SMILES of input PFAS");
			System.out.println("");
			System.out.println("\tsmartspath\t- file containing SMARTS (one per line)");
			System.out.println("\t          \t- for empty SMARTS just include empty line");
			System.out.println("\t          \t- order marks priotity");
			System.out.println("\t          \t- if not given, default \"\" is used");
			System.out.println("");
			System.out.println("\timage     \t- create image of bonds broken (default 'no')");
			System.out.println("");
			System.out.println("\teccs	\t- SMARTS to find carbon of the end of the PFAS chain");
			System.out.println("		\t- (eccs: end chain carbon SMARTS)");
			System.out.println("		\t- (default: FC(F)([C,F])[!$(C(F)(F));!$(F)])");
			System.out.println("");
			System.out.println("\tdf     	\t- debug folder where structure images are written");
			System.out.println("     		\t- used for debugging (doesn't effect 'image' parameter)");
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
		PFAS pfas = null; 
		try {
			pfas = new PFAS(smiles);
		} catch(Exception e) {
			System.err.println("Error: Problem occured. Check input.");
			System.exit(5);
		}
		boolean createImage = false;
		if(argsHash.containsKey("image")) {
			String image = (String)argsHash.get("image");
			if(image.toLowerCase().equals("yes")) createImage = true;
			else if(!image.toLowerCase().equals("no")) {
				System.err.println("Error: Unexpected value for 'image' found " + image + ". 'yes' or 'no' expected.");
				System.exit(4);
			}
		}
		if(argsHash.containsKey("eccs")) {
			endChainCarbonSmarts = argsHash.get("eccs");
			System.out.println("End chain carbon SMARTS set to " + endChainCarbonSmarts);
		}
		if(argsHash.containsKey("df")) {
			debugFolder = argsHash.get("df");
			File debugFolderObj = new File(debugFolder);
 			if(!debugFolderObj.exists()) {
				System.err.println("Error: Debug folder (df) " + debugFolder + " not found.");
				System.exit(5);
 			}
 			if(!debugFolderObj.isDirectory())  {
				System.err.println("Error: Debug folder (df) " + debugFolder + " is no directory.");
				System.exit(6);
 			}
 			System.out.println("Debug folder set to " + debugFolder);
 			if(debugFolderObj.listFiles().length != 0)
 				System.err.println("Warning: Debug folder (df) " + debugFolder + " contains files. You should remove them before using this tool.");
		}
		
		System.out.println("# Step 01");
		int[] endChainCarbonIndexes = pfas.findEndChainCarbons(endChainCarbonSmarts, debugFolder);

		System.out.println("# Step 02");
		List<List<Integer>> bondIndexesToSplitForAllSmarts = new ArrayList<List<Integer>>();
		try {	
			for(int ii = 0; ii < extendedChainSmarts.length; ii++) {
				List<Integer> bondIndexesAfterEndChain = pfas.getBondIndexAfterEndChain(endChainCarbonIndexes, 
					extendedChainSmarts[ii], ii, debugFolder);
				bondIndexesToSplitForAllSmarts.add(bondIndexesAfterEndChain);
			}
		} catch(java.lang.IndexOutOfBoundsException e) {
			System.err.println("Error: The initial match for the PFAS chain ending seemed to cause an error. Check your input SMILES.");
			System.exit(6);
		}
		System.out.println("# Step 03");
		int numberFoundToSplit = 0;
		boolean fragmentFound = false;
		for(int ii = 0; ii < bondIndexesToSplitForAllSmarts.size(); ii++) {
			// favor smarts unequal "" in case of multiple matches
			if(bondIndexesToSplitForAllSmarts.get(ii).size() == 0) continue;
			
			if(numberFoundToSplit == 0) {
				File file = File.createTempFile("pref", ".png", new File(Constants.OS_TEMP_DIR));
				if(createImage) {
					pfas.saveHighlightedBondsImage(bondIndexesToSplitForAllSmarts.get(ii), file.getAbsolutePath());
					System.out.println("stored bond(s) to break highlighted in " + file.getAbsolutePath());
				}
				
				FragmentPFAS fragment = pfas.getSplitResult(bondIndexesToSplitForAllSmarts.get(ii), endChainCarbonIndexes);
				
				if(fragment != null) {
					numberFoundToSplit++;
					System.out.println(fragment.toString() + " smarts='" + extendedChainSmarts[ii] + "'");
					fragmentFound = true;
				}
			}
		}
		if(!fragmentFound) System.out.println("No splittable bond found for the input molecule " + smiles);
		
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
			if (!tmp[0].equals("smiles") && !tmp[0].equals("smartspath") && !tmp[0].equals("image")
					 && !tmp[0].equals("df") && !tmp[0].equals("eccs")) {
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
		if (!argsHash.containsKey("image")) {
			argsHash.put("image", "no");
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
