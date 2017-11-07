package de.ipbhalle.metfragrest;

import java.io.File;

import de.ipbhalle.metfraglib.exceptions.ParameterNotKnownException;
import de.ipbhalle.metfraglib.parameter.ParameterDataTypes;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.FilteredStringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class ProcessArguments {

	private String fragmentpeakmatchabsolutemassdeviation;
	private String fragmentpeakmatchrelativemassdeviation;
	private String databasesearchrelativemassdeviation;
	private String precursorcompoundids;
	private String ionizedprecursormass;
	private String neutralprecursormass;
	private String neutralprecursormolecularformula;
	private String precursorionmode;
	private String precursoriontype;
	private String peakliststring;
	private String metfragdatabasetype;
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(fragmentpeakmatchabsolutemassdeviation != null) builder.append("fragmentpeakmatchabsolutemassdeviation=" + fragmentpeakmatchabsolutemassdeviation + "\n");
		if(fragmentpeakmatchrelativemassdeviation != null) builder.append("fragmentpeakmatchrelativemassdeviation=" + fragmentpeakmatchrelativemassdeviation + "\n");
		if(databasesearchrelativemassdeviation != null) builder.append("databasesearchrelativemassdeviation=" + databasesearchrelativemassdeviation + "\n");
		if(precursorcompoundids != null) builder.append("precursorcompoundids=" + precursorcompoundids + "\n");
		if(ionizedprecursormass != null) builder.append("ionizedprecursormass=" + ionizedprecursormass + "\n");
		if(neutralprecursormass != null) builder.append("neutralprecursormass=" + neutralprecursormass + "\n");
		if(neutralprecursormolecularformula != null) builder.append("neutralprecursormolecularformula=" + neutralprecursormolecularformula + "\n");
		if(precursorionmode != null) builder.append("precursorionmode=" + precursorionmode + "\n");
		if(precursoriontype != null) builder.append("precursoriontype=" + precursoriontype + "\n");								
		if(peakliststring != null) builder.append("peakliststring=" + peakliststring + "\n");									
		if(metfragdatabasetype != null) builder.append("metfragdatabasetype=" + metfragdatabasetype + "\n");							
		return builder.toString();
	}
	
	public MetFragGlobalSettings getSettingsObject(File resFolder) throws ParameterNotKnownException {
		MetFragGlobalSettings settings = new MetFragGlobalSettings();
		if(fragmentpeakmatchabsolutemassdeviation != null) 
			settings.set(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME, ParameterDataTypes.getParameter(fragmentpeakmatchabsolutemassdeviation, VariableNames.ABSOLUTE_MASS_DEVIATION_NAME));
		if(fragmentpeakmatchrelativemassdeviation != null) 
			settings.set(VariableNames.RELATIVE_MASS_DEVIATION_NAME, ParameterDataTypes.getParameter(fragmentpeakmatchabsolutemassdeviation, VariableNames.RELATIVE_MASS_DEVIATION_NAME));
		if(databasesearchrelativemassdeviation != null) 	
			settings.set(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME, ParameterDataTypes.getParameter(databasesearchrelativemassdeviation, VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME));
		if(precursorcompoundids != null) 	
			settings.set(VariableNames.PRECURSOR_DATABASE_IDS_NAME, ParameterDataTypes.getParameter(precursorcompoundids, VariableNames.PRECURSOR_DATABASE_IDS_NAME));
		if(ionizedprecursormass != null) 
			settings.set(VariableNames.PRECURSOR_ION_MASS_NAME, ParameterDataTypes.getParameter(ionizedprecursormass, VariableNames.PRECURSOR_ION_MASS_NAME));
		if(neutralprecursormass != null)
			settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, ParameterDataTypes.getParameter(neutralprecursormass, VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
		if(neutralprecursormolecularformula != null)
			settings.set(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME, ParameterDataTypes.getParameter(neutralprecursormolecularformula, VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME));
		if(precursorionmode != null)
			settings.set(VariableNames.PRECURSOR_ION_MODE_NAME, ParameterDataTypes.getParameter(precursorionmode, VariableNames.PRECURSOR_ION_MODE_NAME));
		if(precursoriontype != null)
			settings.set(VariableNames.PRECURSOR_ION_MODE_STRING_NAME, ParameterDataTypes.getParameter(precursoriontype, VariableNames.PRECURSOR_ION_MODE_STRING_NAME));
		if(peakliststring != null)
			settings.set(VariableNames.PEAK_LIST_STRING_NAME, ParameterDataTypes.getParameter(peakliststring, VariableNames.PEAK_LIST_STRING_NAME));
		if(metfragdatabasetype != null)
			settings.set(VariableNames.METFRAG_DATABASE_TYPE_NAME, ParameterDataTypes.getParameter(metfragdatabasetype, VariableNames.METFRAG_DATABASE_TYPE_NAME));
		
		this.addAdditionalParameters(settings, resFolder);
		return settings;
	}
	
	private void addAdditionalParameters(MetFragGlobalSettings settings, File resFolder) {
		settings.set(VariableNames.METFRAG_PEAK_LIST_READER_NAME, FilteredStringTandemMassPeakListReader.class.getName());
		settings.set(VariableNames.STORE_RESULTS_PATH_NAME, resFolder.getAbsolutePath());
		settings.set(VariableNames.SAMPLE_NAME, resFolder.getName());
		settings.set(VariableNames.METFRAG_CANDIDATE_WRITER_NAME, new String[] {"CSV"});
	}
	
	public String getFragmentpeakmatchabsolutemassdeviation() {
		return fragmentpeakmatchabsolutemassdeviation;
	}
	
	public void setFragmentpeakmatchabsolutemassdeviation(String fragmentPeakMatchAbsoluteMassDeviation) {
		fragmentpeakmatchabsolutemassdeviation = fragmentPeakMatchAbsoluteMassDeviation;
	}
	
	public String getFragmentpeakmatchrelativemassdeviation() {
		return fragmentpeakmatchrelativemassdeviation;
	}
	
	public void setFragmentpeakmatchrelativemassdeviation(String fragmentPeakMatchRelativeMassDeviation) {
		fragmentpeakmatchrelativemassdeviation = fragmentPeakMatchRelativeMassDeviation;
	}

	public String getDatabasesearchrelativemassdeviation() {
		return databasesearchrelativemassdeviation;
	}

	public void setDatabasesearchrelativemassdeviation(String databasesearchrelativemassdeviation) {
		this.databasesearchrelativemassdeviation = databasesearchrelativemassdeviation;
	}

	public String getPrecursorcompoundids() {
		return precursorcompoundids;
	}

	public void setPrecursorcompoundids(String precursorcompoundids) {
		this.precursorcompoundids = precursorcompoundids;
	}

	public String getIonizedprecursormass() {
		return ionizedprecursormass;
	}

	public void setIonizedprecursormass(String ionizedprecursormass) {
		this.ionizedprecursormass = ionizedprecursormass;
	}

	public String getNeutralprecursormass() {
		return neutralprecursormass;
	}

	public void setNeutralprecursormass(String neutralprecursormass) {
		this.neutralprecursormass = neutralprecursormass;
	}

	public String getNeutralprecursormolecularformula() {
		return neutralprecursormolecularformula;
	}

	public void setNeutralprecursormolecularformula(String neutralprecursormolecularformula) {
		this.neutralprecursormolecularformula = neutralprecursormolecularformula;
	}

	public String getPrecursorionmode() {
		return precursorionmode;
	}

	public void setPrecursorionmode(String precursorionmode) {
		this.precursorionmode = precursorionmode;
	}

	public String getPrecursoriontype() {
		return precursoriontype;
	}

	public void setPrecursoriontype(String precursoriontype) {
		this.precursoriontype = precursoriontype;
	}

	public String getPeakliststring() {
		return peakliststring;
	}

	public void setPeakliststring(String peakliststring) {
		this.peakliststring = peakliststring;
	}

	public String getMetfragdatabasetype() {
		return metfragdatabasetype;
	}

	public void setMetfragdatabasetype(String metfragdatabasetype) {
		this.metfragdatabasetype = metfragdatabasetype;
	}
	
}
