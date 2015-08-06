package com.quikj.ace.messages.vo.talk;

public class DisconnectMessage implements TalkMessageInterface {
	private static final long serialVersionUID = 4750408049670291550L;

	private long sessionId = -1;

	private DisconnectReasonElement reason = null;

	private CalledNameElement calledInfo = null;

	private String transferId = null;

	private CallPartyElement from = null;

	private boolean transcript = false;
	
	public DisconnectMessage() {
	}

	public CalledNameElement getCalledInfo() {
		return calledInfo;
	}

	public DisconnectReasonElement getDisconnectReason() {
		return reason;
	}

	public long getSessionId() {
		return sessionId;
	}

	public String getTransferId() {
		return transferId;
	}

	public boolean isTranscript() {
		return transcript;
	}

	public void setCalledInfo(CalledNameElement element) {
		calledInfo = element;
	}

	public void setDisconnectReason(DisconnectReasonElement reason) {
		this.reason = reason;
	}

	public void setSessionId(long id) {
		sessionId = id;
	}

	public void setTranscript(boolean transcript) {
		this.transcript = transcript;
	}

	public void setTransferId(String transferId) {
		this.transferId = transferId;
	}

	public CallPartyElement getFrom() {
		return from;
	}

	public void setFrom(CallPartyElement from) {
		this.from = from;
	}
}
