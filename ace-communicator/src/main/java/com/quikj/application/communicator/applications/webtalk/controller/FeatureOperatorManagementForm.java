/*
 * FeatureManagementForm.java
 *
 * Form Bean for Operator feature
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class FeatureOperatorManagementForm extends ActionForm {

	private static final long serialVersionUID = 1769193518383874399L;

	private String name;

	private String submit = "Find";

	private boolean active = false;

	private int maxOperators;

	private int maxSessions;

	private int maxQueueSize;

	private String domain;

	private boolean displayWaitTimeEstimation;

	public FeatureOperatorManagementForm() {
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public int getMaxOperators() {
		return this.maxOperators;
	}

	public int getMaxQueueSize() {
		return this.maxQueueSize;
	}

	public int getMaxSessions() {
		return this.maxSessions;
	}

	public String getName() {
		return this.name;
	}

	public HashMap<String, String> getParamsList() {
		HashMap<String, String> map = new HashMap<String, String>(4);

		map.put("max-sessions", String.valueOf(getMaxSessions()));
		map.put("max-queue-size", String.valueOf(getMaxQueueSize()));
		map.put("max-operators", String.valueOf(getMaxOperators()));
		map.put("display-wait-time", String.valueOf(isDisplayWaitTimeEstimation()));
		return map;
	}

	public String getSubmit() {
		return this.submit;
	}

	public boolean isActive() {
		return active;
	}

	public void reset() {
		name = null;
		submit = "Find";
		active = false;

		maxOperators = 0;
		maxSessions = 0;
		maxQueueSize = 0;

		displayWaitTimeEstimation = false;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setMaxOperators(int maxOperators) {
		this.maxOperators = maxOperators;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	public void setMaxSessions(int maxSessions) {
		this.maxSessions = maxSessions;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setParamsList(HashMap<String, String> map) {
		String value;

		value = (String) map.get("max-sessions");
		if (value != null) {
			try {
				setMaxSessions(Integer.parseInt(value));
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementForm.setParamsList() -- Non-numeric parm encountered : max-sessions");
			}
		}

		value = (String) map.get("max-queue-size");
		if (value != null) {
			try {
				setMaxQueueSize(Integer.parseInt(value));
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementForm.setParamsList() -- Non-numeric parm encountered : max-queue-size");
			}
		}

		value = (String) map.get("max-operators");
		if (value != null) {
			try {
				setMaxOperators(Integer.parseInt(value));
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"FeatureOperatorManagementForm.setParamsList() -- Non-numeric parm encountered : max-operators");
			}
		}

		value = (String) map.get("display-wait-time");
		if (value != null) {
			setDisplayWaitTimeEstimation(Boolean.parseBoolean(value));
		}
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		if ((name == null) || (name.length() == 0)) {
			errors.add("name", new ActionError("error.feature.no.name"));
		}

		if (DataCheckUtility.followsTableIdRules(name) == false) {
			errors.add("name", new ActionError("error.feature.invalid.id"));
		}

		if (DataCheckUtility.followsBlankSpaceRules(name) == false) {
			errors.add("name", new ActionError("error.feature.invalid.id"));
		}

		// general checks for create/modify
		if (submit.equals("Create") || submit.equals("Modify")) {
			if (maxOperators <= 0) {
				errors.add("maxOperators", new ActionError("error.feature.operator.maxoperators"));
			}

			if (maxSessions <= 0) {
				errors.add("maxSessions", new ActionError("error.feature.operator.maxsessions"));
			}
		}

		return errors;
	}

	public boolean isDisplayWaitTimeEstimation() {
		return displayWaitTimeEstimation;
	}

	public void setDisplayWaitTimeEstimation(boolean displayWaitTimeEstimation) {
		this.displayWaitTimeEstimation = displayWaitTimeEstimation;
	}
}
