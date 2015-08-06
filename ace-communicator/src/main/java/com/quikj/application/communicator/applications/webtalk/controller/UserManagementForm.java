/*
 * UserManagementForm.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author bhm
 */
public class UserManagementForm extends ActionForm {

	private static final long serialVersionUID = -7559747885009107717L;

	private static final int MIN_PASSWORD_LENGTH = 4;

	private String name;

	private String password;

	private String verifyPassword;

	private String submit;

	private String additionalInfo;

	private Object[] belongsToGroups;

	private Object[] ownsGroups;

	private String fullName;

	private String address;

	private String unavailXferTo;

	private String avatar;

	private ArrayList<LabelValueBean> userGroups = new ArrayList<LabelValueBean>();

	private String gatekeeper;

	private String flags;

	private boolean locked;

	private boolean changePassword;

	private String securityQuestion1;

	private String securityAnswer1;

	private String securityQuestion2;

	private String securityAnswer2;

	private String securityQuestion3;

	private String securityAnswer3;

	private String domain;

	private boolean privateInfo;

	public UserManagementForm() {
		reset();
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = trim(domain);
	}

	public static void validatePassword(String password, ActionErrors errors) {
		password = password.trim();
		if (password.length() == 0) {
			errors.add("password", new ActionError(
					"error.user.password.hasblanks"));
		} else {
			int len = password.length();

			if (len < MIN_PASSWORD_LENGTH) {
				errors.add("password", new ActionError(
						"error.user.password.tooshort", new Integer(
								MIN_PASSWORD_LENGTH)));
			}

			// it must have both letters and digits
			boolean letter = false;
			boolean digit = false;

			for (int i = 0; i < len; i++) {
				char c = password.charAt(i);
				if (Character.isDigit(c) == true) {
					digit = true;
				} else if (Character.isLetter(c)) {
					letter = true;
				} else if (Character.isSpaceChar(c)) {
					errors.add("password", new ActionError(
							"error.user.password.hasblanks"));
				}
			}

			if (!letter || !digit) {
				errors.add("password", new ActionError(
						"error.user.password.content"));
			}
		}
	}

	public String getAdditionalInfo() {
		return this.additionalInfo;
	}

	public String getAddress() {
		return this.address;
	}

	public Object[] getBelongsToGroups() {
		return this.belongsToGroups;
	}

	public String getFullName() {
		return this.fullName;
	}

	public String getGatekeeper() {
		return this.gatekeeper;
	}

	public String getName() {
		return this.name;
	}

	public Object[] getOwnsGroups() {
		return this.ownsGroups;
	}

	public String getPassword() {
		return this.password;
	}

	public String getSubmit() {
		return this.submit;
	}

	public String getUnavailXferTo() {
		return this.unavailXferTo;
	}

	public ArrayList<LabelValueBean> getUserGroups() {
		return userGroups;
	}

	public String getVerifyPassword() {
		return this.verifyPassword;
	}

	public void reset() {
		belongsToGroups = null;
		ownsGroups = null;
		name = null;
		password = null;
		verifyPassword = null;
		submit = "Find";
		additionalInfo = null;
		fullName = null;
		address = null;
		unavailXferTo = null;
		gatekeeper = null;
		domain = null;
		avatar = null;
		flags = null;
		locked = false;
		privateInfo = false;
		clearSecurityQuestions();
	}

	private String trim(String string) {
		if (string != null) {
			string = string.trim();
		}

		return string;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = trim(additionalInfo);
	}

	public void setAddress(String address) {
		this.address = trim(address);
	}

	public void setBelongsToGroups(Object[] belongsToGroups) {
		this.belongsToGroups = belongsToGroups;
	}

	public void setFullName(String fullName) {
		this.fullName = trim(fullName);
	}

	public void setGatekeeper(String gatekeeper) {
		this.gatekeeper = trim(gatekeeper);
	}

	public void setName(String name) {
		this.name = trim(name);
	}

	public void setOwnsGroups(Object[] ownsGroups) {
		this.ownsGroups = ownsGroups;
	}

