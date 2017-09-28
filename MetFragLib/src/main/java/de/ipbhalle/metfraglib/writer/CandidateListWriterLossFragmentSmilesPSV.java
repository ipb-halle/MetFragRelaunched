package de.ipbhalle.metfraglib.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.fingerprint.Fingerprint;
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

public class CandidateListWriterLossFragmentSmilesPSV implements IWriter {
	
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
		
		StringBuilder[] lines = new StringBuilder[candidateList.getNumberElements()];
		StringBuilder heading = new StringBuilder();
		
		Fingerprint fingerprint = new Fingerprint((String)settings.get(VariableNames.FINGERPRINT_TYPE_NAME));
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			System.out.println(i);
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
			
			StringBuilder peaksExplained = new StringBuilder();
			StringBuilder sumFormulasOfFragmentsExplainedPeaks = new StringBuilder();
			StringBuilder smilesOfFragmentsExplainedPeaks = new StringBuilder();
			StringBuilder aromaticSmilesOfFragmentsExplainedPeaks = new StringBuilder();
			
			StringBuilder fingerprintOfFragmentsExplainedPeaks = new StringBuilder();
			
			if(scoredCandidate.getMatchList() != null) 
			{
				String[] matchedFormulas = new String[scoredCandidate.getMatchList().getNumberElements()];
				double[] correctedMasses = new double[scoredCandidate.getMatchList().getNumberElements()];
				for(int ii = 0; ii < scoredCandidate.getMatchList().getNumberElements(); ii++) 
				{
					try {
						double intensity = scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getIntensity();
						peaksExplained.append(scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass());
						peaksExplained.append("_");
						peaksExplained.append(intensity);
						peaksExplained.append(";");
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
					sumFormulasOfFragmentsExplainedPeaks.append(scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass());
					sumFormulasOfFragmentsExplainedPeaks.append(":");
					sumFormulasOfFragmentsExplainedPeaks.append(formula);
					sumFormulasOfFragmentsExplainedPeaks.append(";");
					// get fragment of explained peak
					IFragment frag = scoredCandidate.getMatchList().getElement(ii).getBestMatchedFragment();
					String fp = null;
					String smiles = null;
					try {
						IAtomContainer con = fingerprint.getNormalizedAtomContainer(scoredCandidate.getPrecursorMolecule(), frag);
						smiles = fingerprint.getNormalizedSmiles(con);
						fp = fingerprint.getNormalizedFingerprint(con);
					} catch(Exception e) {
						continue;
					}
					fingerprintOfFragmentsExplainedPeaks.append(mass);
					fingerprintOfFragmentsExplainedPeaks.append(":");
					fingerprintOfFragmentsExplainedPeaks.append(fp);
					fingerprintOfFragmentsExplainedPeaks.append(";");	
					smilesOfFragmentsExplainedPeaks.append(scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass());
					smilesOfFragmentsExplainedPeaks.append(":");
					smilesOfFragmentsExplainedPeaks.append(smiles);
					smilesOfFragmentsExplainedPeaks.append(";");
					aromaticSmilesOfFragmentsExplainedPeaks.append(scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass());
					aromaticSmilesOfFragmentsExplainedPeaks.append(":");
					aromaticSmilesOfFragmentsExplainedPeaks.append(frag.getAromaticSmiles(scoredCandidate.getPrecursorMolecule()));
					aromaticSmilesOfFragmentsExplainedPeaks.append(";");
				}
				
				scoredCandidate.setProperty("ExplPeaks", peaksExplained.length() == 0 ? "NA" : peaksExplained.substring(0, peaksExplained.length() - 1));
				scoredCandidate.setProperty("FormulasOfExplPeaks", sumFormulasOfFragmentsExplainedPeaks.length() == 0 ? "NA" : sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1));
				scoredCandidate.setProperty("SmilesOfExplPeaks", smilesOfFragmentsExplainedPeaks.length() == 0 ? "NA" : smilesOfFragmentsExplainedPeaks.substring(0, smilesOfFragmentsExplainedPeaks.length() - 1));
				scoredCandidate.setProperty("FragmentFingerprintOfExplPeaks", fingerprintOfFragmentsExplainedPeaks.length() == 0 ? "NA" : fingerprintOfFragmentsExplainedPeaks.substring(0, fingerprintOfFragmentsExplainedPeaks.length() - 1));
				scoredCandidate.setProperty("AromaticSmilesOfExplPeaks", aromaticSmilesOfFragmentsExplainedPeaks.length() == 0 ? "NA" : aromaticSmilesOfFragmentsExplainedPeaks.substring(0, aromaticSmilesOfFragmentsExplainedPeaks.length() - 1));
				
				scoredCandidate.setProperty("NumberPeaksUsed", numberOfPeaksUsed);
				scoredCandidate.setProperty("NoExplPeaks", countExplainedPeaks);
				//add loss information
				if(settings != null) {
					String[] lossesInformation = createLossAnnotations(scoredCandidate.getPrecursorMolecule(), scoredCandidate.getMatchList(), settings, correctedMasses, fingerprint);
					scoredCandidate.setProperty("LossSmilesOfExplPeaks", lossesInformation[0]);
					scoredCandidate.setProperty("LossAromaticSmilesOfExplPeaks", lossesInformation[1]);
					scoredCandidate.setProperty("LossFingerprintOfExplPeaks", lossesInformation[2]);
				}
			}
	
