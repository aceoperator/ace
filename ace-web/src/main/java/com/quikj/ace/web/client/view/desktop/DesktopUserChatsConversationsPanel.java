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
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.UserChatsPresenter;
import com.quikj.ace.web.client.view.UserConversation;

/**
 * @author beckie
 * 
 */
public class DesktopUserChatsConversationsPanel extends LayoutPanel {

	private CellTable<UserConversation> table;
	private ListDataProvider<UserConversation> dataProvider;
	private UserChatsPresenter presenter;
	private ScrollPanel tableScrollPanel;

	public DesktopUserChatsConversationsPanel() {
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

		table = new CellTable<UserConversation>(keyProvider);
		tableScrollPanel.setWidget(table);
		table.setSize("100%", "100%");

		// create the cell for the table handling
		final DesktopUserChatsConversationCell cell = new DesktopUserChatsConversationCell(
				this);

		// create the image column
		Column<UserConversation, String> imageColumn = new Column<UserConversation, String>(
				cell) {
			@Override
			public String getValue(UserConversation conversation) {
				return cell.renderImage(conversation);
			}
		};
		table.addColumn(imageColumn, "");

		// create the 'With' column
		Column<UserConversation, String> with = new Column<UserConversation, String>(
				cell) {
			@Override
			public String getValue(UserConversation conversation) {
				return (cell.renderOtherParty(conversation));
			}
		};
		table.addColumn(with, ApplicationController.getMessages()
				.DesktopUserChatsConversationsPanel_conversationWith());

		// create the start-time column
		Column<UserConversation, String> starttimeColumn = new Column<UserConversation, String>(
				cell) {
			@Override
			public String getValue(UserConversation conversation) {
				return (cell.renderStartTime(conversation));
			}
		};
		table.addColumn(starttimeColumn, ApplicationController.getMessages()
				.DesktopUserChatsConversationsPanel_startTime());

		// set selection parms
		final SingleSelectionModel<UserConversation> selectionModel = new SingleSelectionModel<UserConversation>(
				keyProvider);
		table.setSelectionModel(selectionModel);
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
					public void onSelectionChange(SelectionChangeEvent event) {
						UserConversation selected = selectionModel
								.getSelectedObject();
						if (selected != null
								&& table.getSelectionModel().isSelected(
										selected)) {
							table.getSelectionModel().setSelected(selected,
									false);
							selected.setChatEvent("");
							refreshConversations();
							presenter.showChat(selected.getSessionId());
						}
					}
				});

		// create the wrapped table
		List<UserConversation> conversations = new ArrayList<UserConversation>();
		dataProvider = new ListDataProvider<UserConversation>(conversations,
				keyProvider);
		dataProvider.addDataDisplay(table);
		table.setRowCount(conversations.size());
	}

	private static ProvidesKey<UserConversation> keyProvider = new ProvidesKey<UserConversation>() {
		public Object getKey(UserConversation conversation) {
			return (conversation == null) ? null : conversation.getSessionId();
		}
	};

	public UserConversation findConversation(long sessionId) {

		int index = dataProvider.getList().indexOf(
				new UserConversation(sessionId));
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

	public void addNewConversation(UserConversation conv) {
		dataProvider.getList().add(0, conv);
	}

	public void removeConversation(long sessionId) {
		dataProvider.getList().remove(new UserConversation(sessionId));
	}

	public void replaceConversationSessionId(long oldSessionId, long newSessionId) {
		int index = dataProvider.getList().indexOf(
				new UserConversation(oldSessionId));
		if (index == -1) {
			// TODO add a warning
			return;
		}

		UserConversation conv = dataProvider.getList().get(index);
		conv.setSessionId(newSessionId);
	}

	public void refreshConversations() {
		dataProvider.refresh();
	}

	public void setConversationEvent(long sessionId, String event) {
		UserConversation conv = findConversation(sessionId);
		if (conv != null) {
			conv.setChatEvent(event);
			refreshConversations();
		}
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
