package com.quikj.application.web.talk.plugin;

import com.quikj.ace.messages.vo.talk.TalkMessageInterface;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceMessageInterface;

public class MessageEvent implements AceMessageInterface {
	public static final int CLIENT_REQUEST_MESSAGE = 1; // unhandled request
														// from client
	public static final int CLIENT_RESPONSE_MESSAGE = 2; // unhandled response
															// from server
	public static final int REGISTRATION_REQUEST = 3;
	public static final int REGISTRATION_RESPONSE = 4;
	public static final int SETUP_REQUEST = 5;
	public static final int SETUP_RESPONSE = 6;
	public static final int RTP_MESSAGE = 7;
	public static final int DISCONNECT_MESSAGE = 8;

	private static final String MESSAGE_TYPE = "TalkMessageEvent";

	private int eventType;

	private EndPointInterface from;

	private int responseStatus = 0;

	private String reason;

	private TalkMessageInterface message;

	private Object userParm;

	private int requestId = -1;

	public MessageEvent(int event_type, EndPointInterface from,
			int response_status, String reason, TalkMessageInterface message,
			Object user_parm) {
		this(event_type, from, response_status, reason, message, user_parm, -1);
	}

	public MessageEvent(int event_type, EndPointInterface from,
			int response_status, String reason, TalkMessageInterface message,
			Object user_parm, int request_id) {
		eventType = event_type;
		this.from = from;
		responseStatus = response_status;
		this.reason = reason;
		this.message = message;
		userParm = user_parm;
		requestId = request_id;
	}

	public MessageEvent(int event_type, EndPointInterface from,
			TalkMessageInterface message, Object user_parm) {
		this(event_type, from, message, user_parm, -1);
	}

	public MessageEvent(int event_type, EndPointInterface from,
			TalkMessageInterface message, Object user_parm, int request_id) {
		this(event_type, from, 0, null, message, user_parm, request_id);
	}

	public int getEventType() {
		return eventType;
	}

	public EndPointInterface getFrom() {
		return from;
	}

	public TalkMessageInterface getMessage() {
		return message;
	}
	public String getReason() {
		return reason;
	}
	public int getRequestId() {
		return requestId;
	}
	public int getResponseStatus() {
		return responseStatus;
	}
	public Object getUserParm() {
		return userParm;
	}
	public boolean isRequest() {
		if (responseStatus == 0) {
			return true;
		} else {
			return false;
		}
	}
	public String messageType() {
		return MESSAGE_TYPE;
	}
}
