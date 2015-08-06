package com.quikj.ace.web.client.view.desktop;

import java.util.HashSet;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.quikj.ace.web.client.ChatSessionInfo;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.view.UserConversation;

public class DesktopUserChatsConversationCell extends
		AbstractSafeHtmlCell<java.lang.String> {
	private static final int COLUMN_IMAGE = 0;
	private static final int COLUMN_OTHER_PARTY = 1;
	private static final int COLUMN_STARTTIME = 2;

	private DesktopUserChatsConversationsPanel parent;

	public DesktopUserChatsConversationCell(DesktopUserChatsConversationsPanel parent) {
		super(SimpleSafeHtmlRenderer.getInstance(), new HashSet<String>());
		this.parent = parent;
	}

	@Override
	public boolean handlesSelection() {
		return false;
	}

	@Override
	public boolean resetFocus(Cell.Context context, Element parent, String value) {
		return true;
	}

	public String renderImage(UserConversation conversation) {
		String avatar = parent.getPresenter().getOtherPartyAvatar(
				conversation.getSessionId());
		
		String value = Images.CRITICAL_TINY;
		if (parent.getPresenter()
				.getChatNumParties(conversation.getSessionId()) > 1) {
			value = Images.GROUP_TINY;
		} else if (parent.getPresenter()
				.getChatStatus(conversation.getSessionId())
				.equals(ChatSessionInfo.ChatStatus.CONNECTED)) {
			if (avatar != null) {
				value = avatar;
			} else {
				value = Images.USER_CHATTING_TINY;
			}
		} else if (parent.getPresenter()
				.getChatStatus(conversation.getSessionId())
				.equals(ChatSessionInfo.ChatStatus.SETUP_IN_PROGRESS)) {
			if (avatar != null) {
				value = avatar;
			} else {
				value = Images.USER_TINY;
			}
		}

		StringBuilder buf = new StringBuilder("<img src='");
		buf.append(value);
		buf.append("' border='0' align='middle'");
		buf.append(" width='");
		buf.append(Images.TINY_IMG_WIDTH);
		buf.append("' height='");
		buf.append(Images.TINY_IMG_HEIGHT);
		buf.append("'>");

		if (conversation.getChatEvent() != null
				&& conversation.getChatEvent().length() > 0) {
			buf.append("&nbsp&nbsp<img src='");
			buf.append(Images.USER_HIGHLIGHT_TINY);
			buf.append("' border='0' align='middle'>");
		}

		return buf.toString();
	}

	public String renderOtherParty(UserConversation conversation) {
		return parent.getPresenter().getChatOtherParties(
				conversation.getSessionId());
	}

	public String renderStartTime(UserConversation conversation) {
		return DateTimeFormat.getFormat(
				ClientProperties.getInstance().getStringValue(
						ClientProperties.DATE_TIME_FORMAT,
						ClientProperties.DEFAULT_DATE_TIME_FORMAT)).format(
				conversation.getStartTime());
	}

	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml data, SafeHtmlBuilder sb) {
		if (data == null) {
			return;
		}

		UserConversation conv = this.parent.findConversation((Long) context
				.getKey());
		if (conv == null) {
			return;
		}

		switch (context.getColumn()) {
		case COLUMN_IMAGE:
			sb.appendHtmlConstant(renderImage(conv));
			break;
		case COLUMN_OTHER_PARTY:
			sb.appendHtmlConstant(renderOtherParty(conv));
			break;
		case COLUMN_STARTTIME:
			sb.appendHtmlConstant(renderStartTime(conv));
			break;
		default:
			break;
		}
	}

}
