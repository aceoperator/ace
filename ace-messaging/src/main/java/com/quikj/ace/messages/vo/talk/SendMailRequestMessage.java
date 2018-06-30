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

	private boolean replyRequired = true;

	public SendMailRequestMessage() {
	}

	public MailElement getMailElement() {
		return mailElement;
	}

	public boolean isReplyRequired() {
		return this.replyRequired;
	}

	public void setMailElement(MailElement mailElement) {
		this.mailElement = mailElement;
	}

	public void setReplyRequired(boolean replyRequired) {
		this.replyRequired = replyRequired;
	}
}
