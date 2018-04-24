package de.ipbhalle.metfragweb.datatype;

import java.io.Serializable;

public class AdditionalFileDatabase implements Serializable {

	private String filename;
	private String type;
	private String label;
    private static final long serialVersionUID = 1L;
    private Long id;
    
	public AdditionalFileDatabase(Long id, String filename, String type, String label) {
		this.filename = filename;
		this.type = type;
		this.label = label;
		this.id = id;
	}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
	    return this.filename;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof AdditionalFileDatabase) {
			return ((AdditionalFileDatabase)obj).getFilename().equals(this.filename);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.filename.hashCode();
	}
	
}
