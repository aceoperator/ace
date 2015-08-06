package com.quikj.ace.messages.vo.talk;

public class RegistrationRequestMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = -4516762428225597250L;

	private String user = null;

	private String password = null;

	private String additionalInfo = null;

	private String language = null;

	public RegistrationRequestMessage() {
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * Getter for property language.
	 * 
	 * @return Value of property language.
	 */
	public java.lang.String getLanguage() {
		return language;
	}

	public String getPassword() {
		return password;
	}

	public String getUserName() {
		return user;
	}

	public void setAdditionalInfo(String additional_info) {
		additionalInfo = additional_info;
	}

	/**
	 * Setter for property language.
	 * 
	 * @param language
	 *            New value of property language.
	 */
	public void setLanguage(java.lang.String language) {
		this.language = language;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setUserName(String user) {
		this.user = user;
	}
}
