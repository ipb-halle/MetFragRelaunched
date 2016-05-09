package de.ipbhalle.metfraglib.settings;

import java.util.HashMap;

public class Settings {
	
	protected HashMap<String, Object> map;
	protected HashMap<String, Object> defaults;
	
	public Settings() {
		this.map = new HashMap<String, Object>();
		this.defaults = new HashMap<String, Object>();
	}
	
	public Object remove(String key) {
		Object obj = this.defaults.remove(key);
		if(this.map.containsKey(key) && this.map.get(key) != null)
			return this.map.remove(key);
		return obj;
	}
	
	public Object get(String variableName) {
		if(this.map.containsKey(variableName) && this.map.get(variableName) != null) 
			return this.map.get(variableName);
		return this.defaults.get(variableName);
	}
	
	public Object getDefaults(String variableName) {
		return this.defaults.get(variableName);
	}

	public Object getMap(String variableName) {
		return this.map.get(variableName);
	}

	public void set(String variableName, Object obj) {
		this.map.put(variableName, obj);
	}
	
	public boolean containsKey(String key) {
		return this.map.containsKey(key) || this.defaults.containsKey(key);
	}
	
	public java.util.Set<String> getKeys() {
		return this.map.keySet();
	}
	
	public static boolean checkEmailAddress(String emailAddress) {
		String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
	    java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
	    java.util.regex.Matcher m = p.matcher(emailAddress);
	  	return m.matches();
	}
	
	public void printMapKeys() {
		java.util.Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
	}
	
	public void printDefaultKeys() {
		java.util.Iterator<String> it = this.defaults.keySet().iterator();
		while(it.hasNext()) {
			System.out.println(it.next());
		}
	}

	public void printMapValues() {
		java.util.Iterator<String> it = this.map.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			System.out.println(key + " => " + this.map.get(key));
		}
	}

	public void printDefaultValues() {
		java.util.Iterator<String> it = this.defaults.keySet().iterator();
		while(it.hasNext()) {
			String key = it.next();
			System.out.println(key + " => " + this.defaults.get(key));
		}
	}
	
	//for MetFragR

	public void set(String variableName, Integer obj) {
		this.map.put(variableName, obj);
	}

	public void set(String variableName, Double obj) {
		this.map.put(variableName, obj);
	}
	
	public void set(String variableName, Byte obj) {
		this.map.put(variableName, obj);
	}
	
	public void set(String variableName, String[] obj) {
		this.map.put(variableName, obj);
	}
	
	public void set(String variableName, Double[] obj) {
		this.map.put(variableName, obj);
	}

	public void set(String variableName, double[] obj) {
		Double[] values = new Double[obj.length];
		for(int i = 0; i < obj.length; i++) values[i] = obj[i]; 
		this.map.put(variableName, values);
	}

	public void set(String variableName, Boolean obj) {
		this.map.put(variableName, obj);
	}

	public void set(String variableName, String obj) {
		this.map.put(variableName, obj);
	}
}
