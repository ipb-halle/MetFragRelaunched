package de.ipbhalle.metfragweb.datatype;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.application.Resource;
import jakarta.faces.context.FacesContext;

public class DownloadResource extends Resource implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3508891145640705734L;
	private String path = "";
    private HashMap<String, String> headers;
    private byte[] bytes;
    
	public DownloadResource(byte[] bytes) {
		this.bytes = bytes;
        this.headers = new HashMap<String, String>();
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return new ByteArrayInputStream(this.bytes);
	}

	@Override
	public Map<String, String> getResponseHeaders() {
		return this.headers;
	}

	@Override
	public String getRequestPath() {
		return this.path;
	}

	@Override
	public URL getURL() {
		return null;
	}

	@Override
	public boolean userAgentNeedsUpdate(FacesContext context) {
		return false;
	}

}
