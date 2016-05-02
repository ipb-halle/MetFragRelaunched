package de.ipbhalle.metfraglib.database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.Vector;

import de.ipbhalle.metfraglib.additionals.MathTools;
import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalDerivatisedKeggDatabase extends AbstractLocalDatabase {

	public LocalDerivatisedKeggDatabase(Settings settings) {
		super(settings);
		
		this.DATABASE_NAME 					= 	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_NAME							); 
		this.TABLE_NAME						=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_TABLE_NAME			);
		this.PORT							=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PORT_NUMBER_NAME				);
		this.SERVER							=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SERVER_IP_NAME				);
		this.MASS_COLUMN_NAME				=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_MASS_COLUMN_NAME				);
		this.FORMULA_COLUMN_NAME			=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_FORMULA_COLUMN_NAME			);
		this.INCHI_COLUMN_NAME				=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHI_COLUMN_NAME			);
		this.INCHIKEY1_COLUMN_NAME			=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME		);
		this.INCHIKEY2_COLUMN_NAME			=	(String) settings.get(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME		);
		this.CID_COLUMN_NAME				=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_CID_COLUMN_NAME				);
		this.SMILES_COLUMN_NAME				=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SMILES_COLUMN_NAME			);
		this.COMPOUND_NAME_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME	);
		
		this.db_user				=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_USER_NAME				);
		this.db_password			=	(String) settings.get( 	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PASSWORD_NAME			);
		
	}

	public Vector<String> getCandidateIdentifiers(double monoisotopicMass, double relativeMassDeviation) {
		double error = MathTools.calculateAbsoluteDeviation(monoisotopicMass, relativeMassDeviation);
		String query = "SELECT " + CID_COLUMN_NAME + " from " 
				+ TABLE_NAME + " where " + MASS_COLUMN_NAME 
				+ " between " + (monoisotopicMass - error) + " and " + (monoisotopicMass + error) + ";";
		logger.trace(query);
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
		String query = "SELECT " + CID_COLUMN_NAME + " from " + TABLE_NAME 
				+ " where " + FORMULA_COLUMN_NAME + " = \"" + molecularFormula + "\";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public Vector<String> getCandidateIdentifiers(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new Vector<String>();
		String query = "SELECT " + CID_COLUMN_NAME + " from " + TABLE_NAME 
				+ " where " + CID_COLUMN_NAME + " =\"" + identifiers.get(0) + "\"";
		for(int i = 1; i < identifiers.size(); i++)
			query += "or " + CID_COLUMN_NAME + " =\"" + identifiers.get(i) + "\"";
		query += ";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		Vector<String> cids = new Vector<String>();
		if(rs == null) return cids;
		try {
			while(rs.next()) cids.add(rs.getString(CID_COLUMN_NAME));
			rs.close();
			this.statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return cids;
	}

	public ICandidate getCandidateByIdentifier(String identifier) {
		String query = "SELECT " + INCHI_COLUMN_NAME + "," + INCHIKEY1_COLUMN_NAME 
				+ "," + INCHIKEY2_COLUMN_NAME + "," + FORMULA_COLUMN_NAME + "," + MASS_COLUMN_NAME + "," + COMPOUND_NAME_COLUMN_NAME + " from " + TABLE_NAME + " where " 
				+ CID_COLUMN_NAME + " =\"" + identifier + "\";";;
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return null;
		Vector<String> inChIKeys1 = new Vector<String>();
		Vector<String> inChIKeys2 = new Vector<String>();
		Vector<String> formulas = new Vector<String>();
		Vector<String> inchis = new Vector<String>();
		Vector<Double> masses = new Vector<Double>();
		Vector<String> names = new Vector<String>();
		
		try {
			while(rs.next()) {
				inchis.add(rs.getString(INCHI_COLUMN_NAME));
				inChIKeys1.add(rs.getString(INCHIKEY1_COLUMN_NAME));
				inChIKeys2.add(rs.getString(INCHIKEY2_COLUMN_NAME));
				try {
					masses.add(rs.getDouble(MASS_COLUMN_NAME));
				}
				catch(Exception e) {
					try {
						masses.add(Double.parseDouble((String)rs.getString(MASS_COLUMN_NAME)));
					}
					catch(Exception e1) {
						e1.printStackTrace();
						return null;
					}
				}
				formulas.add(rs.getString(FORMULA_COLUMN_NAME));
				names.add(rs.getString(COMPOUND_NAME_COLUMN_NAME));
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
		candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, names.get(0));
		
		return candidate;
	}

	public CandidateList getCandidateByIdentifier(Vector<String> identifiers) {
		if(identifiers.size() == 0) return new CandidateList();
		String query = "SELECT " + CID_COLUMN_NAME + ", " 
				+ INCHI_COLUMN_NAME + "," + INCHIKEY1_COLUMN_NAME + "," 
				+ INCHIKEY2_COLUMN_NAME + "," + FORMULA_COLUMN_NAME + "," + MASS_COLUMN_NAME + "," + COMPOUND_NAME_COLUMN_NAME + " from " + TABLE_NAME + " where " 
				+ CID_COLUMN_NAME + " =\"" + identifiers.get(0) + "\"";
		for(String cid : identifiers)
			query += " or " + CID_COLUMN_NAME + " = \"" + cid + "\"";
		query += ";";
		logger.trace(query);
		ResultSet rs = this.submitQuery(query);
		if(rs == null) return new CandidateList();
		CandidateList candidates = new CandidateList();
		try {
			while(rs.next()) {
				String inchi = rs.getString(INCHI_COLUMN_NAME);
				ICandidate candidate = new TopDownPrecursorCandidate(inchi, rs.getString(CID_COLUMN_NAME));
				
				candidate.setProperty(VariableNames.INCHI_KEY_1_NAME, rs.getString(INCHIKEY1_COLUMN_NAME));
				candidate.setProperty(VariableNames.INCHI_KEY_2_NAME, rs.getString(INCHIKEY2_COLUMN_NAME));
				candidate.setProperty(VariableNames.MOLECULAR_FORMULA_NAME, rs.getString(FORMULA_COLUMN_NAME));
				candidate.setProperty(VariableNames.COMPOUND_NAME_NAME, rs.getString(COMPOUND_NAME_COLUMN_NAME));
				try {
					candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, rs.getDouble(MASS_COLUMN_NAME));
				}
				catch(Exception e) {
					try {
						candidate.setProperty(VariableNames.MONOISOTOPIC_MASS_NAME, Double.parseDouble((String)rs.getString(MASS_COLUMN_NAME)));
					}
					catch(Exception e1) {
						continue;
					}
				}
				
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
	private ResultSet submitQuery(String query) {
		ResultSet rs = null; 
		try {
			this.driver = new com.mysql.jdbc.Driver();
			DriverManager.registerDriver (this.driver);
			this.databaseConnection = DriverManager.getConnection("jdbc:mysql://" + SERVER + ":" + PORT + "/" + DATABASE_NAME, this.db_user, this.db_password);
			this.statement = this.databaseConnection.createStatement();
			rs = this.statement.executeQuery(query);
		    SQLWarning warning = rs.getWarnings();
		    if(warning != null) logger.error("error code: " + warning.getErrorCode());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

}
