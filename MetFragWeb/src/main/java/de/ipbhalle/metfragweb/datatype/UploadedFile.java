package de.ipbhalle.metfragweb.datatype;

public class UploadedFile {

	protected String name;
	protected String size;
	protected String contentType;
	protected String info;
	protected String absPath;
	protected int id;
	
	public UploadedFile(String name, String size, String contentType, String info, String absPath, int id) {
		 this.name = name;
         this.size = size;
         this.contentType = contentType;
         this.info = info;
         this.absPath = absPath;
         this.id = id;
	}

    public String getName() { 
    	return name; 
    }
    
    public String getSize() { return size; }
    public String getContentType() { return contentType; }
    public String getInfo() { return info; }
    public String getAbsolutePath() { return absPath; }
    public int getId() {
    	return this.id;
    }
    
    @Override
    public boolean equals(Object uploadedFile) {
    	return this.name.equals(((UploadedFile)uploadedFile).getName());
    }
}
