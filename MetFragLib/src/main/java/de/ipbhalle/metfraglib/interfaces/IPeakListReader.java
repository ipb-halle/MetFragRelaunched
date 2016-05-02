package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.list.DefaultPeakList;

public interface IPeakListReader {

	public DefaultPeakList read() throws Exception;
	
}
