package de.ipbhalle.metfragweb.container;

import de.ipbhalle.metfraglib.list.SortedScoredCandidateList;
import de.ipbhalle.metfragweb.datatype.MetFragResult;
import de.ipbhalle.metfragweb.datatype.Molecule;

public class MetFragResultsContainer {

	protected java.util.List<MetFragResult> molecules;
	protected int numberPeaksUsed;
	protected boolean compoundNameAvailable;
	protected boolean simScoreAvailable;
	
	public MetFragResultsContainer() {
		this.compoundNameAvailable = false;
		this.simScoreAvailable = false;
		this.molecules = new java.util.ArrayList<MetFragResult>();
	}
	
	public void addMetFragResult(MetFragResult molecule) {
		this.molecules.add(molecule);
	}

	public void addMetFragResultScoreSorted(MetFragResult molecule) {
		int index = 0;
		while(index < this.molecules.size() && molecule.getScore() < this.molecules.get(index).getScore())  
			index++;
		this.molecules.add(index, molecule);
	}
	
	public void addMetFragResult(int index, MetFragResult molecule) {
		this.molecules.add(index, molecule);
	}
	
	public java.util.List<MetFragResult> getMetFragResults() {
		return this.molecules;
	}

	public int getNumberPeaksUsed() {
		return numberPeaksUsed;
	}

	public void setNumberPeaksUsed(int numberPeaksUsed) {
		this.numberPeaksUsed = numberPeaksUsed;
	}

	public boolean isSimScoreAvailable() {
		return this.simScoreAvailable;
	}
	
	public boolean isCompoundNameAvailable() {
		return compoundNameAvailable;
	}

	public void setCompoundNameAvailable(boolean compoundNameAvailable) {
		this.compoundNameAvailable = compoundNameAvailable;
	}

	public void setSimScoreAvailable(boolean simScoreAvailable) {
		this.simScoreAvailable = simScoreAvailable;
	}
	
	public SortedScoredCandidateList getScoredCandidateList() {
		SortedScoredCandidateList candidateList = new SortedScoredCandidateList();
		candidateList.setNumberPeaksUsed(this.numberPeaksUsed);
		for(int i = 0; i < this.molecules.size(); i++) {
			Molecule root = this.molecules.get(i).getRoot();
			candidateList.addElement(root.getCandidate());
		}
		return candidateList;
	}
	
}
