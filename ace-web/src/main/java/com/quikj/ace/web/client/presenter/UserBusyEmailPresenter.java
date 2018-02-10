/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.talk.MailElement;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;
import com.quikj.ace.web.client.AceOperatorService;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.view.UserBusyEmailPanel;

/**
 * @author amit
 * 
 */
public class UserBusyEmailPresenter {

	private UserBusyEmailPanel view;
	private Logger logger;
	private static UserBusyEmailPresenter instance;

	public UserBusyEmailPresenter() {
		logger = Logger.getLogger(getClass().getName());
		instance = this;
	}

	public static UserBusyEmailPresenter getCurrentInstance() {
		return instance;
	}

	public void informationSubmitted(String name, String email, String message,
			String captcha, String captchaType) {
		if (name == null || name.trim().isEmpty()) {
			MessageBoxPresenter.getInstance().show(
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_error(),
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_nameMissing(),
					MessageBoxPresenter.Severity.WARN, true);
			return;
		}

		if (message == null || message.trim().isEmpty()) {
			MessageBoxPresenter.getInstance().show(
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_error(),
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_messageMissing(),
					MessageBoxPresenter.Severity.WARN, true);
			return;
		}

		if (captcha == null || captcha.trim().isEmpty()) {
			MessageBoxPresenter.getInstance().show(
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_error(),
					ApplicationController.getMessages()
							.UserBusyEmailPresenter_imagePasscodeMissing(),
					MessageBoxPresenter.Severity.WARN, true);
			return;
		}

		SendMailRequestMessage mail = new SendMailRequestMessage();
		mail.setReplyRequired(false);
		MailElement melement = new MailElement();
		mail.setMailElement(melement);

		List<String> to = new ArrayList<>();
		to.add(ClientProperties.getInstance().getStringValue(
				ClientProperties.ALL_OPERATOR_BUSY_EMAIL, null));
		melement.setTo(to);

		melement.setSubject(ApplicationController.getMessages()
				.UserBusyEmailPresenter_emailSubjectOperatorBusy());

		StringBuffer buffer = new StringBuffer();
		buffer.append(ApplicationController.getMessages()
				.UserBusyEmailPresenter_from());
		buffer.append(": ");
		buffer.append(name.trim());
		buffer.append("\n");

		if (email != null && email.trim().length() > 0) {
			buffer.append(ApplicationController.getMessages()
					.UserBusyEmailPresenter_email());
			buffer.append(": ");
			buffer.append(email.trim());
			buffer.append("\n");
			melement.setFrom(email.trim());
		}

		buffer.append("\n");
		buffer.append(ApplicationController.getMessages()
				.UserBusyEmailPresenter_message());
		buffer.append(":\n");
		buffer.append(message);

		melement.setBody(buffer.toString());

		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.sendMail(mail, captcha.trim(), captchaType, new AsyncCallback<String>() {

					@Override
					public void onSuccess(String result) {
						if (result == null) {
							view.setEmailSentResult(null);
						} else {
							MessageBoxPresenter
									.getInstance()
									.show(ApplicationController.getMessages()
											.UserBusyEmailPresenter_warning(),
											ApplicationController
													.getMessages()
													.UserBusyEmailPresenter_unmatchedCapchaChars(),
											MessageBoxPresenter.Severity.WARN,
											true);
						}
					}

					@Override
					public void onFailure(Throwable caught) {
						view.setEmailSentResult(caught.getMessage());
					}
				});
		
		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - " + e.getMessage());
		}
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

	private UserBusyEmailPanel createView() {
		return (UserBusyEmailPanel)GWT.create(UserBusyEmailPanel.class);
	}
}
