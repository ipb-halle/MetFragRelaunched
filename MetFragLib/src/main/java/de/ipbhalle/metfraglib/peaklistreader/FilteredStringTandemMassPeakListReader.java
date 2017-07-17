package de.ipbhalle.metfraglib.peaklistreader;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class FilteredStringTandemMassPeakListReader extends AbstractPeakListReader {

	protected double minimumAbsolutePeakIntensity;
	protected byte precursorAdductTypeIndex;
	
	public FilteredStringTandemMassPeakListReader(Settings settings) {
		super(settings);
		this.minimumAbsolutePeakIntensity = (Double)settings.get(VariableNames.MINIMUM_ABSOLUTE_PEAK_INTENSITY_NAME);
		this.precursorAdductTypeIndex = (byte)Constants.ADDUCT_NOMINAL_MASSES.indexOf((Integer)settings.get(VariableNames.PRECURSOR_ION_MODE_NAME));
	}

	public DefaultPeakList read() throws Exception {
		SortedTandemMassPeakList peakList = null;
		String stringname = (String)this.settings.get(VariableNames.PEAK_LIST_STRING_NAME);
		String peakDelim1 = "\\n";
		String peakDelim2 = "\\s+";
		if(stringname.contains(";") || stringname.contains("_")) {
			peakDelim1 = ";";
			peakDelim2 = "_";
		}
		peakList = new SortedTandemMassPeakList((Double)this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
		String[] tmp = stringname.split(peakDelim1);
		for(int i = 0; i < tmp.length; i++) 
		{
			tmp[i] = tmp[i].trim();
			if(tmp[i].startsWith("#") || tmp[i].length() == 0) continue;
			String[] tmp2 = tmp[i].split(peakDelim2);
			TandemMassPeak peak = new TandemMassPeak(Double.parseDouble(tmp2[0].trim()), Double.parseDouble(tmp2[1].trim()));
			/*
			 * filtering step
			 */
			if(peak.getMass() >= ((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME) - 5.0 + Constants.ADDUCT_MASSES.get(this.precursorAdductTypeIndex))) 
				continue;
			if(peak.getAbsoluteIntensity() < this.minimumAbsolutePeakIntensity) 
				continue;
			
			peakList.addElement(peak);
		}
		for(int i = 0; i < peakList.getNumberElements(); i++)
			peakList.getElement(i).setID(i);
		peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
		return peakList;
	}

}
