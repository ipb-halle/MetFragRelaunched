package de.ipbhalle.metfragweb.helper;

import java.io.IOException;

import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;
import de.ipbhalle.metfragweb.container.AvailableParameters;
import de.ipbhalle.metfragweb.container.BeanSettingsContainer;
import de.ipbhalle.metfragweb.container.Messages;

public class SettingsInitialiser {

	public static boolean initilizeSettings(BeanSettingsContainer beanSettings, 
			MetFragGlobalSettings settings, Messages infoMessages, Messages errorMessages, 
			AvailableParameters avalableParameters, java.util.HashMap<String, java.io.File> zipEntries) {
		java.util.Iterator<String> it = settings.getKeys().iterator();
		String infos = "";
		String errors = "";
		boolean checksFine = true;
		while(it.hasNext()) {
			String parameterName = it.next();
			//database
			if(parameterName.equals("NeutralPrecursorMass")) {
				beanSettings.setNeutralMonoisotopicMass(String.valueOf((Double)settings.get(parameterName)));
			}
			else if(parameterName.equals("MetFragDatabaseType")) {
				String database = (String)settings.get(parameterName);
				if(database.equals("ExtendedPubChem")) {
					database = "PubChem";
					beanSettings.setIncludeReferences(true);
				}
				beanSettings.setDatabase(database);
			}
			else if(parameterName.equals("DatabaseSearchRelativeMassDeviation")) {
				beanSettings.setDatabaseRelativeMassDeviation(String.valueOf((Double)settings.get(parameterName)));
			}
			else if(parameterName.equals("LocalDatabasePath")) {
				String name = (String)settings.get(parameterName);
				java.io.File zipEntry = zipEntries.get(parameterName);
				try {
					beanSettings.getUserInputDataHandler().handleLocalCandidateFile(zipEntry, infoMessages, errorMessages, name, beanSettings);
				}
				catch(Exception e) {
					checksFine = false;
					errors += "LocalDatabasePath: Reading file " + name + " failed <br />";
				}
			}
			else if(parameterName.equals("PrecursorCompoundIDs")) {
				String[] tmp = (String[])settings.get(parameterName);
				String value = "";
				if(tmp.length >= 1) value += tmp[0];
				for(int i = 1; i < tmp.length; i++)
					value += "," + tmp[i];
				beanSettings.setIdentifiers(value);
			}
			else if(parameterName.equals("NeutralPrecursorMolecularFormula")) {
				beanSettings.setFormula((String)settings.get(parameterName));
			}
			else if(parameterName.equals("IonizedPrecursorMass")) {
				beanSettings.setMeasuredMass(String.valueOf((Double)settings.get(parameterName)));
			}
			//filters
			else if(parameterName.equals("MetFragPreProcessingCandidateFilter")) {
				String[] tmp = (String[])settings.get(parameterName);
				for(int i = 0; i < tmp.length; i++) {
					if(tmp[i].equals(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME)) continue;
					else if(tmp[i].equals("ElementInclusionExclusiveFilter")) beanSettings.setFilterEnabled(true, "includedFilterElements");
					else if(tmp[i].equals("ElementInclusionFilter")) beanSettings.setFilterEnabled(true, "includedFilterElements");
					else if(tmp[i].equals("ElementExclusionFilter")) beanSettings.setFilterEnabled(true, "excludedFilterElements");
					else if(tmp[i].equals("MaximumElementsFilter")) beanSettings.setFilterEnabled(true, "includedFilterMaximumElements");
					else if(tmp[i].equals("MinimumElementsFilter")) beanSettings.setFilterEnabled(true, "includedFilterMinimumElements");
					else if(tmp[i].equals("SmartsSubstructureInclusionFilter")) beanSettings.setFilterEnabled(true, "includedFilterSmarts");
					else if(tmp[i].equals("SmartsSubstructureExclusionFilter")) beanSettings.setFilterEnabled(true, "excludedFilterSmarts");
					else if(tmp[i].equals("SuspectListFilter")) beanSettings.setFilterEnabled(true, "suspectListsFilter");
					else if(tmp[i].equals("UnconnectedCompoundFilter")) beanSettings.setFilterEnabled(true, "unconnectedStructureExclusionFilter");
					else if(tmp[i].equals("IsotopeFilter")) beanSettings.setFilterEnabled(true, "isotopeFilter");
					else {
						checksFine = false;
						errors += "MetFragPreProcessingCandidateFilter: " + tmp[i] + " is not known <br />";
					}
				}
			}
			else if(parameterName.equals("FilterExcludedElements")) {
				beanSettings.setExcludedFilterElements((String)settings.get(parameterName));
			}
			else if(parameterName.equals("FilterIncludedElements")) {
				beanSettings.setIncludedFilterElements((String)settings.get(parameterName));
			}
			else if(parameterName.equals("FilterSmartsInclusionList")) {
				beanSettings.setAdditionalSmartsFilterInclusion((String)settings.get(parameterName));
			}
			else if(parameterName.equals("FilterSmartsExclusionList")) {
				beanSettings.setAdditionalSmartsFilterExclusion((String)settings.get(parameterName));
			}
			else if(parameterName.equals("FilterSuspectLists")) {
				String[] tmp = (String[])settings.get(parameterName);
				for(int i = 0; i < tmp.length; i++) {
					java.io.File zipEntry = zipEntries.get(tmp[i]);
					if(zipEntry == null) {
						checksFine = false;
						errors += "FilterSuspectLists: File " + tmp[i] + " not found in ZIP archive <br />";
					}
					else {
						if(!beanSettings.getUserInputDataHandler().handleSuspectListFilterFile(zipEntry, infoMessages, errorMessages, beanSettings)) {
							checksFine = false;
							errors += "FilterSuspectLists: Reading file " + tmp[i] + " failed <br />";
						}
					}
				}
			}
			else if(parameterName.equals("FilterMaximumElements")) {
				beanSettings.setIncludedMaximumElements((String)settings.get(parameterName));
			}
			else if(parameterName.equals("FilterMinimumElements")) {
				beanSettings.setIncludedMinimumElements((String)settings.get(parameterName));
			}
			//scores		
			else if(parameterName.equals("MetFragScoreTypes")) {
				String[] tmp = (String[])settings.get(parameterName);
				for(int i = 0; i < tmp.length; i++) {
					if(tmp[i].equals(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME)) continue;
					else if(tmp[i].equals(VariableNames.METFRAG_FRAGMENTER_SCORE_NAME)) beanSettings.setScoreEnabled(true, "fragmenterScore");
					else if(tmp[i].equals("SmartsSubstructureInclusionScore")) beanSettings.setScoreEnabled(true, "includedScoreSmarts");
					else if(tmp[i].equals("SmartsSubstructureExclusionScore")) beanSettings.setScoreEnabled(true, "excludedScoreSmarts");
					else if(tmp[i].equals("MetFusionMoNAScore")) beanSettings.setScoreEnabled(true, "spectralSimilarity");
					else if(tmp[i].equals("SuspectListScore")) beanSettings.setScoreEnabled(true, "suspectListsScore");
					else if(tmp[i].equals("RetentionTimeScore")) beanSettings.setScoreEnabled(true, "retentionTimeTrainingFile");
					else infos += "MetFragScoreTypes: " + tmp[i] + " needs to be set manually  in MetFragWeb<br />";
				}
			}
			else if(parameterName.equals("ExperimentalRetentionTimeValue")) {
				infos += "ExperimentalRetentionTimeValue: Needs to be set manually  in MetFragWeb<br />";
			}
			else if(parameterName.equals("ScoreSmartsInclusionList")) {
				beanSettings.setAdditionalSmartsScoreInclusion((String)settings.get(parameterName));
			}
			else if(parameterName.equals("ScoreSmartsExclusionList")) {
				beanSettings.setAdditionalSmartsScoreExclusion((String)settings.get(parameterName));
			}
			else if(parameterName.equals("ScoreSuspectLists")) {
				String[] tmp = (String[])settings.get(parameterName);
				for(int i = 0; i < tmp.length; i++) {
					java.io.File zipEntry = zipEntries.get(tmp[i]);
					if(zipEntry == null) {
						checksFine = false;
						errors += "ScoreSuspectLists: File " + tmp[i] + " not found in ZIP archive <br />";
					}
					else {
						if(!beanSettings.getUserInputDataHandler().handleSuspectListScoreFile(zipEntry, infoMessages, errorMessages, beanSettings)) {
							checksFine = false;
							errors += "ScoreSuspectLists: Reading file " + tmp[i] + " failed <br />";	
						}
					}
				}
			}
			else if(parameterName.equals("RetentionTimeTrainingFile")) {
				infos += "RetentionTimeTrainingFile: Needs to be uploaded manually in MetFragWeb<br />";
			}
			else if(parameterName.equals("ExternalPartitioningCoefficientColumnName")) {
				infos += "ExternalPartitioningCoefficientColumnName: Needs to be set manually in MetFragWeb <br />";
			}
			//fragmenter
			else if(parameterName.equals("IsPositiveIonMode")) {
				beanSettings.setPositiveCharge((Boolean)settings.get(parameterName));
			}
			else if(parameterName.equals("PrecursorIonMode")) {
				int value = (Integer)settings.get(parameterName);
				if(avalableParameters.isValidPrecusorMode(value)) {
					if(beanSettings.isPositiveCharge() && value == 0) beanSettings.setMode(1000);
					else if(!beanSettings.isPositiveCharge() && value == 0) beanSettings.setMode(-1000);
					else beanSettings.setMode(value);
				}
				else {
					checksFine = false;
					errors += "PrecursorIonMode: Wrong value <br />";
				}
			}
			else if(parameterName.equals("FragmentPeakMatchRelativeMassDeviation")) {
				beanSettings.setRelativeMassDeviation(String.valueOf((Double)settings.get(parameterName)));
			}
			else if(parameterName.equals("FragmentPeakMatchAbsoluteMassDeviation")) {
				beanSettings.setAbsoluteMassDeviation(String.valueOf((Double)settings.get(parameterName)));
			}
			else if(parameterName.equals("PeakListPath")) {
				String path = (String)settings.get(parameterName);
				java.io.File zipEntry = zipEntries.get(path);
				if(zipEntry == null) {
					checksFine = false;
					errors += "PeakListPath: File " + path + " not found in ZIP archive <br />";
				}
				else {
					try {
						java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(zipEntry));
						String line = "";
						String peakList = "";
						while((line = breader.readLine()) != null) {
							line = line.trim();
							if(line.length() == 0) continue;
							if(line.startsWith("#")) continue;
							peakList += line + "\n";  
						}
						beanSettings.setPeakList(peakList);
						breader.close();
					} catch (IOException e) {
						checksFine = false;
						errors += "PeakListPath: File " + path + " contains invalid values <br />";
					}
				}
			}
			else infos += parameterName + " not used <br />";
		}
		if(infos.length() == 0 && checksFine) infos = "Fine!";
		if(infos.length() != 0 && checksFine) infoMessages.setMessage("uploadParametersInfo", infos);
		if(errors.length() != 0 && !checksFine) errorMessages.setMessage("uploadParametersError", errors);
		
		System.out.println(infos + " " + checksFine);
		return checksFine;
	}
	
}