	public void setPassword(String password) {
		this.password = trim(password);
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public void setUnavailXferTo(String unavailXferTo) {
		this.unavailXferTo = trim(unavailXferTo);
	}

	public void setUserGroups(ArrayList<LabelValueBean> userGroups) {
		this.userGroups = userGroups;
	}

	public void setVerifyPassword(String verifyPassword) {
		this.verifyPassword = trim(verifyPassword);
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		if (submit.startsWith("Finished") || submit.startsWith("Cancel")) {
			return null;
		}

		ActionErrors errors = new ActionErrors();

		if (flags != null && flags.trim().length() > 0) {
			try {
				Integer.decode(flags.trim());
			} catch (NumberFormatException e) {
				errors.add("flags", new ActionError("error.user.flags.invalid"));
			}
		}

		// Check for mandatory data
		try {
			if ((name == null) || (name.length() == 0)) {
				errors.add("name", new ActionError("error.user.no.name"));
			}

			if (DataCheckUtility.followsTableIdRules(name) == false) {
				errors.add("name", new ActionError("error.user.invalid.id"));
			}

			if (DataCheckUtility.followsBlankSpaceRules(name) == false) {
				errors.add("name", new ActionError("error.user.invalid.id"));
			}

			// for create-specific options
			if (submit.startsWith("Create")) {
				if ((password == null) || (password.length() == 0)) {
					errors.add("password", new ActionError(
							"error.user.no.password"));
				} else {
					validatePassword(password, errors);

					if (verifyPassword != null) {
						if (password.equals(verifyPassword) == false) {
							errors.add("password", new ActionError(
									"error.user.password.mismatch"));
						}
					} else {
						errors.add("password", new ActionError(
								"error.user.password.mismatch"));
					}
				}
			}

			// for modify-specific options
			if (submit.equals("Modify")) {
				if ((password != null) && (password.length() > 0)) {
					validatePassword(password, errors);

					if (verifyPassword != null) {
						if (password.equals(verifyPassword) == false) {
							errors.add("password", new ActionError(
									"error.user.password.mismatch"));
						}
					} else {
						errors.add("password", new ActionError(
								"error.user.password.mismatch"));
					}
				}
			}

			// general checks for create/modify
			if (submit.startsWith("Create") || submit.equals("Modify")) {
				// check owned vs. member groups, no overlap
				if (ownsGroups != null) {
					if (belongsToGroups != null) {
						for (int i = 0; i < ownsGroups.length; i++) {
							String name = ownsGroups[i].toString();

							for (int j = 0; j < belongsToGroups.length; j++) {
								if (belongsToGroups[j].toString().equals(name) == true) {
									errors.add(
											"belongsToGroups",
											new ActionError(
													"error.user.groups.illegal"));
									break;
								}
							}
						}
					}
				}

				// check unavailable xfer-to != this user name
				if (unavailXferTo != null) {
					if (unavailXferTo.equals(name)) {
						errors.add("unavailXferTo", new ActionError(
								"error.user.unavail.invalid"));
					}
				}

				// check security questions - if question entered or modified,
				// answer must be entered
				if (securityQuestion1 != null
						&& securityQuestion1.trim().length() > 0) {
					if (securityAnswer1 == null
							|| securityAnswer1.trim().length() == 0) {
						errors.add("securityQuestion1", new ActionError(
								"error.user.security.answer.missing", 1));
					}
				}

				if (securityQuestion2 != null
						&& securityQuestion2.trim().length() > 0) {
					if (securityAnswer2 == null
							|| securityAnswer2.trim().length() == 0) {
						errors.add("securityQuestion2", new ActionError(
								"error.user.security.answer.missing", 2));
					}
				}

				if (securityQuestion3 != null
						&& securityQuestion3.trim().length() > 0) {
					if (securityAnswer3 == null
							|| securityAnswer3.trim().length() == 0) {
						errors.add("securityQuestion3", new ActionError(
								"error.user.security.answer.missing", 3));
					}
				}
			}

			if (!errors.isEmpty()) {
				GroupBean groupBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), GroupBean.class);
				ArrayList<LabelValueBean> list = GroupManagementAction
						.populateGroups(groupBean, null);
				userGroups = list;
			}
		} catch (Exception e) {
			errors.add("unavailXferTo", new ActionError("error.internal.error"));
		}
		return errors;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = trim(flags);
	}

	public String getSecurityQuestion1() {
		return securityQuestion1;
	}

	public void setSecurityQuestion1(String securityQuestion1) {
		this.securityQuestion1 = trim(securityQuestion1);
	}

	public String getSecurityAnswer1() {
		return securityAnswer1;
	}

	public void setSecurityAnswer1(String securityAnswer1) {
		this.securityAnswer1 = trim(securityAnswer1);
	}

	public String getSecurityQuestion2() {
		return securityQuestion2;
	}

	public void setSecurityQuestion2(String securityQuestion2) {
		this.securityQuestion2 = trim(securityQuestion2);
	}

	public String getSecurityAnswer2() {
		return securityAnswer2;
	}

	public void setSecurityAnswer2(String securityAnswer2) {
		this.securityAnswer2 = trim(securityAnswer2);
	}

	public String getSecurityQuestion3() {
		return securityQuestion3;
	}

	public void setSecurityQuestion3(String securityQuestion3) {
		this.securityQuestion3 = trim(securityQuestion3);
	}

	public String getSecurityAnswer3() {
		return securityAnswer3;
	}

	public void setSecurityAnswer3(String securityAnswer3) {
		this.securityAnswer3 = trim(securityAnswer3);
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = trim(avatar);
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
	}

	public void clearSecurityQuestions() {
		securityQuestion1 = null;
		securityQuestion2 = null;
		securityQuestion3 = null;
		securityAnswer1 = null;
		securityAnswer2 = null;
		securityAnswer3 = null;
	}

	public boolean isPrivateInfo() {
		return privateInfo;
	}

	public void setPrivateInfo(boolean privateInfo) {
		this.privateInfo = privateInfo;
	}
}