package de.ipbhalle.metfraglib.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.exceptions.MultipleHeadersFoundInInputDatabaseException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.process.ProcessingStatus;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
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
public class LocalZippedPSVDatabase extends AbstractDatabase {

	private java.util.ArrayList<ICandidate> candidates;

	public LocalZippedPSVDatabase(Settings settings) {
		super(settings);
	}

	public java.util.ArrayList<String> getCandidateIdentifiers() throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if(this.settings.containsKey(VariableNames.PROCESS_STATUS_OBJECT_NAME) && this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME) != null)
			((ProcessingStatus)this.settings.get(VariableNames.PROCESS_STATUS_OBJECT_NAME)).setRetrievingStatusString("Retrieving Candidates");
		if (this.candidates == null)
			this.readCandidatesFromFile();
		if (this.settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME) != null)
			return this.getCandidateIdentifiers((String[]) settings.get(VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if (this.settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME) != null)
			return this.getCandidateIdentifiers((String) settings.get(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if (this.settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME) != null)
			return this.getCandidateIdentifiers((Double) settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (Double) settings.get(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		ArrayList<String> identifiers = new ArrayList<String>();
		for (ICandidate candidate : candidates) {
			identifiers.add(candidate.getIdentifier());
		}
		return identifiers;
	}

	public ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> identifiers = new ArrayList<String>();
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

	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) throws Exception {
		if (this.candidates == null)
			try {
				this.readCandidatesFromFile();
			} catch (MultipleHeadersFoundInInputDatabaseException e) {
				e.printStackTrace();
			}
		ArrayList<String> identifiers = new ArrayList<String>();
		for (int i = 0; i < this.candidates.size(); i++) {
			if (molecularFormula.equals(this.candidates.get(i).getProperty(VariableNames.MOLECULAR_FORMULA_NAME)))
				identifiers.add(this.candidates.get(i).getIdentifier());
		}
		return identifiers;
	}

	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) throws MultipleHeadersFoundInInputDatabaseException, Exception {
		if (this.candidates == null)
			this.readCandidatesFromFile();
		ArrayList<String> verifiedIdentifiers = new ArrayList<String>();
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

	public void nullify() {}

	/**
	 * @throws MultipleHeadersFoundInInputDatabaseException
	 * @throws IOException
	 * 
	 */
	private void readCandidatesFromFile() throws MultipleHeadersFoundInInputDatabaseException, Exception {
		this.candidates = new java.util.ArrayList<ICandidate>();
		java.io.File f = new java.io.File((String) this.settings.get(VariableNames.LOCAL_DATABASE_PATH_NAME));
		
		BufferedReader reader = null;
		ZipFile zipFile = null;
		InputStream stream = null;
		if (f.isFile()) {
			zipFile = new ZipFile(f.getAbsolutePath());

		    java.util.Enumeration<? extends ZipEntry> entries = zipFile.entries();
		    ZipEntry entry = entries.nextElement();
		    stream = zipFile.getInputStream(entry);
			reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
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
					if(zipFile != null)
						 zipFile.close();
					throw new MultipleHeadersFoundInInputDatabaseException("Found " + colNames[i] + " several times in header!");
				}
				propNameToIndex.put(colNames[i], i);
			}
			java.util.ArrayList<String> identifiers = new java.util.ArrayList<String>();
			String line = "";
			//int internal_identifier = 0;
			while ((line = reader.readLine()) != null) {
				String[] tmp = line.split("\\|");
				ICandidate precursorCandidate = null;
				String identifier = tmp[propNameToIndex.get(VariableNames.IDENTIFIER_NAME)].trim();
				/*
				if(identifiers.contains(identifier)) {
					reader.close();
					throw new Exception();
				}
				*/
				//identifier = String.valueOf(++internal_identifier);
				identifiers.add(identifier);
				try {
					precursorCandidate = new TopDownPrecursorCandidate(tmp[propNameToIndex.get(VariableNames.INCHI_NAME)].trim(), identifier);
				} catch(Exception e) {
					e.printStackTrace();
					System.out.println(line);
					System.exit(1);
				}
				/*
				 * store all read property fields within the candidate container
				 */
				for (int k = 0; k < colNames.length; k++) {
					if (k == propNameToIndex.get(VariableNames.INCHI_NAME) || k == propNameToIndex.get(VariableNames.IDENTIFIER_NAME))
						continue;
					if (propNameToIndex.get(VariableNames.MONOISOTOPIC_MASS_NAME) != null && k == propNameToIndex.get(VariableNames.MONOISOTOPIC_MASS_NAME))
						try {
							precursorCandidate.setProperty(colNames[k], Double.parseDouble(tmp[propNameToIndex.get(colNames[k])]));
						} catch(Exception e) {
							System.err.println("error in file " + f.getName() + " for " + identifier);
							System.err.println("error in column: " + colNames[k] + " " + tmp[propNameToIndex.get(colNames[k])]);
							e.printStackTrace();
						}
					else
						try {
							precursorCandidate.setProperty(colNames[k], tmp[propNameToIndex.get(colNames[k])]);
						}
						catch(Exception e) {
							System.out.println(line);
							System.out.println(colNames[k]);
							System.out.println(propNameToIndex.get(colNames[k]));
							System.out.println(tmp.length);
							System.out.println("cols");
							for(int i = 0; i < tmp.length; i++)
								System.out.println(tmp[i]);
							e.printStackTrace();
						}
				}
				this.candidates.add(precursorCandidate);
			}
		    zipFile.close();
		}
		if (reader != null)
			reader.close();
		if(zipFile != null)
			zipFile.close();
		if(stream != null)
			stream.close();
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
	
	public static void main(String[] args) {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		settings.set(VariableNames.LOCAL_DATABASE_PATH_NAME, "/home/chrisr/Documents/PhD/MetFrag/fingerprint_training/casmi/runs/output/scores/00000_10000/FI_PHXATPHONSXBIL-01_peak.psv.zip");
		LocalZippedPSVDatabase db = new LocalZippedPSVDatabase(settings);
		try {
			ArrayList<String> ids = db.getCandidateIdentifiers();
			System.out.println(ids.size());
			ICandidate cand = db.getCandidateByIdentifier(ids.get(0));
			System.out.println(cand.getInChI());
		} catch (MultipleHeadersFoundInInputDatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
