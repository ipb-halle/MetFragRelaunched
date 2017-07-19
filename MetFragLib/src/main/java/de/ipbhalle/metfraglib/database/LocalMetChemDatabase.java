package de.ipbhalle.metfraglib.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalMetChemDatabase extends LocalPostgresDatabase {
	
	protected String library;
	protected CandidateList tempCandidates;
	protected Vector<String> tempSubstanceIDs = new Vector<String>();
	
	public LocalMetChemDatabase(Settings settings) {
		super(settings);
		
		this.DATABASE_NAME 			= 	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_NAME					); 
		this.PORT					=	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_PORT_NUMBER_NAME		);
		this.SERVER					=	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_SERVER_IP_NAME			);
		
		this.db_user				=	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_USER_NAME				);
		this.db_password			=	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_PASSWORD_NAME			);
		
		this.library 				=	(String) settings.get( 	VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME			);
		
		this.INCHI_COLUMN_NAME			=	"inchi";
		this.SMILES_COLUMN_NAME			=	"smiles";
		this.INCHIKEY1_COLUMN_NAME		=	"inchi_key_1";
		this.INCHIKEY2_COLUMN_NAME		=	"inchi_key_2";
		this.CID_COLUMN_NAME			=	"accession";
		this.FORMULA_COLUMN_NAME		=	"molecular_formula";
		this.MASS_COLUMN_NAME			=	"monoisotopic_mass";
		this.COMPOUND_NAME_COLUMN_NAME	=	"name";
		
	}
	
	protected void fillCandidateVectors(ResultSet rs) {
		this.tempCandidates = new CandidateList();
		this.tempSubstanceIDs = new Vector<String>();
		try {
			while(rs.next()) {
				ICandidate candidate = new TopDownPrecursorCandidate(rs.getString(this.INCHI_COLUMN_NAME), rs.getString(this.CID_COLUMN_NAME));
				candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, rs.getString(this.INCHIKEY1_COLUMN_NAME));
				candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, rs.getString(this.INCHIKEY2_COLUMN_NAME));
				candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, rs.getString(this.INCHI_COLUMN_NAME).split("/")[1]);
				candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, rs.getDouble(this.MASS_COLUMN_NAME));
				candidate.setProperty(VariableNames.SMILES_NAME, rs.getString(this.SMILES_COLUMN_NAME));
				candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, "NA");
				candidate.setProperty("XlogP3", "NA");
				try {
					candidate.setProperty("XlogP3", rs.getDouble("xlogp3_aa"));
				}
				catch(org.postgresql.util.PSQLException e) {}
				try {
					candidate.setProperty("XlogP3", rs.getDouble("xlogp3"));
				}
				catch(org.postgresql.util.PSQLException e) {}
				try {
					candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, rs.getString(this.COMPOUND_NAME_COLUMN_NAME));
				}
				catch(org.postgresql.util.PSQLException e) {
					
				}
				catch(java.lang.NullPointerException e) {
					
				}
				
				this.tempSubstanceIDs.add(rs.getString("substance_id"));
				this.tempCandidates.addElement(candidate);
			}
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		logger.info("Fetching candidates from MetChem");
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		
		double error = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		
		String query = "select n.name, s.substance_id, s." + this.CID_COLUMN_NAME + ", comp.compound_id , comp." + this.INCHI_COLUMN_NAME + ", comp." + this.MASS_COLUMN_NAME + ", comp." + this.FORMULA_COLUMN_NAME  + ", comp." + this.SMILES_COLUMN_NAME + ", comp." + this.INCHIKEY1_COLUMN_NAME + ", comp." + this.INCHIKEY2_COLUMN_NAME + " "
				+ "from (select c.compound_id, c." + this.INCHI_COLUMN_NAME + ", c." + this.MASS_COLUMN_NAME + ", c." + this.FORMULA_COLUMN_NAME  + ", c." + this.SMILES_COLUMN_NAME + ", c." + this.INCHIKEY1_COLUMN_NAME + ", c." + this.INCHIKEY2_COLUMN_NAME  + " "
				+ 	"from compound c where c." + this.MASS_COLUMN_NAME + " between '" + (monoisotopicMass - error) + "' and '" + (monoisotopicMass + error) + "') as comp "
				+	"left join substance s on s.compound_id = comp.compound_id "
				+	"left join name n on s.substance_id = n.substance_id where s.library_id='" + library_id + "';";
		
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new Vector<String>();
		this.fillCandidateVectors(rs);
		return this.tempSubstanceIDs;
	}
	
	public Vector<String> getCandidateIdentifiers(String molecularFormula) {
		logger.info("Fetching candidates from MetChem");
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;

		String query = "select n.name, s.substance_id, s." + this.CID_COLUMN_NAME + ", comp.compound_id, comp." + this.INCHI_COLUMN_NAME + ", comp." + this.MASS_COLUMN_NAME + ", comp." + this.FORMULA_COLUMN_NAME  + ", comp." + this.SMILES_COLUMN_NAME + ", comp." + this.INCHIKEY1_COLUMN_NAME + ", comp." + this.INCHIKEY2_COLUMN_NAME + " "
                                + "from (select c.compound_id, c." + this.INCHI_COLUMN_NAME + ", c." + this.MASS_COLUMN_NAME + ", c." + this.FORMULA_COLUMN_NAME  + ", c." + this.SMILES_COLUMN_NAME + ", c." + this.INCHIKEY1_COLUMN_NAME + ", c." + this.INCHIKEY2_COLUMN_NAME  + " "
                                +       "from compound c where c." + this.FORMULA_COLUMN_NAME + " = '" + molecularFormula + "'"
                                +		" or c." + this.FORMULA_COLUMN_NAME + " = '" + molecularFormula + "+'" 
                                +		" or c." + this.FORMULA_COLUMN_NAME + " = '" + molecularFormula + "-'"
                                + 		") as comp "
                                +       "left join substance s on s.compound_id = comp.compound_id "
                                +       "left join name n on s.substance_id = n.substance_id where s.library_id='" + library_id + "';";
		System.out.println(query);
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new Vector<String>();
		this.fillCandidateVectors(rs);
		return this.tempSubstanceIDs;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) {
		logger.info("Fetching candidates from MetChem");
		if(identifiers.size() == 0) return new Vector<String>();
		
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		
		String identifierString = "'" + identifiers.get(0) + "'";
		for(String cid : identifiers)
			identifierString += ",'" + cid + "'";
		
		String query = "select s.substance_id as substance_id, s." + this.CID_COLUMN_NAME + " as " + this.CID_COLUMN_NAME + ", n." + this.COMPOUND_NAME_COLUMN_NAME 
				+ " as " + this.COMPOUND_NAME_COLUMN_NAME + ", c." + this.SMILES_COLUMN_NAME + " as " + this.SMILES_COLUMN_NAME 
				+ ", c." + this.INCHI_COLUMN_NAME + " as " + this.INCHI_COLUMN_NAME + ", c." 
				+ this.MASS_COLUMN_NAME + " as " + this.MASS_COLUMN_NAME + " ," + " c." + this.FORMULA_COLUMN_NAME 
				+ " as " + this.FORMULA_COLUMN_NAME + ", c." + this.INCHIKEY1_COLUMN_NAME + " as " + this.INCHIKEY1_COLUMN_NAME 
				+ ", c." + this.INCHIKEY2_COLUMN_NAME + " as " + this.INCHIKEY2_COLUMN_NAME + " " +
				"from compound c inner join substance s on s.compound_id = c.compound_id " +
				"inner join name n on n.substance_id = s.substance_id " +
				"where s." + this.CID_COLUMN_NAME + " in (" + identifierString + ") " +
				"and s.library_id='" + library_id + "';";

		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new Vector<String>();
		this.fillCandidateVectors(rs);
		return this.tempSubstanceIDs;
	}

	public Vector<String> getCandidateIdentifiers(String[] identifiers) {
		
		if(identifiers == null || identifiers.length == 0) return new Vector<String>();
		Vector<String> ids = new Vector<String>();
		for(String id : identifiers)
			ids.add(id);
		return this.getCandidateIdentifiers(ids);
	}
	
	/**
	 * identifier is a MetChem internal identifier namely a substance_id
	 */
	public ICandidate getCandidateByIdentifier(String identifier) {
		if(this.tempCandidates == null || this.tempSubstanceIDs == null || this.tempCandidates.getNumberElements() == 0) return null;
		
		for(int i = 0; i < this.tempSubstanceIDs.size(); i++) {
			if(this.tempSubstanceIDs.get(i).equals(identifier)) {
				ICandidate candidate = this.tempCandidates.getElement(i);
				this.tempCandidates = null;
				this.tempSubstanceIDs = null;
				return candidate; 
			}
		}
		
		this.tempCandidates = null;
		this.tempSubstanceIDs = null;
		
		return null;
	}

	/**
	 * 
	 */
	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		if(identifiers.size() == 0 || this.tempSubstanceIDs == null || this.tempCandidates == null || this.tempCandidates.getNumberElements() == 0) 
			return new CandidateList();
		
		CandidateList candidates = new CandidateList();
		for(int j = 0; j < identifiers.size(); j++) {
			for(int i = 0; i < this.tempSubstanceIDs.size(); i++) {
				if(this.tempSubstanceIDs.get(i).equals(identifiers.get(j))) {
					candidates.addElement(this.tempCandidates.getElement(i));
					break;
				}
			}
		}	
		
		this.tempCandidates = null;
		this.tempSubstanceIDs = null;
		
		return candidates;
	}

	/**
	 * get library id of this.library
	 * if not available return -1
	 * 
	 * @return
	 */
	protected int getLibraryIdentifier() {
		String query = "select library_id from library where library_name=\'" + this.library + "\';";
		ResultSet resultSet = this.submitQuery(query);
		int library_id = -1;
		if(resultSet == null) return -1;
		try {
			while(resultSet.next()) {
				library_id = resultSet.getInt("library_id");
				break;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
		return library_id;
	}
	
}
