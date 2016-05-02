package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.interfaces.IFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;

public class MatchFragmentNode {

	private MatchFragmentNode next;
	private double score;
	private Double[] fragmentScores;
	private Double[] optimalValues;
	private IMatch match;

	public MatchFragmentNode(IMatch match) {
		this.match = match;
	}

	public IMatch getMatch() {
		return this.match;
	}
	
	public IFragment getFragment() {
		return this.match.getMatchedFragmentList().getElement(0);
	}
	
	public Double[] getFragmentScores() {
		return fragmentScores;
	}

	public void setFragmentScores(Double[] fragmentScores) {
		this.fragmentScores = fragmentScores;
	}

	public Double[] getOptimalValues() {
		return optimalValues;
	}

	public void setOptimalValues(Double[] optimalValues) {
		this.optimalValues = optimalValues;
	}

	public boolean hasNext() {
		return this.next != null;
	}
	
	public double getScore() {
		return this.score;
	}

	public MatchFragmentNode getNext() {
		return next;
	}

	public void setNext(MatchFragmentNode next) {
		this.next = next;
	}

	public void setScore(double score) {
		this.score = score;
	}

	
}