			java.util.Enumeration<String> keys = scoredCandidate.getProperties().keys();
			if(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) heading.append(key);
				lines[i].append(scoredCandidate.getProperty(key));
			}
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				if(i == 0) {
					heading.append("|");
					heading.append(key);
					
				}
				lines[i].append("|");
				lines[i].append(scoredCandidate.getProperty(key));
			}
		}
		java.io.BufferedWriter bwriter;
		try {
			bwriter = new java.io.BufferedWriter(new FileWriter(file));
			bwriter.write(heading.toString());
			bwriter.newLine();
			for(int i = 0; i < lines.length; i++) {
				bwriter.write(lines[i].toString());
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
	private String[] createLossAnnotations(IMolecularStructure precursorMolecule, MatchList matchList, Settings settings, double[] correctedMasses, Fingerprint fingerprint) throws Exception {
		java.util.ArrayList<String> lossFingerprint = new java.util.ArrayList<String>();
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
					/*
					String smiles = diffFragment.getSmiles(precursorMolecule);
					IAtomContainer con;
					try {
						con = MoleculeFunctions.getAtomContainerFromSMILES(smiles);
					} catch (Exception e) {
						System.err.println("Could not generate container from " + smiles);
						continue;
					}

					String preparedSmiles = MoleculeFunctions.generateSmiles(con);
					lossFingerprint.add(MoleculeFunctions.fingerPrintToString(fingerprint.calculateFingerPrint(con)));
					MoleculeFunctions.prepareAtomContainer(con, false);
					*/
					IAtomContainer con = fingerprint.getNormalizedAtomContainer(precursorMolecule, diffFragment);
						
					lossFingerprint.add(fingerprint.getNormalizedFingerprint(con));
					lossSmiles.add(fingerprint.getNormalizedSmiles(con));
					lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
					lossMassDiff.add(diff);
				}
			}
			//do the same for the precursor ion
			double diff = ionmass - peakMassI;
			IFragment diffFragment = fragmentI.getDifferenceFragment(precursorMolecule);
			if(diffFragment == null) continue;
			
			/*
			String smiles = diffFragment.getSmiles(precursorMolecule);
			IAtomContainer con;
			try {
				con = MoleculeFunctions.getAtomContainerFromSMILES(smiles);
			} catch (Exception e) {
				System.err.println("Could not generate container from " + smiles);
				continue;
			}
			
			MoleculeFunctions.prepareAtomContainer(con, false);
			String preparedSmiles = MoleculeFunctions.generateSmiles(con);
			*/
			IAtomContainer con = fingerprint.getNormalizedAtomContainer(precursorMolecule, diffFragment);
			lossFingerprint.add(fingerprint.getNormalizedFingerprint(con));
			lossSmiles.add(fingerprint.getNormalizedSmiles(con));
			lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
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
		CandidateListWriterLossFragmentSmilesPSV clw = new CandidateListWriterLossFragmentSmilesPSV();
		System.out.println(clw.calculateMassOfFormula(formula));
		formula = "[C6H6NO2S]-";
		System.out.println(clw.calculateMassOfFormula(formula));
	}
}
