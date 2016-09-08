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
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterExtendedPSV implements IWriter {

	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.write(list, filename, path);
	}
	
	public boolean write(IList list, String filename, String path) {
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
		String heading = "";
		String[] lines = new String[candidateList.getNumberElements()];
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate scoredCandidate = candidateList.getElement(i);
			int countExplainedPeaks = 0;
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
			
			String atomBitArray = "";
			String bondBitArray = "";
			String brokenBondBitArray = "";
			
			if(scoredCandidate.getMatchList() != null) {
				for(int ii = 0; ii < scoredCandidate.getMatchList().getNumberElements(); ii++) {
					try {
						double intensity = scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity();
						peaksExplained += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() 
								+ "_" + intensity + ";";
					} catch (RelativeIntensityNotDefinedException e1) {
						continue;
					}
					String formula = scoredCandidate.getMatchList().getElement(ii).getModifiedFormulasStringOfBestMatchedFragment();
					sumFormulasOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + formula + ";";
				
					atomBitArray += scoredCandidate.getMatchList().getElement(ii).getMatchFragmentsAtomsInfo() + ";";
					bondBitArray += scoredCandidate.getMatchList().getElement(ii).getMatchFragmentsBondsInfo() + ";";
					brokenBondBitArray += scoredCandidate.getMatchList().getElement(ii).getMatchFragmentsBrokenBondsInfo() + ";";
				
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(atomBitArray.length() != 0) {
					atomBitArray = atomBitArray.substring(0, atomBitArray.length() - 1);
					bondBitArray = bondBitArray.substring(0, bondBitArray.length() - 1);
					brokenBondBitArray = brokenBondBitArray.substring(0, brokenBondBitArray.length() - 1);
				}
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				
				scoredCandidate.setProperty("FragmentAtomBitArrays", atomBitArray);
				scoredCandidate.setProperty("FragmentBondBitArrays", bondBitArray);
				scoredCandidate.setProperty("FragmentBrokenBondBitArrays", brokenBondBitArray);
				scoredCandidate.setProperty("ExplPeaks", peaksExplained);
				scoredCandidate.setProperty("FormulasOfExplPeaks", sumFormulasOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("NumberPeaksUsed", numberOfPeaksUsed);
				scoredCandidate.setProperty("NoExplPeaks", countExplainedPeaks);
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
		try {
			bwriter = new java.io.BufferedWriter(new FileWriter(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + "_extended.psv")));
			bwriter.write(heading);
			bwriter.newLine();
			for(int i = 0; i < lines.length; i++) {
				bwriter.write(lines[i]);
				bwriter.newLine();
			}
			bwriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return false;
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

}
