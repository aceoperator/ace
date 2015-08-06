/**
 * 
 */
package com.quikj.ace.web.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ClosingEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.app.WebMessage;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.ConferenceInformationMessage;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.GroupActivityMessage;
import com.quikj.ace.messages.vo.talk.RTPMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.messages.vo.talk.ReplaceSessionMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.UserToUserMessage;
import com.quikj.ace.web.client.ChatSessionInfo.ChatStatus;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.comm.RequestListener;
import com.quikj.ace.web.client.presenter.ChatSessionPresenter;
import com.quikj.ace.web.client.presenter.LoginPresenter;
import com.quikj.ace.web.client.presenter.LostPasswordPresenter;
import com.quikj.ace.web.client.presenter.LostUsernamePresenter;
import com.quikj.ace.web.client.presenter.MainPanelPresenter;
import com.quikj.ace.web.client.presenter.MessageBoxPresenter;
import com.quikj.ace.web.client.presenter.UserBusyEmailPresenter;
import com.quikj.ace.web.client.presenter.UserContactsPresenter;
import com.quikj.ace.web.client.presenter.UserPanelPresenter;
import com.quikj.ace.web.client.presenter.VisitorInfoPresenter;
import com.quikj.ace.web.client.theme.ThemeFactory;

/**
 * @author amit
 * 
 */
public class ApplicationController {

	private static final String DEFAULT_LOCALE = "en_US";
	private static final String DEFAULT_TYPE = ClientProperties.OPERATOR_TYPE;

	private static ApplicationController instance = null;
	private RootPanel rootPanel;
	private Logger logger;
	private String locale;
	private boolean connected = false;
	private List<RootPanelResizeListener> rootPanelResizeListeners = new ArrayList<RootPanelResizeListener>();
	private boolean operator = true;
	private long timeAdjustment = 0L;
	private boolean disconnectExpected;
	private AceMessages messages;

	private ApplicationController() {
		logger = Logger.getLogger(getClass().getName());
		initLocale();
		messages = (AceMessages) GWT.create(AceMessages.class);
		ThemeFactory.initTheme();
		initRootPanel();
		initCommunications();
		initOnWindowCloseAction();

		initEmailTranscriptInfo();
	}

	private void initEmailTranscriptInfo() {
		EmailTranscriptInfo email = new EmailTranscriptInfo();
		logger.info("Email transcript enabled : " + email.isEmailTranscript());
		SessionInfo.getInstance().put(SessionInfo.EMAIL_TRANSCRIPT_INFO, email);
	}

	private void initLocale() {
		locale = ClientProperties.getInstance().getStringValue(
				ClientProperties.LOCALE, DEFAULT_LOCALE);
	}

	private void initCommunications() {
		CommunicationsFactory.getServerCommunications().setRequestListener(
				new RequestListener() {
					@Override
					public void requestReceived(int reqId, String contentType,
							RequestMessage req) {
						WebMessage msg = req.getMessage();
						if (msg instanceof SetupRequestMessage) {
							new ChatSessionPresenter().processChatRequest(
									reqId, contentType,
									(SetupRequestMessage) msg);
						} else if (msg instanceof DisconnectMessage) {
							ChatSessionPresenter
									.disconnectReceived((DisconnectMessage) msg);
						} else if (msg instanceof RTPMessage) {
							ChatSessionPresenter.rtpReceived((RTPMessage) msg);
						} else if (msg instanceof ReplaceSessionMessage) {
							ReplaceSessionMessage rep = (ReplaceSessionMessage) msg;
							processReplace(rep);
						} else if (msg instanceof ConferenceInformationMessage) {
							ConferenceInformationMessage conf = (ConferenceInformationMessage) msg;
							processConferenceInfo(conf);
						} else if (msg instanceof GroupActivityMessage) {
							UserContactsPresenter.getCurrentInstance()
									.updateContacts(
											((GroupActivityMessage) msg)
													.getGroup());
						} else if (msg instanceof UserToUserMessage) {
							logger.warning("Unsupported UserToUserMessage message received, going to discard");
						} else {
							logger.warning("Unsupported message "
									+ msg.getClass().getName()
									+ " received, going to discard");
						}
					}

					@Override
					public void disconnected() {
						if (connected) {
							if (!disconnectExpected) {
								MessageBoxPresenter
										.getInstance()
										.show(messages
												.ApplicationController_disconnectedFromServer()
												+ "!",
												messages.ApplicationController_serverDisconnected(),
												MessageBoxPresenter.Severity.SEVERE,
												true);
							}

							dispose();
							disconnectExpected = false;
						}
					}

					@Override
					public void connected() {
						connected = true;
						if (operator) {
							LoginPresenter.getCurrentInstance().connected();
						} else {
							VisitorInfoPresenter.getCurrentInstance().dispose();
							new ChatSessionPresenter().setupOutboundChat(
									ClientProperties.getInstance()
											.getStringValue(
													ClientProperties.GROUP,
													null), null, null, null,
									-1L, null, false);
						}
					}
				});
	}

