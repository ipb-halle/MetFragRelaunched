package de.ipbhalle.metfraglib.database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;
import de.ipbhalle.metfraglib.process.ProcessingStatus;

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
public class LocalCSVDatabase extends AbstractDatabase {

	private java.util.HashMap<String, ICandidate> candidates;

	public LocalCSVDatabase(Settings settings) {
		super(settings);
	}

	public java.util.Vector<String> getCandidateIdentifiers() throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setRetrievingStatusString("Retrieving Candidates");
		if (this.candidates == null) {
			this.readCandidatesFromFile();
		}
		if (this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if (this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			return this.getCandidateIdentifiers((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if (this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			return this.getCandidateIdentifiers((Double) settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double) settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		Vector<String> identifiers = new Vector<String>();
		java.util.Iterator<String> it = candidates.keySet().iterator();
		while (it.hasNext()) {
			identifiers.add(it.next());
		}
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		Vector<String> identifiers = new Vector<String>();
		double mzabs = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		double lowerLimit = monoisotopicMass - mzabs;
		double upperLimit = monoisotopicMass + mzabs;
		
		java.util.Iterator<String> keyIt = this.candidates.keySet().iterator();
		while(keyIt.hasNext()) {
			String currentKey = keyIt.next();
			double currentMonoisotopicMass = (Double) this.candidates.get(currentKey).getProperty(VariableNames.MONOISOTOPIC_MASS_NAME);
			if (lowerLimit <= currentMonoisotopicMass && currentMonoisotopicMass <= upperLimit)
				identifiers.add(this.candidates.get(currentKey).getIdentifier());
		}
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		if (this.candidates == null)
			try {
				this.readCandidatesFromFile();
			} catch (MultipleHeadersFoundInInputDatabaseException e) {
				e.printStackTrace();
			}
		Vector<String> identifiers = new Vector<String>();
		java.util.Iterator<String> keyIt = this.candidates.keySet().iterator();
		while(keyIt.hasNext()) {
			String currentKey = keyIt.next();
			if (molecularFormula.equals(this.candidates.get(currentKey).getProperty(VariableNames.MOLECULAR_FORMULA_NAME)))
				identifiers.add(this.candidates.get(currentKey).getIdentifier());
		}
		return identifiers;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) throws MultipleHeadersFoundInInputDatabaseException, Exception {
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
		ICandidate candidate = null;
		try {
			candidate = this.candidates.get(identifier);
			if(candidate == null) throw new DatabaseIdentifierNotFoundException(identifier);
		} catch(Exception e) {
			throw new DatabaseIdentifierNotFoundException(identifier);
		}
		return candidate;
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
	 * @throws MultipleHeadersFoundInInputDatabaseException
	 * @throws IOException
	 * 
	 */
	private void readCandidatesFromFile() throws MultipleHeadersFoundInInputDatabaseException, Exception {
		this.candidates = new java.util.HashMap<String, ICandidate>();
		java.io.File f = new java.io.File((String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
		java.util.List<String> propertyNames = new java.util.ArrayList<String>();
		
		BufferedReader reader = null;
		if (f.isFile()) {
			reader = new BufferedReader(new FileReader(f));
			CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
			java.util.Iterator<?> it = parser.getHeaderMap().keySet().iterator();
			boolean identifierColDefined = false;
			boolean inchiColDefined = false;
			while(it.hasNext()) {
				String colname = (String)it.next();
				propertyNames.add(colname);
				if(colname.equals(VariableNames.IDENTIFIER_NAME)) identifierColDefined = true;
				if(colname.equals(VariableNames.INCHI_NAME)) inchiColDefined = true;
			}
			
			if(!identifierColDefined) {
				logger.error("Error: No Identifier column defined.");
				parser.close();
				reader.close();
				throw new Exception();
			}
			if(!inchiColDefined) {
				logger.error("Error: No InChI column defined.");
				parser.close();
				reader.close();
				throw new Exception();
			}
			
			for(CSVRecord record : parser) {
				ICandidate precursorCandidate = new TopDownPrecursorCandidate(record.get(VariableNames.INCHI_NAME), record.get(VariableNames.IDENTIFIER_NAME));
				for(int ii = 0; ii < propertyNames.size(); ii++) {
					String colname = propertyNames.get(ii);
					if(!colname.equals(VariableNames.INCHI_NAME) && !colname.equals(VariableNames.IDENTIFIER_NAME)) {
						if(colname.equals(VariableNames.MONOISOTOPIC_MASS_NAME))
							precursorCandidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, Double.parseDouble(record.get(VariableNames.MONOISOTOPIC_MASS_NAME)));
						else {
							precursorCandidate.setProperty(colname, record.get(colname));
						}
					}	
				}
				this.candidates.put(precursorCandidate.getIdentifier(), precursorCandidate);
			
			}
			
			parser.close();
			reader.close();
			
			return;
		} 
		throw new Exception();
	}
}
