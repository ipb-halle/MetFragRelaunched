package de.ipbhalle.model;

import org.springframework.hateoas.EntityModel;

public class StatusAssembler extends RepresentationModelAssembler {

	private final String status;

	public StatusAssembler(String name, String status) {
		super();
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	@Override
	public EntityModel<StatusAssembler> toModel() {
        return EntityModel.of(this);
	}

}
