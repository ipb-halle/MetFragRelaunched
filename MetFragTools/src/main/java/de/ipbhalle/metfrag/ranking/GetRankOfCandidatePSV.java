package de.ipbhalle.metfrag.ranking;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetRankOfCandidatePSV {

	public static ArrayList<String> scorePropertyNamesForLog = new ArrayList<String>(); 
	public static ArrayList<String> scorePropertyNamesForTakeRawValue = new ArrayList<String>(); 
	public static String delimiter = "\\|";
	public static String[] combinedReferenceScoreValues;
	public static boolean outputSortedList = false;
	
	static {
		scorePropertyNamesForLog.add("PubMed");
		scorePropertyNamesForLog.add("de.ipbhalle.metfrag.score.PubChemPatentScore");
		scorePropertyNamesForLog.add("de.ipbhalle.metfrag.score.PubChemPubMedReferenceScore");
		scorePropertyNamesForTakeRawValue.add("metlikeness");
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		if(args.length < 3) {
			System.out.println("usage: progname psv propname=value prop1=weight1 [prop2=weight2 ...] [true|false] [filename]");
			System.exit(1);
		}
		String resultPSVFilename = args[0];
		String toSearchPropertyName = args[1].trim().split("=")[0];
		String toSearchPropertyValue = args[1].trim().split("=")[1];
		String[] toSearchPropertyValues = toSearchPropertyValue.split("/");
		String filename = "";
		if(args[args.length - 2].trim().equals("true")) {
			outputSortedList = true;
			filename = args[args.length - 1].trim();
		}
		else if(args[args.length - 1].trim().equals("true")) {
			outputSortedList = true;
		}
		else outputSortedList = false;

		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultPSVFilename);
		LocalPSVDatabase db = new LocalPSVDatabase(settings);

		ArrayList<String> identifiers = null;
		try {
			identifiers = db.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CandidateList candidates = db.getCandidateByIdentifier(identifiers);
		
		ArrayList<String> scoringPropertyNames = new ArrayList<String>();
		HashMap<String, Double> scorePropertyToWeight = new HashMap<String, Double>();
		
		ArrayList<Integer> indexesOfCorrectMolecules = new ArrayList<Integer>();

		for(int i = 2; i < args.length; i++) {	
			String[] tmp = args[i].split("=");
			if(tmp.length == 1) continue;
			if(tmp[0].equals("CombinedReferenceScore")) {
				scorePropertyToWeight.put(tmp[0], Double.parseDouble(tmp[2]));
				combinedReferenceScoreValues = tmp[1].trim().split(",");
			}
			else 
				scorePropertyToWeight.put(tmp[0], Double.parseDouble(tmp[1]));
			scoringPropertyNames.add(tmp[0]);
		}

		double[] scorePropertyToMaximumValue = new double[scorePropertyToWeight.size()];
		HashMap<String, Double> inchiKeysToFinalScore = new HashMap<String, Double>();
		HashMap<String, String> inchiKeysToInChI = new HashMap<String, String>();
		int found = -1;
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			String currentInChIKey1 = (String)candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
			String currentPropertyValue = (String)candidates.getElement(i).getProperty(toSearchPropertyName);
			if(currentPropertyValue == null) continue;
			inchiKeysToFinalScore.put(currentInChIKey1, (double)Integer.MIN_VALUE);
			inchiKeysToInChI.put(currentInChIKey1, (String)candidates.getElement(i).getProperty(VariableNames.INCHI_NAME));
			
			for(int l = 0; l < toSearchPropertyValues.length; l++) {
				if(currentPropertyValue.compareTo(toSearchPropertyValues[l]) == 0) {
					found = l;
					indexesOfCorrectMolecules.add(i);
				}
			}
			for(int l = 0; l < scoringPropertyNames.size(); l++) {
				double curVal = 0.0;
				/*
				 * added for combined candidate score
				 */
				if(scoringPropertyNames.get(l).equals("CombinedReferenceScore")) {
					curVal = getCombinedScoreValue(candidates.getElement(i));
				}
				else {	
					curVal = Double.parseDouble((String)candidates.getElement(i).getProperty(scoringPropertyNames.get(l)));
				}
				if(scorePropertyNamesForLog.contains(scoringPropertyNames.get(l)) && curVal != 0.0) {
					curVal = Math.log(curVal);
				}
				if(scorePropertyToMaximumValue[l] < curVal) 
					scorePropertyToMaximumValue[l] = curVal;
			}
		}
		
		boolean[] negativeScore = new boolean[scoringPropertyNames.size()];
		
		for(int l = 0; l < scorePropertyToMaximumValue.length; l++) {
			if(scorePropertyToMaximumValue[l] == 0.0) 
				scorePropertyToMaximumValue[l] = 1.0;
			else if(scorePropertyToMaximumValue[l] < 0) {
				negativeScore[l] = true;
				scorePropertyToMaximumValue[l] = 1.0 / Math.abs(scorePropertyToMaximumValue[l]);
			}
		}

		if(found == -1 && !outputSortedList) {
			System.out.println(toSearchPropertyValue + " not found in " + resultPSVFilename);
			System.exit(0);
		}

		HashMap<String, Integer> inchikeyToIndex = new HashMap<String, Integer>();
		for(int j = 0; j < candidates.getNumberElements(); j++) {
			double curScore = 0.0;
			for(int l = 0; l < scoringPropertyNames.size(); l++) {
				double currentValue = 0.0;
				if(scoringPropertyNames.get(l).equals("CombinedReferenceScore")) {
					currentValue = getCombinedScoreValue(candidates.getElement(j));
					candidates.getElement(j).setProperty("CombinedReferenceScore", currentValue);
				}
				else currentValue = Double.parseDouble((String)candidates.getElement(j).getProperty(scoringPropertyNames.get(l)));
				if(!scorePropertyNamesForTakeRawValue.contains(scoringPropertyNames.get(l))) 
					curScore += (currentValue / scorePropertyToMaximumValue[l]) * scorePropertyToWeight.get(scoringPropertyNames.get(l));
				else if(negativeScore[l]) 
					curScore += (1.0 / Math.abs(currentValue)) * scorePropertyToWeight.get(scoringPropertyNames.get(l));
				else 
					curScore += (currentValue) * scorePropertyToWeight.get(scoringPropertyNames.get(l));
			}

			String currentInChIKey = (String)candidates.getElement(j).getProperty(VariableNames.INCHI_KEY_1_NAME);
				
			if(inchiKeysToFinalScore.containsKey(currentInChIKey)) {
				double value = inchiKeysToFinalScore.get(currentInChIKey);
				if(value < curScore) {
					inchiKeysToFinalScore.put(currentInChIKey, curScore);
					inchikeyToIndex.put(currentInChIKey, j);
				}
			}
			else {
				try {
					inchiKeysToFinalScore.put(currentInChIKey, curScore);
					inchikeyToIndex.put(currentInChIKey, j);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		int rank = 1;
		double wc = 0;
		double bc = 0;
		
		ArrayList<String> correctInChIKeys = new ArrayList<String>();
		for (int i = 0; i < indexesOfCorrectMolecules.size(); i++) {
			correctInChIKeys.add((String)candidates.getElement(indexesOfCorrectMolecules.get(i)).getProperty(VariableNames.INCHI_KEY_1_NAME));
		}
	
		int indexOfCorrect = -1; 
		String inchikeyMaximalScore = "";
		double scoreOfCorrect = (double)Integer.MIN_VALUE;
		for(int i = 0; i < correctInChIKeys.size(); i++) {
			double currentScore = inchiKeysToFinalScore.get(correctInChIKeys.get(i));
			if(currentScore > scoreOfCorrect) {
				inchikeyMaximalScore = correctInChIKeys.get(i);
				indexOfCorrect = inchikeyToIndex.get(correctInChIKeys.get(i));
				scoreOfCorrect = currentScore;
			}
		}
		
		for(String inchiKey : inchiKeysToFinalScore.keySet()) 
		{
			double currentScore = inchiKeysToFinalScore.get(inchiKey);
			if(currentScore > scoreOfCorrect) {
				rank++;
			}
			if(currentScore == scoreOfCorrect && !correctInChIKeys.contains(inchiKey)) rank++;
			if(currentScore > scoreOfCorrect) bc++;
            if(currentScore < scoreOfCorrect) wc++;
		}
		
		double rrp = 1.0;
		if(inchiKeysToFinalScore.size() != 1) 
			rrp = 0.5 * (1.0 - (bc - wc) / (double)((inchiKeysToFinalScore.size() - 1)));
		
		if(found != -1) {
			if(!outputSortedList) {
				String values = scoringPropertyNames.get(0) + "=" + candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames.get(0)) + "";
				String maximalValues = "Max" + scoringPropertyNames.get(0) + "=" + scorePropertyToMaximumValue[0];
				for(int i = 1; i < scoringPropertyNames.size(); i++) {
					values += " " + scoringPropertyNames.get(i) + "=" + candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames.get(i));
					maximalValues += " Max" + scoringPropertyNames.get(i) + "=" + scorePropertyToMaximumValue[i];
				}
				System.out.println(new File(resultPSVFilename).getName() + " " + toSearchPropertyValue + " " 
						+ rank + " " + (inchiKeysToFinalScore.size()) + " "
						+ candidates.getElement(indexOfCorrect).getProperty(VariableNames.IDENTIFIER_NAME) 
						+ " " + candidates.getElement(indexOfCorrect).getProperty("NoExplPeaks") 
						+ " " + candidates.getElement(indexOfCorrect).getProperty("NumberPeaksUsed")
						+ " " + rrp + " " + bc + " " + wc+ " " + values + " " + inchikeyMaximalScore + " " + maximalValues);
			}
			else {
				java.util.Iterator<?> it = inchiKeysToFinalScore.keySet().iterator();
				java.util.ArrayList<Double> scores = new java.util.ArrayList<Double>();
				java.util.ArrayList<String> inchikeys = new java.util.ArrayList<String>();
				while(it.hasNext()) {
					String currentInChIKey = (String)it.next();
					double currentScore = inchiKeysToFinalScore.get(currentInChIKey);
					int currentIndex = addSorted(scores, currentScore);
					inchikeys.add(currentIndex, currentInChIKey);
				}
				if(filename.length() == 0) {
					for(int i = 0; i < scores.size(); i++) {
						System.out.println(inchiKeysToInChI.get(inchikeys.get(i)) + " " + scores.get(i) + " " + inchikeys.get(i));
					}
				}
				else {
					java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
					for(int i = 0; i < scores.size(); i++) {
						bwriter.write(inchiKeysToInChI.get(inchikeys.get(i)) + " " + scores.get(i) + " " + inchikeys.get(i));
						bwriter.newLine();
					}
					bwriter.close();
				}
			}
		}
		else if(outputSortedList) { 
			java.util.Iterator<?> it = inchiKeysToFinalScore.keySet().iterator();
			java.util.ArrayList<Double> scores = new java.util.ArrayList<Double>();
			java.util.ArrayList<String> inchikeys = new java.util.ArrayList<String>();
			while(it.hasNext()) {
				String currentInChIKey = (String)it.next();
				double currentScore = inchiKeysToFinalScore.get(currentInChIKey);
				int currentIndex = addSorted(scores, currentScore);
				inchikeys.add(currentIndex, currentInChIKey);
			}
			if(filename.length() == 0) {
				for(int i = 0; i < scores.size(); i++) {
					System.out.println(inchiKeysToInChI.get(inchikeys.get(i)) + " " + scores.get(i) + " " + inchikeys.get(i));
				}
			}
			else {
				java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
				for(int i = 0; i < scores.size(); i++) {
					bwriter.write(inchiKeysToInChI.get(inchikeys.get(i)) + " " + scores.get(i) + " " + inchikeys.get(i));
					bwriter.newLine();
				}
				bwriter.close();
			}
		}
		else {
			System.out.println(toSearchPropertyValue + " not found in " + resultPSVFilename);
		}
		
	}

	/**
	 * 
	 * @param scores
	 * @param inchikeys
	 * @param correctInchiKey
	 * @return
	 */
	public static double[] calculateRankingValues(double[] scores, String[] inchikeys, String correctInchiKey) {
		int indexOfCorrect = -1;
		double scoreOfCorrect = -1.0;
		for(int i = 0; i < inchikeys.length; i++) {
			if(inchikeys[i].equals(correctInchiKey) && scoreOfCorrect < scores[i]) {
				indexOfCorrect = i;
				scoreOfCorrect = scores[i];
			}
		}
		if(indexOfCorrect == -1) {
			System.err.println(correctInchiKey + " not found!");
			return new double[] {0.0, 0.0, 0.0, 0.0, 0.0};
		}
		int rank = 1;
		int worse_candidates = 0;
		int better_candidates = 0;
		int equal_candidates = 0;
		int total_candidates = 1;
		for(int i = 0; i < scores.length; i++) {
			if(inchikeys[i].equals(correctInchiKey)) continue; 
			total_candidates++;
			if(scores[i] > scoreOfCorrect) {
				better_candidates++;
				rank++;
			}
			else if(scores[i] == scoreOfCorrect) {
				rank++;
				equal_candidates++;
			}
			else {
				worse_candidates++;
			}
		}
		double rrp = 1.0;
		if(total_candidates != 1) rrp = 0.5 * (1.0 - ((double)better_candidates - (double)worse_candidates) / ((double)total_candidates - 1.0));
		return new double[] {rank, better_candidates, equal_candidates, worse_candidates, total_candidates, rrp};
	}
	
	public static double getCombinedScoreValue(ICandidate candidate) {
		double value = 0.0;
		for(String prop : combinedReferenceScoreValues) {
			prop = prop.trim();
			if(candidate.getProperties().containsKey(prop)) {
				value += Double.parseDouble((String)candidate.getProperty(prop));
			}
		}
		return value;
	}
	
	/**
	 * 
	 * @param con
	 * @return
	 * @throws CDKException
	 */
	public static String calculateInchiKey(IAtomContainer con) throws CDKException {
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(con);
        CDKHydrogenAdder hAdder = CDKHydrogenAdder.getInstance(con.getBuilder());
        for(int i = 0; i < con.getAtomCount(); i++) {
        	try {
        		hAdder.addImplicitHydrogens(con, con.getAtom(i));
        	} 
        	catch(CDKException e) {
        		continue;
        	}
        }
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(con);
		InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance();
		InChIGenerator gen = factory.getInChIGenerator(con);
		return gen.getInchiKey().split("-")[0];
	}
	
	public static int addSorted(java.util.ArrayList<Double> scores, double score) {
		int index = 0;
		while(index < scores.size() && score < scores.get(index)) {
			index++;
		}
		scores.add(index, score);
		return index;
	}
}
