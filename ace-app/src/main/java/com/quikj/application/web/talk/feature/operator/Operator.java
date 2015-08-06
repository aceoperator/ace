/*
 * Operator.java
 *
 * Created on May 13, 2002, 7:23 AM
 */

package com.quikj.application.web.talk.feature.operator;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.CalledNameElement;
import com.quikj.ace.messages.vo.talk.DisconnectMessage;
import com.quikj.ace.messages.vo.talk.GroupActivityMessage;
import com.quikj.ace.messages.vo.talk.GroupElement;
import com.quikj.ace.messages.vo.talk.GroupMemberElement;
import com.quikj.ace.messages.vo.talk.HtmlElement;
import com.quikj.ace.messages.vo.talk.MediaElements;
import com.quikj.ace.messages.vo.talk.RegistrationRequestMessage;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.application.web.talk.plugin.EndPointInfo;
import com.quikj.application.web.talk.plugin.RegisteredEndPointList;
import com.quikj.application.web.talk.plugin.FeatureInterface;
import com.quikj.application.web.talk.plugin.GatekeeperInterface;
import com.quikj.application.web.talk.plugin.MessageEvent;
import com.quikj.application.web.talk.plugin.OPMUtil;
import com.quikj.application.web.talk.plugin.ServiceController;
import com.quikj.application.web.talk.plugin.UnregistrationEvent;
import com.quikj.application.web.talk.plugin.UserElement;
import com.quikj.client.raccess.AceRMIImpl;
import com.quikj.client.raccess.RemoteServiceInterface;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;
import com.quikj.server.framework.AceTimer;
import com.quikj.server.framework.AceTimerMessage;

/**
 * 
 * @author amit
 */
