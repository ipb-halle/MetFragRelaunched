package de.ipbhalle.metfragweb.datatype;

import java.io.Serializable;

public class Weight implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8797747796102574125L;
	protected String name;
	protected int value;
	
	public Weight(String name, int value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
}
