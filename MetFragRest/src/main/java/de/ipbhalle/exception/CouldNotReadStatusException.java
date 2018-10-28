package de.ipbhalle.exception;

public class CouldNotReadStatusException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1943879727154448029L;
	private static final String MESSAGE_FORMAT = "Could not read status for process id '%s'";

    public CouldNotReadStatusException(String processId) {
        super(String.format(MESSAGE_FORMAT, processId));
    }    
}
