package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.interfaces.ICandidate;

public class CandidateList extends DefaultList {
	
	public CandidateList() {
		this.list = new java.util.Vector<Object>();
	}

	public ICandidate getElement(int index) {
		return (ICandidate) this.list.get(index);
	}
	
	public int getNumberElements() {
		if(this.list == null) return 0;
		return this.list.size();
	}

	public void addElement(ICandidate candidate) {
		this.list.add(candidate);
	}
	
	public CandidateList clone() {
		CandidateList clone = new CandidateList();
		java.util.Vector<Object> listClone = new java.util.Vector<Object>();
		for(int i = 0; i < this.list.size(); i++) {
			listClone.add(((ICandidate)this.list.get(i)).clone());
		}
		clone.setList(listClone);
		return clone;
	}

}
