/*
 * SubscriberElement.java
 *
 * Created on May 18, 2002, 7:18 AM
 */

package com.quikj.application.web.talk.feature.operator;

import com.quikj.server.app.EndPointInterface;

/**
 * 
 * @author amit
 */
public class SubscriberElement {
	private long sessionId = -1;

	private EndPointInterface endpoint = null;

	private int requestId = -1;

	private long startWaitTime;
	
	public EndPointInterface getEndpoint() {
		return endpoint;
	}

	public int getRequestId() {
		return requestId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public long getStartWaitTime() {
		return this.startWaitTime;
	}

	public void setEndpoint(EndPointInterface endpoint) {
		this.endpoint = endpoint;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public void setStartWaitTime(long startWaitTime) {
		this.startWaitTime = startWaitTime;
	}
}
