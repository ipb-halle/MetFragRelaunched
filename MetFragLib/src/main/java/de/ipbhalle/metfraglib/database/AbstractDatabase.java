package de.ipbhalle.metfraglib.database;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractDatabase implements IDatabase {

	protected Settings settings;
	protected Logger logger = Logger.getLogger(this.getClass());

	public AbstractDatabase(Settings settings) {
		this.settings = settings;
		logger.setLevel((Level)this.settings.get(VariableNames.LOG_LEVEL_NAME));
	}
	
	public java.util.ArrayList<String> getCandidateIdentifiers() throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setRetrievingStatusString("Retrieving Candidates");
		java.util.ArrayList<String> identifiers = null;
		if(this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			identifiers = this.getCandidateIdentifiers((String[])settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		else if(this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null) {
			identifiers = this.getCandidateIdentifiers((String)settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		}
		else if(this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null) {
			identifiers = this.getCandidateIdentifiers((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double)settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		}
		if(identifiers != null) {
		//	statusString = "Fetching " + identifiers.size() + " Candidates";
			return identifiers;
		}
		return new java.util.ArrayList<String>();
	}

	//ToDo: check whether identifiers are valid and exist
	public java.util.ArrayList<String> getCandidateIdentifiers(String[] identifiers) throws Exception {
		java.util.ArrayList<String> identifiersAsArrayList = new java.util.ArrayList<String>();
		for(String identifier : identifiers)  
			identifiersAsArrayList.add(identifier);
		return identifiersAsArrayList;
	}
	
}
