package com.quikj.application.web.talk.plugin;

import java.util.ArrayList;
import java.util.List;

import com.quikj.server.app.EndPointInterface;

public class SessionInfo {
	private long sessionId;

	private List<EndPointInterface> endPoints = new ArrayList<EndPointInterface>();

	private boolean connected = false;

	private int requestId = -1;

	private ConferenceBridge conferenceBridge = null;

	private String billingId = null;
	
	private String transcriptFile;
	
	public SessionInfo(long session, EndPointInterface calling) {
		sessionId = session;
		endPoints.add(calling);
	}

	public void addEndPoint(EndPointInterface endpoint) {
		endPoints.add(endpoint);
	}

	public EndPointInterface elementAt(int index) {
		return (EndPointInterface) endPoints.get(index);
	}

	public String getBillingId() {
		return billingId;
	}

	public EndPointInterface getCallingEndPoint() {
		// the calling endpoint is always the first one
		return elementAt(0);
	}

	public ConferenceBridge getConferenceBridge() {
		return conferenceBridge;
	}

	public int getRequestId() {
		return requestId;
	}

	public long getSessionId() {
		return sessionId;
	}

	public int indexOf(EndPointInterface endpoint) {
		return endPoints.indexOf(endpoint);
	}

	public boolean isConnected() {
		return connected;
	}

	public int numEndPoints() {
		return endPoints.size();
	}

	public boolean removeEndPoint(EndPointInterface endpoint) {
		int index = indexOf(endpoint);

		if (index >= 0) {
			endPoints.remove(index);
		} else {
			return false;
		}
		return true;
	}

	public boolean replaceEndPoint(int index, EndPointInterface endpoint) {
		if (index < numEndPoints()) {
			endPoints.set(index, endpoint);
		} else {
			return false;
		}
		return true;
	}

	public void setBillingId(String billingId) {
		this.billingId = billingId;
	}

	public void setConferenceBridge(ConferenceBridge bridge) {
		conferenceBridge = bridge;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void setRequestId(int request_id) {
		requestId = request_id;
	}

	public void setSessionId(long new_session_id) {
		sessionId = new_session_id;
	}

	public String getTranscriptFile() {
		return transcriptFile;
	}

	public void setTranscriptFile(String transcriptFile) {
		this.transcriptFile = transcriptFile;
	}
}
