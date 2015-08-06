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

	/** Holds value of property startWaitTime. */
	private long startWaitTime;

	/** Creates a new instance of SubscriberElement */
	public SubscriberElement() {
	}

	/**
	 * Getter for property endpoint.
	 * 
	 * @return Value of property endpoint.
	 */
	public EndPointInterface getEndpoint() {
		return endpoint;
	}

	/**
	 * Getter for property requestId.
	 * 
	 * @return Value of property requestId.
	 * 
	 */
	public int getRequestId() {
		return requestId;
	}

	/**
	 * Getter for property sessionId.
	 * 
	 * @return Value of property sessionId.
	 */
	public long getSessionId() {
		return sessionId;
	}

	/**
	 * Getter for property startWaitTime.
	 * 
	 * @return Value of property startWaitTime.
	 * 
	 */
	public long getStartWaitTime() {
		return this.startWaitTime;
	}

	/**
	 * Setter for property endpoint.
	 * 
	 * @param endpoint
	 *            New value of property endpoint.
	 */
	public void setEndpoint(EndPointInterface endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * Setter for property requestId.
	 * 
	 * @param requestId
	 *            New value of property requestId.
	 * 
	 */
	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	/**
	 * Setter for property sessionId.
	 * 
	 * @param sessionId
	 *            New value of property sessionId.
	 */
	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	/**
	 * Setter for property startWaitTime.
	 * 
	 * @param startWaitTime
	 *            New value of property startWaitTime.
	 * 
	 */
	public void setStartWaitTime(long startWaitTime) {
		this.startWaitTime = startWaitTime;
	}

}
