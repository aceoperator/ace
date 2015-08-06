/*
 * AceMailServiceMessage.java
 *
 * Created on November 16, 2002, 8:48 PM
 */

package com.quikj.server.framework;

/**
 * 
 * @author bhm
 */
public class AceMailServiceMessage implements AceMessageInterface {

	private AceMailMessage mailMessage = null;

	/** Creates a new instance of AceMailServiceMessage */
	public AceMailServiceMessage(AceMailMessage mailmsg) {
		mailMessage = mailmsg;
	}

	/**
	 * Getter for property mailMessage.
	 * 
	 * @return Value of property mailMessage.
	 */
	public AceMailMessage getMailMessage() {
		return mailMessage;
	}

	public String messageType() {
		return new String("AceMailServiceMessage");
	}

	/**
	 * Setter for property mailMessage.
	 * 
	 * @param mailMessage
	 *            New value of property mailMessage.
	 */
	public void setMailMessage(AceMailMessage mailMessage) {
		this.mailMessage = mailMessage;
	}

}
