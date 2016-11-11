package de.ipbhalle.metfraglib.database;

import java.util.Vector;

import org.openscience.cdk.ChemObject;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalInMemoryDatabase extends AbstractDatabase {

	private java.util.Vector<TopDownPrecursorCandidate> candidates;
	
	public LocalInMemoryDatabase(Settings settings) {
		super(settings);
	}
	
	public java.util.Vector<String> getCandidateIdentifiers() throws Exception {
		if(this.candidates == null) this.initialiseCandidatesFromMemory();
		if(this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[])settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if(this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null) {
			try {
				return this.getCandidateIdentifiers((String)settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null) {
			try {
				return this.getCandidateIdentifiers((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double)settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Vector<String> identifiers = new Vector<String>();
		for(TopDownPrecursorCandidate candidate : candidates)
			identifiers.add(candidate.getIdentifier());
		return identifiers;
	}
	
	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		if(this.candidates == null) this.initialiseCandidatesFromMemory();
		Vector<String> identifiers = new Vector<String>();
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		double lowerLimit = monoisotopicMass - mzabs;
		double upperLimit = monoisotopicMass + mzabs;
		for(int i = 0; i < this.candidates.size(); i++) {
			double currentMonoisotopicMass = 0.0;
			try {
				IAtomContainer con = this.candidates.get(i).getAtomContainer();
				MoleculeFunctions.prepareAtomContainer(con, false);
				currentMonoisotopicMass = MoleculeFunctions.calculateMonoIsotopicMassImplicitHydrogens(this.candidates.get(i).getAtomContainer());
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(lowerLimit >= currentMonoisotopicMass && currentMonoisotopicMass <= upperLimit)
				identifiers.add(this.candidates.get(i).getIdentifier());
		}
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(String molecularFormula) {
		if(this.candidates == null) this.initialiseCandidatesFromMemory();
		Vector<String> identifiers = new Vector<String>();
		org.openscience.cdk.interfaces.IMolecularFormula queryFormula = MolecularFormulaManipulator.getMolecularFormula(molecularFormula, new ChemObject().getBuilder());
		for(int i = 0; i < this.candidates.size(); i++) {
			org.openscience.cdk.interfaces.IMolecularFormula currentFormula = null;
			try {
				currentFormula = MolecularFormulaManipulator.getMolecularFormula(MoleculeFunctions.convertExplicitToImplicitHydrogens(this.candidates.get(i).getAtomContainer()));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if(queryFormula.equals(currentFormula)) identifiers.add(this.candidates.get(i).getIdentifier());
		}
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) {
		if(this.candidates == null) this.initialiseCandidatesFromMemory();
		Vector<String> verifiedIdentifiers = new Vector<String>();
		for(int i = 0; i < identifiers.size(); i++) {
			try {
				this.getCandidateByIdentifier(identifiers.get(i));
			} catch (DatabaseIdentifierNotFoundException e) {
				logger.warn("Warning: Candidate identifier " + identifiers.get(i) + " not found.");
				continue;
			}
			verifiedIdentifiers.add(identifiers.get(i));
		}
		return verifiedIdentifiers;
	
	}

	public ICandidate getCandidateByIdentifier(String identifier) throws DatabaseIdentifierNotFoundException {
		int index = this.indexOfIdentifier(identifier);
		if(index == -1) 
			throw new DatabaseIdentifierNotFoundException(identifier);
		return this.candidates.get(index);
	}

	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		CandidateList candidateList = new CandidateList();
		for(int i = 0; i < identifiers.size(); i++) {
			ICandidate candidate = null;
			try {
				candidate = this.getCandidateByIdentifier(identifiers.get(i));
			} catch (DatabaseIdentifierNotFoundException e) {
				logger.warn("Candidate identifier " + identifiers.get(i) + " not found.");
			}
			if(candidate != null) candidateList.addElement(candidate);
		}
		return candidateList;
	}

	public void nullify() {
	}
	
	private void initialiseCandidatesFromMemory() {
		IAtomContainer[] molecules = (IAtomContainer[])this.settings.get(VariableNames.MOLECULES_IN_MEMORY);
		this.candidates = new Vector<TopDownPrecursorCandidate>();
		if(molecules == null) return;
		for(int i = 0; i < molecules.length; i++) {
			MoleculeFunctions.prepareAtomContainer(molecules[i], true);
			String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(molecules[i]);
			TopDownPrecursorCandidate precursorCandidate = new TopDownPrecursorCandidate(inchiInfo[0], (i + 1) + "");

			java.util.Iterator<Object> properties = molecules[i].getProperties().keySet().iterator();
			while(properties.hasNext()) {
				String key = (String)properties.next();
				if(molecules[i].getProperties().containsKey(key) && molecules[i].getProperty(key) != null) precursorCandidate.setProperty(key, molecules[i].getProperty(key));
			}
			
			precursorCandidate.setProperty(VariableNames.INCHI_KEY_NAME, inchiInfo[1]);
			precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchiInfo[1].split("-")[0]);
			precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchiInfo[1].split("-")[1]);
			precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, inchiInfo[0].split("/")[1]);
			
			this.candidates.add(precursorCandidate);
		}
		return;
	}
	
	private int indexOfIdentifier(String identifier) {
		for(int i = 0; i < this.candidates.size(); i++)
			if(this.candidates.get(i).getIdentifier().equals(identifier)) return i;
		return -1;
	}
}
