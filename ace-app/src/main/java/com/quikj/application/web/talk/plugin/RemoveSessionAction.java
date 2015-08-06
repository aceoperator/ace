/*
 * MergeEndPointAction.java
 *
 * Created on March 8, 2002, 1:32 AM
 */

package com.quikj.application.web.talk.plugin;

/**
 * 
 * @author amit
 */
public class RemoveSessionAction implements EndPointActionInterface {
	long sessionId = -1;

	public RemoveSessionAction(long session_id) {
		sessionId = session_id;
	}

	public long getSessionId() {
		return sessionId;
	}
}
