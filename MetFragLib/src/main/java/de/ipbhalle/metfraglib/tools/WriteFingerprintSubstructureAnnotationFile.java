package de.ipbhalle.metfraglib.tools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Vector;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.PeakToFingerprintGroupListCollection;

public class WriteFingerprintSubstructureAnnotationFile {

	/*
	 * write annotation file
	 * 
	 * filename - input file name
	 * mzppm
	 * mzabs
	 * probtype - probability type: 1 - P ( s | p ); 2 - P ( p | s ); 3 - P ( p , s ) from s; 4 - P ( p , s ) from p; 5 - P ( s | p ) P ( p | s ) P ( p , s )_s P ( p , s )_p
	 * output - output smarts
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
		Integer occurThresh = null;
		if(readParameters.containsKey("output")) output = readParameters.get("output");
		if(readParameters.containsKey("occurThresh")) occurThresh = Integer.parseInt(readParameters.get("occurThresh"));
		
		Vector<Double> peakMassesSorted = new Vector<Double>();
		Vector<String> fingerprintsSorted = new Vector<String>();
		
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		LocalPSVDatabase db = new LocalPSVDatabase(settings);
		java.util.Vector<String> ids = db.getCandidateIdentifiers();
		CandidateList candidateList = db.getCandidateByIdentifier(ids);
		//SmilesOfExplPeaks
		PeakToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new PeakToFingerprintGroupListCollection();
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			String fingerprintsOfExplPeaks = (String)candidate.getProperty("FragmentFingerprintOfExplPeaks");
			fingerprintsOfExplPeaks = fingerprintsOfExplPeaks.trim();
			String[] fingerprintPairs = fingerprintsOfExplPeaks.split(";");
			for(int k = 0; k < fingerprintPairs.length; k++) {
				String[] tmp = fingerprintPairs[k].split(":");
				Double peak = Double.parseDouble(tmp[0]);
				String fingerprint = null;
				try {
					fingerprint = tmp[1];
					addSortedFeature(peak, fingerprint, peakMassesSorted, fingerprintsSorted);
				}
				catch(Exception e) {
					continue;
				}
				
			}
		}
		for(int i = 0; i < peakMassesSorted.size(); i++) {
			Double currentPeak = peakMassesSorted.get(i);
			PeakToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeakInterval(currentPeak, mzppm, mzabs);
			if(peakToFingerprintGroupList == null) {
				peakToFingerprintGroupList = new PeakToFingerprintGroupList(currentPeak);
				FingerprintGroup obj = new FingerprintGroup(0.0, null, null, null);
				obj.setFingerprint(fingerprintsSorted.get(i));
				peakToFingerprintGroupList.addElement(obj);
				peakToFingerprintGroupListCollection.addElementSorted(peakToFingerprintGroupList);
			}
			else {
				FingerprintGroup fingerprintGroup = peakToFingerprintGroupList.getElementByFingerprint(fingerprintsSorted.get(i));
				if(fingerprintGroup != null) {
					fingerprintGroup.incerementNumberObserved();
				}
				else {
					fingerprintGroup = new FingerprintGroup(0.0, null, null, null);
					fingerprintGroup.setFingerprint(fingerprintsSorted.get(i));
					fingerprintGroup.incerementNumberObserved();
					peakToFingerprintGroupList.addElement(fingerprintGroup);
				}
			}
		}

		// test filtering
		if(occurThresh != null) peakToFingerprintGroupListCollection.filterByOccurence(occurThresh);
		
		peakToFingerprintGroupListCollection.annotateIds();
		//get absolute numbers of single substructure occurences
		//N^(s)
		int[] substrOccurences = peakToFingerprintGroupListCollection.calculateSubstructureAbsoluteProbabilities();
		int[] peakOccurences = peakToFingerprintGroupListCollection.calculatePeakAbsoluteProbabilities();
		
		//P ( s | p ) 
		if(probabilityType == 1) {
			// calculate P ( s | p ) 
			peakToFingerprintGroupListCollection.updateConditionalProbabilities();

			peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_sp();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
		}
		//P ( p | s ) 
		if(probabilityType == 2) {
			System.out.println("annotating IDs");
			// calculate P ( p | s ) 
			peakToFingerprintGroupListCollection.updateProbabilities(substrOccurences);
			
			peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_ps();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
		}
		
		//P ( p , s )_s 
		if(probabilityType == 3) {
			System.out.println("annotating IDs");
			// calculate P ( p , s ) 
			peakToFingerprintGroupListCollection.updateJointProbabilitiesWithSubstructures(substrOccurences);
			
			peakToFingerprintGroupListCollection.setProbabilityToJointProbability();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
		}

		//P ( p , s )_p
		if(probabilityType == 4) {
			System.out.println("annotating IDs");
			// calculate P ( p , s ) 
			peakToFingerprintGroupListCollection.updateJointProbabilitiesWithPeaks(peakOccurences);
			
			peakToFingerprintGroupListCollection.setProbabilityToJointProbability();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
		}
		
		//P ( s | p ) P ( p | s ) P( s, p )_s
		if(probabilityType == 5) {
			System.out.println("annotating IDs");
			peakToFingerprintGroupListCollection.updateConditionalProbabilities();
			peakToFingerprintGroupListCollection.updateProbabilities(substrOccurences);
			peakToFingerprintGroupListCollection.updateJointProbabilitiesWithSubstructures(substrOccurences);
			
			peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_sp();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_1")));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.close();
			}
			
			peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_ps();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_2")));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.close();
			}

			peakToFingerprintGroupListCollection.setProbabilityToJointProbability();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_3")));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.close();
			}
		}

		if(probabilityType != 5) {
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.close();
			}
		}
	}
	
	public static void addSortedFeature(double mass, String fingerprint, Vector<Double> masses, Vector<String> fingerprints) {
		int index = 0;
		while(index < masses.size() && masses.get(index) < mass) {
			index++;
		}
		masses.add(index, mass);
		fingerprints.add(index, fingerprint);
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
