package de.ipbhalle.metfrag.split;

import java.util.List;

import de.ipbhalle.metfraglib.candidate.TopDownPrecursorCandidate;
import de.ipbhalle.metfraglib.fragment.AbstractTopDownBitArrayFragment;

public class FragmentPFAS {

	private List<AbstractTopDownBitArrayFragment> createdFragments;
	private boolean[] isChainPFAS;
	protected TopDownPrecursorCandidate pfasStructure;
	protected int numberBrokenBonds;
	
	public FragmentPFAS(List<AbstractTopDownBitArrayFragment> createdFragments, boolean[] isChainPFAS, int numberBrokenBonds, TopDownPrecursorCandidate pfasStructure) {
		this.createdFragments = createdFragments;
		this.isChainPFAS = isChainPFAS;
		this.pfasStructure = pfasStructure;
		this.numberBrokenBonds = numberBrokenBonds;
	}
	
	public String getFunctionalGroupsSmiles() {
		for(int i = 0; i < this.isChainPFAS.length; i++) 
			if(!this.isChainPFAS[i]) return this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule());
		return "";
	}
	
	public String getPfasSmiles() {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < this.isChainPFAS.length; i++) 
			if(this.isChainPFAS[i]) { 
				if(stringBuilder.length() == 0) stringBuilder.append(this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule()));
				else {
					stringBuilder.append("|");
					stringBuilder.append(this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule()));
				}
			}
		return stringBuilder.toString();
	}
	
	public int getNumberBrokenBonds() {
		return this.numberBrokenBonds;
	}
	
	public String toString() {
		return this.getFunctionalGroupsSmiles() + " " + this.getPfasSmiles() + " " + this.getNumberBrokenBonds();
	}
}
