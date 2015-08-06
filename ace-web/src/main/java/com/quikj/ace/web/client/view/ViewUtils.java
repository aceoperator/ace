/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.List;

import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;

/**
 * @author beckie
 * 
 */
public class ViewUtils {

	private static final int MAX_COMMENT_LENGTH = 255;

	public static String formatName(CallPartyElement userInfo) {
		return formatName(userInfo.getName(), userInfo.getFullName());
	}

	public static String formatName(String userName, String fullName) {

		StringBuffer buffer = new StringBuffer();
		if (fullName != null) {
			buffer.append(fullName);
		}

		boolean hideIds = ClientProperties.getInstance().getBooleanValue(
				ClientProperties.HIDE_LOGIN_IDS, false);

		if (userName != null && !hideIds) {
			if (fullName != null) {
				buffer.append(" (");
			}

			buffer.append(userName);

			if (fullName != null) {
				buffer.append(")");
			}
		}

		if (buffer.length() == 0) {
			buffer.append(ApplicationController.getMessages()
					.ChatSessionPresenter_privateParty());
		}

		return buffer.toString();
	}

	public static String formatUserInfo(CallPartyElement party) {
		return formatUserInfo(party, "<br/>", true);
	}

	public static String formatUserInfo(CallPartyElement party,
			boolean truncateAdditionalInfo) {
		return formatUserInfo(party, "<br/>", truncateAdditionalInfo);
	}

	private static String formatUserInfo(CallPartyElement party,
			String lineSeparator, boolean truncateAdditionalInfo) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(ApplicationController.getMessages().ViewUtils_name()
				+ ": ");
		buffer.append(formatName(party));

		if (party.getEmail() != null) {
			buffer.append(lineSeparator);
			buffer.append(ApplicationController.getMessages().ViewUtils_email()
					+ ": ");
			buffer.append(party.getEmail());
		}

		if (party.isCookiesEnabled() && party.getEndUserCookie() != null) {
			buffer.append(lineSeparator);
			buffer.append(ApplicationController.getMessages()
					.ViewUtils_cookie() + ": ");
			buffer.append(party.getEndUserCookie());
		} else if (party.getIpAddress() != null) {
			buffer.append(lineSeparator);
			buffer.append(ApplicationController.getMessages()
					.ViewUtils_ipAddress() + ": ");
			buffer.append(party.getIpAddress());
		}

		if (party.getComment() != null) {
			buffer.append(lineSeparator);
			buffer.append(ApplicationController.getMessages()
					.ViewUtils_additionalInfo() + ": ");
			String comment = party.getComment();

			if (truncateAdditionalInfo) {
				if (comment.length() > MAX_COMMENT_LENGTH) {
					comment = comment.substring(0, MAX_COMMENT_LENGTH) + "...";
				}
			}
			buffer.append(comment);
		}

		return buffer.toString();
	}

	public static String parseName(String formattedName) {

		int start = formattedName.indexOf("(");
		if (start < 0) {
			return formattedName;
		}

		int end = formattedName.indexOf(")", start);
		if (end < 0) {
			return formattedName.substring(start + 1);
		}

		return formattedName.substring(start + 1, end);
	}

	public static String formatNames(List<CallPartyElement> users) {
		StringBuffer buffer = new StringBuffer();

		int count = 0;
		for (CallPartyElement user : users) {
			if (count++ > 0) {
				buffer.append("<br>");
			}
			buffer.append(formatName(user));
		}

		return buffer.toString();
	}
}
