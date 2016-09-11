/**
 * 
 */
package com.quikj.ace.social;

import java.util.Date;

/**
 * @author amit
 *
 */
public class SocialMessage {
	private String screenName;
	private String fullName;
	private Date postedOn;
	private Media media;

	public SocialMessage(String screenName, String fullName, Date postedOn,
			Media media) {
		this.screenName = screenName;
		this.fullName = fullName;
		this.postedOn = postedOn;
		this.media = media;
	}
	public String getScreenName() {
		return screenName;
	}
	public String getFullName() {
		return fullName;
	}
	public Date getPostedOn() {
		return postedOn;
	}
	public Media getMedia() {
		return media;
	} 
}
