package de.ipbhalle.metfragrest;

public class StatusArguments {

	private String processid;
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(processid != null) builder.append("processid=" + processid + "\n");
		return builder.toString();
	}
	
	public String getProcessid() {
		return processid;
	}
	
	public void setFragmentpeakmatchabsolutemassdeviation(String processid) {
		this.processid = processid;
	}
	
}
