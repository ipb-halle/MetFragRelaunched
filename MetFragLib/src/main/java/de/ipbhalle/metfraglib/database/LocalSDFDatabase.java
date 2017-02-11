package de.ipbhalle.metfraglib.database;

import java.util.Vector;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalSDFDatabase extends AbstractDatabase {

	private java.util.Vector<ICandidate> candidates;

	public LocalSDFDatabase(Settings settings) {
		super(settings);
	}

	public java.util.Vector<String> getCandidateIdentifiers() throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		if (this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if (this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			return this.getCandidateIdentifiers((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if (this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			return this.getCandidateIdentifiers((Double) settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double) settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		Vector<String> identifiers = new Vector<String>();
		for (ICandidate candidate : candidates)
			identifiers.add(candidate.getIdentifier());
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		Vector<String> identifiers = new Vector<String>();
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

	public Vector<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		Vector<String> identifiers = new Vector<String>();
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

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) throws Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		Vector<String> verifiedIdentifiers = new Vector<String>();
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

	public ICandidate getCandidateByIdentifier(String identifier) throws DatabaseIdentifierNotFoundException {
		int index = this.indexOfIdentifier(identifier);
		if (index == -1)
			throw new DatabaseIdentifierNotFoundException(identifier);
		return this.candidates.get(index);
	}

	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
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

	public void nullify() {}

	/**
	 * @throws Exception
	 * 
	 */
	private void readCandidatesFromFile() throws Exception {
		this.candidates = new java.util.Vector<ICandidate>();
		java.io.File f = new java.io.File((String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
		java.util.Vector<String> identifiers = new java.util.Vector<String>();
		if (f.isFile()) {
			IteratingSDFReader reader = new IteratingSDFReader(new java.io.FileReader(f), DefaultChemObjectBuilder.getInstance());
			int index = 1;
			while (reader.hasNext()) {
				IAtomContainer molecule = reader.next();
				String identifier = molecule.getID();
				if (molecule.getProperty("Identifier") != null)
					identifier = (String) molecule.getProperty("Identifier");
				molecule = MoleculeFunctions.convertImplicitToExplicitHydrogens(molecule);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);

				if (identifier == null || identifier.length() == 0)
					identifier = String.valueOf(index);
				if(identifiers.contains(identifier)) {
					reader.close();
					throw new Exception();
				}
				identifiers.add(identifier);
				String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(molecule);
				ICandidate precursorCandidate = new TopDownPrecursorCandidate(inchiInfo[0], identifier);

				java.util.Iterator<Object> properties = molecule.getProperties().keySet().iterator();
				while (properties.hasNext()) {
					String key = (String) properties.next();
					if (key != null && molecule.getProperty(key) != null)
						precursorCandidate.setProperty(key, molecule.getProperty(key));
				}
				precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchiInfo[1].split("-")[0]);
				precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchiInfo[1].split("-")[1]);
				precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, inchiInfo[0].split("/")[1]);
				try {
					precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, precursorCandidate.getMolecularFormula().getMonoisotopicMass());
				} catch (AtomTypeNotKnownFromInputListException e) {
					continue;
				}
				this.candidates.add(precursorCandidate);
				index++;
			}
			reader.close();
		}
	}

	/**
	 * 
	 * @param identifier
	 * @return
	 */
	private int indexOfIdentifier(String identifier) {
		for (int i = 0; i < this.candidates.size(); i++)
			if (this.candidates.get(i).getIdentifier().equals(identifier))
				return i;
		return -1;
	}
}
