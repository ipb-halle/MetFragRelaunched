package de.ipbhalle.metfraglib.exceptions;

public class RelativeScoreValueNotDefinedException extends Exception {

	private static final long serialVersionUID = 1L;

	public RelativeScoreValueNotDefinedException() {
	}
	
	public RelativeScoreValueNotDefinedException(String message) {
		super(message);
	}
	
	public RelativeScoreValueNotDefinedException (Throwable cause)
    {
		super (cause);
    }

	public RelativeScoreValueNotDefinedException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
