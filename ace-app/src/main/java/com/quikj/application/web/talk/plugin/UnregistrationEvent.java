package com.quikj.application.web.talk.plugin;

import com.quikj.server.framework.AceMessageInterface;

public class UnregistrationEvent implements AceMessageInterface {
	private static final String MESSAGE_TYPE = "UnregistrationMessage";

	private String user;

	public UnregistrationEvent(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public String messageType() {
		return MESSAGE_TYPE;
	}
}
