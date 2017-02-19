package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.cell.client.AbstractSafeHtmlCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.SimpleSafeHtmlRenderer;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.view.UserContact;
import com.quikj.ace.web.client.view.ViewUtils;

public class DesktopUserContactCell extends
		AbstractSafeHtmlCell<java.lang.String> {
	private static final int COLUMN_IMAGE = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_ACTION = 2;
	private static final int COLUMN_CALLCOUNT = 3;
	public static final int COLUMN_STATUS = 4;

	private DesktopUserContactsPanel parent;

	public DesktopUserContactCell(DesktopUserContactsPanel parent) {
		super(SimpleSafeHtmlRenderer.getInstance(), "mouseup");
		this.parent = parent;
	}

	@Override
	public boolean handlesSelection() {
		return true;
	}

	@Override
	public boolean resetFocus(Cell.Context context, Element parent, String value) {
		return true;
	}

	@Override
	public void onBrowserEvent(Cell.Context context, Element parent,
			String value, NativeEvent event, ValueUpdater<String> valueUpdater) {

		switch (context.getColumn()) {
		case COLUMN_ACTION:
			if (event.getType().equals("mouseup")) {
				this.parent.getPresenter().chatWith((String) context.getKey());
			}
			break;
		case COLUMN_IMAGE:
			break;
		case COLUMN_NAME:
			break;
		case COLUMN_CALLCOUNT:
			break;
		case COLUMN_STATUS:
			break;
		}
	}

	public String renderImage(UserContact contact) {

		String avatar = contact.getAvatar();
		if (avatar == null) {
			avatar = Images.USER_TINY;
		}
		return "<img src='" + avatar + "' align='middle' border='0'"
				+ " width='" + Images.TINY_IMG_WIDTH + "' height='"
				+ Images.TINY_IMG_HEIGHT + "'>";
	}
	
	public String renderStatus(UserContact contact) {
		boolean dnd = contact.isDnd();
		if (dnd) {			
			return "<img src='" + Images.OFFLINE_HIGHLIGHT_TINY + "' align='middle' border='0'"
					+ " width='" + Images.TINY_IMG_WIDTH + "' height='"
					+ Images.TINY_IMG_HEIGHT + "'>";
		}
		return "";
	}

	public String renderName(UserContact contact) {
		return ViewUtils.formatName(contact.getUser(), contact.getFullName());
	}

	public String renderAction(UserContact contact) {
		if (contact.isDnd()) {
			return "";
		}
		
		return "<button title='"
				+ ApplicationController.getMessages()
						.DesktopUserContactCell_clickToChat()
				+ "' class='gwt-Button' type='button'><img src='"
				+ Images.USER_CHATTING_TINY
				+ "' align='middle' border='0'></button>";
	}

	public String renderCallCount(UserContact contact) {
		return String.valueOf(contact.getCallCount());
	}

	@Override
	protected void render(com.google.gwt.cell.client.Cell.Context context,
			SafeHtml data, SafeHtmlBuilder sb) {
		if (data == null) {
			return;
		}

		UserContact contact = parent.findContact((String) context.getKey());
		if (contact == null) {
			return;
		}

		switch (context.getColumn()) {
		case COLUMN_ACTION:
			sb.appendHtmlConstant(renderAction(contact));
			break;
		case COLUMN_IMAGE:
			sb.appendHtmlConstant(renderImage(contact));
			break;
		case COLUMN_NAME:
			sb.appendHtmlConstant(renderName(contact));
			break;
		case COLUMN_CALLCOUNT:
			sb.appendHtmlConstant(renderCallCount(contact));
			break;
		case COLUMN_STATUS:
			sb.appendHtmlConstant(renderStatus(contact));
			break;
		}
	}
}
