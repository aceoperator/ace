package com.quikj.ace.messages.vo.talk;

import java.io.Serializable;

public class CallPartyElement implements Serializable {

	private static final long serialVersionUID = 2095212436967527632L;

	private String name = null;
	private String fullName = null;
	private String ipAddress = null;
	private String email = null;
	private String docbase = null;
	private String page = null;
	private String comment = null;
	private String language = null;
	private String environment = null;
	private String endUserCookie = null;
	private boolean cookiesEnabled;
	private String avatar;
	private boolean changePassword;
	private boolean privateInfo;

	public CallPartyElement() {
	}

	public CallPartyElement(String name, String fullName) {
		this.name = name;
		this.fullName = fullName;
	}

	public CallPartyElement(CallPartyElement callPartyElementToClone) {
		this(callPartyElementToClone.getName(), callPartyElementToClone
				.getFullName(), callPartyElementToClone.getIpAddress(),
				callPartyElementToClone.getEmail(), callPartyElementToClone
						.getDocumentBase(), callPartyElementToClone.getPage(),
				callPartyElementToClone.getComment(), callPartyElementToClone
						.getLanguage(), callPartyElementToClone
						.getEnvironment(), callPartyElementToClone
						.getEndUserCookie(), callPartyElementToClone
						.isCookiesEnabled(), callPartyElementToClone
						.getAvatar(), callPartyElementToClone
						.isChangePassword(), callPartyElementToClone
						.isPrivateInfo());
	}

	public CallPartyElement(String name, String fullName, String ipAddress,
			String email, String docbase, String page, String comment,
			String language, String environment, String endUserCookie,
			boolean cookiesEnabled, String avatar, boolean changePassword,
			boolean privateInfo) {
		this.name = name;
		this.fullName = fullName;
		this.ipAddress = ipAddress;
		this.email = email;
		this.docbase = docbase;
		this.page = page;
		this.comment = comment;
		this.language = language;
		this.environment = environment;
		this.endUserCookie = endUserCookie;
		this.cookiesEnabled = cookiesEnabled;
		this.avatar = avatar;
		this.changePassword = changePassword;
		this.privateInfo = privateInfo;
	}

	public String getComment() {
		return comment;
	}

	public String getDocumentBase() {
		return docbase;
	}

	public String getEmail() {
		return email;
	}

	public String getEnvironment() {
		return environment;
	}

	public String getFullName() {
		return fullName;
	}

	public String getHTMLPage() {
		return page;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getLanguage() {
		return language;
	}

	public String getName() {
		return name;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setDocumentBase(String docbase) {
		this.docbase = docbase;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public void setFullName(String full_name) {
		fullName = full_name;
	}

	public void setHTMLPage(String page) {
		this.page = page;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEndUserCookie() {
		return endUserCookie;
	}

	public void setEndUserCookie(String endUserCookie) {
		this.endUserCookie = endUserCookie;
	}

	public boolean isCookiesEnabled() {
		return cookiesEnabled;
	}

	public void setCookiesEnabled(boolean cookiesEnabled) {
		this.cookiesEnabled = cookiesEnabled;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public boolean isChangePassword() {
		return changePassword;
	}

	public void setChangePassword(boolean changePassword) {
		this.changePassword = changePassword;
	}

	public boolean isPrivateInfo() {
		return privateInfo;
	}

	public void setPrivateInfo(boolean privateInfo) {
		this.privateInfo = privateInfo;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
}
