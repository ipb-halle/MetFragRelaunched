package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.fragment.DefaultBitArrayFragment;
import de.ipbhalle.metfraglib.interfaces.IMatch;

public class MatchFragmentList {

	private MatchFragmentNode rootNode;
	
	public MatchFragmentList(MatchFragmentNode rootNode) {
		this.rootNode = rootNode;
	}

	public MatchFragmentList(IMatch fragment, double score) {
		this.rootNode = new MatchFragmentNode(fragment);
		this.rootNode.setScore(score);
	}

	public void removeElementByID(int id) {
		MatchFragmentNode nextNode = this.rootNode;
		MatchFragmentNode currentNode = null;
		boolean found = false;
		while(!found && nextNode != null) {
			if(nextNode.getFragment().getID() == id) {
				found = true;
				if(currentNode != null) {
					currentNode.setNext(nextNode.getNext());
				}
				else {
					this.rootNode = nextNode.getNext();
				}
			}
			else {
				currentNode = nextNode;
				nextNode = currentNode.getNext();
			}
		}
	}
	
	public void removeFirst() {
		this.rootNode = this.rootNode.getNext();
	}

	public void removeAfterRoot() {
		this.rootNode.setNext(null);
	}
	
	public MatchFragmentNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(MatchFragmentNode rootNode) {
		this.rootNode = rootNode;
	}
	
	/**
	 * 
	 * @param fingerprint
	 * @return
	 */
	public Double[] containsByFingerprint(de.ipbhalle.metfraglib.FastBitArray fingerprint) {
		MatchFragmentNode currentNode = this.rootNode;
		Double[] values = new Double[2];
		while(currentNode != null) {
			if(((DefaultBitArrayFragment)currentNode.getFragment()).getAtomsFastBitArray().equals(fingerprint)) {
				values[0] = currentNode.getScore();
				values[1] = (double)currentNode.getFragment().getID();
				return values;
			}
			currentNode = currentNode.getNext();
		}
		return null;
	}
	
	/**
	 * 
	 * @param newNode
	 */
	public void insert(MatchFragmentNode newNode) {
		newNode.setNext(null);
		if(this.rootNode == null) {
			this.rootNode = newNode;
			return;
		}
		MatchFragmentNode currentNode = null;
		MatchFragmentNode nextNode = this.rootNode;
		boolean foundPosition = false;
		/*
		 * 
		 */
		while(nextNode != null && !foundPosition) {
			if(nextNode.getScore() < newNode.getScore()) {
				MatchFragmentNode tmpNode = nextNode;
				nextNode = newNode;
				if(currentNode != null) {
					currentNode.setNext(newNode);
					newNode.setNext(tmpNode);
				}
				else {
					this.rootNode = newNode;
					this.rootNode.setNext(tmpNode);
				}
				foundPosition = true;
			}
			else {
				currentNode = nextNode;
				nextNode = nextNode.getNext();
			}
		}
		if(!foundPosition) {
			currentNode.setNext(newNode);
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param score
	 */
	public void insert(IMatch match, double score) {
		MatchFragmentNode currentNode = null;
		MatchFragmentNode nextNode = this.rootNode;
		boolean foundPosition = false;
		MatchFragmentNode newNode = new MatchFragmentNode(match);
		newNode.setScore(score);
		/*
		 * 
		 */
		while(nextNode != null && !foundPosition) {
			if(nextNode.getScore() < score) {
				MatchFragmentNode tmpNode = nextNode;
				nextNode = newNode;
				if(currentNode != null) {
					currentNode.setNext(newNode);
					newNode.setNext(tmpNode);
				}
				else {
					this.rootNode = newNode;
					this.rootNode.setNext(tmpNode);
				}
				foundPosition = true;
			}
			else {
				currentNode = nextNode;
				nextNode = nextNode.getNext();
			}
		}
		if(!foundPosition) currentNode.setNext(newNode);
	}
	
	public void printElements() {
		if(this.rootNode == null) return;
		MatchFragmentNode currentNode = this.rootNode;
		while(currentNode != null) {
			System.out.print(currentNode.getFragment().getID() + ":" + currentNode.getFragment().getMonoisotopicMass() + ":" + currentNode.getFragment().getMolecularFormula().toString() + ":" + currentNode.getScore() + "\t");
			currentNode = currentNode.getNext();
		}
		System.out.println();
		return;
	}
	
	public int countElements() {
		int num = 0;
		if(this.rootNode == null) return num;
		MatchFragmentNode currentNode = this.rootNode;
		num++;
		while(currentNode.hasNext()) {
			num++;
			currentNode = currentNode.getNext();
		}
		return num;
	}
	
}
