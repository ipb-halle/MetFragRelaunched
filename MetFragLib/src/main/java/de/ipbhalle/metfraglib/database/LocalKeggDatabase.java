package de.ipbhalle.metfraglib.database;

import java.util.ArrayList;

import de.ipbhalle.metfraglib.exceptions.DatabaseIdentifierNotFoundException;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.CandidateList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class LocalKeggDatabase extends LocalPostgresDatabase {
	
	public LocalKeggDatabase(Settings settings) {
		super(settings);
		
		this.DATABASE_NAME 			= 	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_NAME					); 
		this.TABLE_NAME				=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_TABLE_NAME	);
		this.PORT					=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_PORT_NUMBER_NAME		);
		this.SERVER					=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_SERVER_IP_NAME			);
		this.MASS_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_MASS_COLUMN_NAME		);
		this.FORMULA_COLUMN_NAME	=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_FORMULA_COLUMN_NAME	);
		this.INCHI_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_INCHI_COLUMN_NAME		);
		this.INCHIKEY1_COLUMN_NAME	=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME	);
		this.INCHIKEY2_COLUMN_NAME	=	(String) settings.get(	VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME	);
		this.CID_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_CID_COLUMN_NAME		);
		this.SMILES_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_SMILES_COLUMN_NAME	);
		this.COMPOUND_NAME_COLUMN_NAME		=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME	);
		
		this.db_user				=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_USER_NAME				);
		this.db_password			=	(String) settings.get( 	VariableNames.LOCAL_KEGG_DATABASE_PASSWORD_NAME			);
		
	}

	public ICandidate getCandidateByIdentifier(String identifier)
			throws DatabaseIdentifierNotFoundException {
		ICandidate candidate = super.getCandidateByIdentifier(identifier);

		return candidate;
	}
	
	/**
	 * 
	 */
	public CandidateList getCandidateByIdentifier(ArrayList<String> identifiers) {
		CandidateList candidateList = super.getCandidateByIdentifier(identifiers);

		return candidateList;
	}

}
