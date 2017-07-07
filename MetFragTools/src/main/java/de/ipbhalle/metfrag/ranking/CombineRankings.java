package de.ipbhalle.metfrag.ranking;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.parameter.Constants;

public class CombineRankings {

	public static java.util.Hashtable<String, String> argsHash;
	
	public static void main(String[] args) {
		boolean argCorrect = getArgs(args);
		if (!argCorrect) {
			System.err.println(
					"run: progname folder='folder'");
			System.exit(1);
		}
		String foldername = argsHash.get("folder");
		File file = new File(foldername);
		if(!file.exists()) {
			System.err.println(foldername + " does not exists.");
			System.exit(1);
		}
		
		FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
               return !pathname.getName().contains("rankings");
            }
         };
		
		File[] files = file.listFiles(filter);
		
		ArrayList<ArrayList<String>> fileLines = new ArrayList<ArrayList<String>>();
		int numberLines = 0;
		
		for(int i = 0; i < files.length; i++) {
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader breader;
			try {
				breader = new BufferedReader(new FileReader(files[i]));
				String line = "";
				while((line = breader.readLine()) != null) {
					lines.add(line);
				}
				fileLines.add(lines);
				breader.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(i == 0) numberLines = lines.size();
			else if(numberLines != lines.size()) {
				System.err.println("Number lines in " + files[i].getName() + " unequal " + numberLines);
				System.exit(1);;
			}
		}

		for(int i = 0; i < numberLines; i++) {
			String filename = foldername + Constants.OS_SPECIFIC_FILE_SEPARATOR + "rankings_" + (i + 1) + ".txt";
			BufferedWriter bwriter;
			try {
				bwriter = new BufferedWriter(new FileWriter(new File(filename)));
				for(int k = 0; k < fileLines.size(); k++) {
					bwriter.write(fileLines.get(k).get(i));
					bwriter.newLine();
				}
				bwriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		// delete old files 
		for(int i = 0; i < files.length; i++) {
			files[i].delete();
		}
	}

	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("folder")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("folder")) {
			System.err.println("no folder defined");
			return false;
		}
		
		return true;
	}

}
