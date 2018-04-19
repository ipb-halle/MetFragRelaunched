package de.ipbhalle.metfrag.substructure;

import java.io.File;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.interfaces.IPeak;
import de.ipbhalle.metfraglib.interfaces.IPeakListReader;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.parameter.SettingsChecker;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterCSV;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class AddMissingNonExplainedPeaks {

	public static void main(String[] args) throws Exception {
		
		String paramfile = args[0];
		String resultfile = args[1];
		String outputfile = args[2];
		
		Settings settings = getSettings(paramfile);
		
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultfile);
		
		IPeakListReader peakListReader = (IPeakListReader) Class
				.forName((String) settings.get(VariableNames.METFRAG_PEAK_LIST_READER_NAME))
				.getConstructor(Settings.class).newInstance(settings);
		
		SettingsChecker settingsChecker = new SettingsChecker();
		if(!settingsChecker.check(settings)) {
			System.err.println("Problems reading settings");
			return;
		}
		
		settings.set(VariableNames.PEAK_LIST_NAME, peakListReader.read());
		
		IDatabase db = null;
		String dbFilename = (String) settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME);
		if (dbFilename.endsWith("psv"))
			db = new LocalPSVDatabase(settings);
		else
			db = new LocalCSVDatabase(settings);
		ArrayList<String> ids = null;
		try {
			ids = db.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		CandidateList candidates = null;
		try {
			candidates = db.getCandidateByIdentifier(ids);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (candidates.getNumberElements() == 0) {
			System.out.println(
					"No candidates found in " + (String) settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
			return;
		}
		
		DefaultPeakList peaklist = (DefaultPeakList)settings.get(VariableNames.PEAK_LIST_NAME);
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			String explPeaks = (String)candidates.getElement(i).getProperty("ExplPeaks");
			String[] explPeaksArray = explPeaks.split(";");
			Double[] explPeaksMasses = null;
			if(!explPeaks.equals("NA")) explPeaksMasses = getDoubleArrayFromPeakList(explPeaksArray);
			String nonExplPeaksString = "";
			for(int k = 0; k < peaklist.getNumberElements(); k++) {
				if(explPeaks.equals("NA")) {
					nonExplPeaksString += ((IPeak)peaklist.getElement(k)).getMass() + ";";
				} else if(!isContained(((IPeak)peaklist.getElement(k)).getMass(), explPeaksMasses)) {
					nonExplPeaksString += ((IPeak)peaklist.getElement(k)).getMass() + ";";
				}
			}
			if(nonExplPeaksString.length() == 0) nonExplPeaksString = "NA";
			if(nonExplPeaksString.endsWith(";")) nonExplPeaksString = nonExplPeaksString.substring(0, nonExplPeaksString.length() - 1);
			candidates.getElement(i).setProperty("NonExplainedMasses", nonExplPeaksString);
		}
		IWriter writer = null;
		if (outputfile.endsWith("psv"))
			writer = new CandidateListWriterPSV();
		else
			writer = new CandidateListWriterCSV();
		
		writer.write(candidates, outputfile);
	}
	
	public static Double[] getDoubleArrayFromPeakList(String[] explPeaks) {
		Double[] peaks = new Double[explPeaks.length];
		for(int i = 0; i < explPeaks.length; i++) {
			peaks[i] = Double.parseDouble(explPeaks[i].split("_")[0]);
		}
		return peaks;
	}
	
	public static boolean isContained(Double mass, Double[] explPeakMasses) {
		for(int i = 0; i < explPeakMasses.length; i++) {
			if(explPeakMasses[i].equals(mass)) return true;
		}
		return false;
	}
	
	public static Settings getSettings(String parameterfile) {
		File parameterFile = new File(parameterfile);
		MetFragGlobalSettings settings = null;
		try {
			settings = MetFragGlobalSettings.readSettings(parameterFile, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return settings;
	}
}
