package de.ipbhalle.metfragweb.helper;

import com.apporiented.algorithm.clustering.AverageLinkageStrategy;
import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;

import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;
import de.ipbhalle.metfragweb.container.MetFragResultsContainer;

public class ClusterCompoundsThreadRunner extends ThreadRunner {
	
	private MetFragResultsContainer filteredMetFragResultsContainer; 
	
	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages) {
		super(beanSettingsContainer, infoMessages, errorMessages);
	}

	public ClusterCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages, MetFragResultsContainer filteredMetFragResultsContainer) {
		super(beanSettingsContainer, infoMessages, errorMessages);
		this.filteredMetFragResultsContainer = filteredMetFragResultsContainer;
	}
	
	@Override
	public void run() {
		System.out.println(this.filteredMetFragResultsContainer);
	}
	
	public static void main(String[] args) {
		String[] names = new String[] { "O1", "O2", "O3", "O4", "O5", "O6" };
		double[][] distances = new double[][] { 
		    { 0, 1, 9, 7, 11, 14 },
		    { 1, 0, 4, 3, 8, 10 }, 
		    { 9, 4, 0, 9, 2, 8 },
		    { 7, 3, 9, 0, 6, 13 }, 
		    { 11, 8, 2, 6, 0, 10 },
		    { 14, 10, 8, 13, 10, 0 }};

		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster cluster = alg.performClustering(distances, names, new AverageLinkageStrategy());
		
		for(int i = 0; i < cluster.getChildren().size(); i++)
			System.out.println(cluster.getChildren().get(i).getChildren().size());
		
		
	}
}
