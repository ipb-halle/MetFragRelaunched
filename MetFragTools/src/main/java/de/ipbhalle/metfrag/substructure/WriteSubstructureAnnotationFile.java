package de.ipbhalle.metfrag.substructure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.PeakToSmartsGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToSmartsGroupListCollection;
import de.ipbhalle.metfraglib.substructure.SmartsGroup;

public class WriteSubstructureAnnotationFile {

	/*
	 * write annotation file
	 * 
	 * filename - input file name
	 * mzppm
	 * mzabs
	 * probtype - probability type: 1 - P ( s | p ); 2 - P ( p | s ); 3 - P ( p , s ) from s; 4 - P ( p , s ) from p; 5 - P ( s | p ) P ( p | s ) P ( p , s )_s P ( p , s )_p
	 * output - output smarts
	 * outputSMILES - output smiles
	 * occurThresh
	 * 
	 */
	
	public static void main(String[] args) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		java.util.Hashtable<String, String> readParameters = readParameters(args);
		if(!readParameters.containsKey("filename")) {
			System.err.println("filename missing");
			System.exit(1);
		}
		if(!readParameters.containsKey("mzppm")) {
			System.err.println("mzppm missing");
			System.exit(1);
		}
		if(!readParameters.containsKey("mzabs")) {
			System.err.println("mzabs missing");
			System.exit(1);
		}
		if(!readParameters.containsKey("probtype")) {
			System.err.println("probtype missing");
			System.exit(1);
		}
		
