package de.ipbhalle.metfraglib.exceptions;

public class TooFewCandidatesException extends Exception {

	private static final long serialVersionUID = 1L;

	public TooFewCandidatesException() {
	}
	
	public TooFewCandidatesException(String message) {
		super(message);
	}
	
	public TooFewCandidatesException (Throwable cause)
    {
		super (cause);
    }

	public TooFewCandidatesException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
