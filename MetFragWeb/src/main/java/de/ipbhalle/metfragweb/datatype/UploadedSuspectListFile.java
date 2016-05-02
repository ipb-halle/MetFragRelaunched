package de.ipbhalle.metfragweb.datatype;

public class UploadedSuspectListFile extends UploadedFile {

	protected int numberEntries;

	public UploadedSuspectListFile(String name, String size, String contentType, String info, String absPath, int id) {
		super(name, size, contentType, info, absPath, id);
	}
	
	public UploadedSuspectListFile(String name, String size, String contentType, String info, String absPath, int id, int numberEntries) {
		super(name, size, contentType, info, absPath, id);
		this.numberEntries = numberEntries;
	}

	public int getNumberEntries() {
		return numberEntries;
	}

	public void setNumberEntries(int numberEntries) {
		this.numberEntries = numberEntries;
	}
	
}
