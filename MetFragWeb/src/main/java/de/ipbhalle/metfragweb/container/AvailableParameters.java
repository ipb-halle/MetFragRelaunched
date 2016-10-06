package de.ipbhalle.metfragweb.container;

import java.util.Hashtable;

import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

import de.ipbhalle.metfraglib.parameter.ClassNames;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfragweb.datatype.AvailableScore;

public class AvailableParameters {
		
	protected java.util.List<SelectItem> databases;
	protected java.util.HashMap<String, Boolean> databaseNeedsLocalFile;
	protected java.util.List<SelectItem> precursorModes;
	protected java.util.List<SelectItem> substructureSmarts;
	protected Hashtable<String, java.util.List<AvailableScore>> databaseToScores;
	protected java.util.Vector<String> preservedCompoundScoreProperties;
	protected java.util.Vector<String> preservedCompoundPartitioningCoefficientProperties;
	
	public AvailableParameters() {
		this.initialiseDatabases();
		this.initialisePrecusorModes();
		this.initialiseSubstructureSmarts();
		this.initialisePreservedCompoundProperties();
	}

	protected void initialisePrecusorModes() {
		int numberPositive = 0;
		int numberNegative = 0;
		for(int i = 0; i < Constants.ADDUCT_NAMES.size(); i++) {
			if(Constants.ADDUCT_NAMES.get(i).equals("-D") || Constants.ADDUCT_NAMES.get(i).equals("+D")) continue;
			if(Constants.ADDUCT_NOMINAL_MASSES.get(i) == 0) continue;
			if(Constants.ADDUCT_CHARGES.get(i)) numberPositive++;
			else numberNegative++;
		}
		SelectItem[] positiveAdducts = new SelectItem[numberPositive + 1];
		SelectItem[] negativeAdducts = new SelectItem[numberNegative + 1];
		
		int indexPositive = 1;
		int indexNegative = 1;
		for(int i = 0; i < Constants.ADDUCT_NAMES.size(); i++) {
			if(Constants.ADDUCT_NAMES.get(i).equals("-D") || Constants.ADDUCT_NAMES.get(i).equals("+D")) continue;
			if(Constants.ADDUCT_NOMINAL_MASSES.get(i) == 0) continue;
			String label = "[M" + Constants.ADDUCT_NAMES.get(i) + "]";
			boolean positive = true;
			if(Constants.ADDUCT_CHARGES.get(i)) label += "+";
			else {
				label += "-";
				positive = false;
			}
			if(positive) {
				positiveAdducts[indexPositive] = new SelectItem(Constants.ADDUCT_NOMINAL_MASSES.get(i), label);
				indexPositive++;
			}
			else {
				negativeAdducts[indexNegative] = new SelectItem(Constants.ADDUCT_NOMINAL_MASSES.get(i), label);
				indexNegative++;
			}
		}
		
		positiveAdducts[0] = new SelectItem(1000, "[M]+");
		negativeAdducts[0] = new SelectItem(-1000, "[M]-");
		
		SelectItemGroup g1 = new SelectItemGroup("Positive");
        g1.setSelectItems(positiveAdducts);
		SelectItemGroup g2 = new SelectItemGroup("Negative");
        g2.setSelectItems(negativeAdducts);
		
        this.precursorModes = new java.util.ArrayList<SelectItem>();
		this.precursorModes.add(g1);
		this.precursorModes.add(g2);
	}
	
	public boolean isValidPrecusorMode(int mode) {
		for(int i = 0; i < this.precursorModes.size(); i++) {
			if((Integer)this.precursorModes.get(i).getValue() == mode) return true;
		}
		return false;
	}
	
	public boolean isPreservedCompoundScoreProperty(String value) {
		return this.preservedCompoundScoreProperties.contains(value) || ClassNames.containsScore(value) || ClassNames.containsScore(value.replaceAll("_Values", ""));
	}

	public boolean isPreservedCompoundPartitioningCoefficientProperty(String value) {
		return this.preservedCompoundPartitioningCoefficientProperties.contains(value) || ClassNames.containsScore(value) || ClassNames.containsScore(value.replaceAll("_Values", ""));
	}
	
