package de.ipbhalle.metfraglib.interfaces;

public interface IList {
	
	/**
	 * returns an element of the list at the specified index position
	 * 
	 * @param index
	 * @return
	 */
	public Object getElement(int index);
	
	/**
	 * returns the number of elements in the list
	 * 
	 * @return
	 */
	public int getNumberElements();
	

	/**
	 * delete all objects
	 */
	public void nullify();
}
