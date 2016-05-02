package de.ipbhalle.metfraglib.interfaces;

public interface IWriter {

	public abstract boolean write(IList list, String filename, String path) throws Exception;
	
	/**
	 * delete all objects
	 */
	public abstract void nullify();
}
