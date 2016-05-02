package de.ipbhalle.metfraglib.exceptions;

public class RelativeIntensityNotDefinedException extends Exception{

	private static final long serialVersionUID = 1L;

	public RelativeIntensityNotDefinedException() {
	}
	
	public RelativeIntensityNotDefinedException(String message) {
		super(message);
	}
	
	public RelativeIntensityNotDefinedException (Throwable cause)
    {
		super (cause);
    }

	public RelativeIntensityNotDefinedException (String message, Throwable cause)
    {
		super (message, cause);
    }
}
