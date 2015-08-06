package com.quikj.application.web.talk.plugin;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

import javax.sql.DataSource;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CalledNameElement;
import com.quikj.ace.messages.vo.talk.CallingNameElement;
import com.quikj.ace.messages.vo.talk.ChangePasswordRequestMessage;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.DisconnectReasonElement;
import com.quikj.ace.messages.vo.talk.DndRequestMessage;
import com.quikj.ace.messages.vo.talk.DndResponseMessage;
import com.quikj.ace.messages.vo.talk.GroupActivityMessage;
import com.quikj.ace.messages.vo.talk.GroupMemberElement;
import com.quikj.ace.messages.vo.talk.JoinRequestMessage;
import com.quikj.ace.messages.vo.talk.JoinResponseMessage;
import com.quikj.ace.messages.vo.talk.MailElement;
import com.quikj.ace.messages.vo.talk.RegistrationRequestMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.messages.vo.talk.SendMailRequestMessage;
import com.quikj.ace.messages.vo.talk.SendMailResponseMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.ace.messages.vo.talk.TalkMessageInterface;
import com.quikj.application.web.talk.plugin.accounting.CDRHandler;
import com.quikj.application.web.talk.plugin.accounting.CDRInterface;
import com.quikj.application.web.talk.plugin.accounting.LogoutCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionDisconnectCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionJoinCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionLeaveCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionSetupCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionSetupResponseCDR;
import com.quikj.application.web.talk.plugin.accounting.SessionTransferCDR;
import com.quikj.application.web.talk.plugin.accounting.UnregisteredUserLoginCDR;
import com.quikj.client.raccess.AceRMIImpl;
import com.quikj.client.raccess.RemoteServiceInterface;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailMessage;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;

