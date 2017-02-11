package de.ipbhalle.metfraglib.interfaces;

import java.io.File;

import de.ipbhalle.metfraglib.settings.Settings;

public interface IWriter {
	
	public abstract boolean write(IList list, String filename, String path) throws Exception;
	
	public abstract boolean write(IList list, String filename, String path, Settings settings) throws Exception;
	
	public abstract boolean write(IList list, String filename) throws Exception;
	
	public abstract boolean writeFile(File file, IList list, Settings settings) throws Exception;

	public abstract boolean writeFile(File file, IList list) throws Exception;
	/**
	 * delete all objects
	 */
	public abstract void nullify();
}