	private void initRootPanel() {
		String rootPanelId = ClientProperties.getInstance().getStringValue(
				ClientProperties.ROOT_PANEL, "");
		if (rootPanelId.length() == 0) {
			rootPanel = RootPanel.get("aceoperator");
		} else if (rootPanelId.equals("none")) {
			rootPanel = RootPanel.get();
		} else {
			rootPanel = RootPanel.get(rootPanelId);
		}

		adjustBorders();
		adjustRootPanelSize();

		Window.addResizeHandler(new ResizeHandler() {

			@Override
			public void onResize(ResizeEvent event) {
				adjustRootPanelSize();
			}
		});
	}

	private void adjustBorders() {
		if ((MainPanelPresenter.getInstance().getBrowserType()
				.equals(MainPanelPresenter.BROWSER_MOBILE) || MainPanelPresenter
				.getInstance().getBrowserType()
				.equals(MainPanelPresenter.BROWSER_TABLET))
				&& ClientProperties.getInstance().getBooleanValue(
						ClientProperties.REMOVE_MOBILE_BORDER, true)) {
			Element header = DOM.getElementById("header");
			if (header != null) {
				header.removeFromParent();
			}

			Element footer = DOM.getElementById("footer");
			if (footer != null) {
				footer.removeFromParent();
			}
		}
	}

	private void adjustRootPanelSize() {
		int height = Window.getClientHeight();
		int width = Window.getClientWidth();

		Element header = DOM.getElementById("header");
		if (header != null) {
			height -= header.getOffsetHeight();
		}

		Element footer = DOM.getElementById("footer");
		if (footer != null) {
			height -= footer.getOffsetHeight();
		}

		if (MainPanelPresenter.getInstance().getBrowserType()
				.equals(MainPanelPresenter.BROWSER_DESKTOP)) {
			height -= 20;
		}

		if (height >= 0) {
			RootPanelResizeListener.Layout layout = computeLayout(height, width);
			rootPanel.setSize("100%", height + "px");

			for (RootPanelResizeListener listener : rootPanelResizeListeners) {
				listener.onResize(rootPanel.getOffsetWidth(),
						rootPanel.getOffsetHeight(), layout);
			}
		}
	}

	public RootPanelResizeListener.Layout getLayout() {
		int height = Window.getClientHeight();
		int width = Window.getClientWidth();

		return computeLayout(height, width);
	}

	private RootPanelResizeListener.Layout computeLayout(int height, int width) {
		RootPanelResizeListener.Layout layout = RootPanelResizeListener.Layout.LANDSCAPE;
		if (height > width) {
			layout = RootPanelResizeListener.Layout.POTRAIT;
		}
		return layout;
	}

	public static ApplicationController getInstance() {
		if (instance == null) {
			instance = new ApplicationController();
		}

		return instance;
	}

	public static AceMessages getMessages() {
		return getInstance().getAceMessages();
	}

	private AceMessages getAceMessages() {
		return messages;
	}

	public RootPanel getRootPanel() {
		return rootPanel;
	}

	public void setRootPanel(RootPanel rootPanel) {
		this.rootPanel = rootPanel;
	}

