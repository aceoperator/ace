package com.quikj.ace.messages.vo.talk;

public class RTPMessage implements TalkMessageInterface {

	private static final long serialVersionUID = -2833426684637704389L;

	private MediaElements media = null;

	private long sessionId = -1;

	private boolean parse = false;

	CallPartyElement from;

	public RTPMessage() {
	}

	public RTPMessage(RTPMessage rtpMessageToClone) {
		this(new MediaElements(rtpMessageToClone.getMediaElements()),
				rtpMessageToClone.getSessionId(), rtpMessageToClone.isParse(),
				new CallPartyElement(rtpMessageToClone.getFrom()));
	}

	public RTPMessage(MediaElements media, long sessionId, boolean parse,
			CallPartyElement from) {
		this.media = media;
		this.sessionId = sessionId;
		this.parse = parse;
		this.from = from;
	}

	public MediaElements getMediaElements() {
		return media;
	}

	public long getSessionId() {
		return sessionId;
	}

	public boolean isParse() {
		return parse;
	}

	public void setMediaElements(MediaElements media) {
		this.media = media;
	}

	public void setParse(boolean parse) {
		this.parse = parse;
	}

	public void setSessionId(long session) {
		sessionId = session;
	}

	public CallPartyElement getFrom() {
		return from;
	}

	public void setFrom(CallPartyElement from) {
		this.from = from;
	}
}
