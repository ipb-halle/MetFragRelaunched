package de.ipbhalle.metfraglib.settings;

import java.io.IOException;

import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.ParameterDataTypes;
import de.ipbhalle.metfraglib.parameter.VariableNames;

public class MetFragGlobalSettings extends Settings {

	public MetFragGlobalSettings() {
		super();

		/*
		 * set default values
		 */
		this.defaults.put(VariableNames.PEAK_LIST_NAME, Constants.DEFAULT_PEAK_LIST);
		this.defaults.put(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, Constants.DEFAULT_FRAGMENT_PEAK_ABSOLUTE_MASS_DEV);
		this.defaults.put(VariableNames.RELATIVE_MASS_DEVIATION_NAME, Constants.DEFAULT_FRAGMENT_PEAK_RELATIVE_MASS_DEV);
		this.defaults.put(VariableNames.MAXIMUM_TREE_DEPTH_NAME, Constants.DEFAULT_MAXIMUM_TREE_DEPTH);
		this.defaults.put(VariableNames.IS_POSITIVE_ION_MODE_NAME, Constants.DEFAULT_POSITIVE_IONISATION_MODE);
		this.defaults.put(VariableNames.CONSIDER_HYDROGEN_SHIFTS_NAME, Constants.DEFAULT_CONSIDER_HYDROGEN_MASS_DIFFERENCE);
		this.defaults.put(VariableNames.PRECURSOR_ION_MODE_NAME, Constants.DEFAULT_PRECURSOR_ION_TYPE);
		this.defaults.put(VariableNames.NUMBER_THREADS_NAME, Constants.DEFAULT_NUMBER_THREADS);
		this.defaults.put(VariableNames.NUMBER_OF_DIGITS_AFTER_ROUNDING_NAME, Constants.DEFAULT_NUMBER_OF_DIGITS_AFTER_ROUNDING);
		this.defaults.put(VariableNames.SCORE_NAMES_NOT_TO_SCALE, Constants.DEFAULT_SCORE_NAMES_NOT_TO_SCALE);
		this.defaults.put(VariableNames.PROCESS_CANDIDATES, Constants.DEFAULT_PROCESS_CANDIDATES);
		this.defaults.put(VariableNames.USE_SMILES_NAME, Constants.DEFAULT_USE_SMILES);
		/*
		 * needs to be set externally
		 */
		this.defaults.put(VariableNames.MINIMUM_FRAGMENT_MASS_LIMIT_NAME, Constants.DEFAULT_MINIMUM_FRAGMENT_MASS_LIMIT);
		this.defaults.put(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, Constants.DEFAULT_PRECURSOR_NEUTRAL_MASS);
		this.defaults.put(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, Constants.DEFAULT_DATABASE_RELATIVE_MASS_DEVIATION);
		this.defaults.put(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME, Constants.DEFAULT_PRECURSOR_MOLECULAR_FORMULA);
		this.defaults.put(VariableNames.PRECURSOR_DATABASE_IDS_NAME, Constants.DEFAULT_PRECURSOR_DATABASE_IDS);
		this.defaults.put(VariableNames.PEAK_LIST_PATH_NAME, Constants.DEFAULT_PEAK_LIST_PATH);
		this.defaults.put(VariableNames.MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME, Constants.DEFAULT_MINIMUM_ABSOLUTE_PEAK_INTENSITY);
		this.defaults.put(VariableNames.HD_MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME, Constants.DEFAULT_MINIMUM_ABSOLUTE_PEAK_INTENSITY);

		this.defaults.put(VariableNames.METFRAG_DATABASE_TYPE_NAME, Constants.DEFAULT_METFRAG_DATABASE_TYPE);
		this.defaults.put(VariableNames.METFRAG_FRAGMENTER_TYPE_NAME, Constants.DEFAULT_METFRAG_FRAGMENTER_TYPE);
		this.defaults.put(VariableNames.METFRAG_ASSIGNER_TYPE_NAME, Constants.DEFAULT_METFRAG_ASSIGNER_TYPE);
		this.defaults.put(VariableNames.METFRAG_ASSIGNER_SCORER_NAME, Constants.DEFAULT_METFRAG_ASSIGNER_SCORER);
		this.defaults.put(VariableNames.METFRAG_SCORE_TYPES_NAME, Constants.DEFAULT_METFRAG_SCORE_TYPES);
		this.defaults.put(VariableNames.METFRAG_CANDIDATE_WRITER_NAME, Constants.DEFAULT_METFRAG_CANDIDATE_WRITER);
		this.defaults.put(VariableNames.METFRAG_SCORE_WEIGHTS_NAME, Constants.DEFAULT_METFRAG_SCORE_WEIGHTS);
		this.defaults.put(VariableNames.METFRAG_PEAK_LIST_READER_NAME, Constants.DEFAULT_METFRAG_PEAK_LIST_READER);

		this.defaults.put(VariableNames.METFRAG_IMAGE_WRITER_NAME, Constants.DEFAULT_METFRAG_IMAGE_WRITER);
		this.defaults.put(VariableNames.BOND_ENERGY_FILE_PATH_NAME, Constants.DEFAULT_BOND_ENERGY_FILE_PATH);

		this.defaults.put(VariableNames.LOG_LEVEL_NAME, Constants.DEFAULT_LOG_LEVEL);
		this.defaults.put(VariableNames.MAXIMUM_NUMBER_OF_TOPDOWN_FRAGMENT_ADDED_TO_QUEUE, Constants.DEFAULT_MAXIMUM_NUMBER_OF_TOPDOWN_FRAGMENT_ADDED_TO_QUEUE);
		this.defaults.put(VariableNames.METFRAG_UNIQUE_FRAGMENT_MATCHES, Constants.DEFAULT_METFRAG_UNIQUE_FRAGMENT_MATCHES);

		this.defaults.put(VariableNames.USER_LOG_P_VALUE_NAME, Constants.DEFAULT_LOG_P_VALUE_NAME);
		this.defaults.put(VariableNames.MINIMUM_COSINE_SIMILARITY_LIMIT_NAME, Constants.DEFAULT_MINIMUM_COSINE_SIMILARITY_LIMIT);

		/*
		 * candidate filters
		 */
		this.defaults.put(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME, Constants.DEFAULT_METFRAG_POST_PROCESSING_CANDIDATE_FILTER);
		this.defaults.put(VariableNames.METFRAG_PRE_PROCESSING_CANDIDATE_FILTER_NAME, Constants.DEFAULT_METFRAG_PRE_PROCESSING_CANDIDATE_FILTER);

		/*
		 * for retention time models
		 */
		this.defaults.put(VariableNames.EXPERIMENTAL_RETENTION_TIME_VALUE_NAME, Constants.DEFAULT_EXPERIMENTAL_RETENTION_TIME_VALUE);
		this.defaults.put(VariableNames.NUMBER_RANDOM_SPECTRA_NAME, Constants.DEFAULT_NUMBER_RANDOM_SPECTRA);
		this.defaults.put(VariableNames.MASSBANK_URL, Constants.DEFAULT_MASSBANK_URL);
		this.defaults.put(VariableNames.MASSBANK_RECORD_CACHE_DIRECTORY, Constants.DEFAULT_MASSBANK_RECORD_CACHE_DIRECTORY);
		this.defaults.put(VariableNames.ENABLE_DEUTERIUM_NAME, Constants.DEFAULT_ENABLE_DEUTERIUM);

		/*
		 * parameters for local database connection
		 */
		this.defaults.put(VariableNames.LOCAL_DATABASE_NAME, Constants.DEFAULT_LOCAL_DATABASE);
		this.defaults.put(VariableNames.LOCAL_DATABASE_COMPOUND_TABLE_NAME, Constants.DEFAULT_LOCAL_DATABASE_COMPOUND_TABLE);
		this.defaults.put(VariableNames.LOCAL_DATABASE_PORT_NUMBER_NAME, Constants.DEFAULT_LOCAL_DATABASE_PORT_NUMBER);
		this.defaults.put(VariableNames.LOCAL_DATABASE_SERVER_IP_NAME, Constants.DEFAULT_LOCAL_DATABASE_SERVER_IP);
		this.defaults.put(VariableNames.LOCAL_DATABASE_MASS_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_MASS_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_FORMULA_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_FORMULA_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_INCHIKEY1_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_INCHIKEY1_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_INCHIKEY2_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_INCHIKEY2_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_CID_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_CID_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_SMILES_COLUMN_NAME, Constants.DEFAULT_LOCAL_DATABASE_SMILES_COLUMN);
		this.defaults.put(VariableNames.LOCAL_DATABASE_USER_NAME, Constants.DEFAULT_LOCAL_DATABASE_USER);
		this.defaults.put(VariableNames.LOCAL_DATABASE_PASSWORD_NAME, Constants.DEFAULT_LOCAL_DATABASE_PASSWORD);
		
		this.defaults.put(VariableNames.LOCAL_METCHEM_DATABASE_LIBRARY_NAME, Constants.DEFAULT_LOCAL_METCHEM_DATABASE_LIBRARY_NAME);
		
		
		this.defaults.put(VariableNames.PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE_NAME, Constants.DEFAULT_PEAK_FINGERPRINT_ANNOTATION_ALPHA_VALUE);
		this.defaults.put(VariableNames.PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE_NAME, Constants.DEFAULT_PEAK_FINGERPRINT_ANNOTATION_BETA_VALUE);;

	}

