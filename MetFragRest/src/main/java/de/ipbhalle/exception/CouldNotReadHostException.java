package de.ipbhalle.exception;

public class CouldNotReadHostException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6289950749164929483L;
	/**
	 * 
	 */
	private static final String MESSAGE_FORMAT = "Could not read host for process id '%s'";

    public CouldNotReadHostException(String processId) {
        super(String.format(MESSAGE_FORMAT, processId));
    }    
}
