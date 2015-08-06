/**
 * 
 */
package com.quikj.ace.web.client;

import com.quikj.ace.web.client.presenter.ChatSessionPresenter;

/**
 * @author amit
 *
 */
public class ChatSessionInfo {

	public enum ChatStatus {
		SETUP_IN_PROGRESS,
		CONNECTED,
		DISCONNECTED
	}
	
	private long sessionId;
	private ChatSessionPresenter chat;
	private ChatStatus status;
	
	public ChatSessionInfo(ChatStatus status) {
		this.status = status;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}

	public ChatSessionPresenter getChat() {
		return chat;
	}

	public void setChat(ChatSessionPresenter chat) {
		this.chat = chat;
	}

	public ChatStatus getStatus() {
		return status;
	}

	public void setStatus(ChatStatus status) {
		this.status = status;
	}
}
