package com.oblac.vertecor.model;

public class ServiceType {
	private Integer id;
	private String code;
	private Boolean active;

	public Integer getId() {
		return id;
	}

	public ServiceType setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public ServiceType setCode(String code) {
		this.code = code;
		return this;
	}

	public Boolean isActive() {
		return active;
	}

	public ServiceType setActive(Boolean active) {
		this.active = active;
		return this;
	}
}
