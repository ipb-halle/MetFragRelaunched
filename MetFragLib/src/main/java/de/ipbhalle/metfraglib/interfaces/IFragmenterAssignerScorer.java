package de.ipbhalle.metfraglib.interfaces;

import de.ipbhalle.metfraglib.collection.ScoreCollection;
import de.ipbhalle.metfraglib.list.FragmentList;
import de.ipbhalle.metfraglib.list.MatchList;

public interface IFragmenterAssignerScorer {

	public void calculate();
	
	public FragmentList getFragments();
	
	public ScoreCollection getScoreCollection();
	
	public MatchList getMatchList();
	
	public void nullify();
	
}
