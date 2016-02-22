/**
 * 
 */
package com.quikj.ace.social.bean;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import twitter4j.DirectMessage;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.social.AbstractChatEndpoint;
import com.quikj.ace.social.SocialChatException;
import com.quikj.ace.social.SocialMessage;
import com.quikj.ace.social.TextMedia;

/**
 * @author amit
 *
 */
public class ChatEndpointTwitterImpl extends AbstractChatEndpoint {

	private Twitter twitter;

	protected void connect(List<FeatureParam> params) {
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setPrettyDebugEnabled(true).setDebugEnabled(true)
				.setUser(principal.getName())
				.setPassword(principal.getCredential().toString());

		twitter = new TwitterFactory(builder.build()).getInstance();
	}

	protected SocialMessage toSocialMessage(Iterator<?> i) {
		DirectMessage directMessage = (DirectMessage) i.next();
		String screenName = directMessage.getSender().getScreenName();
		String name = directMessage.getSender().getName();
		String text = directMessage.getText();
		Date createdAt = directMessage.getCreatedAt();

		SocialMessage message = new SocialMessage(screenName, name, createdAt,
				new TextMedia(text));
		return message;
	}

	protected Iterator<?> getDirectMessages() {
		ResponseList<DirectMessage> messages;
		try {
			messages = twitter.getDirectMessages();
		} catch (TwitterException e) {
			throw new SocialChatException(e);
		}
		return messages.iterator();
	}
}
