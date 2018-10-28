package de.ipbhalle.model;

import org.springframework.hateoas.Resource;

public class ProcessAssembler extends ResourceAssembler {

	private final String processid;
	
	public ProcessAssembler(String name, String processid) {
		super(name);
		this.processid = processid;
	}

	public String getProcessId() {
		return processid;
	}

	@Override
	public Resource<ProcessAssembler> toResource() {
		Resource<ProcessAssembler> resource = new Resource<ProcessAssembler>(this);
		return resource;
	}
	
}
