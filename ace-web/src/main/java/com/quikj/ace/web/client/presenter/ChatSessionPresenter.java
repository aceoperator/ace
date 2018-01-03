/**
 * 
 */
package com.quikj.ace.web.client.presenter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CalledNameElement;
import com.quikj.ace.messages.vo.talk.CallingNameElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.ConferenceInformationMessage;
import com.quikj.ace.messages.vo.talk.ConferencePartyInfo;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.messages.vo.talk.FormDefinitionElement;
import com.quikj.ace.messages.vo.talk.FormSubmissionElement;
import com.quikj.ace.messages.vo.talk.HtmlElement;
import com.quikj.ace.messages.vo.talk.JoinRequestMessage;
import com.quikj.ace.messages.vo.talk.MailElement;
import com.quikj.ace.messages.vo.talk.MediaElementInterface;
import com.quikj.ace.messages.vo.talk.MediaElements;
import com.quikj.ace.messages.vo.talk.RTPMessage;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.ace.messages.vo.talk.TypingElement;
import com.quikj.ace.web.client.ApplicationController;
import com.quikj.ace.web.client.AudioUtils;
import com.quikj.ace.web.client.ChatSessionInfo;
import com.quikj.ace.web.client.ChatSessionInfo.ChatStatus;
import com.quikj.ace.web.client.ChatSettings;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.client.EmailTranscriptInfo;
import com.quikj.ace.web.client.Images;
import com.quikj.ace.web.client.Notifier;
import com.quikj.ace.web.client.SessionInfo;
import com.quikj.ace.web.client.comm.CommunicationsFactory;
import com.quikj.ace.web.client.comm.ResponseListener;
import com.quikj.ace.web.client.view.ChatPanel;
import com.quikj.ace.web.client.view.UserContact;
import com.quikj.ace.web.client.view.ViewUtils;

/**
 * @author amit
 * 
 */
public class ChatSessionPresenter {

	private static final boolean DEFAULT_COOKIE_IN_SUBJECT = false;
	private static final boolean DEFAULT_VERBOSE_MESSAGES = false;
	private static final int TYPING_TIMEOUT = 30000;
	private static final long TYPING_SEND_DELAY = 30000L;

	private ChatPanel view;
	private List<CallPartyElement> otherParties = new ArrayList<CallPartyElement>();
	private Logger logger;
	private ChatSessionInfo chatInfo;
	private Date conversationStart = new Date();
	private Date conversationDisc;
	private int setupRequestId = -1;
	private long joinSessionId = -1L;
	private long xferredFromId = -1L;
	private Date lastTypingTime = null;
	private Timer typingTimer = null;
	private String systemUser = ApplicationController.getMessages().ChatSessionPresenter_systemUser();
	private boolean disconnectChat = false;

	public ChatSessionPresenter() {
		logger = Logger.getLogger(getClass().getName());
	}

	public void showChat() {
		if (ApplicationController.getInstance().isOperator()) {
			// before changing out the old chat, dispose of it if
			// it's not in the conversation list anymore
			ChatSessionPresenter presenter = UserPanelPresenter.getCurrentInstance().getCurrentChatPresenter();
			if (presenter != null) {
				long oldSessionId = presenter.getSessionId();
				if (!UserChatsPresenter.getCurrentInstance().chatExists(oldSessionId)) {
					presenter.dispose(DisconnectReasonElement.NORMAL_DISCONNECT, null);
				}
			}

			UserPanelPresenter.getCurrentInstance().showChat((Widget) view);
		} else {
			MainPanelPresenter.getInstance().attachToMainPanel((Widget) view);
		}
	}

	public String getOtherPartyNames() {
		return ViewUtils.formatNames(otherParties);
	}

	public int getNumParties() {
		return otherParties.size();
	}

	public Long getSessionId() {
		return chatInfo == null ? null : chatInfo.getSessionId();
	}

	public void chatClosed() {
		dispose(DisconnectReasonElement.NORMAL_DISCONNECT, null);

		if (ApplicationController.getInstance().isOperator()) {
			UserPanelPresenter.getCurrentInstance().showConversations();
		}
	}

	public void dispose(int reasonCode, String reasonText) {
		if (chatInfo.getStatus() == ChatStatus.CONNECTED) {
			userDisconnected(reasonCode, reasonText);
		}

		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		chats.remove(chatInfo.getSessionId());
		if (typingTimer != null) {
			cancelTyping();
		}

		if (view != null) {
			if (ApplicationController.getInstance().isOperator()) {
				UserPanelPresenter.getCurrentInstance().removeChat((Widget) view);
				UserChatsPresenter.getCurrentInstance().removeChat(chatInfo.getSessionId());
			}

			view.dispose();
			view = null;
		}
	}

	private ChatPanel createView(CallPartyElement otherParty) {

		CannedMessageElement[] cannedMessages = null;
		if (ApplicationController.getInstance().isOperator()) {
			cannedMessages = (CannedMessageElement[]) SessionInfo.getInstance().get(SessionInfo.CANNED_MESSAGES);
		}

		boolean showDetails = ClientProperties.getInstance().getBooleanValue(ClientProperties.SHOW_OTHER_PARTY_DETAILS,
				true);

		boolean hideLoginIds = ClientProperties.getInstance().getBooleanValue(ClientProperties.HIDE_LOGIN_IDS, false);

		CallPartyElement me = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);

		ChatPanel chat = GWT.create(ChatPanel.class);

