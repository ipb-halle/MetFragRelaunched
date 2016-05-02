package de.ipbhalle.metfraglib.tools;

import java.io.IOException;
import java.util.Vector;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.BitArrayPrecursor;
import de.ipbhalle.metfraglib.rsession.CommunityCalculation;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class CollectCommuniesAndFragmentPeaks {

	private static CollectCommuniesAndFragmentPeaks temp = new CollectCommuniesAndFragmentPeaks();
	private static double intensityThreshold = 100;
	
	public static void main(String[] args) {
		
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, args[0]);
		
		LocalPSVDatabase db = new LocalPSVDatabase(settings);
		
		java.util.Vector<String> ids = null;
		try {
			ids = db.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		CandidateList candidates = db.getCandidateByIdentifier(ids);
		
		Vector<Double> masses = new Vector<Double>();
		Vector<Vector<String>> vector_formulas = new Vector<Vector<String>>();
		Vector<Vector<String>> vector_smiles = new Vector<Vector<String>>();
		Vector<Vector<String>> vector_eawagids = new Vector<Vector<String>>();
		Vector<Vector<Double>> vector_intensities = new Vector<Vector<Double>>();		
		Vector<Integer> occurences = new Vector<Integer>();
		Vector<Vector<FingerPrintFragmentCollection>> matchingFragments = new Vector<Vector<FingerPrintFragmentCollection>>();
		
		for(int i = 0; i < candidates.getNumberElements(); i++) 
		{
		//	System.out.println(candidates.getElement(i).getIdentifier());
			try {
				candidates.getElement(i).initialisePrecursorCandidate();
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			CommunityCalculation c = new CommunityCalculation((BitArrayPrecursor)candidates.getElement(i).getPrecursorMolecule());
			DefaultBitArrayFragment[] communityFragments = c.getCommunityFragments();
			
			String[] formulas = ((String)candidates.getElement(i).getProperties().get("FormulasOfExplPeaks")).split(";");
			String eawagID = (String)candidates.getElement(i).getProperties().get("EawagID");
			String[] smiles = ((String)candidates.getElement(i).getProperties().get("SmilesOfExplPeaks")).split(";");
			String[] explPeaks = ((String)candidates.getElement(i).getProperties().get("ExplPeaks")).split(";");
			
			boolean debug = false;
			for(int k = 0; k < formulas.length; k++) {
				String[] tmp_formula = formulas[k].split(":");
				String[] tmp_smiles = smiles[k].split(":");
				String[] tmp_intensity = explPeaks[k].split("_");
				if(tmp_formula.length != 2 || tmp_smiles.length != 2 || tmp_intensity.length != 2)
					continue;
				double intensity = Double.parseDouble(tmp_intensity[1]);
				if(intensity < intensityThreshold) 
					continue;
				double mass = Double.parseDouble(tmp_formula[0]);
				
				int index = containsDouble(masses, mass, 5.0, 0.001, debug);
				
				if(index == -1) {
					int addedIndex = addMassSorted(masses, mass, debug);
					
					/*
					 * how often we have ssen this peak mass 
					 * here: only once as initial add
					 */
					occurences.add(addedIndex, 1);
					/*
					 * add the fragment
					 */
					addFragmentsAddPositionInitial(addedIndex, matchingFragments, communityFragments, candidates.getElement(i).getIdentifier());
					
					Vector<String> tmp_formulas = new Vector<String>();
					Vector<String> tmp_smiless = new Vector<String>();
					Vector<Double> tmp_intensities = new Vector<Double>();
					Vector<String> tmp_eawagids = new Vector<String>();
					
					tmp_formulas.add(tmp_formula[1]);
					tmp_smiless.add(tmp_smiles[1]);
					tmp_intensities.add(Double.parseDouble(tmp_intensity[1]));
					tmp_eawagids.add(eawagID);
					
					vector_formulas.add(addedIndex, tmp_formulas);
					vector_smiles.add(addedIndex, tmp_smiless);
					vector_intensities.add(addedIndex, tmp_intensities);
					vector_eawagids.add(addedIndex, tmp_eawagids);
				}
				else {
					masses.set(index, (masses.get(index) + mass) / 2.0);
					occurences.set(index, occurences.get(index) + 1);
					addFragmentsAddPosition(index, matchingFragments, communityFragments, candidates.getElement(i).getIdentifier());
					vector_formulas.get(index).add(tmp_formula[1]);
					vector_smiles.get(index).add(tmp_smiles[1]);
					vector_intensities.get(index).add(Double.parseDouble(tmp_intensity[1]));
					vector_eawagids.get(index).add(eawagID);
				}
			}
		}
		
		printMasses(masses);
		
		java.io.BufferedWriter bwriter;
		java.io.BufferedWriter smilesBwriter;
		
		try {
			smilesBwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[2])));
			bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[1])));
			for(int i = 0; i < masses.size(); i++) {
		//		System.out.println(masses.get(i));
				bwriter.write(masses.get(i) + "");
				bwriter.newLine();
				for(int k = 0; k < vector_formulas.get(i).size(); k++) {
					//		System.out.println("\t" + vector_eawagids.get(i).get(k) + ": " + vector_formulas.get(i).get(k) + " " + vector_smiles.get(i).get(k) + " " + vector_intensities.get(i).get(k));
					bwriter.write("\t" + vector_eawagids.get(i).get(k) + ": " + vector_formulas.get(i).get(k) + " " + vector_smiles.get(i).get(k) + " " + vector_intensities.get(i).get(k));
					smilesBwriter.write(vector_smiles.get(i).get(k));
					smilesBwriter.newLine();
					bwriter.newLine();
				}
				//	System.out.print("\t");
				bwriter.write("\t");
				for(int k = 0; k < vector_formulas.get(i).size(); k++) {
					//		System.out.print(vector_eawagids.get(i).get(k) + ".png ");
					bwriter.write(vector_eawagids.get(i).get(k) + ".png ");
				}
				//	System.out.println();
				bwriter.newLine();
				bwriter.newLine();
				//	System.out.println();
			}
			bwriter.close();
			smilesBwriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//	System.out.println("############### communities");
		
		try {
			java.io.BufferedWriter bwriterComms = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[3])));
		
			for(int i = 0; i < matchingFragments.size(); i++) {
				bwriterComms.write(masses.get(i) + " " + occurences.get(i));
				bwriterComms.newLine();
				Vector<FingerPrintFragmentCollection> collections = matchingFragments.get(i);
				for(int j = 0; j < collections.size(); j++) {
					FingerPrintFragmentCollection collection = collections.get(j);
					bwriterComms.write("\t");
					bwriterComms.write(collection.toString());
					bwriterComms.newLine();
				}
			}
			bwriterComms.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int addMassSorted(Vector<Double> masses, double mass, boolean debug) {
		int index = 0;
		while(index < masses.size()) 
		{
			if(mass > masses.get(index)) index++;
			else {
				if(debug) System.out.println("breaking " + mass + " < " + masses.get(index));
				break;
			}
		}
		masses.add(index, mass);
		return index;
	}
	
	/**
	 * 
	 * @param position
	 * @param matchedFragments
	 * @param communityFragments
	 */
	public static void addFragmentsAddPositionInitial(int position, Vector<Vector<FingerPrintFragmentCollection>> matchedFragments, DefaultBitArrayFragment[] communityFragments, String candidateIdentifier) {
		if(communityFragments == null || communityFragments.length == 0) return;
		CollectCommuniesAndFragmentPeaks.FingerPrintFragment fragment = temp.new FingerPrintFragment(communityFragments[0]);
		Vector<FingerPrintFragmentCollection> tmpVec = new Vector<FingerPrintFragmentCollection>();
		FingerPrintFragmentCollection newCollection = temp.new FingerPrintFragmentCollection(fragment, candidateIdentifier);
		tmpVec.add(newCollection);
		matchedFragments.add(position, tmpVec);
		for(int i = 1; i < communityFragments.length; i++) {
			Vector<FingerPrintFragmentCollection> collections = matchedFragments.get(position);
			CollectCommuniesAndFragmentPeaks.FingerPrintFragment newFragment = temp.new FingerPrintFragment(communityFragments[i]);
			boolean fragmentClassFound = false;
			for(int j = 0; j < collections.size(); j++) {
				FingerPrintFragmentCollection currentCollection = collections.get(j);
				if(currentCollection.matchesThreshold(newFragment, 0.95)) {
					currentCollection.addFragment(newFragment, candidateIdentifier);
					fragmentClassFound = true;
					break;
				}
 			}
			if(!fragmentClassFound) {
				FingerPrintFragmentCollection newCollection2 = temp.new FingerPrintFragmentCollection(newFragment, candidateIdentifier);
				collections.add(newCollection2);
			}
		}
	}
	
	public static void addFragmentsAddPosition(int position, Vector<Vector<FingerPrintFragmentCollection>> matchedFragments, DefaultBitArrayFragment[] communityFragments, String candidateIdentifier) {
		for(int i = 0; i < communityFragments.length; i++) {
			Vector<FingerPrintFragmentCollection> collections = matchedFragments.get(position);
			CollectCommuniesAndFragmentPeaks.FingerPrintFragment newFragment = temp.new FingerPrintFragment(communityFragments[i]);
			boolean fragmentClassFound = false;
			for(int j = 0; j < collections.size(); j++) {
				FingerPrintFragmentCollection currentCollection = collections.get(j);
				if(currentCollection.matchesThreshold(newFragment, 0.95)) {
					currentCollection.addFragment(newFragment, candidateIdentifier);
					fragmentClassFound = true;
					break;
				}
 			}
			if(!fragmentClassFound) {
				FingerPrintFragmentCollection newCollection = temp.new FingerPrintFragmentCollection(newFragment, candidateIdentifier);
				collections.add(newCollection);
			}
		}
	}
	
	
	private static void printMasses(Vector<Double> masses) {
		System.out.print("masses ");
		for(int i = 0; i < masses.size(); i++) {
			System.out.print(masses.get(i) + " ");
		}
		System.out.println();
	}
	
	
	public static int containsDouble(Vector<Double> masses, double value, double mzppm, double mzabs, boolean debug) {
		double dev = MathTools.calculateAbsoluteDeviation(value, mzppm);
		dev += mzabs;
		if(debug) {
			System.out.println("searching for " + value + " " + dev);
		}
		int bestMatch = -1;
		double lastDev = (double)Integer.MAX_VALUE;
		for(int i = 0; i < masses.size(); i++) 
		{
			if(debug) System.out.println((masses.get(i) - dev) + " " + value + " " + (masses.get(i) + dev));
			if(masses.get(i) - dev <= value && value <= masses.get(i) + dev) {
				double currentDev = Math.abs(masses.get(i) - value);
				if(currentDev < lastDev) {
					bestMatch = i;
					lastDev = currentDev;
				}
			}
			else if(masses.get(i) > value) break;
			
		}
		return bestMatch;
	}

	class FingerPrintFragment 
	{
		IBitFingerprint fingerprint;
		DefaultBitArrayFragment fragment;
		
		public FingerPrintFragment(DefaultBitArrayFragment fragment) {
			this.fragment = fragment;
			fingerprint = TanimotoSimilarity.calculateFingerPrint(fragment.getStructureAsIAtomContainer());
		}
		
		public DefaultBitArrayFragment getFragment() {
			return this.fragment;
		}
 		
		public IBitFingerprint getFingerprint() {
			return this.fingerprint;
		}
		
		public double calculateSimilarity(FingerPrintFragment fragment) {
			return TanimotoSimilarity.calculateSimilarity(this.fingerprint, fragment.getFingerprint());
		}
	}

	class FingerPrintFragmentCollection 
	{
		private Vector<FingerPrintFragment> collectedFragments;
		private Vector<String> originatingCanidateIdentifier;
		
		public FingerPrintFragmentCollection(FingerPrintFragment fragment, String identifier) {
			this.collectedFragments = new Vector<FingerPrintFragment>();
			this.originatingCanidateIdentifier = new Vector<String>();
			this.collectedFragments.add(fragment);
			this.originatingCanidateIdentifier.add(identifier);
		}
		
		public FingerPrintFragment getFragment(int index) {
			return this.collectedFragments.get(index);
		}

		public String getIdentifier(int index) {
			return this.originatingCanidateIdentifier.get(index);
		}
		
		public void addFragment(FingerPrintFragment fragment, String identifier) {
			this.collectedFragments.add(fragment);
			this.originatingCanidateIdentifier.add(identifier);
		}
		
		public int size() {
			return this.collectedFragments.size();
		}
		
		public void printFragmentCollection() {
			for(int i = 0; i < this.collectedFragments.size(); i++) {
				System.out.print(this.originatingCanidateIdentifier.get(i) + ":" + this.collectedFragments.get(i).getFragment().getSmiles() + " ");
			}
			System.out.println();
		}
		
		public String toString() {
			String string = "";
			for(int i = 0; i < this.collectedFragments.size(); i++) {
				string += this.originatingCanidateIdentifier.get(i) + ":" + this.collectedFragments.get(i).getFragment().getSmiles() + " ";
			}
			return string;
		}
		
		public boolean matchesThreshold(FingerPrintFragment fragment, double threshold) {
			for(int i = 0; i < this.collectedFragments.size(); i++) {
				double sim = this.collectedFragments.get(i).calculateSimilarity(fragment);
				if(sim >= threshold) {
					return true;
				}
			}
			return false;
		}
	}
}
