/*
 * AutoEmailInfo.java
 *
 * Created on March 10, 2003, 2:03 AM
 */

package com.quikj.ace.web.client;

import java.util.ArrayList;

/**
 * 
 * @author amit
 */
public class EmailTranscriptInfo {

	private boolean emailTranscript = false;

	private boolean sendSelf;

	private boolean sendOthers;

	private String[] toList;

	private boolean from = false;

	private boolean fromSelf;

	private String[] fromList;

	public EmailTranscriptInfo() {

		emailTranscript = ClientProperties.getInstance().getBooleanValue(
				ClientProperties.EMAIL_TRANSCRIPT, false);
		if (!emailTranscript) {
			return;
		}

		String param = ClientProperties.getInstance().getStringValue(
				ClientProperties.TRANSCRIPT_EMAIL_FROM, null);
		if (param != null) {
			from = true;
			ArrayList<String> list = new ArrayList<String>();

			String[] tokens = param.split(";");
			for (int i = 0; i < tokens.length; i++) {
				String email = tokens[i];
				if (email.equals("@SELF")) {
					fromSelf = true;
				} else {
					list.add(email);
				}
			}

			fromList = list.toArray(new String[list.size()]);
		}

		param = ClientProperties.getInstance().getStringValue(
				ClientProperties.TRANSCRIPT_EMAIL_TO, null);
		if (param != null) {
			String[] tokens = param.split(";");
			ArrayList<String> list = new ArrayList<String>();
			for (int i = 0; i < tokens.length; i++) {
				String email = tokens[i];
				if (email.equals("@SELF")) {
					sendSelf = true;
				} else if (email.equals("@OTHERS")) {
					sendOthers = true;
				} else {
					list.add(email);
				}
			}

			toList = list.toArray(new String[list.size()]);
		}
	}

	public boolean isEmailTranscript() {
		return emailTranscript;
	}

	public void setEmailTranscript(boolean emailTranscript) {
		this.emailTranscript = emailTranscript;
	}

	public boolean isSendSelf() {
		return sendSelf;
	}

	public void setSendSelf(boolean sendSelf) {
		this.sendSelf = sendSelf;
	}

	public boolean isSendOthers() {
		return sendOthers;
	}

	public void setSendOthers(boolean sendOthers) {
		this.sendOthers = sendOthers;
	}

	public String[] getToList() {
		return toList;
	}

	public void setToList(String[] toList) {
		this.toList = toList;
	}

	public boolean isFrom() {
		return from;
	}

	public void setFrom(boolean from) {
		this.from = from;
	}

	public boolean isFromSelf() {
		return fromSelf;
	}

	public void setFromSelf(boolean fromSelf) {
		this.fromSelf = fromSelf;
	}

	public String[] getFromList() {
		return fromList;
	}

	public void setFromList(String[] fromList) {
		this.fromList = fromList;
	}
}
