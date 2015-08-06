/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;

/**
 * @author beckie
 * 
 */
public class UserMissedChat {

	private String image;
	private String caller;
	private String emailAddress;
	private String startTime;

	public UserMissedChat(String image, String caller, String emailAddress,
			Date startTime) {
		setImage(image);
		setCaller(caller);
		setEmailAddress(emailAddress);
		setStartTime(startTime);
	}

	public UserMissedChat(String startTime) {
		this.startTime = startTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof UserMissedChat) {
			if (this.getStartTime() == ((UserMissedChat) obj).getStartTime()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getStartTime().hashCode();
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		StringBuilder buf = new StringBuilder("<img src='");
		buf.append(image);
		buf.append("' border='0' align='middle'");
		buf.append(" width='");
		buf.append(Images.TINY_IMG_WIDTH);
		buf.append("' height='");
		buf.append(Images.TINY_IMG_HEIGHT);
		buf.append("'>");
		this.image = buf.toString();
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = DateTimeFormat.getFormat(
				ClientProperties.getInstance().getStringValue(
						ClientProperties.DATE_TIME_FORMAT,
						ClientProperties.DEFAULT_DATE_TIME_FORMAT)).format(
				startTime);
	}

}
