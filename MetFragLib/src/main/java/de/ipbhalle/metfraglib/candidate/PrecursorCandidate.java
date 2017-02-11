package de.ipbhalle.metfraglib.candidate;

import java.util.Hashtable;

import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.interfaces.IMolecularStructure;
import de.ipbhalle.metfraglib.list.MatchList;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.precursor.DefaultPrecursor;

public class PrecursorCandidate implements ICandidate {

	protected Hashtable<String, Object> properties;
	protected MatchList matchList;
	protected IMolecularStructure precursorStructure;
	protected boolean useSmiles = false;
	
	public PrecursorCandidate(String inchi, String identifier) {
		this.properties = new Hashtable<String, Object>();
		this.properties.put(VariableNames.IDENTIFIER_NAME, identifier);
		this.properties.put(VariableNames.INCHI_NAME, inchi);
	}

	public PrecursorCandidate(String inchi, String identifier, String smiles) {
		this.properties = new Hashtable<String, Object>();
		this.properties.put(VariableNames.IDENTIFIER_NAME, identifier);
		this.properties.put(VariableNames.INCHI_NAME, inchi);
		this.properties.put(VariableNames.SMILES_NAME, smiles);
	}
	
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
	
	public Object getProperty() {
		return this.properties.get(VariableNames.IDENTIFIER_NAME);
	}
	
	public String getPropertyAsString() {
		return (String)this.properties.get(VariableNames.IDENTIFIER_NAME);
	}
	
	public Object getProperty(String key) {
		return this.properties.get(key);
	}

	public void removeProperty(String key) {
		this.properties.remove(key);
	}
	
	public String getInChI() {
		return (String)this.properties.get(VariableNames.INCHI_NAME);
	}
	
	public String getIdentifier() {
		return (String)this.properties.get(VariableNames.IDENTIFIER_NAME);
	}

	public IAtomContainer getAtomContainer() throws Exception {
		IAtomContainer molecule = null;
		int trials = 1;
		while(trials <= 10) {
			try {
				if(!this.useSmiles)  molecule = MoleculeFunctions.getAtomContainerFromInChI((String)this.properties.get(VariableNames.INCHI_NAME));
				else molecule = MoleculeFunctions.getAtomContainerFromSMILES((String)this.properties.get(VariableNames.SMILES_NAME));
			}
			catch(Exception e) {
				trials++;
				continue;
			}
			break;
		}
		if(molecule == null) {
			if(!this.useSmiles) throw new Exception("Could not read InChI!");
			else throw new Exception("Could not read SMILES!");
		}
		MoleculeFunctions.prepareAtomContainer(molecule, true);
		return molecule;
	}

	public IAtomContainer getImplicitHydrogenAtomContainer() throws Exception {
		IAtomContainer molecule = this.getAtomContainer();
		MoleculeFunctions.convertExplicitToImplicitHydrogens(molecule);
		return molecule;
	}
	
	public IMolecularFormula getMolecularFormula() throws AtomTypeNotKnownFromInputListException {
		return new ByteMolecularFormula((String)this.properties.get(VariableNames.MOLECULAR_FORMULA_NAME));
	}

	public Hashtable<String, Object> getProperties() {
		return this.properties;
	}

	public MatchList getMatchList() {
		return this.matchList;
	}
	
	public void setMatchList(MatchList matchList) {
		this.matchList = matchList;
	}

	public int getNumberPeaksExplained() {
		if(this.matchList == null) return 0;
		return this.matchList.getNumberElements();
	}
	
	public void setPrecursorMolecule(IMolecularStructure precursorStructure) {
		this.precursorStructure = precursorStructure;
	}
	
	public IMolecularStructure getPrecursorMolecule() {
		return this.precursorStructure;
	}
	
	public void nullify() {
		if(this.matchList != null) {
			this.matchList.nullify();
		}
		if(this.precursorStructure != null) {
			this.precursorStructure.nullify();
			this.precursorStructure = null;
		}
	}

	public void shallowNullify() {
		if(this.matchList != null) {
			this.matchList.shallowNullify();
		}
		if(this.precursorStructure != null) {
			this.precursorStructure.nullify();
			this.precursorStructure = null;
		}
	}

	public PrecursorCandidate clone() {
		PrecursorCandidate clone = new PrecursorCandidate(this.getInChI(), this.getIdentifier());
		java.util.Enumeration<String> keys = this.properties.keys();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			clone.setProperty(key, this.properties.get(key));
		}
		return clone;
	}
	
	public void initialisePrecursorCandidate() throws AtomTypeNotKnownFromInputListException, Exception {
		this.precursorStructure = new DefaultPrecursor(this.getImplicitHydrogenAtomContainer());
		this.precursorStructure.preprocessPrecursor();
	}
	
	public String[] getPropertyNames() {
		java.util.Enumeration<?> keys = this.properties.keys();
		java.util.Vector<String> keys_vector = new java.util.Vector<String>();
		while(keys.hasMoreElements()) {
			keys_vector.add((String)keys.nextElement());
		}
		String[] names = new String[keys_vector.size()];
		for(int i = 0; i < names.length; i++)
			names[i] = keys_vector.get(i);
		return names;
	}
	
	public void setUseSmiles(boolean useSmiles) {
		this.useSmiles = useSmiles;
	}

	public boolean isUseSmiles() {
		return this.useSmiles;
	}
}
