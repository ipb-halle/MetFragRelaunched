package de.ipbhalle.model;

import org.springframework.hateoas.EntityModel;

public class ProcessAssembler extends RepresentationModelAssembler {

	private final String processid;

	public ProcessAssembler(String name, String processid) {
		super();
		this.processid = processid;
	}

	public String getProcessId() {
		return processid;
	}

	@Override
	public EntityModel<ProcessAssembler> toModel() {
        return EntityModel.of(this);
	}

}
