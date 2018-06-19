package com.oblac.vertecor.model;

import jodd.util.StringUtil;

/**
 * TimeEntry related user input (from CLI).
 */
public class TimeEntryInput {

	private String signature = null;
	private String message = null;
	private String hours = null;
	private String date = null;
	Integer projectId = null;
	Integer phaseId = null;
	Integer serviceTypeId = null;

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
		if (signature != null) {
			String[] split = StringUtil.splitc(signature, ',');
			if (split.length >= 1) {
				projectId = Integer.parseInt(split[0].trim());
			}
			if (split.length >= 2) {
				phaseId = Integer.parseInt(split[1].trim());
			}
			if (split.length >= 3) {
				serviceTypeId = Integer.parseInt(split[2].trim());
			}
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getHours() {
		return hours;
	}

	public void setHours(String hours) {
		this.hours = hours;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Integer getProjectId() {
		return projectId;
	}

	public Integer getPhaseId() {
		return phaseId;
	}

	public Integer getServiceTypeId() {
		return serviceTypeId;
	}
}
