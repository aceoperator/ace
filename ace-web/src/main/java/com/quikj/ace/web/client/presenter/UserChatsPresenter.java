/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.Date;
import java.util.Map;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ChatSessionInfo;
import com.quikj.ace.web.client.ChatSessionInfo.ChatStatus;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.SessionInfo;
import com.quikj.ace.web.client.view.UserChatsPanel;
import com.quikj.ace.web.client.view.UserConversation;
import com.quikj.ace.web.client.view.UserMissedChat;
import com.quikj.ace.web.client.view.desktop.DesktopUserChatsPanel;

/**
 * @author beckie
 * 
 */
public class UserChatsPresenter {

	private static final int DEFAULT_MAX_LIST_IDLE_CHATS = 3;

	private UserChatsPanel view;
	private static UserChatsPresenter instance;
	private int maxListIdleChats;

	public UserChatsPresenter() {
		instance = this;
	}

	public static UserChatsPresenter getCurrentInstance() {
		return instance;
	}

	public void show() {
		view = createView();
		view.setPresenter(this);

		maxListIdleChats = ClientProperties.getInstance().getIntValue(
				ClientProperties.MAX_LIST_IDLE_CHATS, DEFAULT_MAX_LIST_IDLE_CHATS);

		UserPanelPresenter.getCurrentInstance().addNewPanel(
				ApplicationController.getMessages()
						.UserChatsPresenter_conversations(), (Widget) view);
	}

	public void dispose() {
		view = null;
		instance = null;
	}

	private UserChatsPanel createView() {
		return new DesktopUserChatsPanel();
	}

	public void addNewChat(ChatSessionInfo chat) {
		UserConversation conv = new UserConversation(chat.getSessionId(),
				new Date());
		view.addNewConversation(conv);
	}

	public void addMissedChat(String image, String name, String emailAddress) {
		UserMissedChat chat = new UserMissedChat(image, name, emailAddress,
				new Date());
		view.addMissedChat(chat, maxListIdleChats);
	}

	public void removeChat(long sessionId) {
		view.removeConversation(sessionId);
	}

	public void chatConnected(long sessionId, boolean highlightChatEvent) {
		if (highlightChatEvent) {
			UserPanelPresenter.getCurrentInstance().highlightChatEvent(
					sessionId, "connected");
		}

		view.refreshConversations();
	}

	public void chatDisconnected(long sessionId, boolean farEndEvent) {
		if (farEndEvent) {
			UserPanelPresenter.getCurrentInstance().highlightChatEvent(
					sessionId, "disconnected");
		}

		ChatSessionInfo convToRemove = checkOustOldConversation();
		if (convToRemove != null) {
			if (farEndEvent) {
				// if looking at the chat to be removed, dispose of it later
				// when another chat replaces it
				if (UserPanelPresenter.getCurrentInstance().userIsViewingChat(
						convToRemove.getSessionId())) {
					removeChat(convToRemove.getSessionId()); // from conv list
				} else {
					convToRemove.getChat().dispose(
							DisconnectReasonElement.NORMAL_DISCONNECT, null);
				}
			} else {
				if (convToRemove.getSessionId() == sessionId) {
					convToRemove.getChat().chatClosed(); // goto conv list
				} else {
					convToRemove.getChat().dispose(
							DisconnectReasonElement.NORMAL_DISCONNECT, null);
				}
			}
		}

		view.refreshConversations();
	}

	private ChatSessionInfo checkOustOldConversation() {
		// find num disconnected conversations and oldest disconnected
		ChatSessionInfo oldest = null;

		int count = 0;
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		for (ChatSessionInfo chat : chats.values()) {
			if (chat.getStatus().equals(ChatStatus.DISCONNECTED)) {
				count++;

				if (oldest == null) {
					oldest = chat;
				} else {
					if (oldest.getChat().getConversationDisc()
							.after(chat.getChat().getConversationDisc())) {
						oldest = chat;
					}
				}
			}
		}

		// check max disconnected allowed
		if (count > maxListIdleChats) {
			return oldest;
		}

		return null;
	}

	public int getActiveChatCount() {
		int count = 0;
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		for (ChatSessionInfo chat : chats.values()) {
			if (!chat.getStatus().equals(ChatStatus.DISCONNECTED)) {
				count++;
			}
		}

		return count;
	}

	public void highlightChat(long sessionId, String event) {
		view.setConversationEvent(sessionId, event);
	}

	public void replaceSession(long oldSessionId, long newSessionId) {
		view.replaceConversationSessionId(oldSessionId, newSessionId);
	}

	public void chatInformationChanged(long sessionId) {
		view.refreshConversations();
	}

	public boolean chatExists(long sessionId) {
		return view.findConversation(sessionId) != null;
	}

	// methods called from view

	public void showChat(long sessionId) {

		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo != null) {
			chatInfo.getChat().showChat();
		}
	}

	public void closeChat(long sessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo != null) {
			chatInfo.getChat().dispose(
					DisconnectReasonElement.NORMAL_DISCONNECT, null);
		}
	}

	public ChatSessionInfo.ChatStatus getChatStatus(long sessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo == null) {
			return ChatSessionInfo.ChatStatus.DISCONNECTED;
		}

		return chatInfo.getStatus();
	}

	public int getChatNumParties(long sessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo == null) {
			return 0;
		}

		return chatInfo.getChat().getNumParties();
	}

	public String getChatOtherParties(long sessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo == null) {
			return "";
		}

		return chatInfo.getChat().getOtherPartyNames();
	}

	public String getOtherPartyAvatar(long sessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo chatInfo = (ChatSessionInfo) chats.get(sessionId);
		if (chatInfo == null) {
			return null;
		}

		return chatInfo.getChat().getCalledPartyAvatar();
	}

	public void showErrorDialog(String title, String error) {
		MessageBoxPresenter.getInstance().show(title, error,
				(String) Images.CRITICAL_MEDIUM, true);
	}

}
