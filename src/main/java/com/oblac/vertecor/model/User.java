package com.oblac.vertecor.model;

import java.util.List;

public class User {
	private final String userId;
	private final String username;
	private final String fullName;
	private final List<Integer> projectIds;

	public User(String userId, String username, String fullName, List<Integer> projectIds) {
		this.userId = userId;
		this.username = username;
		this.fullName = fullName;
		this.projectIds = projectIds;
	}

	public String getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getFullName() {
		return fullName;
	}

	public List<Integer> getProjectIds() {
		return projectIds;
	}
}
