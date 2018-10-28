package de.ipbhalle.metfraglib.exceptions;

public class ParameterNotKnownException extends Exception {

	private static final long serialVersionUID = 1L;

	public ParameterNotKnownException() {
	}
	
	public ParameterNotKnownException(String message) {
		super(message);
	}
	
	public ParameterNotKnownException (Throwable cause)
    {
		super (cause);
    }

	public ParameterNotKnownException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
