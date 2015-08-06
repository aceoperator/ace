package com.quikj.ace.web.client.presenter;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.UserPanel;
import com.quikj.ace.web.client.view.desktop.DesktopUserPanel;

/**
 * @author beckie
 * 
 */
public class UserPanelPresenter {

	private static UserPanelPresenter instance;
	private UserPanel view;

	public UserPanelPresenter() {
		instance = this;
	}

	public static UserPanelPresenter getCurrentInstance() {
		return instance;
	}

	public void show(RegistrationResponseMessage rsp) {
		view = createView();
		view.setPresenter(this);

		new UserInfoPresenter();
		UserInfoPresenter.getCurrentInstance().show();

		new UserChatsPresenter();
		UserChatsPresenter.getCurrentInstance().show();

		new UserContactsPresenter();
		UserContactsPresenter.getCurrentInstance().show(rsp);

		new LogoutPresenter();
		LogoutPresenter.getCurrentInstance().show();

		view.showTab(0);

		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
	}

	public void dispose() {
		UserInfoPresenter.getCurrentInstance().dispose();
		UserChatsPresenter.getCurrentInstance().dispose();
		UserContactsPresenter.getCurrentInstance().dispose();
		LogoutPresenter.getCurrentInstance().dispose();
		instance = null;
	}

	private UserPanel createView() {
		return new DesktopUserPanel();
	}

	public void addNewPanel(String label, Widget w) {
		view.attach(w, label);
	}

	public void showChat(Widget w) {
		if (view.getWidgetCount() == 4) {
			view.insertWidget(3, ApplicationController.getMessages()
					.UserPanelPresenter_chat(), w);
		} else {
			view.replaceWidget(3, ApplicationController.getMessages()
					.UserPanelPresenter_chat(), w);
		}

		view.showTab(3);
	}

	public void removeChat(Widget w) {
		view.detachWidget(w);
	}

	public void showContacts() {
		view.showTab(2);
	}

	public void showConversations() {
		view.showTab(1);
	}

	public void highlightTab(int index, boolean highlight) {
		view.highlight(index, highlight, Images.USER_HIGHLIGHT_TINY);
	}

	public void highlightChatEvent(long sessionId, String event) {
		// unless the user is looking at the chat that had the event, highlight
		// either the chat tab if that chat is there or highlight the
		// conversations tab
		if (chatIsInChatTab(sessionId)) {
			if (view.getSelectedTab() != 3) {
				highlightTab(3, true);
			}

			return;
		}

		// chat tab not applicable, highlight the conversations tab
		highlightTab(1, true);
		UserChatsPresenter.getCurrentInstance().highlightChat(
				sessionId, event);

	}

	public ChatSessionPresenter getCurrentChatPresenter() {
		if (view.getWidgetCount() > 4) {
			return ((ChatPanel) view.getWidget(3)).getPresenter();
		}

		return null;
	}

	public boolean chatIsInChatTab(long sessionId) {
		ChatSessionPresenter presenter = getCurrentChatPresenter();
		if (presenter != null) {
			if (presenter.getSessionId() == sessionId) {
				return true;
			}
		}

		return false;
	}

	public boolean userIsViewingChat(long sessionId) {
		if (chatIsInChatTab(sessionId)) {
			if (view.getSelectedTab() == 3) {
				return true;
			}
		}

		return false;
	}

	public void tabSelected(Integer selectedItem) {
		if (selectedItem > 0) {
			highlightTab(selectedItem, false);
		}

		if (selectedItem == (view.getWidgetCount() - 1)) {
			LogoutPresenter.getCurrentInstance().processLogout();
		}
	}

	public void tabDeselected(Integer index) {
		if (index > 0) {
			highlightTab(index, false);
		}
	}

	public void setUserStatus(boolean value) {
		view.highlight(0, true, value == true ? Images.ONLINE_HIGHLIGHT_TINY
				: Images.OFFLINE_HIGHLIGHT_TINY);
	}

	public void lockEnabled(boolean enabled) {
		view.lockEnabled(enabled);
	}
}
