package com.oblac.vertecor.model;

public class Phase {
	private Integer id;
	private String code;
	private Boolean active;

	public Integer getId() {
		return id;
	}

	public Phase setId(Integer id) {
		this.id = id;
		return this;
	}

	public String getCode() {
		return code;
	}

	public Phase setCode(String code) {
		this.code = code;
		return this;
	}

	public Boolean isActive() {
		return active;
	}

	public Phase setActive(Boolean active) {
		this.active = active;
		return this;
	}
}
