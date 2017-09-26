package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fingerprint.FingerprintCollection;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IList;
import de.ipbhalle.metfraglib.interfaces.IMatch;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.interfaces.IWriter;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.list.ScoredCandidateList;
import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class CandidateListWriterLossFragmentSmilesCompletePSV implements IWriter {
	
	public boolean write(IList list, String filename, String path) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".psv"), list, null);
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
		
		String[] lines = new String[candidateList.getNumberElements()];
		String heading = "";
		
		FingerprintCollection fingerprintCollection = new FingerprintCollection();
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
			String smilesOfFragmentsExplainedPeaks = "";
			String aromaticSmilesOfFragmentsExplainedPeaks = "";
			
			String fingerprintOfFragmentsExplainedPeaks[] = new String[fingerprintCollection.getNumberFingerprinters()];
			for(int ii = 0; ii < fingerprintOfFragmentsExplainedPeaks.length; ii++)
				fingerprintOfFragmentsExplainedPeaks[ii] = "";
			
			if(scoredCandidate.getMatchList() != null) 
			{
				String[] matchedFormulas = new String[scoredCandidate.getMatchList().getNumberElements()];
				double[] correctedMasses = new double[scoredCandidate.getMatchList().getNumberElements()];
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
					double mass = scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass();
					if((Boolean)settings.get(VariableNames.CORRECT_MASSES_FOR_FINGERPRINT_ANNOTATION_NAME)) {
						matchedFormulas[ii] = formula;
						correctedMasses[ii] = MathTools.round(calculateMassOfFormula(formula), 5);
						mass = correctedMasses[ii];
					}
					sumFormulasOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + formula + ";";
					// get fragment of explained peak
					IFragment frag = scoredCandidate.getMatchList().getElement(ii).getBestMatchedFragment();
					String[][] fpsm = null;
					try {
						fpsm = fingerprintCollection.getNormalizedFingerprintSmiles(scoredCandidate.getPrecursorMolecule(), frag);
					} catch(Exception e) {
						continue;
					}
					
					for(int iii = 0; iii < fpsm.length; iii++) 
						fingerprintOfFragmentsExplainedPeaks[iii] += mass + ":" + fpsm[iii][0] + ";";	
					smilesOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + fpsm[0][1] + ";";
					aromaticSmilesOfFragmentsExplainedPeaks += scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass() + ":" + frag.getAromaticSmiles(scoredCandidate.getPrecursorMolecule()) + ";";
				}
				if(sumFormulasOfFragmentsExplainedPeaks.length() != 0) sumFormulasOfFragmentsExplainedPeaks = sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1);
				if(peaksExplained.length() != 0) peaksExplained = peaksExplained.substring(0, peaksExplained.length() - 1);
				for(int ii = 0; ii < fingerprintOfFragmentsExplainedPeaks.length; ii++) 
					if(fingerprintOfFragmentsExplainedPeaks[ii].length() != 0) 
						fingerprintOfFragmentsExplainedPeaks[ii] = fingerprintOfFragmentsExplainedPeaks[ii].substring(0, fingerprintOfFragmentsExplainedPeaks[ii].length() - 1);
				if(smilesOfFragmentsExplainedPeaks.length() != 0) smilesOfFragmentsExplainedPeaks = smilesOfFragmentsExplainedPeaks.substring(0, smilesOfFragmentsExplainedPeaks.length() - 1);
				if(aromaticSmilesOfFragmentsExplainedPeaks.length() != 0) aromaticSmilesOfFragmentsExplainedPeaks = aromaticSmilesOfFragmentsExplainedPeaks.substring(0, aromaticSmilesOfFragmentsExplainedPeaks.length() - 1);
				
				if(peaksExplained.length() == 0) peaksExplained = "NA";
				if(sumFormulasOfFragmentsExplainedPeaks.length() == 0) sumFormulasOfFragmentsExplainedPeaks = "NA";
				if(smilesOfFragmentsExplainedPeaks.length() == 0) smilesOfFragmentsExplainedPeaks = "NA";
				if(aromaticSmilesOfFragmentsExplainedPeaks.length() == 0) aromaticSmilesOfFragmentsExplainedPeaks = "NA";
				for(int ii = 0; ii < fingerprintOfFragmentsExplainedPeaks.length; ii++) 
					if(fingerprintOfFragmentsExplainedPeaks[ii].length() == 0) 
						fingerprintOfFragmentsExplainedPeaks[ii] = "NA";
				
				scoredCandidate.setProperty("ExplPeaks", peaksExplained);
				scoredCandidate.setProperty("FormulasOfExplPeaks", sumFormulasOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("SmilesOfExplPeaks", smilesOfFragmentsExplainedPeaks);
				for(int ii = 0; ii < fingerprintCollection.getNumberFingerprinters(); ii++) 
					scoredCandidate.setProperty("FragmentFingerprintOfExplPeaks" + fingerprintCollection.getNameOfFingerprinter(ii), fingerprintOfFragmentsExplainedPeaks[ii]);
				scoredCandidate.setProperty("AromaticSmilesOfExplPeaks", aromaticSmilesOfFragmentsExplainedPeaks);
				scoredCandidate.setProperty("NumberPeaksUsed", numberOfPeaksUsed);
				scoredCandidate.setProperty("NoExplPeaks", countExplainedPeaks);
				//add loss information
				if(settings != null) {
					String[][] lossesInformation = createLossAnnotations(scoredCandidate.getPrecursorMolecule(), scoredCandidate.getMatchList(), settings, correctedMasses, fingerprintCollection);
					scoredCandidate.setProperty("LossSmilesOfExplPeaks", lossesInformation[0][0]);
					scoredCandidate.setProperty("LossAromaticSmilesOfExplPeaks", lossesInformation[0][1]);
					for(int ii = 0; ii < fingerprintCollection.getNumberFingerprinters(); ii++) 
						scoredCandidate.setProperty("LossFingerprintOfExplPeaks" + fingerprintCollection.getNameOfFingerprinter(ii), lossesInformation[ii][2]);
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
			bwriter = new java.io.BufferedWriter(new FileWriter(file));
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
	 * @throws Exception 
	 */
	private String[][] createLossAnnotations(IMolecularStructure precursorMolecule, MatchList matchList, Settings settings, double[] correctedMasses, FingerprintCollection fingerprintCollection) throws Exception {
		java.util.ArrayList<String[]> lossFingerprint = new java.util.ArrayList<String[]>();
		java.util.ArrayList<String> lossSmiles = new java.util.ArrayList<String>();
		java.util.ArrayList<String> lossSmarts = new java.util.ArrayList<String>();
		java.util.ArrayList<Double> lossMassDiff = new java.util.ArrayList<Double>();
		
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
		//	double peakMassI = matchI.getMatchedPeak().getMass();
			double peakMassI = matchI.getMatchedPeak().getMass();
			if((Boolean)settings.get(VariableNames.CORRECT_MASSES_FOR_FINGERPRINT_ANNOTATION_NAME))
				peakMassI = correctedMasses[i];
			//compare with matches with greater mass than the current one
			for(int j = i + 1; j < matchList.getNumberElements(); j++) {
				IMatch matchJ = matchList.getElement(i);
			//	double peakMassJ = matchJ.getMatchedPeak().getMass();
				double peakMassJ = matchJ.getMatchedPeak().getMass();
				if((Boolean)settings.get(VariableNames.CORRECT_MASSES_FOR_FINGERPRINT_ANNOTATION_NAME))
					peakMassJ = correctedMasses[j];
				IFragment fragmentJ = matchJ.getBestMatchedFragment();
				if(fragmentJ.isRealSubStructure(fragmentI)) {
					double diff = peakMassJ - peakMassI;
					IFragment diffFragment = fragmentJ.getDifferenceFragment(precursorMolecule, fragmentI);
					if(diffFragment == null) continue;
					
					String[][] fpsm = fingerprintCollection.getNormalizedFingerprintSmiles(precursorMolecule, diffFragment);
					lossFingerprint.add(fpsm[0]);
					lossSmiles.add(fpsm[0][1]);
					lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
					lossMassDiff.add(diff);
				}
			}
			//do the same for the precursor ion
			double diff = ionmass - peakMassI;
			IFragment diffFragment = fragmentI.getDifferenceFragment(precursorMolecule);
			if(diffFragment == null) continue;
			
			String[][] fpsm = fingerprintCollection.getNormalizedFingerprintSmiles(precursorMolecule, diffFragment);
			String[] fps = new String[fpsm.length];
			for(int ii = 0; ii < fpsm.length; ii++) fps[ii] = fpsm[ii][0];
			lossFingerprint.add(fps);
			lossSmiles.add(fpsm[0][1]);
			lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
			lossMassDiff.add(diff);
		}

		String diffSmiles = "NA";
		String diffSmarts = "NA";
		String[] diffFingerPrints = new String[fingerprintCollection.getNumberFingerprinters()];  
		for(int i = 0; i < diffFingerPrints.length; i++) diffFingerPrints[i] = "NA";
		if(lossMassDiff.size() >= 1) {
			diffSmiles = lossMassDiff.get(0) + ":" + lossSmiles.get(0);
			diffSmarts = lossMassDiff.get(0) + ":" + lossSmarts.get(0);
			for(int ii = 0; ii < diffFingerPrints.length; ii++) 
				diffFingerPrints[ii] = lossMassDiff.get(0) + ":" + lossFingerprint.get(0)[ii];
		}
		for(int i = 1; i < lossMassDiff.size(); i++) {
			diffSmiles += ";" + lossMassDiff.get(i) + ":" + lossSmiles.get(i);
			diffSmarts += ";" + lossMassDiff.get(i) + ":" + lossSmarts.get(i);
			for(int ii = 0; ii < diffFingerPrints.length; ii++) 
				diffFingerPrints[ii] +=  ";" + lossMassDiff.get(i) + ":" + lossFingerprint.get(i)[ii];
		}
		String[][] fps_return = new String[fingerprintCollection.getNumberFingerprinters()][3];
		for(int i = 0; i < fingerprintCollection.getNumberFingerprinters(); i++) {
			fps_return[i] = new String[] {diffSmiles, diffSmarts, diffFingerPrints[i]};
		}
		return fps_return;
	}
	
	public void nullify() {
		
	}

	@Override
	public boolean write(IList list, String filename, String path, Settings settings) throws Exception {
		return this.writeFile(new File(path + Constants.OS_SPECIFIC_FILE_SEPARATOR + filename + ".psv"), list, settings);
	}

	@Override
	public boolean write(IList list, String filename) throws Exception {
		return this.writeFile(new File(filename), list, null);
	}

	@Override
	public boolean writeFile(File file, IList list) throws Exception {
		return this.writeFile(file, list, null);
	}

	private double calculateMassOfFormula(String formula) {
		String part1 = formula.replaceAll("\\[([A-Za-z0-9]*).*\\].*", "$1");
		String charge = formula.substring(formula.length() - 1);
		java.util.ArrayList<String> elementsToAdd = new java.util.ArrayList<String>();
		java.util.ArrayList<String> timesToAdd = new java.util.ArrayList<String>();
		java.util.ArrayList<String> signsForAdd = new java.util.ArrayList<String>();
		for(int i = 0; i < formula.length() - 1; i++) {
			if(formula.charAt(i) == '+' || formula.charAt(i) == '-') {
				signsForAdd.add(formula.charAt(i)+"");
				boolean numberFinished = false;
				String number = "";
				String element = "";
				for(int k = (i+1); k < formula.length() - 1; k++) {
					if(!numberFinished && Character.isDigit(formula.charAt(k))) number += formula.charAt(k);
					else if(!numberFinished && !Character.isDigit(formula.charAt(k))) {
						if(number.equals("")) number = "1";
						numberFinished = true;
					}
					if(Character.isLowerCase(formula.charAt(k)) && !element.equals("")) element += formula.charAt(k);
					if(Character.isUpperCase(formula.charAt(k)) && element.equals("")) element += formula.charAt(k);
					if(Character.isUpperCase(formula.charAt(k)) && !element.equals("")) break;
					if(Character.isDigit(formula.charAt(k)) && numberFinished) break;
					if(!Character.isUpperCase(formula.charAt(k)) && !Character.isLowerCase(formula.charAt(k)) && !Character.isDigit(formula.charAt(k))) break;
				}
				elementsToAdd.add(element);
				timesToAdd.add(number);
			}
		}
		
		double mass = 0.0;
		try {
			boolean isPositive = charge.equals("-") ? false : true;
			double chargeMass = Constants.getChargeMassByType(isPositive);
			ByteMolecularFormula bmf = new ByteMolecularFormula(part1);
			for(int i = 0; i < elementsToAdd.size(); i++) {
				byte atomIndex = (byte)Constants.ELEMENTS.indexOf(elementsToAdd.get(i));
				short amount = Short.parseShort(signsForAdd.get(i) + timesToAdd.get(i));
				bmf.changeNumberElementsFromByte(atomIndex, amount);
			}
			mass = bmf.getMonoisotopicMass() + chargeMass;
		} catch (AtomTypeNotKnownFromInputListException e) {
			e.printStackTrace();
		}
		return mass;
	}
	
	public static void main(String[] args) {
		String formula = "[C10H13N2O4S+H]+H+";
		CandidateListWriterLossFragmentSmilesCompletePSV clw = new CandidateListWriterLossFragmentSmilesCompletePSV();
		System.out.println(clw.calculateMassOfFormula(formula));
		formula = "[C6H6NO2S]-";
		System.out.println(clw.calculateMassOfFormula(formula));
	}
}
