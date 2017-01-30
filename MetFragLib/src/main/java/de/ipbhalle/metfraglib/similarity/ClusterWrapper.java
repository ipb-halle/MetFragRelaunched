package de.ipbhalle.metfraglib.similarity;

import org.openscience.cdk.fingerprint.IBitFingerprint;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.PDistClusteringAlgorithm;

import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class ClusterWrapper {

	private Cluster cluster;
	
	public ClusterWrapper(Cluster cluster) {
		this.cluster = cluster;
	}
	
	public ClusterWrapper[] getChildren() {
		if(this.cluster.getChildren() == null) 
			return new ClusterWrapper[0];
		ClusterWrapper[] children = new ClusterWrapper[this.cluster.getChildren().size()];
		for(int i = 0; i < children.length; i++)
			children[i] = new ClusterWrapper(this.cluster.getChildren().get(i));
		return children;
	}
	
	public boolean isLeaf() {
		return this.cluster.getChildren() == null || this.cluster.getChildren().size() == 0;
	}
	
	public String getName() {
		return this.cluster.getName();
	}
	
	public Cluster getCluster() {
		return this.cluster;
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
		/*
		for(int i = 0; i < distValues.length; i++)
			System.out.println(distValues[i]);
		*/
		ClusteringAlgorithm alg = new PDistClusteringAlgorithm();
		return new ClusterWrapper(alg.performClustering(pdist, names, new AverageLinkageStrategy()));
	}
	
	public static void main(String[] args) {
		String peakListFilePath = "/tmp/peaklist_file_example_1.txt";

		de.ipbhalle.metfraglib.settings.MetFragGlobalSettings settings = new de.ipbhalle.metfraglib.settings.MetFragGlobalSettings();
		//set peaklist path and candidate list path
		settings.set(VariableNames.PEAK_LIST_PATH_NAME, peakListFilePath);
		//set needed parameters
		settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, 5.0);
		settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 0.001);
		settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 253.966126);
		settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, "PubChem");
		settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, new String[] {"50465", "57010914", "56974741", "88419651", "44290588"});
		de.ipbhalle.metfraglib.process.CombinedMetFragProcess metfragProcess = new de.ipbhalle.metfraglib.process.CombinedMetFragProcess(settings);
		
		try {
			metfragProcess.retrieveCompounds();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		CandidateList completeCanddiateList = metfragProcess.getCandidateList();
		CandidateList candidiateList = new CandidateList();
		
		
		for(int i = 0; i < completeCanddiateList.getNumberElements(); i++)
			candidiateList.addElement(completeCanddiateList.getElement(i));
		
		System.out.println("generating cluster");
		try {
			ClusterWrapper clusterWrapper = TanimotoSimilarity.generateCluster(candidiateList);
			ClusterWrapper[] cw = clusterWrapper.getChildren();
			System.out.println(cw.length + " children");
			
			java.util.Stack<ClusterWrapper> clusterStack = new java.util.Stack<ClusterWrapper>();
			java.util.Stack<String> tabStack = new java.util.Stack<String>();
			clusterStack.push(clusterWrapper);
			tabStack.push("");
			
			while(!clusterStack.isEmpty()) {
				ClusterWrapper current = clusterStack.pop();
				String currenTab = tabStack.pop();
				ClusterWrapper[] children = current.getChildren();
				for(ClusterWrapper child : children) {
					clusterStack.push(child);
					tabStack.push(currenTab + " ");
				}
				System.out.println(currenTab + " " + current.getName());
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
