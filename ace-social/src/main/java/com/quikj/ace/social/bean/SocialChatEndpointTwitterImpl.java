/**
 * 
 */
package com.quikj.ace.social.bean;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.social.SocialChatEndpoint;

/**
 * @author tomcat
 *
 */
public class SocialChatEndpointTwitterImpl implements SocialChatEndpoint {

	public SocialChatEndpoint init(Feature feature) {
		// TODO
		return this;
	}
	
	@Override
	public List<Message> pollIncomingMessages() {
		// TODO
		return null;
	}

	@Override
	public void sendOutgoingMessages(List<Message> messages) {
		// TODO
	}
}
