package de.ipbhalle.exception;

public class CouldNotCreateProcessException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6289950749164929483L;
	/**
	 * 
	 */
	private static final String MESSAGE_FORMAT = "Could not create process: '%s'";

    public CouldNotCreateProcessException(String reason) {
        super(String.format(MESSAGE_FORMAT, reason));
    }    
}
