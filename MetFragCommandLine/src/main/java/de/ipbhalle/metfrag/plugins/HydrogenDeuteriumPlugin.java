package de.ipbhalle.metfrag.plugins;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.fragmenterassignerscorer.HDTopDownFragmenterAssignerScorer;
import de.ipbhalle.metfraglib.molecularformula.HDByteMolecularFormula;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.HDFilteredTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;
import org.apache.log4j.Logger;

public class HydrogenDeuteriumPlugin {

	protected Logger logger = Logger.getLogger(HydrogenDeuteriumPlugin.class);
	protected boolean hd_defined = false;
	
	public boolean load(Settings settings) {
		if(!checkSettingsHD(settings)) return false;
		
		String[] score_types = (String[])settings.get(VariableNames.METFRAG_SCORE_TYPES_NAME);
		if(this.hd_defined) {
			HDFilteredTandemMassPeakListReader peakListReaderHD = new HDFilteredTandemMassPeakListReader(settings);
			//set HD peak list reader
			settings.set(VariableNames.HD_PEAK_LIST_NAME, peakListReaderHD.read());
			//set assigner
			settings.set(VariableNames.METFRAG_ASSIGNER_SCORER_NAME, HDTopDownFragmenterAssignerScorer.class.getName());
			//add PostProcessingCandidateHDGroupFlagFilter if enabled 
			if(!settings.containsKey(VariableNames.HD_GROUP_FLAG_FILTER_ENABLED_NAME) || 
					settings.get(VariableNames.HD_GROUP_FLAG_FILTER_ENABLED_NAME) == null ||
					(Boolean)settings.get(VariableNames.HD_GROUP_FLAG_FILTER_ENABLED_NAME)) {	
				if(settings.containsKey(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME) && settings.get(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME) != null) {
					String[] filters = (String[])settings.get(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME);
					String[] filters_new = new String[filters.length + 1];
					filters_new[0] = "HDGroupFlagFilter";
					for(int i = 0; i < filters.length; i++) {
						filters_new[i + 1] = filters[i];
					}
					settings.set(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME, filters_new);
				}
				else {
					String[] filters = {"HDGroupFlagFilter"};
					settings.set(VariableNames.METFRAG_POST_PROCESSING_CANDIDATE_FILTER_NAME, filters);
				}
			}
		}
		else {
			for(int i = 0; i < score_types.length; i++) {
				if(score_types[i].equals("HDFragmenterScore") || score_types[i].equals("HDFragmentPairScore") || score_types[i].equals("HDExchangedHydrogendsScore")) {
					this.logger.error("Error: " + score_types[i] + " is defined but no HD settings (" + VariableNames.HD_PEAK_LIST_PATH_NAME + ", " 
							+ VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME + ", " + VariableNames.HD_PRECURSOR_ION_MASS_NAME + ").");
					
					return false;
				}
			}
		}
		
		return true;
	}

