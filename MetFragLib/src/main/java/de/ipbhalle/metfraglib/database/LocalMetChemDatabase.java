package de.ipbhalle.metfraglib.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalMetChemDatabase extends LocalPostgresDatabase {
	
	protected String library;
	
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
		//this.FORMULA_COLUMN_NAME		=	"molecular_formula";
		//this.MASS_COLUMN_NAME			=	"monoisotopic_mass";
		this.FORMULA_COLUMN_NAME		=	"formula";
		this.MASS_COLUMN_NAME			=	"exact_mass";
		this.COMPOUND_NAME_COLUMN_NAME	=	"name";
		
	}

	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		
		double error = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		
		String query = "select substance." + this.CID_COLUMN_NAME + " from substance, compound" 
				+ " where compound." + this.MASS_COLUMN_NAME + " between " + (monoisotopicMass - error) + " and " + (monoisotopicMass + error)
				+ " and substance.compound_id=compound.compound_id and substance.library_id='" + library_id + "';"; 
		
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(this.CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public Vector<String> getCandidateIdentifiers(String molecularFormula) {
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		
		String query = "select substance." + this.CID_COLUMN_NAME + " from substance, compound" 
				+ " where compound." + this.FORMULA_COLUMN_NAME + "='" + molecularFormula + "'" 
				+ " and substance.compound_id=compound.compound_id and substance.library_id='" + library_id + "';"; 
		
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(this.CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new Vector<String>();
		
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		
		String identifierString = identifiers.get(0);
		for(String cid : identifiers)
			identifierString += ",'" + cid + "'";
		String query = "select " + this.CID_COLUMN_NAME + " from substance "
				+ "where " + this.CID_COLUMN_NAME + " in (" + identifierString + ") and substance.library_id='" + library_id + "';"; 
		
		
		
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(this.CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public ICandidate getCandidateByIdentifier(String identifier)
			throws DatabaseIdentifierNotFoundException {
					
		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return null;
		String query = "select substance." + this.CID_COLUMN_NAME + " as " + this.CID_COLUMN_NAME + ", name." + this.COMPOUND_NAME_COLUMN_NAME 
				+ " as " + this.COMPOUND_NAME_COLUMN_NAME + ", compound." + this.SMILES_COLUMN_NAME + " as " + this.SMILES_COLUMN_NAME 
				+ ", compound." + this.INCHI_COLUMN_NAME + " as " + this.INCHI_COLUMN_NAME + ", compound." 
				+ this.MASS_COLUMN_NAME + " as " + this.MASS_COLUMN_NAME + " ," + " compound." + this.FORMULA_COLUMN_NAME 
				+ " as " + this.FORMULA_COLUMN_NAME + ", compound." + this.INCHIKEY1_COLUMN_NAME + " as " + this.INCHIKEY1_COLUMN_NAME 
				+ ", compound." + this.INCHIKEY2_COLUMN_NAME + " as " + this.INCHIKEY2_COLUMN_NAME 
				+ ", substance.compound_id, substance.substance_id from substance, compound, name" 
				+ " where substance.library_id='" + library_id + "'" 
				+ " and substance." + this.CID_COLUMN_NAME + " = '" + identifier + "' "
				+ " and substance.compound_id=compound.compound_id" 
				+ " and name.substance_id=substance.substance_id"; 		
		
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return null;
		Vector<String> inchis = new Vector<String>();
		Vector<String> inChIKeys1 = new Vector<String>();
		Vector<String> inChIKeys2 = new Vector<String>();
		Vector<String> formulas = new Vector<String>();
		Vector<String> names = new Vector<String>();
		Vector<String> smiles = new Vector<String>();
		Vector<Double> masses = new Vector<Double>();
		Vector<Double> xlogps = new Vector<Double>();
		try {
			while(rs.next()) {
				inchis.add(rs.getString(this.INCHI_COLUMN_NAME));
				inChIKeys1.add(rs.getString(this.INCHIKEY1_COLUMN_NAME));
				inChIKeys2.add(rs.getString(this.INCHIKEY2_COLUMN_NAME));
				smiles.add(rs.getString(this.SMILES_COLUMN_NAME));
				masses.add(rs.getDouble(this.MASS_COLUMN_NAME));
				formulas.add(rs.getString(this.INCHI_COLUMN_NAME).split("/")[1]);
				String name = "NA";
				Double xlogp3 = null;
				try {
					xlogp3 = rs.getDouble("xlogp3_aa");
				}
				catch(org.postgresql.util.PSQLException e) {}
				try {
					xlogp3 = rs.getDouble("xlogp3");
				}
				catch(org.postgresql.util.PSQLException e) {}
				try {
					name = rs.getString(this.COMPOUND_NAME_COLUMN_NAME);
				}
				catch(org.postgresql.util.PSQLException e) {}
				xlogps.add(xlogp3);
				names.add(name);
			}
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		ICandidate candidate = new TopDownPrecursorCandidate(inchis.get(0), identifier);
		candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, inChIKeys1.get(0));
		candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, inChIKeys2.get(0));
		candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, formulas.get(0));
		candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, masses.get(0));
		candidate.setProperty(VariableNames.SMILES_NAME, smiles.get(0));
		candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, names.get(0));
		if(xlogps.size() != 0) { 
			if(xlogps.get(0) != null) candidate.setProperty("XlogP3", xlogps.get(0));
			else candidate.setProperty("XlogP3", "NA");
		}
		return candidate;
	}

	/**
	 * 
	 */
	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new CandidateList();

		int library_id = this.getLibraryIdentifier();
		if(library_id == -1) return new CandidateList();
		String identifierString = identifiers.get(0) + "'";
		for(String cid : identifiers)
			identifierString += ",'" + cid + "'";
		String query = "select substance." + this.CID_COLUMN_NAME + " as " + this.CID_COLUMN_NAME + ", name." + this.COMPOUND_NAME_COLUMN_NAME 
				+ " as " + this.COMPOUND_NAME_COLUMN_NAME + ", compound." + this.SMILES_COLUMN_NAME + " as " + this.SMILES_COLUMN_NAME 
				+ ", compound." + this.INCHI_COLUMN_NAME + " as " + this.INCHI_COLUMN_NAME + ", compound." 
				+ this.MASS_COLUMN_NAME + " as " + this.MASS_COLUMN_NAME + " ," + " compound." + this.FORMULA_COLUMN_NAME 
				+ " as " + this.FORMULA_COLUMN_NAME + ", compound." + this.INCHIKEY1_COLUMN_NAME + " as " + this.INCHIKEY1_COLUMN_NAME 
				+ ", compound." + this.INCHIKEY2_COLUMN_NAME + " as " + this.INCHIKEY2_COLUMN_NAME 
				+ ", substance.compound_id, substance.substance_id from substance, compound, name" 
				+ " where substance.library_id='" + library_id + "'" 
				+ " and substance." + this.CID_COLUMN_NAME + " in ('" + identifierString + ") "
				+ " and substance.compound_id=compound.compound_id" 
				+ " and name.substance_id=substance.substance_id"; 		
		
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new CandidateList();
		CandidateList candidates = new CandidateList();
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
				catch(org.postgresql.util.PSQLException e) {}
				
				candidates.addElement(candidate);
			}
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return candidates;
	}

	/**
	 * get library id of this.library
	 * if not available return -1
	 * 
	 * @return
	 */
	protected int getLibraryIdentifier() {
		ResultSet resultSet = this.submitQuery("select library_id from library where library_name=\'" + this.library + "\';");
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
