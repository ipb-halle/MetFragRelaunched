package de.ipbhalle.metfraglib.database;

import java.util.ArrayList;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractFileDatabase extends AbstractDatabase {

	protected String[] preparedPropertyNames = {
		VariableNames.IDENTIFIER_NAME_3,
		VariableNames.IDENTIFIER_NAME_2,
		VariableNames.IDENTIFIER_NAME,
		VariableNames.MONOISOTOPIC_MASS_NAME,
		VariableNames.INCHI_NAME_2,
		VariableNames.INCHI_NAME,
		VariableNames.MOLECULAR_FORMULA_NAME_2,
		VariableNames.MOLECULAR_FORMULA_NAME,
		VariableNames.SMILES_NAME_2,
		VariableNames.SMILES_NAME,
		VariableNames.INCHI_KEY_NAME_2,
		VariableNames.INCHI_KEY_NAME,
		VariableNames.COMPOUND_NAME_NAME_2,
		VariableNames.COMPOUND_NAME_NAME
	};

	protected java.util.ArrayList<ICandidate> candidates;
	
	public AbstractFileDatabase(Settings settings) {
		super(settings);
	}

	public void nullify() {}
	
	public java.util.ArrayList<String> getCandidateIdentifiers() throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		if (this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if (this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			return this.getCandidateIdentifiers((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if (this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			return this.getCandidateIdentifiers((Double) settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double) settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		ArrayList<String> identifiers = new ArrayList<String>();
		for (ICandidate candidate : this.candidates)
			identifiers.add(candidate.getIdentifier());
		return identifiers;
	}
	
	public java.util.ArrayList<String> getCandidateIdentifiers(String[] identifiers) throws Exception {
		java.util.ArrayList<String> identifiersAsArrayList = new java.util.ArrayList<String>();
		for(String identifier : identifiers) {
			for(int i = 0; i < this.candidates.size(); i++) {
				if(this.candidates.get(i).getIdentifier().matches(identifier + "\\|[0-9]+"))
				identifiersAsArrayList.add(this.candidates.get(i).getIdentifier());	
			}
		}
		return identifiersAsArrayList;
	}
	
	public CandidateList getCandidateByIdentifier(ArrayList<String> identifiers) {
		CandidateList candidateList = new CandidateList();
		for (int i = 0; i < identifiers.size(); i++) {
			ICandidate candidate = null;
			try {
				candidate = this.getCandidateByIdentifier(identifiers.get(i));
			} catch (DatabaseIdentifierNotFoundException e) {
				logger.warn("Candidate identifier " + identifiers.get(i) + " not found.");
			}
			if (candidate != null)
				candidateList.addElement(candidate);
		}
		return candidateList;
	}

	public ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> identifiers = new ArrayList<String>();
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		double lowerLimit = monoisotopicMass - mzabs;
		double upperLimit = monoisotopicMass + mzabs;
		for (int i = 0; i < this.candidates.size(); i++) {
			double currentMonoisotopicMass = (Double) this.candidates.get(i).getProperty(VariableNames.MONOISOTOPIC_MASS_NAME);
			if (lowerLimit <= currentMonoisotopicMass && currentMonoisotopicMass <= upperLimit)
				identifiers.add(this.candidates.get(i).getIdentifier());
		}
		return identifiers;
	}

	public ICandidate getCandidateByIdentifier(String identifier) throws DatabaseIdentifierNotFoundException {
		int index = this.indexOfIdentifier(identifier);
		if (index == -1)
			throw new DatabaseIdentifierNotFoundException(identifier);
		return this.candidates.get(index);
	}

	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> identifiers = new ArrayList<String>();
		ByteMolecularFormula queryFormula = new ByteMolecularFormula(molecularFormula);
		for (int i = 0; i < this.candidates.size(); i++) {
			ByteMolecularFormula currentFormula = null;
			try {
				currentFormula = new ByteMolecularFormula((String)this.candidates.get(i).getProperty(VariableNames.MOLECULAR_FORMULA_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (queryFormula.compareTo(currentFormula))
				identifiers.add(this.candidates.get(i).getIdentifier());
		}
		return identifiers;
	}

	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> verifiedIdentifiers = new ArrayList<String>();
		for (int i = 0; i < identifiers.size(); i++) {
			try {
				this.getCandidateByIdentifier(identifiers.get(i));
			} catch (DatabaseIdentifierNotFoundException e) {
				logger.warn("Candidate identifier " + identifiers.get(i) + " not found.");
				continue;
			}
			verifiedIdentifiers.add(identifiers.get(i));
		}
		return verifiedIdentifiers;

	}
	
	protected abstract void readCandidatesFromFile() throws Exception;

	/**
	 * 
	 * @param identifier
	 * @return
	 */
	protected int indexOfIdentifier(String identifier) {
		for (int i = 0; i < this.candidates.size(); i++) {
			if (this.candidates.get(i).getIdentifier().equals(identifier))
				return i;
		}
		return -1;
	}
	
}
