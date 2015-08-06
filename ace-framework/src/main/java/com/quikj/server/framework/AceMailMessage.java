/*
 * MailMessage.java
 *
 * Created on November 16, 2002, 10:13 PM
 */

package com.quikj.server.framework;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 
 * @author bhm
 */
public class AceMailMessage implements Serializable {

	private String from = null;
	private Vector to = new Vector();
	private Vector cc = new Vector();
	private Vector bcc = new Vector();
	private String subject = null;
	private Vector replyTo = new Vector();
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
	public java.util.Vector getBcc() {
		return bcc;
	}

	/**
	 * Getter for property body.
	 * 
	 * @return Value of property body.
	 */
	public java.lang.String getBody() {
		return body;
	}

	/**
	 * Getter for property cc.
	 * 
	 * @return Value of property cc.
	 */
	public java.util.Vector getCc() {
		return cc;
	}

	/**
	 * Getter for property errorMessage.
	 * 
	 * @return Value of property errorMessage.
	 * 
	 */
	public java.lang.String getErrorMessage() {
		return errorMessage;
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
	public java.util.Vector getReplyTo() {
		return replyTo;
	}

	/**
	 * Getter for property subject.
	 * 
	 * @return Value of property subject.
	 */
	public java.lang.String getSubject() {
		return subject;
	}

	/**
	 * Getter for property to.
	 * 
	 * @return Value of property to.
	 */
	public java.util.Vector getTo() {
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

	/**
	 * Setter for property bcc.
	 * 
	 * @param bcc
	 *            New value of property bcc.
	 */
	public void setBcc(java.util.Vector bcc) {
		this.bcc = bcc;
	}

	/**
	 * Setter for property body.
	 * 
	 * @param body
	 *            New value of property body.
	 */
	public void setBody(java.lang.String body) {
		this.body = body;
	}

	/**
	 * Setter for property cc.
	 * 
	 * @param cc
	 *            New value of property cc.
	 */
	public void setCc(java.util.Vector cc) {
		this.cc = cc;
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
	public void setReplyTo(java.util.Vector replyTo) {
		this.replyTo = replyTo;
	}

	/**
	 * Setter for property subject.
	 * 
	 * @param subject
	 *            New value of property subject.
	 */
	public void setSubject(java.lang.String subject) {
		this.subject = subject;
	}

	/**
	 * Setter for property to.
	 * 
	 * @param to
	 *            New value of property to.
	 */
	public void setTo(java.util.Vector to) {
		this.to = to;
	}

	public javax.mail.Message toEmail(javax.mail.Session session)
	// This method performs validation and returns null if the message shouldn't
	// be sent
	// Call getErrorMessage() in that case, to get the failure string.
	{
		MimeMessage emsg = new MimeMessage(session);
		String field = "\"from\" = " + from;
		if (from != null) {
			if (addressValid(from) == true) {
				try {
					emsg.setFrom(new InternetAddress(from));
				} catch (Exception ex) { // discard & proceed
				}
			}
		}

		int size = to.size();
		for (int i = 0; i < size; i++) {
			String value = (String) to.elementAt(i);
			field = "\"to\" = " + value;
			if (addressValid(value) == false) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.TO,
						new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		size = cc.size();
		for (int i = 0; i < size; i++) {
			String value = (String) cc.elementAt(i);
			field = "\"cc\" = " + value;
			if (addressValid(value) == false) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.CC,
						new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		size = bcc.size();
		for (int i = 0; i < size; i++) {
			String value = (String) bcc.elementAt(i);
			field = "\"bcc\" = " + value;
			if (addressValid(value) == false) {
				continue;
			}

			try {
				emsg.addRecipient(Message.RecipientType.BCC,
						new InternetAddress(value));
			} catch (Exception ex) { // discard & proceed
			}
		}

		field = "\"subject\" = " + subject;
		if (subject != null) {
			try {
				emsg.setSubject(subject, "utf-8");
			} catch (Exception ex) {
				errorMessage = "Error setting " + field + " : "
						+ ex.getClass().getName() + " : " + ex.getMessage();
				return null;
			}
		}

		size = replyTo.size();
		if (size > 0) {
			field = "\"reply-to\"";

			ArrayList reply_to = new ArrayList();
			for (int i = 0; i < size; i++) {
				String value = (String) replyTo.elementAt(i);
				if (addressValid(value) == false) {
					continue;
				}

				try {
					reply_to.add(new InternetAddress(value));
				} catch (Exception ex) { // discard & proceed
				}
			}

			if (reply_to.size() > 0) {
				try {
					InternetAddress[] temp = new InternetAddress[reply_to
							.size()];
					reply_to.toArray(temp);
					emsg.setReplyTo(temp);
				} catch (Exception ex) {
					errorMessage = "Error setting " + field + " : "
							+ ex.getClass().getName() + " : " + ex.getMessage();
					return null;
				}
			}
		}

		field = "\"body\" = " + body;
		if (body != null) {
			try {
				emsg.setText(body, "utf-8", subType);
			} catch (Exception ex) {
				errorMessage = "Error setting " + field + " : "
						+ ex.getClass().getName() + " : " + ex.getMessage();
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
			errorMessage = "Error getting recipients for validation : "
					+ ex.getClass().getName() + " : " + ex.getMessage();
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
