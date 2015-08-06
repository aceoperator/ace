/*
 * SendMailRequestMessage.java
 *
 * Created on November 23, 2002, 3:45 PM
 */

package com.quikj.ace.messages.vo.talk;

/**
 * 
 * @author bhm
 */
public class SendMailRequestMessage implements TalkMessageInterface {
	
	private static final long serialVersionUID = 566825115996869447L;

	private MailElement mailElement = null;

	/** Holds value of property replyRequired. */
	private boolean replyRequired = true;

	/** Creates a new instance of SendMailRequestMessage */
	public SendMailRequestMessage() {
	}

	/**
	 * Getter for property mailElement.
	 * 
	 * @return Value of property mailElement.
	 */
	public MailElement getMailElement() {
		return mailElement;
	}

	/**
	 * Getter for property replyRequired.
	 * 
	 * @return Value of property replyRequired.
	 * 
	 */
	public boolean isReplyRequired() {
		return this.replyRequired;
	}

	/**
	 * Setter for property mailElement.
	 * 
	 * @param mailElement
	 *            New value of property mailElement.
	 */
	public void setMailElement(MailElement mailElement) {
		this.mailElement = mailElement;
	}

	/**
	 * Setter for property replyRequired.
	 * 
	 * @param replyRequired
	 *            New value of property replyRequired.
	 * 
	 */
	public void setReplyRequired(boolean replyRequired) {
		this.replyRequired = replyRequired;
	}

}
