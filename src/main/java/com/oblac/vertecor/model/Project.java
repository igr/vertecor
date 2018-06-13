package com.oblac.vertecor.model;

import java.util.List;

public class Project {

	private Integer id;
	private String code;
	private String description;
	private Boolean active;
	private List<Integer> serviceTypes;
	private List<Integer> phases;

	public Integer getId() {
		return id;
	}

	public Project setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public Project setCode(String code) {
		this.code = code;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public Project setDescription(String description) {
		this.description = description;
		return this;
	}

	public List<Integer> getServiceTypes() {
		return serviceTypes;
	}

	public Project setServiceTypes(List<Integer> serviceTypes) {
		this.serviceTypes = serviceTypes;
		return this;
	}

	public Boolean isActive() {
		return active;
	}

	public Project setActive(Boolean active) {
		this.active = active;
		return this;
	}

	public List<Integer> getPhases() {
		return phases;
	}

	public Project setPhases(List<Integer> phases) {
		this.phases = phases;
		return this;
	}
}
