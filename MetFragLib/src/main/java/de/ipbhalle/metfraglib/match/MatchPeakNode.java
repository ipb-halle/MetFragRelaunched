package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.interfaces.IPeak;

public class MatchPeakNode {

	private final IPeak peak;
	private double score;
	private MatchPeakNode next;
	private int id;

	public MatchPeakNode(IPeak peak, int id) {
		this.peak = peak;
		this.id = id;
	}

	public boolean hasNext() {
		return this.next != null;
	}
	
	public double getScore() {
		return score;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public MatchPeakNode getNext() {
		return next;
	}

	public void setNext(MatchPeakNode next) {
		this.next = next;
	}

	public IPeak getPeak() {
		return peak;
	}
	
	public MatchPeakNode clone() {
		MatchPeakNode newNode = new MatchPeakNode(this.peak, this.id);
		newNode.setScore(score);
		newNode.setNext(null);
		return newNode;
	}
}
