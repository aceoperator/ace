/**
 * 
 */
package com.quikj.ace.web.client.view;

import java.util.Date;
import java.util.List;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.presenter.ChatSessionPresenter;

/**
 * @author amit
 * 
 */
public interface ChatPanel {

	public static final boolean DEFAULT_SUPPRESS_COPYPASTE = false;
	public static final String TYPING = "<span style='color:green'>" + "<img src='" + Images.TYPING_TINY + "'></span>";

	public void appendToConveration(String from, long timeStamp, Object obj);

	public void setPresenter(ChatSessionPresenter presenter);

	public ChatSessionPresenter getPresenter();

	public void makeReadOnly();

	public void emoticonSelected(String url);

	public void attach(String me, CallPartyElement otherParty, CannedMessageElement[] cannedMessages, boolean operator,
			boolean showOtherPartyInfo, boolean hideAvatar);

	public String getTranscript();

	public void chatEnabled();

	public void chatDisabled();

	public void setOtherPartyInfo(List<CallPartyElement> otherParties);

	public void transferSetEnabled(boolean enabled);

	public void showTyping(String from, long timestamp);

	public void hideTyping();

	public void dispose();

	public static class Util {
		public static Widget formatChat(String from, long timeStamp, String message, String me, boolean smallSpace) {
			Date date = new Date(timeStamp);
			String formattedDate = DateTimeFormat.getFormat(ClientProperties.getInstance()
					.getStringValue(ClientProperties.TIME_FORMAT, ClientProperties.DEFAULT_TIME_FORMAT)).format(date);

			String spanStart = null;
			if (from == null) {
				spanStart = "<span class='ao-TranscriptMe'>";
				from = me;
			} else if (from.equals(ApplicationController.getMessages().DesktopChatPanel_system())) {
				spanStart = "<span class='ao-TranscriptSystem'>";
			} else {
				spanStart = "<span class='ao-TranscriptOther'>";
			}

			HTML html;
			if (smallSpace) {
				html = new HTML(
						spanStart + ApplicationController.getMessages().DesktopChatPanel_chatMessageFromAtTimestamp(
								from, formattedDate) + " >></span> " + message + "<br/>");
			} else {
				html = new HTML(
						spanStart + ApplicationController.getMessages().DesktopChatPanel_chatMessageFromAtTimestamp(
								from, formattedDate) + " >></span> <br/>" + message + "<p/>");
			}
			return html;
		}
	}
}
