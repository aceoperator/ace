/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import com.quikj.ace.web.client.view.ConfirmationDialog;
import com.quikj.ace.web.client.view.desktop.DesktopConfirmationDialog;

/**
 * @author amit
 * 
 */
public class ConfirmationDialogPresenter {
	private static ConfirmationDialogPresenter instance = null;
	private ConfirmationDialog view;
	private ConfirmationListener listener;

	private ConfirmationDialogPresenter() {
		view = createView();
		view.setPresenter(this);
	}

	public static ConfirmationDialogPresenter getInstance() {
		if (instance == null) {
			instance = new ConfirmationDialogPresenter();
		}

		return instance;
	}

	private ConfirmationDialog createView() {
		return new DesktopConfirmationDialog();
	}

	public void show(String title, String message, String icon,
			ConfirmationListener listener, boolean showCancel) {
		this.listener = listener;
		view.show(title, message, icon, showCancel);
	}

	public void hide() {
		view.hide();
	}

	public void yes() {
		listener.yes();
	}

	public void no() {
		listener.no();
	}

	public void cancel() {
		listener.cancel();

	}
}
