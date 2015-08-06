package com.quikj.ace.web.client.view.desktop;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.ProvidesKey;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.UserChatsPresenter;
import com.quikj.ace.web.client.view.UserMissedChat;

/**
 * @author beckie
 * 
 */
public class DesktopUserChatsMissedPanel extends LayoutPanel {

	private CellTable<UserMissedChat> table;
	private ListDataProvider<UserMissedChat> dataProvider;
	private UserChatsPresenter presenter;
	private ScrollPanel tableScrollPanel;

	public DesktopUserChatsMissedPanel() {
		super();
		setSize("100%", "100%");

		tableScrollPanel = new ScrollPanel();
		tableScrollPanel.setTouchScrollingDisabled(false);
		tableScrollPanel.setAlwaysShowScrollBars(true);
		add(tableScrollPanel);
		tableScrollPanel.setSize("100%", "100%");
		setWidgetVerticalPosition(tableScrollPanel, Layout.Alignment.BEGIN);
		setWidgetLeftRight(tableScrollPanel, 0, Style.Unit.PCT, 10,
				Style.Unit.PX);

		table = new CellTable<UserMissedChat>(keyProvider);
		tableScrollPanel.setWidget(table);
		table.setSize("100%", "100%");

		// create the cell for the table handling
		final DesktopUserChatsMissedCell cell = new DesktopUserChatsMissedCell(
				this);

		// create the image column
		Column<UserMissedChat, String> imageColumn = new Column<UserMissedChat, String>(
				cell) {
			@Override
			public String getValue(UserMissedChat chat) {
				return cell.renderImage(chat);
			}
		};
		table.addColumn(imageColumn, "");

		// create the caller name column
		Column<UserMissedChat, String> caller = new Column<UserMissedChat, String>(
				cell) {
			@Override
			public String getValue(UserMissedChat chat) {
				return (cell.renderCaller(chat));
			}
		};
		table.addColumn(caller, ApplicationController.getMessages()
				.DesktopUserChatsMissedPanel_userName());

		// create the caller email column
		Column<UserMissedChat, String> emailAddress = new Column<UserMissedChat, String>(
				cell) {
			@Override
			public String getValue(UserMissedChat chat) {
				return (cell.renderEmailAddress(chat));
			}
		};
		table.addColumn(emailAddress, ApplicationController.getMessages()
				.DesktopUserChatsMissedPanel_emailAddress());

		// create the start-time column
		Column<UserMissedChat, String> startTime = new Column<UserMissedChat, String>(
				cell) {
			@Override
			public String getValue(UserMissedChat chat) {
				return (cell.renderStartTime(chat));
			}
		};
		table.addColumn(startTime, ApplicationController.getMessages()
				.DesktopUserChatsMissedPanel_startTime());

		// set selection parms
		final NoSelectionModel<UserMissedChat> selectionModel = new NoSelectionModel<UserMissedChat>(
				keyProvider);
		table.setSelectionModel(selectionModel);

		// create the wrapped table
		List<UserMissedChat> chats = new ArrayList<UserMissedChat>();
		dataProvider = new ListDataProvider<UserMissedChat>(chats, keyProvider);
		dataProvider.addDataDisplay(table);
		table.setRowCount(chats.size());
	}

	public void refreshChats() {
		dataProvider.refresh();
	}

	private static ProvidesKey<UserMissedChat> keyProvider = new ProvidesKey<UserMissedChat>() {
		public Object getKey(UserMissedChat chat) {
			return (chat == null) ? null : chat.getStartTime();
		}
	};

	public UserMissedChat findChat(String startTime) {

		int index = dataProvider.getList().indexOf(
				new UserMissedChat(startTime));
		if (index == -1) {
			return null;
		}

		return dataProvider.getList().get(index);
	}

	public void setPresenter(UserChatsPresenter presenter) {
		this.presenter = presenter;
	}

	public UserChatsPresenter getPresenter() {
		return presenter;
	}

	public void addMissedChat(UserMissedChat chat, int maxListSize) {
		List<UserMissedChat> list = dataProvider.getList();

		list.add(0, chat);
		if (list.size() > maxListSize) {
			list.remove(maxListSize);
		}

		refreshChats();
	}

	@Override
	public void onResize() {
		super.onResize();
		adjustScrollHeight();
	}

	private void adjustScrollHeight() {
		tableScrollPanel.onResize();
	}

}
