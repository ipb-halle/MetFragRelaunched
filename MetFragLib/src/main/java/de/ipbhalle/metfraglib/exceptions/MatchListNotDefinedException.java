package de.ipbhalle.metfraglib.exceptions;

public class MatchListNotDefinedException extends Exception {

	private static final long serialVersionUID = 1L;

	public MatchListNotDefinedException() {
	}
	
	public MatchListNotDefinedException(String message) {
		super(message);
	}
	
	public MatchListNotDefinedException (Throwable cause)
    {
		super (cause);
    }

	public MatchListNotDefinedException (String message, Throwable cause)
    {
		super (message, cause);
    }

}
