package com.quikj.application.web.talk.plugin;

import com.quikj.server.framework.AceMessageInterface;

public class DropEndpointEvent implements AceMessageInterface {
	private static final String MESSAGE_TYPE = "DropEndpointMessage";

	public DropEndpointEvent() {
	}

	public String messageType() {
		return MESSAGE_TYPE;
	}
}
