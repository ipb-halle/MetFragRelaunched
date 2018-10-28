package de.ipbhalle.metfrag.hdx;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class CalculateHydrogenDeuteriumScore {

	public static double ppm = 5.0;
	public static double abs = 0.001;
	
	public static void main(String[] args) {

		if(args == null || args.length != 7) {
			System.err.println("hydrogenResultFileName deuteriumResultFileName maxNumDeuteriums outputFileName mzppm mzabs isPositive");
			System.exit(1);
		}
		
		String hydrogenResultFileName = args[0];
		String deuteriumResultFileName = args[1];
		double maximumNumberDeuteriums = Double.parseDouble(args[2]);
		String outputFileName = args[3];
		ppm = Double.parseDouble(args[4]);
		abs = Double.parseDouble(args[5]);
		boolean isPositive = args[6].trim().toLowerCase().equals("true") ? true : false;
		
		MetFragGlobalSettings hydrogenSettings = new MetFragGlobalSettings();
		MetFragGlobalSettings deuteriumSettings = new MetFragGlobalSettings();
		
		hydrogenSettings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, hydrogenResultFileName);
		deuteriumSettings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, deuteriumResultFileName);
		
		IDatabase hydrogenDatabase = new LocalPSVDatabase(hydrogenSettings);
		IDatabase deuteriumDatabase = new LocalPSVDatabase(deuteriumSettings);
	
		ArrayList<String> hydrogenIdentifiers = null;
		ArrayList<String> deuteriumIdentifiers = null;
		try {
			hydrogenIdentifiers = hydrogenDatabase.getCandidateIdentifiers();
			deuteriumIdentifiers = deuteriumDatabase.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CandidateList hydrogenCandidateList = null;
		CandidateList deuteriumCandidateList = null;
		try {
			hydrogenCandidateList = hydrogenDatabase.getCandidateByIdentifier(hydrogenIdentifiers);
			deuteriumCandidateList = deuteriumDatabase.getCandidateByIdentifier(deuteriumIdentifiers);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Hashtable<String, ICandidate> hashTableHydrogenResults = new Hashtable<String, ICandidate>(); 
		Hashtable<String, ICandidate> hashTableDeuteriumResults = new Hashtable<String, ICandidate>(); 

		for(int i = 0; i < hydrogenCandidateList.getNumberElements(); i++) {
			if(!hashTableHydrogenResults.containsKey(hydrogenCandidateList.getElement(i).getIdentifier())) 
				hashTableHydrogenResults.put(hydrogenCandidateList.getElement(i).getIdentifier(), hydrogenCandidateList.getElement(i));
			else {
				System.err.println("Error: Identifier " + hydrogenCandidateList.getElement(i).getIdentifier() + " already found! Something wrong with the candidate list?");
				System.exit(1);
			}
		}
		
		for(int i = 0; i < deuteriumCandidateList.getNumberElements(); i++) {
			if(!hashTableDeuteriumResults.containsKey(deuteriumCandidateList.getElement(i).getIdentifier())) {
				hashTableDeuteriumResults.put(deuteriumCandidateList.getElement(i).getIdentifier(), deuteriumCandidateList.getElement(i));
			}
			else {
				System.err.println("Error: Identifier " + deuteriumCandidateList.getElement(i).getIdentifier() + " already found! Something wrong with the candidate list?");
				System.exit(1);
			}
		}
		System.out.println("#####");
		Iterator<String> deuteriumKeys = hashTableDeuteriumResults.keySet().iterator();
		Hashtable<String, Double> hd_score_1 = new Hashtable<String, Double>();
		Hashtable<String, Double> hd_score_2 = new Hashtable<String, Double>();
		java.util.ArrayList<String> lines = new java.util.ArrayList<String>();
		
		CandidateList mergedCandidateList = new CandidateList();

		/*
		 * 
		 */
		while(deuteriumKeys.hasNext()) {
			String identifier = deuteriumKeys.next();
			boolean specialCase = false;
			if(identifier.matches(".*-.*")) {
				identifier = identifier.replaceAll("-.*", "");
				specialCase = true;
			}
			if(specialCase) {
				ICandidate hydrogenCandidate = hashTableHydrogenResults.get(identifier);
				int add = 1;
				String nextIdentifier = identifier + "-" + add;
				while(hashTableDeuteriumResults.containsKey(nextIdentifier)) {
					hashTableHydrogenResults.put(nextIdentifier, hydrogenCandidate);
					add++;
					nextIdentifier = identifier + "-" + add;
				}
			}
		}
		
		deuteriumKeys = hashTableDeuteriumResults.keySet().iterator();
		
 		while(deuteriumKeys.hasNext()) {
			
 			String identifier = deuteriumKeys.next();
 			
			ICandidate hydrogenCandidate = hashTableHydrogenResults.get(identifier);
			ICandidate deuteriumCandidate = hashTableDeuteriumResults.get(identifier);
 			
			String formulasOfExplPeaksHydrogenString = (String)hydrogenCandidate.getProperty("FormulasOfExplPeaks");
			String formulasOfExplPeaksDeuteriumString = (String)deuteriumCandidate.getProperty("FormulasOfExplPeaks");
			
			int numberPeaksUsed = Math.min(Integer.parseInt((String)deuteriumCandidate.getProperty("NumberPeaksUsed")), Integer.parseInt((String)hydrogenCandidate.getProperty("NumberPeaksUsed")));
			int numberPeaksExplained = Math.min(Integer.parseInt((String)deuteriumCandidate.getProperty("NoExplPeaks")), Integer.parseInt((String)hydrogenCandidate.getProperty("NoExplPeaks")));
			
			if(formulasOfExplPeaksHydrogenString.equals("NA") || formulasOfExplPeaksDeuteriumString.equals("NA")) {
				hd_score_1.put(identifier, 0.0);
				hd_score_2.put(identifier, 0.0);
				continue;
			}

			String[] tmpHydrogen =  formulasOfExplPeaksHydrogenString.split(";");
			String[] tmpDeuterium =  formulasOfExplPeaksDeuteriumString.split(";");
			
			double[] massesHydrogen = new double[tmpHydrogen.length];
			double[] massesDeuterium = new double[tmpDeuterium.length];
			String[] formulasHydrogen = new String[tmpHydrogen.length];
			String[] formulasDeuterium = new String[tmpDeuterium.length];

			for(int i = 0; i < massesHydrogen.length; i++) {
				String[] tmp = tmpHydrogen[i].split(":");
				massesHydrogen[i] = Double.parseDouble(tmp[0]);
				formulasHydrogen[i] = tmp[1];
			}

			for(int i = 0; i < massesDeuterium.length; i++) {
				String[] tmp = tmpDeuterium[i].split(":");
				massesDeuterium[i] = Double.parseDouble(tmp[0]);
				formulasDeuterium[i] = tmp[1];
			}
			
			int[][] equalPeakPairs = getEqualPeakPairs2(massesHydrogen, massesDeuterium, ppm, abs);
			int[][] deuteriumEqualPeakPairs = getDeuteriumEqualPeakPairs2(massesHydrogen, massesDeuterium, ppm, abs, maximumNumberDeuteriums);
			
			String fragmentAtomFastBitArraysHydrogenString = (String)hydrogenCandidate.getProperty("FragmentAtomFastBitArrays");
			String fragmentAtomFastBitArraysDeuteriumString = (String)deuteriumCandidate.getProperty("FragmentAtomFastBitArrays");
			
			String[] fragmentAtomFastBitArraysHydrogen = fragmentAtomFastBitArraysHydrogenString.split(";");
			String[] fragmentAtomFastBitArraysDeuterium = fragmentAtomFastBitArraysDeuteriumString.split(";");
			
			int countEqualPeakPairs = 0;
			int countDeuteriumEqualPeakPairs = 0;
			ArrayList<Integer> hydrogenMassesUsedForEqualPairs = new ArrayList<Integer>();
			ArrayList<Integer> deuteriumMassesUsedForEqualPairs = new ArrayList<Integer>();
			
			for(int i = 0; i < equalPeakPairs.length; i++) {
				String[] singleFragmentAtomFastBitArraysHydrogen = fragmentAtomFastBitArraysHydrogen[equalPeakPairs[i][0]].split("/");
				String[] singleFragmentAtomFastBitArraysDeuterium = fragmentAtomFastBitArraysDeuterium[equalPeakPairs[i][1]].split("/");

				String[] singleFormulasOfExplPeaksDeuteriumString = formulasDeuterium[equalPeakPairs[i][1]].split("/");
				for(int k = 0; k < singleFragmentAtomFastBitArraysDeuterium.length; k++) {
					int numDeuteriums = containsDeuterium(singleFormulasOfExplPeaksDeuteriumString[k]);
					if(!isPositive) numDeuteriums = containsDeuterium2(singleFormulasOfExplPeaksDeuteriumString[k]);
					boolean found = true;
					for(int l = 0; l < singleFragmentAtomFastBitArraysHydrogen.length; l++) {
						if(singleFragmentAtomFastBitArraysDeuterium[k].equals(singleFragmentAtomFastBitArraysHydrogen[l]) && numDeuteriums == 0 
								&& !hydrogenMassesUsedForEqualPairs.contains(equalPeakPairs[i][0])
								&& !deuteriumMassesUsedForEqualPairs.contains(equalPeakPairs[i][1])) {
							countEqualPeakPairs++;
							found = true;
							hydrogenMassesUsedForEqualPairs.add(equalPeakPairs[i][0]);
							deuteriumMassesUsedForEqualPairs.add(equalPeakPairs[i][1]);
							System.out.println("equal " + massesHydrogen[equalPeakPairs[i][0]] + " " + massesDeuterium[equalPeakPairs[i][1]]);
							break;
						}
						else {
						//	System.out.println("no pair " + massesHydrogen[equalPeakPairs[i][0]] + " " + massesDeuterium[equalPeakPairs[i][1]]);
						}
					}
					if(found) break;
				}
			}

			ArrayList<Integer> hydrogenMassesUsedForDeuteriumEqualPairs = new ArrayList<Integer>();
			ArrayList<Integer> deuteriumMassesUsedForDeuteriumEqualPairs = new ArrayList<Integer>();
			
			for(int i = 0; i < deuteriumEqualPeakPairs.length; i++) {
				String[] singleFragmentAtomFastBitArraysHydrogen = fragmentAtomFastBitArraysHydrogen[deuteriumEqualPeakPairs[i][0]].split("/");
				String[] singleFragmentAtomFastBitArraysDeuterium = fragmentAtomFastBitArraysDeuterium[deuteriumEqualPeakPairs[i][1]].split("/");
				String[] singleFormulasOfExplPeaksDeuteriumString = formulasDeuterium[deuteriumEqualPeakPairs[i][1]].split("/");
				for(int k = 0; k < singleFragmentAtomFastBitArraysDeuterium.length; k++) {
					int numDeuteriums = containsDeuterium2(singleFormulasOfExplPeaksDeuteriumString[k]);
					boolean found = false;
					for(int l = 0; l < singleFragmentAtomFastBitArraysHydrogen.length; l++) {
						if(singleFragmentAtomFastBitArraysDeuterium[k].equals(singleFragmentAtomFastBitArraysHydrogen[l]) 
								&& numDeuteriums > 0 
								&& numDeuteriums == deuteriumEqualPeakPairs[i][2]
								&& !hydrogenMassesUsedForEqualPairs.contains(deuteriumEqualPeakPairs[i][0])
								&& !deuteriumMassesUsedForEqualPairs.contains(deuteriumEqualPeakPairs[i][1])
								&& !hydrogenMassesUsedForDeuteriumEqualPairs.contains(deuteriumEqualPeakPairs[i][0])
								&& !deuteriumMassesUsedForDeuteriumEqualPairs.contains(deuteriumEqualPeakPairs[i][1])) 
						{
							countDeuteriumEqualPeakPairs++;
							hydrogenMassesUsedForDeuteriumEqualPairs.add(deuteriumEqualPeakPairs[i][0]);
							deuteriumMassesUsedForDeuteriumEqualPairs.add(deuteriumEqualPeakPairs[i][1]);
							System.out.println("deuterium " + massesHydrogen[deuteriumEqualPeakPairs[i][0]] + " " + massesDeuterium[deuteriumEqualPeakPairs[i][1]]);
							found = true;
							break;
						}
						else {
					//		System.out.println("no pair " + massesHydrogen[deuteriumEqualPeakPairs[i][0]] + " " + massesDeuterium[deuteriumEqualPeakPairs[i][1]]);
						}
					}
					if(found) break;
				}
			}

			deuteriumCandidate.setProperty(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME, hydrogenCandidate.getProperty(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME));
			deuteriumCandidate.setProperty("FragmenterDeuteriumScore", deuteriumCandidate.getProperty("Score"));
	//		hydrogenCandidate.setProperty("HD-PairScore", (double)(countEqualPeakPairs + countDeuteriumEqualPeakPairs) / (double)numberPeaksUsed);
			deuteriumCandidate.setProperty("HD-PairScore", (double)(countEqualPeakPairs + countDeuteriumEqualPeakPairs) / (double)numberPeaksExplained);
			deuteriumCandidate.setProperty("NumberEqualPairs", countEqualPeakPairs);
			deuteriumCandidate.setProperty("NumberDeuteriumPairs", countDeuteriumEqualPeakPairs);
			double osnDeuteriums = Double.parseDouble((String)deuteriumCandidate.getProperty("OSN-Deuteriums"));
			double missedDeuteriums = Double.parseDouble((String)deuteriumCandidate.getProperty("MissedDeuteriums"));
			if(isPositive) {
				if((maximumNumberDeuteriums - 1) == 0) deuteriumCandidate.setProperty("OSN-DeuteriumsScore", 0.0);
				else deuteriumCandidate.setProperty("OSN-DeuteriumsScore", (osnDeuteriums - missedDeuteriums) / (maximumNumberDeuteriums - 1));
			}
			else {
				if((maximumNumberDeuteriums + 1) == 0) deuteriumCandidate.setProperty("OSN-DeuteriumsScore", 0.0);
				else deuteriumCandidate.setProperty("OSN-DeuteriumsScore", (osnDeuteriums - missedDeuteriums) / (maximumNumberDeuteriums + 1));
			}
			deuteriumCandidate.getProperties().remove("FragmentBrokenBondFastBitArrays");
			deuteriumCandidate.getProperties().remove("FragmentBondFastBitArrays");
			deuteriumCandidate.getProperties().remove("FragmentAtomFastBitArrays");
			
			mergedCandidateList.addElement(deuteriumCandidate);
		//	System.out.println(identifier + " " + (double)(countEqualPeakPairs + countDeuteriumEqualPeakPairs) / (double)numberPeaksUsed + " " + countEqualPeakPairs + " " + countDeuteriumEqualPeakPairs + " " + (countEqualPeakPairs + countDeuteriumEqualPeakPairs) + " " + deuteriumCandidate.getProperty("AromaticDeuteriums") + " " + deuteriumCandidate.getProperty("Score") + " " + hydrogenCandidate.getProperty("Score"));
		
		/*	System.out.println(identifier + " " + (double)(countEqualPeakPairs + countDeuteriumEqualPeakPairs) / (double)numberPeaksUsed + " " + countEqualPeakPairs + " " + countDeuteriumEqualPeakPairs + " " + (countEqualPeakPairs + countDeuteriumEqualPeakPairs) + " " + deuteriumCandidate.getProperty("AromaticDeuteriums") + " " + deuteriumCandidate.getProperty("Score") + " " + hydrogenCandidate.getProperty("Score") 
					+ " " + hydrogenCandidate.getProperty("FragmenterDeuteriumScore") + " " + hydrogenCandidate.getProperty("HD-PairScore")
					+ " " + hydrogenCandidate.getProperty("OSN-DeuteriumsScore"));
			*/
			lines.add(identifier + " " + (double)(countEqualPeakPairs + countDeuteriumEqualPeakPairs) / (double)numberPeaksUsed + " " + countEqualPeakPairs + " " + countDeuteriumEqualPeakPairs + " " + (countEqualPeakPairs + countDeuteriumEqualPeakPairs) + " " + deuteriumCandidate.getProperty("AromaticDeuteriums") + " " + deuteriumCandidate.getProperty("Score") + " " + hydrogenCandidate.getProperty("Score") 
					+ " " + deuteriumCandidate.getProperty("FragmenterDeuteriumScore") + " " + deuteriumCandidate.getProperty("HD-PairScore")
					+ " " + deuteriumCandidate.getProperty("OSN-DeuteriumsScore"));
			
		}
		
		java.io.File outputFile = new java.io.File(outputFileName);
		CandidateListWriterPSV candidateWriter = new CandidateListWriterPSV();

		try {
			candidateWriter.write(mergedCandidateList, outputFile.getName(), outputFile.getParent());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @param massesHydrogen
	 * @param massesDeuterium
	 * @param ppm
	 * @param abs
	 * @return
	 */
	public static int[][] getEqualPeakPairs(double[] massesHydrogen, double[] massesDeuterium, double ppm, double abs) {
		ArrayList<Integer> equalPeakPairHydrogenIndeces = new ArrayList<Integer>();
		ArrayList<Integer> equalPeakPairDeuteriumIndeces = new ArrayList<Integer>();
		
		FastBitArray hydrogensAnnotated = new FastBitArray(massesHydrogen.length);
		FastBitArray deuteriumsAnnotated = new FastBitArray(massesDeuterium.length);
		
		double[] absDeviationHydrogens = new double[massesHydrogen.length];
		double[] absDeviationDeuterium = new double[massesDeuterium.length];
		
		for(int i = 0; i < massesHydrogen.length; i++) {
		
			double mzabs = MathTools.calculateAbsoluteDeviation(massesHydrogen[i], ppm) + abs;
			
			for(int j = 0; j < massesDeuterium.length; j++) {

				double deviation = Math.abs(massesHydrogen[i] - massesDeuterium[j]);
				
				if(deviation <= mzabs) {
					
					if(hydrogensAnnotated.get(i) && deuteriumsAnnotated.get(j)) continue;
					
					else if(hydrogensAnnotated.get(i)) {
						if(deviation < absDeviationHydrogens[i]) {
							int index = equalPeakPairHydrogenIndeces.indexOf(i);
							equalPeakPairHydrogenIndeces.remove(index);
							equalPeakPairDeuteriumIndeces.remove(index);
							equalPeakPairHydrogenIndeces.add(i);
							equalPeakPairDeuteriumIndeces.add(j);
							absDeviationHydrogens[i] = deviation;
							absDeviationDeuterium[j] = deviation;
						}
					}
					
					else if(deuteriumsAnnotated.get(j)) {
						if(deviation < absDeviationDeuterium[j]) {
							int index = equalPeakPairDeuteriumIndeces.indexOf(j);
							equalPeakPairHydrogenIndeces.remove(index);
							equalPeakPairDeuteriumIndeces.remove(index);
							equalPeakPairHydrogenIndeces.add(i);
							equalPeakPairDeuteriumIndeces.add(j);
							absDeviationHydrogens[i] = deviation;
							absDeviationDeuterium[j] = deviation;
						}
					}
					
					else {
						equalPeakPairHydrogenIndeces.add(i);
						equalPeakPairDeuteriumIndeces.add(j);
						absDeviationHydrogens[i] = deviation;
						absDeviationDeuterium[j] = deviation;
					}
					
					hydrogensAnnotated.set(i);
					deuteriumsAnnotated.set(j);	
				}
			
			}
		}

		int[][] pairedIndeces = new int[equalPeakPairHydrogenIndeces.size()][2];
		for(int i = 0; i < equalPeakPairHydrogenIndeces.size(); i++) {
			pairedIndeces[i][0] = equalPeakPairHydrogenIndeces.get(i);
			pairedIndeces[i][1] = equalPeakPairDeuteriumIndeces.get(i);
		}
		
		return pairedIndeces;
	}
	
	public static int[][] getEqualPeakPairs2(double[] massesHydrogen, double[] massesDeuterium, double ppm, double abs) {
		ArrayList<Integer> equalPeakPairHydrogenIndeces = new ArrayList<Integer>();
		ArrayList<Integer> equalPeakPairDeuteriumIndeces = new ArrayList<Integer>();
		
		for(int i = 0; i < massesHydrogen.length; i++) {
		
			double mzabs = MathTools.calculateAbsoluteDeviation(massesHydrogen[i], ppm) + abs;
			
			for(int j = 0; j < massesDeuterium.length; j++) {

				double deviation = Math.abs(massesHydrogen[i] - massesDeuterium[j]);
				
				if(deviation <= mzabs) {
				
					equalPeakPairHydrogenIndeces.add(i);
					equalPeakPairDeuteriumIndeces.add(j);
				
				}
			
			}
		}

		int[][] pairedIndeces = new int[equalPeakPairHydrogenIndeces.size()][2];
		for(int i = 0; i < equalPeakPairHydrogenIndeces.size(); i++) {
			pairedIndeces[i][0] = equalPeakPairHydrogenIndeces.get(i);
			pairedIndeces[i][1] = equalPeakPairDeuteriumIndeces.get(i);
		}
		
		return pairedIndeces;
	}
	/**
	 * 
	 * @param massesHydrogen
	 * @param massesDeuterium
	 * @param ppm
	 * @param abs
	 * @return
	 */
	public static int[][] getDeuteriumEqualPeakPairs(double[] massesHydrogen, double[] massesDeuterium, double ppm, double abs, double maximumNumberDeuteriums) {
		ArrayList<Integer> equalPeakPairHydrogenIndeces = new ArrayList<Integer>();
		ArrayList<Integer> equalPeakPairDeuteriumIndeces = new ArrayList<Integer>();
		ArrayList<Integer> numDeuteriumDiffs = new ArrayList<Integer>();
		
		FastBitArray hydrogensAnnotated = new FastBitArray(massesHydrogen.length);
		FastBitArray deuteriumsAnnotated = new FastBitArray(massesDeuterium.length);
		
		double[] absDeviationHydrogens = new double[massesHydrogen.length];
		double[] absDeviationDeuterium = new double[massesDeuterium.length];
		
		for(int i = 0; i < massesHydrogen.length; i++) {
			double mzabs = MathTools.calculateAbsoluteDeviation(massesHydrogen[i], ppm) + abs;
			
			for(double numDeuteriums = 1.0; numDeuteriums <= maximumNumberDeuteriums; numDeuteriums++) {
			
				for(int j = 0; j < massesDeuterium.length; j++) {

					double deviation = Math.abs(massesHydrogen[i] - (massesDeuterium[j] - (numDeuteriums * (Constants.getMonoisotopicMassOfAtom("D") - Constants.getMonoisotopicMassOfAtom("H")))));
					
					if(deviation <= mzabs) {
						if(hydrogensAnnotated.get(i) && deuteriumsAnnotated.get(j)) continue;
						
						else if(hydrogensAnnotated.get(i)) {
							if(deviation < absDeviationHydrogens[i]) {
								int index = equalPeakPairHydrogenIndeces.indexOf(i);
								deuteriumsAnnotated.set(equalPeakPairDeuteriumIndeces.get(index), false);
								equalPeakPairHydrogenIndeces.remove(index);
								equalPeakPairDeuteriumIndeces.remove(index);
								numDeuteriumDiffs.remove(index);
								equalPeakPairHydrogenIndeces.add(i);
								equalPeakPairDeuteriumIndeces.add(j);
								numDeuteriumDiffs.add((int)numDeuteriums);
								absDeviationHydrogens[i] = deviation;
								absDeviationDeuterium[j] = deviation;
								
							}
						}
						
						else if(deuteriumsAnnotated.get(j)) {
							if(deviation < absDeviationDeuterium[j]) {
								int index = equalPeakPairDeuteriumIndeces.indexOf(j);
								hydrogensAnnotated.set(equalPeakPairHydrogenIndeces.get(index), false);
								equalPeakPairHydrogenIndeces.remove(index);
								equalPeakPairDeuteriumIndeces.remove(index);
								numDeuteriumDiffs.remove(index);
								equalPeakPairHydrogenIndeces.add(i);
								equalPeakPairDeuteriumIndeces.add(j);
								numDeuteriumDiffs.add((int)numDeuteriums);
								absDeviationHydrogens[i] = deviation;
								absDeviationDeuterium[j] = deviation;
							}
						}

						else {
							equalPeakPairHydrogenIndeces.add(i);
							equalPeakPairDeuteriumIndeces.add(j);
							numDeuteriumDiffs.add((int)numDeuteriums);
							absDeviationHydrogens[i] = deviation;
							absDeviationDeuterium[j] = deviation;
						}
						
						hydrogensAnnotated.set(i);
						deuteriumsAnnotated.set(j);	
					}
				}
			}
		}
		
		int[][] pairedIndeces = new int[equalPeakPairHydrogenIndeces.size()][3];
		for(int i = 0; i < equalPeakPairHydrogenIndeces.size(); i++) {
			pairedIndeces[i][0] = equalPeakPairHydrogenIndeces.get(i);
			pairedIndeces[i][1] = equalPeakPairDeuteriumIndeces.get(i);
			pairedIndeces[i][2] = numDeuteriumDiffs.get(i);
			
			System.out.println(massesHydrogen[pairedIndeces[i][0]] + " " + massesDeuterium[pairedIndeces[i][1]]);
		}
		
		return pairedIndeces;
	}
	
	public static int[][] getDeuteriumEqualPeakPairs2(double[] massesHydrogen, double[] massesDeuterium, double ppm, double abs, double maximumNumberDeuteriums) {
		ArrayList<Integer> equalPeakPairHydrogenIndeces = new ArrayList<Integer>();
		ArrayList<Integer> equalPeakPairDeuteriumIndeces = new ArrayList<Integer>();
		ArrayList<Integer> numDeuteriumDiffs = new ArrayList<Integer>();
		
		for(int i = 0; i < massesHydrogen.length; i++) {
			double mzabs = MathTools.calculateAbsoluteDeviation(massesHydrogen[i], ppm) + abs;
			
			for(double numDeuteriums = 1.0; numDeuteriums <= maximumNumberDeuteriums; numDeuteriums++) {
			
				for(int j = 0; j < massesDeuterium.length; j++) {

					double deviation = Math.abs(massesHydrogen[i] - (massesDeuterium[j] - (numDeuteriums * (Constants.getMonoisotopicMassOfAtom("D") - Constants.getMonoisotopicMassOfAtom("H")))));
					
					if(deviation <= mzabs) {
						equalPeakPairHydrogenIndeces.add(i);
						equalPeakPairDeuteriumIndeces.add(j);
						numDeuteriumDiffs.add((int)numDeuteriums);
					}
					
				}
			}
		}
		
		int[][] pairedIndeces = new int[equalPeakPairHydrogenIndeces.size()][3];
		for(int i = 0; i < equalPeakPairHydrogenIndeces.size(); i++) {
			pairedIndeces[i][0] = equalPeakPairHydrogenIndeces.get(i);
			pairedIndeces[i][1] = equalPeakPairDeuteriumIndeces.get(i);
			pairedIndeces[i][2] = numDeuteriumDiffs.get(i);
		}
		
		return pairedIndeces;
	}
	
	private static int containsDeuterium2(String chargedFormula) {
		String charge = chargedFormula.replaceAll(".*]", "").replaceAll("-$", "").replaceAll("\\+$", "");
		int numDeuteriums = 0;
		if(charge.equals("-D")) numDeuteriums = -1;
		else if(charge.equals("+D")) numDeuteriums = +1;
		String _chargedFormula = chargedFormula.replaceAll("].*", "]");
		int i = 1;
		boolean chargeFound = false;
		while(_chargedFormula.charAt(i) != ']') {
			if(_chargedFormula.charAt(i) == '-' || _chargedFormula.charAt(i) == '+') {
				chargeFound = true;
				i++;
			}
			else if(_chargedFormula.charAt(i) == 'D' && !Character.isLowerCase(_chargedFormula.charAt(i + 1))) 
			{
				if(chargeFound) {
					int sign = 1;
					int indexBack = -1;
					String num = "";
					while(Character.isDigit(_chargedFormula.charAt(i + indexBack))) {
						num += _chargedFormula.charAt(i + indexBack);
						indexBack--;
					}
					if(_chargedFormula.charAt(i + indexBack) == '-') sign = -1;
					if(num.length() == 0) numDeuteriums += (1 * sign); 
					else numDeuteriums += (Integer.parseInt(num) * sign); 
					i++;
				}
				else {
					i++;
					String num = "";
					while(Character.isDigit(_chargedFormula.charAt(i))) {
						num += _chargedFormula.charAt(i);
						i++;
					}
					if(num.length() == 0) numDeuteriums += 1;
					else numDeuteriums += Integer.parseInt(num);
				}
			}
			else i++;
		}
		if(numDeuteriums < 0) {
			System.err.println("Error in formula: Number deuteriums less than 0.");
			System.err.println(chargedFormula);
			System.exit(1);
		}
		return numDeuteriums;
	}
	
	/**
	 * 
	 * @param chargedFormula
	 * @return
	 */
	private static int containsDeuterium(String chargedFormula) {
		String _chargedFormula = chargedFormula.replaceAll("].*", "]");
		int numDeuteriums = 0;
		int i = 1;
		boolean chargeFound = false;
		while(_chargedFormula.charAt(i) != ']') {
			if(_chargedFormula.charAt(i) == '-' || _chargedFormula.charAt(i) == '+') {
				chargeFound = true;
				i++;
			}
			else if(_chargedFormula.charAt(i) == 'D' && !Character.isLowerCase(_chargedFormula.charAt(i + 1))) 
			{
				if(chargeFound) {
					int sign = 1;
					int indexBack = -1;
					String num = "";
					while(Character.isDigit(_chargedFormula.charAt(i + indexBack))) {
						num += _chargedFormula.charAt(i + indexBack);
						indexBack--;
					}
					if(_chargedFormula.charAt(i + indexBack) == '-') sign = -1;
					if(num.length() == 0) numDeuteriums += (1 * sign); 
					else numDeuteriums += (Integer.parseInt(num) * sign); 
					i++;
				}
				else {
					i++;
					String num = "";
					while(Character.isDigit(_chargedFormula.charAt(i))) {
						num += _chargedFormula.charAt(i);
						i++;
					}
					if(num.length() == 0) numDeuteriums += 1;
					else numDeuteriums += Integer.parseInt(num);
				}
			}
			else i++;
		}
		if(numDeuteriums < 0) {
			System.err.println("Error in formula: Number deuteriums less than 0.");
			System.err.println(chargedFormula);
			System.exit(1);
		}
		return numDeuteriums;
	}
	
}
