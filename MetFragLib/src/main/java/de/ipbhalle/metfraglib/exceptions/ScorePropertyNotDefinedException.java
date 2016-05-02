package de.ipbhalle.metfraglib.exceptions;

public class ScorePropertyNotDefinedException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public ScorePropertyNotDefinedException() {
	}
	
	public ScorePropertyNotDefinedException(String message) {
		super(message);
	}
	
	public ScorePropertyNotDefinedException (Throwable cause)
    {
		super (cause);
    }

	public ScorePropertyNotDefinedException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