	public void launch() {
		MainPanelPresenter.getInstance().show();
		initAudio();

		String type = ClientProperties.getInstance().getStringValue(
				ClientProperties.TYPE, DEFAULT_TYPE);

		if (type.equals(ClientProperties.OPERATOR_TYPE)) {
			// Operator
			operator = true;
			new LoginPresenter();

			String startPage = ClientProperties.getInstance().getStringValue(
					ClientProperties.ONCLICK_START_PAGE, "");
			if (startPage.equals("lost-username")) {
				new LostUsernamePresenter().show();
			} else if (startPage.equals("lost-password")) {
				new LostPasswordPresenter().show();
			} else {
				LoginPresenter.getCurrentInstance().show();
			}

		} else if (type.equals(ClientProperties.VISITOR_TYPE)) {
			// Visitor
			operator = false;

			String group = ClientProperties.getInstance().getStringValue(
					ClientProperties.GROUP, null);
			if (group == null) {
				MessageBoxPresenter
						.getInstance()
						.show(messages.ApplicationController_error(),
								messages.ApplicationController_errorEncountered()
										+ ": "
										+ messages
												.ApplicationController_noGroupSpecified()
										+ ". "
										+ messages
												.ApplicationController_contactAdministrator(),
								MessageBoxPresenter.Severity.SEVERE, true);
				return;
			}

			RequestBuilder builder = AceOperatorService.Util.getInstance()
					.allOperatorBusy(group, new AsyncCallback<Boolean>() {

						@Override
						public void onFailure(Throwable caught) {
							MessageBoxPresenter
									.getInstance()
									.show(messages
											.ApplicationController_error()
											+ "!",
											messages.ApplicationController_errorReportedAccessingGroup()
													+ ". "
													+ caught.getMessage(),
											MessageBoxPresenter.Severity.SEVERE,
											true);
							logger.severe("Exception "
									+ caught.getClass().getName()
									+ " occured - " + caught.getMessage());
							caught.printStackTrace();
						}

						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								processAllOperatorBusy();
							} else if (ClientProperties
									.getInstance()
									.getStringValue(
											ClientProperties.ONCLICK_START_PAGE,
											"").equals("busy")) {
								processAllOperatorBusy();
							} else {
								// Operators available
								VisitorInfoPresenter visitor = new VisitorInfoPresenter();

								String userName = ClientProperties
										.getInstance().getStringValue(
												ClientProperties.USER_NAME,
												null);
								if (userName != null) {
									String email = ClientProperties
											.getInstance()
											.getStringValue(
													ClientProperties.USER_EMAIL,
													null);

									String message = ClientProperties
											.getInstance()
											.getStringValue(
													ClientProperties.USER_ADDITIONAL_INFO,
													null);

									visitor.infoSubmitted(userName, email,
											message);
								} else {
									visitor.show();
								}
							}
						}
					});
			try {
				CommunicationsFactory.sendMessageToServer(builder);
			} catch (RequestException e) {
				logger.severe("Error sending message to the server - "
						+ e.getMessage());
			}
		} else {
			MessageBoxPresenter
					.getInstance()
					.show(messages.ApplicationController_error(),
							messages.ApplicationController_errorEncountered()
									+ ". "
									+ messages
											.ApplicationController_badClientTypeSpecified(type)
									+ ". "
									+ messages
											.ApplicationController_contactAdministrator(),

							MessageBoxPresenter.Severity.SEVERE, true);
		}
	}

	private void initAudio() {
		new AudioUtils(MainPanelPresenter.getInstance().getMainPanel());
	}

	private void dispose() {
		rootPanelResizeListeners.clear();

		if (operator) {
			if (UserPanelPresenter.getCurrentInstance() != null) {
				UserPanelPresenter.getCurrentInstance().dispose();
			}
		} else {
			processVisitorDisconnectAction();
		}

		SessionInfo.getInstance().clear();
		SessionInfo.getInstance().getChatList().clear();

		Notifier.cancelAlert();

		if (operator) {
			initEmailTranscriptInfo();
			new LoginPresenter();
			LoginPresenter.getCurrentInstance().show();
		}
	}

	public void processVisitorDisconnectAction() {

		if (!disconnectExpected) {
			Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
					.getChatList();
			if (chats.size() > 0) {
				ChatSessionInfo chat = (ChatSessionInfo) chats.values()
						.iterator().next();
				if (chat != null) {
					ChatSessionPresenter presenter = chat.getChat();
					presenter.serverDisconnected();
					return;
				}
			}
		}

		String url = ClientProperties.getInstance().getStringValue(
				ClientProperties.VISITOR_CHAT_ENDED_URL, null);
		if (url != null) {
			Timer t = new Timer() {
				@Override
				public void run() {
					Window.Location.assign(ClientProperties.getInstance()
							.getStringValue(
									ClientProperties.VISITOR_CHAT_ENDED_URL,
									null));
				}
			};

			t.schedule(4000);
		}
	}

	public void initOnWindowCloseAction() {
		Window.addWindowClosingHandler(new Window.ClosingHandler() {

			@Override
			public void onWindowClosing(ClosingEvent event) {
				cleanupSessionsAndCommunications("user closed the Window");
			}
		});
	}

	public void loggedIn(RegistrationResponseMessage rsp) {

		timeAdjustment = rsp.getLoginDate().getTime() - new Date().getTime();

		LoginPresenter.getCurrentInstance().dispose();

		rsp.getCallPartyInfo().setCookiesEnabled(Cookies.isCookieEnabled());
		SessionInfo.getInstance().put(SessionInfo.USER_INFO,
				rsp.getCallPartyInfo());

		initCannedMessages(rsp);

		new UserPanelPresenter();
		UserPanelPresenter.getCurrentInstance().show(rsp);
	}

	private void initCannedMessages(RegistrationResponseMessage rsp) {
		if (rsp.getGroupList() == null) {
			return;
		}

		int size = rsp.getGroupList().numElements();
		String[] groups = new String[size + 1];
		groups[0] = "all";
		for (int i = 0; i < size; i++) {
			groups[i + 1] = rsp.getGroupList().getElementAt(i);
		}

		RequestBuilder builder = AceOperatorService.Util.getInstance()
				.listCannedMessages(
						groups,
						ClientProperties.getInstance().getBooleanValue(
								ClientProperties.GET_CANNED_MESSAGE_CONTENT,
								false),
						new AsyncCallback<CannedMessageElement[]>() {

							@Override
							public void onSuccess(
									CannedMessageElement[] elements) {
								logger.fine("Canned message information downloaded : "
										+ elements.length);
								if (elements != null && elements.length > 0) {
									SessionInfo.getInstance().put(
											SessionInfo.CANNED_MESSAGES,
											elements);
								}
							}

							@Override
							public void onFailure(Throwable caught) {
								logger.severe("Error retrieving canned messages - "
										+ caught.getClass().getName()
										+ " : "
										+ caught.getMessage());
							}
						});
		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
	}

	public String getLocale() {
		return locale;
	}

	public void addResizeListener(RootPanelResizeListener listener) {
		rootPanelResizeListeners.add(listener);
	}

	public void removeResizeListener(RootPanelResizeListener listener) {
		rootPanelResizeListeners.remove(listener);
	}

	public boolean isOperator() {
		return operator;
	}

	private void processAllOperatorBusy() {
		String email = ClientProperties.getInstance().getStringValue(
				ClientProperties.ALL_OPERATOR_BUSY_EMAIL, null);
		String url = ClientProperties.getInstance().getStringValue(
				ClientProperties.ALL_OPERATOR_BUSY_URL, null);

		if (email != null) {
			// Display standard email form
			UserBusyEmailPresenter emailPresenter = new UserBusyEmailPresenter();
			emailPresenter.show();
		} else if (url != null) {
			Window.Location.assign(url);
		} else {
			MessageBoxPresenter.getInstance().show(
					messages.ApplicationController_operatorBusy(),
					messages.ApplicationController_allOperatorsBusy() + ". "
							+ messages.ApplicationController_tryAgainLater()
							+ ".", MessageBoxPresenter.Severity.INFO, true);
		}
	}

	public void connectToServer() {
		MessageBoxPresenter.getInstance().show(
				messages.ApplicationController_connectingToServer(),
				messages.ApplicationController_connectingToServer() + " ...",
				Images.CONNECTING_MEDIUM, false);
		CommunicationsFactory.getServerCommunications().connect();
	}

	private void processReplace(ReplaceSessionMessage rep) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo session = chats.get(rep.getOldSessionId());
		if (session == null) {
			logger.warning("Received a replace session request for a session that is already disconnected."
					+ " Going to ignore");
			return;
		}

		session.getChat().replaceSession(rep.getOldSessionId(),
				rep.getNewSessionId());
	}

	private void processConferenceInfo(ConferenceInformationMessage conf) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();
		ChatSessionInfo session = chats.get(conf.getSessionId());
		if (session == null) {
			logger.warning("Received a conference information request for a session that is already disconnected."
					+ " Going to ignore");
			return;
		}

		session.getChat().changeUserInfo(conf);
	}

	public long getTimeAdjustment() {
		return timeAdjustment;
	}

	public void disconnectExpected() {
		disconnectExpected = true;
	}

	public void cleanupSessionsAndCommunications(String reasonText) {
		// Terminate active sessions
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance()
				.getChatList();

		if (connected) {
			for (ChatSessionInfo chat : chats.values()) {
				if (chat.getStatus() != ChatStatus.DISCONNECTED) {
					chat.getChat().chatTerminated(reasonText);
				}
			}

			chats.clear();

			connected = false;
			CommunicationsFactory.getServerCommunications().disconnect();
			dispose();
		}
	}

	public long timestamp() {
		return new Date().getTime() + timeAdjustment;
	}

}
