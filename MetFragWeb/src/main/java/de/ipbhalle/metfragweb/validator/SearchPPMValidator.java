package de.ipbhalle.metfragweb.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class SearchPPMValidator implements Validator {

	@Override
	public void validate(FacesContext context, UIComponent component, Object arg2)
			throws ValidatorException {
		
		if(isFormulaGiven(context)) {
			System.out.println("Formula is given");
			return;
		}
		if(isIdentifierGiven(context)) {
			System.out.println("Identifier is given.");
			return;
		}
		
		/*
		 * now check for ppm
		 */
		System.out.println("PPM value: " + arg2);
		if(arg2 == null) {
			System.out.println("ppm argument is null -> " + arg2);
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid value.");
			msg.setSummary("Invalid value.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
		
		double ppmValue = -1.0; 
		try {
			ppmValue = (Double)arg2;
		} catch(Exception e) {
			System.out.println("ppm couldn't be parsed -> " + arg2);
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid value.");
			msg.setSummary("Invalid value.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
		
		if(ppmValue < 0.0) {
			System.out.println("ppm smaller 0 -> " + ppmValue);
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid value.");
			msg.setSummary("Invalid value.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
		
	}

	private boolean isFormulaGiven(FacesContext context) {
		UIInput inputFormulaField = (UIInput) context.getViewRoot().findComponent("mainForm:inputFormula");
		if (inputFormulaField == null) {
			return false; 
		}
		String inputFormula = (String) inputFormulaField.getValue();
		
		if(inputFormula != null && inputFormula.trim().length() == 0) {
			return false;
		}
		return true;
	}
	
	private boolean isIdentifierGiven(FacesContext context) {
		UIInput inputIdentifiersField = (UIInput) context.getViewRoot().findComponent("mainForm:inputIdentifiers");
		if (inputIdentifiersField == null) {
			return false; 
		}
		String inputIdentifiers = (String) inputIdentifiersField.getValue();
		
		if(inputIdentifiers != null && inputIdentifiers.trim().length() == 0) {
			return false;
		}
		return true;
	}
	
}
