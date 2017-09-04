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

public class HDCandidateListWriterPSV implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean writeFile(File file, IList list, Settings settings) throws IOException {
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
			
		}
		
		java.util.Hashtable<String, java.util.ArrayList<ICandidate>> hdGroupedCandidates = new java.util.Hashtable<String, java.util.ArrayList<ICandidate>>();	
		String[] lines = new String[candidateList.getNumberElements()];
		String heading = "";
		
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			if(hdGroupedCandidates.containsKey((String)candidate.getProperty(VariableNames.HD_GROUP_FLAG_NAME)))
				((java.util.ArrayList<ICandidate>)hdGroupedCandidates.get((String)candidate.getProperty(VariableNames.HD_GROUP_FLAG_NAME))).add(candidate);
			else {
				java.util.ArrayList<ICandidate> vec = new java.util.ArrayList<ICandidate>();
				vec.add(candidate);
				hdGroupedCandidates.put((String)candidate.getProperty(VariableNames.HD_GROUP_FLAG_NAME), vec);
			}
		}
		java.util.ArrayList<String> propertyNames = new java.util.ArrayList<String>();
		java.util.Iterator<?> it = (java.util.Iterator<?>)hdGroupedCandidates.keys();
		while(it.hasNext()) {
			String currentGroup = (String)it.next();
			java.util.ArrayList<ICandidate> vec = hdGroupedCandidates.get(currentGroup);
			int originalCandidate = -1;
			int maxPropertySize = 0;
			//find original candidate properties
			for(int i = 0; i < vec.size(); i++) {
				if(vec.get(i).getProperties().size() > maxPropertySize) {
					originalCandidate = i;
					maxPropertySize = vec.get(i).getProperties().size();
				}
			}
			//fill in missing properties
			java.util.Hashtable<String, Object> properties = vec.get(originalCandidate).getProperties();
			for(int i = 0; i < vec.size(); i++) {
				ICandidate currentCandidate = vec.get(i);
				java.util.Iterator<?> prop_it = (java.util.Iterator<?>)properties.keys();
				while(prop_it.hasNext()) {
					String key = (String)prop_it.next();
					if(!propertyNames.contains(key)) propertyNames.add(key);
					if(currentCandidate.getProperty(key) == null) 
						currentCandidate.setProperty(key, properties.get(key));
				}
			}
		}
		
		
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate scoredCandidate = candidateList.getElement(i);
			if(propertyNames.size() >= 1) {
				String key = propertyNames.get(0);
				if(i == 0) heading += key;
				lines[i] = "" + checkEmptyProperty(scoredCandidate.getProperty(key));
			}
			for(int k = 1; k < propertyNames.size(); k++) { 
				String key = propertyNames.get(k);
				if(i == 0) heading += "|" + key;
				lines[i] += "|" + checkEmptyProperty(scoredCandidate.getProperty(key));
			}
		}
		
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new FileWriter(file));
		bwriter.write(heading);
		bwriter.newLine();
		for(int i = 0; i < lines.length; i++) {
			bwriter.write(lines[i]);
			bwriter.newLine();
		}
		bwriter.close();
		
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

	@Override
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".csv"), list, null);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

}
