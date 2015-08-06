package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class CannedMessageSearchForm extends ActionForm {

	private static final long serialVersionUID = 6839404321139537873L;

	private long id;

	private String sortBy;

	private String group;

	private String description;

	private String message;

	private ArrayList userGroups = new ArrayList();

	public CannedMessageSearchForm() {
		reset();
	}

	public String getDescription() {
		return this.description;
	}

	public String getGroup() {
		return this.group;
	}

	public long getId() {
		return this.id;
	}

	public String getMessage() {
		return this.message;
	}

	public String getSortBy() {
		return this.sortBy;
	}

	public ArrayList getUserGroups() {
		return this.userGroups;
	}

	public void reset() {
		id = 0L;
		group = null;
		description = null;
		message = null;
		sortBy = "Group";
	}

	public void setDescription(String description) {
		this.description = description.trim();
	}

	public void setGroup(String group) {
		this.group = group.trim();
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message.trim();
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public void setUserGroups(ArrayList userGroups) {
		this.userGroups = userGroups;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		return null;
	}
}
