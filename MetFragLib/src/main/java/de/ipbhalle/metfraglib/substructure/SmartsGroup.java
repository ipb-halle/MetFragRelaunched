package de.ipbhalle.metfraglib.substructure;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultList;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class SmartsGroup extends DefaultList {

	private double probability;
	
	public SmartsGroup(double probability) {
		super();
		this.probability = probability;
	}

	public double getBestSimilarity(String smarts) {
		double maxSimilarity = 0.0;
		IAtomContainer con = MoleculeFunctions.parseSmiles(smarts);
		for(int i = 0; i < this.list.size(); i++) {
			String currentSmarts = ((SMARTSQueryTool)this.list.get(i)).getSmarts();
			double currentSimilarity = TanimotoSimilarity.calculateSimilarity(con, MoleculeFunctions.parseSmiles(currentSmarts));
			if(currentSimilarity > maxSimilarity) maxSimilarity = currentSimilarity;
		}
		return maxSimilarity;
	}
	
	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}

	public String getElement(int index) {
		return ((SMARTSQueryTool)this.list.get(index)).getSmarts();
	}
	
	public void addElement(String smarts) {
		this.list.add(new SMARTSQueryTool(smarts, DefaultChemObjectBuilder.getInstance()));
	}

	public void addElement(int index, SmartsGroup obj) {
		this.list.add(index, obj);
	}
	
	public boolean smartsMatches(ICandidate candidate) {
		IAtomContainer con = null;
		try {
			con = candidate.getAtomContainer();
		} catch (Exception e) {
			return false; 
		}
		for(int i = 0; i < this.list.size(); i++) {
			SMARTSQueryTool queryTool = (SMARTSQueryTool)this.list.get(i);
			try {
				if(queryTool.matches(con)) return true;
			} catch (CDKException e) {
				continue;
			} catch (Exception e) {
				continue;
			}
		}
		return false;
	}
	
	public void print() {
		System.out.print(this.probability);
		for(int i = 0; i < this.list.size(); i++) {
			System.out.print(" " + ((SMARTSQueryTool)this.list.get(i)).getSmarts());
		}
		System.out.println();
	}
}
