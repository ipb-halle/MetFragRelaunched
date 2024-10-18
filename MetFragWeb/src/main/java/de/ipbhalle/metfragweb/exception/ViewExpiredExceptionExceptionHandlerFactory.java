package de.ipbhalle.metfragweb.exception;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;
import de.ipbhalle.metfragweb.exception.ViewExpiredExceptionExceptionHandler;

public class ViewExpiredExceptionExceptionHandlerFactory extends ExceptionHandlerFactory {
	private ExceptionHandlerFactory parent;
	 
    public ViewExpiredExceptionExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }
 
    @Override
    public ExceptionHandler getExceptionHandler() {
        ExceptionHandler result = parent.getExceptionHandler();
        result = new ViewExpiredExceptionExceptionHandler(result);
 
        return result;
    }

}
