package de.ipbhalle.metfraglib.fingerprint;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.similarity.Tanimoto;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.PDistClusteringAlgorithm;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class TanimotoSimilarity {

	protected static final MACCSFingerprinter fingerprinter = new MACCSFingerprinter();
	
	protected IBitFingerprint[] fingerprints;
	
	public TanimotoSimilarity(IAtomContainer con) {
		this.fingerprints = new IBitFingerprint[1];
		try {
			this.fingerprints[0] = fingerprinter.getBitFingerprint(con);
		} catch (CDKException e) {
			this.fingerprints[0] = null;
		}
	}

	public TanimotoSimilarity(IAtomContainer[] cons) {
		this.fingerprints = new IBitFingerprint[cons.length];
		for(int i = 0; i < this.fingerprints.length; i++) 
		{
			try {
				this.fingerprints[i] = fingerprinter.getBitFingerprint(cons[i]);
			} catch (CDKException e) {
				this.fingerprints[i] = null;
			}
		}
	}

	public IBitFingerprint getFingerPrint(int index) {
		return this.fingerprints[index];
	}
	
	public double[] calculateSimilarities(IAtomContainer con) {
		IBitFingerprint fingerprint = null;
		try {
			fingerprint = fingerprinter.getBitFingerprint(con);
		} catch (CDKException e) {
			e.printStackTrace();
		}
		double[] similarities = new double[this.fingerprints.length];
		for(int i = 0; i < similarities.length; i++) {
			try {
				similarities[i] = Tanimoto.calculate(this.fingerprints[i], fingerprint);
			}
			catch(Exception e) {
				similarities[i] = 0.0;
			}
		}
		return similarities;
	}
	
	public static IBitFingerprint calculateFingerPrint(IAtomContainer s1) {
		IBitFingerprint f1 = null;
		try {
			f1 = fingerprinter.getBitFingerprint(s1);
		} catch (CDKException e) {
			f1 = null;
		}
		return f1;
	}
	
	public static double calculateSimilarity(IBitFingerprint f1, IBitFingerprint f2) {
		if(f1 == null || f2 == null) return 0.0;
		return Tanimoto.calculate(f1, f2);
	}
	
	public static double calculateSimilarity(IAtomContainer s1, IAtomContainer s2) {
		IBitFingerprint f1 = null;
		IBitFingerprint f2 = null;
		try {
			f1 = fingerprinter.getBitFingerprint(s1);
			f2 = fingerprinter.getBitFingerprint(s2);
		} catch (CDKException e) {
			return 0.0;
		}
		
		return Tanimoto.calculate(f1, f2);
	}
	
	/**
	 * 
	 * @param candidates
	 * @return
	 * @throws Exception
	 */
	public static ClusterWrapper generateCluster(ICandidate[] candidates) throws Exception {
		String[] names = new String[candidates.length];
		for(int i = 0; i < candidates.length; i++) {
			if(candidates[i].getProperties().containsKey(VariableNames.FINGERPRINT_NAME_NAME) || candidates[i].getProperty(VariableNames.FINGERPRINT_NAME_NAME) == null) {
				candidates[i].setProperty(VariableNames.FINGERPRINT_NAME_NAME, TanimotoSimilarity.calculateFingerPrint(candidates[i].getAtomContainer()));
			}
			names[i] = candidates[i].getIdentifier();
		}
		double[] distValues = new double[(candidates.length * (candidates.length - 1)) / 2];
		
		int index = 0;
		for(int col = 0; col < candidates.length; col++) {
			for(int row = col + 1; row < candidates.length; row++) {
				double distValue = 
						1.0 - TanimotoSimilarity.calculateSimilarity(
								(IBitFingerprint)candidates[row].getProperty(VariableNames.FINGERPRINT_NAME_NAME), 
								(IBitFingerprint)candidates[col].getProperty(VariableNames.FINGERPRINT_NAME_NAME));
				distValues[index] = distValue;
				index++;
			}
		}
		
		double[][] pdist = new double[][] {
				distValues
	    };

		ClusteringAlgorithm alg = new PDistClusteringAlgorithm();
		return new ClusterWrapper(alg.performClustering(pdist, names, new AverageLinkageStrategy()));
	}


	/**
	 * 
	 * @param candidates
	 * @return
	 * @throws Exception
	 */
	public static ClusterWrapper generateCluster(CandidateList candidates) throws Exception {
		String[] names = new String[candidates.getNumberElements()];
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			if(candidates.getElement(i).getProperties().containsKey(VariableNames.FINGERPRINT_NAME_NAME) || candidates.getElement(i).getProperty(VariableNames.FINGERPRINT_NAME_NAME) == null) {
				candidates.getElement(i).setProperty(VariableNames.FINGERPRINT_NAME_NAME, TanimotoSimilarity.calculateFingerPrint(candidates.getElement(i).getAtomContainer()));
			}
			names[i] = candidates.getElement(i).getIdentifier();
		}
		double[] distValues = new double[(candidates.getNumberElements() * (candidates.getNumberElements() - 1)) / 2];
		
		int index = 0;
		for(int col = 0; col < candidates.getNumberElements(); col++) {
			for(int row = col + 1; row < candidates.getNumberElements(); row++) {
				double distValue = 
						1.0 - TanimotoSimilarity.calculateSimilarity(
								(IBitFingerprint)candidates.getElement(row).getProperty(VariableNames.FINGERPRINT_NAME_NAME), 
								(IBitFingerprint)candidates.getElement(col).getProperty(VariableNames.FINGERPRINT_NAME_NAME));
				distValues[index] = distValue;
				index++;
			}
		}
		
		double[][] pdist = new double[][] {
				distValues
	    };

		ClusteringAlgorithm alg = new PDistClusteringAlgorithm();
		return new ClusterWrapper(alg.performClustering(pdist, names, new AverageLinkageStrategy()));
	}
	
	/**
	 * first list is the candidate list for which each candidate gets its maximum similarity
	 * 
	 * @param candidates1
	 * @param candidates2
	 * @return
	 * @throws Exception
	 */
	public static String[][] getMaximumSimilarities(CandidateList candidates1, CandidateList candidates2, Hashtable<String, String> names) throws Exception {
		String[] names1 = new String[candidates1.getNumberElements()];
		String[] names2 = new String[candidates2.getNumberElements()];
		for(int i = 0; i < candidates1.getNumberElements(); i++) {
			if(candidates1.getElement(i).getProperties().containsKey(VariableNames.FINGERPRINT_NAME_NAME) || candidates1.getElement(i).getProperty(VariableNames.FINGERPRINT_NAME_NAME) == null) {
				candidates1.getElement(i).setProperty(VariableNames.FINGERPRINT_NAME_NAME, TanimotoSimilarity.calculateFingerPrint(candidates1.getElement(i).getAtomContainer()));
			}
			names1[i] = candidates1.getElement(i).getIdentifier();
		}
		for(int i = 0; i < candidates2.getNumberElements(); i++) {
			if(candidates2.getElement(i).getProperties().containsKey(VariableNames.FINGERPRINT_NAME_NAME) || candidates2.getElement(i).getProperty(VariableNames.FINGERPRINT_NAME_NAME) == null) {
				candidates2.getElement(i).setProperty(VariableNames.FINGERPRINT_NAME_NAME, TanimotoSimilarity.calculateFingerPrint(candidates2.getElement(i).getAtomContainer()));
			}
			names2[i] = candidates2.getElement(i).getIdentifier();
		}
		
		String[][] maximumSimValues = new String[candidates1.getNumberElements()][2];
		
		for(int col = 0; col < candidates1.getNumberElements(); col++) {
			double maxSimValue = 0.0;
			for(int row = 0; row < candidates2.getNumberElements(); row++) {
				double currentSimValue = 
						TanimotoSimilarity.calculateSimilarity(
								(IBitFingerprint)candidates1.getElement(col).getProperty(VariableNames.FINGERPRINT_NAME_NAME), 
								(IBitFingerprint)candidates2.getElement(row).getProperty(VariableNames.FINGERPRINT_NAME_NAME));
				if(maxSimValue < currentSimValue) maxSimValue = currentSimValue;
			}
			maximumSimValues[col][0] = (String)names.get(candidates1.getElement(col).getProperty("Identifier"));
			maximumSimValues[col][1] = String.valueOf(maxSimValue);
		}
		
		return maximumSimValues;
	}

	public static void main(String[] args) {
		de.ipbhalle.metfraglib.settings.MetFragGlobalSettings settings = new de.ipbhalle.metfraglib.settings.MetFragGlobalSettings();
		//set peaklist path and candidate list path
		String training = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/ids_training.txt";
		String testing = "/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/ids_testing.txt";
		
		java.util.ArrayList<String> ids_training_pos = new java.util.ArrayList<String>();
		java.util.ArrayList<String> ids_training_neg = new java.util.ArrayList<String>();
		java.util.ArrayList<String> ids_testing_pos = new java.util.ArrayList<String>();
		java.util.ArrayList<String> ids_testing_neg = new java.util.ArrayList<String>();
		
		java.util.Hashtable<String, String> names_pos = new java.util.Hashtable<String, String>();
		java.util.Hashtable<String, String> names_neg = new java.util.Hashtable<String, String>();
		
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(training)));
			String line = "";
			while((line = breader.readLine()) != null) {
				String[] tmp = line.split("\\s+");
				if(tmp[0].matches(".*01\\.txt.*")) ids_training_pos.add(tmp[1].trim());
				else {
					ids_training_neg.add(tmp[1].trim());
				}
			}
			breader.close();
			breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(testing)));
			line = "";
			while((line = breader.readLine()) != null) {
				String[] tmp = line.split("\\s+");
				if(tmp[0].matches(".*01\\.txt.*")) {
					ids_testing_pos.add(tmp[1].trim());
					names_pos.put(tmp[1], tmp[0]);
				}
				else {
					ids_testing_neg.add(tmp[1].trim());
					names_neg.put(tmp[1], tmp[0]);
				}
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String ids_training_pos_string = ids_training_pos.get(0);
		String ids_training_neg_string = ids_training_neg.get(0);
		
		for(int i = 1; i < ids_training_pos.size(); i++) ids_training_pos_string += "," + ids_training_pos.get(i);
		for(int i = 1; i < ids_training_neg.size(); i++) ids_training_neg_string += "," + ids_training_neg.get(i);
		
		String ids_testing_pos_string = ids_testing_pos.get(0);
		String ids_testing_neg_string = ids_testing_neg.get(0);
		
		for(int i = 1; i < ids_testing_pos.size(); i++) ids_testing_pos_string += "," + ids_testing_pos.get(i);
		for(int i = 1; i < ids_testing_neg.size(); i++) ids_testing_neg_string += "," + ids_testing_neg.get(i);
		
		
		
		settings.set(VariableNames.PEAK_LIST_PATH_NAME, "/tmp/spec.txt");
		//set needed parameters
		settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.001);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 253.966126);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "PubChem");
		
		CandidateList ids_training_pos_candidates = null;
		CandidateList ids_training_neg_candidates = null;
		CandidateList ids_testing_pos_candidates = null;
		CandidateList ids_testing_neg_candidates = null;
		
		try {
			// ids_training_pos_candidates
			settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids_training_pos_string.split(","));
			de.ipbhalle.metfraglib.process.CombinedMetFragProcess metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
			metfragProcess.retrieveCompounds();
			ids_training_pos_candidates = metfragProcess.getCandidateList();

			// ids_training_neg_candidates
			settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids_training_neg_string.split(","));
			metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
			metfragProcess.retrieveCompounds();
			ids_training_neg_candidates = metfragProcess.getCandidateList();
			
			// ids_testing_pos_candidates
			settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids_testing_pos_string.split(","));
			metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
			metfragProcess.retrieveCompounds();
			ids_testing_pos_candidates = metfragProcess.getCandidateList();
			

			// ids_testing_neg_candidates
			settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids_testing_neg_string.split(","));
			metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
			metfragProcess.retrieveCompounds();
			ids_testing_neg_candidates = metfragProcess.getCandidateList();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			String[][] maxSimPos = TanimotoSimilarity.getMaximumSimilarities(ids_testing_pos_candidates, ids_training_pos_candidates, names_pos);
			String[][] maxSimNeg = TanimotoSimilarity.getMaximumSimilarities(ids_testing_neg_candidates, ids_training_neg_candidates, names_neg);
			System.out.println(maxSimPos.length + " " + maxSimNeg.length);
			java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File("/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/maximum_similarities_pos.txt")));
			for(int i = 0; i < maxSimPos.length; i++) {
				bwriter.write(maxSimPos[i][0] + " " + maxSimPos[i][1]);
				bwriter.newLine();
			}
			bwriter.close();
			bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File("/home/cruttkie/Dokumente/PhD/MetFrag/substructure_training/ufz_train_eawag_test/maximum_similarities_neg.txt")));
			for(int i = 0; i < maxSimNeg.length; i++) {
				bwriter.write(maxSimNeg[i][0] + " " + maxSimNeg[i][1]);
				bwriter.newLine();
			}
			bwriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
