package de.ipbhalle.metfraglib.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.openscience.cdk.interfaces.IAtomContainer;

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
import de.ipbhalle.metfraglib.parameter.ClassNames;
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
		

		java.io.BufferedWriter bwriter = new BufferedWriter(new FileWriter(file));
		
		StringBuilder heading = new StringBuilder();

		candidateList.getElement(0).removeProperty("ExplPeaks");
		candidateList.getElement(0).removeProperty("FormulasOfExplPeaks");
		candidateList.getElement(0).removeProperty("NumberPeaksUsed");
		candidateList.getElement(0).removeProperty("NoExplPeaks");

		String[] fpnames = ClassNames.getFingerprintNames();
		for(int ii = 0; ii < fpnames.length; ii++) {
			candidateList.getElement(0).removeProperty("FragmentFingerprintOfExplPeaks" + fpnames[ii]);
			candidateList.getElement(0).removeProperty("LossFingerprintOfExplPeaks" + fpnames[ii]);
		}	
		
		java.util.Enumeration<String> keys = candidateList.getElement(0).getProperties().keys();
		
		
		if(keys.hasMoreElements()) {
			String key = keys.nextElement();
			heading.append(key);
		}
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			heading.append("|");
			heading.append(key);
		}
		heading.append("|");
		heading.append("ExplPeaks");
		heading.append("|");
		heading.append("FormulasOfExplPeaks");
		heading.append("|");
		heading.append("NumberPeaksUsed");
		heading.append("|");
		heading.append("NoExplPeaks");
		
		for(int i = 0; i < fpnames.length; i++) {
			heading.append("|");
			heading.append("FragmentFingerprintOfExplPeaks" + fpnames[i]);
			heading.append("|");
			heading.append("LossFingerprintOfExplPeaks" + fpnames[i]);
		}
			
		bwriter.write(heading.toString());
		bwriter.newLine();
		
		FingerprintCollection fingerprintCollection = new FingerprintCollection();
		
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			StringBuilder line = new StringBuilder();
			int countExplainedPeaks = 0;
			ICandidate scoredCandidate = candidateList.getElement(i);
			scoredCandidate.removeProperty("ExplPeaks");
			scoredCandidate.removeProperty("FormulasOfExplPeaks");
			scoredCandidate.removeProperty("NumberPeaksUsed");
			scoredCandidate.removeProperty("NoExplPeaks");

			for(int ii = 0; ii < fpnames.length; ii++) {
				scoredCandidate.removeProperty("FragmentFingerprintOfExplPeaks" + fpnames[ii]);
				scoredCandidate.removeProperty("LossFingerprintOfExplPeaks" + fpnames[ii]);
			}	
			
			if(settings != null) scoredCandidate.setUseSmiles((Boolean)settings.get(VariableNames.USE_SMILES_NAME));
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
			StringBuilder sumFormulasOfFragmentsExplainedPeaks =  new StringBuilder();
			
			StringBuilder fingerprintOfFragmentsExplainedPeaks[] = new StringBuilder[fingerprintCollection.getNumberFingerprinters()];
			for(int ii = 0; ii < fingerprintOfFragmentsExplainedPeaks.length; ii++)
				fingerprintOfFragmentsExplainedPeaks[ii] = new StringBuilder();
			
			keys = scoredCandidate.getProperties().keys();
			if(keys.hasMoreElements()) {
				String key = keys.nextElement();
				line.append(scoredCandidate.getProperty(key));
			}
			while(keys.hasMoreElements()) {
				String key = keys.nextElement();
				line.append("|");
				line.append(scoredCandidate.getProperty(key));
			}
			
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
						correctedMasses[ii] = MathTools.round(calculateMassOfFormula(formula), 6.0);
						mass = correctedMasses[ii];
					}
					sumFormulasOfFragmentsExplainedPeaks.append(scoredCandidate.getMatchList().getElement(ii).getMatchedPeak().getMass());
					sumFormulasOfFragmentsExplainedPeaks.append(":");
					sumFormulasOfFragmentsExplainedPeaks.append(formula);
					sumFormulasOfFragmentsExplainedPeaks.append(";");
					// get fragment of explained peak
					IFragment frag = scoredCandidate.getMatchList().getElement(ii).getBestMatchedFragment();
					String[] fps = null;
					try {
						IAtomContainer con = fingerprintCollection.getNormalizedAtomContainer(scoredCandidate.getPrecursorMolecule(), frag);
						fps = fingerprintCollection.getNormalizedFingerprint(con);
					} catch(Exception e) {
						continue;
					}
					
					for(int iii = 0; iii < fps.length; iii++) {
						fingerprintOfFragmentsExplainedPeaks[iii].append(mass);
						fingerprintOfFragmentsExplainedPeaks[iii].append(":");
						fingerprintOfFragmentsExplainedPeaks[iii].append(fps[iii]);
						fingerprintOfFragmentsExplainedPeaks[iii].append(";");	
					}
				}
				
				line.append("|");
				line.append(peaksExplained.length() == 0 ? "NA" : peaksExplained.substring(0, peaksExplained.length() - 1));
				line.append("|");
				line.append(sumFormulasOfFragmentsExplainedPeaks.length() == 0 ? "NA" : sumFormulasOfFragmentsExplainedPeaks.substring(0, sumFormulasOfFragmentsExplainedPeaks.length() - 1));
				line.append("|");
				line.append(numberOfPeaksUsed);
				line.append("|");
				line.append(countExplainedPeaks);
				
				//add loss information
				if(settings != null) {
					String[][] lossesInformation = createLossAnnotations(scoredCandidate.getPrecursorMolecule(), scoredCandidate.getMatchList(), settings, correctedMasses, fingerprintCollection);
					for(int ii = 0; ii < fingerprintCollection.getNumberFingerprinters(); ii++) { 
						line.append("|");
						line.append(fingerprintOfFragmentsExplainedPeaks[ii].length() == 0 ? "NA" : fingerprintOfFragmentsExplainedPeaks[ii].substring(0, fingerprintOfFragmentsExplainedPeaks[ii].length() - 1));
						line.append("|");
						line.append(lossesInformation[ii][2]);
					}
				}
			}
			bwriter.write(line.toString());
			bwriter.newLine();
		}
		bwriter.close();
		return true;
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
		double precursorMass = precursorMolecule.getNeutralMonoisotopicMass();
		
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
					
					
					IAtomContainer con = fingerprintCollection.getNormalizedAtomContainer(precursorMolecule, diffFragment);
					
					lossFingerprint.add(fingerprintCollection.getNormalizedFingerprint(con));
					lossSmiles.add(fingerprintCollection.getNormalizedSmiles(con));
					lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
					lossMassDiff.add(MathTools.round(diff, 6.0));
				}
			}
			//do the same for the precursor ion
			double diff = ionmass - peakMassI;
			IFragment diffFragment = fragmentI.getDifferenceFragment(precursorMolecule);
			if(diffFragment == null) continue;
			
			IAtomContainer con = fingerprintCollection.getNormalizedAtomContainer(precursorMolecule, diffFragment);
			
			lossFingerprint.add(fingerprintCollection.getNormalizedFingerprint(con));
			lossSmiles.add(fingerprintCollection.getNormalizedSmiles(con));
			lossSmarts.add(diffFragment.getAromaticSmiles(precursorMolecule));
			lossMassDiff.add(MathTools.round(diff, 6.0));
		}

		StringBuilder diffSmiles = new StringBuilder();
		StringBuilder diffSmarts = new StringBuilder();
		StringBuilder[] diffFingerPrints = new StringBuilder[fingerprintCollection.getNumberFingerprinters()];  
		for(int i = 0; i < diffFingerPrints.length; i++) diffFingerPrints[i] = new StringBuilder();
		if(lossMassDiff.size() >= 1) {
			diffSmiles.append(lossMassDiff.get(0));
			diffSmiles.append(":");
			diffSmiles.append(lossSmiles.get(0));
			diffSmarts.append(lossMassDiff.get(0));
			diffSmarts.append(":");
			diffSmarts.append(lossSmarts.get(0));
			for(int ii = 0; ii < diffFingerPrints.length; ii++) { 
				diffFingerPrints[ii].append(lossMassDiff.get(0));
				diffFingerPrints[ii].append(":");
				diffFingerPrints[ii].append(lossFingerprint.get(0)[ii]);
			}
		}
		for(int i = 1; i < lossMassDiff.size(); i++) {
			diffSmiles.append(";");
			diffSmiles.append(lossMassDiff.get(i));
			diffSmiles.append(":");
			diffSmiles.append(lossSmiles.get(i));
			diffSmarts.append(";");
			diffSmarts.append(lossMassDiff.get(i));
			diffSmarts.append(":");
			diffSmarts.append(lossSmarts.get(i));
			for(int ii = 0; ii < diffFingerPrints.length; ii++) {
				diffFingerPrints[ii].append(";");
				diffFingerPrints[ii].append(lossMassDiff.get(i));
				diffFingerPrints[ii].append(":");
				diffFingerPrints[ii].append(lossFingerprint.get(i)[ii]);
			}
		}
		String[][] fps_return = new String[fingerprintCollection.getNumberFingerprinters()][3];
		for(int i = 0; i < fingerprintCollection.getNumberFingerprinters(); i++) {
			if(diffFingerPrints[i].toString().equals("")) fps_return[i] = new String[] {"NA", "NA", "NA"};
			else fps_return[i] = new String[] {diffSmiles.toString(), diffSmarts.toString(), diffFingerPrints[i].toString()};
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
