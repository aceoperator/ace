/*
 * CallInfo.java
 *
 * Created on November 30, 2002, 5:52 PM
 */

package com.quikj.application.web.talk.feature.messagebox.server;

import com.quikj.server.app.EndPointInterface;

/**
 * 
 * @author bhm
 */
public class CallInfo {

	private long sessionId = -1;

	private EndPointInterface endpoint = null;

	private String mailboxAddress = null;

	private String fromAddress = null;

	private String mailboxUser = null;

	private boolean connected = false;

	private String encryptedKey = null;

	/** Creates a new instance of CallInfo */
	public CallInfo() {
	}

	public CallInfo(long session_id, EndPointInterface party,
			String mailbox_user) {
		sessionId = session_id;
		endpoint = party;
		mailboxUser = mailbox_user;
	}

	/**
	 * Getter for property encryptedKey.
	 * 
	 * @return Value of property encryptedKey.
	 * 
	 */
	public java.lang.String getEncryptedKey() {
		return encryptedKey;
	}

	/**
	 * Getter for property endpoint.
	 * 
	 * @return Value of property endpoint.
	 * 
	 */
	public com.quikj.server.app.EndPointInterface getEndpoint() {
		return endpoint;
	}

	/**
	 * Getter for property fromAddress.
	 * 
	 * @return Value of property fromAddress.
	 * 
	 */
	public java.lang.String getFromAddress() {
		return fromAddress;
	}

	/**
	 * Getter for property mailboxAddress.
	 * 
	 * @return Value of property mailboxAddress.
	 * 
	 */
	public java.lang.String getMailboxAddress() {
		return mailboxAddress;
	}

	/**
	 * Getter for property mailboxUser.
	 * 
	 * @return Value of property mailboxUser.
	 * 
	 */
	public java.lang.String getMailboxUser() {
		return mailboxUser;
	}

	/**
	 * Getter for property sessionId.
	 * 
	 * @return Value of property sessionId.
	 * 
	 */
	public long getSessionId() {
		return sessionId;
	}

	/**
	 * Getter for property connected.
	 * 
	 * @return Value of property connected.
	 * 
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * Setter for property connected.
	 * 
	 * @param connected
	 *            New value of property connected.
	 * 
	 */
	public void setConnected(boolean connected) {
		this.connected = connected;
	}
	/**
	 * Setter for property encryptedKey.
	 * 
	 * @param encryptedKey
	 *            New value of property encryptedKey.
	 * 
	 */
	public void setEncryptedKey(java.lang.String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}
	/**
	 * Setter for property endpoint.
	 * 
	 * @param endpoint
	 *            New value of property endpoint.
	 * 
	 */
	public void setEndpoint(com.quikj.server.app.EndPointInterface endpoint) {
		this.endpoint = endpoint;
	}
	/**
	 * Setter for property fromAddress.
	 * 
	 * @param fromAddress
	 *            New value of property fromAddress.
	 * 
	 */
	public void setFromAddress(java.lang.String fromAddress) {
		this.fromAddress = fromAddress;
	}
	/**
	 * Setter for property mailboxAddress.
	 * 
	 * @param mailboxAddress
	 *            New value of property mailboxAddress.
	 * 
	 */
	public void setMailboxAddress(java.lang.String mailboxAddress) {
		this.mailboxAddress = mailboxAddress;
	}
	/**
	 * Setter for property mailboxUser.
	 * 
	 * @param mailboxUser
	 *            New value of property mailboxUser.
	 * 
	 */
	public void setMailboxUser(java.lang.String mailboxUser) {
		this.mailboxUser = mailboxUser;
	}
	/**
	 * Setter for property sessionId.
	 * 
	 * @param sessionId
	 *            New value of property sessionId.
	 * 
	 */
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

}
