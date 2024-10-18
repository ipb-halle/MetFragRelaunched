package de.ipbhalle.metfragweb.controller;

import jakarta.annotation.PostConstruct;
import jakarta.faces.bean.ManagedBean;
import jakarta.faces.bean.RequestScoped;
import jakarta.faces.context.FacesContext;

import de.ipbhalle.metfraglib.parameter.Constants;
import de.ipbhalle.metfraglib.parameter.VariableNames;
import de.ipbhalle.metfragweb.datatype.Parameter;

import jakarta.servlet.http.HttpServletRequest;

import org.primefaces.PrimeFaces;


@ManagedBean
@RequestScoped
public class MetFragLandingBean {
	
	private HttpServletRequest requestMap;
	private java.util.List<Parameter> parameters;
	private java.util.List<String> parameterNames;
	private boolean errorOccured;
	private String errorString;
	private java.util.List<String> allowedNames;
	private java.util.List<String> allowedDatabases;
	
	public MetFragLandingBean() {
		System.out.println("MetFragLandingBean");
		this.parameterNames = new java.util.ArrayList<String>();
		this.parameters = new java.util.ArrayList<Parameter>();
		this.errorOccured = false;
		this.errorString = "";
		this.allowedNames = new java.util.LinkedList<String>();
		this.allowedNames.add(VariableNames.ABSOLUTE_MASS_DEVIATION_NAME);
		this.allowedNames.add(VariableNames.RELATIVE_MASS_DEVIATION_NAME);
		this.allowedNames.add(VariableNames.DATABASE_RELATIVE_MASS_DEVIATION_NAME);
		this.allowedNames.add(VariableNames.PRECURSOR_DATABASE_IDS_NAME);
		this.allowedNames.add(VariableNames.PRECURSOR_ION_MASS_NAME);
		this.allowedNames.add(VariableNames.PRECURSOR_NEUTRAL_MASS_NAME);
		this.allowedNames.add(VariableNames.PRECURSOR_MOLECULAR_FORMULA_NAME);
		this.allowedNames.add(VariableNames.PRECURSOR_ION_MODE_NAME);
		this.allowedNames.add(VariableNames.PEAK_LIST_NAME);
		this.allowedNames.add(VariableNames.METFRAG_DATABASE_TYPE_NAME);
		this.allowedDatabases = new java.util.LinkedList<String>();
		this.allowedDatabases.add("KEGG");
		this.allowedDatabases.add("PubChem");
		this.allowedDatabases.add("ChemSpider");
		this.allowedDatabases.add("LipidMaps");
		this.allowedDatabases.add("MetaCyc");
		this.allowedDatabases.add("LocalInChI");
		this.allowedDatabases.add("LocalSDF");
	}
	
	@PostConstruct
	public void init() {
		this.requestMap = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		java.util.Iterator<?> it = this.requestMap.getParameterMap().keySet().iterator();
		while(it.hasNext()) {
			String key = (String)it.next();
			if(this.parameterNames.contains(key)) {
				this.errorString = "Error: " + key + " was found several times in URL.";
				this.errorOccured = true;
				return;
			}
			else {
				String[] tmp = (String[])this.requestMap.getParameterMap().get(key);
				if(tmp == null || tmp.length < 1) {
					this.errorString = "Error: Value for " + key + " not valid.";
					this.errorOccured = true;
					return;
				}
				if(tmp == null || tmp.length > 1) {
					this.errorString = "Error: " + key + " was found several times in URL.";
					this.errorOccured = true;
					return;
				}
				if(!this.allowedNames.contains(key)) {
					this.errorString = "Error: " + key + " not available. Allowed parameters are:";
					for(String name : this.allowedNames)
						this.errorString += " " + name + ",";
					this.errorString = this.errorString.substring(0, this.errorString.length() - 1);
			 		this.errorOccured = true;
					return;
				}
				if(key.equals(VariableNames.PRECURSOR_ION_MODE_NAME)) {
					try {
						int mode = Integer.parseInt(tmp[0]);
						if(mode != 1000 && mode != -1000 && !Constants.ADDUCT_NOMINAL_MASSES.contains(mode)) throw new Exception();
					}
					catch(Exception e) {
						this.errorString = "Error: Value " + tmp[0] + " of " + key + " is not valid.";
						this.errorOccured = true;
						return;
					}
				}
				if(key.equals(VariableNames.METFRAG_DATABASE_TYPE_NAME)) {
					if(!this.allowedDatabases.contains(tmp[0])) {
						this.errorString = "Error: Value " + tmp[0] + " of " + key + " is not valid.";
						this.errorOccured = true;
						return;
					}
				}
				if(key.equals(VariableNames.PEAK_LIST_NAME)) {
					tmp[0] = tmp[0].replaceAll(";", "\\\n").replaceAll("_", " ");
				}
				this.parameters.add(new Parameter(key, tmp[0]));
				this.parameterNames.add(key);
			}
		}
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("landingBean", this);
		FacesContext.getCurrentInstance().getExternalContext().getSessionMap().remove("metFragWebBean");
		/*
		 * redirect to MetFrag
		 */
		PrimeFaces.current().executeScript("window.setTimeout(function(){ window.location = \"index.xhtml\"; },3000)");		
	}

	public java.util.List<Parameter> getParameters() {
		return this.parameters;
	}
	
	public java.util.List<String> getParameterNames() {
		return this.parameterNames;
	}

	public void setParameterNames(java.util.List<String> parameterNames) {
		this.parameterNames = parameterNames;
	}

	public boolean isErrorOccured() {
		return errorOccured;
	}

	public void setErrorOccured(boolean errorOccured) {
		this.errorOccured = errorOccured;
	}

	public String getErrorString() {
		return errorString;
	}

	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
}
