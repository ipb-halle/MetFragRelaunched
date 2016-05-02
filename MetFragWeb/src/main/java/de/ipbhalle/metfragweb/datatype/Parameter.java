package de.ipbhalle.metfragweb.datatype;

public class Parameter {
	
	private String value;
	private String key;
	
	public Parameter(String key, String value) {
		this.value = value;
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
}
