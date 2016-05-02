package de.ipbhalle.metfraglib.exceptions;

public class ExplicitHydrogenRepresentationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ExplicitHydrogenRepresentationException() {
	}
	
	public ExplicitHydrogenRepresentationException(String message) {
		super(message);
	}
	
	public ExplicitHydrogenRepresentationException (Throwable cause)
    {
		super (cause);
    }

	public ExplicitHydrogenRepresentationException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
