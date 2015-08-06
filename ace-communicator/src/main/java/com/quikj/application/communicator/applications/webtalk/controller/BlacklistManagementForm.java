/**
 * 
 */
package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * @author amit
 * 
 */
public class BlacklistManagementForm extends ActionForm {

	private static final long serialVersionUID = -2101048664890356060L;

	private long id;

	private long userId;

	private int level = 0;

	private String identifier;
	
	private int type;

	private String lastModified;

	private String submit;

	private String userName;

	public BlacklistManagementForm() {
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		if (identifier != null) {
			identifier = identifier.trim();
		}
		this.identifier = identifier;
	}

	public String getLastModified() {
		return lastModified;
	}

	public void setLastModified(String lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (submit == null) {
			if (id == 0L && userName == null) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.id.and.username.missing"));
			}

			return errors;
		}

		if (submit.equals("Delete") || submit.equals("Modify")) {
			if (id == 0L) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.id.missing"));
			}
		}

		if (submit.equals("Create") || submit.equals("Modify")) {
			if (identifier == null || identifier.length() == 0) {
				errors.add(identifier, new ActionError(
						"error.blacklist.identifier.missing"));
			}

			if (level == 0) {
				errors.add("level", new ActionError(
						"error.blacklist.level.missing"));
			}
		}

		if (submit.equals("Create")) {
			if (userId == 0L) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.userid.missing"));
			}
		}

		return errors;
	}

	public String getSubmit() {
		return submit;
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
