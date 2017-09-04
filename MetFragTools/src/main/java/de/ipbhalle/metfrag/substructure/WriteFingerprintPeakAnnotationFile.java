package de.ipbhalle.metfrag.substructure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.FastBitArray;
import de.ipbhalle.metfraglib.database.LocalCSVDatabase;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.substructure.FingerprintGroup;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupList;
import de.ipbhalle.metfraglib.substructure.MassToFingerprintGroupListCollection;

public class WriteFingerprintPeakAnnotationFile {

	/*
	 * write annotation file
	 * 
	 * filename - input file name
	 * mzppm
	 * mzabs
	 * probtype - probability type: 0 - counts; 1 - P ( s | p ); 2 - P ( p | s ); 3 - P ( p , s ) from s; 4 - P ( p , s ) from p; 5 - P ( s | p ) P ( p | s ) P ( p , s )_s P ( p , s )_p
	 * occurThresh
	 * output
	 * csv
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
		String csv = "";
		if(readParameters.containsKey("output")) output = readParameters.get("output");
		if(readParameters.containsKey("occurThresh")) occurThresh = Integer.parseInt(readParameters.get("occurThresh"));
		if(readParameters.containsKey("csv")) csv = (String)readParameters.get("csv");
		
		ArrayList<Double> peakMassesSorted = new ArrayList<Double>();
		ArrayList<String> fingerprintsSorted = new ArrayList<String>();
		
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filename);
		IDatabase db = null;
		if(csv == "1") db = new LocalCSVDatabase(settings);
		else if (csv.equals("auto")) {
			if(filename.endsWith("psv")) db = new LocalPSVDatabase(settings);
			else db = new LocalCSVDatabase(settings);
		}
		else db = new LocalPSVDatabase(settings);
		java.util.ArrayList<String> ids = db.getCandidateIdentifiers();
		CandidateList candidateList = db.getCandidateByIdentifier(ids);
		//SmilesOfExplPeaks
		for(int i = 0; i < candidateList.getNumberElements(); i++) {
			ICandidate candidate = candidateList.getElement(i);
			String fingerprintsOfExplPeaks = (String)candidate.getProperty("FragmentFingerprintOfExplPeaks");
			if(fingerprintsOfExplPeaks.equals("NA") || fingerprintsOfExplPeaks.length() == 0) continue;
			fingerprintsOfExplPeaks = fingerprintsOfExplPeaks.trim();
			
			String[] fingerprintPairs = fingerprintsOfExplPeaks.split(";");
			
			for(int k = 0; k < fingerprintPairs.length; k++) {
				String[] tmp1 = fingerprintPairs[k].split(":");
				Double peak1 = Double.parseDouble(tmp1[0]);
				String fingerprint = null;
				try {
					fingerprint = tmp1[1];
					addSortedFeature(peak1, fingerprint, peakMassesSorted, fingerprintsSorted);
				}
				catch(Exception e) {
					continue;
				}
			}
		}
		
		MassToFingerprintGroupListCollection peakToFingerprintGroupListCollection = new MassToFingerprintGroupListCollection();
		//print(peakMassesSorted, fingerprintsSorted);
		System.out.println(peakMassesSorted.size() + " peak fingerprint pairs");

		Integer id = 0;
		Hashtable<Integer, ArrayList<Double>> grouplistid_to_masses = new Hashtable<Integer, ArrayList<Double>>();
		for(int i = 0; i < peakMassesSorted.size(); i++) {
			Double currentPeak = peakMassesSorted.get(i);
			MassToFingerprintGroupList peakToFingerprintGroupList = peakToFingerprintGroupListCollection.getElementByPeakInterval(currentPeak, mzppm, mzabs);
			if(peakToFingerprintGroupList == null) {
				peakToFingerprintGroupList = new MassToFingerprintGroupList(currentPeak);
				peakToFingerprintGroupList.setId(id);
				FingerprintGroup obj = new FingerprintGroup(0.0, null, null, null);
				obj.setFingerprint(fingerprintsSorted.get(i));
				obj.incrementNumberObserved();
				peakToFingerprintGroupList.addElement(obj);
				peakToFingerprintGroupListCollection.addElementSorted(peakToFingerprintGroupList);
				addMass(grouplistid_to_masses, id, currentPeak);
				id++;
			}
			else {
				Integer current_id = peakToFingerprintGroupList.getId();
				addMass(grouplistid_to_masses, current_id, currentPeak);
				FingerprintGroup fingerprintGroup = peakToFingerprintGroupList.getElementByFingerprint(new FastBitArray(fingerprintsSorted.get(i)));
				if(fingerprintGroup != null) {
					fingerprintGroup.incrementNumberObserved();
				}
				else {
					fingerprintGroup = new FingerprintGroup(0.0, null, null, null);
					fingerprintGroup.setFingerprint(fingerprintsSorted.get(i));
					fingerprintGroup.incrementNumberObserved();
					peakToFingerprintGroupList.addElement(fingerprintGroup);
				}
			}
		}
		System.out.println("before filtering " + peakToFingerprintGroupListCollection.getNumberElements());
		
		//peakToFingerprintGroupListCollection.updatePeakMass(mzppm, mzabs);
		peakToFingerprintGroupListCollection.updatePeakMass(grouplistid_to_masses);
	
		// test filtering
		if(occurThresh != null) peakToFingerprintGroupListCollection.filterByOccurence(occurThresh);
		
		peakToFingerprintGroupListCollection.annotateIds();
		//get absolute numbers of single substructure occurences
		//N^(s)
		int[] substrOccurences = peakToFingerprintGroupListCollection.calculateSubstructureAbsoluteProbabilities();
		int[] peakOccurences = peakToFingerprintGroupListCollection.calculatePeakAbsoluteProbabilities();

		//counts
		if(probabilityType == 0) {
			// calculate P ( s | p ) 
			peakToFingerprintGroupListCollection.updateConditionalProbabilities();
			peakToFingerprintGroupListCollection.setProbabilityToNumberObserved();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
		}
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
				bwriter.write("SUMMARY " + getNumberElements(peakToFingerprintGroupListCollection) + " " + getNumberOccurences(peakToFingerprintGroupListCollection));
				bwriter.newLine();
				bwriter.close();
			}
			
			peakToFingerprintGroupListCollection.setProbabilityToConditionalProbability_ps();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_2")));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.write("SUMMARY " + getNumberElements(peakToFingerprintGroupListCollection) + " " + getNumberOccurences(peakToFingerprintGroupListCollection));
				bwriter.newLine();
				bwriter.close();
			}

			peakToFingerprintGroupListCollection.setProbabilityToJointProbability();
			peakToFingerprintGroupListCollection.sortElementsByProbability();
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output + "_3")));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.write("SUMMARY " + getNumberElements(peakToFingerprintGroupListCollection) + " " + getNumberOccurences(peakToFingerprintGroupListCollection));
				bwriter.newLine();
				bwriter.close();
			}
		}

		if(probabilityType != 5) {
			if(output == null) peakToFingerprintGroupListCollection.print();
			else {
				System.out.println("writing to output");
				BufferedWriter bwriter = new BufferedWriter(new FileWriter(new File(output)));
				bwriter.write(peakToFingerprintGroupListCollection.toString());
				bwriter.write("SUMMARY " + getNumberElements(peakToFingerprintGroupListCollection) + " " + getNumberOccurences(peakToFingerprintGroupListCollection));
				bwriter.newLine();
				bwriter.close();
			}
		}
	}
	
	public static int getNumberElements(MassToFingerprintGroupListCollection peakToFingerprintGroupListCollections) {
		int count = 0;
		for(int i = 0; i < peakToFingerprintGroupListCollections.getNumberElements(); i++) {
			MassToFingerprintGroupList groupList = peakToFingerprintGroupListCollections.getElement(i);
			count += groupList.getNumberElements();
		}
		return count;
	}

	public static void addMass(Hashtable<Integer, ArrayList<Double>> grouplistid_to_masses, Integer id, double mass) {
		if(grouplistid_to_masses.containsKey(id) && grouplistid_to_masses.get(id) != null) {
			grouplistid_to_masses.get(id).add(mass);
		} else {
			ArrayList<Double> new_masses = new ArrayList<Double>();
			new_masses.add(mass);
			grouplistid_to_masses.put(id, new_masses);
		}
	}
	
	public static int getNumberOccurences(MassToFingerprintGroupListCollection peakToFingerprintGroupListCollections) {
		int count = 0;
		for(int i = 0; i < peakToFingerprintGroupListCollections.getNumberElements(); i++) {
			MassToFingerprintGroupList groupList = peakToFingerprintGroupListCollections.getElement(i);
			for(int j = 0; j < groupList.getNumberElements(); j++) {
				count += groupList.getElement(j).getNumberObserved();
			}
		}
		return count;
	}
	
	
	public static void addSortedFeature(double mass, String fingerprint, ArrayList<Double> masses, ArrayList<String> fingerprints) {
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
	
	public static void print(ArrayList<Double> peakMassesSorted, ArrayList<String> fingerprintsSorted) {
		for(int i = 0; i < peakMassesSorted.size(); i++) {
			System.out.println(peakMassesSorted.get(i) + " " + fingerprintsSorted.get(i));
		}
	}
	
}
