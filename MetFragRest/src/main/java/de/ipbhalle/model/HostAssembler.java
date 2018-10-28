package de.ipbhalle.model;

import org.springframework.hateoas.Resource;

public class HostAssembler extends ResourceAssembler {

	private final String hostname;
	
	public HostAssembler(String name, String hostname) {
		super(name);
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	@Override
	public Resource<HostAssembler> toResource() {
		Resource<HostAssembler> resource = new Resource<HostAssembler>(this);
		return resource;
	}
	
}
