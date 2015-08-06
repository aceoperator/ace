/*
 * ReplaceSessionMessage.java
 *
 * Created on April 13, 2002, 5:24 PM
 */

package com.quikj.ace.messages.vo.talk;

/**
 * 
 * @author amit
 */
public class ReplaceSessionMessage implements TalkMessageInterface {

	private static final long serialVersionUID = -7048082457080992804L;

	private long oldSessionId = -1;

	private long newSessionId = -1;

	private String encryptedKey = null;

	/** Creates a new instance of ReplaceSessionMessage */
	public ReplaceSessionMessage() {
	}

	/**
	 * Getter for property encryptedKey.
	 * 
	 * @return Value of property encryptedKey.
	 */
	public java.lang.String getEncryptedKey() {
		return encryptedKey;
	}

	/**
	 * Getter for property newSessionId.
	 * 
	 * @return Value of property newSessionId.
	 */
	public long getNewSessionId() {
		return newSessionId;
	}

	/**
	 * Getter for property oldSessionId.
	 * 
	 * @return Value of property oldSessionId.
	 */
	public long getOldSessionId() {
		return oldSessionId;
	}

	/**
	 * Setter for property encryptedKey.
	 * 
	 * @param encryptedKey
	 *            New value of property encryptedKey.
	 */
	public void setEncryptedKey(java.lang.String encryptedKey) {
		this.encryptedKey = encryptedKey;
	}

	/**
	 * Setter for property newSessionId.
	 * 
	 * @param newSessionId
	 *            New value of property newSessionId.
	 */
	public void setNewSessionId(long newSessionId) {
		this.newSessionId = newSessionId;
	}

	/**
	 * Setter for property oldSessionId.
	 * 
	 * @param oldSessionId
	 *            New value of property oldSessionId.
	 */
	public void setOldSessionId(long oldSessionId) {
		this.oldSessionId = oldSessionId;
	}
}
