/*
 * MailMessage.java
 *
 * Created on November 16, 2002, 10:13 PM
 */

package com.quikj.server.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author bhm
 */
public class AceMailMessage implements Serializable {
	private static final long serialVersionUID = 9025011992787835947L;
	private String from = null;
	private List<String> to = new ArrayList<>();
	private List<String> cc = new ArrayList<>();
	private List<String> bcc = new ArrayList<>();
	private String subject = null;
	private List<String> replyTo = new ArrayList<>();
	private String body = null;
	private String subType = "plain";

	public void addBcc(String bcc) {
		this.bcc.add(bcc);
	}

	public void addCc(String cc) {
		this.cc.add(cc);
	}

	public void addReplyTo(String replyTo) {
		this.replyTo.add(replyTo);
	}

	public void addTo(String to) {
		this.to.add(to);
	}

	public List<String> getBcc() {
		return bcc;
	}

	public String getBody() {
		return body;
	}

	public List<String> getCc() {
		return cc;
	}

	public String getFrom() {
		return from;
	}

	public List<String> getReplyTo() {
		return replyTo;
	}

	public String getSubject() {
		return subject;
	}

	public List<String> getTo() {
		return to;
	}

	public int numBcc() {
		return bcc.size();
	}

	public int numCc() {
		return cc.size();
	}

	public int numReplyTo() {
		return replyTo.size();
	}

	public int numTo() {
		return to.size();
	}

	public void setBcc(List<String> bcc) {
		this.bcc = bcc;
	}

	public void setBody(java.lang.String body) {
		this.body = body;
	}

	public void setCc(List<String> cc) {
		this.cc = cc;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setReplyTo(List<String> replyTo) {
		this.replyTo = replyTo;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setTo(List<String> to) {
		this.to = to;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
}
