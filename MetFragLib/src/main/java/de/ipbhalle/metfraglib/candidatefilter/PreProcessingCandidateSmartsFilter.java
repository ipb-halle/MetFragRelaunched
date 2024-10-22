package de.ipbhalle.metfraglib.candidatefilter;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.smiles.smarts.SMARTSQueryTool;

import de.ipbhalle.metfraglib.interfaces.ICandidate;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfraglib.settings.Settings;

public class PreProcessingCandidateSmartsFilter extends AbstractPreProcessingCandidateFilter {
	
	private String formula;
	private SMARTSQueryTool[] smartsQuerytools;
	
	public PreProcessingCandidateSmartsFilter(Settings settings) {
		super(settings);
		java.util.ArrayList<String> smarts_vec = new java.util.ArrayList<String>();
		try {
			this.formula = "";
			String modified_formula = (String)settings.get(VariableNames.PRE_CANDIDATE_FILTER_SMARTS_FORMULA_NAME);
			String[] tmp = modified_formula.trim().split("\\s+");
			for(int i = 0; i < tmp.length; i++) {
				if(tmp[i].matches("\\(+") || tmp[i].matches("\\)+")) this.formula += tmp[i];
				else if(tmp[i].equals("not") || tmp[i].equals("Not") || tmp[i].equals("NOT")) this.formula += "!";
				else if(tmp[i].equals("and") || tmp[i].equals("And") || tmp[i].equals("AND")) this.formula += "&&";
				else if(tmp[i].equals("or") || tmp[i].equals("Or") || tmp[i].equals("OR")) this.formula += "||";
				else if(tmp[i].equals("xor") || tmp[i].equals("Xor") || tmp[i].equals("XOR")) this.formula += "^";
				else {
					this.formula += " " + smarts_vec.size() + " ";
					smarts_vec.add(tmp[i]);
				}
			}
		}
		catch(NullPointerException e) {
			smarts_vec = null;
		}
		catch(ClassCastException e) {
			smarts_vec = null;
		}
		if(smarts_vec != null && smarts_vec.size() != 0) {
			this.smartsQuerytools = new SMARTSQueryTool[smarts_vec.size()];
			for(int i = 0; i < smarts_vec.size(); i++) {
				smartsQuerytools[i] = new SMARTSQueryTool(smarts_vec.get(i), DefaultChemObjectBuilder.getInstance());
			}
		}
	}

	public boolean passesFilter(ICandidate candidate) {
		if(this.smartsQuerytools == null || this.smartsQuerytools.length == 0) return true;		
		
		String replacedFormula = new String(this.formula);
		for(int i = 0; i < this.smartsQuerytools.length; i++) {
			try {
				replacedFormula = replacedFormula.replaceFirst("\\s+" + i + "\\s+", " " + this.smartsQuerytools[i].matches(candidate.getAtomContainer()) + " ");
			} catch (CDKException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean result = false;
		try {
			result = (Boolean) new ScriptEngineManager().getEngineByName("nashorn").eval(replacedFormula);
		} catch (ScriptException e) {
			e.printStackTrace();
			return false;
		}
		return(result);
	}

	public void nullify() {
		this.formula = null;
		this.smartsQuerytools = null;
	}
	
}
