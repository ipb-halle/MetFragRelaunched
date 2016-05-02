package de.ipbhalle.metfraglib.exceptions;

public class AtomTypeNotKnownFromInputListException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public AtomTypeNotKnownFromInputListException() {
	}
	
	public AtomTypeNotKnownFromInputListException(String message) {
		super(message);
	}
	
	public AtomTypeNotKnownFromInputListException (Throwable cause)
    {
		super (cause);
    }

	public AtomTypeNotKnownFromInputListException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