	public boolean checkSettingsHD(Settings settings) {
		//HD information
		Object HDNeutralPrecursorMass = settings.get(VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME);
		Object HDNeutralPrecursorMolecularFormula = settings.get(VariableNames.HD_PRECURSOR_MOLECULAR_FORMULA_NAME);
		Object HDPrecursorIonMode = settings.get(VariableNames.HD_PRECURSOR_ION_MODE_NAME);
		Object HDPeakListPath = settings.get(VariableNames.HD_PEAK_LIST_PATH_NAME);
		Object HDIonizedPrecursorMass = settings.get(VariableNames.HD_PRECURSOR_ION_MASS_NAME);
		Object IsPositiveIonMode = settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
		
		boolean HDNeutralPrecursorMass_defined = false;
		boolean HDNeutralPrecursorMolecularFormula_defined = false;
		boolean HDPrecursorIonMode_defined = false;
		boolean HDPeakListPath_defined = false;
		boolean HDIonizedPrecursorMass_defined = false;
		
		if(HDNeutralPrecursorMass != null) HDNeutralPrecursorMass_defined = true;
		if(HDNeutralPrecursorMolecularFormula != null) HDNeutralPrecursorMolecularFormula_defined = true;
		if(HDPrecursorIonMode != null) HDPrecursorIonMode_defined = true;
		if(HDPeakListPath != null) HDPeakListPath_defined = true;
		if(HDIonizedPrecursorMass != null) HDIonizedPrecursorMass_defined = true;
		
		if(HDIonizedPrecursorMass_defined || HDNeutralPrecursorMass_defined || HDNeutralPrecursorMolecularFormula_defined || HDPrecursorIonMode_defined || HDPeakListPath_defined) {
			boolean checkPositive = true;
			//check peaklist file
			if(settings.get(VariableNames.HD_PEAK_LIST_PATH_NAME) != null) {
				//in case peaklist is defined, settings will be included
				this.hd_defined = true;
				String peakListName = (String)settings.get(VariableNames.HD_PEAK_LIST_PATH_NAME);
				checkPositive = checkFile(VariableNames.HD_PEAK_LIST_PATH_NAME, peakListName);
			}
			else {
				this.logger.error(VariableNames.HD_PEAK_LIST_PATH_NAME + " is not defined!");
				checkPositive = false;
			}

			if(HDNeutralPrecursorMolecularFormula_defined) {
				try {
					HDByteMolecularFormula formula = new HDByteMolecularFormula((String)HDNeutralPrecursorMolecularFormula);
					settings.set(VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME, formula.getMonoisotopicMass());
				} catch (AtomTypeNotKnownFromInputListException e) {
					this.logger.error("Error: " + VariableNames.HD_PRECURSOR_MOLECULAR_FORMULA_NAME + " (" + HDNeutralPrecursorMolecularFormula + ") contains unknown elements");
					checkPositive = false;
				} catch(Exception e) {
					this.logger.error("Error: Check " + VariableNames.HD_PRECURSOR_MOLECULAR_FORMULA_NAME + "!");
					checkPositive = false;
				}
				HDNeutralPrecursorMass_defined = true;
				HDIonizedPrecursorMass_defined = false;
			}
			
			//check for HD ion mode
			if(!HDPrecursorIonMode_defined) {
				this.logger.error(VariableNames.HD_PRECURSOR_ION_MODE_NAME + " is not defined!");
				checkPositive = false;
			}
			else if(IsPositiveIonMode == null) {
				checkPositive = false;
			}
			else {
				int HDPrecursorIonModeValue = 0;
				try {
					HDPrecursorIonModeValue = (Integer)HDPrecursorIonMode;
				}
				catch(Exception e) {
					this.logger.error("No valid value for " + VariableNames.HD_PRECURSOR_ION_MODE_NAME + ": " + HDPrecursorIonModeValue + "!");
					checkPositive = false;
				}
				Boolean IsPositiveIonModeValue = (Boolean)IsPositiveIonMode;
				if(!Constants.ADDUCT_NOMINAL_MASSES.contains(HDPrecursorIonModeValue)) {
					this.logger.error(VariableNames.HD_PRECURSOR_ION_MODE_NAME + " " + HDPrecursorIonModeValue + " is not known!");
					checkPositive = false;
				}
				else if(HDPrecursorIonModeValue != 0) {
					int precursorTypeIndex = Constants.ADDUCT_NOMINAL_MASSES.indexOf(HDPrecursorIonModeValue);
					if((Constants.ADDUCT_CHARGES.get(precursorTypeIndex) && !IsPositiveIonModeValue) || (!Constants.ADDUCT_CHARGES.get(precursorTypeIndex) && IsPositiveIonModeValue)) {
						this.logger.error("Values mismatch: " + VariableNames.HD_PRECURSOR_ION_MODE_NAME + " = " + Constants.ADDUCT_NAMES.get(precursorTypeIndex) + 
								" " + VariableNames.IS_POSITIVE_ION_MODE_NAME + " = " + IsPositiveIonMode);
						checkPositive = false;
					}
				}
	 		}
			
			if(!HDNeutralPrecursorMass_defined) {
				try {
					Double ionMass = (Double)settings.get(VariableNames.HD_PRECURSOR_ION_MASS_NAME);
					Integer ionMode = (Integer)settings.get(VariableNames.HD_PRECURSOR_ION_MODE_NAME);
					Boolean isPositive = (Boolean)settings.get(VariableNames.IS_POSITIVE_ION_MODE_NAME);
					double value = ionMass - Constants.ADDUCT_MASSES.get(Constants.ADDUCT_NOMINAL_MASSES.indexOf(ionMode)) 
							- Constants.POSITIVE_IONISATION_MASS_DIFFERENCE.get(Constants.POSITIVE_IONISATION.indexOf(isPositive));
					settings.set(VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME, value);
				}
				catch(Exception e) {
					this.logger.error("Error: HD-Precursor mass information not sufficient. Define " + VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME 
							+ " or " + VariableNames.HD_PRECURSOR_ION_MASS_NAME + "!");
					checkPositive = false;
				}
			}
			 
			//set number exchanged hydrogens
			if(checkPositive) {
				double _HDNeutralPrecursorMass = (Double)settings.get(VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME);
				double NeutralPrecursorMass = (Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
				byte HDNumberExchangedHydrogens = (byte)Math.round(_HDNeutralPrecursorMass - NeutralPrecursorMass);
				if(HDNumberExchangedHydrogens < 0) {
					this.logger.error("Error: Number of predicted exchanged hydrogens is negative (" + HDNumberExchangedHydrogens + ")! " +
							"This is mostly caused by wrong " + VariableNames.PRECURSOR_NEUTRAL_MASS_NAME + ", " 
							+ VariableNames.HD_PRECURSOR_NEUTRAL_MASS_NAME + " or related parameter settings.");
					checkPositive = false;
				}
				else settings.set(VariableNames.HD_NUMBER_EXCHANGED_HYDROGENS, HDNumberExchangedHydrogens);
			}
			
			return checkPositive;
		}
		else return true;
	}
	
	/**
	 * 
	 */
	private boolean checkFile(String parameterName, String fileName) {
		java.io.File file = null;
		try {
			file = new java.io.File(fileName);
		}
		catch(Exception e) {
			this.logger.error("Problems reading " + parameterName + " " + fileName + "!"); 
			return false;
		}
		if(!file.isFile()) {
			this.logger.error(parameterName + " " + fileName + " is no regular file!"); 
			return false;
		}
		if(!file.exists()) {
			this.logger.error(parameterName + " " + fileName + " not found!"); 
			return false;
		}
		if(!file.canRead()) {
			this.logger.error(parameterName + " " + fileName + " has no read permissions!"); 
			return false;
		}
		return true;
	}
}
