/*
 * ChangePasswordForm.java
 *
 * Created on May 9, 2003, 3:15 PM
 */

package com.quikj.application.communicator.admin.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class ChangePasswordForm extends ActionForm {

	/** Holds value of property oldPassword. */
	private String oldPassword;

	/** Holds value of property newPassword. */
	private String newPassword;

	/** Holds value of property newPasswordAgain. */
	private String newPasswordAgain;

	/** Creates a new instance of ChangePasswordForm */
	public ChangePasswordForm() {
	}

	/**
	 * Getter for property newPassword.
	 * 
	 * @return Value of property newPassword.
	 * 
	 */
	public String getNewPassword() {
		return this.newPassword;
	}

	/**
	 * Getter for property newPasswordAgain.
	 * 
	 * @return Value of property newPasswordAgain.
	 * 
	 */
	public String getNewPasswordAgain() {
		return this.newPasswordAgain;
	}

	/**
	 * Getter for property oldPassword.
	 * 
	 * @return Value of property oldPassword.
	 * 
	 */
	public String getOldPassword() {
		return this.oldPassword;
	}

	/**
	 * Setter for property newPassword.
	 * 
	 * @param newPassword
	 *            New value of property newPassword.
	 * 
	 */
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	/**
	 * Setter for property newPasswordAgain.
	 * 
	 * @param newPasswordAgain
	 *            New value of property newPasswordAgain.
	 * 
	 */
	public void setNewPasswordAgain(String newPasswordAgain) {
		this.newPasswordAgain = newPasswordAgain;
	}

	/**
	 * Setter for property oldPassword.
	 * 
	 * @param oldPassword
	 *            New value of property oldPassword.
	 * 
	 */
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword.trim();
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		if (oldPassword == null) {
			errors.add("oldPassword", new ActionError(
					"error.password.old.empty"));
		} else if (oldPassword.length() == 0) {
			errors.add("oldPassword", new ActionError(
					"error.password.old.empty"));
		}

		if (newPassword == null) {
			errors.add("newPassword", new ActionError(
					"error.password.new.empty"));
		} else if (newPassword.length() == 0) {
			errors.add("newPassword", new ActionError(
					"error.password.new.empty"));
		} else {
			AccountManagementForm.validatePassword(newPassword, errors,
					"newPassword");
		}

		if (newPasswordAgain == null) {
			errors.add("newPasswordAgain", new ActionError(
					"error.password.verify.empty"));
		} else if (newPasswordAgain.length() == 0) {
			errors.add("newPasswordAgain", new ActionError(
					"error.password.verify.empty"));
		}

		if ((newPassword != null) && (newPasswordAgain != null)) {
			if (newPassword.equals(newPasswordAgain) == false) {
				errors.add("newPassword", new ActionError(
						"error.password.mismatch"));
			}
		}

		return errors;
	}
}
