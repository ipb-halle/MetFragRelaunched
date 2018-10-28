package de.ipbhalle.metfraglib.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.ArrayList;

import de.ipbhalle.metfraglib.exceptions.AtomTypeNotKnownFromInputListException;
import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;

public class SiriusDotToMetFragInput {

	private Node rootNode;
	private String nodesString = "";
	
	public SiriusDotToMetFragInput(String filename) {
		try {
			BufferedReader breader = new BufferedReader(new FileReader(new File(filename)));
			String line = "";
			Hashtable<String, Node> labelToNode = new Hashtable<String, Node>();
			breader.readLine();
			while((line = breader.readLine()) != null) {
				line = line.trim();
				if(line.length() <= 1) continue;
				if(line.contains("->")) {
					String[] tmp = line.split("\\s+");
					String loss = tmp[3].substring(8).replaceAll("\"];", "");
					labelToNode.get(tmp[2].trim()).setLoss(loss);
					labelToNode.get(tmp[0].trim()).addChild(labelToNode.get(tmp[2].trim()));
					labelToNode.get(tmp[2].trim()).setFather(labelToNode.get(tmp[0].trim()));
				}
				else {
					line = line.replaceAll("\\\\n", " ");
					String[] tmp = line.split("\\s+");
					Node newNode = new Node(tmp[0].trim(), tmp[1].substring(8).trim() + " " + tmp[4]);
					labelToNode.put(tmp[0].trim(), newNode);
				}
			}
			breader.close();
			java.util.Enumeration<String> keys = labelToNode.keys();
			while(keys.hasMoreElements() && this.rootNode == null) {
				Node currentNode = labelToNode.get(keys.nextElement());
				if(currentNode.getFather() == null) {
					this.rootNode = currentNode;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getNodesString() {
		this.nodesString = "";
		this.getSingleNodeString("", this.rootNode);
		return this.nodesString;
	}
	
	private void getSingleNodeString(String prefix, Node node) {
		this.nodesString += prefix + node.toString() + "\n";
		if(node.hasChild()) {
			for(int i = 0; i < node.getNumberChildren(); i++) 
				this.getSingleNodeString(prefix + "-", node.getChild(i));
		}
	}
	
	class Node {
		private String id;
		private Node father;
		private ArrayList<Node> children;
		private String formula;
		private double mass;
		private String intensity;
		private String loss;
		
		public Node(String id, String label) {
			this.id = id;
			String[] tmp = label.split(" ");
			this.formula = tmp[0].trim();
			this.intensity = tmp[1].trim();
			this.loss = "";
			try {
				this.mass = new ByteMolecularFormula(this.formula).getMonoisotopicMass();
			} catch (AtomTypeNotKnownFromInputListException e) {
				e.printStackTrace();
			}
		}
		
		public String toString() {
			return this.mass + " " + this.intensity + " " + this.formula + " " + this.loss;
		}
		
		public int getNumberChildren() {
			return this.children == null || this.children.size() == 0 ? 0 : this.children.size();
		}
		
		public boolean hasChild() {
			if(this.children == null || this.children.size() == 0) return false;
			return true;
		}
		
		public String getFormula() {
			return this.formula;
		}
		
		public String getLoss() {
			return this.loss;
		}
		
		public void setLoss(String loss) {
			this.loss = loss;
		}
		
		public String getID() {
			return this.id;
		}
		
		public void setFather(Node father) {
			this.father = father;
		}
		
		public Node getFather() {
			return this.father;
		}
		
		public void addChild(Node child) {
			if(this.children == null) {
				this.children = new ArrayList<Node>();
			}
			this.children.add(child);
		}
		
		public Node getChild(int index) {
			return this.children.get(index);
		}
	}
	
	public static void main(String[] args) {
		SiriusDotToMetFragInput sdtmfi = new SiriusDotToMetFragInput(args[0]);
		System.out.println(sdtmfi.getNodesString());
	}
	
}
