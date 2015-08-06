/*
 * AccountManagementForm.java
 *
 * Created on May 2, 2003, 11:09 AM
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
public class AccountManagementForm extends ActionForm {

	private static final long serialVersionUID = 2111409108722781608L;

	private String name;

	private String password;

	private String verifyPassword;

	private String submit;

	private String additionalInfo;

	private static int MIN_PASSWORD_LENGTH = 4;

	public AccountManagementForm() {
		reset();
	}

	public static void validatePassword(String password, ActionErrors errors,
			String fieldname) {
		password = password.trim();
		if (password.length() == 0) {
			errors.add(fieldname, new ActionError(
					"error.account.password.hasblanks"));
		} else {
			int len = password.length();

			if (len < MIN_PASSWORD_LENGTH) {
				errors.add(fieldname, new ActionError(
						"error.account.password.tooshort", new Integer(
								MIN_PASSWORD_LENGTH)));
			}

			// it must have both letters and digits
			boolean letter = false;
			boolean digit = false;

			for (int i = 0; i < len; i++) {
				char c = password.charAt(i);
				if (Character.isDigit(c) == true) {
					digit = true;
				} else if (Character.isLetter(c) == true) {
					letter = true;
				} else if (Character.isSpaceChar(c) == true) {
					errors.add(fieldname, new ActionError(
							"error.account.password.hasblanks"));
				}
			}

			if ((letter == false) || (digit == false)) {
				errors.add(fieldname, new ActionError(
						"error.account.password.content"));
			}
		}
	}

	public String getAdditionalInfo() {
		return this.additionalInfo;
	}

	public String getName() {
		return this.name;
	}

	public String getPassword() {
		return this.password;
	}

	public String getSubmit() {
		return this.submit;
	}

	public String getVerifyPassword() {
		return this.verifyPassword;
	}

	public void reset() {
		password = null;
		verifyPassword = null;
		submit = "Find";
		additionalInfo = null;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo.trim();
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public void setVerifyPassword(String verifyPassword) {
		this.verifyPassword = verifyPassword;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if ((name == null) || (name.length() == 0)) {
			errors.add("name", new ActionError("error.account.no.name"));
		}

		// for create-specific options
		if (submit.equals("Create")) {
			if ((password == null) || (password.length() == 0)) {
				errors.add("password", new ActionError(
						"error.account.no.password"));
			} else {
				validatePassword(password, errors, "password");

				if (verifyPassword != null) {
					if (password.equals(verifyPassword) == false) {
						errors.add("password", new ActionError(
								"error.account.password.mismatch"));
					}
				} else {
					errors.add("password", new ActionError(
							"error.account.password.mismatch"));
				}
			}
		}

		// for modify-specific options
		if (submit.equals("Modify")) {
			if ((password != null) && (password.length() > 0)) {
				validatePassword(password, errors, "password");

				if (verifyPassword != null) {
					if (password.equals(verifyPassword) == false) {
						errors.add("password", new ActionError(
								"error.account.password.mismatch"));
					}
				} else {
					errors.add("password", new ActionError(
							"error.account.password.mismatch"));
				}
			}
		}

		return errors;
	}
}
