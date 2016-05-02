package de.ipbhalle.metfragweb.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

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
