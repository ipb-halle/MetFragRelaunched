package de.ipbhalle.metfraglib.substructure;

import java.util.Vector;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.additionals.MoleculeFunctions;
import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.list.DefaultList;
import de.ipbhalle.metfraglib.similarity.TanimotoSimilarity;

public class SmartsGroup extends DefaultList {

	private Double jointProbability = null;
	private Double conditionalProbability_ps = null;
	private Double conditionalProbability_sp = null;
	
	private Double probability = null;
	
	private java.util.Vector<String> smiles;
	private java.util.Vector<String> fingerprints;
	private Integer id = null;

	public SmartsGroup(Double probability) {
		super();
		this.probability = probability;
	}

	public SmartsGroup(Double probability, Double jointProbability, Double conditionalProbability_ps, Double conditionalProbability_sp) {
		super();
		this.probability = probability;
		this.jointProbability = jointProbability;
		this.conditionalProbability_ps = conditionalProbability_ps;
		this.conditionalProbability_sp = conditionalProbability_sp;
	}

	public void addSmiles(String smiles) {
		if(this.smiles == null) this.smiles = new java.util.Vector<String>();
		this.smiles.add(smiles);
	}

	public void addFingerprint(String fingerprint) {
		if(this.fingerprints == null) this.fingerprints = new java.util.Vector<String>();
		this.fingerprints.add(fingerprint);
	}
	
	public double getBestSimilarity(String smiles) {
		double maxSimilarity = 0.0;
		for(int i = 0; i < this.list.size(); i++) {
			String currentSmiles = this.smiles.get(i);
			if(currentSmiles.equals(smiles)) return 1.0;
		}
		IAtomContainer con = MoleculeFunctions.parseSmiles(smiles);
		for(int i = 0; i < this.list.size(); i++) {
			String currentSmiles = this.smiles.get(i);
			double currentSimilarity = TanimotoSimilarity.calculateSimilarity(con, MoleculeFunctions.parseSmiles(currentSmiles));
			if(currentSimilarity > maxSimilarity) maxSimilarity = currentSimilarity;
		}
		return maxSimilarity;
	}
	
	public void setProbabilityToJointProbability() {
		this.probability = this.jointProbability;
	}

	public void setProbabilityToConditionalProbability_ps() {
		this.probability = this.conditionalProbability_ps;
	}

	public void setProbabilityToConditionalProbability_sp() {
		this.probability = this.conditionalProbability_sp;
	}
	
	public void setProbability(Double probability) {
		this.probability = probability;
	}

	public Double getProbability() {
		return probability;
	}

	public Double getJointProbability() {
		return jointProbability;
	}

	public void setJointProbability(Double jointProbability) {
		this.jointProbability = jointProbability;
	}

	public Double getConditionalProbability_ps() {
		return conditionalProbability_ps;
	}

	public void setConditionalProbability_ps(Double conditionalProbability_ps) {
		this.conditionalProbability_ps = conditionalProbability_ps;
	}

	public Double getConditionalProbability_sp() {
		return conditionalProbability_sp;
	}

	public void setConditionalProbability_sp(Double conditionalProbability_sp) {
		this.conditionalProbability_sp = conditionalProbability_sp;
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
	
	public boolean fingerprintMatches(Vector<String> _fingerprints) {
		for(int i = 0; i < _fingerprints.size(); i++) {
			for(int j = 0; j < this.fingerprints.size(); j++) {
				if(_fingerprints.get(i).equals(this.fingerprints.get(j))) return true;
			}
		}
		return false;
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
	
	public String toString() {
		String string = this.probability + "";
		for(int i = 0; i < this.list.size(); i++) {
			string += " " + ((SMARTSQueryTool)this.list.get(i)).getSmarts();
		}
		return string;
	}
	
	public void removeDuplicates() {
		java.util.Vector<Object> newList = new java.util.Vector<Object>();
		java.util.Vector<String> newSmiles = new java.util.Vector<String>();
		java.util.Vector<String> newFingerprints = new java.util.Vector<String>();
		for(int i = 0; i < this.list.size(); i++) {
			String current = ((SMARTSQueryTool)this.list.get(i)).getSmarts();
			if(!newList.contains(current)) {
				newList.add(current);
				newSmiles.add(this.smiles.get(i));
				newFingerprints.add(this.fingerprints.get(i));
			}
		}
		this.list = new java.util.Vector<Object>();
		for(int i = 0; i < newList.size(); i++) {
			SMARTSQueryTool sqt = new SMARTSQueryTool((String)newList.get(i), DefaultChemObjectBuilder.getInstance());
			this.list.add(sqt);
		}
		this.smiles = newSmiles;
		this.fingerprints = newFingerprints;
	}
	
	public String toStringSmiles() {
		String string = this.probability + "";
		for(int i = 0; i < this.smiles.size(); i++) {
			string += " " + this.smiles.get(i);
		}
		return string;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public java.util.Vector<String> getSmiles() {
		return this.smiles;
	}
	
	public java.util.Vector<String> getFingerprints() {
		return this.fingerprints;
	}
}
