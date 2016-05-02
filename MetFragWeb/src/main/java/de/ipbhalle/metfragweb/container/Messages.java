package de.ipbhalle.metfragweb.container;

import java.util.HashMap;

public class Messages {

	protected HashMap<String, String> errorMessages;
	
	public Messages() {
		this.errorMessages = new HashMap<String, String>();
	}

	public String getMessage(String id) {
		return this.errorMessages.containsKey(id) ? this.errorMessages.get(id) : "";
	}
	
	public void setMessage(String id, String message) {
		this.errorMessages.put(id, message);
	}
	
	public boolean containsKey(String id) {
		return this.errorMessages.containsKey(id);
	}
	
	public void removeKey(String key) {
		this.errorMessages.remove(key);
	}
}