	public void writeSettingsFile(String filename) throws IOException {
		java.io.BufferedWriter bwriter = new java.io.BufferedWriter(new java.io.FileWriter(new java.io.File(filename)));
		java.util.Iterator<String> keys = this.map.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			String type = ParameterDataTypes.getType(key);
			Object value = this.map.get(key);
			if (value == null) {
				continue;
			}
			if (type.length() == 0) {
				continue;
			}
			if (type.equals("Double")) {
				bwriter.write(key + " = " + String.valueOf(value));
			} else if (type.equals("Integer")) {
				bwriter.write(key + " = " + String.valueOf(value));
			} else if (type.equals("String")) {
				bwriter.write(key + " = " + value);
			} else if (type.equals("Byte")) {
				bwriter.write(key + " = " + String.valueOf(value));
			} else if (type.equals("Boolean")) {
				bwriter.write(key + " = " + String.valueOf(value));
			} else if (type.equals("Level")) {
				bwriter.write(key + " = " + String.valueOf(value));
			} else if (type.equals("Double[]")) {
				String toWrite = "";
				Double[] values = (Double[]) value;
				if (values.length == 0)
					continue;
				for (Double currentValue : values)
					toWrite += String.valueOf(currentValue) + ",";
				bwriter.write(key + " = " + toWrite.substring(0, toWrite.length() - 1));
			} else if (type.equals("String[]")) {
				String toWrite = "";
				String[] values = (String[]) value;
				if (values.length == 0)
					continue;
				for (String currentValue : values) {
					toWrite += currentValue + ",";
				}
				bwriter.write(key + " = " + toWrite.substring(0, toWrite.length() - 1));
			} else
				continue;
			bwriter.newLine();
		}
		bwriter.close();
	}

	public static MetFragGlobalSettings readSettings(java.io.File parameterFile, org.apache.log4j.Logger logger) throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		java.util.Vector<String> setParameters = new java.util.Vector<String>();
		java.io.BufferedReader parameterFileReader = new java.io.BufferedReader(new java.io.FileReader(parameterFile));
		String line = "";
		int lineNumber = 1;
		while ((line = parameterFileReader.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("#") || line.length() == 0)
				continue;
			if (!line.contains("=")) {
				if (logger != null)
					logger.error("Line number " + lineNumber + " contains invalid value!");
				continue;
			}
			String[] tmp = line.split("=");
			/*
			 * check whether the current parameter has already been defined
			 */
			if (setParameters.contains(tmp[0].trim())) {
				logger.error(tmp[0].trim() + " was already defined in the parameter file.");
				parameterFileReader.close();
				throw new Exception();
			}
			/*
			 * set parameter value
			 */
			try {
				String valueString = tmp[1].trim();
				for (int i = 2; i < tmp.length; i++)
					valueString += "=" + tmp[i];
				settings.set(tmp[0].trim(), ParameterDataTypes.getParameter(valueString.trim(), tmp[0].trim()));
			}
			catch(Exception e) {
				logger.error("Error in parameter file at \"" + line + " \"");
				parameterFileReader.close();
				throw new Exception("Error in parameter file at \"" + line + " \"");
			}
			/*
			 * 
			 */
			setParameters.add(tmp[0].trim());

			lineNumber++;
		}
		parameterFileReader.close();
		return settings;
	}

	public static MetFragGlobalSettings readSettings(java.util.Hashtable<String, String> arguments, org.apache.log4j.Logger logger) throws Exception {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		java.util.Enumeration<String> keys = arguments.keys();
		while(keys.hasMoreElements()) {
			String currentKey = keys.nextElement();
			if(currentKey.equals(VariableNames.PARAMETER_FILE_NAME)) continue;
			String argument = arguments.get(currentKey);
			settings.set(currentKey, ParameterDataTypes.getParameter(argument, currentKey));
		}
	
		return settings;
	}

	public static void readSettings(java.util.Hashtable<String, String> arguments, MetFragGlobalSettings settings, org.apache.log4j.Logger logger) throws Exception {
		java.util.Enumeration<String> keys = arguments.keys();
		while(keys.hasMoreElements()) {
			String currentKey = keys.nextElement();
			if(currentKey.equals(VariableNames.PARAMETER_FILE_NAME)) continue;
			String argument = arguments.get(currentKey);
			settings.set(currentKey, ParameterDataTypes.getParameter(argument, currentKey));
		}
	}
	
	public void includeSettings(MetFragGlobalSettings settings, boolean overwrite) {
		if(settings == null) return;
		java.util.Set<String> keys = settings.getKeys();
		java.util.Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			if(!overwrite && this.map.containsKey(key)) continue;
			this.map.put(key, settings.get(key));
		}
	}
	
	public void includeSettings(MetFragGlobalSettings settings, boolean overwrite, String[] excludeKeys) {
		if(settings == null) return;
		java.util.Set<String> keys = settings.getKeys();
		java.util.Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			if(!overwrite && this.map.containsKey(key)) continue;
			boolean settingKeyExcluded = false;
			if(excludeKeys != null) {
				for(String excludedKey : excludeKeys) {
					if(excludedKey.equals(key)) {
						settingKeyExcluded = true;
						break;
					}
				}
				if(settingKeyExcluded) continue;
			}
			this.map.put(key, settings.get(key));
		}
	}
	
	public void includeSettings(MetFragGlobalSettings settings, boolean overwrite, java.util.Vector<String> excludeKeys) {
		if(settings == null) return;
		java.util.Set<String> keys = settings.getKeys();
		java.util.Iterator<String> it = keys.iterator();
		while(it.hasNext()) {
			String key = it.next();
			if(!overwrite && this.map.containsKey(key)) continue;
			boolean settingKeyExcluded = false;
			if(excludeKeys != null) {
				for(String excludedKey : excludeKeys) {
					if(excludedKey.equals(key)) {
						settingKeyExcluded = true;
						break;
					}
				}
				if(settingKeyExcluded) continue;
			}
			this.map.put(key, settings.get(key));
		}
	}

}
