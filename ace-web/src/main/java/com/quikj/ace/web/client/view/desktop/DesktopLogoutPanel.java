/**
 * 
 */
package com.quikj.ace.web.client.view.desktop;

import com.google.gwt.user.client.ui.LayoutPanel;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.presenter.ConfirmationDialogPresenter;
import com.quikj.ace.web.client.presenter.ConfirmationListener;
import com.quikj.ace.web.client.presenter.LogoutPresenter;
import com.quikj.ace.web.client.view.LogoutPanel;

/**
 * @author beckie
 * 
 */
public class DesktopLogoutPanel extends LayoutPanel implements LogoutPanel {

	private LogoutPresenter presenter;

	public LogoutPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void setPresenter(LogoutPresenter presenter) {
		this.presenter = presenter;
	}

	public DesktopLogoutPanel() {
		super();
	}

	@Override
	public void logOut() {
		ConfirmationDialogPresenter.getInstance().show(
				ApplicationController.getMessages()
						.DesktopLogoutPanel_pleaseConfirm(),
				ApplicationController.getMessages()
						.DesktopLogoutPanel_areYouSure(), null,
				new ConfirmationListener() {

					@Override
					public void yes() {
						presenter.loggedOut();
					}

					@Override
					public void no() {
					}

					@Override
					public void cancel() {
						// Since the cancel button is not shown, we should not
						// come here
					}
				}, false);
	}

}
