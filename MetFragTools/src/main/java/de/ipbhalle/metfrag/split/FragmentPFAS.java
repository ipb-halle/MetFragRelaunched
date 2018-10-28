package de.ipbhalle.metfrag.split;

import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
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
	
	public String getFunctionalGroupsSmiles() throws CDKException {
		StringBuilder stringBuilder = new StringBuilder();
		for(int i = 0; i < this.isChainPFAS.length; i++) {
			if(!this.isChainPFAS[i]) {
				if(stringBuilder.length() == 0) stringBuilder.append(this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule()));
				else {
					stringBuilder.append("|");
					stringBuilder.append(this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule()));
				}
			}
		}
		return stringBuilder.toString();
	}
	
	public String getPfasSmiles() throws CDKException {
		StringBuilder stringBuilder = new StringBuilder();
		List<String> fragmentSmiles = new LinkedList<String>();
		List<String> fragmentInChIKeys = new LinkedList<String>();
		for(int i = 0; i < this.isChainPFAS.length; i++) {
			if(this.isChainPFAS[i]) { 
				String smiles = this.createdFragments.get(i).getPreparedSmiles(this.pfasStructure.getPrecursorMolecule());
				String[] inchiInfo = MoleculeFunctions.getInChIInfoFromAtomContainer(this.createdFragments.get(i).getStructureAsIAtomContainer(this.pfasStructure.getPrecursorMolecule()));
				String inchikey1 = inchiInfo[1];
				if(!fragmentInChIKeys.contains(inchikey1)) {
					fragmentSmiles.add(smiles);
					fragmentInChIKeys.add(inchikey1);
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
	
	protected String reReadSmiles(String smiles) throws InvalidSmilesException {
		IAtomContainer con = MoleculeFunctions.parseSmiles(smiles);
		MoleculeFunctions.prepareAtomContainer(con, false);
		return MoleculeFunctions.generateSmiles(con);
	}
	
	public int getNumberBrokenBonds() {
		return this.numberBrokenBonds;
	}
	
	public String toString() {
		try {
			return this.getFunctionalGroupsSmiles() + " " + this.getPfasSmiles() + " " + this.getNumberBrokenBonds();
		} catch (CDKException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
}
