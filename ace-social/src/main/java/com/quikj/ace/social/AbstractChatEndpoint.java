/**
 * 
 */
package com.quikj.ace.social;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.social.SocialPrincipal.AuthType;

/**
 * @author amit
 *
 */
public abstract class AbstractChatEndpoint implements ChatEndpoint {

	private Map<String, ChatFarEnd> farEnds = new HashMap<String, ChatFarEnd>();

	protected SocialPrincipal principal;

	@Override
	public ChatEndpoint init(Feature feature) {

		String user = getProperty(feature, "twitter.chat.userId", null);
		if (user == null) {
			throw new SocialChatException("the chat userId is not found");
		}

		String credential = getProperty(feature, "twitter.chat.credential",
				null);
		if (credential == null) {
			throw new SocialChatException("the chat credential is not found");
		}

		String authType = getProperty(feature, "twitter.chat.authType",
				AuthType.PASSWORD.name());

		principal = new SocialPrincipal(user, credential,
				AuthType.valueOf(authType));

		connect(feature.getParams());
		return this;
	}

	protected abstract void connect(List<FeatureParam> params);

	private String getProperty(Feature feature, String name, String defaultValue) {
		for (FeatureParam param : feature.getParams()) {
			if (param.getName().equals(name)) {
				return param.getValue();
			}
		}
		return defaultValue;
	}

	@Override
	public List<Message> pollIncomingMessages() {
		List<Message> returnMessages = new ArrayList<Message>();
		Date timestamp = new Date();
		
		try {
			Iterator<?> i = getDirectMessages();
			while (i.hasNext()) {
				SocialMessage message = toSocialMessage(i);
						
				// TODO discard any messages that was sent earlier (less than 5
				// minutes back) or already read

				ChatFarEnd farEnd = farEnds.get(message.getScreenName());
				if (farEnd == null) {
					handleNewChatRequest(returnMessages, timestamp, message);
				}

				// TODO add existing chat request
			}
			return returnMessages;
		} catch (Exception e) {
			throw new SocialChatException(e);
		}
	}

	/**
	 * @param returnMessages
	 * @param timestamp
	 * @param message
	 */
	private void handleNewChatRequest(List<Message> returnMessages,
			Date timestamp, SocialMessage message) {
		ChatFarEnd farEnd;
		farEnd = new ChatFarEnd();
		farEnds.put(message.getScreenName(), farEnd);

		farEnd.setFullName(message.getFullName());
		farEnd.setUserName(message.getScreenName());
		farEnd.setAdditionalInformation((String)message.getMedia().getMessage());
		farEnd.setLastInteraction(timestamp);
		
		// TODO create a setup message and add to return messages
		SetupRequestMessage setup = new SetupRequestMessage();

		RequestMessage request = new RequestMessage();
		returnMessages.add(request);

		request.setMessage(setup);
	}

	protected abstract SocialMessage toSocialMessage(Iterator<?> i);

	protected abstract Iterator<?> getDirectMessages();

	@Override
	public void sendOutgoingMessages(List<Message> messages) {
		// TODO
	}
}
