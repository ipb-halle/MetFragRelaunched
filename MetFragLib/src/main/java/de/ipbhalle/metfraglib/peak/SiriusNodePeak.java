package de.ipbhalle.metfraglib.peak;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.interfaces.IMolecularFormula;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;

public class SiriusNodePeak extends TandemMassPeak {

	protected SiriusNodePeak father;
	protected IMolecularFormula molecularFormula;
	protected IMolecularFormula lossFormula;
	protected java.util.Vector<SiriusNodePeak> children;
	protected String siriusID;
	protected String completeNodeLabel;
	
	protected boolean flag;
	
	public SiriusNodePeak(double mass, String fragmentFormula, double intensity) throws AtomTypeNotKnownFromInputListException {
		super(mass, intensity);
		this.molecularFormula = new ByteMolecularFormula(fragmentFormula);
		this.flag = false;
	}
	
	public SiriusNodePeak(double mass, double intensity, String fragmentFormula, String lossFormula, String siriusID, String completeNodeLabel) throws AtomTypeNotKnownFromInputListException {
		super(mass, intensity);
		this.molecularFormula = new ByteMolecularFormula(fragmentFormula);
		this.lossFormula = new ByteMolecularFormula(lossFormula);
		this.siriusID = siriusID;
		this.completeNodeLabel = completeNodeLabel;
		this.flag = false;
	}
	
	public IMolecularFormula getMolecularFormula() {
		return molecularFormula;
	}

	public void setMolecularFormula(IMolecularFormula molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public IMolecularFormula getLossFormula() {
		return lossFormula;
	}

	public void setLossFormula(IMolecularFormula lossFormula) {
		this.lossFormula = lossFormula;
	}

	public java.util.Vector<SiriusNodePeak> getChildren() {
		return children;
	}

	public void setChildren(java.util.Vector<SiriusNodePeak> children) {
		this.children = children;
	}

	public String getSiriusID() {
		return siriusID;
	}

	public void setSiriusID(String siriusID) {
		this.siriusID = siriusID;
	}

	public String getCompleteNodeLabel() {
		return completeNodeLabel;
	}

	public void setCompleteNodeLabel(String completeNodeLabel) {
		this.completeNodeLabel = completeNodeLabel;
	}

	public SiriusNodePeak getFather() {
		return father;
	}

	public void setFather(SiriusNodePeak father) {
		this.father = father;
	}
	
	public void addChild(SiriusNodePeak child) {
		if(this.children == null) 
			this.children = new java.util.Vector<SiriusNodePeak>();
		this.children.add(child);
	}
	

	public boolean isLeaf() {
		if(this.children == null || this.children.size() == 0)
			return true;
		return false;
	}

	public boolean isRoot() {
		return this.father == null ? true : false;
	}
	
	public boolean getFlag() {
		return this.flag;
	}
	
	public void setFlag(boolean flag) {
		this.flag = flag;
	}
	
	public double getMass() {
		return this.molecularFormula.getMonoisotopicMass();
	}
	
	/**
	 * 
	 * @param add
	 * @param textcolor
	 * @param fillcolor
	 * @return
	 */
	public String getDotEntry(String add, String textcolor, String fillcolor) {
		add = add.trim();
		String entry = "ID: " + this.siriusID + "\\n" + this.completeNodeLabel;
		if(add.length() != 0) entry += "\\n" +  add;
		entry = this.siriusID + " [label=\"" + entry + "\",fontcolor=\"" + textcolor + "\",color=\"" + fillcolor + "\",style=filled];";
		if(this.children == null || this.children.size() == 0) {
			return entry;
		}
		entry += "\n";
		for(int i = 0; i < this.children.size(); i++) {
			entry += this.siriusID + " -> " + this.children.get(i).getSiriusID() + " " + "[label=\"" +  this.children.get(i).getLossFormula() + "\"];";
			if(i < this.children.size() - 1) entry += "\n";
		}
		return entry;
	}
	
	/**
	 * 
	 * @param add
	 * @return
	 */
	public String getDotEntry(String add) {
		add = add.trim();
		String entry = "ID: " + this.siriusID + " " + "\\n" + this.completeNodeLabel;
		if(add.length() != 0) entry += "\\n" +  add;
		entry = this.siriusID + " [label=\"" + entry + "\"];";
		if(this.children == null || this.children.size() == 0) {
			return entry;
		}
		entry += "\n";
		for(int i = 0; i < this.children.size(); i++) {
			entry += this.siriusID + " -> " + this.children.get(i).getSiriusID() + " " + "[label=\"" + this.children.get(i).getLossFormula() + "\"];";
			if(i < this.children.size() - 1) entry += "\n";
		}
		return entry;
	}
}
