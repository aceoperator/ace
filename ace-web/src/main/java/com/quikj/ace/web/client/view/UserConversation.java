/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.Date;

/**
 * @author beckie
 * 
 */
public class UserConversation implements Comparable<UserConversation> {

	private long sessionId;
	private Date startTime;
	private String chatEvent;

	public UserConversation(long sessionId, Date startTime) {
		this.sessionId = sessionId;
		this.startTime = startTime;
	}

	public UserConversation(long sessionId) {
		this.sessionId = sessionId;
	}

	@Override
	public int compareTo(UserConversation other) {
		if (other == null) {
			return -1;
		}

		if (this.startTime.after(other.startTime)) {
			return -1;
		}

		return 1;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof UserConversation) {
			if (this.getSessionId() == ((UserConversation) obj).getSessionId()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return (int) (this.getSessionId() ^ (this.getSessionId() >>> 32));
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getChatEvent() {
		return chatEvent;
	}

	public void setChatEvent(String event) {
		this.chatEvent = event;
	}
}
