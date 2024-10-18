package de.ipbhalle.metfragweb.exception;
 
import java.util.Iterator;
import java.util.Map;

import jakarta.faces.FacesException;
import jakarta.faces.application.NavigationHandler;
import jakarta.faces.application.ViewExpiredException;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;

import org.primefaces.PrimeFaces;
 
public class ViewExpiredExceptionExceptionHandler extends ExceptionHandlerWrapper {
 
    private ExceptionHandler wrapped;
 
    public ViewExpiredExceptionExceptionHandler(ExceptionHandler wrapped) {
        this.wrapped = wrapped;
    }
 
    @Override
    public ExceptionHandler getWrapped() {
        return this.wrapped;
    }
 
    @Override
    public void handle() throws FacesException {
        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            Throwable t = context.getException();
            if (t instanceof ViewExpiredException) {
                ViewExpiredException vee = (ViewExpiredException) t;
                FacesContext fc = FacesContext.getCurrentInstance();
                Map<String, Object> requestMap = fc.getExternalContext().getRequestMap();
                NavigationHandler nav =
                        fc.getApplication().getNavigationHandler();
                try {
                    // Push some useful stuff to the request scope for
                    // use in the page
                    requestMap.put("currentViewId", vee.getViewId());
 
                    nav.handleNavigation(fc, null, "viewExpired");
                    fc.renderResponse();
                    PrimeFaces.current().executeScript("PF('expiredSessionDialog').show();");
                } finally {
                    i.remove();
                }
            }
        }
        // At this point, the queue will not contain any ViewExpiredEvents.
        // Therefore, let the parent handle them.
        getWrapped().handle();
 
    }
}
