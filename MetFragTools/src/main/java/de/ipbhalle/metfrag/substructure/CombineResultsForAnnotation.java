package de.ipbhalle.metfrag.substructure;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.candidate.PrecursorCandidate;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class CombineResultsForAnnotation {

	public static java.util.Hashtable<String, String> argsHash;
	
	public static void main(String[] args) {
		if(!getArgs(args)) {
			return;
		}
		
		String metfragFolder = argsHash.get("metfragFolder");
		String parametersFolder = argsHash.get("parametersFolder");
		String outputFolder = argsHash.get("outputFolder");
		String outputPrefix = argsHash.get("outputPrefix");
		
		File[] metfragFiles = new File(metfragFolder).listFiles();
		File[] parameterFiles = new File(parametersFolder).listFiles();
		
		CandidateList posList = new CandidateList();
		CandidateList negList = new CandidateList();
		
		for(int i = 0; i < parameterFiles.length; i++) {
			String id = parameterFiles[i].getName().split("\\.")[0];
			try {
				String inchikey1 = getInChIKey1(parameterFiles[i]);
				ICandidate candidate = getMatchingCandidate(metfragFiles, id, inchikey1);
				if(candidate == null) {
					System.out.println(id + " " + getInChIKey1(parameterFiles[i]) + " not found");
					continue;
				}
				ICandidate newCand = new PrecursorCandidate(candidate.getInChI(), candidate.getIdentifier());
				newCand.setProperty("FragmentFingerprintOfExplPeaks", candidate.getProperty("FragmentFingerprintOfExplPeaks"));
				newCand.setProperty("SmilesOfExplPeaks", candidate.getProperty("SmilesOfExplPeaks"));
				newCand.setProperty("LossFingerprintOfExplPeaks", candidate.getProperty("LossFingerprintOfExplPeaks"));
				if(id.endsWith("01")) posList.addElement(newCand);
				else negList.addElement(newCand);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println(posList.getNumberElements() + " pos");
		System.out.println(negList.getNumberElements() + " neg");
		
		CandidateListWriterPSV writer = new CandidateListWriterPSV();
		try {
			writer.write(posList, outputPrefix + "_pos", outputFolder);
			writer.write(negList, outputPrefix + "_neg", outputFolder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param metfragFiles
	 * @param id
	 * @param inchikey1
	 * @return
	 */
	public static ICandidate getMatchingCandidate(File[] metfragFiles, String id, String inchikey1) {
		for(int i = 0; i < metfragFiles.length; i++) {
			if(metfragFiles[i].getName().startsWith(id)) {
				MetFragGlobalSettings settings = new MetFragGlobalSettings();
				settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, metfragFiles[i].getAbsolutePath());
				IDatabase db = null;
				if(metfragFiles[i].getName().endsWith("csv")) db = new LocalCSVDatabase(settings);
				else db = new LocalPSVDatabase(settings);
				ArrayList<String> identifiers = null;
				try {
					identifiers = db.getCandidateIdentifiers();
				} catch (MultipleHeadersFoundInInputDatabaseException e1) {
					e1.printStackTrace();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
				CandidateList candidates = null;
				try {
					candidates = db.getCandidateByIdentifier(identifiers);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				for(int ii = 0; ii < candidates.getNumberElements(); ii++) {
					if(((String)candidates.getElement(ii).getProperty(VariableNames.INCHI_KEY_1_NAME)).equals(inchikey1)) {
						return candidates.getElement(ii);
					}
				}
			}
		}
		return null;
	}
	
	public static String getInChIKey1(File parameterFile) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(parameterFile));
		String line = "";
		while((line = breader.readLine()) != null) {
			if(line.matches("^# [A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z][A-Z].*")) {
				breader.close();
				return line.split("\\s++")[1].trim();
			}
		}
		breader.close();
		return "";
	}
	
	public static boolean getArgs(String[] args) {
		argsHash = new java.util.Hashtable<String, String>();
		for (String arg : args) {
			arg = arg.trim();
			String[] tmp = arg.split("=");
			if (!tmp[0].equals("metfragFolder") && !tmp[0].equals("parametersFolder") &&
					!tmp[0].equals("outputFolder") && !tmp[0].equals("outputPrefix")) {
				System.err.println("property " + tmp[0] + " not known.");
				return false;
			}
			if (argsHash.containsKey(tmp[0])) {
				System.err.println("property " + tmp[0] + " already defined.");
				return false;
			}
			argsHash.put(tmp[0], tmp[1]);
		}
		
		if (!argsHash.containsKey("metfragFolder")) {
			System.err.println("no metfragFolder defined");
			return false;
		}

		if (!argsHash.containsKey("parametersFolder")) {
			System.err.println("no parametersFolder defined");
			return false;
		}

		if (!argsHash.containsKey("outputFolder")) {
			System.err.println("no outputFolder defined");
			return false;
		}

		if (!argsHash.containsKey("outputPrefix")) {
			System.err.println("no outputPrefix defined");
			return false;
		}
		
		return true;
	}

	
}
