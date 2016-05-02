package de.ipbhalle.metfraglib.match;

import de.ipbhalle.metfraglib.interfaces.IPeak;

public class MatchPeakList {

	private MatchPeakNode rootNode;
	
	public MatchPeakList(MatchPeakNode rootNode) {
		this.rootNode = rootNode;
	}

	public MatchPeakList(IPeak peak, double score, int id) {
		this.rootNode = new MatchPeakNode(peak, id);
		this.rootNode.setScore(score);
	}
	
	public void removeFirst() {
		this.rootNode = this.rootNode.getNext();
	}
	
	public void removeAfterRoot() {
		this.rootNode.setNext(null);
	}
	
	public void removeElementByID(int id) {
		MatchPeakNode nextNode = this.rootNode;
		MatchPeakNode currentNode = null;
		boolean found = false;
		while(!found && nextNode != null) {
			if(nextNode.getId() == id) {
				found = true;
				if(currentNode != null) {
					currentNode.setNext(nextNode.getNext());
				}
				else {
					this.rootNode = nextNode.getNext();
				}
				if(this.rootNode != null && this.rootNode.getNext() != null) {
				//	System.out.println("removed " + this.rootNode.getId() + " " + this.rootNode.getNext().getId() + " " + this.rootNode.getNext().getNext());
				//	this.printElements();
				}
				
			}
			else {
				currentNode = nextNode;
				nextNode = currentNode.getNext();
			}
		}
	//	if(this.rootNode != null) System.out.println(this.rootNode.getId() + " current root");
		//next of root points to root
		if(this.rootNode != null && this.rootNode.getNext() != null && this.rootNode.getId() == this.rootNode.getNext().getId()) {
			System.out.println("conflict removing 1");
			System.exit(1);
		}
			
	}
	
	/**
	 * returns score and id of element if contained otherwise null
	 * 
	 * @param id
	 * @return
	 */
	public Double[] contains(int id) {
		MatchPeakNode currentNode = this.rootNode;
		Double[] values = new Double[2];
		while(currentNode != null) {
			if(currentNode.getId() == id) {
				values[0] = currentNode.getScore();
				values[1] = (double)id;
				return values;
			}
			currentNode = currentNode.getNext();
		}
		return null;
	}
	
	public MatchPeakNode getElementById(int id) {
		MatchPeakNode currentNode = this.rootNode;
		while(currentNode != null) {
			if(currentNode.getId() == id) {
				return currentNode;
			}
			currentNode = currentNode.getNext();
		}
		return null;
	}
	
	public MatchPeakNode getRootNode() {
		return rootNode;
	}

	/**
	 * 
	 * @param node
	 * @param score
	 */
	public void insert(IPeak peak, double score, int id) {
		MatchPeakNode newNode = new MatchPeakNode(peak, id);
		newNode.setScore(score);
		if(this.rootNode == null) {
			this.rootNode = newNode;
			return;
		}
		MatchPeakNode currentNode = null;
		MatchPeakNode nextNode = this.rootNode;
		boolean foundPosition = false;
		/*
		 * 
		 */
		while(nextNode != null && !foundPosition) {
			if(nextNode.getScore() < score) {
				MatchPeakNode tmpNode = nextNode;
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
		
		if(this.rootNode != null && this.rootNode.getNext() != null && this.rootNode.getNext().getId() == this.rootNode.getId()) {
			System.out.println("conflict inserting 1");
			this.printElements();
			System.exit(1);
		}
	}
	
	/**
	 * 
	 * @param newNode
	 */
	public void insert(MatchPeakNode newNode) {
		newNode.setNext(null);
		if(this.rootNode == null) {
			this.rootNode = newNode;
			return;
		}
		int rootId = this.rootNode.getId();
		MatchPeakNode currentNode = null;
		MatchPeakNode nextNode = this.rootNode;
		boolean foundPosition = false;
		/*
		 * 
		 */
		while(nextNode != null && !foundPosition) {
			if(nextNode.getScore() < newNode.getScore()) {
				MatchPeakNode tmpNode = nextNode;
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
				if(nextNode != null && nextNode.getId() == rootId) {
					System.out.println("conflict inserting");
					System.out.println(currentNode.getId() + " " + nextNode.getId());
					System.exit(1);
				}
					
			}
		}
		if(!foundPosition) {
			currentNode.setNext(newNode);
		}
	}
	/**
	 * 
	 * @return
	 */
	public int countElements() {
		int num = 0;
		if(this.rootNode == null) return num;
		int rootId = this.rootNode.getId();
		MatchPeakNode currentNode = this.rootNode;
		num++;
		while(currentNode.hasNext()) {
			num++;
			currentNode = currentNode.getNext();
			if(currentNode != null && currentNode.getId() == rootId) {
				System.out.println("conflict counting");
				System.out.println(currentNode.getId() + " " + currentNode.getId());
				System.exit(1);
			}
		}
		return num;
	}
	
	public void printElements() {
		if(this.rootNode == null) return;
		MatchPeakNode currentNode = this.rootNode;
		int rootId = this.rootNode.getId();
		while(currentNode != null) {
			System.out.print(currentNode.getId()+":"+currentNode.getPeak().getMass() + ":" + currentNode.getPeak().getAbsoluteIntensity() + ":" + currentNode.getScore() + " ");
			currentNode = currentNode.getNext();
			if(currentNode != null && currentNode.getId() == rootId) {
				System.out.println("conflict printing");
				System.out.println(currentNode.getId() + " " + currentNode.getId());
				System.exit(1);
			}
		}
		System.out.println();
		return;
	}
	
}
