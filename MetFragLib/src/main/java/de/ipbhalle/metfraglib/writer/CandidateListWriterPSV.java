package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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

public class CandidateListWriterPSV implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File file, IList list, Settings settings) throws Exception {
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

		if(candidateList == null || candidateList.getNumberElements() == 0) {
			writeDefaultHeader(file);
			return false;
		}
		String[] lines = new String[candidateList.getNumberElements()];
		String heading = "";
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			int countExplainedPeaks = 0;
			ICandidate scoredCandidate = candidateList.getElement(i);
			scoredCandidate.initialisePrecursorCandidate();
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
					String formula = scoredCandidate.getMatchList().getElement(ii).getModifiedFormulaStringOfBestMatchedFragment(scoredCandidate.getPrecursorMolecule());
					
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
			if(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) heading += key;
				lines[i] = "" + checkEmptyProperty(scoredCandidate.getProperty(key));
			}
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) heading += "|" + key;
				lines[i] += "|" + checkEmptyProperty(scoredCandidate.getProperty(key));
			}
		}
		java.io.BufferedWriter bwriter;
		bwriter = new java.io.BufferedWriter(new FileWriter(file));
		bwriter.write(heading);
		bwriter.newLine();
		for(int i = 0; i < lines.length; i++) {
			bwriter.write(lines[i]);
			bwriter.newLine();
		}
		bwriter.close();
		
		return true;
	}

	public void writeDefaultHeader(File file) throws IOException {
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(file));
		/*
		String[] defaultHeaderValues = {"Score","MonoisotopicMass","SMILES","InChIKey",
				"NoExplPeaks","NumberPeaksUsed","InChI",
				"MaximumTreeDepth","Identifier","ExplPeaks","InChIKey3","InChIKey2",
				"InChIKey1","CompoundName","FragmenterScore","MolecularFormula","FormulasOfExplPeaks"};
		*/
		String[] defaultHeaderValues = {};
		String header = "";
		if(defaultHeaderValues.length > 0) {
			header = defaultHeaderValues[0];
			for(int i = 1; i < defaultHeaderValues.length; i++) {
				header += "," + defaultHeaderValues[i];
			}
		}
		bwriter.write(header);
		bwriter.newLine();
		bwriter.close();
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

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".psv"), list);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
