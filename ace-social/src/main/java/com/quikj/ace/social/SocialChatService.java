/**
 * 
 */
package com.quikj.ace.social;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.application.web.talk.feature.operator.Operator;
import com.quikj.server.app.adapter.PolledAppServerAdapter;

/**
 * @author amit
 *
 */
public class SocialChatService {
	private Log logger = LogFactory.getLog(SocialChatService.class);

	private Timer timer;

	private Map<String, SocialChatEndpoint> groups = new HashMap<String, SocialChatEndpoint>();

	private FeatureBean featureBean;

	private SocialChatFactory socialChatFactory;

	private long pollPeriod = 60000L;

	public void setPollPeriod(long pollPeriod) {
		this.pollPeriod = pollPeriod;
	}

	public void setSocialChatFactory(SocialChatFactory socialChatFactory) {
		this.socialChatFactory = socialChatFactory;
	}

	public void setFeatureBean(FeatureBean featureBean) {
		this.featureBean = featureBean;
	}

	private class SocialChatOperations extends TimerTask {

		@Override
		public void run() {
			try {
				// TODO the update configuration every few times, not every time
				updateGroupConfigurations();

				for (Entry<String, SocialChatEndpoint> group : groups
						.entrySet()) {
					try {
						List<Message> incoming = group.getValue()
								.pollIncomingMessages();

						for (Message message : incoming) {
							List<Message> outgoing = PolledAppServerAdapter
									.getInstance().exchangeMessages(message);

							try {
								group.getValue().sendOutgoingMessages(outgoing);
							} catch (Exception e) {
								logger.error(
										"Error sending message to the endpoint "
												+ group.getKey(), e);
							}
						}
					} catch (Exception e) {
						logger.error(
								"Error communicating with endpoint or with the server adapter for "
										+ group.getKey()
										+ ". Chat messages are potentially lost",
								e);
					}
				}

			} catch (Throwable e) {
				// DO not allow screw up from one group to cause all social
				// messaging to stop
				logger.error(e);
			}
		}
	}

	@PostConstruct
	public void init() {
		updateGroupConfigurations();

		timer = new Timer();
		timer.schedule(new SocialChatOperations(), pollPeriod);
	}

	@PreDestroy
	public void destroy() {
		timer.cancel();
	}

	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	private void updateGroupConfigurations() {
		List<Feature> features = featureBean.listFeatures(null);

		for (Feature feature : features) {
			try {
				if (!feature.getClassName().equals(Operator.class.getName())) {
					continue;
				}

				boolean supportsChat = socialChatFactory
						.supportsSocialChat(feature.getParams());
				if (groups.containsKey(feature.getName())) {
					if (!supportsChat) {
						// social messaging just got removed/disabled, cleanup,
						// remove from the group
						socialChatFactory.removeChatEndpoint(feature.getName());
						groups.remove(feature.getName());
					} else {
						// check for param changes and reload endpoint if
						// necessary
						SocialChatEndpoint endpoint = socialChatFactory
								.updateChatEndpoint(feature);
						groups.put(feature.getName(), endpoint);
					}
				} else if (supportsChat) {
					// new endpoint that just got added
					SocialChatEndpoint endpoint = socialChatFactory
							.addChatEndpoint(feature);
					groups.put(feature.getName(), endpoint);
				}
			} catch (Exception e) {
				// TODO continue even if one of the endpoints screws up
			}
		}

		for (String feature : groups.keySet()) {
			try {
				if (!findFeature(feature, features)) {
					// The feature got removed, cleanup, remove from the group
					socialChatFactory.removeChatEndpoint(feature);
					groups.remove(feature);
				}
			} catch (Exception e) {
				// TODO continue even if one of the endpoints screws up
			}
		}
	}

	private boolean findFeature(String name, List<Feature> features) {
		for (Feature feature : features) {
			if (feature.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
}