		chat.attach(ViewUtils.formatName(me), otherParty, cannedMessages,
				ApplicationController.getInstance().isOperator(), showDetails, hideLoginIds);
		chat.setPresenter(this);
		return chat;
	}

	public void setupOutboundChat(String called, String transferId, String transferFrom, String transcript,
			long joinSessionId, ChatPanel transferView, boolean userTransfer) {

		SetupRequestMessage message = new SetupRequestMessage();

		// get the information about the calling user (this guy)
		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		CallingNameElement calling = new CallingNameElement();
		calling.setCallParty(cp);
		message.setCallingNameElement(calling);

		// set called user information
		CalledNameElement cledElement = new CalledNameElement();
		CallPartyElement cpElement = new CallPartyElement();
		cpElement.setName(called);
		cledElement.setCallParty(cpElement);
		message.setCalledNameElement(cledElement);

		this.joinSessionId = joinSessionId;
		if (transferView != null) {
			view = transferView;
		}

		if (otherParties.size() > 0) {
			otherParties.set(0, cpElement);
		} else {
			otherParties.add(cpElement);
		}

		if (xferredFromId >= 0L) {
			if (transferId != null) {
				message.setTransferId(transferId);
			}

			if (transferFrom != null) {
				message.setTransferFrom(transferFrom);
			}

			message.setUserTransfer(userTransfer);

			if (transcript != null) {

				MediaElements elements = new MediaElements();
				message.setMedia(elements);

				HtmlElement elem = new HtmlElement();
				elements.getElements().add(elem);
				elem.setHtml("<hr><blockquote>" + transcript + "</blockquote><hr>");
			}
		} else if (joinSessionId >= 0L) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_addingUserToConference(called));
			message.setUserConference(true);
		} else {
			// For normal chat setup
			if (!ApplicationController.getInstance().isOperator()) {
				systemUser = called;
			}

			MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().ChatSessionPresenter_chatSetup(),
					ApplicationController.getMessages().ChatSessionPresenter_settingUpChatWithParty(called) + " ... ",
					(String) Images.USER_CHATTING_MEDIUM, false);
		}

		// send the message
		CommunicationsFactory.getServerCommunications().sendRequest(message, Message.CONTENT_TYPE_XML, true, 100000L,
				new ResponseListener() {

					@Override
					public void timeoutOccured(int requestId) {
						MessageBoxPresenter.getInstance().hide();
						requestId = -1;
						abortOutboundChat(
								ApplicationController.getMessages().ChatSessionPresenter_noResponseFromServer());
					}

					@Override
					public void responseReceived(int requestId, String contentType, ResponseMessage message) {
						MessageBoxPresenter.getInstance().hide();

						setupRequestId = requestId;
						SetupResponseMessage rsp = (SetupResponseMessage) message.getMessage();

						if (rsp.getCalledParty() != null) {
							otherParties.set(0, rsp.getCalledParty().getCallParty());
						}

						switch (message.getStatus()) {
						case SetupResponseMessage.ACK:
							handleReceivedAck(rsp);
							break;

						case SetupResponseMessage.ALERTING:
							handleReceivedAlerting(rsp);
							break;

						case SetupResponseMessage.PROG:
							handleReceivedProgress(requestId, rsp);
							break;

						case SetupResponseMessage.TRANSFER:
							handleReceivedTransfer(rsp);
							break;

						case SetupResponseMessage.CONNECT:
							handleConnected(requestId, contentType, rsp);
							break;

						case SetupResponseMessage.BUSY:
							handleNotConnected(requestId,
									ApplicationController.getMessages().ChatSessionPresenter_busyResponse());
							break;

						case SetupResponseMessage.NOANS:
							handleNotConnected(requestId,
									ApplicationController.getMessages().ChatSessionPresenter_noAnswerResponse());
							break;

						case SetupResponseMessage.UNAVAILABLE:
							handleNotConnected(requestId,
									ApplicationController.getMessages().ChatSessionPresenter_notAvailableResponse());
							break;

						case SetupResponseMessage.FORBIDDEN:
							handleNotConnected(requestId, message.getReason());
							break;

						case SetupResponseMessage.UNKNOWN:
							handleNotConnected(requestId,
									ApplicationController.getMessages().ChatSessionPresenter_notOnlineResponse());
							break;

						default:
							CommunicationsFactory.getServerCommunications().cancelRequest(requestId);
							abortOutboundChat(ApplicationController.getMessages().ChatSessionPresenter_chatSetupFailed()
									+ ": " + message.getReason());
							break;
						}
					}
				});
	}

	private void startNewChat() {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		chats.put(chatInfo.getSessionId(), chatInfo);

		if (xferredFromId >= 0L) {
			if (ApplicationController.getInstance().isOperator()) {
				UserChatsPresenter.getCurrentInstance().replaceSession(xferredFromId, chatInfo.getSessionId());
				UserChatsPresenter.getCurrentInstance().chatInformationChanged(chatInfo.getSessionId());
			}
		} else {
			// Normal chat setup
			if (ApplicationController.getInstance().isOperator()) {
				// add this chat to the list of conversations
				UserChatsPresenter.getCurrentInstance().addNewChat(chatInfo);
			}

			// create this chat panel
			view = createView(otherParties.get(0));
		}
	}

	private void abortOutboundChat(String msg) {
		if (view != null) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), msg);
		} else {
			MessageBoxPresenter.getInstance().show(ApplicationController.getMessages().ChatSessionPresenter_chatSetup(),
					msg, (String) Images.DISCONNECTED_MEDIUM, true);
		}
		conversationDisc = new Date();

		if (joinSessionId >= 0L) {
			disposeJoinSession();
		} else {
			chatInfo.setStatus(ChatStatus.DISCONNECTED);
			String url = ClientProperties.getInstance().getStringValue(ClientProperties.VISITOR_CHAT_DECLINED_URL,
					null);
			if (!ApplicationController.getInstance().isOperator() && url != null) {
				ApplicationController.getInstance().disconnectExpected();
				Window.Location.assign(url);
				return;
			}

			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					if (view != null) {
						view.makeReadOnly();
					}
				}
			});

			if (ApplicationController.getInstance().isOperator()) {
				UserChatsPresenter.getCurrentInstance().chatDisconnected(chatInfo.getSessionId(), true);
			} else {
				ApplicationController.getInstance().disconnectExpected();
			}
		}
	}

	public void processChatRequest(int reqId, String contentType, SetupRequestMessage msg) {

		// create new chat session information
		chatInfo = new ChatSessionInfo(ChatSessionInfo.ChatStatus.SETUP_IN_PROGRESS);
		chatInfo.setChat(this);
		chatInfo.setSessionId(msg.getSessionId());

		// get the calling party info
		otherParties.add(msg.getCallingNameElement().getCallParty());

		ChatSettings chatSettings = (ChatSettings) SessionInfo.getInstance().get(SessionInfo.CHAT_SETTINGS);

		if (chatSettings.isAutoAnswer()) {
			AudioUtils.getInstance().play(AudioUtils.RING);
			answerChat(reqId, contentType, msg);
		} else {
			AcceptTimer acceptTimer = new AcceptTimer(reqId, contentType);
			acceptTimer.schedule(60000);

			String image = Images.USER_CHATTING_MEDIUM;
			if (otherParties.get(0).getAvatar() != null
					&& !ClientProperties.getInstance().getBooleanValue(ClientProperties.HIDE_LOGIN_IDS, false)) {
				image = otherParties.get(0).getAvatar();
			}

			// alert the user to the new chat
			ConfirmationDialogPresenter.getInstance()
					.show(ApplicationController.getMessages().ChatSessionPresenter_incomingChat(),
							ApplicationController.getMessages().ChatSessionPresenter_incomingChatFromParty(
									ViewUtils.formatUserInfo(otherParties.get(0))) + "<p>"
									+ ApplicationController.getMessages().ChatSessionPresenter_doYouWantToAnswer(),
							image, new AcceptCallListener(chatInfo, reqId, contentType, msg, acceptTimer), false);
			Notifier.alert(ApplicationController.getMessages().ChatSessionPresenter_incomingChat());
		}
	}

	class AcceptTimer extends Timer {

		private int requestId;
		private String contentType;

		public AcceptTimer(int requestId, String contentType) {
			this.requestId = requestId;
			this.contentType = contentType;
		}

		@Override
		public void run() {
			ConfirmationDialogPresenter.getInstance().hide();
			Notifier.cancelAlert();
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(chatInfo.getSessionId());
			CommunicationsFactory.getServerCommunications().sendResponse(requestId, SetupResponseMessage.NOANS,
					ApplicationController.getMessages().ChatSessionPresenter_noResponseFromUser(), contentType,
					response);

			addMissedChat(Images.TIMER_TINY);
		}
	}

	class AcceptCallListener implements ConfirmationListener {
		private ChatSessionInfo chatInfo;
		private int reqId;
		private String contentType;
		private SetupRequestMessage msg;
		private AcceptTimer acceptTimer;

		public AcceptCallListener(ChatSessionInfo chatInfo, int reqId, String contentType, SetupRequestMessage msg,
				AcceptTimer acceptTimer) {
			this.chatInfo = chatInfo;
			this.reqId = reqId;
			this.contentType = contentType;
			this.msg = msg;
			this.acceptTimer = acceptTimer;
		}

		@Override
		public void yes() {
			Notifier.cancelAlert();
			acceptTimer.cancel();
			answerChat(reqId, contentType, msg);

			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
				@Override
				public void execute() {
					ConfirmationDialogPresenter.getInstance().hide();

					Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
						@Override
						public void execute() {
							view.chatEnabled();
						}
					});
				}
			});
		}

		@Override
		public void no() {
			Notifier.cancelAlert();
			acceptTimer.cancel();

			// send a busy response to the caller
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(chatInfo.getSessionId());
			CommunicationsFactory.getServerCommunications().sendResponse(reqId, SetupResponseMessage.BUSY,
					ApplicationController.getMessages().ChatSessionPresenter_callerBusy(), contentType, response);

			addMissedChat(Images.REJECT_CALL_TINY);
		}

		@Override
		public void cancel() {
			// The button is not shown, so this will not get called
		}
	}

	private void addMissedChat(String image) {
		CallPartyElement caller = otherParties.get(0);
		UserChatsPresenter.getCurrentInstance().addMissedChat(image, ViewUtils.formatName(caller), caller.getEmail());
	}

	private void processRTPMessage(RTPMessage msg) {
		MediaElements media = msg.getMediaElements();
		CallPartyElement from = msg.getFrom();
		processMedia(media, from);
	}

	private void processMedia(MediaElements media, CallPartyElement from) {
		String formattedName = ViewUtils.formatName(from);

		if (ApplicationController.getInstance().isOperator()) {
			UserPanelPresenter.getCurrentInstance().highlightChatEvent(chatInfo.getSessionId(), "rtp");
		}

		boolean playChime = false;
		int size = media.getElements().size();
		for (int i = 0; i < size; i++) {
			MediaElementInterface element = media.getElements().get(i);
			if (element instanceof HtmlElement) {
				HtmlElement text = (HtmlElement) element;
				cancelTyping();

				view.appendToConveration(formattedName, ApplicationController.getInstance().timestamp(),
						text.getHtml());
				playChime = true;
			} else if (element instanceof FormDefinitionElement) {
				FormDefinitionElement form = (FormDefinitionElement) element;
				view.appendToConveration(formattedName, ApplicationController.getInstance().timestamp(),
						form.getFormId(), form.getFormDef());
			} else if (element instanceof FormSubmissionElement) {
				processFormResponse(formattedName, (FormSubmissionElement) element);
			} else if (element instanceof TypingElement) {
				if (typingTimer != null) {
					cancelTyping();
				}

				view.showTyping(formattedName, ApplicationController.getInstance().timestamp());

				typingTimer = new Timer() {
					@Override
					public void run() {
						cancelTyping();
					}
				};
				typingTimer.schedule(TYPING_TIMEOUT);
			} else {
				logger.warning("Media element of type " + element.getClass().getName() + " is not supported");
			}
		}

		if (playChime) {
			AudioUtils.getInstance().play(AudioUtils.CHIME);
		}
	}

	private void processFormResponse(String from, FormSubmissionElement form) {
		List<String> list = new ArrayList<>();

		for (Entry<String, String> e : form.getResponse().entrySet()) {
			StringBuilder b = new StringBuilder("<li><b>");
			b.append(e.getKey());
			b.append("</b>: ");
			b.append(e.getValue());
			b.append("</li>");
			list.add(b.toString());
		}

		list.sort(new Comparator<String>() {
			@Override
			public int compare(String s1, String s2) {
				return s1.compareTo(s2);
			}
		});

		// TODO internationalize
		StringBuilder builder = new StringBuilder("Form data:<ul>");
		for (String e : list) {
			builder.append(e);
		}
		builder.append("</ul>");

		view.appendToConveration(from, ApplicationController.getInstance().timestamp(), builder.toString());
	}

	public static void rtpReceived(RTPMessage msg) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		ChatSessionInfo chat = (ChatSessionInfo) chats.get(msg.getSessionId());
		if (chat == null) {
			// The user must have disconnected the session
			Logger.getLogger(ChatSessionPresenter.class.getName())
					.warning("Received a RTP message for a session that does not exist");
			return;
		}

		ChatSessionPresenter presenter = chat.getChat();
		presenter.processRTPMessage(msg);
	}

	public void sendTextMessage(String text) {
		lastTypingTime = null;
		HtmlElement element = new HtmlElement();
		element.setHtml(text);
		sendRTPMessage(element);
	}

	private void sendRTPMessage(MediaElementInterface element) {
		RTPMessage rtp = new RTPMessage();
		rtp.setSessionId(chatInfo.getSessionId());
		MediaElements elements = new MediaElements();
		rtp.setMediaElements(elements);
		elements.getElements().add(element);

		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		rtp.setFrom(cloneCallPartyElement(cp));

		CommunicationsFactory.getServerCommunications().sendRequest(rtp, Message.CONTENT_TYPE_XML, false, 0L, null);
	}

	public void submitForm(long formId, Map<String, String> result) {
		lastTypingTime = null;

		if (chatInfo.getStatus() == ChatStatus.CONNECTED) {
			sendRTPMessage(new FormSubmissionElement(result, formId));
			// TODO internationalize
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					"Your input has been submitted. Thank you");
		}
	}

	public void userDisconnected(int reasonCode, String reasonText) {
		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
				ApplicationController.getMessages().ChatSessionPresenter_chatHasEnded());

		displayChatEndedMessage();

		view.makeReadOnly();

		if (xferredFromId >= 0) {
			// The chat session is in the middle of a transfer, the setup
			// message has been sent but no ack received yet. Wait for the ack
			// to send the disconnect
			disconnectChat = true;
		} else {
			disconnectOrTransfer(null, false, reasonCode, reasonText);
		}
	}

	private void displayChatEndedMessage() {
		String chatEndedMessage = ClientProperties.getInstance()
				.getStringValue(ClientProperties.VISITOR_CHAT_ENDED_HTML, null);
		if (chatEndedMessage != null && !ApplicationController.getInstance().isOperator()) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), chatEndedMessage);
		}
	}

	private void disconnectOrTransfer(String transferTo, boolean transferTranscript, int reasonCode,
			String reasonText) {
		conversationDisc = new Date();

		if (ApplicationController.getInstance().isOperator()) {
			EmailTranscriptInfo emailTrInfo = (EmailTranscriptInfo) SessionInfo.getInstance()
					.get(SessionInfo.EMAIL_TRANSCRIPT_INFO);
			if (emailTrInfo != null && emailTrInfo.isEmailTranscript()) {
				emailTranscript(reasonCode, reasonText);
			}
		}

		DisconnectMessage disc = new DisconnectMessage();
		disc.setSessionId(chatInfo.getSessionId());
		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		disc.setFrom(cloneCallPartyElement(cp));
		DisconnectReasonElement reason = new DisconnectReasonElement();
		disc.setDisconnectReason(reason);
		reason.setReasonCode(reasonCode);
		reason.setReasonText(reasonText);

		if (transferTo != null) {
			CalledNameElement called = new CalledNameElement();
			disc.setCalledInfo(called);
			CallPartyElement cparty = new CallPartyElement();
			called.setCallParty(cparty);
			cparty.setName(transferTo);

			if (transferTranscript) {
				disc.setTranscript(transferTranscript);
			}
		}

		CommunicationsFactory.getServerCommunications().sendRequest(disc, Message.CONTENT_TYPE_XML, false, 0L, null);

		chatInfo.setStatus(ChatStatus.DISCONNECTED);
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		ChatSessionInfo chat = (ChatSessionInfo) chats.get(chatInfo.getSessionId());
		chat.setStatus(ChatStatus.DISCONNECTED);

		if (ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().chatDisconnected(chatInfo.getSessionId(), false);
		} else {
			ApplicationController.getInstance().disconnectExpected();
		}
	}

	private void emailTranscript(int reasonCode, String reasonText) {
		EmailTranscriptInfo info = (EmailTranscriptInfo) SessionInfo.getInstance()
				.get(SessionInfo.EMAIL_TRANSCRIPT_INFO);

		SendMailRequestMessage message = new SendMailRequestMessage();
		message.setReplyRequired(false);
		MailElement melement = new MailElement();
		message.setMailElement(melement);

		CallPartyElement cpElement = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);

		Vector<String> replyToList = null;
		if (info.isFrom()) {
			replyToList = new Vector<String>();
			int count = 0;
			if (info.isFromSelf() && cpElement.getEmail() != null) {
				if (count++ == 0) {
					melement.setFrom(cpElement.getEmail());
				}

				replyToList.addElement(cpElement.getEmail());
			}

			String[] cfglist = info.getFromList();
			for (String cfg : cfglist) {
				if (count++ == 0) {
					melement.setFrom(cfg);
				}
				replyToList.addElement(cfg);
			}

			if (replyToList.size() > 0) {
				melement.setReplyTo(replyToList);
			}
		}

		if (info.isSendSelf() && cpElement.getEmail() != null) {
			melement.addTo(cpElement.getEmail());
		}

		if (info.isSendOthers()) {
			for (CallPartyElement other : otherParties) {
				if (other.getEmail() != null) {
					melement.addTo(other.getEmail());
				}
			}
		}

		String[] toList = info.getToList();
		for (String to : toList) {
			melement.addTo(to);
		}

		melement.setSubype("html");

		if (ClientProperties.getInstance().getBooleanValue(ClientProperties.COOKIE_IN_TRANSCRIPT_SUBJECT,
				DEFAULT_COOKIE_IN_SUBJECT)) {

			String visitorId = null;
			for (CallPartyElement other : otherParties) {
				if (other.getName() == null) {
					// Visitor
					visitorId = other.getEndUserCookie();
					if (!other.isCookiesEnabled()) {
						visitorId = other.getIpAddress();
					}
					break;
				}
			}

			String subjectPrefix = visitorId != null ? (visitorId + " - ") : "";
			melement.setSubject(subjectPrefix
					+ ApplicationController.getMessages().ChatSessionPresenter_aceOperatorChatTranscript());
		} else {
			melement.setSubject(ApplicationController.getMessages().ChatSessionPresenter_aceOperatorChatTranscript());
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append(ApplicationController.getMessages().ChatSessionPresenter_conversationStartTime() + " "
				+ DateTimeFormat.getFormat(ClientProperties.getInstance()
						.getStringValue(ClientProperties.DATE_TIME_FORMAT, ClientProperties.DEFAULT_DATE_TIME_FORMAT))
						.format(conversationStart));
		buffer.append("<br>");

		buffer.append(ApplicationController.getMessages().ChatSessionPresenter_conversationEndTime() + " "
				+ (conversationDisc == null ? "--"
						: DateTimeFormat
								.getFormat(ClientProperties.getInstance().getStringValue(
										ClientProperties.DATE_TIME_FORMAT, ClientProperties.DEFAULT_DATE_TIME_FORMAT))
								.format(conversationDisc)));
		buffer.append("<br>");

		String thisParty = ViewUtils.formatName(cpElement);

		StringBuffer otherParty = new StringBuffer();
		int count = 0;
		for (CallPartyElement other : otherParties) {
			if (count++ > 0) {
				otherParty.append(", ");
			}
			otherParty.append(ViewUtils.formatName(other));
		}

		buffer.append(ApplicationController.getMessages().ChatSessionPresenter_conversationUsers(thisParty,
				otherParty.toString()));

		buffer.append("<br>");

		// TODO internationalize the string
		buffer.append("Disconnect status: ");
		if (reasonText != null && reasonText.length() > 0) {
			buffer.append(reasonText);
			buffer.append(" - ");
		}
		buffer.append(DisconnectReasonElement.DISCONNECT_CODE_DESCRIPTIONS[reasonCode]);

		buffer.append("<hr>");
		buffer.append(ApplicationController.getMessages().ChatSessionPresenter_chatTranscript());
		buffer.append(":<br>");
		buffer.append(view.getTranscript());

		melement.setBody(buffer.toString());

		CommunicationsFactory.getServerCommunications().sendRequest(message, Message.CONTENT_TYPE_XML, false, 0L, null);
	}

	public void serverDisconnected() {
		if (chatInfo.getStatus() == ChatSessionInfo.ChatStatus.SETUP_IN_PROGRESS) {
			CommunicationsFactory.getServerCommunications().cancelRequest(setupRequestId);
		}

		conversationDisc = new Date();
		if (view != null) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_chatEnded());
			view.makeReadOnly();
		}

		chatInfo.setStatus(ChatStatus.DISCONNECTED);
		if (typingTimer != null) {
			cancelTyping();
		}
	}

	public static void disconnectReceived(DisconnectMessage msg) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		ChatSessionInfo chat = (ChatSessionInfo) chats.get(msg.getSessionId());
		if (chat == null) {
			// The chat must have already been disconnected
			Logger.getLogger(ChatSessionPresenter.class.getName())
					.warning("Received a disconnect/transfer message for a session that does not exist");
			return;
		}

		ChatSessionPresenter presenter = chat.getChat();

		if (chat.getStatus() == ChatSessionInfo.ChatStatus.SETUP_IN_PROGRESS) {
			CommunicationsFactory.getServerCommunications().cancelRequest(presenter.getSetupRequestId());
		}

		CallPartyElement from = msg.getFrom();

		if (msg.getCalledInfo() != null) {
			// Call transfer
			presenter.processTransfer(msg, from);
		} else {
			presenter.processDisconnect(msg, from, chat);

			if (ApplicationController.getInstance().isOperator()) {
				UserChatsPresenter.getCurrentInstance().chatDisconnected(msg.getSessionId(), true);
			}
		}
	}

	private void processTransfer(DisconnectMessage msg, CallPartyElement from) {

		boolean userTransfer = true;
		if (msg.getFrom().equals(ClientProperties.getInstance().getStringValue(ClientProperties.GROUP, ""))) {
			userTransfer = false;
		}

		if (userTransfer) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), ApplicationController
					.getMessages().ChatSessionPresenter_chatTransferred(ViewUtils.formatName(from)));
		}

		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
				ApplicationController.getMessages().ChatSessionPresenter_transferringToParty(
						ViewUtils.formatName(msg.getCalledInfo().getCallParty())));

		view.chatDisabled();

		String transcript = null;
		if (msg.isTranscript()) {
			transcript = view.getTranscript();
		}

		if (typingTimer != null) {
			cancelTyping();
		}

		// Cleanup
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		chats.remove(chatInfo.getSessionId());
		xferredFromId = chatInfo.getSessionId();
		setupOutboundChat(msg.getCalledInfo().getCallParty().getName(), msg.getTransferId(), msg.getFrom().getName(),
				transcript, -1, null, userTransfer);
	}

	public void cancelTyping() {
		if (typingTimer != null) {
			typingTimer.cancel();
			typingTimer = null;
		}

		if (view != null) {
			view.hideTyping();
		}
	}

	private void processDisconnect(DisconnectMessage msg, CallPartyElement from, ChatSessionInfo chatSession) {
		conversationDisc = new Date();

		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
				ApplicationController.getMessages()
						.ChatSessionPresenter_chatDisconnected(from == null ? "Server" : ViewUtils.formatName(from)));
		displayChatEndedMessage();

		view.makeReadOnly();
		chatInfo.setStatus(ChatStatus.DISCONNECTED);
		chatSession.setStatus(ChatStatus.DISCONNECTED);

		if (ApplicationController.getInstance().isOperator()) {
			EmailTranscriptInfo emailTrInfo = (EmailTranscriptInfo) SessionInfo.getInstance()
					.get(SessionInfo.EMAIL_TRANSCRIPT_INFO);
			if (emailTrInfo != null && emailTrInfo.isEmailTranscript()) {
				emailTranscript(msg.getDisconnectReason().getReasonCode(), msg.getDisconnectReason().getReasonText());
			}
		} else {
			ApplicationController.getInstance().disconnectExpected();
		}

		if (typingTimer != null) {
			cancelTyping();
		}
	}

	public void cannedMessageSelected(int cannedElementIndex) {
		CannedMessageElement element = ((CannedMessageElement[]) SessionInfo.getInstance()
				.get(SessionInfo.CANNED_MESSAGES))[cannedElementIndex];
		if (ClientProperties.getInstance().getBooleanValue(ClientProperties.GET_CANNED_MESSAGE_CONTENT, false)) {
			view.appendToConveration(null, ApplicationController.getInstance().timestamp(), element.getMessage());
		} else {
			view.appendToConveration(null, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_sentCannedMessage() + " - "
							+ element.getDescription());
		}

		RTPMessage rtp = new RTPMessage();
		rtp.setSessionId(chatInfo.getSessionId());
		MediaElements elements = new MediaElements();
		rtp.setMediaElements(elements);
		elements.getElements().add(element);
		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		rtp.setFrom(cloneCallPartyElement(cp));

		CommunicationsFactory.getServerCommunications().sendRequest(rtp, Message.CONTENT_TYPE_XML, false, 0L, null);
	}

	private void answerChat(int reqId, String contentType, SetupRequestMessage msg) {
		// send an answer response to the caller
		SetupResponseMessage response = new SetupResponseMessage();
		response.setSessionId(chatInfo.getSessionId());
		CommunicationsFactory.getServerCommunications().sendResponse(reqId, SetupResponseMessage.CONNECT,
				ApplicationController.getMessages().ChatSessionPresenter_answered(), contentType, response);

		chatInfo.setStatus(ChatSessionInfo.ChatStatus.CONNECTED);

		startNewChat();
		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
				ApplicationController.getMessages().ChatSessionPresenter_chatInformation() + ":<br/>"
						+ ViewUtils.formatUserInfo(otherParties.get(0), false));

		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), ApplicationController
				.getMessages().ChatSessionPresenter_connectedToParty(ViewUtils.formatName(otherParties.get(0))));

		if (msg.isUserTransfer()) {
			String fromParty = msg.getTransferFrom();
			if (fromParty == null) {
				fromParty = ApplicationController.getMessages().ChatSessionPresenter_privateParty();
			}

			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_transferringFrom(fromParty) + ". ");

			if (msg.getMedia() != null) {
				view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
						ApplicationController.getMessages().ChatSessionPresenter_transcriptFollows() + ": ");
			}

		} else if (msg.isUserConference()) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_addingYouToConference());
		}

		if (msg.getMedia() != null) {
			processMedia(msg.getMedia(), systemUserCallPartyElement());
		}

		if (UserChatsPresenter.getCurrentInstance().getActiveChatCount() == 1) {
			showChat();
		} else {
			UserPanelPresenter.getCurrentInstance().highlightChatEvent(chatInfo.getSessionId(), "new");
		}

		view.chatEnabled();
	}

	private CallPartyElement systemUserCallPartyElement() {
		return new CallPartyElement(systemUser, null);
	}

	public int getSetupRequestId() {
		return setupRequestId;
	}

	public List<String> getContactsList() {
		List<UserContact> contacts = UserContactsPresenter.getCurrentInstance().getOnlineContacts();
		List<String> ret = new ArrayList<String>();

		nextContact: for (UserContact contact : contacts) {
			if (contact.isDnd()) {
				continue;
			}

			// Do not add the contact to the list if he/she is already
			// participating in the chat
			for (CallPartyElement other : otherParties) {
				if (other.getName() != null && other.getName().equals(contact.getUser())) {
					continue nextContact;
				}
			}

			ret.add(ViewUtils.formatName(contact.getUser(), contact.getFullName()));
		}
		return ret;
	}

	public void addToConference(String user) {
		new ChatSessionPresenter().setupOutboundChat(ViewUtils.parseName(user), null, null, null,
				chatInfo.getSessionId(), view, false);
	}

	class TransferListener implements ConfirmationListener {

		private String transferTo;

		public TransferListener(String transferTo) {
			this.transferTo = transferTo;
		}

		@Override
		public void yes() {
			view.appendToConveration(systemUser, new Date().getTime(),
					ApplicationController.getMessages().ChatSessionPresenter_transferringChatTo(transferTo));
			view.makeReadOnly();
			disconnectOrTransfer(ViewUtils.parseName(transferTo), true, DisconnectReasonElement.NORMAL_DISCONNECT,
					null);
		}

		@Override
		public void no() {
			view.appendToConveration(systemUser, new Date().getTime(),
					ApplicationController.getMessages().ChatSessionPresenter_transferringChatTo(transferTo));
			view.makeReadOnly();
			disconnectOrTransfer(ViewUtils.parseName(transferTo), false, DisconnectReasonElement.NORMAL_DISCONNECT,
					null);
		}

		@Override
		public void cancel() {
			// The user does not want to transfer any more
		}
	}

	public void transferTo(String user) {
		ConfirmationDialogPresenter.getInstance().show(
				ApplicationController.getMessages().ChatSessionPresenter_chatTransfer(),
				ApplicationController.getMessages().ChatSessionPresenter_shouldIncludeTranscript(), Images.INFO_MEDIUM,
				new TransferListener(user), true);
	}

	private void joinSessions(String contentType) {
		JoinRequestMessage join = new JoinRequestMessage();
		join.getSessionList().add(joinSessionId);
		join.getSessionList().add(getSessionId());

		CommunicationsFactory.getServerCommunications().sendRequest(join, contentType, false, 100000L,
				new ResponseListener() {

					@Override
					public void timeoutOccured(int requestId) {
						MessageBoxPresenter.getInstance().show(
								ApplicationController.getMessages().ChatSessionPresenter_error(),
								ApplicationController.getMessages().ChatSessionPresenter_failedToAddUser(),
								MessageBoxPresenter.Severity.SEVERE, true);
						disposeJoinSession();
					}

					@Override
					public void responseReceived(int requestId, String contentType, ResponseMessage message) {
						disposeJoinSession();
					}
				});
	}

	public void replaceSession(long oldSessionId, long newSessionId) {
		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		ChatSessionInfo session = chats.get(oldSessionId);
		session.setSessionId(newSessionId);
		chats.remove(oldSessionId);
		chats.put(newSessionId, session);

		if (ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().replaceSession(oldSessionId, newSessionId);
		}
	}

	private void handleReceivedAck(SetupResponseMessage rsp) {
		try {
			chatInfo = new ChatSessionInfo(ChatSessionInfo.ChatStatus.SETUP_IN_PROGRESS);
			chatInfo.setChat(this);
			chatInfo.setSessionId(rsp.getSessionId());
			if (rsp.getCallingCookie() != null) {
				CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
				cp.setEndUserCookie(rsp.getCallingCookie());
			}

			if (joinSessionId < 0L) {
				startNewChat();
				showChat();
			}

			if (disconnectChat) {
				disconnectOrTransfer(null, false, DisconnectReasonElement.NORMAL_DISCONNECT, null);
				return;
			}

			if (ClientProperties.getInstance().getBooleanValue(ClientProperties.VERBOSE_MESSAGES,
					DEFAULT_VERBOSE_MESSAGES)) {
				view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
						ApplicationController.getMessages().ChatSessionPresenter_initiatedChatWith(
								ViewUtils.formatName(otherParties.get(0))) + "...");
			}

			// process media elements
			if (rsp.getMediaElements() != null) {
				processMedia(rsp.getMediaElements(), systemUserCallPartyElement());
			}
		} finally {
			// At this point treat it as a normal call setup
			xferredFromId = -1L;
			disconnectChat = false;
		}
	}

	private void handleReceivedTransfer(SetupResponseMessage rsp) {
		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
				ApplicationController.getMessages().ChatSessionPresenter_transferringToParty(
						ViewUtils.formatName(rsp.getCalledParty().getCallParty())));

		// process media elements
		if (rsp.getMediaElements() != null) {
			processMedia(rsp.getMediaElements(), systemUserCallPartyElement());
		}

		Map<Long, ChatSessionInfo> chats = SessionInfo.getInstance().getChatList();
		ChatSessionInfo chat = (ChatSessionInfo) chats.get(chatInfo.getSessionId());
		if (chat == null) {
			logger.log(Level.SEVERE, "During a transfer, the old session was not found in the list of chats");
			return;
		}

		chats.remove(chatInfo.getSessionId());
		chat.setSessionId(rsp.getNewSessionId());
		chats.put(chat.getSessionId(), chat);
		chatInfo.setSessionId(chat.getSessionId());

		if (ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().replaceSession(rsp.getSessionId(), rsp.getNewSessionId());
			UserChatsPresenter.getCurrentInstance().chatInformationChanged(chatInfo.getSessionId());
		}

		// TODO look into handling timeouts
	}

	private void handleReceivedAlerting(SetupResponseMessage rsp) {
		if (ClientProperties.getInstance().getBooleanValue(ClientProperties.VERBOSE_MESSAGES,
				DEFAULT_VERBOSE_MESSAGES)) {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_notifyingUser() + " "
							+ ViewUtils.formatName(otherParties.get(0)));
		}

		if (joinSessionId < 0L) {
			view.setOtherPartyInfo(otherParties);
		}

		if (ChatSessionPresenter.this.joinSessionId < 0L && ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().chatInformationChanged(chatInfo.getSessionId());
		}

		// process media elements
		if (rsp.getMediaElements() != null) {
			processMedia(rsp.getMediaElements(), systemUserCallPartyElement());
		}
	}

	private void handleReceivedProgress(int requestId, SetupResponseMessage rsp) {
		CommunicationsFactory.getServerCommunications().changeTimeout(requestId, 120000L);

		// process media elements
		if (rsp.getMediaElements() != null) {
			processMedia(rsp.getMediaElements(), systemUserCallPartyElement());
		} else {
			view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(),
					ApplicationController.getMessages().ChatSessionPresenter_setupInProgress() + " ... ");
		}
	}

	private void handleConnected(int requestId, String contentType, SetupResponseMessage rsp) {
		chatInfo.setStatus(ChatStatus.CONNECTED);

		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), ApplicationController
				.getMessages().ChatSessionPresenter_connectedToParty(ViewUtils.formatName(otherParties.get(0))));

		if (joinSessionId < 0L) {
			view.chatEnabled();
		}

		CommunicationsFactory.getServerCommunications().cancelRequest(requestId);

		if (ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().chatConnected(rsp.getSessionId(), true);
		}

		// process media elements
		if (rsp.getMediaElements() != null) {
			processMedia(rsp.getMediaElements(), systemUserCallPartyElement());
		}

		if (ChatSessionPresenter.this.joinSessionId >= 0L) {
			joinSessions(contentType);
		}
	}

	private void handleNotConnected(int requestId, String msg) {
		CommunicationsFactory.getServerCommunications().cancelRequest(requestId);
		abortOutboundChat(msg);
	}

	public void changeUserInfo(ConferenceInformationMessage conf) {

		List<CallPartyElement> newOtherParties = new ArrayList<CallPartyElement>();

		int num = conf.getEndpointList().size();
		for (int i = 0; i < num; i++) {
			ConferencePartyInfo party = conf.getEndpointList().get(i);

			CallPartyElement other = party.getParticipantInfo();
			CallPartyElement me = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);

			if (ApplicationController.getInstance().isOperator()) {
				if (other.getName() != null && me.getName().equals(other.getName())) {
					continue;
				}
			} else {
				// Visitor
				if (other.getName() == null && me.getFullName().equals(other.getFullName())) {
					continue;
				}
			}

			switch (party.getStatus()) {
			case ConferencePartyInfo.STATUS_ADDED:
				partiesChanged(other, ApplicationController.getMessages().ChatSessionPresenter_joinedChat());
				newOtherParties.add(other);
				break;

			case ConferencePartyInfo.STATUS_REMOVED:
				partiesChanged(other, ApplicationController.getMessages().ChatSessionPresenter_leftChat());
				break;

			default:
				newOtherParties.add(other);
				break;
			}
		}

		otherParties = newOtherParties;

		if (otherParties.size() > 1) {
			// disable the call transfer button
			view.transferSetEnabled(false);
		} else {
			// re-enable the call transfer button
			view.transferSetEnabled(true);
		}

		view.setOtherPartyInfo(otherParties);

		if (ApplicationController.getInstance().isOperator()) {
			UserChatsPresenter.getCurrentInstance().chatInformationChanged(chatInfo.getSessionId());
		}
	}

	private void partiesChanged(CallPartyElement element, String message) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(ApplicationController.getMessages().ChatSessionPresenter_user());
		buffer.append(" ");
		buffer.append(ViewUtils.formatName(element));
		buffer.append(" ");
		buffer.append(message);

		view.appendToConveration(systemUser, ApplicationController.getInstance().timestamp(), buffer.toString());
	}

	private void disposeJoinSession() {
		view = null;
		otherParties = null;
		chatInfo = null;
	}

	public Date getConversationDisc() {
		return conversationDisc;
	}

	public void typing() {
		boolean sendTyping = false;
		if (lastTypingTime == null) {
			sendTyping = true;
		} else if (new Date().getTime() - TYPING_SEND_DELAY > lastTypingTime.getTime()) {
			sendTyping = true;
		}

		if (sendTyping) {
			lastTypingTime = new Date();

			RTPMessage message = new RTPMessage();
			message.setSessionId(chatInfo.getSessionId());
			MediaElements elements = new MediaElements();

			TypingElement tyelem = new TypingElement();
			elements.getElements().add(tyelem);
			message.setMediaElements(elements);

			CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
			message.setFrom(cloneCallPartyElement(cp));

			CommunicationsFactory.getServerCommunications().sendRequest(message, Message.CONTENT_TYPE_XML, false, 0L,
					null);
		}
	}

	public void chatTerminated(String reasonText) {
		if (ApplicationController.getInstance().isOperator()) {
			EmailTranscriptInfo emailTrInfo = (EmailTranscriptInfo) SessionInfo.getInstance()
					.get(SessionInfo.EMAIL_TRANSCRIPT_INFO);
			if (emailTrInfo != null && emailTrInfo.isEmailTranscript()) {
				int reasonCode = DisconnectReasonElement.NORMAL_DISCONNECT;
				if (reasonText != null) {
					reasonCode = DisconnectReasonElement.CLIENT_EXIT;
				}
				emailTranscript(reasonCode, reasonText);
			}
		}

		DisconnectMessage disc = new DisconnectMessage();
		disc.setSessionId(chatInfo.getSessionId());
		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		disc.setFrom(cloneCallPartyElement(cp));
		DisconnectReasonElement reason = new DisconnectReasonElement();
		disc.setDisconnectReason(reason);

		reason.setReasonCode(DisconnectReasonElement.NORMAL_DISCONNECT);
		if (reasonText != null) {
			reason.setReasonCode(DisconnectReasonElement.CLIENT_EXIT);
			reason.setReasonText(reasonText);
		}

		CommunicationsFactory.getServerCommunications().sendRequest(disc, Message.CONTENT_TYPE_XML, false, 0L, null);

		if (!ApplicationController.getInstance().isOperator()) {
			ApplicationController.getInstance().disconnectExpected();
		}
	}

	private CallPartyElement cloneCallPartyElement(CallPartyElement cp) {
		return new CallPartyElement(cp.getName(), cp.getFullName());
	}

	public String getCalledPartyAvatar() {
		if (otherParties.size() == 0) {
			return null;
		}

		return otherParties.get(0).getAvatar();
	}

	public void notifyFileSharing(String fileName) {
		RTPMessage rtp = new RTPMessage();
		rtp.setSessionId(chatInfo.getSessionId());
		MediaElements elements = new MediaElements();
		rtp.setMediaElements(elements);
		HtmlElement element = new HtmlElement();
		elements.getElements().add(element);
		String fileUrl = GWT.getModuleBaseURL() + "../fileDownload?file=" + fileName;
		String href = "<a href='" + fileUrl + "' target='_blank'>"
				+ ApplicationController.getMessages().ChatSessionPresenter_fileShared();
		element.setHtml(href);
		CallPartyElement cp = (CallPartyElement) SessionInfo.getInstance().get(SessionInfo.USER_INFO);
		rtp.setFrom(cloneCallPartyElement(cp));

		CommunicationsFactory.getServerCommunications().sendRequest(rtp, Message.CONTENT_TYPE_XML, false, 0L, null);
	}
}
