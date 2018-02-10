/*
 * MailMessage.java
 *
 * Created on November 16, 2002, 10:13 PM
 */

package com.quikj.server.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

	private String errorMessage = "";

	/** Creates a new instance of MailMessage */
	public AceMailMessage() {
	}

	public static boolean addressValid(String addr) {
		// check for domain presence
		StringTokenizer tok = new StringTokenizer(addr, "@");
		if (tok.countTokens() < 2) {
			return false;
		}

		tok.nextToken();
		String domain = tok.nextToken();

		StringTokenizer tok2 = new StringTokenizer(domain, ".");
		if (tok2.countTokens() < 2) {
			return false;
		}

		return true;
	}

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

	public String getErrorMessage() {
		return errorMessage;
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

	public javax.mail.Message toEmail(javax.mail.Session session) {
		// This method performs validation and returns null if the message
		// shouldn't
		// be sent
		// Call getErrorMessage() in that case, to get the failure string.

		MimeMessage emsg = new MimeMessage(session);
		String field = "\"from\" = " + from;
		if (from != null) {
			if (addressValid(from)) {
				try {
					emsg.setFrom(new InternetAddress(from));
				} catch (Exception ex) { // discard & proceed
				}
			}
		}

		int size = to.size();
		for (String value : to) {
			field = "\"to\" = " + value;
			if (!addressValid(value)) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.TO, new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		size = cc.size();
		for (String value : cc) {
			field = "\"cc\" = " + value;
			if (!addressValid(value)) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.CC, new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		size = bcc.size();
		for (String value : bcc) {
			field = "\"bcc\" = " + value;
			if (!addressValid(value)) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.BCC, new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		field = "\"subject\" = " + subject;
		if (subject != null) {
			try {
				emsg.setSubject(subject, "utf-8");
			} catch (Exception ex) {
				errorMessage = "Error setting " + field + " : " + ex.getClass().getName() + " : " + ex.getMessage();
				return null;
			}
		}

		size = replyTo.size();
		if (size > 0) {
			field = "\"reply-to\"";

			ArrayList<InternetAddress> replyToList = new ArrayList<>();
			for (String value : replyTo) {
				if (!addressValid(value)) {
					continue;
				}

				try {
					replyToList.add(new InternetAddress(value));
				} catch (Exception ex) { // discard & proceed
				}
			}

			if (replyToList.size() > 0) {
				try {
					InternetAddress[] temp = new InternetAddress[replyToList.size()];
					replyToList.toArray(temp);
					emsg.setReplyTo(temp);
				} catch (Exception ex) {
					errorMessage = "Error setting " + field + " : " + ex.getClass().getName() + " : " + ex.getMessage();
					return null;
				}
			}
		}

		field = "\"body\" = " + body;
		if (body != null) {
			try {
				emsg.setText(body, "utf-8", subType);
			} catch (Exception ex) {
				errorMessage = "Error setting " + field + " : " + ex.getClass().getName() + " : " + ex.getMessage();
				return null;
			}
		} else {
			errorMessage = "Invalid body : null";
			return null;
		}

		// check for at least one recipient

		try {
			Address[] recipients = emsg.getAllRecipients();

			if (recipients == null || recipients.length == 0) {
				errorMessage = "No valid recipients";
				return null;
			}
		} catch (Exception ex) {
			errorMessage = "Error getting recipients for validation : " + ex.getClass().getName() + " : "
					+ ex.getMessage();
			return null;
		}

		return emsg;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}
}
