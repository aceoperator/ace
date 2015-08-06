/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.logging.Logger;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.AceOperatorService;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.view.LostUsernamePanel;
import com.quikj.ace.web.client.view.desktop.DesktopLostUsernamePanel;

/**
 * @author becky
 * 
 */
public class LostUsernamePresenter {

	private LostUsernamePanel view;
	private Logger logger;
	private boolean fromLoginPage = true;
	private static LostUsernamePresenter instance;

	public LostUsernamePresenter() {
		logger = Logger.getLogger(getClass().getName());
		String startPage = ClientProperties.getInstance().getStringValue(
				ClientProperties.ONCLICK_START_PAGE, null);
		if (startPage != null) {
			fromLoginPage = false;
		}
		instance = this;
	}

	public static LostUsernamePresenter getCurrentInstance() {
		return instance;
	}

	public void show() {
		view = createView();
		view.setPresenter(this);

		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
	}

	private DesktopLostUsernamePanel createView() {
		return new DesktopLostUsernamePanel();
	}

	public void dispose() {
		MainPanelPresenter.getInstance().detachFromMainPanel();
		instance = null;
		view = null;
	}

	public void backToLoginPage() {
		this.dispose();
		LoginPresenter.getCurrentInstance().show();
	}

	public void finishedLostUsername(String message,
			MessageBoxPresenter.Severity sev) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostUsernamePresenter_findUsername(), message, sev,
				new MessageCloseListener() {

					@Override
					public void closed() {
						if (fromLoginPage) {
							backToLoginPage();
						} else {
							view.reset();
						}
					}
				}, true);
	}

	public void emailAddressSubmitted(final String address, String captcha) {
		if (address == null || address.trim().length() == 0) {
			validationError(ApplicationController.getMessages()
					.LostUsernamePresenter_addressMissing());
			return;
		}

		if (captcha == null || captcha.trim().length() == 0) {
			validationError(ApplicationController.getMessages()
					.UserBusyEmailPresenter_imagePasscodeMissing());
			return;
		}

		String[] addressTokens = address.trim().split("@");
		String domain = addressTokens[addressTokens.length - 1];
		if ((addressTokens.length < 2) || (domain.indexOf(".") == -1)) {
			validationError(ApplicationController.getMessages()
					.LostUsernamePresenter_addressInvalid());
			return;
		}

		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.recoverLostUsername(address.trim(), captcha.trim(), null,
						ApplicationController.getInstance().getLocale(),
						new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								finishedLostUsername(
										ApplicationController
												.getMessages()
												.LostUsernamePresenter_successfulUsernameRecovery(),
										MessageBoxPresenter.Severity.INFO);
							}

							@Override
							public void onFailure(Throwable caught) {
								handleError(caught);
							}
						});

		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
	}

	private void handleError(Throwable caught) {
		String error = caught.getMessage();
		if ("databaseError".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.LostUsernamePresenter_databaseError());
		} else if ("unmatchedCapchaChars".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.UserBusyEmailPresenter_unmatchedCapchaChars());
		} else if ("imagePasscodeErrorTryAgain".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.Captcha_imagePasscodeErrorTryAgain());
		} else if ("usernameEmailFailed".equals(error)) {
			finishedLostUsername(ApplicationController.getMessages()
					.LostUsernamePresenter_usernameEmailFailed(),
					MessageBoxPresenter.Severity.SEVERE);
		} else if ("userNotFound".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.LostUsernamePresenter_userNotFound());
		} else {
			errorResult(ApplicationController.getMessages()
					.LostUsernamePresenter_findFailed(error));
		}
	}

	public void errorResult(String message) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostUsernamePresenter_warning(), message,
				MessageBoxPresenter.Severity.WARN, true);
	}

	public void validationError(String message) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostUsernamePresenter_error(), message,
				MessageBoxPresenter.Severity.WARN, true);
	}

}
