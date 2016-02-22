/**
 * 
 */
package com.quikj.ace.social;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.messages.vo.app.Message;

/**
 * @author amit
 *
 */
public interface ChatEndpoint {
	ChatEndpoint init(Feature feature);

	List<Message> pollIncomingMessages();

	void sendOutgoingMessages(List<Message> messages);
}