		String filename = readParameters.get("filename");
		Double mzppm = Double.parseDouble(readParameters.get("mzppm"));
		Double mzabs = Double.parseDouble(readParameters.get("mzabs"));
		Integer probabilityType = Integer.parseInt(readParameters.get("probtype"));
		String output = null;
		String outputSmiles = null;
		Integer occurThresh = null;
		if(readParameters.containsKey("output")) output = readParameters.get("output");
		if(readParameters.containsKey("outputSMILES")) outputSmiles = readParameters.get("outputSMILES");
		if(readParameters.containsKey("occurThresh")) occurThresh = Integer.parseInt(readParameters.get("occurThresh"));
		
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		LocalPSVDatabase db = new LocalPSVDatabase(settings);
		java.util.Vector<String> ids = db.getCandidateIdentifiers();
		CandidateList candidateList = db.getCandidateByIdentifier(ids);
		//SmilesOfExplPeaks
		PeakToSmartsGroupListCollection peakToSmartGroupListCollection = new PeakToSmartsGroupListCollection();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			System.out.println(i);
			ICandidate candidate = candidateList.getElement(i);
			String smilesOfExplPeaks = (String)candidate.getProperty("SmilesOfExplPeaks");
			String aromaticSmilesOfExplPeaks = (String)candidate.getProperty("AromaticSmilesOfExplPeaks");
			smilesOfExplPeaks = smilesOfExplPeaks.trim();
			aromaticSmilesOfExplPeaks = aromaticSmilesOfExplPeaks.trim();
			if(smilesOfExplPeaks.equals("NA") || aromaticSmilesOfExplPeaks.equals("NA")) continue;
			String[] pairs = smilesOfExplPeaks.split(";");
			String[] aromaticPairs = aromaticSmilesOfExplPeaks.split(";");
			if(pairs.length != aromaticPairs.length) {
				System.out.println(candidate.getIdentifier() + " " + candidate.getProperty(VariableNames.INCHI_KEY_1_NAME));
				continue;
			}
			for(int k = 0; k < pairs.length; k++) {
				String[] tmp = pairs[k].split(":");
				String[] aromaticTmp = aromaticPairs[k].split(":");
				Double peak = Double.parseDouble(tmp[0]);
				String smiles = null;
				String smarts = null;
				try {
					smiles = tmp[1];
					smarts = aromaticTmp[1];
				}
				catch(Exception e) {
					continue;
				}
				PeakToSmartsGroupList peakToSmartGroupList = peakToSmartGroupListCollection.getElementByPeak(peak, mzppm, mzabs);
				if(peakToSmartGroupList == null) {
					peakToSmartGroupList = new PeakToSmartsGroupList(peak);
					SmartsGroup obj = new SmartsGroup(0.0, null, null, null);
					obj.addElement(smarts);
					obj.addSmiles(smiles);
					peakToSmartGroupList.addElement(obj);
					peakToSmartGroupListCollection.addElementSorted(peakToSmartGroupList);
				}
				else {
					peakToSmartGroupList.setPeakmz((peakToSmartGroupList.getPeakmz() + peak) / 2.0);
					SmartsGroup smartsGroup = peakToSmartGroupList.getElementBySmiles(smiles, 1.0);
					if(smartsGroup != null) {
						smartsGroup.addElement(smarts);
						smartsGroup.addSmiles(smiles);
					}
					else {
						smartsGroup = new SmartsGroup(0.0, null, null, null);
						smartsGroup.addElement(smarts);
						smartsGroup.addSmiles(smiles);
						peakToSmartGroupList.addElement(smartsGroup);
					}
				}
			}
		}

		// test filtering
		if(occurThresh != null) peakToSmartGroupListCollection.filterByOccurence(occurThresh);
		
		peakToSmartGroupListCollection.annotateIds();
		//get absolute numbers of single substructure occurences
		//N^(s)
		int[] substrOccurences = peakToSmartGroupListCollection.calculateSubstructureAbsoluteProbabilities();
		int[] peakOccurences = peakToSmartGroupListCollection.calculatePeakAbsoluteProbabilities();
		
		//P ( s | p ) 
		if(probabilityType == 1) {
			// calculate P ( s | p ) 
			peakToSmartGroupListCollection.updateConditionalProbabilities();

			peakToSmartGroupListCollection.removeDuplicates();
			peakToSmartGroupListCollection.setProbabilityToConditionalProbability_sp();
			peakToSmartGroupListCollection.sortElementsByProbability();
		}
		//P ( p | s ) 
		if(probabilityType == 2) {
			System.out.println("annotating IDs");
			// calculate P ( p | s ) 
			peakToSmartGroupListCollection.updateProbabilities(substrOccurences);
			
			peakToSmartGroupListCollection.removeDuplicates();
			peakToSmartGroupListCollection.setProbabilityToConditionalProbability_ps();
			peakToSmartGroupListCollection.sortElementsByProbability();
		}
		
		//P ( p , s )_s 
		if(probabilityType == 3) {
			System.out.println("annotating IDs");
			// calculate P ( p , s ) 
			peakToSmartGroupListCollection.updateJointProbabilitiesWithSubstructures(substrOccurences);
			
			peakToSmartGroupListCollection.removeDuplicates();
			peakToSmartGroupListCollection.setProbabilityToJointProbability();
			peakToSmartGroupListCollection.sortElementsByProbability();
		}

		//P ( p , s )_p
		if(probabilityType == 4) {
			System.out.println("annotating IDs");
			// calculate P ( p , s ) 
			peakToSmartGroupListCollection.updateJointProbabilitiesWithPeaks(peakOccurences);
			
			peakToSmartGroupListCollection.removeDuplicates();
			peakToSmartGroupListCollection.setProbabilityToJointProbability();
			peakToSmartGroupListCollection.sortElementsByProbability();
		}
		
		//P ( s | p ) P ( p | s ) P( s, p )_s
		if(probabilityType == 5) {
			System.out.println("annotating IDs");
			peakToSmartGroupListCollection.updateConditionalProbabilities();
			peakToSmartGroupListCollection.updateProbabilities(substrOccurences);
			peakToSmartGroupListCollection.updateJointProbabilitiesWithSubstructures(substrOccurences);

			peakToSmartGroupListCollection.removeDuplicates();
			
			peakToSmartGroupListCollection.setProbabilityToConditionalProbability_sp();
			peakToSmartGroupListCollection.sortElementsByProbability();
			if(output == null) peakToSmartGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_1")));
				bwriter.write(peakToSmartGroupListCollection.toString());
				bwriter.close();
			}
			if(outputSmiles != null) {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputSmiles + "_1")));
				bwriter.write(peakToSmartGroupListCollection.toStringSmiles());
				bwriter.close();
			}

			peakToSmartGroupListCollection.setProbabilityToConditionalProbability_ps();
			peakToSmartGroupListCollection.sortElementsByProbability();
			if(output == null) peakToSmartGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_2")));
				bwriter.write(peakToSmartGroupListCollection.toString());
				bwriter.close();
			}
			if(outputSmiles != null) {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputSmiles + "_2")));
				bwriter.write(peakToSmartGroupListCollection.toStringSmiles());
				bwriter.close();
			}

			peakToSmartGroupListCollection.setProbabilityToJointProbability();
			peakToSmartGroupListCollection.sortElementsByProbability();
			if(output == null) peakToSmartGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_3")));
				bwriter.write(peakToSmartGroupListCollection.toString());
				bwriter.close();
			}
			if(outputSmiles != null) {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputSmiles + "_3")));
				bwriter.write(peakToSmartGroupListCollection.toStringSmiles());
				bwriter.close();
			}
		}

		if(probabilityType != 5) {
			if(output == null) peakToSmartGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
				bwriter.write(peakToSmartGroupListCollection.toString());
				bwriter.close();
			}
			if(outputSmiles != null) {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(outputSmiles)));
				bwriter.write(peakToSmartGroupListCollection.toStringSmiles());
				bwriter.close();
			}
		}
	}
	
	public static java.util.Hashtable<String, String> readParameters(String[] params) {
		java.util.Hashtable<String, String> parameters = new java.util.Hashtable<String, String>();
		for(int i = 0; i < params.length; i++) {
			String param = params[i];
			String[] tmp = param.split("=");
			if(tmp.length != 2) continue;
			parameters.put(tmp[0], tmp[1]);
		}
		return parameters;
	}
}
