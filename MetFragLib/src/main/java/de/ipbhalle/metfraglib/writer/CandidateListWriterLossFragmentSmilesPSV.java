package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class CandidateListWriterLossFragmentSmilesPSV implements IWriter {
	
	public boolean write(IList list, String filename, String path) {
		return write(list, filename, path, null);
	}
	
	public boolean write(IList list, String filename, String path, Settings settings) {
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
		
		String[] lines = new String[candidateList.getNumberElements()];
		String heading = "";
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
			String smilesOfFragmentsExplainedPeaks = "";
			String aromaticSmilesOfFragmentsExplainedPeaks = "";
			
			String fingerprintOfFragmentsExplainedPeaks = "";
			
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
					// get fragment of explained peak
					IFragment frag = scoredCandidate.getMatchList().getElement(ii).getBestMatchedFragment();
					fingerprintOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(frag.getStructureAsIAtomContainer())) + ";";	
					smilesOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + frag.getSmiles() + ";";
					aromaticSmilesOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + frag.getAromaticSmiles() + ";";
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				if(fingerprintOfFragmentsExplainedPeaks.length() != 0) fingerprintOfFragmentsExplainedPeaks = fingerprintOfFragmentsExplainedPeaks.substring(0, fingerprintOfFragmentsExplainedPeaks.length() - 1);
				if(smilesOfFragmentsExplainedPeaks.length() != 0) smilesOfFragmentsExplainedPeaks = smilesOfFragmentsExplainedPeaks.substring(0, smilesOfFragmentsExplainedPeaks.length() - 1);
				if(aromaticSmilesOfFragmentsExplainedPeaks.length() != 0) aromaticSmilesOfFragmentsExplainedPeaks = aromaticSmilesOfFragmentsExplainedPeaks.substring(0, aromaticSmilesOfFragmentsExplainedPeaks.length() - 1);
				
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				if(smilesOfFragmentsExplainedPeaks.length() == 0) smilesOfFragmentsExplainedPeaks = "NA";
				if(aromaticSmilesOfFragmentsExplainedPeaks.length() == 0) aromaticSmilesOfFragmentsExplainedPeaks = "NA";
				
				scoredCandidate.setProperty("ExplPeaks", peaksExplained);
				scoredCandidate.setProperty("FormulasOfExplPeaks", sumFormulasOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("SmilesOfExplPeaks", smilesOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("FragmentFingerprintOfExplPeaks", fingerprintOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("AromaticSmilesOfExplPeaks", aromaticSmilesOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("NumberPeaksUsed", numberOfPeaksUsed);
				scoredCandidate.setProperty("NoExplPeaks", countExplainedPeaks);
				//add loss information
				if(settings != null) {
					String[] lossesInformation = createLossAnnotations(scoredCandidate.getMatchList(), settings);
					scoredCandidate.setProperty("LossSmilesOfExplPeaks", lossesInformation[0]);
					scoredCandidate.setProperty("LossAromaticSmilesOfExplPeaks", lossesInformation[1]);
					scoredCandidate.setProperty("LossFingerprintOfExplPeaks", lossesInformation[2]);
				}
			}
	
			java.util.Enumeration<String> keys = scoredCandidate.getProperties().keys();
			if(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) heading += key;
				lines[i] = "" + scoredCandidate.getProperty(key);
			}
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) heading += "|" + key;
				lines[i] += "|" + scoredCandidate.getProperty(key);
			}
		}
		java.io.BufferedWriter bwriter;
		try {
			bwriter = new java.io.BufferedWriter(new FileWriter(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".psv")));
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
	
	/**
	 * 
	 * @param matchList
	 * @param settings
	 */
	private String[] createLossAnnotations(MatchList matchList, Settings settings) {
		java.util.Vector<String> lossFingerprint = new java.util.Vector<String>();
		java.util.Vector<String> lossSmiles = new java.util.Vector<String>();
		java.util.Vector<String> lossSmarts = new java.util.Vector<String>();
		java.util.Vector<Double> lossMassDiff = new java.util.Vector<Double>();
		
		//for the precursor ion
		int ionmode = (Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME);
		boolean ispositive = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		
		double adductMass = Constants.getIonisationTypeMassCorrection(Constants.ADDUCT_NOMINAL_MASSES.indexOf(ionmode), ispositive);
		double precursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		
		double ionmass = precursorMass + adductMass ;
		
		//check all matches
		for(int i = 0; i < matchList.getNumberElements(); i++) {
			IMatch matchI = matchList.getElement(i);
			IFragment fragmentI = matchI.getBestMatchedFragment();
			double peakMassI = matchI.getMatchedPeak().getMass();
			//compare with matches with greater mass than the current one
			for(int j = i + 1; j < matchList.getNumberElements(); j++) {
				IMatch matchJ = matchList.getElement(i);
				double peakMassJ = matchJ.getMatchedPeak().getMass();
				IFragment fragmentJ = matchJ.getBestMatchedFragment();
				if(fragmentJ.isRealSubStructure(fragmentI)) {
					double diff = peakMassJ - peakMassI;
					IFragment diffFragment = fragmentJ.getDifferenceFragment(fragmentI);
					if(diffFragment == null) continue;
					lossFingerprint.add(MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(diffFragment.getStructureAsIAtomContainer())));
					lossSmiles.add(diffFragment.getSmiles());
					lossSmarts.add(diffFragment.getAromaticSmiles());
					lossMassDiff.add(diff);
				}
			}
			//do the same for the precursor ion
			double diff = ionmass - peakMassI;
			IFragment diffFragment = fragmentI.getDifferenceFragment();
			if(diffFragment == null) continue;
			lossFingerprint.add(MoleculeFunctions.fingerPrintToString(TanimotoSimilarity.calculateFingerPrint(diffFragment.getStructureAsIAtomContainer())));
			lossSmiles.add(diffFragment.getSmiles());
			lossSmarts.add(diffFragment.getAromaticSmiles());
			lossMassDiff.add(diff);
		}

		String diffSmiles = "NA";
		String diffSmarts = "NA";
		String diffFingerPrint = "NA";
		if(lossMassDiff.size() >= 1) {
			diffSmiles = lossMassDiff.get(0) + ":" + lossSmiles.get(0);
			diffSmarts = lossMassDiff.get(0) + ":" + lossSmarts.get(0);
			diffFingerPrint = lossMassDiff.get(0) + ":" + lossFingerprint.get(0);
		}
		for(int i = 1; i < lossMassDiff.size(); i++) {
			diffSmiles += ";" + lossMassDiff.get(i) + ":" + lossSmiles.get(i);
			diffSmarts += ";" + lossMassDiff.get(i) + ":" + lossSmarts.get(i);
			diffFingerPrint += ";" + lossMassDiff.get(i) + ":" + lossFingerprint.get(i);
		}
		return new String[] {diffSmiles, diffSmarts, diffFingerPrint};
	}
	
	public void nullify() {
		
	}

}
