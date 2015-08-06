package com.quikj.ace.messages.vo.talk;

import java.util.Vector;

/**
 * 
 * @author bhm
 */
public class MailElement implements TalkMessageInterface {
	private static final long serialVersionUID = -5429162754197702119L;
	
	private String from = null;
	private StringListElement to = new StringListElement();
	private StringListElement cc = new StringListElement();
	private StringListElement bcc = new StringListElement();
	private StringListElement replyTo = new StringListElement();
	private String subject = null;
	private String textBody = null;
	private String subype = "plain";

	/** Creates a new instance of MailElement */
	public MailElement() {
	}

	public void addBcc(String bcc) {
		(this.bcc).addElement(new String(bcc));
	}

	public void addCc(String cc) {
		(this.cc).addElement(new String(cc));
	}

	public void addReplyTo(String reply_to) {
		(this.replyTo).addElement(new String(reply_to));
	}

	public void addTo(String to) {
		(this.to).addElement(new String(to));
	}

	/**
	 * Getter for property bcc.
	 * 
	 * @return Value of property bcc.
	 */
	public Vector<String> getBcc() {
		return bcc.getElements();
	}

	public String getBccAt(int index) {
		return bcc.elementAt(index);
	}

	/**
	 * Getter for property textBody.
	 * 
	 * @return Value of property textBody.
	 */
	public java.lang.String getBody() {
		return textBody;
	}

	/**
	 * Getter for property cc.
	 * 
	 * @return Value of property cc.
	 */
	public Vector<String> getCc() {
		return cc.getElements();
	}

	public String getCcAt(int index) {
		return cc.elementAt(index);
	}

	/**
	 * Getter for property from.
	 * 
	 * @return Value of property from.
	 */
	public java.lang.String getFrom() {
		return from;
	}

	/**
	 * Getter for property replyTo.
	 * 
	 * @return Value of property replyTo.
	 */
	public Vector<String> getReplyTo() {
		return replyTo.getElements();
	}

	public String getReplyToAt(int index) {
		return replyTo.elementAt(index);
	}

	/**
	 * Getter for property subject.
	 * 
	 * @return Value of property subject.
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Getter for property to.
	 * 
	 * @return Value of property to.
	 */
	public Vector<String> getTo() {
		return to.getElements();
	}

	public String getToAt(int index) {
		return to.elementAt(index);
	}

	public int numBcc() {
		return bcc.numElements();
	}

	public int numCc() {
		return cc.numElements();
	}

	public int numReplyTo() {
		return replyTo.numElements();
	}

	public int numTo() {
		return to.numElements();
	}

	/**
	 * Setter for property bcc.
	 * 
	 * @param bcc
	 *            New value of property bcc.
	 */
	public void setBcc(Vector<String> bcc) {
		this.bcc.setElements(bcc);
	}

	/**
	 * Setter for property textBody.
	 * 
	 * @param textBody
	 *            New value of property textBody.
	 */
	public void setBody(java.lang.String body) {
		this.textBody = body;
	}

	/**
	 * Setter for property cc.
	 * 
	 * @param cc
	 *            New value of property cc.
	 */
	public void setCc(Vector<String> cc) {
		(this.cc).setElements(cc);
	}

	/**
	 * Setter for property from.
	 * 
	 * @param from
	 *            New value of property from.
	 */
	public void setFrom(java.lang.String from) {
		this.from = from;
	}

	/**
	 * Setter for property replyTo.
	 * 
	 * @param replyTo
	 *            New value of property replyTo.
	 */
	public void setReplyTo(Vector<String> replyTo) {
		(this.replyTo).setElements(replyTo);
	}

	/**
	 * Setter for property subject.
	 * 
	 * @param subject
	 *            New value of property subject.
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Setter for property to.
	 * 
	 * @param to
	 *            New value of property to.
	 */
	public void setTo(Vector<String> to) {
		(this.to).setElements(to);
	}

	public String getSubype() {
		return subype;
	}

	public void setSubype(String subype) {
		this.subype = subype;
	}
}
