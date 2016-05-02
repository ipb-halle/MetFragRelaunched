package de.ipbhalle.metfragweb.datatype;

import de.ipbhalle.metfraglib.interfaces.IFragment;

public class Fragment {

	protected String molecularFormula;
	protected IFragment fragment;
	protected String pathToImage;
	protected double mass;
	protected double peakMass;
	protected int id; 
	
	public Fragment(String molecularFormula, double mass, String pathToImage, double peakMass, int id) {
		this.molecularFormula = molecularFormula;
		this.mass = mass;
		this.pathToImage = pathToImage;
		this.peakMass = peakMass;
		this.id = id;
	}
	
	public double getPeakMass() {
		return peakMass;
	}

	public void setPeakMass(double peakMass) {
		this.peakMass = peakMass;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public String getMolecularFormula() {
		return this.molecularFormula;
	}

	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	public String getFragmentImage() {
		return this.pathToImage;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
}
