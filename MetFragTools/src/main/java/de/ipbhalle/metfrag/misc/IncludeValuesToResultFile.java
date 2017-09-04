package de.ipbhalle.metfrag.misc;

import java.util.ArrayList;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class IncludeValuesToResultFile {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filenameFrom = args[0];
		String filenameTo = args[1];
		String[] valuenames = args[3].trim().split(",");
		
		MetFragGlobalSettings settingsFrom = new MetFragGlobalSettings();
		settingsFrom.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filenameFrom);
		LocalPSVDatabase dbFrom = new LocalPSVDatabase(settingsFrom);
		ArrayList<String> identifiersFrom = null;
		try {
			identifiersFrom = dbFrom.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CandidateList candidatesFrom = dbFrom.getCandidateByIdentifier(identifiersFrom);
		
		MetFragGlobalSettings settingsTo = new MetFragGlobalSettings();
		settingsTo.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filenameTo);
		LocalPSVDatabase dbTo = new LocalPSVDatabase(settingsTo);
		ArrayList<String> identifiersTo = null;
		try {
			identifiersTo = dbTo.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		CandidateList candidatesTo = dbTo.getCandidateByIdentifier(identifiersTo);
		
		if(candidatesFrom.getNumberElements() < candidatesTo.getNumberElements())
			System.out.println(filenameTo + " has more candidates than " + filenameTo);
		
		int touched = 0;
		for(int i = 0; i < candidatesFrom.getNumberElements(); i++) {
			ICandidate candidateFrom = candidatesFrom.getElement(i);
			ICandidate candidateTo = getCandidateById(candidateFrom.getIdentifier(), candidatesTo);
			if(candidateTo == null) {
				continue;
			}
			for(int k = 0; k < valuenames.length; k++) 
				candidateTo.setProperty(valuenames[k], candidateFrom.getProperty(valuenames[k]));
			touched++;
		}
		
		if(touched != candidatesTo.getNumberElements()) {
			System.err.println("Only found " + touched + " candidates of " + candidatesTo.getNumberElements());
		}
		
		CandidateListWriterPSV writer = new CandidateListWriterPSV();
		String filename = args[2].replaceAll(".*\\/", "").replaceAll("\\..*$", "");
		String path = args[2].replaceAll(filename + "\\..*$", "");
		try {
			writer.write(candidatesTo, filename, path);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static ICandidate getCandidateById(String id, CandidateList candidatesTo) {
		for(int i = 0; i < candidatesTo.getNumberElements(); i++) {
			if(candidatesTo.getElement(i).getIdentifier().equals(id))
				return candidatesTo.getElement(i);
		}
		return null;
	}
	
}
