package de.ipbhalle.model;

import org.springframework.hateoas.Resource;

public abstract class ResourceAssembler {

	private final String name;
	
	public ResourceAssembler(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	public abstract Resource<?> toResource();
	
}
