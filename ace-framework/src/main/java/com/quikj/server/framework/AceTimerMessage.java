package com.quikj.server.framework;

public final class AceTimerMessage implements AceMessageInterface {
	private long expiryTime;

	private AceThread requestingThread;

	private int timerId;

	private long userSpecifiedParm;

	protected AceTimerMessage(long exp, AceThread thr, int id, long user) {
		expiryTime = exp;
		requestingThread = thr;
		timerId = id;
		userSpecifiedParm = user;
	}

	public long getExpiryTime() {
		return expiryTime;
	}

	public AceThread getRequestingThread() {
		return requestingThread;
	}
	public int getTimerId() {
		return timerId;
	}
	public long getUserSpecifiedParm() {
		return userSpecifiedParm;
	}
	public String messageType() {
		return new String("AceTimerMessage");
	}
}
