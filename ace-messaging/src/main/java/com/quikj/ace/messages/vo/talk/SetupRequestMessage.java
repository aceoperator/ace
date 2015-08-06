package com.quikj.ace.messages.vo.talk;

public class SetupRequestMessage implements TalkMessageInterface {

	private static final long serialVersionUID = 980467070567185954L;

	private CallingNameElement calling = null;

	private CalledNameElement called = null;

	private long sessionId = -1;

	private String transferId = null;

	private String transferFrom = null;

	private boolean userTransfer = false;

	private boolean userConference = false;

	private MediaElements media = null;

	public SetupRequestMessage() {
	}

	public CalledNameElement getCalledNameElement() {
		return called;
	}

	public CallingNameElement getCallingNameElement() {
		return calling;
	}

	public MediaElements getMedia() {
		return media;
	}

	public long getSessionId() {
		return sessionId;
	}

	public String getTransferFrom() {
		return transferFrom;
	}

	public String getTransferId() {
		return transferId;
	}

	public void setCalledNameElement(CalledNameElement called) {
		this.called = called;
	}

	public void setCallingNameElement(CallingNameElement calling) {
		this.calling = calling;
	}

	public void setMedia(MediaElements media) {
		this.media = media;
	}

	public void setSessionId(long id) {
		sessionId = id;
	}

	public void setTransferFrom(java.lang.String transferFrom) {
		this.transferFrom = transferFrom;
	}

	public void setTransferId(java.lang.String transferId) {
		this.transferId = transferId;
	}

	public boolean isUserTransfer() {
		return userTransfer;
	}

	public void setUserTransfer(boolean userTransfer) {
		this.userTransfer = userTransfer;
	}

	public boolean isUserConference() {
		return userConference;
	}

	public void setUserConference(boolean userConference) {
		this.userConference = userConference;
	}
}
