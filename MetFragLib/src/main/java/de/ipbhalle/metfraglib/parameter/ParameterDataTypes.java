package de.ipbhalle.metfraglib.parameter;

import java.util.HashMap;

import org.apache.log4j.Level;

import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;

public class ParameterDataTypes {

	/*
	 * stores the datatype of the related parameter
	 */
	private static final HashMap<String, String> parameterDatatypes = new HashMap<String, String>();
	static {
		parameterDatatypes.put(	VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, 							"Double"	);
		parameterDatatypes.put(	VariableNames.CHEMSPIDER_TOKEN_NAME, 									"String"	);
		parameterDatatypes.put(	VariableNames.CONSIDER_HYDROGEN_SHIFTS_NAME, 							"Boolean"	);
		parameterDatatypes.put(	VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, 					"Double"	);
		parameterDatatypes.put(	VariableNames.IS_POSITIVE_ION_MODE_NAME, 								"Boolean"	);
		parameterDatatypes.put(	VariableNames.MAXIMUM_TREE_DEPTH_NAME, 									"Byte"		);
		parameterDatatypes.put(	VariableNames.PEAK_LIST_PATH_NAME, 										"String"	);
		parameterDatatypes.put(	VariableNames.PEAK_LIST_STRING_NAME, 									"String"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_DATABASE_IDS_NAME, 								"String[]"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_ION_MODE_NAME, 									"Integer"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_ION_MODE_STRING_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 								"Double"	);
		parameterDatatypes.put(	VariableNames.PRECURSOR_ION_MASS_NAME, 									"Double"	);
		parameterDatatypes.put(	VariableNames.RELATIVE_MASS_DEVIATION_NAME, 							"Double"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_PATH_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME, 					"Double"	);
		parameterDatatypes.put(	VariableNames.MAXIMUM_CANDIDATE_LIMIT_TO_STOP_NAME, 					"Integer"	);
		parameterDatatypes.put(	VariableNames.PROCESS_CANDIDATES,					 					"Boolean"	);
		parameterDatatypes.put(	VariableNames.USE_SMILES_NAME,					 						"Boolean"	);
		
		parameterDatatypes.put(	VariableNames.METFRAG_DATABASE_TYPE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.COMBINED_REFERENCE_SCORE_VALUES,							"String[]"	);
		parameterDatatypes.put(	VariableNames.METFRAG_FRAGMENTER_TYPE_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.METFRAG_ASSIGNER_TYPE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.METFRAG_SCORE_TYPES_NAME, 								"String[]"	);
		parameterDatatypes.put(	VariableNames.METFRAG_SCORE_WEIGHTS_NAME, 								"Double[]"	);
		parameterDatatypes.put(	VariableNames.METFRAG_CANDIDATE_FRAGMENT_WRITER_NAME,					"String"	);
		parameterDatatypes.put(	VariableNames.METFRAG_CANDIDATE_WRITER_NAME, 							"String[]"	);
		parameterDatatypes.put(	VariableNames.METFRAG_CANDIDATE_IMAGE_GENERATOR_NAME,					"String"	);
		parameterDatatypes.put(	VariableNames.METFRAG_FRAGMENT_IMAGE_GENERATOR_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.METFRAG_UNIQUE_FRAGMENT_MATCHES,		 					"Boolean"	);
		
		parameterDatatypes.put(	VariableNames.SAMPLE_NAME, 												"String"	);
		parameterDatatypes.put(	VariableNames.STORE_RESULTS_PATH_NAME, 									"String"	);
		parameterDatatypes.put(	VariableNames.STORE_RESULTS_FILE_NAME, 									"String"	);
		parameterDatatypes.put(	VariableNames.NUMBER_THREADS_NAME, 										"Byte"		);
		parameterDatatypes.put(	VariableNames.MAXIMUM_NUMBER_OF_TOPDOWN_FRAGMENT_ADDED_TO_QUEUE, 		"Byte"		);
		parameterDatatypes.put(	VariableNames.METFRAG_PEAK_LIST_READER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.BOND_ENERGY_FILE_PATH_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME, 					"Double"	);
		parameterDatatypes.put(	VariableNames.LOG_LEVEL_NAME, 											"Level"		);
		parameterDatatypes.put(	VariableNames.MONA_PRESENT_INCHIKEYS_FILE_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.SMARTS_PEAK_ANNOTATION_FILE_NAME, 						"String"	);
		parameterDatatypes.put( VariableNames.OFFLINE_SPECTRAL_DATABASE_FILE_NAME, 						"String"	);
		
		/*
		 * candidate filters
		 */
		parameterDatatypes.put(	VariableNames.PRE_CANDIDATE_FILTER_EXCLUDED_ELEMENTS_NAME,				"String[]"	);
		parameterDatatypes.put(	VariableNames.PRE_CANDIDATE_FILTER_INCLUDED_ELEMENTS_NAME,				"String[]"	);
		parameterDatatypes.put( VariableNames.METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME, 			"String[]"  );
		parameterDatatypes.put( VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME, 			"String[]"  );
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_SMARTS_INCLUSION_LIST_NAME, 			"String[]"  );
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_SMARTS_EXCLUSION_LIST_NAME, 			"String[]"  );
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_SUSPECT_LIST_NAME, 					"String[]"  );
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_MAXIMUM_ELEMENTS_NAME, 				"String"  	);
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_MINIMUM_ELEMENTS_NAME, 				"String"  	);
		parameterDatatypes.put( VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME, 				"String"  	);

		/*
		 * candidate filters
		 */
		parameterDatatypes.put( VariableNames.SCORE_SMARTS_INCLUSION_LIST_NAME, 						"String[]"  );
		parameterDatatypes.put( VariableNames.SCORE_SMARTS_EXCLUSION_LIST_NAME, 						"String[]"  );
		parameterDatatypes.put( VariableNames.SCORE_SUSPECT_LISTS_NAME,			 						"String[]"  );
		parameterDatatypes.put( VariableNames.RETENTION_TIME_TRAINING_FILE_NAME,			 			"String"  	);
		parameterDatatypes.put( VariableNames.USER_LOG_P_VALUE_NAME,						 			"String"  	);
		
		/*
		 * substructure learning
		 */
		parameterDatatypes.put( VariableNames.SMARTS_PEAK_ANNOTATION_FILE_NAME,						 			"String"  	);
		parameterDatatypes.put( VariableNames.SMARTS_LOSS_ANNOTATION_FILE_NAME,						 			"String"  	);
		
		/*
		 * parameters for local database connection
		 */
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_NAME, 										"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_COMPOUND_TABLE_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_PORT_NUMBER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_SERVER_IP_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_MASS_COLUMN_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_FORMULA_COLUMN_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_INCHI_COLUMN_NAME,		 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_INCHIKEY1_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_INCHIKEY2_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_CID_COLUMN_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_SMILES_COLUMN_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_USER_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_PASSWORD_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DATABASE_COMPOUND_NAME_COLUMN_NAME,					"String"	);

		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_TABLE_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_PORT_NUMBER_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_SERVER_IP_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_MASS_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_FORMULA_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_INCHI_COLUMN_NAME,		 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_INCHIKEY1_COLUMN_NAME, 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_INCHIKEY2_COLUMN_NAME, 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_CID_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_SMILES_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_USER_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_PASSWORD_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_PUBCHEM_DATABASE_COMPOUND_NAME_COLUMN_NAME,			"String"	);

		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_TABLE_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_PORT_NUMBER_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_SERVER_IP_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_MASS_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_FORMULA_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_INCHI_COLUMN_NAME,		 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_CID_COLUMN_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_SMILES_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_USER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_PASSWORD_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME,			"String"	);
		
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_COMPOUND_TABLE_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_PORT_NUMBER_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_SERVER_IP_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_MASS_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_FORMULA_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHI_COLUMN_NAME,		 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHIKEY1_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_INCHIKEY2_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_CID_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_SMILES_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_USER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_PASSWORD_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_LIPIDMAPS_DATABASE_COMPOUND_NAME_COLUMN_NAME,			"String"	);

		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_TABLE_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PORT_NUMBER_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SERVER_IP_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_MASS_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_FORMULA_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHI_COLUMN_NAME,		 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY1_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_INCHIKEY2_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_CID_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_SMILES_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_USER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_PASSWORD_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_DERIVATISED_KEGG_DATABASE_COMPOUND_NAME_COLUMN_NAME,			"String"	);

		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_NAME, 								"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_COMPOUND_TABLE_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_PORT_NUMBER_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_SERVER_IP_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_MASS_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_FORMULA_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_INCHI_COLUMN_NAME,		 			"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_INCHIKEY1_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_INCHIKEY2_COLUMN_NAME, 				"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_CID_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_SMILES_COLUMN_NAME, 					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_USER_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_PASSWORD_NAME, 						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_CHEBI_DATABASE_COMPOUND_NAME_COLUMN_NAME,			"String"	);
		
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME,						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_PASSWORD_NAME,						"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_PORT_NUMBER_NAME,					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_SERVER_IP_NAME,					"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_USER_NAME,							"String"	);
		parameterDatatypes.put(	VariableNames.LOCAL_METCHEM_DATABASE_NAME,								"String"	);

		parameterDatatypes.put(	VariableNames.NUMBER_RANDOM_SPECTRA_NAME, 								"Integer"	);
		parameterDatatypes.put(	VariableNames.ENABLE_DEUTERIUM_NAME, 									"Boolean"	);
		/*
		 * email feedback settings
		 */
		parameterDatatypes.put(	VariableNames.FEEDBACK_EMAIL_HOST, 										"String"	);
		parameterDatatypes.put(	VariableNames.FEEDBACK_EMAIL_TO, 										"String"	);
		parameterDatatypes.put(	VariableNames.FEEDBACK_EMAIL_PORT, 										"Integer"	);
		parameterDatatypes.put(	VariableNames.FEEDBACK_EMAIL_PASS, 										"String"	);
		parameterDatatypes.put(	VariableNames.FEEDBACK_EMAIL_USER, 										"String"	);

		/*
		 * email feedback settings
		 */
		parameterDatatypes.put(	VariableNames.MONA_PROXY_SERVER, 										"String"	);
		parameterDatatypes.put(	VariableNames.MONA_PROXY_PORT, 											"Integer"	);
		parameterDatatypes.put(	VariableNames.KEGG_PROXY_SERVER, 										"String"	);
		parameterDatatypes.put(	VariableNames.KEGG_PROXY_PORT, 											"Integer"	);
		parameterDatatypes.put(	VariableNames.METACYC_PROXY_SERVER, 									"String"	);
		parameterDatatypes.put(	VariableNames.METACYC_PROXY_PORT, 										"Integer"	);
		parameterDatatypes.put(	VariableNames.PUBCHEM_PROXY_SERVER, 									"String"	);
		parameterDatatypes.put(	VariableNames.PUBCHEM_PROXY_PORT, 										"Integer"	);
		
		/*
		 * hd settings
		 */
		parameterDatatypes.put(	VariableNames.HD_PRECURSOR_MOLECULAR_FORMULA_NAME, 							"String"	);
		parameterDatatypes.put(	VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME, 								"Double"	);
		parameterDatatypes.put(	VariableNames.HD_PRECURSOR_ION_MASS_NAME, 									"Double"	);
		parameterDatatypes.put(	VariableNames.HD_PEAK_LIST_PATH_NAME, 										"String"	);
		parameterDatatypes.put(	VariableNames.HD_PRECURSOR_ION_MODE_NAME, 									"Integer"	);
		parameterDatatypes.put(	VariableNames.HD_MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME,						"Double"	);
		parameterDatatypes.put(	VariableNames.HD_GROUP_FLAG_FILTER_ENABLED_NAME, 							"Boolean"	);
		
	}
	
	/*
	 * function to retrieve parameters
	 */
	public static Object getParameter(String parameter, String parameterName) throws ParameterNotKnownException {
		String type = parameterDatatypes.get(parameterName);
		if(type == null) throw new ParameterNotKnownException("Parameter '" + parameterName + "' not known.");
		
		if(type.equals("Double")) return Double.parseDouble(parameter);
		if(type.equals("Integer")) return Integer.parseInt(parameter);
		if(type.equals("Byte")) return Byte.parseByte(parameter);
		if(type.equals("Boolean")) return parameter.toLowerCase().equals("true");
		if(type.equals("Level")) return(Level.toLevel(parameter));
		if(type.equals("Double[]")) {
			String[] tmp = parameter.split(",");
			Double[] values = new Double[tmp.length];
			for(int i = 0; i < values.length; i++)
				values[i] = Double.parseDouble(tmp[i]);
			return values;
		}
		if(type.equals("String[]")) {
			String[] tmp = parameter.split(",");
			for(int i = 0; i < tmp.length; i++)
				tmp[i] = tmp[i].trim();
			return tmp;
		}
		return parameter;
	}
	
	public static String getType(String parameterName) {
		if(!parameterDatatypes.containsKey(parameterName)) return "";
		return parameterDatatypes.get(parameterName);
	}
}
