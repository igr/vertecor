package com.oblac.vertecor.model;

public class TimeEntry {

	private Project project;
	private Phase phase;
	private ServiceType serviceType;
	private String description;
	private int minutes;

	public Project getProject() {
		return project;
	}

	public TimeEntry setProject(Project project) {
		this.project = project;
		return this;
	}

	public Phase getPhase() {
		return phase;
	}

	public TimeEntry setPhase(Phase phase) {
		this.phase = phase;
		return this;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public TimeEntry setServiceType(ServiceType serviceType) {
		this.serviceType = serviceType;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public TimeEntry setDescription(String description) {
		this.description = description;
		return this;
	}

	public int getMinutes() {
		return minutes;
	}

	public TimeEntry setMinutes(int minutes) {
		this.minutes = minutes;
		return this;
	}
}