public class ServiceController extends AceThread implements
		RemoteServiceInterface {

	private Hashtable<Long, SessionInfo> sessionList = new Hashtable<Long, SessionInfo>();

	private Object sessionIdLock = new Object();

	private long sessionId = generateSessionIdRoot();

	private AceSQL database;

	private Hashtable<EndPointInterface, DbOperationInterface> pendingDbOps = new Hashtable<EndPointInterface, DbOperationInterface>();

	private static ServiceController instance = null;

	public ServiceController() throws IOException, AceException {
		super("TalkServiceController");

		FeatureFactory factory = new FeatureFactory();
		if (!factory.init()) {
			throw new AceException(factory.getErrorMessage());
		}

		database = new AceSQL(ApplicationServer.getInstance().getBean(DataSource.class));

		new GroupList();
		new RegisteredEndPointList();

		instance = this;
	}

	private static long generateSessionIdRoot() {
		return (((new Date().getTime() / 1000L) & 0x7FFFFFFFL) << 32);
	}

	public static Locale getLocale(String localeString) {
		if (localeString == null) {
			return Locale.getDefault();
		}

		localeString = localeString.trim();
		if (localeString.toLowerCase().equals("default")) {
			return Locale.getDefault();
		}

		// Extract language
		int languageIndex = localeString.indexOf('_');
		String language = null;
		if (languageIndex == -1) {
			// No further "_" so is "{language}" only
			return new Locale(localeString, "");
		} else {
			language = localeString.substring(0, languageIndex);
		}

		// Extract country
		int countryIndex = localeString.indexOf('_', languageIndex + 1);
		String country = null;
		if (countryIndex == -1) {
			// No further "_" so is "{language}_{country}"
			country = localeString.substring(languageIndex + 1);
			return new Locale(language, country);
		} else {
			// Assume all remaining is the variant so is
			// "{language}_{country}_{variant}"
			country = localeString.substring(languageIndex + 1, countryIndex);
			String variant = localeString.substring(countryIndex + 1);
			return new Locale(language, country, variant);
		}
	}

	public static ServiceController Instance() {
		return instance;
	}

	private boolean addSession(SessionInfo session) {
		Long session_id = new Long(session.getSessionId());
		if (sessionList.get(session_id) == null) {
			sessionList.put(session_id, session);
			return true;
		} else {
			writeErrorMessage(session_id + " already exists", null);
			return false;
		}
	}

	private void cancelDbOperation(EndPointInterface endpoint) {
		// System.out.println ("ServiceController.cancelDbOperation() -
		// canceling operation for endpoint" + endpoint);
		synchronized (pendingDbOps) {
			DbOperationInterface op = (DbOperationInterface) pendingDbOps
					.get(endpoint);
			if (op != null) {
				pendingDbOps.remove(endpoint);
				op.cancel();
			}
		}
	}

	private void checkUnavailableUserTransfer(SessionInfo session,
			String unavail_username, EndPointInterface active_called_endpoint,
			SetupResponseMessage original_setupresp_message,
			int original_setupresp_status, String original_setupresp_reason) {
		DbUnavailableUserTransfer dbOp = new DbUnavailableUserTransfer(
				unavail_username, session, active_called_endpoint,
				original_setupresp_message, original_setupresp_status,
				original_setupresp_reason, this, database);

		if (!dbOp.checkForTransfer()) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.checkUnavailableUserTransfer() (TALK) -- Failure initiating DB check for unavailable user "
									+ unavail_username
									+ ", error : "
									+ dbOp.getLastError());

			if (session.getCallingEndPoint().sendEvent(
					new MessageEvent(MessageEvent.SETUP_RESPONSE, null,
							original_setupresp_status,
							original_setupresp_reason, null, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.checkUnavailableUserTransfer() (TALK) -- Could not send setup response message to the endpoint "
										+ session.getCallingEndPoint());
			}

			// remove the call from the session list
			removeSession(original_setupresp_message.getSessionId());

			return;
		}

		pendingDbOps.put(session.getCallingEndPoint(), dbOp);

		// nothing else to do now, the db operation object will handle the
		// database result
		return;
	}

	public void dispose() {
		// interrupt the wait (kill this thread)
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");
	}

	private void cleanup() {
		// if any database operations are in progress, cancel them
		// System.out.println ("ServiceController.dispose() - start canceling DB
		// ops in progress");
		synchronized (pendingDbOps) {
			Enumeration<DbOperationInterface> ops = pendingDbOps.elements();
			while (ops.hasMoreElements()) {
				ops.nextElement().cancel();
			}
			pendingDbOps.clear();
		}

		// System.out.println ("ServiceController.dispose() - done canceling DB
		// ops in progress");

		// close all billing records
		Enumeration<SessionInfo> e = sessionList.elements();
		while (e.hasMoreElements()) {
			SessionInfo session = e.nextElement();
			sendCDR(new SessionDisconnectCDR(session.getBillingId(), 0));

			int num = session.numEndPoints();
			for (int i = 0; i < num; i++) {
				EndPointInterface ep = session.elementAt(i);
				// if the party is a non-registered user, generate a logout CDR
				if (RegisteredEndPointList.Instance()
						.findRegisteredEndPointInfo(ep) == null) {
					sendCDR(new LogoutCDR(ep.getIdentifier()));
				}
			}

		}

		// generate the logout CDRs
		RegisteredEndPointList.Instance().clearEndpointList();

		RegisteredEndPointList.Instance().dispose();
		GroupList.Instance().dispose();

		database.dispose();

		FeatureFactory.getInstance().dispose();

		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) // if remote service has been started
		{
			rs.unregisterService("com.quikj.application.web.talk.plugin.ServiceController");
		}

		super.dispose();
		instance = null;
	}

	private SessionInfo findSession(long session) {
		return (SessionInfo) sessionList.get(new Long(session));
	}

	public void finishSetupResponse(SetupResponseMessage message, int status,
			String reason, EndPointInterface from) {
		// we may get here immediately from processSetupResponse() or we may get
		// here
		// later after checking for unavailable user transfer then finding out
		// transfer is
		// not applicable

		// verify session exists
		SessionInfo session = findSession(message.getSessionId());

		if (session == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.finishSetupResponse() (TALK) -- Could not find session with id "
									+ message.getSessionId());

			return;
		}

		// send the message over to the calling party
		if (session.getCallingEndPoint().sendEvent(
				new MessageEvent(MessageEvent.SETUP_RESPONSE, from, status,
						reason, message, null)) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.finishSetupResponse() (TALK) -- Could not send setup response message to the endpoint "
									+ session.getCallingEndPoint());

			// remove the call from the session list
			removeSession(message.getSessionId());
			return;
		}

		// Generate CDR
		generateSetupResponseCDR(message, status, from, session);

		if (status == SetupResponseMessage.CONNECT) {
			session.setConnected(true);
		} else {
			// for all other cases, this must be considered the end of the call
			removeSession(message.getSessionId());
		}
	}

	public void generateSetupResponseCDR(SetupResponseMessage message,
			int status, EndPointInterface from, SessionInfo session) {
		String called_name = "unspecified";
		if (from != null) {
			called_name = from.getIdentifier();

		} else if (message.getCalledParty() != null) {
			if (message.getCalledParty().getCallParty() != null) {
				called_name = message.getCalledParty().getCallParty().getName();
			}
		}
		SessionSetupResponseCDR cdr = new SessionSetupResponseCDR(
				session.getBillingId(), called_name, status);
		sendCDR(cdr);
	}

	public AceSQL getDatabase() {
		return database;
	}

	public long getNewSessionId() {
		long sess;
		synchronized (sessionIdLock) {
			sess = sessionId++;
		}
		return sess;
	}

	public String getRMIParam(String param) {
		String str = "logged-in:";
		if (param.startsWith(str) == true) {
			String name = param.substring(str.length());
			if (name.length() > 0) {
				EndPointInterface ep = RegisteredEndPointList.Instance()
						.findRegisteredEndPoint(name);
				if (ep == null) {
					return "no";
				} else {
					return "yes";
				}
			}
		}
		return null;
	}

	private void groupNotifyOfAvailabilityChange(EndPointInfo info,
			boolean available) {
		String username = info.getName();

		String[] notify_endpoints = RegisteredEndPointList.Instance()
				.notifyOfLoginLogout(username);

		if (notify_endpoints != null) {
			GroupActivityMessage ga = new GroupActivityMessage();
			com.quikj.ace.messages.vo.talk.GroupElement ge = new com.quikj.ace.messages.vo.talk.GroupElement();
			GroupMemberElement gm = new GroupMemberElement();

			gm.setUser(username);
			if (available == true) {
				gm.setOperation(GroupMemberElement.OPERATION_ADD_LIST);
				gm.setFullName(info.getUserData().getFullName());
				gm.setAvatar(info.getUserData().getAvatar());
				gm.setCallCount(info.getCallCount());
			} else {
				gm.setOperation(GroupMemberElement.OPERATION_REM_LIST);
			}

			ge.addElement(gm);
			ga.setGroup(ge);

			for (int i = 0; i < notify_endpoints.length; i++) {
				EndPointInterface endpoint = RegisteredEndPointList.Instance()
						.findRegisteredEndPoint(notify_endpoints[i]);
				if (endpoint != null) {
					// send group message to the endpoint

					if (endpoint
							.sendEvent(new MessageEvent(
									MessageEvent.CLIENT_REQUEST_MESSAGE, null,
									ga, null)) == false) {
						// print error message
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										"ServiceController.groupNotifyOfAvailabilityChange (TALK) -- Could not send group activity message to the endpoint "
												+ endpoint);
					}

				}
			}
		}
	}

	private void groupNotifyOfAvailabilityChange(EndPointInterface endpoint,
			boolean available) {
		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(endpoint);
		if (info == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.groupNotifyOfAvailabilityChange() -- EndPoint not registered "
									+ endpoint);

			return;
		}

		groupNotifyOfAvailabilityChange(info, available);
	}

	public void groupNotifyOfAvailabilityChange(String username,
			boolean available) {
		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(username);
		if (info == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.groupNotifyOfAvailabilityChange() -- Username not registered "
									+ username);

			return;

		}

		groupNotifyOfAvailabilityChange(info, available);
	}

	private void groupNotifyOfCallCountChange(EndPointInfo info) {
		String username = info.getName();

		String[] notify_endpoints = RegisteredEndPointList.Instance()
				.notifyOfCallCountChange(username);

		int call_count = info.getCallCount();
		if (call_count == -1) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.groupNotifyOfCallCountChange() -- Could not get new call count for user "
									+ username
									+ ", the user is not in the EndPointList");

			return;
		}

		if (notify_endpoints != null && !info.isDnd()) {
			GroupActivityMessage ga = new GroupActivityMessage();
			com.quikj.ace.messages.vo.talk.GroupElement ge = new com.quikj.ace.messages.vo.talk.GroupElement();
			GroupMemberElement gm = new GroupMemberElement();

			gm.setUser(username);
			gm.setOperation(GroupMemberElement.OPERATION_MOD_LIST);
			gm.setCallCount(info.getCallCount());

			ge.addElement(gm);
			ga.setGroup(ge);

			for (int i = 0; i < notify_endpoints.length; i++) {
				EndPointInterface endpoint = RegisteredEndPointList.Instance()
						.findRegisteredEndPoint(notify_endpoints[i]);

				if (endpoint != null) {
					// send group message to the endpoint

					if (endpoint
							.sendEvent(new MessageEvent(
									MessageEvent.CLIENT_REQUEST_MESSAGE, null,
									ga, null)) == false) {
						// print error message
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										"ServiceController.groupNotifyOfCallCountChange(TALK) -- Could not send group activity message to the endpoint "
												+ endpoint);

					}

				}
			}
		}
	}

	public void groupNotifyOfCallCountChange(EndPointInterface endpoint) {
		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(endpoint);
		if (info == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.groupNotifyOfCallCountChange() -- EndPoint not registered "
									+ endpoint);

			return;
		}

		groupNotifyOfCallCountChange(info);
	}

	private void processChangePasswordRequest(
			ChangePasswordRequestMessage message, EndPointInterface from,
			int req_id) {
		String enc_old_password = message.getOldPassword();
		String old_password = null;
		if (enc_old_password != null) {
			old_password = enc_old_password;
		}

		String enc_new_password = message.getNewPassword();
		String new_password = null;
		if (enc_new_password != null) {
			new_password = enc_new_password;
		}

		DbChangeUserPassword cp = new DbChangeUserPassword(
				message.getUserName(), old_password, new_password, from, this,
				database, req_id, null);

		if (cp.initiate() == false) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processChangePasswordRequest() (TALK) -- Failure initiating change password for "
									+ message.getUserName()
									+ ", error : "
									+ cp.getLastError());

			if (from.sendEvent(new MessageEvent(
					MessageEvent.CLIENT_RESPONSE_MESSAGE,
					null,
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle
							.getBundle(
									"com.quikj.application.web.talk.plugin.language",
									getLocale((String) from
											.getParam("language")))
							.getString(
									"Database_error_occured_while_trying_to_change_password"),
					new RegistrationResponseMessage(), null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processChangePasswordRequest() (TALK) -- Could not send change password response message to the endpoint "
										+ from);
			}
		}
	}

	private void processClientRequestMessage(TalkMessageInterface message,
			EndPointInterface from, int req_id) {
		if ((message instanceof JoinRequestMessage) == true) {
			processJoinRequest((JoinRequestMessage) message, from, req_id);
		} else if ((message instanceof ChangePasswordRequestMessage) == true) {
			processChangePasswordRequest(
					(ChangePasswordRequestMessage) message, from, req_id);
		} else if ((message instanceof SendMailRequestMessage) == true) {
			processSendMailRequest((SendMailRequestMessage) message, from,
					req_id);
		} else if ((message instanceof DndRequestMessage) == true) {
			processDndRequest((DndRequestMessage) message, from, req_id);
		} else {
			// print warning message
			AceLogger
					.Instance()
					.log(AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processClientRequestMessage() (TALK) -- Unknown message of type "
									+ message.getClass().getName()
									+ " received");
		}
	}

	private void processDisconnectRequest(DisconnectMessage message,
			EndPointInterface from) {
		// check for null 'from' party - this could be a transfer due to called
		// party not logged in
		if (from != null) {
			cancelDbOperation(from);
		}

		long session_id = message.getSessionId();

		SessionInfo session = findSession(session_id);

		if (session == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processDisconnectRequest() (TALK) -- Could not find session with id "
									+ session_id);

			return;
		}

		// remove the endpoint from the session
		if (from != null) {
			session.removeEndPoint(from);
		}

		SessionTransferCDR transfer_cdr = null;
		int num_ep = session.numEndPoints();
		if (message.getCalledInfo() != null) { // transfer
			if (num_ep != 1) {
				// if there are more than 1 party or if there are no parties
				// left, remove the called party info, because the call
				// cannot be transferred.
				message.setCalledInfo(null);
				DisconnectReasonElement disc_reason = message
						.getDisconnectReason();
				if (disc_reason == null) {
					// if it was not included
					disc_reason = new DisconnectReasonElement();
					disc_reason
							.setReasonCode(DisconnectReasonElement.SERVER_DISCONNECT);
				}

				disc_reason.setReasonText(java.util.ResourceBundle.getBundle(
						"com.quikj.application.web.talk.plugin.language",
						getLocale((String) session.elementAt(0).getParam(
								"language"))).getString(
						"Transfer_failed_-_cannot_transfer_conference_call"));
				message.setDisconnectReason(disc_reason);
			} else {
				transfer_cdr = new SessionTransferCDR(session.getBillingId(),
						message.getCalledInfo().getCallParty().getName());
				message.setTransferId(transfer_cdr.getIdentifier());

				// default the transferFrom, if not already set
				if (from != null) {
					if (message.getFrom() == null) {
						UserElement udata = RegisteredEndPointList.Instance()
								.findRegisteredUserData(from);
						if (udata != null) {
							message.setFrom(new CallPartyElement(udata
									.getName(), null));
						}
					}
				}
			}
		}

		int code = 0;
		if (message.getDisconnectReason() != null) {
			code = message.getDisconnectReason().getReasonCode();
		}

		if (num_ep == 0) {
			// remove the session
			removeSession(session_id);

			if (transfer_cdr == null) {
				sendCDR(new SessionDisconnectCDR(session.getBillingId(), code));
			} else {
				sendCDR(transfer_cdr);
			}

			// if the from party is a non-registered user, generate a logout CDR
			if (RegisteredEndPointList.Instance().findRegisteredEndPointInfo(
					from) == null) {
				sendCDR(new LogoutCDR(from.getIdentifier()));
			}

		} else if (num_ep == 1) {
			// two-party call or caller
			// being transferred due to
			// called party not logged in

			// remove the session
			removeSession(session_id);

			if (transfer_cdr == null) {
				sendCDR(new SessionDisconnectCDR(session.getBillingId(), code));
			} else {
				sendCDR(transfer_cdr);
			}

			EndPointInterface party = session.elementAt(0);

			// if the from party is a non-registered user, generate a logout CDR
			if (from != null) {
				if (RegisteredEndPointList.Instance()
						.findRegisteredEndPointInfo(from) == null) {
					sendCDR(new LogoutCDR(from.getIdentifier()));
				}
			}

			// if the other party is a non-registered user, generate a logout
			// CDR
			if (RegisteredEndPointList.Instance().findRegisteredEndPointInfo(
					party) == null) {
				sendCDR(new LogoutCDR(party.getIdentifier()));
			}

			// send the DISCONNECT message to the other party
			if (party.sendEvent(new MessageEvent(
					MessageEvent.DISCONNECT_MESSAGE, from, message, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processDisconnectRequest() (TALK) -- Could not send disconnect message to the endpoint "
										+ party);
				return;
			}
		} else {
			// conference scenario
			ConferenceBridge bridge = session.getConferenceBridge();
			if (bridge == null) {
				// something must be wrong
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processDisconnectRequest() (TALK) -- Could not find conference bridge for a multi-party session");
				return;
			}

			bridge.removeEndPoint(from);
			bridge.notifyEndPoints();

			sendCDR(new SessionLeaveCDR(session.getBillingId(),
					from.getIdentifier()));

			// if the from party is a non-registered user, generate a logout CDR
			if (RegisteredEndPointList.Instance().findRegisteredEndPointInfo(
					from) == null) {
				sendCDR(new LogoutCDR(from.getIdentifier()));
			}

			// if there are only two parties left
			if (num_ep == 2) {
				// send message to each of the party to change end-point
				EndPointInterface ep0 = session.elementAt(0);
				EndPointInterface ep1 = session.elementAt(1);

				ActionEvent event = new ActionEvent(null);
				ChangeEndPointAction change = new ChangeEndPointAction(
						session_id, ep1);
				event.addAction(change);

				if (ep0.sendEvent(event) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processDisconnectRequest() (TALK) -- Could not send action event to an endpoint: "
											+ ep0);

					// and continue (??)
				}

				event = new ActionEvent(null);
				change = new ChangeEndPointAction(session_id, ep0);
				event.addAction(change);

				if (ep1.sendEvent(event) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processDisconnectRequest() (TALK) -- Could not send action event to an endpoint: "
											+ ep1);

					// and continue (??)
				}

				// and get rid of the conference bridge
				// by interrupting the wait
				bridge.interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");

				session.setConferenceBridge(null);
			}
		}

	}

	private void processDndRequest(DndRequestMessage message,
			EndPointInterface from, int req_id) {
		boolean enable = message.isEnable();
		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(from);
		if (info == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processDndRequest() (TALK) -- Could not find the endpoint info "
									+ from);

			if (from.sendEvent(new MessageEvent(
					MessageEvent.CLIENT_RESPONSE_MESSAGE, null,
					ResponseMessage.INTERNAL_ERROR, "Internal error",
					new DndResponseMessage(), null, req_id)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processDndRequest() (TALK) -- Could not send DndResponse to  "
										+ from);
			}

			return;
		}

		info.setDnd(enable);
		if (enable == true) {
			groupNotifyOfAvailabilityChange(from, false);
		} else {
			groupNotifyOfAvailabilityChange(from, true);

			if (info.getCallCount() > 0) {
				groupNotifyOfCallCountChange(info);
			}

		}

		if (from.sendEvent(new MessageEvent(
				MessageEvent.CLIENT_RESPONSE_MESSAGE, null, ResponseMessage.OK,
				"OK", new DndResponseMessage(), null, req_id)) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processDndRequest() (TALK) -- Could not send ok response message to the endpoint "
									+ from);
		}
	}

	private void processJoinRequest(JoinRequestMessage message,
			EndPointInterface from, int req_id) {
		int num_sessions = message.getSessionList().size();
		if (num_sessions <= 1) {
			// print warning message
			AceLogger
					.Instance()
					.log(AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processJoinRequest() (TALK) -- Received join request with only one session");

			// send join response
			if (from.sendEvent(new MessageEvent(
					MessageEvent.CLIENT_RESPONSE_MESSAGE, null,
					ResponseMessage.BAD_REQUEST,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							getLocale((String) from.getParam("language")))
							.getString(
									"At_least_two_sessions_must_be_specified"),
					null, null, req_id)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
										+ from);
			}
			return;
		}

		long first_session_id = message.getSessionList().get(0);
		SessionInfo first_session = findSession(first_session_id);
		if (first_session == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processJoinRequest() (TALK) -- Could not find session for a join request");

			// send response
			if (from.sendEvent(new MessageEvent(
					MessageEvent.CLIENT_RESPONSE_MESSAGE,
					null,
					ResponseMessage.BAD_REQUEST,
					java.util.ResourceBundle
							.getBundle(
									"com.quikj.application.web.talk.plugin.language",
									getLocale((String) from
											.getParam("language")))
							.getString(
									"Could_not_get_information_on_the_first_session"),
					null, null, req_id)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
										+ from);
			}

			return;
		}

		ConferenceBridge bridge = null;

		SessionJoinCDR cdr = new SessionJoinCDR();

		// Make sure that all sessions specified belong to the "from" end point
		// and all the sessions are connected.
		// Also, iterate through the session to find if a conferenece bridge
		// is already allocated for any other sessions. In that case, re-use
		// the bridge. If there are more than 1 conference bridge present,
		// get rid of them.
		for (int i = 0; i < num_sessions; i++) {
			long session = message.getSessionList().get(i);
			SessionInfo session_info = findSession(session);
			if (session_info == null) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not find session for a join request");

				// send join response
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.INTERNAL_ERROR,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										getLocale((String) from
												.getParam("language")))
								.getString("Could_not_find_session"), null,
						null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
											+ from);
				}
				return;
			}

			cdr.addSession(session_info.getBillingId());

			// all sessions must be in connected state
			if (session_info.isConnected() == false) {
				// print warning message
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- All specified session in a join request are not connected");

				// send join response
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.BAD_REQUEST,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										getLocale((String) from
												.getParam("language")))
								.getString(
										"All_sessions_must_be_in_connected_state"),
						null, null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
											+ from);
				}
				return;
			}

			// the session must belong to the from end point
			if (session_info.indexOf(from) == -1) {
				// print warning message
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) --  All specified sessions for a join request do not belong to the same end point");

				// send join response
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.BAD_REQUEST,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										ServiceController
												.getLocale((String) from
														.getParam("language")))
								.getString(
										"The_end_point_does_not_have_all_the_specified_sessions"),
						null, null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
											+ from);
				}
				return;
			}

			if (bridge != null) // if a bridge has not been found yet
			{
				bridge = session_info.getConferenceBridge();
			} else {
				// get rid of any other conference bridge since one has been
				// found
				ConferenceBridge conf = session_info.getConferenceBridge();
				if (conf != null) {
					conf.interruptWait(AceSignalMessage.SIGNAL_TERM);
					session_info.setConferenceBridge(null);
				}
			}
		}

		try {
			String bridge_name = "Bridge_" + first_session_id;
			if (bridge == null) // if a bridge has not been found
			{
				bridge = new ConferenceBridge(bridge_name);
				bridge.setSessionId(first_session_id);
				bridge.start();
			} else {
				// change the conference bridge session name
				bridge.setName(bridge_name);
				bridge.setSessionId(first_session_id);
			}
		} catch (IOException ex) {
			// should not happen

			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processJoinRequest() (TALK) -- IO Error occured while creating a conference bridge",
							ex);

			// send join response
			if (from.sendEvent(new MessageEvent(
					MessageEvent.CLIENT_RESPONSE_MESSAGE, null,
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							getLocale((String) from.getParam("language")))
							.getString("Could_not_obtain_a_conference_bridge"),
					null, null, req_id)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
										+ from);
			}

			return;
		}

		// set the conference bridge to the first session
		first_session.setConferenceBridge(bridge);

		// next add all the end-points to the bridge
		// and send messages to the end-point regarding
		// the new configuration
		for (int i = 0; i < num_sessions; i++) {
			long session = message.getSessionList().get(i);
			SessionInfo session_info = findSession(session);
			if (session_info == null) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not find session for a join request");

				// send join response
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.INTERNAL_ERROR,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										getLocale((String) from
												.getParam("language")))
								.getString("Could_not_find_session"), null,
						null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
											+ from);
				}
				return;
			}

			int num_ep = session_info.numEndPoints();
			for (int j = 0; j < num_ep; j++) {
				EndPointInterface ep = session_info.elementAt(j);
				if (!bridge.containsEndPoint(ep)) {
					bridge.addEndPoint(ep);
				}

				// add the end points to the first session
				if (first_session.indexOf(ep) == -1) {
					first_session.addEndPoint(ep);
				}
			}

			// (2) Remove the other sessions from the session list
			if (i > 0) {
				removeSession(session);
			}
		} // for i ...

		// create the action event to be sent
		ActionEvent action = new ActionEvent(null);
		for (int j = 1; j < num_sessions; j++) {
			ReplaceSessionAction replace = new ReplaceSessionAction(message
					.getSessionList().get(j), message.getSessionList().get(0));
			action.addAction(replace);
		}

		ChangeEndPointAction change = new ChangeEndPointAction(
				first_session_id, bridge);
		action.addAction(change);

		// send message to the end-point
		int num_end_points = first_session.numEndPoints();
		for (int i = 0; i < num_end_points; i++) {
			EndPointInterface ep = first_session.elementAt(i);

			// send the event to the end point
			if (ep.sendEvent(action) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processJoinRequest() (TALK) -- Could not send action event to an endpoint: "
										+ ep);

				// and continue (??)
			}
		}

		// send the join response
		JoinResponseMessage response = new JoinResponseMessage();
		for (int i = 0; i < num_sessions; i++) {
			response.getSessionList().add(message.getSessionList().get(i));
		}

		if (!from.sendEvent(new MessageEvent(
				MessageEvent.CLIENT_RESPONSE_MESSAGE, null, ResponseMessage.OK,
				"OK", response, null, req_id))) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processJoinRequest() (TALK) -- Could not send join response message to the endpoint "
									+ from);
		} else {
			bridge.notifyEndPoints();

			sendCDR(cdr);
		}
	}

	private void processMessageEvent(MessageEvent event) {
		int type = event.getEventType();

		switch (type) {
		case MessageEvent.REGISTRATION_REQUEST:
			processRegistrationRequest(
					(RegistrationRequestMessage) event.getMessage(),
					event.getFrom());
			break;

		case MessageEvent.SETUP_REQUEST:
			processSetupRequest((SetupRequestMessage) event.getMessage(),
					event.getFrom());
			break;

		case MessageEvent.SETUP_RESPONSE:
			processSetupResponse((SetupResponseMessage) event.getMessage(),
					event.getResponseStatus(), event.getReason(),
					event.getFrom());
			break;

		case MessageEvent.DISCONNECT_MESSAGE:
			processDisconnectRequest((DisconnectMessage) event.getMessage(),
					event.getFrom());
			break;

		case MessageEvent.CLIENT_REQUEST_MESSAGE:
			processClientRequestMessage(event.getMessage(), event.getFrom(),
					event.getRequestId());
			break;

		default:
			// print error message
			AceLogger.Instance().log(
					AceLogger.WARNING,
					AceLogger.SYSTEM_LOG,
					"ServiceController.processMessageEvent() (TALK)-- Unknown event : "
							+ event.messageType() + " received");
			break;
		}
	}

	private void processRegistrationRequest(RegistrationRequestMessage message,
			EndPointInterface from) {
		// set language information, if any
		if (message.getLanguage() != null) {
			from.setParam("language", message.getLanguage());
		}

		// make sure that it is not a duplicate
		if (RegisteredEndPointList.Instance().findRegisteredEndPoint(
				message.getUserName()) != null) {
			// check if this is a feature
			String[] features = FeatureFactory.getInstance().getFeatureNames();
			int num_features = features.length;

			for (int i = 0; i < num_features; i++) {
				if (features[i].equals(message.getUserName()) == true) {
					if (from.sendEvent(new MessageEvent(
							MessageEvent.REGISTRATION_RESPONSE,
							null,
							ResponseMessage.FORBIDDEN,
							java.util.ResourceBundle
									.getBundle(
											"com.quikj.application.web.talk.plugin.language",
											ServiceController.getLocale((String) from
													.getParam("language")))
									.getString("User_already_registered"),
							null, null)) == false) {
						// print error message
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										"ServiceController.processRegistrationRequest() (TALK) -- Could not send registration response message to the endpoint "
												+ from);
					}

					return;
				}
			}
		}

		String password = null;
		String enc_password = message.getPassword();
		if (enc_password != null) {
			password = enc_password;
		}

		DbEndPointRegistration regRequest = new DbEndPointRegistration(from,
				message.getUserName(), password, this, database);

		if (!regRequest.registerEndPoint()) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processRegistrationRequest() (TALK) -- Failure authenticating user "
									+ message.getUserName()
									+ ", error : "
									+ regRequest.getLastError());

			if (from.sendEvent(new MessageEvent(
					MessageEvent.REGISTRATION_RESPONSE, null,
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) from
									.getParam("language"))).getString(
							"Failure_authenticating_user"), null, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processRegistrationRequest() (TALK) -- Could not send registration response message to the endpoint "
										+ from);
			}

			return;
		}

		pendingDbOps.put(from, regRequest);

		// nothing else to do now, the reg_request object will handle the
		// database result
		return;
	}

	private void processSendMailRequest(SendMailRequestMessage message,
			EndPointInterface from, int req_id) {
		// validate the requesting endpoint
		if (RegisteredEndPointList.Instance().findRegisteredEndPointInfo(from) == null) {
			// log a warning
			AceLogger
					.Instance()
					.log(AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processSendMailRequest() (TALK) -- Send mail request message received from unregistered endpoint "
									+ from);

			// send a response
			if (message.isReplyRequired() == true) {
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.FORBIDDEN,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										ServiceController
												.getLocale((String) from
														.getParam("language")))
								.getString(
										"Unauthorized_send_mail_attempt_rejected"),
						new SendMailResponseMessage(), null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processSendMailRequest() (TALK) -- Could not send unauthorized send-mail response message to the endpoint "
											+ from);
				}
			}
			return;
		}

		if (AceMailService.getInstance() == null) {
			// mail service is not active
			if (message.isReplyRequired() == true) {
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.SERVICE_UNAVAILABLE,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										ServiceController
												.getLocale((String) from
														.getParam("language")))
								.getString("Mail_Service_not_active"),
						new SendMailResponseMessage(), null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processSendMailRequest() (TALK) -- Could not send service unavailable response message to the endpoint "
											+ from);
				}
			}
			return;
		}

		MailElement rcv_mail = message.getMailElement();
		AceMailMessage out_mail = new AceMailMessage();

		out_mail.setSubType(rcv_mail.getSubype());

		int num_items = rcv_mail.numBcc();
		for (int i = 0; i < num_items; i++) {
			out_mail.addBcc(rcv_mail.getBccAt(i));
		}

		num_items = rcv_mail.numCc();
		for (int i = 0; i < num_items; i++) {
			out_mail.addCc(rcv_mail.getCcAt(i));
		}

		num_items = rcv_mail.numTo();
		for (int i = 0; i < num_items; i++) {
			out_mail.addTo(rcv_mail.getToAt(i));
		}

		num_items = rcv_mail.numReplyTo();
		for (int i = 0; i < num_items; i++) {
			out_mail.addReplyTo(rcv_mail.getReplyToAt(i));
		}

		String rcv_body = rcv_mail.getBody();
		if (rcv_body != null) {
			out_mail.setBody(rcv_body);
		}

		String rcv_from = rcv_mail.getFrom();
		if (rcv_from != null) {
			out_mail.setFrom(rcv_from);
		}

		String rcv_subject = rcv_mail.getSubject();
		if (rcv_subject != null) {
			out_mail.setSubject(rcv_subject);
		}

		// enqueue the outgoing mail with the Ace Mail Service

		if (AceMailService.getInstance().addToMailQueue(out_mail) == true) {
			if (message.isReplyRequired() == true) {
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE, null,
						ResponseMessage.OK, "OK",
						new SendMailResponseMessage(), null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processSendMailRequest() (TALK) -- Could not send OK send-mail response message to the endpoint "
											+ from);
				}
			}
		} else {
			if (message.isReplyRequired() == true) {
				if (from.sendEvent(new MessageEvent(
						MessageEvent.CLIENT_RESPONSE_MESSAGE,
						null,
						ResponseMessage.INTERNAL_ERROR,
						java.util.ResourceBundle
								.getBundle(
										"com.quikj.application.web.talk.plugin.language",
										ServiceController
												.getLocale((String) from
														.getParam("language")))
								.getString("Send_mail_attempt_failed"),
						new SendMailResponseMessage(), null, req_id)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processSendMailRequest() (TALK) -- Could not send fail send-mail response message to the endpoint "
											+ from);
				}
			}
		}
	}

	private void processSetupRequest(SetupRequestMessage message,
			EndPointInterface from) {
		// set the language information
		if (from.getParam("language") == null) {
			// not initialized, probably because the user is not a registered
			// user
			if (message.getCallingNameElement() != null) {
				String language = message.getCallingNameElement()
						.getCallParty().getLanguage();
				if (language != null) {
					from.setParam("language", language);
				}
			}
		}

		long session_id = message.getSessionId();
		if (findSession(session_id) != null) {
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(session_id);
			if (from.sendEvent(new MessageEvent(MessageEvent.SETUP_RESPONSE,
					null, ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							getLocale((String) from.getParam("language")))
							.getString("Duplicate_session_id"), response, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processSetupRequest() (TALK) -- Could not send setup response message to the endpoint "
										+ from);
			}
			return;
		}

		if (message.getCallingNameElement().getCallParty().getEndUserCookie() != null) {
			DbBlacklistVerification bl = new DbBlacklistVerification(from,
					message, this, database);
			if (!bl.checkBlacklist()) {
				adjustFailedTransferCallCount(message);
				SetupResponseMessage response = new SetupResponseMessage();
				response.setSessionId(session_id);
				if (!from
						.sendEvent(new MessageEvent(
								MessageEvent.SETUP_RESPONSE,
								null,
								ResponseMessage.INTERNAL_ERROR,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.plugin.language",
												getLocale((String) from
														.getParam("language")))
										.getString("Database_error")
										+ " " + bl.getLastError(), response,
								null))) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"ServiceController.processSetupRequest() (TALK) -- Could not send setup response message to the endpoint "
											+ from);
				}
				return;
			}

			pendingDbOps.put(from, bl);
			return;
		}

		finishChatSetup(message, from);
	}

	public void adjustFailedTransferCallCount(SetupRequestMessage message) {
		// In case of a transfer from a feature (example, operator), the
		// chat count may already have been incremented (to avoid race
		// condition). However if the setup request will not be sent to the
		// called party, the call count must be adjusted
		if (message.getTransferId() == null) {
			return;
		}

		String calledName = message.getCalledNameElement().getCallParty()
				.getName();

		EndPointInterface calledEndpoint = RegisteredEndPointList.Instance()
				.findRegisteredEndPoint(calledName);
		if (calledEndpoint == null) {
			return;
		}

		groupNotifyOfCallCountChange(calledEndpoint);
	}

	public void finishChatSetup(SetupRequestMessage message,
			EndPointInterface from) {

		long session_id = message.getSessionId();
		if (message.getTransferId() == null) {
			// Not a transfer

			// if the calling user is an unregistered user, generate the
			// necessary CDR
			if (RegisteredEndPointList.Instance().findRegisteredEndPointInfo(
					from) == null) {
				// unregistered user
				String name = null;
				String email = null;
				String additional = null;
				String environment = null;
				String ip = null;
				String cookie = null;

				CallingNameElement calling = message.getCallingNameElement();
				if (calling != null) // calling name specified
				{
					name = calling.getCallParty().getFullName();
					email = calling.getCallParty().getEmail();
					additional = calling.getCallParty().getComment();
					environment = calling.getCallParty().getEnvironment();
					ip = calling.getCallParty().getIpAddress();
					cookie = calling.getCallParty().getEndUserCookie();
				}

				UnregisteredUserLoginCDR cdr = new UnregisteredUserLoginCDR(
						from.getIdentifier(), name, email, additional,
						environment, ip, cookie);

				sendCDR(cdr);
			}
		}

		String called_name = message.getCalledNameElement().getCallParty()
				.getName();

		// check if the user is in the registered list
		EndPointInterface called_endpoint = RegisteredEndPointList.Instance()
				.findRegisteredEndPoint(called_name);
		if (called_endpoint == null) {
			adjustFailedTransferCallCount(message);

			// called user not logged in, check for unavailable transfer-to
			// number

			SessionSetupCDR cdr = new SessionSetupCDR(from.getIdentifier(),
					called_name, message.getTransferId());
			sendCDR(cdr);

			// add to the session list
			SessionInfo session = new SessionInfo(session_id, from);
			session.setBillingId(cdr.getIdentifier());
			addSession(session);

			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(session_id);
			response.setCalledParty(message.getCalledNameElement());

			checkUnavailableUserTransfer(
					session,
					called_name,
					null,
					response,
					SetupResponseMessage.UNAVAILABLE,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) from
									.getParam("language"))).getString(
							"User_not_found"));
			// Setup response info may be needed later
			return;
		}

		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(called_endpoint);
		if (info == null) {
			adjustFailedTransferCallCount(message);
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processSetupRequest() (TALK) -- Could not find the end-point info for "
									+ called_endpoint);

			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(session_id);

			// send a response
			if (from.sendEvent(new MessageEvent(
					MessageEvent.SETUP_RESPONSE,
					null,
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle
							.getBundle(
									"com.quikj.application.web.talk.plugin.language",
									getLocale((String) from
											.getParam("language")))
							.getString(
									"Failed_to_send_message_to_the_called_party"),
					response, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processSetupRequest() (TALK) -- Could not send setup response message to the endpoint "
										+ from);
			}
			return;
		}

		if (info.isDnd()) {
			adjustFailedTransferCallCount(message);

			// the user has set DO NOT DISTURB
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(session_id);

			// send a response
			if (!from.sendEvent(new MessageEvent(MessageEvent.SETUP_RESPONSE,
					null, SetupResponseMessage.BUSY,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							getLocale((String) from.getParam("language")))
							.getString("The_user_has_enabled_do_not_disturb"),
					response, null))) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processSetupRequest() (TALK) -- Could not send setup response message to the endpoint "
										+ from);
			}
			return;
		}

		// send the message to the called party
		if (!called_endpoint.sendEvent(new MessageEvent(
				MessageEvent.SETUP_REQUEST, from, message, null))) {
			adjustFailedTransferCallCount(message);
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processSetupRequest() (TALK) -- Could not send setup message to the called endpoint "
									+ called_endpoint);

			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(session_id);

			// send a response
			if (!from
					.sendEvent(new MessageEvent(
							MessageEvent.SETUP_RESPONSE,
							null,
							ResponseMessage.INTERNAL_ERROR,
							java.util.ResourceBundle
									.getBundle(
											"com.quikj.application.web.talk.plugin.language",
											getLocale((String) from
													.getParam("language")))
									.getString(
											"Failed_to_send_message_to_the_called_party"),
							response, null))) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processSetupRequest() (TALK) -- Could not send setup response message to the endpoint "
										+ from);
			}
			return;
		}

		SessionSetupCDR cdr = new SessionSetupCDR(from.getIdentifier(),
				called_endpoint.getIdentifier(), message.getTransferId());
		sendCDR(cdr);

		// add to the session list
		SessionInfo session = new SessionInfo(session_id, from);
		session.setBillingId(cdr.getIdentifier());
		session.addEndPoint(called_endpoint);
		addSession(session);
	}

	private void processSetupResponse(SetupResponseMessage message, int status,
			String reason, EndPointInterface from) {
		SessionInfo session = findSession(message.getSessionId());
		if (session == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processSetupResponse() (TALK) -- Could not find session with id "
									+ message.getSessionId());

			return;
		}

		if ((status == SetupResponseMessage.NOANS)
				|| (status == SetupResponseMessage.BUSY)) {
			// see if called party has unavailable transfer-to number set
			UserElement unavail_user = RegisteredEndPointList.Instance()
					.findRegisteredUserData(from);
			String xferto_name = unavail_user.getUnavailXferTo();

			if (xferto_name != null) {
				if (xferto_name.length() > 0) {
					generateSetupResponseCDR(message, status, from, session);

					// see if xferto user logged in
					UserElement xferto_user = RegisteredEndPointList.Instance()
							.findRegisteredUserData(xferto_name);
					if (xferto_user != null) {
						transferUserUnavailableCall(session, xferto_user, from,
								unavail_user.getName());
						return;
					} else {
						// Start xfer process using original called party name,
						// so caller sees first xfer hop
						checkUnavailableUserTransfer(session,
								unavail_user.getName(), from, message, status,
								reason);
						return;
					}
				}
			}

			finishSetupResponse(message, status, reason, from);

		} else if (status == SetupResponseMessage.TRANSFER) {
			// remove the chat from the session list
			removeSession(session.getSessionId());

			// Create a transfer CDR
			SessionTransferCDR transferCdr = new SessionTransferCDR(
					session.getBillingId(), message.getCalledParty()
							.getCallParty().getName());
			sendCDR(transferCdr);

			// Create a new session id (treat the transfer like a new session
			// request)
			long newSessionId = getNewSessionId();
			message.setNewSessionId(newSessionId);

			// Forward the message to the calling end point
			if (!session.getCallingEndPoint().sendEvent(
					new MessageEvent(MessageEvent.SETUP_RESPONSE, from, status,
							reason, message, null))) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"ServiceController.processSetupResponse() (TALK) -- Could not send setup response message to the endpoint "
										+ session.getCallingEndPoint());
				return;
			}

			// Create a setup request message and send to the transferred
			// endpoint
			SetupRequestMessage req = new SetupRequestMessage();
			req.setSessionId(newSessionId);

			CallPartyElement epInfo = (CallPartyElement) session
					.getCallingEndPoint().getParam(
							EndPointInterface.PARAM_SELF_INFO);
			if (epInfo != null) {
				CallingNameElement cpElement = new CallingNameElement();
				req.setCallingNameElement(cpElement);

				cpElement.setCallParty(epInfo);
			}

			req.setCalledNameElement(message.getCalledParty());

			req.setUserTransfer(true);
			req.setTransferId(transferCdr.getIdentifier());
			req.setTransferFrom(message.getTransferredFrom().getName());

			// handle the setup as if it came from an endpoint
			processSetupRequest(req, session.getCallingEndPoint());
		} else {
			// All other messages
			finishSetupResponse(message, status, reason, from);
		}
	}

	private void processUnregistrationEvent(UnregistrationEvent message) {
		unregisterUser(message.getUser());
	}

	private boolean removeSession(long session) {
		Long session_id = new Long(session);

		if (sessionList.get(session_id) == null) {
			writeErrorMessage(session + " does not exist", null);
			return false;
		}

		sessionList.remove(session_id);
		return true;
	}

	public void run() {
		// start the features
		FeatureFactory.getInstance().startUp();

		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) // if remote service has been started
		{
			rs.registerService(
					"com.quikj.application.web.talk.plugin.ServiceController",
					this);
		}

		while (true) {
			try {
				AceMessageInterface message = waitMessage();
				if (message == null) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- ServiceController.run() -- A null message was received while waiting for a message - "
											+ getErrorMessage());

					break;
				}

				if (message instanceof AceSignalMessage) {
					// A signal message is received

					AceLogger
							.Instance()
							.log(AceLogger.INFORMATIONAL,
									AceLogger.SYSTEM_LOG,
									getName()
											+ " - ServiceController.run() --  A signal "
											+ ((AceSignalMessage) message)
													.getSignalId()
											+ " is received : "
											+ ((AceSignalMessage) message)
													.getMessage());
					break;
				} else if (message instanceof MessageEvent) {
					processMessageEvent((MessageEvent) message);
				} else if (message instanceof UnregistrationEvent) {
					processUnregistrationEvent((UnregistrationEvent) message);
				} else if (message instanceof AceSQLMessage) {
					DbOperationInterface op = (DbOperationInterface) ((AceSQLMessage) message)
							.getUserParm();
					if (op != null) {
						synchronized (pendingDbOps) {
							if (pendingDbOps.contains(op)) {
								if (op.processResponse((AceSQLMessage) message)) {
									pendingDbOps.remove(op.getEndPoint());
								}
							} else {
								op.processResponse((AceSQLMessage) message);
							}
						}
					} else {
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										getName()
												+ "- ServiceController.run() -- No database handler for database event.");
					}
				} else {
					// unexpected event
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- ServiceController.run() -- An unexpected event is received : "
											+ message.messageType());
				}

			} catch (Exception e) {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- ServiceController.run() -- An exception occured while processing queue messages : "
										+ e.getClass().getName(), e);
			}
		}

		cleanup();
	}

	protected void sendCDR(CDRInterface cdr) {
		CDRHandler handler = CDRHandler.getInstance();
		if (handler != null) {
			if (handler.sendCDR(cdr) == false) {
				// print error message
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"ServiceController.sendCDR() (TALK) -- Could not send CDR"
								+ getErrorMessage());
			}
		}
	}

	public boolean sendEvent(AceMessageInterface message) {
		return sendMessage(message);
	}

	public boolean sendRegistrationResponse(EndPointInterface endpoint,
			int response_status, String reason, TalkMessageInterface message,
			Object user_parm) {
		if (endpoint.sendEvent(new MessageEvent(
				MessageEvent.REGISTRATION_RESPONSE, null, response_status,
				reason, message, user_parm)) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.sendRegistrationResponse() -- Could not send registration response message to the endpoint "
									+ endpoint);

			return false;
		}

		return true;
	}

	public boolean sendSetupResponse(EndPointInterface endpoint,
			int responseStatus, String reason, TalkMessageInterface message,
			Object userParm) {
		if (endpoint.sendEvent(new MessageEvent(MessageEvent.SETUP_RESPONSE,
				null, responseStatus, reason, message, userParm)) == false) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- ServiceController.sendSetupResponse() -- Could not send setup response message to the endpoint "
									+ endpoint);
			return false;
		}

		return true;
	}

	public boolean setRMIParam(String param, String value) {
		return false;
	}

	public void transferUserUnavailableCall(SessionInfo session,
			UserElement xferto, EndPointInterface from, String transferFromName) {
		DisconnectMessage message = new DisconnectMessage();
		message.setSessionId(session.getSessionId());
		DisconnectReasonElement reason = new DisconnectReasonElement();
		reason.setReasonCode(DisconnectReasonElement.NORMAL_DISCONNECT);

		String user = null;
		if (xferto.getFullName() != null) {
			user = xferto.getFullName();
		} else {
			user = xferto.getName();
		}

		reason.setReasonText(java.util.ResourceBundle
				.getBundle(
						"com.quikj.application.web.talk.plugin.language",
						ServiceController.getLocale((String) session
								.getCallingEndPoint().getParam("language")))
				.getString(
						"Called_party_unavailable_-_your_session_is_being_transferred_to_")
				+ ' ' + user);
		message.setDisconnectReason(reason);

		CalledNameElement called = new CalledNameElement();
		CallPartyElement party = new CallPartyElement();
		party.setName(xferto.getName());
		party.setFullName(xferto.getFullName());
		called.setCallParty(party);
		message.setCalledInfo(called);

		message.setFrom(new CallPartyElement(transferFromName, null));

		processDisconnectRequest(message, from);

	}

	public void unregisterUser(String user_name) {
		EndPointInfo info = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(user_name);
		if (info == null) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processUnregistrationEvent() (TALK) -- Could not find endpoint info "
									+ user_name
									+ " in the registered end-point list");
			return;
		}

		EndPointInterface endpoint = info.getEndPoint();

		// if db operation in progress, cancel
		cancelDbOperation(endpoint);

		// notify user's group members of logout
		if (info.isDnd() == false) {
			groupNotifyOfAvailabilityChange(user_name, false);
		}

		LogoutCDR cdr = new LogoutCDR(endpoint.getIdentifier());
		// send the CDR to the CDR processing thread
		sendCDR(cdr);

		// remove the user from the registered end points list
		if (RegisteredEndPointList.Instance().removeRegisteredEndPoint(
				user_name) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"ServiceController.processUnregistrationEvent() (TALK) -- Could not remove endpoint "
									+ user_name
									+ " from the registered end-point list");
			return;
		}

		// TODO check if there is any session containing this end-point (not
		// currently required
		// because the end-point sends session disconnects before sending
		// registrations
	}
}
