/**
 * 
 */
package com.quikj.ace.web.client.view;

import com.quikj.ace.web.client.presenter.UserChatsPresenter;

/**
 * @author beckie
 * 
 */
public interface UserChatsPanel {

	public void setPresenter(UserChatsPresenter presenter);

	public void addNewConversation(UserConversation conv);

	public void removeConversation(long sessionId);

	public void refreshConversations();

	public void setConversationEvent(long sessionId, String event);

	public void replaceConversationSessionId(long oldSessionId,
			long newSessionId);

	public UserConversation findConversation(long sessionId);

	public void addMissedChat(UserMissedChat chat, int maxListSize);

}
