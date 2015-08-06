/*
 * GroupWizardCannedMessageInfoForm.java
 *
 * Created on March 28, 2004, 10:28 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

/**
 * 
 * @author bhm
 */
public class GroupWizardCannedMessageForm extends ActionForm {

	private static final long serialVersionUID = -4130845290210364969L;

	private int counter = 0;

	private String content;

	private String description;

	private String group;

	private String submit;

	private ArrayList<LabelValueBean> userGroups = new ArrayList<LabelValueBean>();

	private String domain;

	public GroupWizardCannedMessageForm() {
	}

	public String getContent() {
		return this.content;
	}

	public int getCounter() {
		return this.counter;
	}

	public String getDescription() {
		return this.description;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getGroup() {
		return this.group;
	}

	public String getSubmit() {
		return this.submit;
	}

	public ArrayList<LabelValueBean> getUserGroups() {
		return this.userGroups;
	}

	public void setContent(String content) {
		this.content = content.trim();
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public void setDescription(String description) {
		this.description = description.trim();
	}

	public void setDomain(String domain) {
		this.domain = domain.trim();
	}

	public void setGroup(String group) {
		this.group = group.trim();
	}

	public void setSubmit(String submit) {
		this.submit = submit.trim();
	}

	public void setUserGroups(ArrayList<LabelValueBean> userGroups) {
		this.userGroups = userGroups;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		if ((submit.startsWith("Finished"))
				|| (submit.startsWith("Cancel"))) {
			return null;
		}

		// Check for mandatory data
		ActionErrors errors = new ActionErrors();
		try {
			if ((group == null) || (group.length() == 0)) {
				errors.add("group", new ActionError(
						"error.cannedmessage.no.group"));
			}

			if ((description == null) || (description.length() == 0)) {
				errors.add("description", new ActionError(
						"error.cannedmessage.no.description"));
			}

			if ((content == null) || (content.length() == 0)) {
				errors.add("content", new ActionError(
						"error.cannedmessage.no.content"));
			}

			if (!errors.isEmpty()) {
				ArrayList<LabelValueBean> list = GroupWizardCannedMessageAddAction
						.populateGroups(request, domain);
				userGroups = list;
			}
		} catch (Exception e) {
			errors.add("content", new ActionError("error.internal.error"));
		}
		return errors;
	}

}