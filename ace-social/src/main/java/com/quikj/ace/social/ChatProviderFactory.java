/**
 * 
 */
package com.quikj.ace.social;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.social.bean.ChatEndpointTwitterImpl;

/**
 * @author amit
 *
 */
public class ChatProviderFactory {

	private Map<String, String> providers = new HashMap<String, String>();

	public void setProviders(Map<String, String> providers) {
		this.providers = providers;
	}

	// TODO remove this method, instead initialize the providers using spring
	// injection
	@PostConstruct
	public void init() {
		providers.put("twitter", ChatEndpointTwitterImpl.class.getName());
	}

	public boolean supportsSocialChat(List<FeatureParam> params) {
		// Currently only twitter is supported
		return getProvider(params) != null;
	}

	private String getProvider(List<FeatureParam> params) {
		for (FeatureParam param : params) {
			if (param.getName().equals("social.chat.provider")
					&& providers.containsKey(param.getValue())) {
				return param.getValue();
			}
		}
		return null;
	}

	public ChatEndpoint addChatEndpoint(Feature feature) {
		try {
			String provider = getProvider(feature.getParams());
			String className = providers.get(provider);
			Class<?> clazz = Class.forName(className);
			ChatEndpoint endpoint = (ChatEndpoint) clazz.newInstance();

			return endpoint.init(feature);
		} catch (Exception e) {
			throw new SocialChatException(e);
		}
	}

	public ChatEndpoint updateChatEndpoint(Feature feature) {
		// TODO
		return null;
	}

	public void removeChatEndpoint(String name) {
		// TODO Auto-generated method stub

	}
}