public class Operator extends AceThread implements FeatureInterface,
		EndPointInterface, RemoteServiceInterface, GatekeeperInterface {
	private static String hostName;

	private static int counter = 0;

	private static Object counterLock = new Object();

	private String identifier;

	private String userName = null;

	private LinkedList<OperatorElement> operatorQueue = new LinkedList<OperatorElement>();

	private LinkedList<SubscriberElement> subscriberQueue = new LinkedList<SubscriberElement>();

	private int maxSessionsPerOperator = 1;

	private CallPartyElement selfInfo;

	private boolean registered = false;

	private int maxOperators = -1;

	private int maxQSize = -1;

	private String password;

	private Object paramLock = new Object();

	private HashMap<String, Object> keyValuePair = new HashMap<String, Object>();

	// Timer IDs
	private static final int CALL_Q_MESSAGE_TIMER = 0; // timer parm

	// OPM_STORAGE_INTERVAL must divide evenly into 60
	private static final int OPM_TIMER = 1; // timer parm

	// call queue message timing
	private static final long CALL_Q_MESSAGE_INTERVAL = 2 * 60 * 1000L;
	
	private int callQMessageTimerId = -1;

	// operational measurements collection
	private Date opmCollectionTime; // collect at top of each minute

	private static final int OPM_STORAGE_INTERVAL = 15; // store in DB every 15

	// min
	private int opmCollectionTimerId = -1;

	private OPMUtil opms;

	private static final String OPM_TABLE_NAME = "opm_operator_tbl";

	private static final String OPM_ACTIVE_OPERATOR_COUNT = "actv_ops"; // sampled

	private static final String OPM_USERS_WAITING = "users_waiting"; // sampled

	private static final String OPM_USERS_TALKING = "users_talking"; // sampled

	private static final String OPM_USER_WAIT_TIME = "user_wait_time";

	public Operator() throws IOException {
		super("TalkFeatureOperator");
	}

	private void addToQueue(GroupMemberElement gel) {
		OperatorElement operator = new OperatorElement();
		operator.setOperatorInfo(gel);

		ListIterator<OperatorElement> iter = operatorQueue.listIterator(0);
		boolean added = false;

		int call_count = gel.getCallCount();
		while (iter.hasNext()) {
			GroupMemberElement element = iter.next().getOperatorInfo();
			if (element.getCallCount() > call_count) {
				try {
					iter.previous(); // go back to the previous entry
					iter.add(operator);
				} catch (NoSuchElementException ex) {
					operatorQueue.addFirst(operator);
				}
				added = true;
				break;
			}
		}

		if (added == false) {
			operatorQueue.addLast(operator);
		}
	}

	private void adjustQueueEntry(GroupMemberElement operator) {
		ListIterator<OperatorElement> iter = operatorQueue.listIterator(0);
		String name = operator.getUser();
		int call_count = operator.getCallCount();
		boolean found = false;

		OperatorElement element = null;
		while (iter.hasNext()) {
			element = iter.next();
			if (name.equals(element.getOperatorInfo().getUser())) {
				if (call_count == element.getOperatorInfo().getCallCount()) {
					// if there is no change in count, there is nothing to
					// be done
					return;
				} else {
					iter.remove(); // remove it temporarily
					found = true;
					break;
				}
			}
		} // end while

		if (found) {
			// copy the operator name to the new element
			operator.setFullName(element.getOperatorInfo().getFullName());
			addToQueue(operator);
		} else {
			AceLogger
					.Instance()
					.log(AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							Thread.currentThread().getName()
									+ "- Operator.adjustQueueEntry() -- Adjusting queue for operator "
									+ name
									+ ", but that operator wasn't in the queue");
		}
	}

	public boolean allow(EndPointInterface ep, EndPointInfo info) {
		if (operatorQueue.size() >= maxOperators) {

			for (OperatorElement e : operatorQueue) {
				if (e.getOperatorInfo().getUser()
						.equals(info.getUserData().getName())) {
					// The same user is logging in again, the service controller
					// will remove the previous session, all the user to login
					return true;
				}
			}

			return false;
		}

		return true;
	}

	private void checkQueueStatus(boolean processing_setup_request) {
		if (subscriberQueue.size() <= 0) { // no one there
			return;
		}

		if (operatorQueue.size() <= 0) { // no operator
			dropAllSubscribers();
			return;
		}

		OperatorElement operator = operatorQueue.removeFirst();

		if (operator.getOperatorInfo().getCallCount() >= maxSessionsPerOperator) {
			// if the operators' hands are full
			operatorQueue.addFirst(operator); // add him back
			return;
		}

		// subscriber available, operator available, transfer call
		SubscriberElement subscriber = (SubscriberElement) subscriberQueue
				.removeFirst();

		if (transferSessionToOperator(subscriber, operator.getOperatorInfo())) {
			operator.getOperatorInfo().setCallCount(
					operator.getOperatorInfo().getCallCount() + 1);

			// add operator back to queue with re-adjusted count
			addToQueue(operator.getOperatorInfo());

			// peg wait time opm
			int wait_time = (int) (new Date().getTime() - subscriber
					.getStartWaitTime()) / 1000;
			opms.collectOPM(OPM_USER_WAIT_TIME, wait_time);
		} else { // call transfer failed
			operatorQueue.addFirst(operator); // add him back
			subscriberQueue.addFirst(subscriber);
		}
	}

	public void clearStatsCounts() {
	}

	public void dispose() {
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");
	}

	private void cleanup() {

		// drop any calls that may be in the subscriber queue
		dropAllSubscribers();

		if (registered) {
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
											+ "- Operator.dispose() -- Error sending unregistration message to the service controller");
				}
			}

			AceRMIImpl rs = AceRMIImpl.getInstance();
			if (rs != null) { // if remote service has been started
				rs.unregisterService("com.quikj.application.web.talk.feature.operator.Operator:"
						+ userName);
			}

			if (ApplicationServer.getInstance() != null) {
				ApplicationServer.getInstance().unregisterMbean(
						OperatorManagementMBean.MBEAN_SUFFIX + userName);
			}
			registered = false;
		}

		if (callQMessageTimerId != -1) {
			AceTimer.Instance().cancelTimer(callQMessageTimerId);
			callQMessageTimerId = -1;
		}

		if (opmCollectionTimerId != -1) {
			AceTimer.Instance().cancelTimer(opmCollectionTimerId);
			opmCollectionTimerId = -1;
		}

		super.dispose();
	}

	private void dropAllSubscribers() {
		ListIterator<SubscriberElement> iter = subscriberQueue.listIterator();
		long curr_time = new Date().getTime();

		while (iter.hasNext()) {
			SubscriberElement element = iter.next();
			// send a BUSY message
			SetupResponseMessage resp = new SetupResponseMessage();
			resp.setSessionId(element.getSessionId());

			// send the message
			if (ServiceController
					.Instance()
					.sendMessage(
							new MessageEvent(
									MessageEvent.SETUP_RESPONSE,
									this,
									SetupResponseMessage.UNAVAILABLE,
									java.util.ResourceBundle
											.getBundle(
													"com.quikj.application.web.talk.feature.operator.language",
													ServiceController
															.getLocale((String) element
																	.getEndpoint()
																	.getParam(
																			"language")))
											.getString(
													"No_operators_are_currently_available,_please_try_again_later"),
									resp, null)) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- Operator.dropAllSubscribers() -- Error sending UNAVAILABLE message to the service controller");
			}


			// peg wait time opm
			int wait_time = (int) (curr_time - element.getStartWaitTime()) / 1000;
			opms.collectOPM(OPM_USER_WAIT_TIME, wait_time);
		}
		subscriberQueue.clear();
	}

	public String getIdentifier() {
		return identifier;
	}

	public Object getParam(String key) {
		synchronized (keyValuePair) {
			return keyValuePair.get(key);
		}
	}

	public String getRMIParam(String key) {
		synchronized (paramLock) {
			if (key.equals("operator-queue-size")) {
				return (new Integer(operatorQueue.size())).toString();
			} else if (key.equals("all-operators-busy") == true) {
				if (operatorQueue.size() == 0) {
					return "true";
				}

				// if the max queue has been initialized
				if (maxQSize >= 0) {
					if (maxQSize == 0) {
						for (OperatorElement operator : operatorQueue) {
							if (operator.getOperatorInfo().getCallCount() < maxSessionsPerOperator) {
								return "false";
							}
						}

						return "true";
					} else if (subscriberQueue.size() >= maxQSize) {
						return "true";
					} else {
						return "false";
					}
				} else {
					return "false";
				}

			} else if (key.equals("subscriber-queue-size")) {
				return (new Integer(subscriberQueue.size())).toString();
			}

			return null;
		}
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public boolean init(String name, Map<?,?> params) {
		userName = name;

		if (!initParams(params)) {
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
					+ (new Date()).getTime() + ":" + counter++;
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
									+ "- Operator.init() -- could not send registration message to the service controller");
			return false;
		}

		return true;
	}

	private boolean initParams(Map<?, ?> params) {
		synchronized (paramLock) {
			String max_session_s = (String)params.get("max-sessions");
			if (max_session_s != null) {
				try {
					maxSessionsPerOperator = Integer.parseInt(max_session_s);
				} catch (NumberFormatException ex) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- Operator.initParams() -- max-sessions must be numeric");
					return false;
				}
			}

			password = (String)params.get("password");

			String max_operators_s = (String)params.get("max-operators");
			if (max_operators_s != null) {
				try {
					maxOperators = Integer.parseInt(max_operators_s);
				} catch (NumberFormatException ex) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- Operator.initParams() -- max-operators must be numeric");
					return false;
				}
			}

			String max_queue_s = (String)params.get("max-queue-size");
			if (max_queue_s != null) {
				try {
					maxQSize = Integer.parseInt(max_queue_s);
				} catch (NumberFormatException ex) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- Operator.initParams() -- max-queue-size must be numeric");
					return false;
				}
			}

			return true;
		}
	}

	private boolean isMyOperator(GroupMemberElement element) {
		UserElement my_info = RegisteredEndPointList.Instance().findRegisteredUserData(
				selfInfo.getName());

		UserElement op_info = RegisteredEndPointList.Instance().findRegisteredUserData(
				element.getUser());

		if (op_info == null) {
			return false;
		}

		String[] my_groups = my_info.getOwnsGroups();

		for (int i = 0; i < my_groups.length; i++) {
			if (op_info.belongsToGroup(my_groups[i]) == true) {
				return true;
			}
		}

		return false;
	}

	private boolean processCallQMessageTimerEvent(AceTimerMessage event) {
		// the CALL Q timer must have expired
		ListIterator<SubscriberElement> iter = subscriberQueue.listIterator();

		while (iter.hasNext() == true) {
			SubscriberElement subs = iter.next();

			// send a progress message to the guy
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(subs.getSessionId());

			MediaElements media = new MediaElements();
			HtmlElement helem = new HtmlElement();
			helem.setHtml(java.util.ResourceBundle
					.getBundle(
							"com.quikj.application.web.talk.feature.operator.language",
							ServiceController.getLocale((String) subs
									.getEndpoint().getParam("language")))
					.getString(
							"Operator_Services:_All_operators_are_currently_busy_assisting_other_customers,_please_hold_for_the_next_available_representative") + '\n');
			media.getElements().add(helem);
			response.setMediaElements(media);

			if (subs.getEndpoint()
					.sendEvent(
							new MessageEvent(
									MessageEvent.SETUP_RESPONSE,
									this,
									SetupResponseMessage.PROG,
									java.util.ResourceBundle
											.getBundle(
													"com.quikj.application.web.talk.feature.operator.language",
													ServiceController
															.getLocale((String) subs
																	.getEndpoint()
																	.getParam(
																			"language")))
											.getString(
													"Operator_Services:_Please_hold_while_we_transfer_you_to_an_operator"),
									response, null)) == false) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- Operator.processCallQMessageTimerEvent() -- Error sending progress event to the calling party");

				return true;
			}
		}

		startCallQMessageTimer(); // restart

		return true;
	}

	private boolean processClientRequestMessage(MessageEvent event) {
		if ((event.getMessage() instanceof GroupActivityMessage) == true) {
			GroupActivityMessage gam = (GroupActivityMessage) event
					.getMessage();
			GroupElement gm = gam.getGroup();

			int num = gm.numElements();
			for (int i = 0; i < num; i++) {
				GroupMemberElement ge = gm.elementAt(i);
				int operation = ge.getOperation();
				switch (operation) {
				case GroupMemberElement.OPERATION_ADD_LIST:
					if (isMyOperator(ge) == true) {
						if (maxOperators > 0) {
							// if a size has been specified
							if (operatorQueue.size() >= maxOperators) {
								AceLogger
										.Instance()
										.log(AceLogger.INFORMATIONAL,
												AceLogger.SYSTEM_LOG,
												Thread.currentThread()
														.getName()
														+ "- Operator.processClientRequestMessage() -- "
														+ "Operator queue "
														+ userName
														+ " has reached its capacity, unable to add new user");
								return true;
							}
						}

						addToQueue(ge);

						// check if anyone is waiting in the subscriber queue
						// if, yes, maybe, an operator is available for a call
						checkQueueStatus(false);
					}
					break;

				case GroupMemberElement.OPERATION_MOD_LIST:
					if (isMyOperator(ge) == true) {
						adjustQueueEntry(ge);

						// check if anyone is waiting in the subscriber queue
						// if, yes, maybe an operator is available for a call
						checkQueueStatus(false);
					}
					break;

				case GroupMemberElement.OPERATION_REM_LIST:
					removeFromQueue(ge);

					// check if all operators are gone, if yes and if there
					// are subscribers in the queue, drop them
					checkQueueStatus(false);
					break;
				}
			}
		}
		return true;
	}

	private boolean processDisconnectMessage(MessageEvent event) {
		if (event.getMessage() instanceof DisconnectMessage) {
			DisconnectMessage disc = (DisconnectMessage) event.getMessage();
			long session = disc.getSessionId();

			ListIterator<SubscriberElement> iter = subscriberQueue
					.listIterator();

			while (iter.hasNext()) {
				SubscriberElement element = iter.next();
				if (element.getSessionId() == session) {
					iter.remove();

					// peg wait time opm
					int wait_time = (int) (new Date().getTime() - element
							.getStartWaitTime()) / 1000;
					opms.collectOPM(OPM_USER_WAIT_TIME, wait_time);

					break;
				}
			}
		}
		return true;
	}

	private boolean processMessageEvent(MessageEvent message) {
		synchronized (paramLock) {
			switch (message.getEventType()) {
			case MessageEvent.REGISTRATION_RESPONSE:
				return processRegistrationResponseEvent(message);

			case MessageEvent.CLIENT_REQUEST_MESSAGE:
				return processClientRequestMessage(message);

			case MessageEvent.SETUP_REQUEST:
				return processSetupRequestEvent(message);

			case MessageEvent.DISCONNECT_MESSAGE:
				return processDisconnectMessage(message);

			default:
				// ignore other messages
				break;
			}

			return true; // ignore unknown message event
		}
	}

	private boolean processOPMs(AceTimerMessage event) {
		// collect sampled OPMs

		opms.collectOPM(opmCollectionTime, OPM_ACTIVE_OPERATOR_COUNT,
				operatorQueue.size());

		opms.collectOPM(opmCollectionTime, OPM_USERS_WAITING,
				subscriberQueue.size());

		// determine number of users being served, note it will also reflect
		// opr-opr calls
		int num_opr_calls = 0;
		ListIterator<OperatorElement> operator_iter = operatorQueue
				.listIterator(0);
		while (operator_iter.hasNext()) {
			num_opr_calls += operator_iter.next().getOperatorInfo()
					.getCallCount();
		} // end while

		opms.collectOPM(opmCollectionTime, OPM_USERS_TALKING, num_opr_calls);

		// check OPM storage interval mark, if it's time, store the data in the
		// database

		Calendar cal = Calendar.getInstance();
		cal.setTime(opmCollectionTime);
		if ((cal.get(Calendar.MINUTE) % OPM_STORAGE_INTERVAL) == 0) {
			// process non-sampled OPMs for this interval

			// accumulate/reset current user wait times:
			long curr_time = cal.getTime().getTime();
			ListIterator<SubscriberElement> subs_iter = subscriberQueue
					.listIterator();
			while (operator_iter.hasNext()) {
				SubscriberElement subs = subs_iter.next();
				int wait_time = (int) (curr_time - subs.getStartWaitTime()) / 1000;
				if (wait_time >= 0) {
					opms.collectOPM(opmCollectionTime, OPM_USER_WAIT_TIME,
							wait_time);
					subs.setStartWaitTime(curr_time);
				}
			}

			// for non-sampled OPMs, if no entries this period, add one with
			// value zero so that
			// the opm is present in the db
			if (opms.getNumCollectedOPMs(OPM_USER_WAIT_TIME) == 0) {
				opms.collectOPM(opmCollectionTime, OPM_USER_WAIT_TIME, 0);
			}

			// average collected OPMs
			opms.averageOPMs(opmCollectionTime);

			// store the averages in the database
			if (!opms.storeOPMs()) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- Operator.processOPMs() -- Failure storing Operator feature OPMs in the database.");
			}

			// clear the opms for the next interval
			opms.clearOPMs();
		}

		startOPMTimer(opmCollectionTime); // restart

		return true;
	}

	private boolean processRegistrationResponseEvent(MessageEvent event) {
		if (registered) {
			// if already registered
			// something must be wrong

			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- Operator.processRegistrationResponseEvent() -- A registration response event is received for this feature that is already registered");
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
									+ "- Operator.processRegistrationResponseEvent() --  Registration failed, status: "
									+ event.getResponseStatus());
			return false;
		}

		RegistrationResponseMessage resp_message = (RegistrationResponseMessage) event
				.getMessage();
		if (resp_message != null) {
			selfInfo = resp_message.getCallPartyInfo();
			GroupElement gel = resp_message.getGroup();

			if (gel != null) {
				int size = gel.numElements();

				// add all the operators to the queue
				for (int i = 0; i < size; i++) {
					if (isMyOperator(gel.elementAt(i))) {
						addToQueue(gel.elementAt(i));
					}
				}
			}
		}

		UserElement userData = RegisteredEndPointList.Instance().findRegisteredUserData(
				this);
		opms = new OPMUtil();
		opms.setTableName(OPM_TABLE_NAME);
		opms.setKeyColumnValue(userData.getName());
		startOPMTimer(new Date());

		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) // if remote service has been started
		{
			rs.registerService(
					"com.quikj.application.web.talk.feature.operator.Operator:"
							+ userName, this);
		}

		if (ApplicationServer.getInstance() != null) {
			ApplicationServer.getInstance().registerMbean(
					OperatorManagementMBean.MBEAN_SUFFIX
							+ resp_message.getCallPartyInfo().getName(),
					new OperatorManagement(this));
		}

		registered = true;
		return true;
	}

	private boolean processSetupRequestEvent(MessageEvent event) {
		if ((event.getMessage() instanceof SetupRequestMessage) == true) {
			SetupRequestMessage setup = (SetupRequestMessage) event
					.getMessage();

			SubscriberElement subs = new SubscriberElement();
			subs.setSessionId(setup.getSessionId());
			subs.setEndpoint(event.getFrom());

			boolean send_busy = false;
			if (maxQSize >= 0) { // if a queue size has been specified
				if (maxQSize == 0) {
					// queue size = 0 means no queueing is allowed.
					// If operators are immediately available, then
					// transfer to an operator, else send busy
					send_busy = true;
					for (OperatorElement operator : operatorQueue) {
						if (operator.getOperatorInfo().getCallCount() < maxSessionsPerOperator) {
							send_busy = false;
							break;
						}
					}
				} else if (subscriberQueue.size() >= maxQSize) {
					send_busy = true;
				}
			}

			if (send_busy) {
				// send a BUSY message
				SetupResponseMessage resp = new SetupResponseMessage();
				resp.setSessionId(setup.getSessionId());

				// send the message
				if (ServiceController
						.Instance()
						.sendMessage(
								new MessageEvent(
										MessageEvent.SETUP_RESPONSE,
										this,
										SetupResponseMessage.BUSY,
										java.util.ResourceBundle
												.getBundle(
														"com.quikj.application.web.talk.feature.operator.language",
														ServiceController
																.getLocale((String) event
																		.getFrom()
																		.getParam(
																				"language")))
												.getString(
														"All_operators_are_currently_busy,_please_try_again_later"),
										resp, null)) == false) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Operator.processSetupRequestEvent() -- Error sending BUSY message to the service controller");
				}

				return true;
			}

			// send a call progress message
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(setup.getSessionId());

			CalledNameElement called = new CalledNameElement();
			called.setCallParty(selfInfo);
			response.setCalledParty(called);

			MediaElements media = new MediaElements();
			HtmlElement helem = new HtmlElement();
			helem.setHtml(java.util.ResourceBundle
					.getBundle(
							"com.quikj.application.web.talk.feature.operator.language",
							ServiceController.getLocale((String) event
									.getFrom().getParam("language")))
					.getString(
							"Operator_Services:_Please_hold_while_we_transfer_you_to_an_operator"));
			media.getElements().add(helem);
			response.setMediaElements(media);

			if (event
					.getFrom()
					.sendEvent(
							new MessageEvent(
									MessageEvent.SETUP_RESPONSE,
									this,
									SetupResponseMessage.PROG,
									java.util.ResourceBundle
											.getBundle(
													"com.quikj.application.web.talk.feature.operator.language",
													ServiceController
															.getLocale((String) event
																	.getFrom()
																	.getParam(
																			"language")))
											.getString(
													"Operator_Services:_Please_hold,_while_the_call_is_being_transferred_to_an_operator"),
									response, null)) == false) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- Operator.processSetupRequestEvent() -- Error sending progress event to the calling party");

				return true;
			}

			// add the subscriber to the call queue
			subs.setRequestId(event.getRequestId());
			subs.setStartWaitTime(new Date().getTime());

			subscriberQueue.addLast(subs);

			if (callQMessageTimerId == -1) {
				startCallQMessageTimer();
			}

			// Check if any operator is available, etc.
			// if yes, transfer the call
			checkQueueStatus(true);
		}
		return true;
	}

	private void removeFromQueue(GroupMemberElement operator) {
		ListIterator<OperatorElement> iter = operatorQueue.listIterator(0);
		String name = operator.getUser();
		while (iter.hasNext() == true) {
			OperatorElement element = iter.next();
			if (name.equals(element.getOperatorInfo().getUser()) == true) {
				iter.remove();
				break;
			}
		}
	}

	public void removeParam(String key) {
		synchronized (keyValuePair) {
			keyValuePair.remove(key);
		}
	}

	public void resynchParam(Map<?,?> params) {
		// set default values
		maxSessionsPerOperator = 1;
		maxOperators = -1;
		maxQSize = -1;
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
										+ "- Operator.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received

				// print informational message
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						getName() + " - Operator.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());
				break;
			} else if (message instanceof MessageEvent) {
				boolean ret = processMessageEvent((MessageEvent) message);
				if (ret == false) {
					break;
				}
			} else if (message instanceof AceTimerMessage) {
				int parm = (int) ((AceTimerMessage) message)
						.getUserSpecifiedParm();
				boolean ret = true;

				switch (parm) {
				case CALL_Q_MESSAGE_TIMER: {
					ret = processCallQMessageTimerEvent((AceTimerMessage) message);
				}
					break;
				case OPM_TIMER: {
					ret = processOPMs((AceTimerMessage) message);
				}
					break;
				default: {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- Operator.run() -- No handling for timer expiry with user parm = "
											+ parm);
				}
					break;
				}

				if (ret == false) {
					break;
				}
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- Operator.run() -- An unexpected message was received while waiting for a message - "
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

	public boolean setRMIParam(String key, String value) {
		return false;
	}

	public void start() {
		super.start();
	}

	private void startCallQMessageTimer() {
		// start the periodic timer
		callQMessageTimerId = AceTimer.Instance().startTimer(
				CALL_Q_MESSAGE_INTERVAL, CALL_Q_MESSAGE_TIMER);
		if (callQMessageTimerId == -1) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- Operator.startCallQMessageTimer() -- Could not start the call queue message timer - "
									+ getErrorMessage());

			// and continue
		}
	}

	private void startOPMTimer(Date from_time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from_time);
		cal.add(Calendar.SECOND, 60 - cal.get(Calendar.SECOND));
		opmCollectionTime = cal.getTime();

		opmCollectionTimerId = AceTimer.Instance().startTimer(
				opmCollectionTime, OPM_TIMER);
		if (opmCollectionTimerId == -1) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- Operator.startOPMTimer() -- Could not start the measurements collection timer - "
									+ getErrorMessage());

			// and continue
		}
	}

	private boolean transferSessionToOperator(SubscriberElement subscriber,
			GroupMemberElement operator) {

		SetupResponseMessage resp = new SetupResponseMessage();
		resp.setSessionId(subscriber.getSessionId());
		resp.setTransferredFrom(selfInfo);

		CalledNameElement called = new CalledNameElement();
		resp.setCalledParty(called);
		CallPartyElement party = new CallPartyElement();
		party.setName(operator.getUser());
		party.setFullName(operator.getFullName());
		called.setCallParty(party);

		// send the message
		if (!ServiceController
				.Instance()
				.sendMessage(
						new MessageEvent(
								MessageEvent.SETUP_RESPONSE,
								this,
								SetupResponseMessage.TRANSFER,
								java.util.ResourceBundle
										.getBundle(
												"com.quikj.application.web.talk.feature.operator.language",
												ServiceController
														.getLocale((String) subscriber
																.getEndpoint()
																.getParam(
																		"language")))
										.getString(
												"You_are_being_connected_to_operator_")
										+ ' ' + operator.getUser(), resp, null))) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							Thread.currentThread().getName()
									+ "- Operator.transferCallToSubscriber() -- Error sending TRANSFER message to the service controller");
			return false;
		}

		return true;
	}

	public int getOperatorQueueSize() {
		synchronized (paramLock) {
			return operatorQueue.size();
		}
	}

	public int getSubscriberQueueSize() {
		synchronized (paramLock) {
			return subscriberQueue.size();
		}
	}

	public String getOperatorSummary() {
		StringBuffer buffer = new StringBuffer();
		synchronized (paramLock) {
			for (OperatorElement operator : operatorQueue) {
				appendProperty(buffer, "Operator: ", operator.getOperatorInfo()
						.getUser(), true);
				appendProperty(buffer, "# chats:", operator.getOperatorInfo()
						.getCallCount(), false);
				buffer.append("\n");
			}
			return buffer.toString();
		}
	}

	public String getVisitorSummary() {
		StringBuffer buffer = new StringBuffer();
		synchronized (paramLock) {
			for (SubscriberElement subscriber : subscriberQueue) {
				appendProperty(buffer, "Visitor: ", subscriber.getEndpoint()
						.getIdentifier(), true);
				appendProperty(buffer, "Waiting since: ",
						new Date(subscriber.getStartWaitTime()), true);
				buffer.append("\n");
			}
		}

		return buffer.toString();
	}

	public void appendProperty(StringBuffer buffer, String label, Object value,
			boolean first) {
		if (!first) {
			buffer.append(", ");
		}
		buffer.append(label);
		buffer.append(value);
	}
}
