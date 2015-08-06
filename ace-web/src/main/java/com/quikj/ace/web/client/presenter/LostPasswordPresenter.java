/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.HashMap;
import java.util.logging.Logger;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.web.client.AceOperatorService;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.view.LostPasswordPanel;
import com.quikj.ace.web.client.view.desktop.DesktopLostPasswordPanel1;
import com.quikj.ace.web.client.view.desktop.DesktopLostPasswordPanel2;

/**
 * @author becky
 * 
 */
public class LostPasswordPresenter {

	private LostPasswordPanel view;
	private Logger logger;
	private boolean fromLoginPage = true;
	private static LostPasswordPresenter instance;

	public LostPasswordPresenter() {
		logger = Logger.getLogger(getClass().getName());
		String startPage = ClientProperties.getInstance().getStringValue(
				ClientProperties.ONCLICK_START_PAGE, null);
		if (startPage != null) {
			fromLoginPage  = false;
		}
		instance = this;
	}

	public static LostPasswordPresenter getCurrentInstance() {
		return instance;
	}

	public void show() {
		showInitialView();
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

	public void finishedLostPassword(String message,
			MessageBoxPresenter.Severity sev) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostPasswordPresenter_resetPassword(), message, sev,
				new MessageCloseListener() {

					@Override
					public void closed() {
						if (fromLoginPage) {
							backToLoginPage();
						} else {
							showInitialView();
						}
					}
				}, true);
	}

	private void showInitialView() {
		view = new DesktopLostPasswordPanel1();
		view.setPresenter(this);
		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
	}

	private void showQuestionAnswerView(String name,
			HashMap<Integer, String> result) {
		view = new DesktopLostPasswordPanel2(name, result);
		view.setPresenter(this);
		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
	}

	public void userNameSubmitted(final String name, String captcha) {
		if (name == null || name.trim().length() == 0) {
			validationError(ApplicationController.getMessages()
					.LostPasswordPresenter_nameMissing());
			return;
		}

		if (captcha == null || captcha.trim().length() == 0) {
			validationError(ApplicationController.getMessages()
					.UserBusyEmailPresenter_imagePasscodeMissing());
			return;
		}

		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.getSecurityQuestions(name.trim(), captcha.trim(), null,
						new AsyncCallback<HashMap<Integer, String>>() {

							@Override
							public void onSuccess(
									HashMap<Integer, String> result) {
								if (result == null) {
									finishedLostPassword(
											ApplicationController
													.getMessages()
													.LostPasswordPresenter_noUserOrEmail(),
											MessageBoxPresenter.Severity.SEVERE);
								} else if (result.isEmpty()) {
									finishedLostPassword(
											ApplicationController
													.getMessages()
													.LostPasswordPresenter_noSecurityQuestions(),
											MessageBoxPresenter.Severity.SEVERE);
								} else {
									showQuestionAnswerView(name, result);
								}
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

	public void answersSubmitted(String name, HashMap<Integer, String> answers,
			String captcha) {
		for (String answer : answers.values()) {
			if (answer.length() == 0) {
				validationError(ApplicationController.getMessages()
						.LostPasswordPresenter_answersMissing());
				return;
			}
		}

		if (captcha == null || captcha.trim().length() == 0) {
			validationError(ApplicationController.getMessages()
					.UserBusyEmailPresenter_imagePasscodeMissing());
			return;
		}

		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.resetPassword(name.trim(), captcha.trim(), null, answers,
						ApplicationController.getInstance().getLocale(),
						new AsyncCallback<Void>() {

							@Override
							public void onSuccess(Void result) {
								finishedLostPassword(
										ApplicationController
												.getMessages()
												.LostPasswordPresenter_successfulPasswordReset(),
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
					.LostPasswordPresenter_databaseError());
		} else if ("unmatchedCapchaChars".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.UserBusyEmailPresenter_unmatchedCapchaChars());
		} else if ("imagePasscodeErrorTryAgain".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.Captcha_imagePasscodeErrorTryAgain());
		} else if ("resetPasswordEmailFailed".equals(error)) {
			finishedLostPassword(ApplicationController.getMessages()
					.LostPasswordPresenter_resetPasswordEmailFailed(),
					MessageBoxPresenter.Severity.SEVERE);
		} else if ("unmatchedAnswers".equals(error)) {
			errorResult(ApplicationController.getMessages()
					.LostPasswordPresenter_unmatchedAnswers());
		} else {
			errorResult(ApplicationController.getMessages()
					.LostPasswordPresenter_resetFailed(error));
		}

	}

	public void errorResult(String message) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostPasswordPresenter_warning(), message,
				MessageBoxPresenter.Severity.WARN, true);
	}

	public void validationError(String message) {
		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages()
						.LostPasswordPresenter_error(), message,
				MessageBoxPresenter.Severity.WARN, true);
	}

}
