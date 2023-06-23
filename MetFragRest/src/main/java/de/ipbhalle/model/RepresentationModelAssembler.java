package de.ipbhalle.model;

import org.springframework.hateoas.EntityModel;

public abstract class RepresentationModelAssembler {

	private String name;

	public void ResourceAssembler(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract EntityModel<?> toModel();

}
