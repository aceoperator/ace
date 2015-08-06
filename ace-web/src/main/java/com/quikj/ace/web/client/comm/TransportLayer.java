package com.quikj.ace.web.client.comm;

import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.app.ResponseMessage;

public interface TransportLayer {

	public static final int RPC_TIMEOUT = 45 * 1000;
	public static final int DEFAULT_SLOW_POLL_TIMER = 15 * 1000;
	public static final int DEFAULT_FAST_POLL_TIMER = 5 * 1000;
	public static final int DEFAULT_MAX_FAILURE_COUNT = 5;

	public abstract void connect();

	public abstract void disconnect(boolean sendDisconnect);

	public abstract void disconnect();

	public abstract boolean isConnected();

	public abstract int sendRequest(RequestMessage message);

	public abstract void sendResponse(int requestId, ResponseMessage message);

	public void setApplication(ApplicationLayer application);

}