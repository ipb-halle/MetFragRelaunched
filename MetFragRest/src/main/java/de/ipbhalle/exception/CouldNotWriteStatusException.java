package de.ipbhalle.exception;

public class CouldNotWriteStatusException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6289950749164929483L;
	/**
	 * 
	 */
	private static final String MESSAGE_FORMAT = "Could not write status: '%s'";

    public CouldNotWriteStatusException(String reason) {
        super(String.format(MESSAGE_FORMAT, reason));
    }    
}
