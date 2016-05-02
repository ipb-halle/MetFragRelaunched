package de.ipbhalle.metfraglib.exceptions;

public class MassLimitsNotDefinedException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public MassLimitsNotDefinedException() {
	}
	
	public MassLimitsNotDefinedException(String message) {
		super(message);
	}
	
	public MassLimitsNotDefinedException (Throwable cause)
    {
		super (cause);
    }

	public MassLimitsNotDefinedException (String message, Throwable cause)
    {
		super (message, cause);
    }

}
