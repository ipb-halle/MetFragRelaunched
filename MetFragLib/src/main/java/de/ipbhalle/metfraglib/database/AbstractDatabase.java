package de.ipbhalle.metfraglib.database;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IDatabase;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.Settings;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public abstract class AbstractDatabase implements IDatabase {

	protected Settings settings;
    protected static final Logger logger = LogManager.getLogger();

    public AbstractDatabase(Settings settings) {
		this.settings = settings;
        Configurator.setLevel(logger.getName(), (Level)this.settings.get(VariableNames.LOG_LEVEL_NAME));
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
	
	public boolean addSMILESFromInChI(ICandidate candidate) throws Exception {
		if(!candidate.getProperties().containsKey(VariableNames.SMILES_NAME) || candidate.getProperty(VariableNames.SMILES_NAME) == null) {
			if(candidate.getProperties().containsKey(VariableNames.INCHI_NAME) && candidate.getProperty(VariableNames.INCHI_NAME) != null) {
				String smiles = MoleculeFunctions.generateSmiles(MoleculeFunctions.getAtomContainerFromInChI((String)candidate.getProperty(VariableNames.INCHI_NAME)));
				this.addProperty(VariableNames.SMILES_NAME, smiles, candidate);							
			} else return false;
		}
		return true;
	}

	public boolean addInChIFromSmiles(ICandidate candidate) throws Exception {
		if(!candidate.getProperties().containsKey(VariableNames.INCHI_NAME) || candidate.getProperty(VariableNames.INCHI_NAME) == null) {
			if(candidate.getProperties().containsKey(VariableNames.SMILES_NAME) && candidate.getProperty(VariableNames.SMILES_NAME) != null) {
				String[] inchiinfo = MoleculeFunctions.getInChIInfoFromSmiles((String)candidate.getProperty(VariableNames.SMILES_NAME));
				this.addProperty(VariableNames.INCHI_NAME, inchiinfo[0], candidate);
				String[] tmp = inchiinfo[1].split("-");
				if(tmp.length >= 1) this.addProperty(VariableNames.INCHI_KEY_1_NAME, tmp[0], candidate);	
				if(tmp.length >= 2) this.addProperty(VariableNames.INCHI_KEY_2_NAME, tmp[1], candidate);	
				if(tmp.length >= 3) this.addProperty(VariableNames.INCHI_KEY_3_NAME, tmp[2], candidate);							
			} else return false;
		}
		return true;
	}

	public boolean addInChIKeyFromSmiles(ICandidate candidate) throws Exception {
		if(!candidate.getProperties().containsKey(VariableNames.INCHI_KEY_NAME) || candidate.getProperty(VariableNames.INCHI_KEY_NAME) == null) {
			if(candidate.getProperties().containsKey(VariableNames.SMILES_NAME) && candidate.getProperty(VariableNames.SMILES_NAME) != null) {
				String[] inchiinfo = MoleculeFunctions.getInChIInfoFromSmiles((String)candidate.getProperty(VariableNames.SMILES_NAME));
				String[] tmp = inchiinfo[1].split("-");
				if(tmp.length >= 1) this.addProperty(VariableNames.INCHI_KEY_1_NAME, tmp[0], candidate);	
				if(tmp.length >= 2) this.addProperty(VariableNames.INCHI_KEY_2_NAME, tmp[1], candidate);	
				if(tmp.length >= 3) this.addProperty(VariableNames.INCHI_KEY_3_NAME, tmp[2], candidate);							
			} else return false;
		}
		return true;
	}

	public boolean setInChIValues(ICandidate candidate) {
		if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_NAME) != null) {
			String inchikey = (String)candidate.getProperty(VariableNames.INCHI_KEY_NAME);
			if(inchikey.length() == 0 || inchikey.equals("NA")) {
				if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_1_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null &&	
						candidate.getProperties().containsKey(VariableNames.INCHI_KEY_2_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) != null &&	
						candidate.getProperties().containsKey(VariableNames.INCHI_KEY_3_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_3_NAME) != null)
					inchikey = candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) + "-" + candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) + "-" + candidate.getProperty(VariableNames.INCHI_KEY_3_NAME);
					candidate.setProperty(VariableNames.INCHI_KEY_NAME, inchikey);	
			} else {
				String[] tmp = ((String)candidate.getProperty(VariableNames.INCHI_KEY_NAME)).split("-");
				if(tmp.length >= 1) this.addProperty(VariableNames.INCHI_KEY_1_NAME, tmp[0], candidate);	
				if(tmp.length >= 2) this.addProperty(VariableNames.INCHI_KEY_2_NAME, tmp[1], candidate);	
				if(tmp.length >= 3) this.addProperty(VariableNames.INCHI_KEY_3_NAME, tmp[2], candidate);
			}
		} else {
			if(candidate.getProperties().containsKey(VariableNames.INCHI_KEY_1_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) != null &&	
					candidate.getProperties().containsKey(VariableNames.INCHI_KEY_2_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) != null &&	
					candidate.getProperties().containsKey(VariableNames.INCHI_KEY_3_NAME) && candidate.getProperty(VariableNames.INCHI_KEY_3_NAME) != null) {
				String inchikey = (String)candidate.getProperty(VariableNames.INCHI_KEY_1_NAME) + "-" + (String)candidate.getProperty(VariableNames.INCHI_KEY_2_NAME) + "-" + (String)candidate.getProperty(VariableNames.INCHI_KEY_3_NAME);
				this.addProperty(VariableNames.INCHI_KEY_NAME, inchikey, candidate);	
			}
		}
		return true;
	}
	
	protected void addProperty(String name, Object value, ICandidate candidate) {
		if(!candidate.getProperties().containsKey(name) || candidate.getProperty(name) == null) {
			candidate.setProperty(name, value);
		}
	}
	
	protected void checkAlternativePropertyNames(ICandidate candidate) {
		// alternative inchi
		if(!candidate.getProperties().containsKey(VariableNames.INCHI_NAME) || candidate.getProperty(VariableNames.INCHI_NAME) == null) {
			if(candidate.getProperties().containsKey("INCHI STRING") && candidate.getProperty("INCHI STRING") != null) 
				candidate.setProperty(VariableNames.INCHI_NAME, candidate.getProperty("INCHI STRING"));
		}
		// alternative molecular formula
		if(!candidate.getProperties().containsKey(VariableNames.MOLECULAR_FORMULA_NAME) || candidate.getProperty(VariableNames.MOLECULAR_FORMULA_NAME) == null) {
			if(candidate.getProperties().containsKey("MOL FORMULA") && candidate.getProperty("MOL FORMULA") != null) 
				candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, candidate.getProperty("MOL FORMULA"));
			else if(candidate.getProperties().containsKey("MOLECULAR FORMULA") && candidate.getProperty("MOLECULAR FORMULA") != null) 
				candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, candidate.getProperty("MOLECULAR FORMULA"));
		}// alternative monoisotopic mass
		if(!candidate.getProperties().containsKey(VariableNames.MONOISOTOPIC_MASS_NAME) || candidate.getProperty(VariableNames.MONOISOTOPIC_MASS_NAME) == null) {
			if(candidate.getProperties().containsKey("MONOSIOTOPIC MASS") && candidate.getProperty("MONOSIOTOPIC MASS") != null) 
				candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, Double.parseDouble((String)candidate.getProperty("MONOSIOTOPIC MASS")));
		}
	}
	
}
