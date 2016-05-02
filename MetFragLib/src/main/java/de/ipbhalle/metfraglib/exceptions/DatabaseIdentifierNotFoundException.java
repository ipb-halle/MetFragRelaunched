package de.ipbhalle.metfraglib.exceptions;

public class DatabaseIdentifierNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public DatabaseIdentifierNotFoundException() {
	}
	
	public DatabaseIdentifierNotFoundException(String message) {
		super(message);
	}
	
	public DatabaseIdentifierNotFoundException (Throwable cause)
    {
		super (cause);
    }

	public DatabaseIdentifierNotFoundException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
