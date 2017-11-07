package de.ipbhalle.metfragrest;

public class FetchArguments {

	private String processid;
	private String format = "csv";
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if(processid != null) builder.append("processid=" + processid + "\n");
		if(format != null) builder.append("format=" + format + "\n");
		return builder.toString();
	}
	
	public String getProcessid() {
		return processid;
	}
	
	public void setFragmentpeakmatchabsolutemassdeviation(String processid) {
		this.processid = processid;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
}
