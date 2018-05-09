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

	protected boolean identifierSearch = false;
	protected boolean massSearch = false;
	protected boolean formulaSearch = false;
	
	protected ByteMolecularFormula formula;
	protected String[] searchIdentifiers;
	protected Double monoisotopicMass;
	protected Double relativeMassDeviation;
	protected Double mzabs;
	protected Double lowerLimit;
	protected Double upperLimit;
	
	protected java.util.ArrayList<ICandidate> candidates;
	protected java.util.ArrayList<String> identifiers;
	
	public AbstractFileDatabase(Settings settings) {
		super(settings);
	}

	public void nullify() {}
	
	public java.util.ArrayList<String> getCandidateIdentifiers() throws Exception {
		this.massSearch = false;
		this.formulaSearch = false;
		this.identifierSearch = false;
		this.identifiers = new java.util.ArrayList<String>();
		if (this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null) {
			this.identifierSearch = true;
			this.searchIdentifiers = (String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME);
			return this.getCandidateIdentifiers(this.searchIdentifiers);
		}
		if (this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null) {
			this.formulaSearch = true;
			this.formula = new ByteMolecularFormula((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
			return this.getCandidateIdentifiers((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		}
		if (this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null) {
			this.massSearch = true;
			this.monoisotopicMass = (Double) settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
			this.relativeMassDeviation = (Double) settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME);
			this.mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
			this.lowerLimit = monoisotopicMass - mzabs;
			this.upperLimit = monoisotopicMass + mzabs;
			return this.getCandidateIdentifiers(this.monoisotopicMass, this.relativeMassDeviation);
		}
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> identifiers = new ArrayList<String>();
		for (ICandidate candidate : this.candidates)
			identifiers.add(candidate.getIdentifier());
		return identifiers;
	}

	protected boolean checkFilter(ICandidate precursorCandidate) {
		if(this.identifierSearch) {
			for(int i = 0; i < this.searchIdentifiers.length; i++) {
				if(precursorCandidate.getIdentifier().startsWith(this.searchIdentifiers[i] + "|")) return true;
			}
			return false;
		} else if(this.formulaSearch) {
			ByteMolecularFormula currentFormula = null;
			try {
				currentFormula = new ByteMolecularFormula((String)precursorCandidate.getProperty(VariableNames.MOLECULAR_FORMULA_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (this.formula.compareTo(currentFormula)) return true;
			return false;
		} else if(this.massSearch) {
			double currentMonoisotopicMass = (Double) precursorCandidate.getProperty(VariableNames.MONOISOTOPIC_MASS_NAME);
			if (this.lowerLimit <= currentMonoisotopicMass && currentMonoisotopicMass <= this.upperLimit)
				return true;
			return false;
		}
		return true;
	}
	
	public java.util.ArrayList<String> getCandidateIdentifiers(String[] identifiers) throws Exception {
		// read candidate first
		this.readCandidatesFromFile();
		return this.identifiers;
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
		// read candidate first
		this.readCandidatesFromFile();
		return this.identifiers;
	}

	public ICandidate getCandidateByIdentifier(String identifier) throws DatabaseIdentifierNotFoundException {
		int index = this.indexOfIdentifier(identifier);
		if (index == -1)
			throw new DatabaseIdentifierNotFoundException(identifier);
		return this.candidates.get(index);
	}

	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		// read candidate first
		this.readCandidatesFromFile();
		return this.identifiers;
	}

	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		this.identifierSearch = true;
		this.searchIdentifiers = (String[])identifiers.toArray();
		this.readCandidatesFromFile();
		return this.identifiers;
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
