package com.quikj.ace.messages.vo.talk;

import java.util.List;

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
	private String subType = "plain";

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

	public List<String> getBcc() {
		return bcc.getElements();
	}

	public String getBccAt(int index) {
		return bcc.elementAt(index);
	}

	public String getBody() {
		return textBody;
	}

	public List<String> getCc() {
		return cc.getElements();
	}

	public String getCcAt(int index) {
		return cc.elementAt(index);
	}

	public java.lang.String getFrom() {
		return from;
	}

	public List<String> getReplyTo() {
		return replyTo.getElements();
	}

	public String getReplyToAt(int index) {
		return replyTo.elementAt(index);
	}

	public String getSubject() {
		return subject;
	}

	public List<String> getTo() {
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

	public void setBcc(List<String> bcc) {
		this.bcc.setElements(bcc);
	}

	public void setBody(String body) {
		this.textBody = body;
	}

	public void setCc(List<String> cc) {
		(this.cc).setElements(cc);
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setReplyTo(List<String> replyTo) {
		(this.replyTo).setElements(replyTo);
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setTo(List<String> to) {
		(this.to).setElements(to);
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
}
