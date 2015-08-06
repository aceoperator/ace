package com.quikj.ace.web.client.view.desktop;

import java.util.HashSet;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.quikj.ace.web.client.view.UserMissedChat;

public class DesktopUserChatsMissedCell extends
		AbstractSafeHtmlCell<java.lang.String> {
	private static final int COLUMN_IMAGE = 0;
	private static final int COLUMN_CALLER = 1;
	private static final int COLUMN_EMAIL_ADDRESS = 2;
	private static final int COLUMN_STARTTIME = 3;

	private DesktopUserChatsMissedPanel parent;

	public DesktopUserChatsMissedCell(DesktopUserChatsMissedPanel parent) {
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

	public String renderImage(UserMissedChat chat) {
		return chat.getImage();
	}

	public String renderCaller(UserMissedChat chat) {
		return chat.getCaller();
	}

	public String renderEmailAddress(UserMissedChat chat) {
		return chat.getEmailAddress();
	}

	public String renderStartTime(UserMissedChat chat) {
		return chat.getStartTime();
	}

	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml data, SafeHtmlBuilder sb) {
		if (data == null) {
			return;
		}

		UserMissedChat chat = this.parent.findChat((String) context.getKey());
		if (chat == null) {
			return;
		}

		switch (context.getColumn()) {
		case COLUMN_IMAGE:
			sb.appendHtmlConstant(renderImage(chat));
			break;
		case COLUMN_CALLER:
			sb.appendHtmlConstant(renderCaller(chat));
			break;
		case COLUMN_EMAIL_ADDRESS:
			sb.appendHtmlConstant(renderEmailAddress(chat));
			break;
		case COLUMN_STARTTIME:
			sb.appendHtmlConstant(renderStartTime(chat));
			break;
		default:
			break;
		}
	}

}
