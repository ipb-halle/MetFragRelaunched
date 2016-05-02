package de.ipbhalle.metfragweb.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class DoubleValueValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value)
			throws ValidatorException {
		if(value == null) value = "0.0";
		try {
			Double.parseDouble((String)value);
		} catch(Exception e) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid value.");
			msg.setSummary("Invalid value.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}

	
	
}
