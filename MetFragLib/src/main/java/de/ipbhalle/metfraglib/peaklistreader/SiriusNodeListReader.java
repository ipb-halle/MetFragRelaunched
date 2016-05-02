package de.ipbhalle.metfraglib.peaklistreader;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.list.SiriusNodePeakList;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class SiriusNodeListReader extends AbstractPeakListReader {

	public SiriusNodeListReader(Settings settings) {
		super(settings);
	}

	public DefaultPeakList read() {
		return new SiriusNodePeakList((Double)settings.get(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME), (String)this.settings.get(VariableNames.PEAK_LIST_PATH_NAME));
	}

}
