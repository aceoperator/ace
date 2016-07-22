/*
 * MessageBox.java
 *
 * Created on November 30, 2002, 12:10 PM
 */

package com.quikj.application.web.talk.feature.messagebox.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CalledNameElement;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.messages.vo.talk.HtmlElement;
import com.quikj.ace.messages.vo.talk.MediaElementInterface;
import com.quikj.ace.messages.vo.talk.MediaElements;
import com.quikj.ace.messages.vo.talk.RTPMessage;
import com.quikj.ace.messages.vo.talk.RegistrationRequestMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.application.web.talk.plugin.FeatureInterface;
import com.quikj.application.web.talk.plugin.MessageEvent;
import com.quikj.application.web.talk.plugin.RegisteredEndPointList;
import com.quikj.application.web.talk.plugin.ServiceController;
import com.quikj.application.web.talk.plugin.UnregistrationEvent;
import com.quikj.application.web.talk.plugin.UserElement;
import com.quikj.application.web.talk.plugin.UserTable;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailMessage;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author bhm
 */
public class MessageBox extends AceThread implements FeatureInterface,
		EndPointInterface {
	private static String hostName;

	private static int counter = 0;

	private static Object counterLock = new Object();

	private String identifier;

	private String userName = null;

	private CallPartyElement selfInfo;

	private boolean registered = false;

	private String password;

	private String defaultFrom;

	private Object paramLock = new Object();

	private HashMap keyValuePair = new HashMap();

	private int msgCountSuccess;

	private Hashtable callList = new Hashtable(); // key = session ID, value =

	// CallInfo
	private Hashtable pendingDbOps = new Hashtable(); // key = session ID, value
														// = DbOperation

	private class DbOperation {
		private CallInfo call;

		private AceSQL database;

		private long operationId;

		private String lastError = "";

		public DbOperation(CallInfo call, AceSQL database) {
			this.call = call;
			this.database = database;
		}

		public void cancel() {
			database.cancelSQL(operationId, MessageBox.this);
		}

		public String getLastError() {
			return lastError;
		}

		public long getSessionId() {
			return call.getSessionId();
		}

		public boolean initiate() {
			SQLParam[] statements = new SQLParam[1];
			statements[0] = UserTable.getQueryStatement(call.getMailboxUser());

			operationId = database
					.executeSQL(MessageBox.this, null, statements);

			if (operationId == -1L) {
				lastError = MessageBox.this.getErrorMessage();
				return false;
			}

			return true;
		}

		public boolean processResponse(AceSQLMessage message) {
			// returns done or not
			if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								MessageBox.this.getName()
										+ "- DbOperation.processResponse() -- Database error result.");

				// send a response to the client
				MessageBox.this
						.dropCall(
								call.getSessionId(),
								DisconnectReasonElement.SERVER_DISCONNECT,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.feature.messagebox.server.language",
												ServiceController
														.getLocale((String) call
																.getEndpoint()
																.getParam(
																		"language")))
										.getString(
												"Message_processing_database_error_encountered._Please_try_your_call_again_later."));

				return true;
			}

			List<Object> results = message.getResults();
			if (results == null || results.size() == 0 || results.get(0) == null) {
				// send a response to the client
				// note valid race condition - user just deleted
				MessageBox.this
						.dropCall(
								call.getSessionId(),
								DisconnectReasonElement.SERVER_DISCONNECT,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.feature.messagebox.server.language",
												ServiceController
														.getLocale((String) call
																.getEndpoint()
																.getParam(
																		"language")))
										.getString(
												"Message_collection_not_possible_for_this_user._Please_try_your_call_again_later."));

				return true;
			}

			UserElement userData = (UserElement) results.get(0);
			userData.setName(call.getMailboxUser());

			if ((userData.getAddress() == null)
					|| (userData.getAddress().length() == 0)) {
				MessageBox.this
						.dropCall(
								call.getSessionId(),
								DisconnectReasonElement.SERVER_DISCONNECT,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.feature.messagebox.server.language",
												ServiceController
														.getLocale((String) call
																.getEndpoint()
																.getParam(
																		"language")))
										.getString(
												"Message_collection_is_not_enabled_for_this_user._Please_try_your_call_again_later."));
			} else {
				call.setMailboxAddress(userData.getAddress());
				MessageBox.this.answerCall(call);
			}

			return true;
		}

	}

	public MessageBox() {
		super("MessageBox");
	}

	private void addToCallList(long session_id, CallInfo call) {
		callList.put(new Long(session_id), call);

		RegisteredEndPointList.Instance().setCallCount(this, callList.size());
		ServiceController.Instance().groupNotifyOfCallCountChange(this);
	}

	private void answerCall(CallInfo call) {
		SetupResponseMessage response = new SetupResponseMessage();
		response.setSessionId(call.getSessionId());

		CalledNameElement cp = new CalledNameElement();
		cp.setCallParty(selfInfo);
		cp.setTerminal("com.quikj.application.web.talk.feature.messagebox.client.MessageBoxClient");
		response.setCalledParty(cp);

		MediaElements media = new MediaElements();
		HtmlElement helem = new HtmlElement();
		helem.setHtml(java.util.ResourceBundle
				.getBundle(
						"com.quikj.application.web.talk.feature.messagebox.server.language",
						ServiceController.getLocale((String) call.getEndpoint()
								.getParam("language")))
				.getString(
						"No_one_is_available._To_leave_a_message,_type_the_message_below_and_click_on_the_\"Send\"_button"));
		media.getElements().add(helem);
		response.setMediaElements(media);

		// send the message to the calling party via the service controller
		if (ServiceController
				.Instance()
				.sendMessage(
						new MessageEvent(
								MessageEvent.SETUP_RESPONSE,
								this,
								SetupResponseMessage.CONNECT,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.feature.messagebox.server.language",
												ServiceController
														.getLocale((String) call
																.getEndpoint()
																.getParam(
																		"language")))
										.getString("Answered"), response, null)) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- MessageBox.answerCall() -- Error sending message (CONNECT) to the service controller");

			return;
		}

		call.setConnected(true);

	}

	public void clearStatsCounts() {
		msgCountSuccess = 0;
	}

	public void dispose() {
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");
	}

	private void cleanup() {
		// drop active calls and any DB operations in progress
		synchronized (callList) {
			Enumeration calls = callList.elements(); // full list to process

			while (calls.hasMoreElements() == true) {
				CallInfo call_info = (CallInfo) calls.nextElement();

				DbOperation db_op = (DbOperation) pendingDbOps.get(new Long(
						call_info.getSessionId()));
				if (db_op != null) {
					db_op.cancel();
				}

				DisconnectMessage message = new DisconnectMessage();
				message.setSessionId(call_info.getSessionId());
				DisconnectReasonElement disc_element = new DisconnectReasonElement();
				disc_element
						.setReasonCode(DisconnectReasonElement.SERVER_DISCONNECT);
				disc_element
						.setReasonText(java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.feature.messagebox.server.language",
										ServiceController
												.getLocale((String) call_info
														.getEndpoint()
														.getParam("language")))
								.getString("Message_Service_disconnected"));
				message.setDisconnectReason(disc_element);

				if (ServiceController.Instance().sendMessage(
						new MessageEvent(MessageEvent.DISCONNECT_MESSAGE, this,
								message, null)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- MessageBox.dispose() -- Error sending disconnect message to the service controller");
				}
			}

			callList.clear();
			pendingDbOps.clear();
		}

		if (registered == true) {
			// send unregistration message
			if (ServiceController.Instance() != null) {
				if (ServiceController.Instance().sendMessage(
						new UnregistrationEvent(userName)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- MessageBox.dispose() -- Error sending unregistration message to the service controller");
				}
			}

			registered = false;
		}

		super.dispose();
	}

	private void dropCall(long session_id, int reason_code, String reason_text) {
		// send a disconnect message to the caller

		DisconnectMessage message = new DisconnectMessage();
		message.setSessionId(session_id);
		DisconnectReasonElement disc_reason = new DisconnectReasonElement();
		disc_reason.setReasonCode(reason_code);
		disc_reason.setReasonText(reason_text);
		message.setDisconnectReason(disc_reason);

		if (ServiceController.Instance().sendMessage(
				new MessageEvent(MessageEvent.DISCONNECT_MESSAGE, this,
						message, null)) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- MessageBox.dropCall() -- Error sending disconnect message to the service controller");
		}

		// remove the call from the list
		synchronized (callList) {
			removeFromCallList(session_id);
		}
	}

	private CallInfo getCallInfo(long session_id) {
		return (CallInfo) callList.get(new Long(session_id));
	}

	public String getIdentifier() {
		return identifier;
	}

	public Object getParam(String key) {
		synchronized (keyValuePair) {
			return keyValuePair.get(key);
		}
	}

	public String getUserName() {
		return userName;
	}

	public boolean init(String name, Map params) {
		if (AceMailService.getInstance() == null) // mail service is not active
		{
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"Feature "
									+ name
									+ " error : Ace mail service must be active for this feature to operate");

			return false;
		}

		userName = name;

		if (initParams(params) == false) {
			return false;
		}

		if (hostName == null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				hostName = "Unknown";
			}
		}

		synchronized (counterLock) {
			identifier = hostName + ":feature:" + userName + ":"
					+ (new java.util.Date()).getTime() + ":" + counter++;
		}

		// send registration message to the Service Controller
		RegistrationRequestMessage reg = new RegistrationRequestMessage();
		reg.setUserName(name);
		reg.setPassword(password);

		boolean ret = ServiceController.Instance().sendEvent(
				new MessageEvent(MessageEvent.REGISTRATION_REQUEST, this, reg,
						null));

		if (ret == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- MessageBox.init() -- could not send registration message to the service controller");
			return false;
		}

		return true;
	}

	private boolean initParams(Map params) {
		synchronized (paramLock) {
			password = (String) params.get("password");

			defaultFrom = (String) params.get("from");
			if (defaultFrom != null) {
				try {
					InternetAddress addr = new InternetAddress(defaultFrom);
				} catch (AddressException ex) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"Feature "
											+ userName
											+ " error : The format of the 'from' parameter is not a valid internet email address");

					return false;
				}
			}
		}

		return true;
	}

	private boolean processDisconnectMessage(MessageEvent event) {
		if ((event.getMessage() instanceof DisconnectMessage) == true) {
			DisconnectMessage message = (DisconnectMessage) event.getMessage();
			long session_id = message.getSessionId();

			CallInfo call = getCallInfo(session_id);
			if (call == null) {
				return true;
			}

			// check for DB op & cancel, remove call from list
			synchronized (callList) {
				DbOperation db_op = (DbOperation) pendingDbOps.get(new Long(
						session_id));
				if (db_op != null) {
					db_op.cancel();
					pendingDbOps.remove(new Long(session_id));
				}

				// remove the call from the list
				removeFromCallList(session_id);
			}
		}

		return true;
	}

	private boolean processMessageEvent(MessageEvent message) {
		switch (message.getEventType()) {
		case MessageEvent.REGISTRATION_RESPONSE:
			return processRegistrationResponseEvent(message);

		case MessageEvent.SETUP_REQUEST:
			return processSetupRequestEvent(message);

		case MessageEvent.DISCONNECT_MESSAGE:
			return processDisconnectMessage(message);

		case MessageEvent.RTP_MESSAGE:
			return processRTPMessage(message);

		default:
			// ignore other messages
			break;
		}

		return true; // ignore unknown message event
	}

	private boolean processRegistrationResponseEvent(MessageEvent event) {
		if (registered == true) // if already registered
		{
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- MessageBox.processRegistrationResponseEvent() -- A registration response event is received for this feature that is already registered");
			return false;
		}

		// check the status
		if (event.getResponseStatus() != ResponseMessage.OK) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- MessageBox.processRegistrationResponseEvent() --  Registration failed, status: "
									+ event.getResponseStatus());
			return false;
		}

		RegistrationResponseMessage resp_message = (RegistrationResponseMessage) event
				.getMessage();
		if (resp_message != null) {
			selfInfo = resp_message.getCallPartyInfo();
		}

		registered = true;
		return true;
	}

	private boolean processRTPMessage(MessageEvent event) {
		if ((event.getMessage() instanceof RTPMessage) == true) {
			RTPMessage message = (RTPMessage) event.getMessage();

			long session_id = message.getSessionId();

			CallInfo call_info = getCallInfo(session_id);
			if (call_info == null) // not found
			{
				return true;
			}

			if (call_info.isConnected() == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- MessageBox.processRTPMessage() -- An RTP message is received for a call that is not connected");
				// and ignore
				return true;
			}

			// process the message and send the email - respond with
			// DisconnectMessage regardless
			MediaElements media = message.getMediaElements();
			int num = media.getElements().size();

			for (int i = 0; i < num; i++) {
				MediaElementInterface medium = media.getElements().get(i);
				if ((medium instanceof HtmlElement) == true) {
					String text = ((HtmlElement) medium).getHtml();

					if (text != null) {
						// build and send the email
						AceMailMessage out_mail = new AceMailMessage();

						out_mail.addTo(call_info.getMailboxAddress());
						if ((call_info.getFromAddress() == null)
								|| ((call_info.getFromAddress()).length() < 1)) {
							if (defaultFrom != null) {
								out_mail.setFrom(defaultFrom);
							}
						} else {
							out_mail.setFrom(call_info.getFromAddress());
						}
						out_mail.setReplyTo(new Vector());
						out_mail.addReplyTo(out_mail.getFrom());

						out_mail.setBody(text);

						out_mail.setSubject("ACE MAILBOX MESSAGE");

						if (AceMailService.getInstance().addToMailQueue(
								out_mail) == true) {
							// send successful Disconnect to caller
							dropCall(
									session_id,
									DisconnectReasonElement.NORMAL_DISCONNECT,
									java.util.ResourceBundle
											.getBundle(
													"com.quikj.application.web.talk.feature.messagebox.server.language",
													ServiceController
															.getLocale((String) call_info
																	.getEndpoint()
																	.getParam(
																			"language")))
											.getString(
													"Your_message_is_being_sent"));
							msgCountSuccess++;

						} else {
							dropCall(
									session_id,
									DisconnectReasonElement.SERVER_DISCONNECT,
									java.util.ResourceBundle
											.getBundle(
													"com.quikj.application.web.talk.feature.messagebox.server.language",
													ServiceController
															.getLocale((String) call_info
																	.getEndpoint()
																	.getParam(
																			"language")))
											.getString(
													"Sorry,_your_message_couldn't_be_sent"));
						}
					}
				}
			}
		}

		return true;
	}

	private boolean processSetupRequestEvent(MessageEvent event) {
		if ((event.getMessage() instanceof SetupRequestMessage) == true) {
			if (registered == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- MessageBox.processSetupRequestEvent() -- A setup request event is received but this endpoint is not registered");
				return false;
			}

			EndPointInterface calling_party = event.getFrom();

			SetupRequestMessage incoming_message = (SetupRequestMessage) event
					.getMessage();
			long session_id = incoming_message.getSessionId();
			String messagebox_user = incoming_message.getTransferFrom();

			if (messagebox_user == null) {
				// log an error, the transfer from info should be there
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- MessageBox.processSetupRequestEvent() -- No transfer-from info available");

				dropCall(
						session_id,
						DisconnectReasonElement.SERVER_DISCONNECT,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.feature.messagebox.server.language",
										ServiceController
												.getLocale((String) calling_party
														.getParam("language")))
								.getString("Message_processing_failure"));

				return true;
			}

			// if the user is logged in, their email address is immediately
			// available
			UserElement user_data = RegisteredEndPointList.Instance()
					.findRegisteredUserData(messagebox_user);

			if (user_data != null) {
				if ((user_data.getAddress() == null)
						|| (user_data.getAddress().length() == 0)) {
					dropCall(
							session_id,
							DisconnectReasonElement.SERVER_DISCONNECT,
							java.util.ResourceBundle
									.getBundle(
											"com.quikj.application.web.talk.feature.messagebox.server.language",
											ServiceController
													.getLocale((String) calling_party
															.getParam("language")))
									.getString(
											"Message_service_is_not_enabled_for_this_user._Please_try_your_call_again_later."));
				} else {
					// create a call
					CallInfo call = new CallInfo(session_id, calling_party,
							messagebox_user);
					call.setMailboxAddress(user_data.getAddress());
					if (incoming_message.getCallingNameElement() != null)
						if (incoming_message.getCallingNameElement()
								.getCallParty() != null) {
							call.setFromAddress(incoming_message
									.getCallingNameElement().getCallParty()
									.getEmail());
						}

					synchronized (callList) {
						addToCallList(session_id, call);
					}

					answerCall(call);
				}

				return true;
			}

			// need to go to the database to get the user's email address

			CallInfo call = new CallInfo(session_id, calling_party,
					messagebox_user);
			if (incoming_message.getCallingNameElement() != null)
				if (incoming_message.getCallingNameElement().getCallParty() != null) {
					call.setFromAddress(incoming_message
							.getCallingNameElement().getCallParty().getEmail());
				}

			DbOperation op = new DbOperation(call, ServiceController.Instance()
					.getDatabase());

			if (op.initiate() == false) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"MessageBox.processSetupRequestEvent() -- Failure initiating DB operation for user "
										+ messagebox_user
										+ ", error : "
										+ op.getLastError());

				dropCall(
						session_id,
						DisconnectReasonElement.SERVER_DISCONNECT,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.feature.messagebox.server.language",
										ServiceController
												.getLocale((String) calling_party
														.getParam("language")))
								.getString(
										"Message_processing_database_failure"));

				return true;
			}

			synchronized (callList) {
				pendingDbOps.put(new Long(session_id), op);
				addToCallList(session_id, call);
			}

			// nothing else to do now, the db object will handle the database
			// result
		}

		return true;
	}

	private void removeFromCallList(long session_id) {
		if (callList.remove(new Long(session_id)) != null) {
			RegisteredEndPointList.Instance().setCallCount(this,
					callList.size());
			ServiceController.Instance().groupNotifyOfCallCountChange(this);
		}
	}

	public void removeParam(String key) {
		synchronized (keyValuePair) {
			keyValuePair.remove(key);
		}
	}

	public void resynchParam(Map params) {
		initParams(params);
	}

	public void run() {
		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- MessageBox.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if ((message instanceof AceSignalMessage) == true) {
				// A signal message is received

				// print informational message
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						getName() + " - MessageBox.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());
				break;
			} else if (message instanceof MessageEvent) {
				boolean ret = processMessageEvent((MessageEvent) message);
				if (ret == false) {
					break;
				}
			} else if (message instanceof AceSQLMessage) {
				DbOperation op = (DbOperation) ((AceSQLMessage) message)
						.getUserParm();
				if (op != null) {
					synchronized (callList) {
						if (pendingDbOps.contains(op)) {
							if (op.processResponse((AceSQLMessage) message)) {
								pendingDbOps
										.remove(new Long(op.getSessionId()));
							}
						} else {
							AceLogger
									.Instance()
									.log(AceLogger.ERROR,
											AceLogger.SYSTEM_LOG,
											getName()
													+ "- MessageBox.run() -- database handler not in db operation list.");
						}
					}
				} else {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- MessageBox.run() -- No database handler for database event.");
				}
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- MessageBox.run() -- An unexpected message was received while waiting for a message - "
										+ message.messageType());
			}

		} // while

		cleanup();
	}

	public boolean sendEvent(AceMessageInterface message) {
		return super.sendMessage(message);
	}

	public void setParam(String key, Object value) {
		synchronized (keyValuePair) {
			keyValuePair.put(key, value);
		}
	}

	public void start() {
		super.start();
	}
}
