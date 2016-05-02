package de.ipbhalle.metfragweb.helper;

import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;

public class RetrieveCompoundsThreadRunner extends ThreadRunner {
	
	public RetrieveCompoundsThreadRunner(BeanSettingsContainer beanSettingsContainer, 
			Messages infoMessages, Messages errorMessages) {
		super(beanSettingsContainer, infoMessages, errorMessages);
	}
	
	@Override
	public void run() {
		try {
			this.beanSettingsContainer.retrieveCompounds(this.infoMessages, this.errorMessages);
		}
		catch(Exception e) {
			System.err.println("Error retrieving candidates");
			return;
		}
		if (this.beanSettingsContainer.isCompoundsRetrieved()) {
			this.beanSettingsContainer.initialiseAvailableDatabaseScores();
			this.beanSettingsContainer.initialiseAvailableCandidatePartitioningCoefficients();
		}

	}
	
}
