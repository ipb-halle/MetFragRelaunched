package de.ipbhalle.metfragweb.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class PositiveDoubleValueValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value)
			throws ValidatorException {
		if(value == null) value = "0.0";
		try {
			Double val = Double.parseDouble((String)value);
			if(val < 0.0) throw new Exception();
		} catch(Exception e) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid value.");
			msg.setSummary("Invalid value.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
	
	public static boolean check(String value) {
		if(value == null) return false;
		try {
			Double val = Double.parseDouble((String)value);
			if(val < 0.0) return false;
			return true;
		} catch(Exception e) {
			return false;
		}
	}

}
