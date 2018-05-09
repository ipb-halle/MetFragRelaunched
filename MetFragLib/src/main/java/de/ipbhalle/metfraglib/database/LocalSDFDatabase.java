package de.ipbhalle.metfraglib.database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingSDFReader;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalSDFDatabase extends AbstractFileDatabase {

	public LocalSDFDatabase(Settings settings) {
		super(settings);
	}

	/**
	 * @throws Exception
	 * 
	 */
	protected void readCandidatesFromFile() throws Exception {
		this.candidates = new java.util.ArrayList<ICandidate>();
		java.io.File f = this.curateSDF((String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
		if (f.isFile()) {
			
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
			
			IteratingSDFReader reader = new IteratingSDFReader(new java.io.FileReader(f), DefaultChemObjectBuilder.getInstance(), true);
			java.util.LinkedHashSet<String> properties = new java.util.LinkedHashSet<String>();
			
			int index = 0;
			while (reader.hasNext()) {
				IAtomContainer molecule = reader.next();
				MoleculeFunctions.prepareAtomContainer(molecule, false);
				if(molecule.getAtomCount() == 0) continue;
				index++;
				molecule.removeProperty("cdk:Title");
				java.util.Map<Object, Object> props =  molecule.getProperties();
				String identifier = String.valueOf(index);
				for(String identifierName : possibleIdentifierNames) {
					if(props.containsKey(identifierName) && props.get(identifierName) != null) {
						identifier = props.get(identifierName) + "|" + identifier;
						break;
					}
				}
				molecule = MoleculeFunctions.convertImplicitToExplicitHydrogens(molecule);
				AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
				String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(molecule);
				ICandidate precursorCandidate = new TopDownPrecursorCandidate(inchiInfo[0], identifier);
				
				java.util.Iterator<String> keys = nameToInputName.keySet().iterator();
				for(String curKey : this.preparedPropertyNames) {
					curKey = keys.next();
					String inputName = nameToInputName.get(curKey);
					if(!precursorCandidate.hasDefinedProperty(inputName) && props.containsKey(curKey) && props.get(curKey) != null) 
						precursorCandidate.setProperty(inputName, props.get(curKey));
				}
				
				java.util.Iterator<Object> it = props.keySet().iterator();
				while (it.hasNext()) {
					String key = (String) it.next();
					if(!precursorCandidate.hasDefinedProperty(key)) precursorCandidate.setProperty(key, props.get(key));
					properties.add(key);
				}
				
				if(!precursorCandidate.hasDefinedProperty(VariableNames.INCHI_KEY_1_NAME)) precursorCandidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inchiInfo[1].split("-")[0]);
				if(!precursorCandidate.hasDefinedProperty(VariableNames.INCHI_KEY_2_NAME)) precursorCandidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inchiInfo[1].split("-")[1]);
				if(!precursorCandidate.hasDefinedProperty(VariableNames.MOLECULAR_FORMULA_NAME)) precursorCandidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, inchiInfo[0].split("/")[1]);
				if(!precursorCandidate.hasDefinedProperty(VariableNames.SMILES_NAME)) precursorCandidate.setProperty(VariableNames.SMILES_NAME, MoleculeFunctions.generateSmiles(molecule));

				if(!precursorCandidate.hasDefinedProperty(VariableNames.MONOISOTOPIC_MASS_NAME)) {
					try {
						precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, precursorCandidate.getMolecularFormula().getMonoisotopicMass());
					} catch (AtomTypeNotKnownFromInputListException e) {
						continue;
					}
				} else precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, Double.parseDouble((String)precursorCandidate.getProperty(VariableNames.MONOISOTOPIC_MASS_NAME)));
				
				if(!this.addInChIFromSmiles(precursorCandidate)) continue;
				if(!this.addSMILESFromInChI(precursorCandidate)) continue;
				if(!this.addInChIKeyFromSmiles(precursorCandidate)) continue;
				if(!this.setInChIValues(precursorCandidate)) continue;
				

				if(this.checkFilter(precursorCandidate)) {
					this.identifiers.add(precursorCandidate.getIdentifier());
					this.candidates.add(precursorCandidate);
				}
			}

			for(int i = 0; i < this.candidates.size(); i++) {
				java.util.Iterator<String> keys = properties.iterator();
				while(keys.hasNext()) {
					String curKey = keys.next();
					if(!this.candidates.get(i).hasDefinedProperty(curKey)) this.candidates.get(i).setProperty(curKey, "-");
					if(((String)this.candidates.get(i).getProperty(curKey)).equals("")) this.candidates.get(i).setProperty(curKey, "-");
				}
			}
			
			reader.close();
			f.delete();
		}
	}

	private java.io.File curateSDF(String filename) throws IOException {
		BufferedReader breader = new BufferedReader(new FileReader(new java.io.File(filename)));
		java.io.File f = java.io.File.createTempFile("curated_sdf_file", ".sdf");
		BufferedWriter bwriter = new BufferedWriter(new FileWriter(f));
		String line = "";
		boolean skipNextEmpytLine = false;
		while((line = breader.readLine()) != null) {
			line = line.replaceAll("\\s+$", "");
			if(line.equals("") && skipNextEmpytLine) {
				skipNextEmpytLine = false;
				continue;
			} else if(skipNextEmpytLine) {
				skipNextEmpytLine = false;
			}
			if(line.equals("M  END")) skipNextEmpytLine = true;
			bwriter.write(line);
			bwriter.newLine();
		}
		bwriter.close();
		breader.close();
		return f;
	}
	
	public static void main(String[] args) throws Exception {
		Settings settings = new Settings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, "/home/cruttkie/Documents/PhD/MetFrag/debugs/emma/msready/v2.0.16-msready/MetFragWeb_Parameters_sdf_2/ChemistryDashboard-Batch-Search_2018-03-03_09_08_54.sdf");
		//settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, "/tmp/test2.sdf");
		LocalSDFDatabase db = new LocalSDFDatabase(settings);
		System.out.println(db.getCandidateIdentifiers().size());
		
		System.out.println(db.getCandidateByIdentifier(db.getCandidateIdentifiers().get(0)).getProperty(VariableNames.MOLECULAR_FORMULA_NAME));
		
	}
	
}
