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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

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
import com.quikj.application.web.talk.plugin.FeatureInterface;
import com.quikj.application.web.talk.plugin.GatekeeperInterface;
import com.quikj.application.web.talk.plugin.MessageEvent;
import com.quikj.application.web.talk.plugin.OPMUtil;
import com.quikj.application.web.talk.plugin.RegisteredEndPointList;
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
public class Operator extends AceThread
		implements FeatureInterface, EndPointInterface, RemoteServiceInterface, GatekeeperInterface, OperatorMBean {
	private static final int CALL_Q_MESSAGE_TIMER = 0; // timer parm

	private static final int OPM_TIMER = 1; // timer parm

	private static final long CALL_Q_MESSAGE_INTERVAL = 2 * 60 * 1000L;

	private static final String OPM_TABLE_NAME = "opm_operator_tbl";

	private static final String OPM_ACTIVE_OPERATOR_COUNT = "actv_ops"; // sampled

	private static final String OPM_USERS_WAITING = "users_waiting"; // sampled

	private static final String OPM_USERS_TALKING = "users_talking"; // sampled

	private static final String OPM_USER_WAIT_TIME = "user_wait_time";

	private static final int OPM_STORAGE_INTERVAL = 15; // store in DB every 15

	private static String hostName;

	private static int counter = 0;

	private static Object counterLock = new Object();

	private String identifier;

	private String userName = null;

	private LinkedList<OperatorElement> operatorQueue = new LinkedList<OperatorElement>();

	private LinkedList<SubscriberElement> visitorQueue = new LinkedList<SubscriberElement>();

	private LinkedList<OperatorElement> dndList = new LinkedList<OperatorElement>();

	private int maxSessionsPerOperator = 1;

	private CallPartyElement selfInfo;

	private boolean registered = false;

	private int maxOperators = -1;

	private int maxQSize = -1;

	private String password;

	private HashMap<String, Object> keyValuePair = new HashMap<String, Object>();

	private int callQMessageTimerId = -1;

	private Date opmCollectionTime; // collect at top of each minute

	private int opmCollectionTimerId = -1;

	private OPMUtil measurements;

	private Date pausedUntil = new Date();

	private boolean displayWaitTime;

	// sum of all wait times in seconds
	private long sumWaitTime;

	private long waitTimeCount;

	public Operator() throws IOException {
		super("TalkFeatureOperator");
	}

	private void addToQueue(GroupMemberElement gel) {
		OperatorElement operator = new OperatorElement();
		operator.setOperatorInfo(gel);

		ListIterator<OperatorElement> iter = operatorQueue.listIterator(0);
		boolean added = false;

		int callCount = gel.getCallCount();
		while (iter.hasNext()) {
			GroupMemberElement element = iter.next().getOperatorInfo();
			if (element.getCallCount() > callCount) {
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

		if (!added) {
			operatorQueue.addLast(operator);
		}
	}

	private void adjustQueue(GroupMemberElement operator) {
		String name = operator.getUser();
		ListIterator<OperatorElement> i = operatorQueue.listIterator(0);
		while (i.hasNext()) {
			OperatorElement element = i.next();
			if (name.equals(element.getOperatorInfo().getUser())) {
				if (operator.isDnd()) {
					// The operator has enabled DND, take him out of the queue
					i.remove();
					dndList.add(element);
				} else if (operator.getCallCount() != element.getOperatorInfo().getCallCount()) {
					// If there is a call count change, re-adjust the queue
					i.remove(); // remove it temporarily
					element.getOperatorInfo().setCallCount(operator.getCallCount());
					addToQueue(element.getOperatorInfo());
				}

				return;
			}
		}

		// Check if the operator is in the DND list
		Iterator<OperatorElement> j = dndList.iterator();
		while (j.hasNext()) {
			OperatorElement element = j.next();
			if (name.equals(element.getOperatorInfo().getUser())) {
				// Update the call count
				element.getOperatorInfo().setCallCount(operator.getCallCount());

				// If the operator disabled the DND, add him back to the
				// operator queue
				if (!operator.isDnd()) {
					addToQueue(element.getOperatorInfo());
					j.remove();
				}
				return;
			}
		}
	}

	@Override
	public boolean allow(EndPointInterface ep, EndPointInfo info) {
		if (operatorQueue.size() >= maxOperators) {
			for (OperatorElement e : operatorQueue) {
				if (e.getOperatorInfo().getUser().equals(info.getUserData().getName())) {
					// The same user is logging in again, the service controller
					// will remove the previous session, all the user to login
					return true;
				}
			}

			return false;
		}

		return true;
	}

	private void checkQueueStatus() {
		if (visitorQueue.isEmpty()) {
			// no one waiting to be serviced
			return;
		}

		// Some operators have the capacity to take calls but they can be on DND
		if (operatorQueue.isEmpty()) {
			// No operators are available to accept chat requests because they
			// must be on DND
			return;
		}

		OperatorElement operator = operatorQueue.removeFirst();
		if (operatorBusy(operator)) {
			// if the operator is busy - should not happen
			operatorQueue.addFirst(operator); // add him back
			return;
		}

		// subscriber available, operator available, transfer call
		SubscriberElement subscriber = (SubscriberElement) visitorQueue.removeFirst();

		if (transferSessionToOperator(subscriber, operator.getOperatorInfo())) {
			// Bump up the count so that we do not transfer another call to this
			// user while waiting for the response
			operator.getOperatorInfo().setCallCount(operator.getOperatorInfo().getCallCount() + 1);

			// add operator back to queue with re-adjusted count
			addToQueue(operator.getOperatorInfo());

			// peg wait time OPM
			int waitTime = (int) (new Date().getTime() - subscriber.getStartWaitTime()) / 1000;

			// Save wait time information
			sumWaitTime += waitTime;
			waitTimeCount++;

			// measurements.collectOPM(OPM_USER_WAIT_TIME, waitTime);
		} else { // call transfer failed
			operatorQueue.addFirst(operator); // add him back
			visitorQueue.addFirst(subscriber);
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
			// send logout message
			if (ServiceController.Instance() != null) {
				if (ServiceController.Instance().sendMessage(new UnregistrationEvent(userName)) == false) {
					// print error message
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
							+ "- Operator.dispose() -- Error sending unregistration message to the service controller");
				}
			}

			AceRMIImpl rs = AceRMIImpl.getInstance();
			if (rs != null) { // if remote service has been started
				rs.unregisterService("com.quikj.application.web.talk.feature.operator.Operator:" + userName);
			}

			if (ApplicationServer.getInstance() != null) {
				ApplicationServer.getInstance().unregisterMbean(OperatorMBean.MBEAN_SUFFIX + userName);
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
		ListIterator<SubscriberElement> iter = visitorQueue.listIterator();
		long currentTime = new Date().getTime();

		while (iter.hasNext()) {
			dropSubscriber(iter.next(), currentTime);
		}
		visitorQueue.clear();
	}

	private void dropSubscriber(SubscriberElement element, long currentTime) {
		// send a BUSY message
		SetupResponseMessage resp = new SetupResponseMessage();
		resp.setSessionId(element.getSessionId());

		// send the message
		if (ServiceController.Instance()
				.sendMessage(new MessageEvent(MessageEvent.SETUP_RESPONSE, this, SetupResponseMessage.UNAVAILABLE,
						java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.feature.operator.language",
										ServiceController
												.getLocale((String) element.getEndpoint().getParam("language")))
								.getString("No_operators_are_currently_available,_please_try_again_later"),
						resp, null)) == false) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- Operator.dropAllSubscribers() -- Error sending UNAVAILABLE message to the service controller");
		}

		// peg wait time opm
//		int wait_time = (int) (currentTime - element.getStartWaitTime()) / 1000;
//		measurements.collectOPM(OPM_USER_WAIT_TIME, wait_time);
	}

	public String getIdentifier() {
		return identifier;
	}

	public Object getParam(String key) {
		synchronized (keyValuePair) {
			return keyValuePair.get(key);
		}
	}

	public synchronized String getRMIParam(String key) {
		if (key.equals("operator-queue-size")) {
			return Integer.toString(operatorQueue.size());
		} else if (key.equals("all-operators-busy")) {
			return Boolean.toString(allOperatorsBusy());
		} else if (key.equals("subscriber-queue-size")) {
			return Integer.toString(visitorQueue.size());
		} else if (key.equals("operators-with-dnd-count")) {
			return Integer.toString(dndList.size());
		} else if (key.equals("paused-until")) {
			return Long.toString(pausedUntil.getTime());
		} else if (key.equals("estimated-wait-time")) {
			return formatTime(computeMaxWaitTime(),
					java.util.ResourceBundle.getBundle("com.quikj.application.web.talk.feature.operator.language"));
		}

		return null;
	}

	public String getUserName() {
		return userName;
	}

	@Override
	public boolean init(String name, Map<?, ?> params) {
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
			identifier = hostName + ":feature:" + userName + ":" + (new Date()).getTime() + ":" + counter++;
		}

		// send registration message to the Service Controller
		RegistrationRequestMessage reg = new RegistrationRequestMessage();
		reg.setUserName(name);
		reg.setPassword(password);
		boolean ret = ServiceController.Instance()
				.sendEvent(new MessageEvent(MessageEvent.REGISTRATION_REQUEST, this, reg, null));

		if (ret == false) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					getName() + "- Operator.init() -- could not send registration message to the service controller");
			return false;
		}

		return true;
	}

	private synchronized boolean initParams(Map<?, ?> params) {
		String maxSessions = (String) params.get("max-sessions");
		if (maxSessions != null) {
			try {
				maxSessionsPerOperator = Integer.parseInt(maxSessions);
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						getName() + "- Operator.initParams() -- max-sessions must be numeric");
				return false;
			}
		}

		password = (String) params.get("password");

		String maxOperatorsString = (String) params.get("max-operators");
		if (maxOperatorsString != null) {
			try {
				maxOperators = Integer.parseInt(maxOperatorsString);
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						getName() + "- Operator.initParams() -- max-operators must be numeric");
				return false;
			}
		}

		String maxQueueString = (String) params.get("max-queue-size");
		if (maxQueueString != null) {
			try {
				maxQSize = Integer.parseInt(maxQueueString);
			} catch (NumberFormatException ex) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						getName() + "- Operator.initParams() -- max-queue-size must be numeric");
				return false;
			}
		}

		String displayWaitTimeString = (String) params.get("display-wait-time");
		if (displayWaitTimeString != null) {
			displayWaitTime = Boolean.parseBoolean(displayWaitTimeString);
		}

		return true;
	}

	private boolean isMyOperator(GroupMemberElement element) {
		UserElement myInfo = RegisteredEndPointList.Instance().findRegisteredUserData(selfInfo.getName());

		UserElement operatorIinfo = RegisteredEndPointList.Instance().findRegisteredUserData(element.getUser());

		if (operatorIinfo == null) {
			return false;
		}

		String[] myGroups = myInfo.getOwnsGroups();

		for (String myGroup : myGroups) {
			if (operatorIinfo.belongsToGroup(myGroup)) {
				return true;
			}
		}

		return false;
	}

	private boolean processCallQMessageTimerEvent(AceTimerMessage event) {
		// the CALL Q timer must have expired

		ListIterator<SubscriberElement> i = visitorQueue.listIterator();
		int index = 0;
		while (i.hasNext()) {
			index++;
			SubscriberElement subs = i.next();

			// send a progress message to the visitor
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(subs.getSessionId());

			MediaElements media = new MediaElements();
			HtmlElement helem = new HtmlElement();
			StringBuilder builder = new StringBuilder(
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.feature.operator.language",
									ServiceController.getLocale(
											(String) subs.getEndpoint().getParam("language")))
					.getString(
							"Operator_Services:_All_operators_are_currently_busy_assisting_other_customers,_please_hold_for_the_next_available_representative"));
			if (displayWaitTime) {
				builder.append("<br/>");
				builder.append(java.util.ResourceBundle
						.getBundle("com.quikj.application.web.talk.feature.operator.language",
								ServiceController.getLocale((String) subs.getEndpoint().getParam("language")))
						.getString("Operator_Services:_estimated_wait_time_is"));
				builder.append(" ");
				builder.append(getEstimatedWaitTime(index,
						java.util.ResourceBundle.getBundle("com.quikj.application.web.talk.feature.operator.language",
								ServiceController.getLocale((String) subs.getEndpoint().getParam("language")))));
			}
			helem.setHtml(builder.toString());

			media.getElements().add(helem);
			response.setMediaElements(media);

			if (!subs.getEndpoint()
					.sendEvent(
							new MessageEvent(MessageEvent.SETUP_RESPONSE, this, SetupResponseMessage.PROG,
									java.util.ResourceBundle
											.getBundle("com.quikj.application.web.talk.feature.operator.language",
													ServiceController.getLocale(
															(String) subs.getEndpoint().getParam("language")))
									.getString("Operator_Services:_Please_hold_while_we_transfer_you_to_an_operator"),
							response, null))) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- Operator.processCallQMessageTimerEvent() -- Error sending progress event to the calling party");

				return true;
			}
		}

		startCallQMessageTimer(); // restart

		return true;
	}

	private String getEstimatedWaitTime(int index, ResourceBundle bundle) {
		long total = computeEstimatedWaitTime(index);
		return formatTime(total, bundle);
	}

	private long computeMaxWaitTime() {
		if (visitorQueue.isEmpty()) {
			return 0L;
		}		
		return computeBottomWaitTime();
	}

	private long computeTopWaitTime() {
		return (System.currentTimeMillis() - visitorQueue.getFirst().getStartWaitTime()) / 1000;
	}
	
	private long computeBottomWaitTime() {
		return (System.currentTimeMillis() - visitorQueue.getLast().getStartWaitTime()) / 1000;
	}

	private long computeEstimatedWaitTime(int index) {
		if (visitorQueue.isEmpty()) {
			return 0L;
		}

		long alreadyWaitedFor = computeTopWaitTime();
		long waitTime = getAverageWaitTime();

		long total = (index * waitTime) - alreadyWaitedFor;
		if (total <= 0) {
			// If the visitor has waited longer than the estimated wait time,
			// set a default value
			total = 60L * index;
		}
		return total;
	}

	private long getAverageWaitTime() {
		// set an arbitrary value for the default wait time
		long waitTime = 300L;
		if (waitTimeCount > 0) {
			// If wait time statistics is available
			waitTime = sumWaitTime / waitTimeCount;
		}
		return waitTime;
	}

	private String formatTime(long time, ResourceBundle bundle) {
		StringBuilder builder = new StringBuilder();

		long hour = time / 3600;
		if (hour > 0) {
			builder.append(pad(hour));
		}

		long minute = (time - (hour * 3600)) / 60;
		if (builder.length() > 0) {
			builder.append(bundle.getString("hour_abbr"));
			builder.append(":");
		}
		builder.append(pad(minute));

		long seconds = time - (hour * 3600) - (minute * 60);
		builder.append(bundle.getString("min_abbr"));
		builder.append(":");

		builder.append(pad(seconds));
		builder.append(bundle.getString("sec_abbr"));

		return builder.toString();
	}

	String pad(long value) {
		StringBuilder ret = new StringBuilder();
		if (value >= 0 && value <= 9) {
			ret.append("0");
		}
		ret.append(value);
		return ret.toString();
	}

	private boolean processClientRequestMessage(MessageEvent event) {
		if (event.getMessage() instanceof GroupActivityMessage) {
			GroupActivityMessage gam = (GroupActivityMessage) event.getMessage();
			GroupElement gm = gam.getGroup();

			for (GroupMemberElement ge : gm.getElements()) {
				int operation = ge.getOperation();
				switch (operation) {
				case GroupMemberElement.OPERATION_ADD_LIST:
					if (isMyOperator(ge)) {
						if (maxOperators > 0 && operatorQueue.size() >= maxOperators) {
							AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName() + "- Operator.processClientRequestMessage() -- "
											+ "Operator queue " + userName
											+ " has reached its capacity, unable to add new user");
						} else {
							addToQueue(ge);

							// check if anyone is waiting in the subscriber
							// queue. If, yes, maybe an operator is available
							// for a call
							checkQueueStatus();
						}
					}
					break;

				case GroupMemberElement.OPERATION_MOD_LIST:
					if (isMyOperator(ge)) {
						adjustQueue(ge);

						// check if anyone is waiting in the subscriber queue
						// if, yes, maybe an operator is available for a call
						checkQueueStatus();
					}
					break;

				case GroupMemberElement.OPERATION_REM_LIST:
					removeFromQueue(ge);

					if (operatorQueue.isEmpty() && dndList.isEmpty() && !visitorQueue.isEmpty()) {
						dropAllSubscribers();
					}
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

			ListIterator<SubscriberElement> iter = visitorQueue.listIterator();

			while (iter.hasNext()) {
				SubscriberElement element = iter.next();
				if (element.getSessionId() == session) {
					iter.remove();
					break;
				}
			}
		}
		return true;
	}

	private synchronized boolean processMessageEvent(MessageEvent message) {
		switch (message.getEventType()) {
		case MessageEvent.REGISTRATION_RESPONSE:
			return processRegistrationResponseEvent(message);

		case MessageEvent.CLIENT_REQUEST_MESSAGE:
			return processClientRequestMessage(message);

		case MessageEvent.SETUP_REQUEST:
			return processSetupRequestEvent(message);

		case MessageEvent.DISCONNECT_MESSAGE:
			return processDisconnectMessage(message);
		}

		return true; // ignore unknown message event
	}

	private boolean processOPMs(AceTimerMessage event) {
		// collect sampled OPMs

		measurements.collectOPM(opmCollectionTime, OPM_ACTIVE_OPERATOR_COUNT, operatorQueue.size());

		measurements.collectOPM(opmCollectionTime, OPM_USERS_WAITING, visitorQueue.size());

		// determine number of users being served, note it will also reflect
		// opr-opr calls
		int numOprCalls = 0;
		ListIterator<OperatorElement> operatorIter = operatorQueue.listIterator(0);
		while (operatorIter.hasNext()) {
			numOprCalls += operatorIter.next().getOperatorInfo().getCallCount();
		}

		measurements.collectOPM(opmCollectionTime, OPM_USERS_TALKING, numOprCalls);

		measurements.collectOPM(opmCollectionTime, OPM_USER_WAIT_TIME, (int)computeMaxWaitTime());
		
		// check OPM storage interval mark, if it's time, store the data in the
		// database

		Calendar cal = Calendar.getInstance();
		cal.setTime(opmCollectionTime);
		if ((cal.get(Calendar.MINUTE) % OPM_STORAGE_INTERVAL) == 0) {
			// average collected OPMs
			measurements.averageOPMs(opmCollectionTime);

			// store the averages in the database
			if (!measurements.storeOPMs()) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- Operator.processOPMs() -- Failure storing Operator feature OPMs in the database.");
			}

			// clear the opms for the next interval
			measurements.clearOPMs();
		}

		startOPMTimer(opmCollectionTime); // restart

		return true;
	}

	private boolean processRegistrationResponseEvent(MessageEvent event) {
		if (registered) {
			// if already registered
			// something must be wrong

			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, getName()
					+ "- Operator.processRegistrationResponseEvent() -- A registration response event is received for this feature that is already registered");
			return false;
		}

		// check the status
		if (event.getResponseStatus() != ResponseMessage.OK) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					getName() + "- Operator.processRegistrationResponseEvent() --  Registration failed, status: "
							+ event.getResponseStatus());
			return false;
		}

		RegistrationResponseMessage resp_message = (RegistrationResponseMessage) event.getMessage();
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

		UserElement userData = RegisteredEndPointList.Instance().findRegisteredUserData(this);
		measurements = new OPMUtil();
		measurements.setTableName(OPM_TABLE_NAME);
		measurements.setKeyColumnValue(userData.getName());
		startOPMTimer(new Date());

		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) // if remote service has been started
		{
			rs.registerService("com.quikj.application.web.talk.feature.operator.Operator:" + userName, this);
		}

		if (ApplicationServer.getInstance() != null) {
			ApplicationServer.getInstance()
					.registerMbean(OperatorMBean.MBEAN_SUFFIX + resp_message.getCallPartyInfo().getName(), this);
		}

		registered = true;
		return true;
	}

	private boolean processSetupRequestEvent(MessageEvent event) {
		if (event.getMessage() instanceof SetupRequestMessage) {
			SetupRequestMessage setup = (SetupRequestMessage) event.getMessage();

			SubscriberElement subs = new SubscriberElement();
			subs.setSessionId(setup.getSessionId());
			subs.setEndpoint(event.getFrom());

			boolean busy = allOperatorsBusy();
			if (busy) {
				SetupResponseMessage resp = new SetupResponseMessage();
				resp.setSessionId(setup.getSessionId());

				// send the message
				if (!ServiceController.Instance()
						.sendMessage(
								new MessageEvent(MessageEvent.SETUP_RESPONSE, this, SetupResponseMessage.BUSY,
										java.util.ResourceBundle
												.getBundle("com.quikj.application.web.talk.feature.operator.language",
														ServiceController.getLocale(
																(String) event.getFrom().getParam("language")))
										.getString("All_operators_are_currently_busy,_please_try_again_later"), resp,
								null))) {
					// print error message
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
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
					.getBundle("com.quikj.application.web.talk.feature.operator.language",
							ServiceController.getLocale((String) event.getFrom().getParam("language")))
					.getString("Operator_Services:_Please_hold_while_we_transfer_you_to_an_operator"));
			media.getElements().add(helem);
			response.setMediaElements(media);

			if (!event.getFrom()
					.sendEvent(
							new MessageEvent(MessageEvent.SETUP_RESPONSE, this, SetupResponseMessage.PROG,
									java.util.ResourceBundle
											.getBundle("com.quikj.application.web.talk.feature.operator.language",
													ServiceController
															.getLocale((String) event.getFrom().getParam("language")))
											.getString(
													"Operator_Services:_Please_hold,_while_the_call_is_being_transferred_to_an_operator"),
					response, null))) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
						+ "- Operator.processSetupRequestEvent() -- Error sending progress event to the calling party");

				return true;
			}

			// add the subscriber to the call queue
			subs.setRequestId(event.getRequestId());
			subs.setStartWaitTime(System.currentTimeMillis());

			visitorQueue.addLast(subs);

			if (callQMessageTimerId == -1) {
				startCallQMessageTimer();
			}

			// Check if any operator is available, etc.
			// if yes, transfer the call
			checkQueueStatus();
		}
		return true;
	}

	private boolean allOperatorsBusy() {
		boolean busy = true;
		if (maxQSize == 0) {
			// queue size = 0 means no queuing is allowed.
			// If operators are immediately available, then
			// transfer to an operator, else send busy
			busy = !operatorsAvailableImmediately();
		} else if (maxQSize > 0) {
			// if a queue size has been specified
			if ((!operatorQueue.isEmpty() || !dndList.isEmpty()) && (visitorQueue.size() < maxQSize)
					&& new Date().after(pausedUntil)) {
				// If there is capacity available in the queue
				busy = false;
			}
		} else if (!operatorQueue.isEmpty() || !dndList.isEmpty() && new Date().after(pausedUntil)) {
			// unlimited queue size and operators available
			busy = false;
		}
		return busy;
	}

	private boolean operatorsAvailableImmediately() {
		for (OperatorElement operator : operatorQueue) {
			if (!operatorBusy(operator)) {
				return true;
			}
		}

		return false;
	}

	private boolean operatorBusy(OperatorElement operator) {
		return operator.getOperatorInfo().getCallCount() < maxSessionsPerOperator ? false : true;
	}

	private void removeFromQueue(GroupMemberElement operator) {
		String name = operator.getUser();

		ListIterator<OperatorElement> i = operatorQueue.listIterator(0);
		while (i.hasNext()) {
			OperatorElement element = i.next();
			if (name.equals(element.getOperatorInfo().getUser())) {
				i.remove();
				return;
			}
		}

		ListIterator<OperatorElement> j = dndList.listIterator(0);
		while (j.hasNext()) {
			OperatorElement element = j.next();
			if (name.equals(element.getOperatorInfo().getUser())) {
				j.remove();
				return;
			}
		}
	}

	public void removeParam(String key) {
		synchronized (keyValuePair) {
			keyValuePair.remove(key);
		}
	}

	public void resynchParam(Map<?, ?> params) {
		setDefaultValue();

		initParams(params);

		long currentTime = System.currentTimeMillis();

		// If the queue size was reduced from the management interface, remove
		// visitor waiting in the queue
		if (maxQSize >= 0) {
			int diff = visitorQueue.size() - maxQSize;
			Iterator<SubscriberElement> iter = visitorQueue.descendingIterator();
			for (int i = 0; i < diff; i++) {
				SubscriberElement element = iter.next();
				dropSubscriber(element, currentTime);
				iter.remove();
			}
		}

		// Because of the changes, it is possible that some more operators
		// became available
		checkQueueStatus();
	}

	private void setDefaultValue() {
		maxSessionsPerOperator = 1;
		maxOperators = -1;
		maxQSize = -1;
		displayWaitTime = false;
	}

	public void run() {
		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				// print error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						getName() + "- Operator.run() -- A null message was received while waiting for a message - "
								+ getErrorMessage());

				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received

				AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
						getName() + " - Operator.run() --  A signal " + ((AceSignalMessage) message).getSignalId()
								+ " is received : " + ((AceSignalMessage) message).getMessage());
				break;
			} else if (message instanceof MessageEvent) {
				boolean ret = processMessageEvent((MessageEvent) message);
				if (ret == false) {
					break;
				}
			} else if (message instanceof AceTimerMessage) {
				int parm = (int) ((AceTimerMessage) message).getUserSpecifiedParm();
				boolean ret = true;

				switch (parm) {
				case CALL_Q_MESSAGE_TIMER:
					ret = processCallQMessageTimerEvent((AceTimerMessage) message);
					break;
				case OPM_TIMER:
					ret = processOPMs((AceTimerMessage) message);
					break;
				default:
					AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
							getName() + "- Operator.run() -- No handling for timer expiry with user parm = " + parm);
					break;
				}

				if (!ret) {
					break;
				}
			} else {
				AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
						getName()
								+ "- Operator.run() -- An unexpected message was received while waiting for a message - "
								+ message.messageType());
			}
		}

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

	public synchronized boolean setRMIParam(String key, String value) {
		if (key.equals("pause-for")) {
			try {
				long duration = Long.valueOf(value);
				pausedUntil = new Date(System.currentTimeMillis() + duration * 60 * 1000L);
				return true;
			} catch (NumberFormatException e) {
				AceLogger.Instance().log(AceLogger.WARNING, AceLogger.SYSTEM_LOG,
						getName() + "- Operator.setRMIParam() -- Invalid pause duration specified");
			}
		}

		return false;
	}

	public void start() {
		super.start();
	}

	private void startCallQMessageTimer() {
		// start the periodic timer
		callQMessageTimerId = AceTimer.Instance().startTimer(CALL_Q_MESSAGE_INTERVAL, CALL_Q_MESSAGE_TIMER);
		if (callQMessageTimerId == -1) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					getName() + "- Operator.startCallQMessageTimer() -- Could not start the call queue message timer - "
							+ getErrorMessage());
		}
	}

	private void startOPMTimer(Date from_time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(from_time);
		cal.add(Calendar.SECOND, 60 - cal.get(Calendar.SECOND));
		opmCollectionTime = cal.getTime();

		opmCollectionTimerId = AceTimer.Instance().startTimer(opmCollectionTime, OPM_TIMER);
		if (opmCollectionTimerId == -1) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					getName() + "- Operator.startOPMTimer() -- Could not start the measurements collection timer - "
							+ getErrorMessage());
		}
	}

	private boolean transferSessionToOperator(SubscriberElement subscriber, GroupMemberElement operator) {

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
		if (!ServiceController.Instance()
				.sendMessage(
						new MessageEvent(MessageEvent.SETUP_RESPONSE, this, SetupResponseMessage.TRANSFER,
								java.util.ResourceBundle
										.getBundle("com.quikj.application.web.talk.feature.operator.language",
												ServiceController.getLocale(
														(String) subscriber.getEndpoint().getParam("language")))
								.getString("You_are_being_connected_to_operator_") + ' ' + operator.getUser(), resp,
						null))) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, Thread.currentThread().getName()
					+ "- Operator.transferCallToSubscriber() -- Error sending TRANSFER message to the service controller");
			return false;
		}

		return true;
	}

	@Override
	public synchronized int getOperatorAvailableQueueSize() {
		return operatorQueue.size();
	}

	@Override
	public synchronized int getOperatorsWithDNDSize() {
		return dndList.size();
	}

	@Override
	public synchronized int getSubscriberQueueSize() {
		return visitorQueue.size();
	}

	@Override
	public synchronized Date getPausedUntil() {
		return pausedUntil;
	}

	@Override
	public synchronized String getOperatorSummary() {
		StringBuilder b = new StringBuilder();
		b.append("Operators: ");
		for (OperatorElement operator : operatorQueue) {
			appendProperty(b, "name", operator.getOperatorInfo().getUser());
			appendProperty(b, "numChats", operator.getOperatorInfo().getCallCount());
		}

		b.append(" Operators with DND: ");
		for (OperatorElement operator : dndList) {
			appendProperty(b, "name", operator.getOperatorInfo().getUser());
			appendProperty(b, "numChats", operator.getOperatorInfo().getCallCount());
		}
		return b.toString();
	}

	@Override
	public synchronized String getVisitorSummary() {
		StringBuilder b = new StringBuilder();
		b.append("Visitors: ");
		for (SubscriberElement subscriber : visitorQueue) {
			appendProperty(b, "identifier", subscriber.getEndpoint().getIdentifier());
			appendProperty(b, "waitingSince", new Date(subscriber.getStartWaitTime()));
		}
		return b.toString();
	}

	public void appendProperty(StringBuilder b, String label, Object value) {
		if (b.length() > 0) {
			b.append(" ");
		}
		b.append(label);
		b.append("=");
		b.append(value);
	}
}
