package de.ipbhalle.exception;

public class CouldNotFetchResultsException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6289950749164929483L;
	/**
	 * 
	 */
	private static final String MESSAGE_FORMAT = "Could not fetch results for process id '%s'";

    public CouldNotFetchResultsException(String processId) {
        super(String.format(MESSAGE_FORMAT, processId));
    }    
}
