package com.quikj.application.web.talk.plugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.app.WebMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CalledNameElement;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.ace.messages.vo.talk.ConferenceInformationMessage;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.messages.vo.talk.FormDefinitionElement;
import com.quikj.ace.messages.vo.talk.FormSubmissionElement;
import com.quikj.ace.messages.vo.talk.HtmlElement;
import com.quikj.ace.messages.vo.talk.JoinRequestMessage;
import com.quikj.ace.messages.vo.talk.JoinResponseMessage;
import com.quikj.ace.messages.vo.talk.MediaElementInterface;
import com.quikj.ace.messages.vo.talk.RTPMessage;
import com.quikj.ace.messages.vo.talk.RegistrationRequestMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.messages.vo.talk.ReplaceSessionMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.ace.messages.vo.talk.TalkMessageInterface;
import com.quikj.ace.messages.vo.talk.UserToUserMessage;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.app.PluginAppClientInterface;
import com.quikj.server.app.RemoteEndPoint;
import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceNetworkAccess;

public class TalkEndpoint implements PluginAppClientInterface {

	private enum MessageDirection {
		Incoming, Outgoing
	}

	private static final String TRANSCRIPT_PREFIX = "<transcript>\n";
	private static final String TRANSCRIPT_SUFFIX = "\n</transcript>";

	private boolean registered = false;

	private String host;

	private RemoteEndPoint parent;

	private Hashtable<Long, SessionInfo> chatList = new Hashtable<Long, SessionInfo>();

	private SessionInfo lastSession = null;

	private int registrationRequestId = -1;

	private String registeredUserName;

	private boolean unregistrationComplete = false;

	private boolean firstReqRcvd = false;

	private String endUserCookie;

	ContentFilter contentFilter;

	public TalkEndpoint() {
		contentFilter = ApplicationServer.getInstance().getBean(ContentFilter.class);
	}

	private void addToCallList(long sessionId, SessionInfo session) {
		chatList.put(sessionId, session);

		if (registered) {
			RegisteredEndPointList.Instance().setCallCount(parent, chatList.size());
			ServiceController.Instance().groupNotifyOfCallCountChange(parent);
		}
	}

	private void changeSessionId(long fromSessionId, long toSessionId) {
		SessionInfo session = getSessionInfo(fromSessionId);
		if (session != null) {
			session.setSessionId(toSessionId);
			chatList.remove(fromSessionId);

			// The cookie is set to null because in the replace session, there
			// is no cookie information. In the scenario where V calls A and A
			// adds B to the chat, B's transcript will not have V's cookie
			// information. This is the correct behavior.
			String transFile = renameTranscript(session.getTranscriptFile(), toSessionId, null);
			session.setTranscriptFile(transFile);

			chatList.put(new Long(toSessionId), session);
		}
	}

	private boolean checkRequestMessage(String contentType, WebMessage body) {
		if (body == null) {
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.checkRequestMessage() -- Request message does not have a body");
			return false;
		}

		if (!contentType.equalsIgnoreCase(Message.CONTENT_TYPE_XML)) {
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.checkRequestMessage() -- Content type of a request message is not application/xml");
			return false;
		}
		return true;
	}

	private boolean checkResponseMessage(String contentType, WebMessage body) {
		if (body != null) {
			if (!contentType.equalsIgnoreCase(Message.CONTENT_TYPE_XML)) {
				AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.checkResponseMessage() -- Content type of a response message is not application/xml");
				return false;
			}
		}
		return true;
	}

	public void connectionClosed(String reasonText) {
		// equivalent to receiving a disconnect for all sessions

		Enumeration<SessionInfo> sessions = chatList.elements();

		while (sessions.hasMoreElements()) {
			SessionInfo session_info = sessions.nextElement();
			closeTranscript(session_info.getTranscriptFile());

			DisconnectMessage message = new DisconnectMessage();
			message.setSessionId(session_info.getSessionId());
			DisconnectReasonElement disc_element = new DisconnectReasonElement();

			CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (selfInfo != null) {
				message.setFrom(new CallPartyElement(selfInfo.getName(), selfInfo.getFullName()));
			}

			disc_element.setReasonCode(DisconnectReasonElement.SERVER_DISCONNECT);
			if (reasonText == null) {
				disc_element.setReasonText(java.util.ResourceBundle
						.getBundle("com.quikj.application.web.talk.plugin.language",
								ServiceController.getLocale((String) parent.getParam("language")))
						.getString("end_point_disconnected"));
			} else {
				disc_element.setReasonText(reasonText);
			}
			message.setDisconnectReason(disc_element);

			if (!ServiceController.Instance()
					.sendMessage(new MessageEvent(MessageEvent.DISCONNECT_MESSAGE, parent, message, null))) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.connectionClosed() -- Error sending disconnect message to the service controller");
			}
		}

		chatList.clear();

