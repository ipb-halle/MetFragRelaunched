package de.ipbhalle.metfrag.ranking;

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.inchi.InChIGenerator;
import org.openscience.cdk.inchi.InChIGeneratorFactory;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.database.LocalSDFDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class GetRankOfCandidateSDF {

	public static Vector<String> scorePropertyNamesForLog = new Vector<String>(); 
	public static Vector<String> scorePropertyNamesForTakeRawValue = new Vector<String>(); 
	public static String delimiter = "\\|";
	
	static {
		scorePropertyNamesForLog.add("PubMed");
		scorePropertyNamesForLog.add("de.ipbhalle.metfrag.score.PubChemPatentScore");
		scorePropertyNamesForLog.add("de.ipbhalle.metfrag.score.PubChemPubMedReferenceScore");
		scorePropertyNamesForTakeRawValue.add("metlikeness");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length < 3) {
			System.out.println("usage: progname csv propname=value prop1=weight1 [prop2=weight2 ...]");
			System.exit(1);
		}
		String resultSDFFilename = args[0];
		String toSearchPropertyName = args[1].trim().split("=")[0];
		String toSearchPropertyValue = args[1].trim().split("=")[1];
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, resultSDFFilename);
		LocalSDFDatabase db = new LocalSDFDatabase(settings);
		
		Vector<String> identifiers = null;
		try {
			identifiers = db.getCandidateIdentifiers();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CandidateList candidates = db.getCandidateByIdentifier(identifiers);
		
		Vector<String> scoringPropertyNames = new Vector<String>();
		HashMap<String, Double> scorePropertyToWeight = new HashMap<String, Double>();
		
		Vector<Integer> indexesOfCorrectMolecules = new Vector<Integer>();
		
		for(int i = 2; i < args.length; i++) {	
			String[] tmp = args[i].split("=");
			scorePropertyToWeight.put(tmp[0], Double.parseDouble(tmp[1]));
			scoringPropertyNames.add(tmp[0]);
		}
		
		double[] scorePropertyToMaximumValue = new double[scorePropertyToWeight.size()];
		HashMap<String, Double> inchiKeysToFinalScore = new HashMap<String, Double>();
		
		boolean found = false;
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			String currentInChIKey1 = (String)candidates.getElement(i).getProperty(VariableNames.INCHI_KEY_1_NAME);
			String currentPropertyValue = (String)candidates.getElement(i).getProperty(toSearchPropertyName);
			if(currentPropertyValue == null) continue;
			inchiKeysToFinalScore.put(currentInChIKey1, 0.0);
			
			if(currentPropertyValue.compareTo(toSearchPropertyValue) == 0) {
				found = true;
				indexesOfCorrectMolecules.add(i);
			}
			for(int l = 0; l < scoringPropertyNames.size(); l++) {
				double curVal = Double.parseDouble((String)candidates.getElement(i).getProperty(scoringPropertyNames.get(l)));
				if(scorePropertyNamesForLog.contains(scoringPropertyNames.get(l)) && curVal != 0.0) {
					curVal = Math.log(curVal);
				}
				if(scorePropertyToMaximumValue[l] < curVal) scorePropertyToMaximumValue[l] = curVal;
			}
		}
		
		boolean[] negativeScore = new boolean[scoringPropertyNames.size()];
		
		for(int l = 0; l < scorePropertyToMaximumValue.length; l++) 
			if(scorePropertyToMaximumValue[l] == 0.0) 
				scorePropertyToMaximumValue[l] = 1.0;
			else if(scorePropertyToMaximumValue[l] < 0) {
				negativeScore[l] = true;
				scorePropertyToMaximumValue[l] = 1.0 / Math.abs(scorePropertyToMaximumValue[l]);
			}
		
		if(!found) {
			System.out.println(toSearchPropertyValue + " not found in " + resultSDFFilename);
			System.exit(0);
		}
		
		HashMap<String, Integer> inchikeyToIndex = new HashMap<String, Integer>();
		
		for(int j = 0; j < candidates.getNumberElements(); j++) {
			double curScore = 0.0;
			for(int l = 0; l < scoringPropertyNames.size(); l++) {
				double currentValue = Double.parseDouble((String)candidates.getElement(j).getProperty(scoringPropertyNames.get(l)));
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
		
		int rank = 0;
		double wc = 0;
		double bc = 0;
		
		String inchikeyOfCorrect = (String)candidates.getElement(indexesOfCorrectMolecules.get(0)).getProperty(VariableNames.INCHI_KEY_1_NAME);
		int indexOfCorrect = inchikeyToIndex.get(inchikeyOfCorrect);
		double scoreOfCorrect = inchiKeysToFinalScore.get(inchikeyOfCorrect);
		for(String inchiKey : inchiKeysToFinalScore.keySet()) {
			double currentScore = inchiKeysToFinalScore.get(inchiKey);
			if(currentScore >= scoreOfCorrect) rank++;
			if(currentScore > scoreOfCorrect) bc++;
            if(currentScore < scoreOfCorrect) wc++;
		}
		
		double rrp = 1.0;
		if(inchiKeysToFinalScore.size() != 1) 
			rrp = 0.5 * (1.0 - (bc - wc) / (double)((inchiKeysToFinalScore.size() - 1)));
		
		if(found) {
			
			String values = scoringPropertyNames.get(0) + "=" + candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames.get(0)) + "";
			for(int i = 1; i < scoringPropertyNames.size(); i++) {
				values += " " + scoringPropertyNames.get(i) + "=" + candidates.getElement(indexOfCorrect).getProperty(scoringPropertyNames.get(i));
			}
			System.out.println(new File(resultSDFFilename).getName() + " " + toSearchPropertyValue + " " 
					+ rank + " " + (inchiKeysToFinalScore.size()) + " "
					+ candidates.getElement(indexOfCorrect).getProperty(VariableNames.IDENTIFIER_NAME) 
					+ " " + candidates.getElement(indexOfCorrect).getProperty("NoExplPeaks") 
					+ " " + candidates.getElement(indexOfCorrect).getProperty("NumberPeaksUsed")
					+ " " + rrp + " " + bc + " " + wc+ " " + values);
		}
		else System.out.println(toSearchPropertyValue + " not found in " + resultSDFFilename);
		
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
}
