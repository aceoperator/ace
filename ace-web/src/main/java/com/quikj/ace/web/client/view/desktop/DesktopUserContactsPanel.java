/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import java.util.ArrayList;
import java.util.Collections;
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
import com.quikj.ace.web.client.presenter.UserContactsPresenter;
import com.quikj.ace.web.client.view.UserContact;
import com.quikj.ace.web.client.view.UserContactsPanel;

/**
 * @author beckie
 * 
 */
public class DesktopUserContactsPanel extends LayoutPanel implements
		UserContactsPanel {

	private CellTable<UserContact> table;
	private ListDataProvider<UserContact> dataProvider;
	private UserContactsPresenter presenter;
	private ScrollPanel tableScrollPanel;

	public DesktopUserContactsPanel() {
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

		table = new CellTable<UserContact>(keyProvider);
		tableScrollPanel.setWidget(table);
		table.setSize("100%", "100%");

		// create the cell for the table handling
		final DesktopUserContactCell cell = new DesktopUserContactCell(this);

		// create the image column
		Column<UserContact, String> imageColumn = new Column<UserContact, String>(
				cell) {
			@Override
			public String getValue(UserContact contact) {
				return cell.renderImage(contact);
			}
		};
		table.addColumn(imageColumn, "");

		// create the name column
		Column<UserContact, String> nameColumn = new Column<UserContact, String>(
				cell) {
			@Override
			public String getValue(UserContact contact) {
				return (cell.renderName(contact));
			}
		};
		table.addColumn(nameColumn, ApplicationController.getMessages()
				.DesktopUserContactsPanel_contact());

		// create the action column
		Column<UserContact, String> actionColumn = new Column<UserContact, String>(
				cell) {
			@Override
			public String getValue(UserContact contact) {
				return (cell.renderAction(contact));
			}
		};
		table.addColumn(actionColumn, ApplicationController.getMessages()
				.DesktopUserContactsPanel_action());

		// create the callcount column
		Column<UserContact, String> callcountColumn = new Column<UserContact, String>(
				cell) {
			@Override
			public String getValue(UserContact contact) {
				return (cell.renderCallCount(contact));
			}
		};
		table.addColumn(callcountColumn, ApplicationController.getMessages()
				.DesktopUserContactsPanel_chatCount());

		// set selection parms
		NoSelectionModel<UserContact> selectionModel = new NoSelectionModel<UserContact>(
				keyProvider);
		table.setSelectionModel(selectionModel);

		// fill up the table with data
		List<UserContact> contacts = new ArrayList<UserContact>();
		dataProvider = new ListDataProvider<UserContact>(contacts, keyProvider);
		dataProvider.addDataDisplay(table);
		table.setRowCount(contacts.size());

	}

	private static ProvidesKey<UserContact> keyProvider = new ProvidesKey<UserContact>() {
		public Object getKey(UserContact contact) {
			return (contact == null) ? null : contact.getUser();
		}
	};

	public UserContact findContact(String user) {

		int index = dataProvider.getList().indexOf(new UserContact(user));
		if (index == -1) {
			return null;
		}

		return dataProvider.getList().get(index);
	}

	@Override
	public List<UserContact> getOnlineContacts() {
		return dataProvider.getList();
	}

	@Override
	public void addContact(UserContact contact) {
		List<UserContact> list = dataProvider.getList();

		list.add(contact);
		Collections.sort(list);
	}

	@Override
	public void addContacts(List<UserContact> contacts) {
		List<UserContact> list = dataProvider.getList();

		list.addAll(contacts);
		Collections.sort(list);
	}

	@Override
	public void modifyContact(String user, int callCount) {
		UserContact contact = findContact(user);
		if (contact != null) {
			contact.setCallCount(callCount);
			dataProvider.refresh();
		}
	}

	@Override
	public void removeContact(String user) {
		dataProvider.getList().remove(new UserContact(user));
	}

	@Override
	public void setPresenter(UserContactsPresenter presenter) {
		this.presenter = presenter;
	}

	public UserContactsPresenter getPresenter() {
		return presenter;
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
