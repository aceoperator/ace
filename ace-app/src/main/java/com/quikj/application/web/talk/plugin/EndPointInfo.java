package com.quikj.application.web.talk.plugin;

import com.quikj.server.app.EndPointInterface;

public class EndPointInfo {
	private EndPointInterface endpoint = null;

	private int callCount = 0;

	private UserElement userData = null;

	private boolean dnd = false;
	
	public EndPointInfo() {
	}

	public int getCallCount() {
		return callCount;
	}

	public EndPointInterface getEndPoint() {
		return endpoint;
	}

	public String getName() {
		if (userData == null) {
			return "Nulldata";
		}

		return userData.getName();
	}

	public UserElement getUserData() {
		return userData;
	}

	public boolean isDnd() {
		return dnd;
	}

	public void setCallCount(int callCount) {
		this.callCount = callCount;
	}

	public void setDnd(boolean dnd) {
		this.dnd = dnd;
	}

	public void setEndPoint(EndPointInterface endpoint) {
		this.endpoint = endpoint;
	}

	public void setUserData(UserElement userData) {
		this.userData = userData;
	}
}
