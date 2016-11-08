package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.settings.Settings;

public interface IWriter {
	
	public abstract boolean write(IList list, String filename, String path) throws Exception;
	
	public abstract boolean write(IList list, String filename, String path, Settings settings) throws Exception;
	
	/**
	 * delete all objects
	 */
	public abstract void nullify();
}
