package com.quikj.ace.messages.vo.talk;

public class SetupResponseMessage implements TalkMessageInterface {

	private static final long serialVersionUID = 1985268754607150343L;

	public static final int ACK = 230;
	public static final int ALERTING = 231;
	public static final int PROG = 232;
	public static final int CONNECT = 233;

	public static final int TRANSFER = 310;

	public static final int BUSY = 430;
	public static final int NOANS = 431;
	public static final int UNAVAILABLE = 432;
	public static final int UNKNOWN = 433;
	public static final int FORBIDDEN = 434;

	private MediaElements media = null;

	private long sessionId = -1;

	private CalledNameElement called = null;

	private CallPartyElement transferredFrom;

	private long newSessionId = -1L;
	
	private String callingCookie;

	public SetupResponseMessage() {
	}

	public SetupResponseMessage(SetupResponseMessage toClone) {
		this(toClone.getMediaElements(), toClone.getSessionId(), new CalledNameElement(toClone
				.getCalledParty()), new CallPartyElement(toClone.getTransferredFrom()), toClone
				.getSessionId(), toClone.getCallingCookie());
	}

	public SetupResponseMessage(MediaElements media, long sessionId,
			CalledNameElement called, CallPartyElement transferredFrom,
			long newSessionId, String callingCookie) {
		this.media = media;
		this.sessionId = sessionId;
		this.called = called;
		this.transferredFrom = transferredFrom;
		this.newSessionId = newSessionId;
		this.callingCookie = callingCookie;
	}

	public long getNewSessionId() {
		return newSessionId;
	}

	public void setNewSessionId(long newSessionId) {
		this.newSessionId = newSessionId;
	}

	public CallPartyElement getTransferredFrom() {
		return transferredFrom;
	}

	public void setTransferredFrom(CallPartyElement transferredFrom) {
		this.transferredFrom = transferredFrom;
	}

	public CalledNameElement getCalledParty() {
		return called;
	}

	public MediaElements getMediaElements() {
		return media;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setCalledParty(CalledNameElement called) {
		this.called = called;
	}

	public void setMediaElements(MediaElements media) {
		this.media = media;
	}

	public void setSessionId(long session) {
		sessionId = session;
	}

	public String getCallingCookie() {
		return callingCookie;
	}

	public void setCallingCookie(String callingCookie) {
		this.callingCookie = callingCookie;
	}
}
