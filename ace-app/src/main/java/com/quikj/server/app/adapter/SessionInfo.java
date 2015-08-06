/**
 * 
 */
package com.quikj.server.app.adapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.quikj.ace.messages.vo.app.Message;
import com.quikj.server.app.RemoteEndPoint;

/**
 * @author amit
 *
 */
public class SessionInfo {

	private String sessionId;
	private RemoteEndPoint endPoint;
	private Date lastComm;
	private List<Message> messages = new ArrayList<Message>();
	
	public SessionInfo(String sessionId, RemoteEndPoint endPoint, Date lastComm) {
		this.sessionId = sessionId;
		this.endPoint = endPoint;
		this.lastComm = lastComm;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public RemoteEndPoint getEndPoint() {
		return endPoint;
	}

	public void setEndPoint(RemoteEndPoint endPoint) {
		this.endPoint = endPoint;
	}

	public Date getLastComm() {
		return lastComm;
	}

	public void setLastComm(Date lastComm) {
		this.lastComm = lastComm;
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
}
