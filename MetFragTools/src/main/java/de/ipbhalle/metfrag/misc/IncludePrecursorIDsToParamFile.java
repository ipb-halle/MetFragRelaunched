package de.ipbhalle.metfrag.misc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.process.CombinedMetFragProcess;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class IncludePrecursorIDsToParamFile {

	public static void main(String[] args) {
		String paramFile = args[0];
		String paramOutFolder = args[1];
		int maxCandidates = Integer.parseInt(args[2]);
		MetFragGlobalSettings settings;
		try {
			settings = MetFragGlobalSettings.readSettings(new File(paramFile), null);
			SettingsChecker settingsChecker = new SettingsChecker();
			if(!settingsChecker.check(settings)) System.exit(2);
			
			String[] PrecursorCompoundIDs = null;
			if(settings.containsKey("PrecursorCompoundIDs")) {
				PrecursorCompoundIDs = (String[])settings.get("PrecursorCompoundIDs");
				if(PrecursorCompoundIDs.length != 1) PrecursorCompoundIDs = null;
				settings.remove("PrecursorCompoundIDs");
			}
			
			CombinedMetFragProcess mp = new CombinedMetFragProcess(settings);
			
			//retrieve candidates from database
			boolean candidatesRetrieved = mp.retrieveCompounds();
			if(!candidatesRetrieved) throw new Exception();
			
			CandidateList cands = mp.getCandidateList();
			
			String lineToAdd = "PrecursorCompoundIDs = ";
			
			ArrayList<Integer> list = new ArrayList<Integer>();
			for(int i = 0; i < cands.getNumberElements(); i++) {
				list.add(i);
			}
			Collections.shuffle(list);
			boolean correctIdentifierFound = false;
			for(int i = 0; i < list.size(); i++) {
				String curIdentifier = cands.getElement(list.get(i)).getIdentifier();
				if(i < maxCandidates) {
					lineToAdd += curIdentifier + ",";
				}
				if(PrecursorCompoundIDs != null) if(curIdentifier.equals(PrecursorCompoundIDs[0])) correctIdentifierFound = true;
			}
			if(correctIdentifierFound && PrecursorCompoundIDs != null) lineToAdd = lineToAdd + PrecursorCompoundIDs[0];
			else lineToAdd = lineToAdd.substring(0, lineToAdd.length() - 1);
			
			if(PrecursorCompoundIDs != null && !correctIdentifierFound) {
				System.out.println("Didn't find correct identifier " + PrecursorCompoundIDs[0] + " for " + paramFile);
				System.exit(1);
			}
			rewriteParamFile(paramFile, 
					paramOutFolder + Constants.OS_SPECIFIC_FILE_SEPARATOR + paramFile.replaceAll(".*/", ""), 
					lineToAdd);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}

	public static void rewriteParamFile(String filenameIn, String filenameOut, String precursorString) throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader breader = new BufferedReader(new FileReader(new File(filenameIn)));
		String line = "";
		while((line = breader.readLine()) != null) {
			if(line.startsWith("PrecursorCompoundIDs")) {
				lines.add("# " + line);
				lines.add(precursorString);
			} else lines.add(line);
		}
		breader.close();
		// write new lines
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(filenameOut)));
		for(int i = 0; i < lines.size(); i++) {
			bwriter.write(lines.get(i));
			bwriter.newLine();
		}
		bwriter.close();
	}
	
}
