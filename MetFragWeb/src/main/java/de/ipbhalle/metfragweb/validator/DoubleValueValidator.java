package de.ipbhalle.metfragweb.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

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
