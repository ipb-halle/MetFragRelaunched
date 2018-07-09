package de.ipbhalle.metfrag.split;

import java.util.LinkedList;
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
		List<String> fragmentSmiles = new LinkedList<String>();
		for(int i = 0; i < this.isChainPFAS.length; i++) {
			if(this.isChainPFAS[i]) { 
				String smiles = this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule());
				if(!fragmentSmiles.contains(smiles)) {
					fragmentSmiles.add(smiles);
					if(stringBuilder.length() == 0) stringBuilder.append(smiles);
					else {
						stringBuilder.append("|");
						stringBuilder.append(smiles);
					}
				}
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
