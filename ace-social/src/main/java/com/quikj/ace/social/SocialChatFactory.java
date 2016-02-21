/**
 * 
 */
package com.quikj.ace.social;

import java.util.List;

import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.FeatureParam;
import com.quikj.ace.social.bean.SocialChatEndpointTwitterImpl;

/**
 * @author amit
 *
 */
public class SocialChatFactory {
	
	public boolean supportsSocialChat(List<FeatureParam> params) {
		// Currently only twitter is supported
		return twitterEnabled(params);
	}

	private boolean twitterEnabled(List<FeatureParam> params) {
		for (FeatureParam param: params) {
			if (param.getName().equals("twitter.chat.enabled")) {
				return true;
			}
		}
		return false;
	}

	public SocialChatEndpoint addChatEndpoint(Feature feature) {
		if (!twitterEnabled(feature.getParams())) {
			throw new SocialChatException("No social chat properties were found");
		}		
		
		return new SocialChatEndpointTwitterImpl().init(feature);
	}

	public SocialChatEndpoint updateChatEndpoint(Feature feature) {
		// TODO
		return null;
	}

	public void removeChatEndpoint(String name) {
		// TODO Auto-generated method stub
		
	}
}
