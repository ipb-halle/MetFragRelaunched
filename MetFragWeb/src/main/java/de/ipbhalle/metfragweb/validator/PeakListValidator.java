package de.ipbhalle.metfragweb.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import de.ipbhalle.metfraglib.list.DefaultPeakList;
import de.ipbhalle.metfraglib.peak.TandemMassPeak;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.peaklistreader.StringTandemMassPeakListReader;
import de.ipbhalle.metfraglib.settings.MetFragGlobalSettings;

public class PeakListValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value)
			throws ValidatorException {
		try {
			if(value == null) throw new Exception();
			String string = ((String)value).trim();
			if(string.length() == 0) throw new Exception();
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			settings.set(VariableNames.PEAK_LIST_STRING_NAME, string);
			settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 0.0);
			DefaultPeakList peaklist = new StringTandemMassPeakListReader(settings).read();
			if(((TandemMassPeak)peaklist.getElement(peaklist.getNumberElements() - 1)).getMass() > 1000)
				throw new Exception();
		} 
		catch(Exception e) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid peak list.");
			msg.setSummary("Invalid peak list.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
	
	public static boolean check(String value) {
		try {
			if(value == null) throw new Exception();
			String string = ((String)value).trim();
			if(string.length() == 0) throw new Exception();
			MetFragGlobalSettings settings = new MetFragGlobalSettings();
			settings.set(VariableNames.PEAK_LIST_STRING_NAME, string);
			settings.set(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME, 0.0);
			new StringTandemMassPeakListReader(settings).read();
		} 
		catch(Exception e) {
			return false;
		}
		return true;
	}
	
}
