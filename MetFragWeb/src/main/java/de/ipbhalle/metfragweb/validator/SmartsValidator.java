package de.ipbhalle.metfragweb.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

public class SmartsValidator implements Validator {

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object value)
			throws ValidatorException {
		try {
			UncaughtExceptionHandlerSmarts h = new UncaughtExceptionHandlerSmarts();
			RunThreadSmarts t = new RunThreadSmarts(value);
			t.setUncaughtExceptionHandler(h);
			t.start();
			while(t.isAlive()) {
				
			}
			if(!h.hasWorked()) throw new Exception();
		} 
		catch(Exception e) {
			FacesMessage msg = new FacesMessage();
			msg.setDetail("Invalid SMARTS. Example: c1ccccc1,CCCO");
			msg.setSummary("Invalid SMARTS. Example: c1ccccc1,CCCO");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
	
	public static boolean check(String value) {
		SmartsValidator s = new SmartsValidator();
		UncaughtExceptionHandlerSmarts h = s.new UncaughtExceptionHandlerSmarts();
		RunThreadSmarts t = s.new RunThreadSmarts(value);
		t.setUncaughtExceptionHandler(h);
		t.start();
		while(t.isAlive()) {
			
		}
		return h.hasWorked() && t.hasWorked();
	}

	public static boolean check(String[] value) {
		SmartsValidator s = new SmartsValidator();
		UncaughtExceptionHandlerSmarts h = s.new UncaughtExceptionHandlerSmarts();
		String string = "";
		if(value != null && value.length >= 1)
			string += value[0];
		for(int i = 1; i < value.length; i++) {
			string += "," + value[i];
		}
		RunThreadSmarts t = s.new RunThreadSmarts(string);
		t.setUncaughtExceptionHandler(h);
		t.start();
		while(t.isAlive()) {
			
		}
		return h.hasWorked() && t.hasWorked();
	}
	
	class UncaughtExceptionHandlerSmarts implements Thread.UncaughtExceptionHandler {

		private boolean worked = true;
		
		@Override
		public void uncaughtException(Thread arg0, Throwable arg1) {
			this.worked = false;
		}
		
		public void setWorked(boolean worked) {
			this.worked = worked;
		}
		
		public boolean hasWorked() {
			return this.worked;
		}
		
	}
	
	class RunThreadSmarts extends Thread {
		
		private Object value;
		private boolean worked;
		
		public RunThreadSmarts(Object value) {
			this.value = value;
			this.worked = true;
		}
		
		public boolean hasWorked() {
			return this.worked;
		}
		
		@Override
		public void run() {
			this.worked = true;
			if(value == null) {
				this.worked = false;
				return;
			}
			String string = ((String)value).trim();
			if(string.equals("")) {
				this.worked = false;
				return;
			}
			if(string.endsWith(",")) {
				this.worked = false;
				return;
			}
			String[] tmp = ((String)value).split(",");
			SMARTSQueryTool smartsQuerytools = new SMARTSQueryTool("CC", DefaultChemObjectBuilder.getInstance());
			for(int i = 0; i < tmp.length; i++) {
				if(tmp[i] == null) {
					this.worked = false;
					return;
				}
				tmp[i] = tmp[i].trim();
				if(tmp[i].equals("")) {
					this.worked = false;
					return;
				}
				try {
					smartsQuerytools.setSmarts(tmp[i]);
				} catch (CDKException e) {
					this.worked = false;
					return;
				}
			}
		}
	}
}
