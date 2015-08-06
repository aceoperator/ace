/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.SessionInfo;
import com.quikj.ace.web.client.view.VisitorInfoPanel;

/**
 * @author amit
 * 
 */
public class VisitorInfoPresenter {

	private static final int MAX_ADDNL_INFO_LENGTH = 2000;

	private VisitorInfoPanel view;
	private static VisitorInfoPresenter instance;

	public VisitorInfoPresenter() {
		instance = this;
	}

	public static VisitorInfoPresenter getCurrentInstance() {
		return instance;
	}

	public void infoSubmitted(String name, String email, String message) {
		if (name == null || name.trim().length() == 0) {
			MessageBoxPresenter.getInstance().show(
					ApplicationController.getMessages()
							.VisitorInfoPresenter_error(),
					ApplicationController.getMessages()
							.VisitorInfoPresenter_missingName(),
					MessageBoxPresenter.Severity.WARN, true);
			return;
		}

		boolean emailMandatory = false;
		if (!ClientProperties.getInstance().getBooleanValue(
				ClientProperties.VISITOR_EMAIL_HIDDEN, false)) {
			emailMandatory = ClientProperties.getInstance().getBooleanValue(
					ClientProperties.VISITOR_EMAIL_MANDATORY, true);
		}

		if (emailMandatory) {
			if (email == null || email.trim().length() == 0) {
				MessageBoxPresenter.getInstance().show(
						ApplicationController.getMessages()
								.VisitorInfoPresenter_error(),
						ApplicationController.getMessages()
								.VisitorInfoPresenter_missingEmail(),
						MessageBoxPresenter.Severity.WARN, true);
				return;
			}

			String[] addressTokens = email.trim().split("@");
			String domain = addressTokens[addressTokens.length - 1];
			if ((addressTokens.length < 2) || (domain.indexOf(".") == -1)) {
				MessageBoxPresenter.getInstance().show(
						ApplicationController.getMessages()
								.VisitorInfoPresenter_error(),
						ApplicationController.getMessages()
								.LostUsernamePresenter_addressInvalid(),
						MessageBoxPresenter.Severity.WARN, true);
				return;
			}
		}

		message = message.trim();
		if (message.length() > MAX_ADDNL_INFO_LENGTH) {
			message = message.substring(0, MAX_ADDNL_INFO_LENGTH);
		}

		CallPartyElement cp = new CallPartyElement();
		SessionInfo.getInstance().put(SessionInfo.USER_INFO, cp);

		cp.setFullName(name);
		cp.setEmail(email);
		cp.setLanguage(ApplicationController.getInstance().getLocale());
		cp.setComment(message);
		cp.setCookiesEnabled(Cookies.isCookieEnabled());

		ApplicationController.getInstance().connectToServer();
	}

	public void show() {
		view = createView();
		view.setPresenter(this);
		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
	}

	public void dispose() {
		MainPanelPresenter.getInstance().detachFromMainPanel();
		instance = null;
	}

	private VisitorInfoPanel createView() {
		return (VisitorInfoPanel)GWT.create(VisitorInfoPanel.class);
	}
}
