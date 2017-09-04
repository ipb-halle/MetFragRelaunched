package de.ipbhalle.metfraglib.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalPostgresDatabase extends AbstractLocalDatabase {
	
	public LocalPostgresDatabase(Settings settings) {
		super(settings);
	}

	public ArrayList<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		double error = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		String query = "SELECT " + this.CID_COLUMN_NAME + " from " 
				+ this.TABLE_NAME + " where " + this.MASS_COLUMN_NAME 
				+ " between " + (monoisotopicMass - error)+" and "+(monoisotopicMass + error)+";";
		ResultSet rs = this.submitQuery(query);
		ArrayList<String> cids = new ArrayList<String>();
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

	public ArrayList<String> getCandidateIdentifiers(String molecularFormula) {
		String query = "SELECT " + this.CID_COLUMN_NAME + " from " + this.TABLE_NAME 
				+ " where " + this.FORMULA_COLUMN_NAME + " = \'" + molecularFormula + "\';";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		ArrayList<String> cids = new ArrayList<String>();
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

	public ArrayList<String> getCandidateIdentifiers(ArrayList<String> identifiers) {
		if(identifiers.size() == 0) return new ArrayList<String>();
		String query = "SELECT " + this.CID_COLUMN_NAME + " from " + this.TABLE_NAME 
				+ " where " + this.CID_COLUMN_NAME + " =\'" + identifiers.get(0) + "\'";
		for(int i = 1; i < identifiers.size(); i++)
			query += "or " + this.CID_COLUMN_NAME + " =\'" + identifiers.get(i) + "\'";
		query += ";";
		ResultSet rs = this.submitQuery(query);
		ArrayList<String> cids = new ArrayList<String>();
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
		String fields = this.INCHI_COLUMN_NAME + "," + this.INCHIKEY1_COLUMN_NAME 
				+ "," + this.INCHIKEY2_COLUMN_NAME + "," + this.SMILES_COLUMN_NAME + "," + this.MASS_COLUMN_NAME;
		if(this.COMPOUND_NAME_COLUMN_NAME != null && this.COMPOUND_NAME_COLUMN_NAME.length() != 0) fields += "," +this.COMPOUND_NAME_COLUMN_NAME;
		String query = "SELECT " + fields + " from " + this.TABLE_NAME + " where " 
				+ this.CID_COLUMN_NAME + " =\'" + identifier + "\';";
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return null;
		ArrayList<String> inchis = new ArrayList<String>();
		ArrayList<String> inChIKeys1 = new ArrayList<String>();
		ArrayList<String> inChIKeys2 = new ArrayList<String>();
		ArrayList<String> formulas = new ArrayList<String>();
		ArrayList<String> smiles = new ArrayList<String>();
		ArrayList<Double> masses = new ArrayList<Double>();
		ArrayList<String> names = new ArrayList<String>();
		try {
			while(rs.next()) {
				inchis.add(rs.getString(this.INCHI_COLUMN_NAME));
				inChIKeys1.add(rs.getString(this.INCHIKEY1_COLUMN_NAME));
				inChIKeys2.add(rs.getString(this.INCHIKEY2_COLUMN_NAME));
				smiles.add(rs.getString(this.SMILES_COLUMN_NAME));
				masses.add(rs.getDouble(this.MASS_COLUMN_NAME));
				formulas.add(rs.getString(this.INCHI_COLUMN_NAME).split("/")[1]);
				if(rs.getString(this.COMPOUND_NAME_COLUMN_NAME) != null) names.add(rs.getString(this.COMPOUND_NAME_COLUMN_NAME));
				else names.add("NA");
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
		
		return candidate;
	}

	/**
	 * 
	 */
	public CandidateList getCandidateByIdentifier(ArrayList<String> identifiers) {
		if(identifiers.size() == 0) return new CandidateList();
	/*	String query = "SELECT " + this.CID_COLUMN_NAME + ", " 
				+ this.INCHI_COLUMN_NAME + "," + this.INCHIKEY1_COLUMN_NAME + "," 
				+ this.INCHIKEY2_COLUMN_NAME + " from " + this.TABLE_NAME + " where " 
				+ this.CID_COLUMN_NAME + " =\'" + identifiers.get(0) + "\'"; */
		String fields = this.CID_COLUMN_NAME + "," + this.INCHI_COLUMN_NAME + "," + this.INCHIKEY1_COLUMN_NAME 
				+ "," + this.INCHIKEY2_COLUMN_NAME + "," + this.SMILES_COLUMN_NAME + "," + this.MASS_COLUMN_NAME;
		if(this.COMPOUND_NAME_COLUMN_NAME != null && this.COMPOUND_NAME_COLUMN_NAME.length() != 0) fields += "," +this.COMPOUND_NAME_COLUMN_NAME;
		String query = "SELECT " + fields + " from " + this.TABLE_NAME + " where " 
				+ this.CID_COLUMN_NAME + " in (\'" + identifiers.get(0) + "\'";
		
		for(String cid : identifiers)
			query += ",\'" + cid + "\'";
		query += ");";

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
				if(rs.getString(this.COMPOUND_NAME_COLUMN_NAME) != null) candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, rs.getString(this.COMPOUND_NAME_COLUMN_NAME));
				else candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, "NA");
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
	 * 
	 * @param query
	 * @return
	 */
	protected ResultSet submitQuery(String query) {
		ResultSet rs = null;
		try {
			this.driver = new org.postgresql.Driver();
			DriverManager.registerDriver (this.driver);
			String jdbcString = "jdbc:postgresql://" + this.SERVER + ":" + this.PORT + "/" + this.DATABASE_NAME;
			this.databaseConnection = DriverManager.getConnection(jdbcString, this.db_user, this.db_password);
				this.statement = this.databaseConnection.createStatement();
			rs = this.statement.executeQuery(query);
		//    st.close();
		   	SQLWarning warning = rs.getWarnings();
		    if(warning != null) System.err.println("error code: " + warning.getErrorCode());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				this.databaseConnection.close();
				DriverManager.deregisterDriver(this.driver);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return rs;
	}

}
