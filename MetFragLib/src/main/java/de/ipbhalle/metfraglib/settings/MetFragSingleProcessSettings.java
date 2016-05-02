package de.ipbhalle.metfraglib.settings;

public class MetFragSingleProcessSettings extends Settings {

	protected Settings includedSettings;
	
	public MetFragSingleProcessSettings(Settings includedSettings) {
		super();
		this.includedSettings = includedSettings;
	}
	
	public boolean containsKey(String variableName) {
		return super.containsKey(variableName) || this.includedSettings.containsKey(variableName);
	}
	
	public Object get(String variableName) {
		Object obj = super.get(variableName);
		if(obj == null) obj = this.includedSettings.get(variableName);
		return obj;
	}
}
