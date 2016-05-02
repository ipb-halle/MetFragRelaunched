package de.ipbhalle.metfraglib.similarity;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.IBitFingerprint;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.similarity.Tanimoto;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.PDistClusteringAlgorithm;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class TanimotoSimilarity {

	private static final MACCSFingerprinter fingerprinter = new MACCSFingerprinter();
	
	private IBitFingerprint[] fingerprints;
	
	public TanimotoSimilarity(IAtomContainer con) {
		IAtomContainer structure = AtomContainerManipulator.removeHydrogens(con);
		this.fingerprints = new IBitFingerprint[1];
		try {
			this.fingerprints[0] = fingerprinter.getBitFingerprint(structure);
		} catch (CDKException e) {
			this.fingerprints[0] = null;
		}
	}

	public TanimotoSimilarity(IAtomContainer[] cons) {
		this.fingerprints = new IBitFingerprint[cons.length];
		for(int i = 0; i < this.fingerprints.length; i++) 
		{
			IAtomContainer structure = AtomContainerManipulator.removeHydrogens(cons[i]);
			try {
				this.fingerprints[i] = fingerprinter.getBitFingerprint(structure);
			} catch (CDKException e) {
				this.fingerprints[i] = null;
			}
		}
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
		IAtomContainer con1 = AtomContainerManipulator.removeHydrogens(s1);
		IAtomContainer con2 = AtomContainerManipulator.removeHydrogens(s2);
		
		IBitFingerprint f1 = null;
		IBitFingerprint f2 = null;
		try {
			f1 = fingerprinter.getBitFingerprint(con1);
			f2 = fingerprinter.getBitFingerprint(con2);
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
	public static TanimotoClusterWrapper generateCluster(ICandidate[] candidates) throws Exception {
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
		return new TanimotoClusterWrapper(alg.performClustering(pdist, names, new AverageLinkageStrategy()));
	}

	/**
	 * 
	 * @param candidates
	 * @return
	 * @throws Exception
	 */
	public static TanimotoClusterWrapper generateCluster(CandidateList candidates) throws Exception {
		String[] names = new String[candidates.getNumberElements()];
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			if(candidates.getElement(i).getProperties().containsKey(VariableNames.FINGERPRINT_NAME_NAME) || candidates.getElement(i).getProperty(VariableNames.FINGERPRINT_NAME_NAME) == null) {
				candidates.getElement(i).setProperty(VariableNames.FINGERPRINT_NAME_NAME, TanimotoSimilarity.calculateFingerPrint(candidates.getElement(i).getAtomContainer()));
			}
			names[i] = candidates.getElement(i).getIdentifier();
		}

		int index = 0;
		double[] distValues = new double[(candidates.getNumberElements() * (candidates.getNumberElements() - 1)) / 2];
		System.out.println(distValues.length + " " + candidates.getNumberElements());
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
		
		for(int i = 0; i < distValues.length; i++)
			System.out.println(distValues[i]);
		
		ClusteringAlgorithm alg = new PDistClusteringAlgorithm();
		return new TanimotoClusterWrapper(alg.performClustering(pdist, names, new AverageLinkageStrategy()));
	}
	
	public static void main(String[] args) {
		String peakListFilePath = "/tmp/peaklist_file_example_1.txt";
		String candidateListFilePath = "/tmp/candidate_file_example_1.txt";
		de.ipbhalle.metfraglib.settings.MetFragGlobalSettings settings = new de.ipbhalle.metfraglib.settings.MetFragGlobalSettings();
		//set peaklist path and candidate list path
		settings.set(VariableNames.PEAK_LIST_PATH_NAME, peakListFilePath);
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, candidateListFilePath);
		//set needed parameters
		settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.001);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 253.966126);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "PubChem");
		
		de.ipbhalle.metfraglib.process.CombinedMetFragProcess metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
		
		try {
			metfragProcess.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CandidateList completeCanddiateList = metfragProcess.getCandidateList();
		CandidateList canddiateList = new CandidateList();
		
		
		for(int i = 0; i < completeCanddiateList.getNumberElements(); i++)
			canddiateList.addElement(completeCanddiateList.getElement(i));
		
	}
	
}
