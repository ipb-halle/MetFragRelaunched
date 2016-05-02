package de.ipbhalle.metfraglib.peaklistreader;

import de.ipbhalle.metfraglib.interfaces.IPeakListReader;
import de.ipbhalle.metfraglib.settings.Settings;

public abstract class AbstractPeakListReader implements IPeakListReader {

	protected Settings settings;
	
	public AbstractPeakListReader(Settings settings) {
		this.settings = settings;
	}

}
