/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.GroupElement;
import com.quikj.ace.messages.vo.talk.GroupMemberElement;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.AudioUtils;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.view.UserContact;
import com.quikj.ace.web.client.view.UserContactsPanel;
import com.quikj.ace.web.client.view.desktop.DesktopUserContactsPanel;

/**
 * @author beckie
 * 
 */
public class UserContactsPresenter {

	private UserContactsPanel view;
	private static UserContactsPresenter instance;

	public UserContactsPresenter() {
		instance = this;
	}

	public static UserContactsPresenter getCurrentInstance() {
		return instance;
	}

	public void show(RegistrationResponseMessage rsp) {

		view = createView();
		view.setPresenter(this);

		UserPanelPresenter.getCurrentInstance().addNewPanel(
				ApplicationController.getMessages()
						.UserContactsPresenter_contacts(), (Widget) view);

		List<UserContact> contacts = getInitialContactList(rsp.getGroup());
		view.addContacts(contacts);
	}

	private List<UserContact> getInitialContactList(GroupElement group) {
		List<UserContact> contacts = new ArrayList<UserContact>();

		if (group == null) {
			return contacts;
		}

		if (group.numElements() > 0) {
			AudioUtils.getInstance().play(AudioUtils.KNOCK);
		}

		for (int i = 0; i < group.numElements(); i++) {
			GroupMemberElement contact = group.elementAt(i);
			contacts.add(new UserContact(contact.getUser(), contact
					.getFullName(), contact.getCallCount(), contact.getAvatar()));
		}

		return contacts;
	}

	public void updateContacts(GroupElement contactsToUpdate) {
		if (contactsToUpdate == null) {
			return;
		}

		for (int i = 0; i < contactsToUpdate.numElements(); i++) {
			GroupMemberElement contact = contactsToUpdate.elementAt(i);

			switch (contact.getOperation()) {
			case GroupMemberElement.OPERATION_ADD_LIST: {
				AudioUtils.getInstance().play(AudioUtils.KNOCK);
				view.addContact(new UserContact(contact.getUser(), contact
						.getFullName(), contact.getCallCount(), contact
						.getAvatar()));
				UserPanelPresenter.getCurrentInstance().highlightTab(2, true);
				break;
			}
			case GroupMemberElement.OPERATION_MOD_LIST: {
				view.modifyContact(contact.getUser(), contact.getCallCount());
				break;
			}
			case GroupMemberElement.OPERATION_REM_LIST: {
				AudioUtils.getInstance().play(AudioUtils.OPEN);
				view.removeContact(contact.getUser());
				UserPanelPresenter.getCurrentInstance().highlightTab(2, true);
				break;
			}
			}
		}
	}

	public List<UserContact> getOnlineContacts() {
		return view.getOnlineContacts();
	}

	public void dispose() {
		view = null;
		instance = null;
	}

	private UserContactsPanel createView() {
		return new DesktopUserContactsPanel();
	}

	public void chatWith(String user) {
		new ChatSessionPresenter().setupOutboundChat(user, null, null, null,
				-1, null, false);
	}

	public void showErrorDialog(String title, String error) {
		MessageBoxPresenter.getInstance().show(title, error,
				(String) Images.CRITICAL_MEDIUM, true);
	}
}
