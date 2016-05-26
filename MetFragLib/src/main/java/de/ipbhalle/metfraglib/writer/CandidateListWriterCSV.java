package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CandidateListWriterCSV implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws IOException {
		return this.write(list, filename, path);
	}
	
	public boolean write(IList list, String filename, String path) throws IOException {
		CandidateList candidateList = null;
		int numberOfPeaksUsed = 0;
		if(list instanceof ScoredCandidateList || list instanceof SortedScoredCandidateList) {
			candidateList = (ScoredCandidateList) list;
			numberOfPeaksUsed = ((ScoredCandidateList) list).getNumberPeaksUsed();
		}
		if(list instanceof CandidateList) {
			candidateList = (CandidateList) list;
		}
		if(candidateList == null) return false;

		File file = new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".csv");
		java.io.Writer writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), Charset.forName("UTF-8"));
		CSVPrinter csvFilePrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
		java.util.List<Object> header = new java.util.ArrayList<Object>();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			int countExplainedPeaks = 0;
			ICandidate scoredCandidate = candidateList.getElement(i);
			if(scoredCandidate.getMatchList() != null) {
				MatchList matchList = scoredCandidate.getMatchList();
				for(int l = 0; l < matchList.getNumberElements(); l++) {
					try {
						matchList.getElement(l).getMatchedPeak().getIntensity();
					}
					catch(RelativeIntensityNotDefinedException e1) {
						continue;
					}
					countExplainedPeaks++;
				}
			}
			
			String peaksExplained = "";
			String sumFormulasOfFragmentsExplainedPeaks = "";
			
			if(scoredCandidate.getMatchList() != null) 
			{
				for(int ii = 0; ii < scoredCandidate.getMatchList().getNumberElements(); ii++) 
				{
					try {
						double intensity = scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity();
						peaksExplained += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + intensity + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					String formula = scoredCandidate.getMatchList().getElement(ii).getModifiedFormulaStringOfBestMatchedFragment();
					
					sumFormulasOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + formula + ";";
				
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				scoredCandidate.setProperty(VariableNames.EXPLAINED_PEAKS_COLUMN, peaksExplained);
				scoredCandidate.setProperty(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN, sumFormulasOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty(VariableNames.NUMBER_PEAKS_USED_COLUMN, numberOfPeaksUsed);
				scoredCandidate.setProperty(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN, countExplainedPeaks);
			}
	
			java.util.Enumeration<String> keys = scoredCandidate.getProperties().keys();
			
			if(i == 0) {
				while(keys.hasMoreElements()) {
					String key = keys.nextElement();
					header.add(key);
				}
				csvFilePrinter.printRecord(header);
			}
			java.util.List<Object> entries = new java.util.ArrayList<Object>();
			for(int ii = 0; ii < header.size(); ii++) {
				entries.add(checkEmptyProperty(scoredCandidate.getProperty((String)header.get(ii))));
			}
			csvFilePrinter.printRecord(entries);
		}
		writer.flush();
		writer.close();
		csvFilePrinter.close();
		return true;
	}
	
	private Object checkEmptyProperty(Object prop) {
		try {
			String value = (String)prop;
			if(value.trim().length() == 0) return "NA";
		}
		catch(Exception e) {
			return prop;
		}
		return prop;
	}
	
	public void nullify() {
		
	}
	
	public static void main(String[] args) throws IOException {
		java.io.Writer writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(new File("/tmp/test.csv")), Charset.forName("UTF-8"));
		CSVPrinter csvFilePrinter = new CSVPrinter(writer, CSVFormat.EXCEL);
		java.util.List<Object> header = new java.util.ArrayList<Object>();
		header.add("col1");
		header.add("col2");
		csvFilePrinter.printRecord(header);
		csvFilePrinter.printRecord(header);
		writer.close();
		csvFilePrinter.close();
	}

}
