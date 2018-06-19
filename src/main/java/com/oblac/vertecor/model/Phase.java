package com.oblac.vertecor.model;

import java.util.List;

public class Phase {
	private Integer id;
	private String code;
	private Boolean active;
	private List<Integer> subphases;

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

	public Phase setSubphases(List<Integer> subphases) {
		this.subphases = subphases;
		return this;
	}

	public List<Integer> getSubphases() {
		return subphases;
	}

	@Override
	public String toString() {
		return code;
	}
}
