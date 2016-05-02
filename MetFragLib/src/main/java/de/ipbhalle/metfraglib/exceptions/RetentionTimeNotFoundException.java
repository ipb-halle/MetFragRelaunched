package de.ipbhalle.metfraglib.exceptions;

public class RetentionTimeNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	public RetentionTimeNotFoundException() {
	}
	
	public RetentionTimeNotFoundException(String message) {
		super(message);
	}
	
	public RetentionTimeNotFoundException (Throwable cause)
    {
		super (cause);
    }

	public RetentionTimeNotFoundException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
