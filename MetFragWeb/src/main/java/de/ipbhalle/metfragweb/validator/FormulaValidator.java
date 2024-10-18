package de.ipbhalle.metfragweb.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import de.ipbhalle.metfraglib.molecularformula.ByteMolecularFormula;

public class FormulaValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value)
			throws ValidatorException {
		try {
			if(value == null) throw new Exception();
			String string = ((String)value).trim();
			if(string.length() == 0) throw new Exception();
			new ByteMolecularFormula(string);
		} 
		catch(Exception e) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid formula.");
			msg.setSummary("Invalid formula.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
	
	public static boolean check(String value) {
		try {
			if(value == null) throw new Exception();
			String string = ((String)value).trim();
			if(string.length() == 0) throw new Exception();
			new ByteMolecularFormula(string);
		} 
		catch(Exception e) {
			return false;
		}
		return true;
	}
	
}