		if (registered) {
			if (!unregistrationComplete) {
				if (!ServiceController.Instance().sendMessage(new UnregistrationEvent(registeredUserName))) {
					// print error message
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
							+ "- TalkEndoint.connectionClosed() -- Error sending unregistration message to the service controller");
				}
			}
		}

		parent.removeParam(EndPointInterface.PARAM_SELF_INFO);
	}

	public boolean eventReceived(AceMessageInterface event) {
		if (event instanceof MessageEvent) {
			MessageEvent eventRcvd = (MessageEvent) event;

			switch (eventRcvd.getEventType()) {
			case MessageEvent.REGISTRATION_RESPONSE:
				return processRegistrationResponseEvent(eventRcvd);

			case MessageEvent.SETUP_RESPONSE:
				return processSetupResponseEvent(eventRcvd);

			case MessageEvent.SETUP_REQUEST:
				return processSetupRequestEvent(eventRcvd);

			case MessageEvent.DISCONNECT_MESSAGE:
				return processDisconnectEvent(eventRcvd);

			case MessageEvent.RTP_MESSAGE:
				return processRTPEvent(eventRcvd);

			default:
				// The event is for the client, send it to the client

				if (eventRcvd.getMessage() != null) {
					long sessionId = 0L;
					boolean save = false;
					if (eventRcvd.getMessage() instanceof JoinResponseMessage) {
						JoinResponseMessage join = (JoinResponseMessage) eventRcvd.getMessage();
						sessionId = join.getSessionList().get(0);
						save = true;
					} else if (eventRcvd.getMessage() instanceof ConferenceInformationMessage) {
						ConferenceInformationMessage conf = (ConferenceInformationMessage) eventRcvd.getMessage();
						sessionId = conf.getSessionId();
						save = true;
					}

					if (save) {
						SessionInfo sessionInfo = getSessionInfo(sessionId);
						saveTranscript(sessionInfo.getTranscriptFile(), eventRcvd.getMessage(),
								MessageDirection.Outgoing, eventRcvd.getResponseStatus(), eventRcvd.getReason());
					}
				}

				if (eventRcvd.isRequest()) {
					// request message
					if (!parent.sendRequestMessageToClient(eventRcvd.getRequestId(), Message.CONTENT_TYPE_XML,
							eventRcvd.getMessage())) {
						AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
								+ "- TalkEndoint.eventReceived() -- Error sending server request message to the endpoint");
						return false;
					}
				} else {
					// response message
					if (!parent.sendResponseMessageToClient(eventRcvd.getRequestId(), eventRcvd.getResponseStatus(),
							eventRcvd.getReason(), Message.CONTENT_TYPE_XML, eventRcvd.getMessage())) {
						AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
								+ "- TalkEndoint.eventReceived() -- Error sending server response message to the endpoint");
						return false;
					}
				}
				return true;
			}
		} else if (event instanceof ActionEvent) {
			return processActionEvent((ActionEvent) event);
		} else if (event instanceof DropEndpointEvent) {
			return processDropEndpointEvent((DropEndpointEvent) event);
		} else {
			// unexpected event
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.eventReceived() --  Unknown type of event received: " + event.messageType());
			return false;
		}
	}

	private SessionInfo getSessionInfo(long sessionId) {
		return chatList.get(sessionId);
	}

	public boolean newConnection(String host, String endUserCookie, RemoteEndPoint parent) {
		this.host = host;
		this.parent = parent;
		this.endUserCookie = endUserCookie;
		return true;
	}

	private boolean processActionEvent(ActionEvent event) {
		EndPointActionInterface[] actions = event.getActionList();

		for (int i = 0; i < actions.length; i++) {
			if ((actions[i] instanceof ChangeEndPointAction)) {
				ChangeEndPointAction change = (ChangeEndPointAction) actions[i];
				long session_id = change.getSessionId();

				SessionInfo session = getSessionInfo(session_id);
				if (session == null) {
					return true; // ignore
				}

				int num_ep = session.numEndPoints();
				for (int j = 0; j < num_ep; j++) {
					EndPointInterface ep = session.elementAt(j);
					if (ep != parent) {
						session.replaceEndPoint(j, change.getEndPoint());
					}
				}
			} else if ((actions[i] instanceof RemoveSessionAction)) {
				RemoveSessionAction remove = (RemoveSessionAction) actions[i];
				long sessionId = remove.getSessionId();

				SessionInfo session = getSessionInfo(sessionId);
				if (session == null) {
					return true; // ignore
				}

				removeFromCallList(sessionId);

				saveTranscript(session.getTranscriptFile(), remove, MessageDirection.Outgoing, null, null);

				closeTranscript(session.getTranscriptFile());

			} else if (actions[i] instanceof ReplaceSessionAction) {
				ReplaceSessionAction replace = (ReplaceSessionAction) actions[i];
				long old_session_id = replace.getOldSessionId();
				long new_session_id = replace.getNewSessionId();

				SessionInfo new_session_info = getSessionInfo(new_session_id);
				SessionInfo old_session_info = getSessionInfo(old_session_id);

				if (new_session_info == null) {
					// if the new session does not exist
					if (old_session_info != null) {
						// the service controller is telling me to replace the
						// old session id with the new one
						old_session_info.setSessionId(new_session_id);

						// send notification to the client
						ReplaceSessionMessage message = new ReplaceSessionMessage();
						message.setNewSessionId(new_session_id);
						message.setOldSessionId(old_session_id);

						String key = replace.getNewKey();
						message.setEncryptedKey(key);

						if (!parent.sendRequestMessageToClient(0, Message.CONTENT_TYPE_XML, message)) {
							// print error message
							AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread()
									.getName()
									+ "- TalkEndoint.processActionEvent() -- Error sending replace session message to the endpoint");
						}

						saveTranscript(old_session_info.getTranscriptFile(), message, MessageDirection.Outgoing, null,
								null);
						changeSessionId(old_session_id, new_session_id);
					}
					// else, ignore
				} else if (old_session_info != null) {
					// the service controller is telling me to get rid of
					// the old session
					saveTranscript(old_session_info.getTranscriptFile(), replace, MessageDirection.Outgoing, null,
							null);
					removeFromCallList(old_session_id);
					closeTranscript(old_session_info.getTranscriptFile());
				}
			}
			// else, ignore
		}
		return true;
	}

	private boolean processDisconnectEvent(MessageEvent event) {
		// System.out.println(
		// Thread.currentThread().getName()
		// + " In processDisconnectEvent");

		DisconnectMessage message = (DisconnectMessage) event.getMessage();
		long session_id = message.getSessionId();

		SessionInfo session = getSessionInfo(session_id);
		if (session == null) {
			return true;
		}

		removeFromCallList(session_id);

		if (message.getCalledInfo() != null && message.getCalledInfo().getCallParty() != null
				&& message.getCalledInfo().getCallParty().getName() != null) {

			EndPointInfo info = RegisteredEndPointList.Instance()
					.findRegisteredEndPointInfo(message.getCalledInfo().getCallParty().getName());
			if (info != null && info.getUserData().isPrivateInfo()) {
				message.getCalledInfo().setCallParty(new CallPartyElement(message.getCalledInfo().getCallParty()));

				setPrivate(message.getCalledInfo().getCallParty());
			}
		}

		// send the disconnect message to the client
		if (!parent.sendRequestMessageToClient(0, Message.CONTENT_TYPE_XML, message)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processDisconnectEvent() -- Error sending disconnect message to the endpoint");
		}

		saveTranscript(session.getTranscriptFile(), message, MessageDirection.Outgoing, null, null);
		closeTranscript(session.getTranscriptFile());

		if (!registered && message.getCalledInfo() == null) {
			// close the connection
			return false;
		}
		// else, retain the connection

		return true;
	}

	private void closeTranscript(String transcriptFile) {
		if (!registered) {
			return;
		}

		parent.closeFile(transcriptFile, TRANSCRIPT_SUFFIX);
	}

	private boolean processDisconnectMessage(DisconnectMessage message) {
		// System.out.println(Thread.currentThread().getName()
		// + " In processDisconnectMessage");

		// get the call information from the call list
		long sessionId = message.getSessionId();

		SessionInfo sessionInfo = null;
		if (sessionId == -1) { // not specified
			sessionInfo = lastSession;
		} else {
			sessionInfo = getSessionInfo(sessionId);
		}

		if (sessionInfo == null) { // not found
			return true;
		}

		saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Incoming, null, null);

		removeFromCallList(sessionId);

		if (registered) {
			CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (selfInfo != null && selfInfo.isPrivateInfo()) {
				message.setFrom(new CallPartyElement(selfInfo.getName(), null));
			}
		}

		// propagate the message to the service controller
		if (!ServiceController.Instance()
				.sendMessage(new MessageEvent(MessageEvent.DISCONNECT_MESSAGE, parent, message, null))) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processDisconnectMessage() -- Error sending message to the service controller");

			return false;
		}

		closeTranscript(sessionInfo.getTranscriptFile());

		if (!registered) {
			// close the connection
			return false;
		}

		return true;
	}

	private boolean processDropEndpointEvent(DropEndpointEvent event) {
		unregistrationComplete = true;
		return false;
	}

	private boolean processRegistrationRequestMessage(int request_id, RegistrationRequestMessage message) {
		AceNetworkAccess access = TalkPluginApp.Instance().getAccessInfo();
		if (access != null) // specified in the config file
		{
			if (!access.match(host)) {
				// print error message
				AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ "- TalkEndoint.processRegistrationRequestMessage() -- Unauthorized registered user access from host "
								+ host);

				if (!parent.sendResponseMessageToClient(request_id, ResponseMessage.FORBIDDEN,
						java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.plugin.language",
										ServiceController.getLocale((String) parent.getParam("language")))
								.getString("You_are_not_allowed_to_login_from_this_location"),
						Message.CONTENT_TYPE_XML, null)) {
					// print error message
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
							+ "- TalkEndoint.processRegistrationRequestMessage() -- Error sending registration response message to the endpoint");
				}

				return false; // no point continuing
			}
		}

		if (!registered) { // not registered
			registeredUserName = message.getUserName();

			// send the message to the ServiceController
			if (!ServiceController.Instance()
					.sendMessage(new MessageEvent(MessageEvent.REGISTRATION_REQUEST, parent, message, null))) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processRegistrationRequestMessage() -- Error sending registration request message to the service controller");
				return false;
			}
			registrationRequestId = request_id;
		} else {
			// already registered
			// print error message
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRegistrationRequestMessage() -- A registration message is received for a client that is already registered");

			if (!parent.sendResponseMessageToClient(request_id, ResponseMessage.FORBIDDEN,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) parent.getParam("language")))
							.getString("Already_registered"),
					Message.CONTENT_TYPE_XML, null)) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processRegistrationRequestMessage() -- Error sending registration response message to the endpoint");
				return false;
			}
			return true;
		}
		return true;
	}

	private boolean processRegistrationResponseEvent(MessageEvent event) {
		if (registered) {
			// if already registered
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRegistrationResponseEvent() -- A registration response event is received for a client that is already registered");
			return false;
		}

		RegistrationResponseMessage resp_message = (RegistrationResponseMessage) event.getMessage();

		// send the response to the client
		if (!parent.sendResponseMessageToClient(registrationRequestId, event.getResponseStatus(), event.getReason(),
				Message.CONTENT_TYPE_XML, resp_message)) {
			// print an error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRegistrationResponseEvent() -- Error sending registration response message to the endpoint");
			return false;
		}

		if (event.getResponseStatus() == ResponseMessage.OK) {
			registered = true;
			parent.setParam(EndPointInterface.PARAM_SELF_INFO, resp_message.getCallPartyInfo());
		} else {
			return false;
		}

		return true;
	}

	private boolean processRTPEvent(MessageEvent event) {
		RTPMessage message = (RTPMessage) event.getMessage();

		long session_id = message.getSessionId();
		SessionInfo session_info = getSessionInfo(session_id);
		if (session_info == null) { // not found
			return true;
		}

		if (!session_info.isConnected()) {
			// we do not expect any setup response

			// print error message
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRTPEvent() -- An RTP message is received for a call that is not connected");
			// and ignore
			return true;
		}

		CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
		if (selfInfo != null) {
			Map<String, String> variables = new HashMap<String, String>();
			variables.put("cookie", selfInfo.getEndUserCookie());
			variables.put("email", selfInfo.getEmail() != null ? selfInfo.getEmail() : "");
			variables.put("name", selfInfo.getName() != null ? selfInfo.getName() : "");
			variables.put("fullName", selfInfo.getFullName() != null ? selfInfo.getFullName() : "");
			variables.put("ip", selfInfo.getIpAddress() != null ? selfInfo.getIpAddress() : "");
			variables.put("message", selfInfo.getComment() != null ? selfInfo.getComment() : "");
			message = resolvePlaceholders(message, variables);
		}

		// send the message to the client
		if (!parent.sendRequestMessageToClient(0, Message.CONTENT_TYPE_XML, message)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRTPEvent() -- Error sending RTP message to the endpoint");
			return false;
		}

		saveTranscript(session_info.getTranscriptFile(), message, MessageDirection.Outgoing, null, null);

		return true;
	}

	private String formatTranscriptMessage(Object message, MessageDirection direction, Integer status, String reason) {
		StringBuffer buffer = new StringBuffer("<element>\n");
		buffer.append("<timestamp>");
		buffer.append(new Date().getTime());
		buffer.append("</timestamp>\n");
		buffer.append("<direction>");
		buffer.append(direction.name());
		buffer.append("</direction>\n");

		if (status != null) {
			buffer.append("<status>");
			buffer.append(status);
			buffer.append("</status>\n");
		}

		if (status != null) {
			buffer.append("<reason>");
			buffer.append(reason);
			buffer.append("</reason>\n");
		}

		buffer.append("<message>\n");
		buffer.append(XMLSerializer.getInstance().serialize(message));
		buffer.append("\n</message>\n");
		buffer.append("</element>\n");
		return buffer.toString();
	}

	private boolean processRTPMessage(RTPMessage message) {
		// get the call information from the call list
		long sessionId = message.getSessionId();
		SessionInfo sessionInfo = getSessionInfo(sessionId);
		if (sessionInfo == null) { // not found
			return true;
		}

		if (!sessionInfo.isConnected()) {
			// we do not expect any setup response
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRTPMessage() -- An RTP message is received for a call that is not connected");
			// and ignore
			return true;
		}

		List<Integer> cannedList = resolveCannedMediaElements(message);

		handleSpecialContent(message);

		if (!registered) {
			scrubMessage(message, cannedList);
		}

		RTPMessage fromPrivateRTPMessage = message;
		if (registered) {
			CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (selfInfo != null && selfInfo.isPrivateInfo()) {
				fromPrivateRTPMessage = new RTPMessage(message);
				setPrivate(fromPrivateRTPMessage.getFrom());
			}
		}

		// propagate the message to the other endpoints
		int numEndPoints = sessionInfo.numEndPoints();

		for (int i = 0; i < numEndPoints; i++) {
			EndPointInterface endpoint = sessionInfo.elementAt(i);

			if (endpoint == parent) {
				continue;
			}

			RTPMessage rtp = message;
			CallPartyElement toInfo = (CallPartyElement) endpoint.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (toInfo != null && toInfo.getName() == null) {
				rtp = fromPrivateRTPMessage;
			}

			if (!endpoint.sendEvent(new MessageEvent(MessageEvent.RTP_MESSAGE, parent, rtp, null))) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ "- TalkEndoint.processRTPMessage() -- Could not send RTP message to the endpoint "
								+ endpoint);
				// and ignore
			}
		}

		saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Incoming, null, null);

		return true;

	}

	private RTPMessage resolvePlaceholders(RTPMessage message, Map<String, String> variables) {
		boolean cloned = false;
		RTPMessage output = message;
		for (int i = 0; i < output.getMediaElements().getElements().size(); i++) {
			MediaElementInterface medium = output.getMediaElements().getElements().get(i);
			if (medium instanceof HtmlElement
					&& PlaceHolderResolver.placeHoldersExist(((HtmlElement) medium).getHtml())) {
				if (!cloned) {
					output = new RTPMessage(message);
					cloned = true;
				}

				HtmlElement replaced = new HtmlElement(
						PlaceHolderResolver.replace(((HtmlElement) medium).getHtml(), variables));
				output.getMediaElements().getElements().set(i, replaced);
			}
		}

		return output;

	}

	private List<Integer> resolveCannedMediaElements(RTPMessage message) {
		int index = 0;
		List<Integer> cannedList = new ArrayList<Integer>();
		ListIterator<MediaElementInterface> i = message.getMediaElements().getElements().listIterator();
		while (i.hasNext()) {
			MediaElementInterface medium = i.next();
			if (medium instanceof CannedMessageElement) {
				CannedMessageElement canned = (CannedMessageElement) medium;

				MediaElementInterface element = null;
				if (canned.getMessage() == null) {
					String result = SynchronousDbOperations.getInstance().queryCannedMessages(canned.getId());
					if (result != null) {
						element = resolveContent(result);
						if (element instanceof FormDefinitionElement) {
							long formId = SynchronousDbOperations.getInstance().createFormRecord(message.getSessionId(),
									canned.getId());
							if (formId == -1L) {
								// This is unlikely to happen. Will happen when
								// there is a database connectivity issue
								AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
										Thread.currentThread().getName()
												+ "- TalkEndoint.resolveCannedMediaElements() -- Could not create a form record for session "
												+ message.getSessionId());
							} else {
								((FormDefinitionElement) element).setFormId(formId);
							}
						}
					} else {
						AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- TalkEndoint.resolveCannedMediaElements() -- A canned message with group = "
										+ canned.getGroup() + " and id " + canned.getId() + " could not be found."
										+ " Going to ignore");
					}
				} else {
					element = new HtmlElement(canned.getMessage());
				}

				i.remove();
				if (element != null) {
					i.add(element);
					cannedList.add(index);
				}
			}

			index++;
		}

		return cannedList;
	}

	private String[] tokenizeFormDef(String content) {
		Matcher matcher = Pattern.compile("^#form\\|(.*)\\r?\\n((?s:.)*)", Pattern.CASE_INSENSITIVE).matcher(content);
		if (!matcher.matches()) {
			return null;
		}

		return new String[] { matcher.group(1), matcher.group(2) };
	}

	private MediaElementInterface resolveContent(String content) {
		MediaElementInterface element = null;

		String[] tokens = tokenizeFormDef(content);
		if (tokens != null) {
			FormDefinitionElement form = new FormDefinitionElement();
			element = form;
			form.setFormDef(tokens[1]);
		}

		if (element == null) {
			element = new HtmlElement(content);
		}

		return element;
	}

	private void scrubMessage(RTPMessage message, List<Integer> cannedList) {
		int num = message.getMediaElements().getElements().size();
		for (int i = 0; i < num; i++) {
			if (cannedList.contains(i)) {
				// Do not scrub canned media elements
				continue;
			}

			MediaElementInterface medium = message.getMediaElements().getElements().get(i);
			if (medium instanceof HtmlElement) {
				HtmlElement e = (HtmlElement) medium;
				e.setHtml(contentFilter.scrubHtml(e.getHtml()));
			}
		}
	}

	private void handleSpecialContent(RTPMessage message) {
		for (MediaElementInterface e : message.getMediaElements().getElements()) {
			if (e instanceof FormSubmissionElement) {
				processFormSubmission((FormSubmissionElement) e);
			}
		}
	}

	private void processFormSubmission(FormSubmissionElement form) {
		long cannedMessageId = SynchronousDbOperations.getInstance().retrieveFormRecord(form.getFormId());
		if (cannedMessageId == -1L) {
			// The form has already been submitted
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processFormSubmission() -- Could not retrieve form record for " + form.getFormId());
			return;
		}
		
		String result = SynchronousDbOperations.getInstance().queryCannedMessages(cannedMessageId);
		String[] tokens = tokenizeFormDef(result);
		if (tokens != null) {
			String[] splits = tokens[0].split("\\|");
			if (splits.length > 2) {
				// TODO send out email, etc
			}
		}
	}

	private void saveTranscript(String transcriptFile, Object message, MessageDirection direction, Integer status,
			String reason) {
		if (!registered) {
			return;
		}

		if (transcriptFile == null) {
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.saveTranscript() -- Null transcript file specified");
			return;
		}
		parent.writeToFile(transcriptFile, TRANSCRIPT_PREFIX,
				formatTranscriptMessage(message, direction, status, reason));
	}

	private String saveTranscript(long sessionId, String cookie, Object message, MessageDirection direction,
			Integer status, String reason) {
		if (!registered) {
			return null;
		}

		String transFile = formatTranscriptPathName(sessionId, cookie);
		parent.writeToFile(transFile, TRANSCRIPT_PREFIX, formatTranscriptMessage(message, direction, status, reason));
		return transFile;
	}

	private String renameTranscript(String oldTranscriptFile, long newSessionId, String cookie) {
		if (!registered) {
			return null;
		}

		String newTranscriptFile = formatTranscriptPathName(newSessionId, cookie);
		parent.renameFile(oldTranscriptFile, newTranscriptFile);
		return newTranscriptFile;
	}

	private String formatTranscriptPathName(long sessionId, String cookie) {
		try {
			return AceConfigFileHelper.getAcePath("transcripts",
					registeredUserName + "_" + sessionId + (cookie != null ? "_" + cookie : "") + ".xml");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean processSetupRequestEvent(MessageEvent event) {
		// System.out.println(Thread.currentThread().getName()
		// + " In processSetupRequestEvent");
		if (!registered) {
			// something must be wrong

			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupRequestEvent() -- A setup request event is received for a session that is not registered");
			return false;
		}

		EndPointInterface callingParty = event.getFrom();

		SetupRequestMessage incomingMessage = (SetupRequestMessage) event.getMessage();

		long sessionId = incomingMessage.getSessionId();

		// send an ALTERTING message to the calling party
		SetupResponseMessage resp = new SetupResponseMessage();
		resp.setSessionId(sessionId);

		CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
		if (selfInfo != null) {
			CalledNameElement cpElement = new CalledNameElement();
			cpElement.setCallParty(selfInfo);
			resp.setCalledParty(cpElement);
		}

		// send the message
		if (!callingParty.sendEvent(new MessageEvent(MessageEvent.SETUP_RESPONSE, parent, SetupResponseMessage.ALERTING,
				"Alterting", resp, null))) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupRequestEvent() -- Error sending alerting event to the calling party");
			return false;
		}

		// send the message to the client
		if (!parent.sendRequestMessageToClient(0, Message.CONTENT_TYPE_XML, incomingMessage)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupRequestEvent() -- Error sending setup message to the endpoint");

			return false;
		}

		String cookie = null;
		CallPartyElement cp = incomingMessage.getCallingNameElement().getCallParty();
		if (cp != null) {
			cookie = cp.getEndUserCookie();
		}

		String transFile = saveTranscript(sessionId, cookie, incomingMessage, MessageDirection.Outgoing, null, null);

		// create a session
		SessionInfo session = new SessionInfo(sessionId, callingParty);
		session.addEndPoint(parent);
		session.setTranscriptFile(transFile);

		// add it to the session list
		addToCallList(sessionId, session);

		return true;
	}

	private boolean processSetupRequestMessage(int requestId, SetupRequestMessage message) {
		// System.out.println(Thread.currentThread().getName()
		// + " In processSetupRequestMessage");

		if (!registered && chatList.size() > 0) {
			// unregistered users get one call

			// send a response
			parent.sendResponseMessageToClient(requestId, ResponseMessage.FORBIDDEN,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) parent.getParam("language")))
							.getString("Only_one_call_allowed_for_unregistered_user"),
					Message.CONTENT_TYPE_XML, null);
			return false;
		}

		// get a unique session id
		long sessionId = ServiceController.Instance().getNewSessionId();
		message.setSessionId(sessionId); // save the session id

		String transFile = saveTranscript(sessionId, null, message, MessageDirection.Incoming, null, null);

		// send an ACK response
		SetupResponseMessage rsp = new SetupResponseMessage();
		if (!registered) {
			rsp.setCallingCookie(endUserCookie);
		}
		rsp.setSessionId(sessionId);

		if (!parent.sendResponseMessageToClient(requestId, SetupResponseMessage.ACK, "Acknowledgement",
				Message.CONTENT_TYPE_XML, rsp)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupRequestMessage() -- Error sending message to the endpoint");
			return false;
		}

		saveTranscript(transFile, rsp, MessageDirection.Outgoing, SetupResponseMessage.ACK, "Acknowledgement");

		// set unregistered caller's host address in the message before
		// processing it
		if (!registered) {
			message.getCallingNameElement().getCallParty().setIpAddress(host);
			message.getCallingNameElement().getCallParty().setEndUserCookie(endUserCookie);
		}

		// send the message to the service controller
		if (!ServiceController.Instance()
				.sendMessage(new MessageEvent(MessageEvent.SETUP_REQUEST, parent, message, null))) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupRequestMessage() -- Error sending message to the service controller");

			parent.sendResponseMessageToClient(requestId, ResponseMessage.SERVICE_UNAVAILABLE,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) parent.getParam("language")))
							.getString("Unable_to_notify_the_service_controller"),
					Message.CONTENT_TYPE_XML, null);

			return false;
		}

		// if the end point is not registered. save the information about the
		// caller
		if (!registered) {
			parent.setParam(EndPointInterface.PARAM_SELF_INFO, message.getCallingNameElement().getCallParty());
		} else {
			// for registered user, save the information about the caller only
			// if the service controller did not provide such information during
			// the registration.
			CallPartyElement selfInfo = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (selfInfo == null) {
				parent.setParam(EndPointInterface.PARAM_SELF_INFO, message.getCallingNameElement().getCallParty());
			}
		}

		// create a session info
		SessionInfo session = new SessionInfo(sessionId, parent);
		session.setRequestId(requestId);
		session.setTranscriptFile(transFile);

		// and add it to the list of sessions
		addToCallList(sessionId, session);

		lastSession = session;
		return true;
	}

	private boolean processSetupResponseEvent(MessageEvent event) {
		// get the call information from the call list
		SetupResponseMessage message = (SetupResponseMessage) event.getMessage();
		long sessionId = message.getSessionId();
		SessionInfo sessionInfo = getSessionInfo(sessionId);
		if (sessionInfo == null) { // not found
			return true; // ignore
		}

		if (sessionInfo.isConnected()) {
			// we do not expect any setup response

			// print error message
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupResponseEvent() -- A setup response event is received for a call that is already in connected state");
			// and ignore
			return true;
		}

		int status = event.getResponseStatus();
		EndPointInterface from = event.getFrom();

		// process the request
		switch (status) {
		case SetupResponseMessage.ALERTING:
			if (sessionInfo.numEndPoints() <= 1) {
				sessionInfo.addEndPoint(from);
			}

			setupPrivateInfo(message);

			// send the alerting message to the client
			if (!parent.sendResponseMessageToClient(sessionInfo.getRequestId(), status, event.getReason(),
					Message.CONTENT_TYPE_XML, message)) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processSetupResponseEvent() -- Error sending setup response (ALERTING) to the endpoint");
				return false;
			}

			saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Outgoing, status,
					event.getReason());
			break;

		case SetupResponseMessage.PROG:
			if (sessionInfo.numEndPoints() <= 1) {
				sessionInfo.addEndPoint(from);
			}

			setupPrivateInfo(message);

			// send the progress message to the client
			if (!parent.sendResponseMessageToClient(sessionInfo.getRequestId(), status, event.getReason(),
					Message.CONTENT_TYPE_XML, message)) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processSetupResponseEvent() -- Error sending setup response (PROG) to the endpoint");
				return false;
			}
			saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Outgoing, status,
					event.getReason());
			break;

		case SetupResponseMessage.CONNECT:
			if (sessionInfo.numEndPoints() <= 1) {
				sessionInfo.addEndPoint(from);
			}

			setupPrivateInfo(message);

			// send the connect message to the client
			if (!parent.sendResponseMessageToClient(sessionInfo.getRequestId(), status, event.getReason(),
					Message.CONTENT_TYPE_XML, message)) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processSetupResponseEvent() -- Error sending setup response (CONNECT) to the endpoint");
				return false;
			}

			saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Outgoing, status,
					event.getReason());
			sessionInfo.setConnected(true);
			break;

		case SetupResponseMessage.TRANSFER:
			removeFromCallList(sessionId, false); // remove the old session

			setupPrivateInfo(message);

			if (!parent.sendResponseMessageToClient(sessionInfo.getRequestId(), status, event.getReason(),
					Message.CONTENT_TYPE_XML, message)) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ "- TalkEndoint.processSetupResponseEvent() -- Error sending setup response (" + status
								+ ") to the endpoint");
				return false;
			}

			saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Outgoing, status,
					event.getReason());
			closeTranscript(sessionInfo.getTranscriptFile());

			long newSessionId = message.getNewSessionId();
			SessionInfo newSessionInfo = new SessionInfo(newSessionId, parent);
			newSessionInfo.setRequestId(sessionInfo.getRequestId());
			addToCallList(newSessionId, newSessionInfo); // add the new session

			// This is really an event and therefore, must store only the event
			// object, not the message
			String cookie = null;
			if (!registered) {
				CallPartyElement cp = (CallPartyElement) parent.getParam(EndPointInterface.PARAM_SELF_INFO);
				if (cp != null) {
					cookie = cp.getEndUserCookie();
				}
			}

			String transFile = saveTranscript(newSessionId, cookie, event, MessageDirection.Incoming, null, null);
			newSessionInfo.setTranscriptFile(transFile);
			break;

		default:
			// all other cases, received a response that is a terminating
			// response and not going
			// to transition to connected state

			removeFromCallList(sessionId);

			setupPrivateInfo(message);

			// send the message to the client
			if (!parent.sendResponseMessageToClient(sessionInfo.getRequestId(), status, event.getReason(),
					Message.CONTENT_TYPE_XML, message)) {
				// print an error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ "- TalkEndoint.processSetupResponseEvent() -- Error sending setup response (" + status
								+ ") to the endpoint");
				return false;
			}
			saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Outgoing, status,
					event.getReason());
			closeTranscript(sessionInfo.getTranscriptFile());

			if (!registered) {
				return false;
			}

			break;
		}

		return true;
	}

	private void setupPrivateInfo(SetupResponseMessage message) {
		if (registered) {
			return;
		}

		if (message.getCalledParty() == null) {
			return;
		}

		if (message.getCalledParty().getCallParty().getName() == null) {
			return;
		}

		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(message.getCalledParty().getCallParty().getName());
		if (info == null) {
			return;
		}

		if (!info.getUserData().isPrivateInfo()) {
			return;
		}

		CallPartyElement party = new CallPartyElement(message.getCalledParty().getCallParty());
		message.getCalledParty().setCallParty(party);

		setPrivate(party);
	}

	public static void setPrivate(CallPartyElement party) {
		// TODO internationalize the full name
		party.setFullName("Private");
		party.setEmail(null);
		party.setIpAddress(null);
	}

	public boolean processSetupResponseMessage(int status, String reason, SetupResponseMessage message) {
		// System.out.println(Thread.currentThread().getName()
		// + " In processSetupResponseMessage");

		long session_id = message.getSessionId();

		SessionInfo session = getSessionInfo(session_id);

		if (session == null) {
			return true;
		}

		if (session.isConnected()) {
			// we do not expect any setup response

			// print error message
			AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processSetupResponseMessage() -- A setup response message is received for a session that is already connected");

			// and ignore
			return true;
		}

		saveTranscript(session.getTranscriptFile(), message, MessageDirection.Incoming, status, reason);

		switch (status) {
		case SetupResponseMessage.CONNECT:
			session.setConnected(true);

			// propagate the message to the calling party via the service
			// controller
			if (!ServiceController.Instance().sendMessage(
					new MessageEvent(MessageEvent.SETUP_RESPONSE, parent, status, reason, message, null))) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processSetupResponseMessage() -- Error sending message (CONNECT) to the service controller");
			}
			break;

		default:
			// all other cases, received a response that is a terminating
			// response and not going
			// to transition to connected state

			removeFromCallList(session_id);
			closeTranscript(session.getTranscriptFile());

			// propagate the message to the calling party via the service
			// controller
			if (!ServiceController.Instance().sendMessage(
					new MessageEvent(MessageEvent.SETUP_RESPONSE, parent, status, reason, message, null))) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						Thread.currentThread().getName()
								+ "- TalkEndoint.processSetupResponseMessage() -- Error sending message (" + status
								+ ") to the service controller");
			}
			break;
		}

		return true;
	}

	private boolean processUserToUserRequestMessage(int request_id, UserToUserMessage message) {
		String name = message.getEndPointName();
		if (name == null) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processUserToUserRequestMessage() -- user to user message does not contain the end-point name parameter");

			return false;
		}

		EndPointInterface endpoint = RegisteredEndPointList.Instance().findRegisteredEndPoint(name);
		if (endpoint == null) {
			// send a response to the client saying that the end-point is not
			// available
			if (!parent.sendResponseMessageToClient(request_id, ResponseMessage.SERVICE_UNAVAILABLE,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) parent.getParam("language")))
							.getString("The_end_point_is_not_available"),
					Message.CONTENT_TYPE_XML, null)) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processRegistrationRequestMessage() -- Error sending user-to-user response message to the endpoint");
				return false;
			}
			return true;
		}

		MessageEvent me = new MessageEvent(MessageEvent.CLIENT_REQUEST_MESSAGE, parent, message, null, request_id);
		if (!endpoint.sendEvent(me)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					Thread.currentThread().getName()
							+ "- TalkEndoint.processUserToUserRequestMessage() -- Error sending client request message to endpoint "
							+ name);

			// send a response to the client saying that the end-point is not
			// available
			if (!parent.sendResponseMessageToClient(request_id, ResponseMessage.SERVICE_UNAVAILABLE,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) parent.getParam("language")))
							.getString("The_end_point_is_not_available"),
					Message.CONTENT_TYPE_XML, null)) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.processRegistrationRequestMessage() -- Error sending user-to-user response message to the endpoint");
				return false;
			}

			return true;
		}

		return true;
	}

	private boolean processUserToUserResponseMessage(int status, String reason, UserToUserMessage message) {
		String name = message.getEndPointName();
		if (name == null) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processUserToUserResponseMessage() -- user to user message does not contain the end-point name parameter");

			return false;
		}

		EndPointInterface endpoint = RegisteredEndPointList.Instance().findRegisteredEndPoint(name);
		if (endpoint == null) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processRegistrationResponseMessage() -- Endpoint does not exist - coluld not send response event");
			return true;
		}

		if (!endpoint.sendEvent(
				new MessageEvent(MessageEvent.CLIENT_RESPONSE_MESSAGE, parent, status, reason, message, null))) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.processUserToUserResponseMessage() -- Error sending user-to-user response message to the endpoint");
			return false;
		}

		return true;
	}

	private void removeFromCallList(long session_id, boolean notify) {
		chatList.remove(new Long(session_id));

		if (registered) {
			RegisteredEndPointList.Instance().setCallCount(parent, chatList.size());
			if (notify) {
				ServiceController.Instance().groupNotifyOfCallCountChange(parent);
			}
		}
	}

	private void removeFromCallList(long sessionId) {
		removeFromCallList(sessionId, true);
	}

	public boolean requestReceived(int requestId, String contentType, WebMessage body) {
		if (!checkRequestMessage(contentType, body)) {
			return true;
		}

		TalkMessageInterface message = (TalkMessageInterface) body;

		// if this is the first message from the client, it can only be a
		// SetupRequest or a RegistrationRequest message
		if (!firstReqRcvd
				&& !(message instanceof RegistrationRequestMessage || message instanceof SetupRequestMessage)) {

			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- TalkEndoint.requestReceived() -- The first request received from the client is not a setup or a registration request");

			if (!parent.sendResponseMessageToClient(requestId, ResponseMessage.FORBIDDEN, "Bad sequence",
					Message.CONTENT_TYPE_XML, null)) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.requestReceived() -- Error sending forbidden response message to the endpoint");
			}
			return false;
		}
		firstReqRcvd = true;

		if (message instanceof RegistrationRequestMessage) {
			return processRegistrationRequestMessage(requestId, (RegistrationRequestMessage) message);
		} else if (message instanceof SetupRequestMessage) {
			return processSetupRequestMessage(requestId, (SetupRequestMessage) message);
		} else if (message instanceof RTPMessage) {
			return processRTPMessage((RTPMessage) message);
		} else if (message instanceof DisconnectMessage) {
			return processDisconnectMessage((DisconnectMessage) message);
		} else if (message instanceof UserToUserMessage) {
			return processUserToUserRequestMessage(requestId, (UserToUserMessage) message);
		} else {
			if (message instanceof JoinRequestMessage) {
				JoinRequestMessage join = (JoinRequestMessage) message;
				SessionInfo sessionInfo = chatList.get(join.getSessionList().get(0));
				saveTranscript(sessionInfo.getTranscriptFile(), message, MessageDirection.Incoming, null, null);
			}

			// unknown type of message, send it to the service controller
			MessageEvent me = new MessageEvent(MessageEvent.CLIENT_REQUEST_MESSAGE, parent, message, null, requestId);
			if (!ServiceController.Instance().sendMessage(me)) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.requestReceived() -- Error sending client request message to the service controller");
				return false;
			} else {
				return true;
			}
		}
	}

	public boolean responseReceived(int request_id, int status, String reason, String content_type, WebMessage body) {
		// do some error checking
		if (!checkResponseMessage(content_type, body)) {
			return false;
		}

		TalkMessageInterface message = (TalkMessageInterface) body;
		if (message instanceof SetupResponseMessage) {
			return processSetupResponseMessage(status, reason, (SetupResponseMessage) message);
		} else if (message instanceof UserToUserMessage) {
			return processUserToUserResponseMessage(status, reason, (UserToUserMessage) message);
		} else {
			if (!ServiceController.Instance().sendMessage(
					new MessageEvent(MessageEvent.CLIENT_RESPONSE_MESSAGE, parent, status, reason, message, null))) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- TalkEndoint.responseReceived() -- Error sending client response message to the service controller");
				return false;
			} else {
				return true;
			}
		}
	}
}
