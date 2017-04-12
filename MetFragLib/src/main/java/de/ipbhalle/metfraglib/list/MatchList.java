package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.interfaces.IMatch;

/**
 * match list contains elements of type IMatch
 * is used for the IScorer to calculate a score
 * 
 * @author cruttkie
 *
 */
public class MatchList extends DefaultList {

	public MatchList() {
		this.list = new java.util.Vector<Object>();
	}
	
	public IMatch getElement(int index) {
		return (IMatch)this.list.get(index);
	}

	public int getNumberElements() {
		return this.list.size();
	}

	public void addElement(IMatch match) {
		this.list.add(match);
	}

	public void addElementSorted(IMatch match) {
		int index = 0;
		while(index < this.list.size() && match.getMatchedPeak().getMass() > ((IMatch)this.list.get(index)).getMatchedPeak().getMass()) {
			index++;
		}
		this.list.add(index, match);
	}
	
	public boolean containsMass(double mass) {
		for(int i = 0; i < this.list.size(); i++)
			if(((IMatch)this.list.get(i)).getMatchedPeak().getMass() == mass) return true;
		return false;
	}

	public IMatch getMatchByMass(Double peakMass) {
		for(int i = 0; i < this.list.size(); i++) {
			IMatch match = (IMatch)this.list.get(i);
			if(match.getMatchedPeak().getMass().equals(peakMass)) return match;
		}
		return null;
	}
	
	public boolean containsPeakID(int peakID) {
		for(int i = 0; i < this.list.size(); i++)
			if(((IMatch)this.list.get(i)).getMatchedPeak().getID() == peakID) return true;
		return false;
	}

	public int getIndexOfPeakID(int peakID) {
		for(int i = 0; i < this.list.size(); i++)
			if(((IMatch)this.list.get(i)).getMatchedPeak().getID() == peakID) return i;
		return -1;
	}
	
	public void nullify() {
		for(int i = 0; i < this.list.size(); i++) 
			this.getElement(i).nullify();
	}

	public void shallowNullify() {
		for(int i = 0; i < this.list.size(); i++) 
			this.getElement(i).shallowNullify();
	}
}