	protected void initialisePreservedCompoundProperties() {
		this.preservedCompoundScoreProperties = new java.util.Vector<String>();
		this.preservedCompoundScoreProperties.add(VariableNames.COMPOUND_NAME_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.IDENTIFIER_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.INCHI_KEY_1_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.INCHI_KEY_2_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.INCHI_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.IUPAC_NAME_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.MOLECULAR_FORMULA_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.MONOISOTOPIC_MASS_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.PUBCHEM_XLOGP_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.SMILES_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.CHEMSPIDER_ALOGP_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.CHEMSPIDER_XLOGP_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.FINAL_SCORE_COLUMN_NAME);
		this.preservedCompoundScoreProperties.add(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN);
		this.preservedCompoundScoreProperties.add(VariableNames.EXPLAINED_PEAKS_COLUMN);
		this.preservedCompoundScoreProperties.add(VariableNames.NUMBER_PEAKS_USED_COLUMN);
		this.preservedCompoundScoreProperties.add(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN);
		
		this.preservedCompoundPartitioningCoefficientProperties = new java.util.Vector<String>();
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.COMPOUND_NAME_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.IDENTIFIER_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.INCHI_KEY_1_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.INCHI_KEY_2_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.IUPAC_NAME_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.MOLECULAR_FORMULA_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.MONOISOTOPIC_MASS_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.SMILES_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.MAXIMUM_TREE_DEPTH_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_ALOGP_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_XLOGP_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.FINAL_SCORE_COLUMN_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.NUMBER_EXPLAINED_PEAKS_COLUMN);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.EXPLAINED_PEAKS_COLUMN);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.NUMBER_PEAKS_USED_COLUMN);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.FORMULAS_OF_PEAKS_EXPLAINED_COLUMN);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.PUBCHEM_NUMBER_PATENTS_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.PUBCHEM_NUMBER_PUBMED_REFERENCES_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_DATA_SOURCE_COUNT);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_NUMBER_EXTERNAL_REFERENCES_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_NUMBER_PUBMED_REFERENCES_NAME);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_REFERENCE_COUNT);
		this.preservedCompoundPartitioningCoefficientProperties.add(VariableNames.CHEMSPIDER_RSC_COUNT);
	}
	
	protected void initialiseSubstructureSmarts() {
		this.substructureSmarts = new java.util.ArrayList<SelectItem>();
		this.substructureSmarts.add(new SelectItem("[cR1]1[cR1][cR1][cR1][cR1][cR1]1", "Unfused benzene ring"));
		this.substructureSmarts.add(new SelectItem("[CX3](=O)[OX2H1]", "Carboxyl"));
		this.substructureSmarts.add(new SelectItem("[NX3][CX2]#[NX1]", "Cyanamide"));
		this.substructureSmarts.add(new SelectItem("c12ccccc1cccc2", "Fused benzene rings"));
		this.substructureSmarts.add(new SelectItem("[#6][CX3](=O)[#6]", "Ketone"));
		this.substructureSmarts.add(new SelectItem("[OX2H]", "Hydroxyl"));
		this.substructureSmarts.add(new SelectItem("[#16X2H]", "Thiol"));
	}
	
	protected void initialiseDatabases() {
		SelectItem[] serverdbs = new SelectItem[] {
			new SelectItem("PubChem", "PubChem"), 
			new SelectItem("KEGG", "KEGG"), 
			new SelectItem("ChemSpider", "ChemSpider"),
			new SelectItem("MetaCyc", "MetaCyc"),
			new SelectItem("FOR-IDENT", "FOR-IDENT"),
			new SelectItem("LipidMaps", "LipidMaps"),
			new SelectItem("LocalDerivatisedKegg", "KEGG (derivatised)")
		};
		SelectItem[] filedbs = new SelectItem[] {
			new SelectItem("LocalCSV", "CSV"), 
			new SelectItem("LocalPSV", "PSV"), 
			new SelectItem("LocalSDF", "SDF")
		};
		SelectItemGroup g1 = new SelectItemGroup("Server Databases");
        g1.setSelectItems(serverdbs);
		SelectItemGroup g2 = new SelectItemGroup("File Databases");
        g2.setSelectItems(filedbs);
	
		this.databases = new java.util.ArrayList<SelectItem>();
		this.databases.add(g1);
		this.databases.add(g2);
		
		this.databaseNeedsLocalFile = new java.util.HashMap<String, Boolean>();
		this.databaseNeedsLocalFile.put("PubChem", false);
		this.databaseNeedsLocalFile.put("KEGG", false);
		this.databaseNeedsLocalFile.put("ChemSpider", false);
		this.databaseNeedsLocalFile.put("MetaCyc", false);
		this.databaseNeedsLocalFile.put("FOR-IDENT", false);
		this.databaseNeedsLocalFile.put("LipidMaps", false);
		this.databaseNeedsLocalFile.put("LocalDerivatisedKegg", false);

		this.databaseNeedsLocalFile.put("LocalCSV", true);
		this.databaseNeedsLocalFile.put("LocalPSV", true);
		this.databaseNeedsLocalFile.put("LocalSDF", true);
	}
	
	public boolean isNeedLocalFile(String databasename) {
		return this.databaseNeedsLocalFile.get(databasename);
	}
	
	public java.util.List<SelectItem> getPrecursorModes() {
		return this.precursorModes;
	}
	
	public java.util.List<SelectItem> getDatabases() {
		return this.databases;
	}
		
	public java.util.List<SelectItem> getSubstructureSmarts() {
		return this.substructureSmarts;
	}
}
