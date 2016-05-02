package de.ipbhalle.metfraglib.exceptions;

public class MultipleHeadersFoundInInputDatabaseException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MultipleHeadersFoundInInputDatabaseException() {
	}
	
	public MultipleHeadersFoundInInputDatabaseException(String message) {
		super(message);
	}
	
	public MultipleHeadersFoundInInputDatabaseException (Throwable cause)
    {
		super (cause);
    }

	public MultipleHeadersFoundInInputDatabaseException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
