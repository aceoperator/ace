package com.quikj.server.framework;

public class AceSignalMessage implements AceMessageInterface {
	public static final int SIGNAL_TERM = 0;

	private int signalId;

	private String message;

	protected AceSignalMessage(int sig_id) {
		this(sig_id, "");
	}

	protected AceSignalMessage(int sig_id, String message) {
		signalId = sig_id;
		this.message = new String(message);
	}

	public String getMessage() {
		return message;
	}

	public int getSignalId() {
		return signalId;
	}
	public String messageType() {
		return new String("AceSignalMessage");
	}

}
