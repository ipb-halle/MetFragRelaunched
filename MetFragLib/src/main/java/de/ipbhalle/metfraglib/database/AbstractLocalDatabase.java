package de.ipbhalle.metfraglib.database;

import java.sql.DriverManager;
import java.sql.SQLException;

import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractLocalDatabase extends AbstractDatabase {

	protected java.sql.Driver driver;
	protected java.sql.Connection databaseConnection;
	protected java.sql.Statement statement;
	
	protected String DATABASE_NAME;
	protected String TABLE_NAME;
	protected String PORT;
	protected String SERVER;
	protected String MASS_COLUMN_NAME;
	protected String FORMULA_COLUMN_NAME;
	protected String INCHI_COLUMN_NAME;
	protected String INCHIKEY1_COLUMN_NAME;
	protected String INCHIKEY2_COLUMN_NAME;
	protected String CID_COLUMN_NAME;
	protected String SMILES_COLUMN_NAME;
	protected String COMPOUND_NAME_COLUMN_NAME;
	
	protected String db_user;
	protected String db_password;

	public AbstractLocalDatabase(Settings settings) {
		super(settings);
		this.DATABASE_NAME 			= 	(String) settings.get( 	VariableNames.LOCAL_DATABASE_NAME								); 
		this.TABLE_NAME				=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_COMPOUND_TABLE_NAME				);
		this.PORT					=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_PORT_NUMBER_NAME					);
		this.SERVER					=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_SERVER_IP_NAME						);
		this.MASS_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_MASS_COLUMN_NAME					);
		this.FORMULA_COLUMN_NAME	=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_FORMULA_COLUMN_NAME				);
		this.INCHI_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_INCHI_COLUMN_NAME					);
		this.INCHIKEY1_COLUMN_NAME	=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_INCHIKEY1_COLUMN_NAME				);
		this.INCHIKEY2_COLUMN_NAME	=	(String) settings.get(	VariableNames.LOCAL_DATABASE_INCHIKEY2_COLUMN_NAME				);
		this.CID_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_CID_COLUMN_NAME					);
		this.SMILES_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_SMILES_COLUMN_NAME					);
		this.COMPOUND_NAME_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_COMPOUND_NAME_COLUMN_NAME	);
		
		this.db_user				=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_USER_NAME				);
		this.db_password			=	(String) settings.get( 	VariableNames.LOCAL_DATABASE_PASSWORD_NAME			);
	}


	public void nullify() {
		try {
			if(this.statement != null) this.statement.close();
			if(this.databaseConnection != null && !this.databaseConnection.isClosed()) this.databaseConnection.close();
			if(this.driver != null) DriverManager.deregisterDriver(this.driver);
			this.statement = null;
			this.databaseConnection = null;
			this.driver = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
