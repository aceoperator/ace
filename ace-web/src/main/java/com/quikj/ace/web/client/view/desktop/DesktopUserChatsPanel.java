/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.UserChatsPresenter;
import com.quikj.ace.web.client.view.UserChatsPanel;
import com.quikj.ace.web.client.view.UserConversation;
import com.quikj.ace.web.client.view.UserMissedChat;

/**
 * @author beckie
 * 
 */
public class DesktopUserChatsPanel extends StackLayoutPanel implements
		UserChatsPanel {

	private static final double HEADER_SIZE = 30.0;
	private DesktopUserChatsConversationsPanel conversations;
	private DesktopUserChatsMissedPanel missedChats;

	public DesktopUserChatsPanel() {
		super(Unit.PX);

		conversations = new DesktopUserChatsConversationsPanel();
		add(conversations, ApplicationController.getMessages()
				.UserChatsPresenter_conversations(), false, HEADER_SIZE);

		missedChats = new DesktopUserChatsMissedPanel();
		add(missedChats, ApplicationController.getMessages()
				.UserChatsPresenter_missedChats(), false, HEADER_SIZE);
	}

	@Override
	public void setPresenter(UserChatsPresenter presenter) {
		conversations.setPresenter(presenter);
		missedChats.setPresenter(presenter);
	}

	@Override
	public void addNewConversation(UserConversation conv) {
		conversations.addNewConversation(conv);
	}

	@Override
	public void removeConversation(long sessionId) {
		conversations.removeConversation(sessionId);
	}

	@Override
	public void refreshConversations() {
		conversations.refreshConversations();
	}

	@Override
	public void setConversationEvent(long sessionId, String event) {
		conversations.setConversationEvent(sessionId, event);
		if (!getVisibleWidget().equals(conversations)) {
			showWidget(conversations);
		}
	}

	@Override
	public void replaceConversationSessionId(long oldSessionId,
			long newSessionId) {
		conversations.replaceConversationSessionId(oldSessionId, newSessionId);
	}

	@Override
	public UserConversation findConversation(long sessionId) {
		return conversations.findConversation(sessionId);
	}

	@Override
	public void addMissedChat(UserMissedChat chat, int maxListSize) {
		missedChats.addMissedChat(chat, maxListSize);
	}

}
