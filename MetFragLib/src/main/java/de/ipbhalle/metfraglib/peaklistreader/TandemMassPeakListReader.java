package de.ipbhalle.metfraglib.peaklistreader;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SortedTandemMassPeakList;
import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.settings.Settings;

public class TandemMassPeakListReader extends AbstractPeakListReader {

	public TandemMassPeakListReader(Settings settings) {
		super(settings);
	}

	public DefaultPeakList read() {
		int numberMaximumPeaksUsed = (Integer)settings.get(VariableNames.NUMBER_MAXIMUM_PEAKS_USED_NAME);
		SortedTandemMassPeakList peakList = null;
		String filename = (String)this.settings.get(VariableNames.PEAK_LIST_PATH_NAME);
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			peakList = new SortedTandemMassPeakList((Double)this.settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME));
			String line = "";
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#") || line.length() == 0) continue;
				String[] tmp = line.split("\\s+");
				peakList.addElement(new TandemMassPeak(Double.parseDouble(tmp[0].trim()), Double.parseDouble(tmp[1].trim())));
			}
			breader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for(int i = 0; i < peakList.getNumberElements(); i++)
			peakList.getElement(i).setID(i);
		this.deleteByMaximumNumberPeaksUsed(numberMaximumPeaksUsed, peakList);
		peakList.calculateRelativeIntensities(Constants.DEFAULT_MAXIMUM_RELATIVE_INTENSITY);
		return peakList;
	}

}
