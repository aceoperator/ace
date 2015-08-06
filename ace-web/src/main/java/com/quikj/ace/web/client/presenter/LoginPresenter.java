/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.RegistrationRequestMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.AudioUtils;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.comm.ResponseListener;
import com.quikj.ace.web.client.view.LoginPanel;
import com.quikj.ace.web.client.view.desktop.DesktopLoginPanel;

/**
 * @author amit
 * 
 */
public class LoginPresenter {

	private LoginPanel view;
	private String user;
	private String password;

	private static LoginPresenter instance;

	public LoginPresenter() {
		instance = this;
	}

	public static LoginPresenter getCurrentInstance() {
		return instance;
	}

	public void processLogin(String user, String password) {
		if (user.length() == 0 || password.length() == 0) {
			MessageBoxPresenter.getInstance().show(
					ApplicationController.getMessages()
							.LoginPresenter_loginFailed(),
					ApplicationController.getMessages()
							.LoginPresenter_namePasswordRequired(),
					Images.WARNING_MEDIUM, true);
			return;
		}

		// Initiate the connection
		ApplicationController.getInstance().connectToServer();

		this.user = user;
		this.password = password;
	}

	public void connected() {
		RegistrationRequestMessage request = new RegistrationRequestMessage();
		request.setUserName(user);
		request.setPassword(password);
		request.setLanguage(ApplicationController.getInstance().getLocale());

		MessageBoxPresenter.getInstance().show(
				ApplicationController.getMessages().LoginPresenter_login(),
				ApplicationController.getMessages().LoginPresenter_loggingIn()
						+ " ...", Images.LOGIN_MEDIUM, false);
		CommunicationsFactory.getServerCommunications().sendRequest(request,
				Message.CONTENT_TYPE_XML, false, 100000L,
				new ResponseListener() {

					@Override
					public void timeoutOccured(int requestId) {
						CommunicationsFactory.getServerCommunications().disconnect();
						MessageBoxPresenter.getInstance().show(
								ApplicationController.getMessages()
										.LoginPresenter_login(),
								ApplicationController.getMessages()
										.LoginPresenter_noServerResponse(),
								Images.DISCONNECTED_MEDIUM, true);
					}

					@Override
					public void responseReceived(int requestId,
							String contentType, ResponseMessage message) {

						if (message.getStatus() == ResponseMessage.OK) {
							RegistrationResponseMessage rsp = (RegistrationResponseMessage) message
									.getMessage();
							ApplicationController.getInstance().loggedIn(rsp);
							MessageBoxPresenter.getInstance().hide();
						} else {
							ApplicationController.getInstance()
									.disconnectExpected();
							MessageBoxPresenter.getInstance().show(
									ApplicationController.getMessages()
											.LoginPresenter_login(),
									ApplicationController.getMessages()
											.LoginPresenter_loginFailed()
											+ ": "
											+ message.getReason()
											+ " ("
											+ message.getStatus() + ")",
									(String) Images.WARNING_MEDIUM, true);
							view.reset();
						}
					}
				});
	}

	public void show() {
		view = createView();
		view.setPresenter(this);

		MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
		AudioUtils.getInstance().play(AudioUtils.WELCOME);
	}

	public void dispose() {
		MainPanelPresenter.getInstance().detachFromMainPanel();
		instance = null;
	}

	private LoginPanel createView() {
		return new DesktopLoginPanel();
	}

	public void resetLostPassword() {
		new LostPasswordPresenter();
		LostPasswordPresenter.getCurrentInstance().show();
	}

	public void recoverLostUsername() {
		new LostUsernamePresenter();
		LostUsernamePresenter.getCurrentInstance().show();
	}
}
