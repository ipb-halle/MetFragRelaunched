package de.ipbhalle.metfraglib.database;

import java.util.ArrayList;

import de.ipbhalle.metfraglib.exceptions.NoValidDatabaseSearchSettingsDefined;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalPubChem2010Database extends AbstractDatabase {

	public LocalPubChem2010Database(Settings settings) {
		super(settings);
	}

	public ArrayList<String> getCandidateIdentifiers(double monoisotopicMass,
			double mzabs) {
		// TODO Auto-generated method stub
		return null;
	}

	public java.util.ArrayList<String> getCandidateIdentifiers() throws Exception {
		if(settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[])settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if(settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			return this.getCandidateIdentifiers((String)settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if(settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			return this.getCandidateIdentifiers((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double)settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		try {
			throw new NoValidDatabaseSearchSettingsDefined();
		} catch (NoValidDatabaseSearchSettingsDefined e) {
			e.printStackTrace();
		}
		return new java.util.ArrayList<String>();
	}
	
	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	public ICandidate getCandidateByIdentifier(String identifier) {
		// TODO Auto-generated method stub
		return null;
	}

	public CandidateList getCandidateByIdentifier(
			ArrayList<String> identifiers) {
		// TODO Auto-generated method stub
		return null;
	}

	public void nullify() {
		// TODO Auto-generated method stub
		
	}

}
