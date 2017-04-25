package de.ipbhalle.metfrag.misc;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.ipbhalle.metfraglib.database.LocalPropertyFileDatabase;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class CalculateMassOfFormula {

	public static void main(String[] args) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		String stringFormula = args[0];
		boolean withOutput = false;
		if(args.length == 2) withOutput = true;
		java.io.File file = new java.io.File(stringFormula);
		if(!file.exists() || !file.canRead()) {
			try {
				HDByteMolecularFormula formula = new HDByteMolecularFormula(stringFormula);
				System.out.println(formula.toString() + " " + formula.getMonoisotopicMass());
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
		}
		else {
			try {
				MetFragGlobalSettings settings = new MetFragGlobalSettings();
				settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, file.getAbsolutePath());
				
				LocalPropertyFileDatabase db = new LocalPropertyFileDatabase(settings);
				java.util.Vector<String> ids = db.getCandidateIdentifiers();
				CandidateList candidates = db.getCandidateByIdentifier(ids);
				System.out.println("Read " + candidates.getNumberElements() + " entries.");
				java.io.BufferedWriter bwriter = null;
				if(withOutput) {
					bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(args[1])));
				}
				for(int i = 0; i < candidates.getNumberElements(); i++) {
					try {
						if(((String)candidates.getElement(i).getProperty(VariableNames.MOLECULAR_FORMULA_NAME)).contains(".")) {
							System.out.println("discarded " + candidates.getElement(i).getIdentifier());
							continue;
						}
						System.out.println((String)candidates.getElement(i).getProperty(VariableNames.MOLECULAR_FORMULA_NAME));
						HDByteMolecularFormula formula = new HDByteMolecularFormula((String)candidates.getElement(i).getProperty(VariableNames.MOLECULAR_FORMULA_NAME));
						java.util.Enumeration<String> keys = candidates.getElement(i).getProperties().keys();
						String line = "";
						while(keys.hasMoreElements()) {
							String currentKey = keys.nextElement();
							if(currentKey.equals(VariableNames.MONOISOTOPIC_MASS_NAME)) continue;
							line += candidates.getElement(i).getProperty(currentKey) + "|";
						}
						line += formula.getMonoisotopicMass();
						if(withOutput) {
							bwriter.write(line);
							bwriter.newLine();
						}
						else System.out.println(line);
					} catch (AtomTypeNotKnownFromInputListException e) {
						System.out.println("discarded " + candidates.getElement(i).getIdentifier());
						continue;
					} catch (java.lang.NumberFormatException e) {
						System.out.println("discarded " + candidates.getElement(i).getIdentifier());
						continue;
					}
				}
				if(withOutput) {
					bwriter.close();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
