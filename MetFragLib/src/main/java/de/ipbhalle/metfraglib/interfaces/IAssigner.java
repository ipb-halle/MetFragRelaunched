package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.list.MatchList;

public interface IAssigner {

	/**
	 * returns a list of matches
	 * 
	 * @return
	 */
	public MatchList assign();
	
	/**
	 * delete all objects
	 */
	public void nullify();
	
	/**
	 * 
	 */
	public void setFragmentList(IList list);
	
}
