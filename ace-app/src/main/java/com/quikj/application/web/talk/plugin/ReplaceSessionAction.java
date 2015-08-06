/*
 * ChangeSessionAction.java
 *
 * Created on April 13, 2002, 4:41 PM
 */

package com.quikj.application.web.talk.plugin;

/**
 * 
 * @author amit
 */
public class ReplaceSessionAction implements EndPointActionInterface {
	private long newSessionId;
	private long oldSessionId;
	private String newKey = null;

	public ReplaceSessionAction(long old_session_id, long new_session_id) {
		oldSessionId = old_session_id;
		newSessionId = new_session_id;
	}

	public String getNewKey() {
		return newKey;
	}

	public long getNewSessionId() {
		return newSessionId;
	}

	public long getOldSessionId() {
		return oldSessionId;
	}

	public void setNewKey(String newKey) {
		this.newKey = newKey;
	}

	public void setNewSessionId(long newSessionId) {
		this.newSessionId = newSessionId;
	}

	public void setOldSessionId(long oldSessionId) {
		this.oldSessionId = oldSessionId;
	}
}
