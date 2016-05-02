package de.ipbhalle.metfraglib.tools;

import java.io.IOException;
import java.util.Vector;

import de.ipbhalle.metfraglib.database.LocalPSVDatabase;
import de.ipbhalle.metfraglib.database.OnlineExtendedPubChemDatabase;
import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfraglib.writer.CandidateListWriterPSV;

public class DownloadEntriesFromPubChem {
	
	public static void main(String[] args) {
		if(args.length == 1) downloadFromString(args[0]);
		else if(args.length == 2) downloadFromCandidateFile(args[0], args[1]);
	}
	
	public static void downloadFromCandidateFile(String filenameIn, String filenameOut) {
		MetFragGlobalSettings settingsIn = new MetFragGlobalSettings();
		settingsIn.set(VariableNames.LOCAL_DATABASE_PATH_NAME, filenameIn);
		LocalPSVDatabase dbIn = new LocalPSVDatabase(settingsIn);
		Vector<String> identifiers = null;
		try {
			identifiers = dbIn.getCandidateIdentifiers();
		} catch (MultipleHeadersFoundInInputDatabaseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CandidateList candidates = dbIn.getCandidateByIdentifier(identifiers);
		String[] ids = new String[candidates.getNumberElements()];
		for(int i = 0; i < ids.length; i++)
			ids[i] = candidates.getElement(i).getIdentifier();
		MetFragGlobalSettings settingsPubChem = new MetFragGlobalSettings();
		settingsPubChem.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids);
		OnlineExtendedPubChemDatabase pubchemDB = new OnlineExtendedPubChemDatabase(settingsPubChem);
		try {
			identifiers = pubchemDB.getCandidateIdentifiers();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		CandidateList candidatesPubChem = null;
		try {
			candidatesPubChem = pubchemDB.getCandidateByIdentifier(identifiers);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		for(int i = 0; i < candidatesPubChem.getNumberElements(); i++) {
			String identifier = candidatesPubChem.getElement(i).getIdentifier();
			try {
				ICandidate currentCandidate = dbIn.getCandidateByIdentifier(identifier);
				currentCandidate.setProperty(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME, candidatesPubChem.getElement(i).getProperty(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME));
				currentCandidate.setProperty(VariableNames.PUBCHEM_NUMBER_PATENTS_NAME, candidatesPubChem.getElement(i).getProperty(VariableNames.PUBCHEM_NUMBER_PATENTS_NAME));
			} catch (DatabaseIdentifierNotFoundException e) {
				e.printStackTrace();
			}
		}
		CandidateListWriterPSV writer = new CandidateListWriterPSV();
		String filename = filenameOut.replaceAll(".*\\/", "").replaceAll("\\..*$", "");
		String path = filenameOut.replaceAll(filename + "\\..*$", "");
		try {
			writer.write(candidates, filename, path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void downloadFromString(String idString) {
		String[] ids = idString.trim().split(",");
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ids);
		OnlineExtendedPubChemDatabase db = new OnlineExtendedPubChemDatabase(settings);
		CandidateList candidates = null;
		try {
			candidates = db.getCandidateByIdentifier(db.getCandidateIdentifiers());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		for(int i = 0; i < candidates.getNumberElements(); i++) {
			ICandidate candidate = candidates.getElement(i);
			try {
				System.out.println(candidate.getIdentifier() + "|" + candidate.getInChI() + "|" + candidate.getMolecularFormula().toString() + "|" + 
						candidate.getMolecularFormula().getMonoisotopicMass() + "|" + 
						candidate.getProperty(VariableNames.PUBCHEM_XLOGP_NAME) + "|" +
						candidate.getProperty(VariableNames.INCHI_KEY_1_NAME)+ "|" +
						candidate.getProperty(VariableNames.INCHI_KEY_2_NAME)+ "|" +
						candidate.getProperty(VariableNames.PUBCHEM_NUMBER_PATENTS_NAME)+ "|" +
						candidate.getProperty(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME) + "|" + 
						candidate.getProperty(VariableNames.SMILES_NAME));
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
		}
	}
	
}
