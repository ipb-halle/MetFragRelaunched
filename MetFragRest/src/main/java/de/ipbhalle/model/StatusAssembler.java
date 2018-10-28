package de.ipbhalle.model;

import org.springframework.hateoas.Resource;

public class StatusAssembler extends ResourceAssembler {

	private final String status;
	
	public StatusAssembler(String name, String status) {
		super(name);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public Resource<StatusAssembler> toResource() {
		Resource<StatusAssembler> resource = new Resource<StatusAssembler>(this);
		return resource;
	}
	
}
