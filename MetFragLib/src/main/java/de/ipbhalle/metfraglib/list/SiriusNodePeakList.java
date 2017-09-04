package de.ipbhalle.metfraglib.list;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.exceptions.RelativeIntensityNotDefinedException;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;
import de.ipbhalle.metfraglib.peak.SiriusNodePeak;

public class SiriusNodePeakList extends SortedTandemMassPeakList {
	
	protected SiriusNodePeak root;
	protected java.util.ArrayList<SiriusNodePeak> leaves;
	
	public SiriusNodePeakList(double measuredPrecursorMass, String filename) {
		super(measuredPrecursorMass);
		this.list = new java.util.ArrayList<Object>();
		this.leaves = new java.util.ArrayList<SiriusNodePeak>();
		this.parseDotFile(filename);
	}
	
	public SiriusNodePeak getRoot() {
		return this.root;
	}
	
	/**
	 * generate SiriusNodePeakList from dot file
	 * 
	 * @param filename
	 * @return
	 */
	public void parseDotFile(String filename) {
		java.util.Hashtable<String, SiriusNodePeak> nodes = new java.util.Hashtable<String, SiriusNodePeak>();
		double maxIntensity = 0.0;
		try {
			java.io.BufferedReader breader = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(filename)));
			String line = breader.readLine();
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() == 1) continue;
				if(line.contains("->")) {
					String[] tmp = line.split("\\s+");
					
					SiriusNodePeak curChild = nodes.get(tmp[2].trim());
					SiriusNodePeak curFather = nodes.get(tmp[0].trim());
					
					try {
						curChild.setLossFormula(new ByteMolecularFormula(tmp[3].replaceFirst("\\[label=\"", "").replace("\"];", "").trim()));
					} catch (AtomTypeNotKnownFromInputListException e) {
						e.printStackTrace();
					}
					curChild.setFather(curFather);
					curFather.addChild(curChild);
				}
				else {
					String properties = line.replaceFirst(".*label=\"", "").replace("\"];", "");
					String id = line.split("\\s+")[0].trim();
					String[] tmp = properties.split("\\\\n");
					String fragmentFormula = tmp[0];
					double mass = Double.parseDouble(tmp[1].split("\\s+")[0].trim());
					double intensity = 0.0;
					try {
						intensity = Double.parseDouble(properties.split("Intensity=")[1].split("\\\\n")[0]);
					}
					catch(java.lang.ArrayIndexOutOfBoundsException e) {
						//precursor has no intensity
						intensity = 0.0;
					}
					if(intensity > maxIntensity) maxIntensity = intensity;
					SiriusNodePeak curNode = null;
					try {
						curNode = new SiriusNodePeak(mass, fragmentFormula, intensity);
					} catch (AtomTypeNotKnownFromInputListException e) {
						e.printStackTrace();
					}
					curNode.setCompleteNodeLabel(properties);
					curNode.setIntensity(intensity);
					curNode.setSiriusID(id);
					nodes.put(id, curNode);
					/*
					 * add peak sorted
					 */
					this.addElement(curNode);
				}
			}
			breader.close();
		} catch (java.io.FileNotFoundException e) {
			e.printStackTrace();
		} catch (java.io.IOException e) {
			e.printStackTrace();
		}
		
		java.util.Enumeration<SiriusNodePeak> elems = nodes.elements();
		while(elems.hasMoreElements()) {
			SiriusNodePeak node = elems.nextElement();
			try {
				node.setIntensity((node.getIntensity() / maxIntensity) * 999.0);
			} catch (RelativeIntensityNotDefinedException e) {
				e.printStackTrace();
			}
			if(node.isRoot()) {
				this.root = node;
			} 
			if(node.isLeaf()) this.leaves.add(node);
		}
	}
	
	public void addElement(SiriusNodePeak tandemMassPeak) {
		int index = 0;
		double mass = tandemMassPeak.getMass();
		while(index < this.list.size() && mass > ((SiriusNodePeak)this.list.get(index)).getMass()) index++;
		this.list.add(index, tandemMassPeak);
	}
	
	public java.util.ArrayList<SiriusNodePeak> getLeaves() {
		return this.leaves;
	}
	
	public int getNumberElements() {
		return this.list.size();
	}
	
}
