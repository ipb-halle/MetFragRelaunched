package de.ipbhalle.metfraglib.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

/**
 * 
 * InChI database file with one candidate entry per line semicolon separated
 * like: Identifier|InChI|MolecularFormula|MonoisotopicMass|InChIKey1|InChIKey2
 * EA021313
 * |InChI=1S/C12H17NO/c1-4-13(5-2)12(14)11-8-6-7-10(3)9-11/h6-9H,4-5H2,1-
 * 3H3|C12H17NO|191.131014|MMOXZBCLCQITDF|UHFFFAOYSA
 * 
 * @author chrisr
 * 
 */
public class LocalPSVDatabase extends AbstractFileDatabase {

	public LocalPSVDatabase(Settings settings) {
		super(settings);
	}

	/**
	 * @throws MultipleHeadersFoundInInputDatabaseException
	 * @throws IOException
	 * 
	 */
	protected void readCandidatesFromFile() throws Exception {
		this.candidates = new java.util.ArrayList<ICandidate>();
		java.io.File f = new java.io.File((String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
		BufferedReader reader = null;
		if (f.isFile()) {
			reader = new BufferedReader(new FileReader(f));
			
			java.util.HashMap<String, String> nameToInputName = new java.util.HashMap<String, String>();
			nameToInputName.put(VariableNames.IDENTIFIER_NAME_3, VariableNames.IDENTIFIER_NAME);
			nameToInputName.put(VariableNames.IDENTIFIER_NAME_2, VariableNames.IDENTIFIER_NAME);
			nameToInputName.put(VariableNames.IDENTIFIER_NAME, VariableNames.IDENTIFIER_NAME);
			nameToInputName.put(VariableNames.MONOISOTOPIC_MASS_NAME_2, VariableNames.MONOISOTOPIC_MASS_NAME);
			nameToInputName.put(VariableNames.MONOISOTOPIC_MASS_NAME, VariableNames.MONOISOTOPIC_MASS_NAME);
			nameToInputName.put(VariableNames.INCHI_NAME_2, VariableNames.INCHI_NAME);
			nameToInputName.put(VariableNames.INCHI_NAME, VariableNames.INCHI_NAME);
			nameToInputName.put(VariableNames.MOLECULAR_FORMULA_NAME_2, VariableNames.MOLECULAR_FORMULA_NAME);
			nameToInputName.put(VariableNames.MOLECULAR_FORMULA_NAME, VariableNames.MOLECULAR_FORMULA_NAME);
			nameToInputName.put(VariableNames.SMILES_NAME_2, VariableNames.SMILES_NAME);
			nameToInputName.put(VariableNames.SMILES_NAME, VariableNames.SMILES_NAME);
			nameToInputName.put(VariableNames.INCHI_KEY_NAME_2, VariableNames.INCHI_KEY_NAME);
			nameToInputName.put(VariableNames.INCHI_KEY_NAME, VariableNames.INCHI_KEY_NAME);
			nameToInputName.put(VariableNames.COMPOUND_NAME_NAME_2, VariableNames.COMPOUND_NAME_NAME);
			nameToInputName.put(VariableNames.COMPOUND_NAME_NAME, VariableNames.COMPOUND_NAME_NAME);
			
			String[] possibleIdentifierNames = {VariableNames.IDENTIFIER_NAME_3, VariableNames.IDENTIFIER_NAME_2, VariableNames.IDENTIFIER_NAME};
			String[] possibleInChINames = {VariableNames.INCHI_NAME_2, VariableNames.INCHI_NAME};
			
			java.util.HashMap<String, Boolean> nameToWasFound = new java.util.HashMap<String, Boolean>();
			java.util.Iterator<String> keys = nameToInputName.keySet().iterator();
			while(keys.hasNext())
				nameToWasFound.put(keys.next(), new Boolean(false));
			
			/*
			 * skip first line as header
			 */
			String header = reader.readLine();
			String[] colNames = header.split("\\|");
			java.util.HashMap<String, Integer> propNameToIndex = new java.util.HashMap<String, Integer>();
			for (int i = 0; i < colNames.length; i++) {
				if (propNameToIndex.get(colNames[i]) != null) {
					if (reader != null)
						reader.close();
					throw new MultipleHeadersFoundInInputDatabaseException("Found " + colNames[i] + " several times in header!");
				}
				propNameToIndex.put(colNames[i], i);
				if(nameToInputName.containsKey(colNames[i])) nameToWasFound.put(colNames[i], new Boolean(true));
			}
			
			String properIdentifierName = "";
			String properInChIName = "";
			for(String name : possibleIdentifierNames) {
				if(nameToWasFound.get(name)) {
					properIdentifierName = name;
					break;
				}
			}
			for(String name : possibleInChINames) {
				if(nameToWasFound.get(name)) {
					properInChIName = name;
					break;
				}
			}
			String line = "";
			
			int index = 0;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\|");
				String identifier = tmp[propNameToIndex.get(properIdentifierName)].trim();
				ICandidate precursorCandidate = new TopDownPrecursorCandidate(tmp[propNameToIndex.get(properInChIName)].trim(), identifier + "|" + index);
				keys = nameToWasFound.keySet().iterator();
				for(String curKey : this.preparedPropertyNames) {
					curKey = keys.next();
					if(nameToWasFound.get(curKey)) {
						String inputName = nameToInputName.get(curKey);
						if(!precursorCandidate.hasDefinedProperty(inputName)) precursorCandidate.setProperty(inputName, tmp[propNameToIndex.get(curKey)].trim());
					}
				}
				/*
				 * store all read property fields within the candidate container
				 */
				for (int k = 0; k < colNames.length; k++) {
					String colname = colNames[k];
					if(!precursorCandidate.hasDefinedProperty(colname)) {
						precursorCandidate.setProperty(colname, tmp[propNameToIndex.get(colname)]);
					}
				}
					
				if(!precursorCandidate.hasDefinedProperty(VariableNames.MONOISOTOPIC_MASS_NAME)) {
					try {
						precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, precursorCandidate.getMolecularFormula().getMonoisotopicMass());
					} catch (AtomTypeNotKnownFromInputListException e) {
						continue;
					}
				} else {
					precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, Double.parseDouble((String)precursorCandidate.getProperty(VariableNames.MONOISOTOPIC_MASS_NAME)));
				}
				if(!this.addInChIFromSmiles(precursorCandidate)) continue;
				if(!this.addSMILESFromInChI(precursorCandidate)) continue;
				if(!this.addInChIKeyFromSmiles(precursorCandidate)) continue;
				if(!this.setInChIValues(precursorCandidate)) continue;
				

				if(this.checkFilter(precursorCandidate)) {
					this.identifiers.add(precursorCandidate.getIdentifier());
					this.candidates.add(precursorCandidate);
				}
			}
		}
		if (reader != null)
			reader.close();
	}
	
}
