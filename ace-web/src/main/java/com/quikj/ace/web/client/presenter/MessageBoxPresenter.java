/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import com.google.gwt.core.client.GWT;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.view.MessageBox;

/**
 * @author amit
 *
 */
public class MessageBoxPresenter {
	private static MessageBoxPresenter instance = null;
	private MessageBox view;
	private MessageCloseListener listener;
	
	public enum Severity {
		INFO,
		WARN,
		SEVERE
	}
	
	private MessageBoxPresenter() {
		view = createView();
		view.setPresenter(this);
	}
	
	public static MessageBoxPresenter getInstance() {
		if (instance == null) {
			instance = new MessageBoxPresenter();
		}
		
		return instance;
	}
	
	private MessageBox createView() {
		return (MessageBox)GWT.create(MessageBox.class);
	}
	
	public void show(String title, String message, String icon, boolean enableClose) {
		show(title, message, icon, null, enableClose);
	}

	public void show(String title, String message, String icon,
			MessageCloseListener listener, boolean enableClose) {
		this.listener = listener;
		view.show(title, message, icon, enableClose);
	}
	
	public void show(String title, String message, Severity severity, boolean enableClose) {
		show(title, message, severity, null, enableClose);
	}

	public void show(String title, String message, Severity severity,
			MessageCloseListener listener, boolean enableClose) {
		String icon = null;
		if (severity == Severity.INFO) {
			icon = Images.INFO_MEDIUM;
		} else if (severity == Severity.WARN) {
			icon = Images.WARNING_MEDIUM;
		} else {
			icon = Images.CRITICAL_MEDIUM;
		}

		show(title, message, icon, listener, enableClose);
	}	
	
	public void hide() {
		view.hide();
	}

	public void close() {
		if (listener != null) {
			listener.closed();
			listener = null;
		}
	}
}
