/**
 * 
 */
package com.quikj.ace.social;

import java.util.List;

import com.quikj.ace.messages.vo.app.Message;

/**
 * @author amit
 *
 */
public interface SocialChatEndpoint {
	List<Message> pollIncomingMessages();
	void sendOutgoingMessages(List<Message> messages);
}
