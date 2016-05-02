package de.ipbhalle.metfraglib.exceptions;

public class NoValidDatabaseSearchSettingsDefined extends Exception {
	
	private static final long serialVersionUID = 1L;

	public NoValidDatabaseSearchSettingsDefined() {
	}
	
	public NoValidDatabaseSearchSettingsDefined(String message) {
		super(message);
	}
	
	public NoValidDatabaseSearchSettingsDefined (Throwable cause)
    {
		super (cause);
    }

	public NoValidDatabaseSearchSettingsDefined (String message, Throwable cause)
    {
		super (message, cause);
    }
}