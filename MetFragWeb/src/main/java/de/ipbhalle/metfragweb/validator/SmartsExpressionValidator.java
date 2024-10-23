package de.ipbhalle.metfragweb.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

public class SmartsExpressionValidator implements Validator {

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
			msg.setDetail("Invalid SMARTS Expression.");
			msg.setSummary("Invalid SMARTS Expression.");
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}
	
	public static boolean check(String value) {
		SmartsExpressionValidator s = new SmartsExpressionValidator();
		UncaughtExceptionHandlerSmarts h = s.new UncaughtExceptionHandlerSmarts();
		RunThreadSmarts t = s.new RunThreadSmarts(value);
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
			java.util.Vector<String> smarts_vec = new java.util.Vector<String>();
			String modified_formula = ((String)this.value).trim();
			String formula = "";
			String[] tmp = modified_formula.trim().split("\\s+");
			for(int i = 0; i < tmp.length; i++) {
				if(tmp[i].matches("\\(+") || tmp[i].matches("\\)+")) formula += tmp[i];
				else if(tmp[i].equals("not") || tmp[i].equals("Not") || tmp[i].equals("NOT")) formula += "!";
				else if(tmp[i].equals("and") || tmp[i].equals("And") || tmp[i].equals("AND")) formula += "&&";
				else if(tmp[i].equals("or") || tmp[i].equals("Or") || tmp[i].equals("OR")) formula += "||";
				else if(tmp[i].equals("xor") || tmp[i].equals("Xor") || tmp[i].equals("XOR")) formula += "^";
				else {
					formula += " true ";
					smarts_vec.add(tmp[i]);
				}
			}

			try {
				new ScriptEngineManager().getEngineByName("nashorn").eval(formula);
			} catch (ScriptException e) {
				this.worked = false;
				return;
			}
			
			SMARTSQueryTool smartsQuerytools = new SMARTSQueryTool("CC", DefaultChemObjectBuilder.getInstance());
			for(int i = 0; i < smarts_vec.size(); i++) {
				if(smarts_vec.get(i) == null) {
					this.worked = false;
					return;
				}
				String current_smarts = smarts_vec.get(i).trim();
				if(current_smarts.equals("")) {
					this.worked = false;
					return;
				}
				try {
					smartsQuerytools.setSmarts(current_smarts);
				} catch (CDKException e) {
					this.worked = false;
					return;
				}
			}
		}
	}
}
