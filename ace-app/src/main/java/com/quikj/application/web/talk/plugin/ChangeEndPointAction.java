/*
 * ChangeEndPointAction.java
 *
 * Created on March 8, 2002, 1:41 AM
 */

package com.quikj.application.web.talk.plugin;

import com.quikj.server.app.EndPointInterface;

/**
 * 
 * @author amit
 */
public class ChangeEndPointAction implements EndPointActionInterface {

	private long sessionId;

	private EndPointInterface endPoint;

	/** Creates a new instance of ChangeEndPointAction */
	public ChangeEndPointAction(long session, EndPointInterface to_end_point) {
		sessionId = session;
		endPoint = to_end_point;
	}

	public EndPointInterface getEndPoint() {
		return endPoint;
	}
	
	public long getSessionId() {
		return sessionId;
	}
}
