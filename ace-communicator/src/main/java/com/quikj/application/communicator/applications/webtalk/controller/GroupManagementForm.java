/*
 * GroupManagementForm.java
 *
 * Created on May 2, 2003, 11:09 AM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class GroupManagementForm extends ActionForm {

	/** Holds value of property name. */
	private String name;

	/** Holds value of property submit. */
	private String submit = "Find"; // didn't have to do this w/accountform -
									// why?

	/** Holds value of property memberLoginNotifyOwner. */
	private boolean memberLoginNotifyOwner;

	/** Holds value of property memberLoginNotifyMembers. */
	private boolean memberLoginNotifyMembers;

	/** Holds value of property memberCallCountNotifyOwner. */
	private boolean memberCallCountNotifyOwner;

	/** Holds value of property memberCallCountNotifyMembers. */
	private boolean memberCallCountNotifyMembers;

	/** Holds value of property ownerLoginNotifyMembers. */
	private boolean ownerLoginNotifyMembers;

	/** Holds value of property ownerCallCountNotifyMembers. */
	private boolean ownerCallCountNotifyMembers;

	/** Holds value of property domain. */
	private String domain;

	/** Creates a new instance of GroupManagementForm */
	public GroupManagementForm() {
	}

	/**
	 * Getter for property domain.
	 * 
	 * @return Value of property domain.
	 * 
	 */
	public String getDomain() {
		return this.domain;
	}

	/**
	 * Getter for property name.
	 * 
	 * @return Value of property name.
	 * 
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Getter for property submit.
	 * 
	 * @return Value of property submit.
	 * 
	 */
	public String getSubmit() {
		return this.submit;
	}

	/**
	 * Getter for property memberCallCountNotifyMembers.
	 * 
	 * @return Value of property memberCallCountNotifyMembers.
	 * 
	 */
	public boolean isMemberCallCountNotifyMembers() {
		return this.memberCallCountNotifyMembers;
	}

	/**
	 * Getter for property memberCallCountNotifyOwner.
	 * 
	 * @return Value of property memberCallCountNotifyOwner.
	 * 
	 */
	public boolean isMemberCallCountNotifyOwner() {
		return this.memberCallCountNotifyOwner;
	}

	/**
	 * Getter for property memberLoginNotifyMembers.
	 * 
	 * @return Value of property memberLoginNotifyMembers.
	 * 
	 */
	public boolean isMemberLoginNotifyMembers() {
		return this.memberLoginNotifyMembers;
	}

	/**
	 * Getter for property memberLoginNotifyOwner.
	 * 
	 * @return Value of property memberLoginNotifyOwner.
	 * 
	 */
	public boolean isMemberLoginNotifyOwner() {
		return this.memberLoginNotifyOwner;
	}

	/**
	 * Getter for property ownerCallCountNotifyMembers.
	 * 
	 * @return Value of property ownerCallCountNotifyMembers.
	 * 
	 */
	public boolean isOwnerCallCountNotifyMembers() {
		return this.ownerCallCountNotifyMembers;
	}

	/**
	 * Getter for property ownerLoginNotifyMembers.
	 * 
	 * @return Value of property ownerLoginNotifyMembers.
	 * 
	 */
	public boolean isOwnerLoginNotifyMembers() {
		return this.ownerLoginNotifyMembers;
	}

	public void reset() {
		memberCallCountNotifyMembers = false;
		memberCallCountNotifyOwner = false;
		memberLoginNotifyMembers = false;
		memberLoginNotifyOwner = false;
		ownerCallCountNotifyMembers = false;
		ownerLoginNotifyMembers = false;
		domain = null;
	}

	/**
	 * Setter for property domain.
	 * 
	 * @param domain
	 *            New value of property domain.
	 * 
	 */
	public void setDomain(String domain) {
		this.domain = domain.trim();
	}

	/**
	 * Setter for property memberCallCountNotifyMembers.
	 * 
	 * @param memberCallCountNotifyMembers
	 *            New value of property memberCallCountNotifyMembers.
	 * 
	 */
	public void setMemberCallCountNotifyMembers(
			boolean memberCallCountNotifyMembers) {
		this.memberCallCountNotifyMembers = memberCallCountNotifyMembers;
	}

	/**
	 * Setter for property memberCallCountNotifyOwner.
	 * 
	 * @param memberCallCountNotifyOwner
	 *            New value of property memberCallCountNotifyOwner.
	 * 
	 */
	public void setMemberCallCountNotifyOwner(boolean memberCallCountNotifyOwner) {
		this.memberCallCountNotifyOwner = memberCallCountNotifyOwner;
	}

	/**
	 * Setter for property memberLoginNotifyMembers.
	 * 
	 * @param memberLoginNotifyMembers
	 *            New value of property memberLoginNotifyMembers.
	 * 
	 */
	public void setMemberLoginNotifyMembers(boolean memberLoginNotifyMembers) {
		this.memberLoginNotifyMembers = memberLoginNotifyMembers;
	}

	/**
	 * Setter for property memberLoginNotifyOwner.
	 * 
	 * @param memberLoginNotifyOwner
	 *            New value of property memberLoginNotifyOwner.
	 * 
	 */
	public void setMemberLoginNotifyOwner(boolean memberLoginNotifyOwner) {
		this.memberLoginNotifyOwner = memberLoginNotifyOwner;
	}

	/**
	 * Setter for property name.
	 * 
	 * @param name
	 *            New value of property name.
	 * 
	 */
	public void setName(String name) {
		this.name = name.trim();
	}

	/**
	 * Setter for property ownerCallCountNotifyMembers.
	 * 
	 * @param ownerCallCountNotifyMembers
	 *            New value of property ownerCallCountNotifyMembers.
	 * 
	 */
	public void setOwnerCallCountNotifyMembers(
			boolean ownerCallCountNotifyMembers) {
		this.ownerCallCountNotifyMembers = ownerCallCountNotifyMembers;
	}

	/**
	 * Setter for property ownerLoginNotifyMembers.
	 * 
	 * @param ownerLoginNotifyMembers
	 *            New value of property ownerLoginNotifyMembers.
	 * 
	 */
	public void setOwnerLoginNotifyMembers(boolean ownerLoginNotifyMembers) {
		this.ownerLoginNotifyMembers = ownerLoginNotifyMembers;
	}

	/**
	 * Setter for property submit.
	 * 
	 * @param submit
	 *            New value of property submit.
	 * 
	 */
	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		if ((name == null) || (name.length() == 0)) {
			errors.add("name", new ActionError("error.group.no.name"));
		}

		if (DataCheckUtility.followsTableIdRules(name) == false) {
			errors.add("name", new ActionError("error.group.invalid.id"));
		}
		
		if (DataCheckUtility.followsBlankSpaceRules(name) == false) {
			errors.add("name", new ActionError("error.user.invalid.id"));
		}

		// create-specific checks
		if (submit.equals("Create") == true) {
			if (name.equals("all") == true) {
				errors.add("name", new ActionError("error.group.invalid.name"));
			}
		}

		// general checks for create/modify
		if ((submit.equals("Modify") == true)
				|| (submit.equals("Create") == true)) {
			// verify domain specified
			if ((domain == null) || (domain.length() == 0)) {
				errors.add("domain", new ActionError("error.group.no.domain"));
			}

		}

		return errors;
	}

}
