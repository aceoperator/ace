/*
 * UserSearchForm.java
 *
 */

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
public class UserSearchForm extends ActionForm {

	private static final long serialVersionUID = 8524903017688102967L;

	private String name;

	private String additionalInfo;

	private Object[] belongsToGroups;

	private Object[] ownsGroups;

	private String fullName;

	private String address;

	private String avatar;

	private String domain;

	private String unavailXferTo;

	private ArrayList userGroups = new ArrayList();

	private String gatekeeper;

	private String locked;

	private String changePassword;

	public UserSearchForm() {
		reset();
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

	public String getUnavailXferTo() {
		return this.unavailXferTo;
	}

	public ArrayList getUserGroups() {
		return this.userGroups;
	}

	public void reset() {
		belongsToGroups = null;
		ownsGroups = null;
		name = null;
		additionalInfo = null;
		fullName = null;
		address = null;
		unavailXferTo = null;
		gatekeeper = null;
		domain = null;
		avatar = null;
		locked = null;
		changePassword = null;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo.trim();
	}

	public void setAddress(String address) {
		this.address = address.trim();
	}

	public void setBelongsToGroups(Object[] belongsToGroups) {
		this.belongsToGroups = belongsToGroups;
	}
	
	public void setFullName(String fullName) {
		this.fullName = fullName.trim();
	}

	public void setGatekeeper(String gatekeeper) {
		this.gatekeeper = gatekeeper;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setOwnsGroups(Object[] ownsGroups) {
		this.ownsGroups = ownsGroups;
	}

	public void setUnavailXferTo(String unavailXferTo) {
		this.unavailXferTo = unavailXferTo.trim();
	}

	public void setUserGroups(ArrayList userGroups) {
		this.userGroups = userGroups;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getLocked() {
		return locked;
	}

	public void setLocked(String locked) {
		this.locked = locked;
	}

	public String getChangePassword() {
		return changePassword;
	}

	public void setChangePassword(String changePassword) {
		this.changePassword = changePassword;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		return null;
	}
}
