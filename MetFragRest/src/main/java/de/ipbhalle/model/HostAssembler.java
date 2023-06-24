package de.ipbhalle.model;

import org.springframework.hateoas.EntityModel;

public class HostAssembler extends RepresentationModelAssembler {

	private final String hostname;

	public HostAssembler(String name, String hostname) {
		super();
		this.hostname = hostname;
	}

	public String getHostname() {
		return hostname;
	}

	@Override
	public EntityModel<HostAssembler> toModel() {
		EntityModel<HostAssembler> resource = new EntityModel<HostAssembler>(this);
		return resource;
	}

}
