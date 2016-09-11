/**
 * 
 */
package com.quikj.ace.social;

import java.util.Date;

/**
 * @author amit
 *
 */
public class ChatFarEnd {
	public enum SessionStatus {
		INIITIATED,
		ESTABLISHING,
		CONNECTED,
		DISCONNECTED
	}
	
	private Date lastInteraction;
	private String userName;
	private String fullName;
	private SessionStatus sessionStatus = SessionStatus.INIITIATED;
	private String additionalInformation;
	
	public String getAdditionalInformation() {
		return additionalInformation;
	}
	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}
	public SessionStatus getSessionStatus() {
		return sessionStatus;
	}
	public void setSessionStatus(SessionStatus sessionStatus) {
		this.sessionStatus = sessionStatus;
	}
	public Date getLastInteraction() {
		return lastInteraction;
	}
	public void setLastInteraction(Date lastInteraction) {
		this.lastInteraction = lastInteraction;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}	
}
