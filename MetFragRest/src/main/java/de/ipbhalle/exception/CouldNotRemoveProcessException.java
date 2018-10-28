package de.ipbhalle.exception;

public class CouldNotRemoveProcessException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6289950749164929483L;
	/**
	 * 
	 */
	private static final String MESSAGE_FORMAT = "Could not remove process: '%s'";

    public CouldNotRemoveProcessException(String reason) {
        super(String.format(MESSAGE_FORMAT, reason));
    }    
}
