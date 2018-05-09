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

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * 
 * local csv database file with one candidate entry per line semicolon separated
 * like: "Identifier","InChI","MolecularFormula",MonoisotopicMass,"InChIKey1","InChIKey2"
 * 
 * @author chrisr
 * 
 */
public class LocalCSVDatabase extends AbstractFileDatabase {

	public LocalCSVDatabase(Settings settings) {
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
		java.util.List<String> propertyNames = new java.util.ArrayList<String>();
		BufferedReader reader = null;
		if (f.isFile()) {
			reader = new BufferedReader(new FileReader(f));
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			java.util.Iterator<?> it = parser.getHeaderMap().keySet().iterator();
			
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
			
			while(it.hasNext()) {
				String colname = (String)it.next();
				propertyNames.add(colname);
				if(nameToInputName.containsKey(colname)) nameToWasFound.put(colname, new Boolean(true));
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
					
			if(properIdentifierName.equals("")) {
				logger.error("Error: No Identifier column defined.");
				parser.close();
				reader.close();
				throw new Exception();
			}
			if(possibleInChINames.equals("")) {
				logger.error("Error: No InChI column defined.");
				parser.close();
				reader.close();
				throw new Exception();
			}
			int index = 0;
			for(CSVRecord record : parser) {
				index++;
				String identifier = record.get(properIdentifierName);
				if(identifier == null) continue;
				identifier = identifier.trim();
				if(identifier.equals("-") || identifier.equals("NO_MATCH")) continue;
				ICandidate precursorCandidate = new TopDownPrecursorCandidate(record.get(properInChIName), identifier + "|" + index);
				keys = nameToWasFound.keySet().iterator();
				for(String curKey : this.preparedPropertyNames) {
					if(nameToWasFound.get(curKey)) {
						String inputName = nameToInputName.get(curKey);
						if(!precursorCandidate.hasDefinedProperty(inputName)) precursorCandidate.setProperty(inputName, record.get(curKey));
					}
				}
				for(int ii = 0; ii < propertyNames.size(); ii++) {
					String colname = propertyNames.get(ii);
					if(!precursorCandidate.hasDefinedProperty(colname)) {
						precursorCandidate.setProperty(colname, record.get(colname));
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
			
			parser.close();
			reader.close();
			
			return;
		} 
		throw new Exception();
	}
	
	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, "/home/cruttkie/Documents/PhD/MetFrag/debugs/emma/msready/ChemistryDashboard-Batch-Search_2018-02-23_07_30_43_Worksheet3.csv");
		LocalCSVDatabase db = new LocalCSVDatabase(settings);
		System.out.println(db.getCandidateIdentifiers().size());	
	}
	
}
