/*
 * ConferenceBridge.java
 *
 * Created on March 3, 2002, 4:14 AM
 */

package com.quikj.application.web.talk.plugin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.ConferenceInformationMessage;
import com.quikj.ace.messages.vo.talk.ConferencePartyInfo;
import com.quikj.ace.messages.vo.talk.RTPMessage;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;

/**
 * 
 * @author amit
 */
public class ConferenceBridge extends AceThread implements EndPointInterface {
	private String identifier;
	private static int counter = 0;
	private static Object identifierLock = new Object();
	private static String hostName = null;

	private LinkedList<ConferencedEndPoints> endPointList = new LinkedList<ConferencedEndPoints>();
	private long sessionId = -1;

	private HashMap<String, Object> keyValuePair = new HashMap<String, Object>();

	public ConferenceBridge(String name) throws IOException {
		super(name);

		if (hostName == null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				hostName = "unknown";
			}
		}

		synchronized (identifierLock) {
			identifier = hostName + ":conference:" + (new Date()).getTime()
					+ ":" + counter++;
		}

	}

	public void addEndPoint(EndPointInterface ep) {
		synchronized (endPointList) {
			ConferencedEndPoints cep = new ConferencedEndPoints(ep);
			CallPartyElement info = (CallPartyElement) ep
					.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (info != null) {
				cep.setInformation(info);
				cep.setStatus(ConferencePartyInfo.STATUS_ADDED);
			}

			endPointList.add(cep);
		}
	}

	public boolean containsEndPoint(EndPointInterface ep) {
		synchronized (endPointList) {
			Iterator<ConferencedEndPoints> iter = endPointList.iterator();
			while (iter.hasNext()) {
				ConferencedEndPoints cep = iter.next();
				if (cep.getEndpoint() == ep) {
					return true;
				}
			}
		}

		return false;
	}

	public void dispose() {
		super.dispose();
	}

	public String getIdentifier() {
		return identifier;

	}

	public Object getParam(String key) {
		synchronized (keyValuePair) {
			return keyValuePair.get(key);
		}
	}

	public long getSessionId() {
		return sessionId;
	}

	public EndPointInterface[] listEndPoints() {
		synchronized (endPointList) {
			int size = numEndPoints();
			EndPointInterface[] elements = new EndPointInterface[size];
			Iterator<ConferencedEndPoints> iter = endPointList.iterator();

			int count = 0;
			while (iter.hasNext()) {
				elements[count++] = iter.next().getEndpoint();
			}

			return elements;
		}
	}

	public void notifyEndPoints() {
		synchronized (endPointList) {
			ConferenceInformationMessage msgForOps = createConferenceInfoMessageForOperators();
			ConferenceInformationMessage msgForVis = createConferenceInfoMessageForVisitors();

			// send the message to the end-points
			Iterator<ConferencedEndPoints> iter = endPointList.iterator();
			while (iter.hasNext()) {
				ConferencedEndPoints conf = iter.next();

				if (conf.getStatus() == ConferencePartyInfo.STATUS_REMOVED) {
					iter.remove();
					continue;
				}

				EndPointInterface to = conf.getEndpoint();

				ConferenceInformationMessage message = msgForOps;
				CallPartyElement epInfo = (CallPartyElement) to
						.getParam(EndPointInterface.PARAM_SELF_INFO);
				if (epInfo != null && epInfo.getName() == null) {
					message = msgForVis;
				}

				MessageEvent me = new MessageEvent(
						MessageEvent.CLIENT_REQUEST_MESSAGE, this, message,
						null);
				if (!to.sendEvent(me)) {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- ConferenceBridge.notifyEvent() -- Error sending conference information message to the client");
				}

				conf.setStatus(ConferencePartyInfo.STATUS_PARTY);
			}
		}
	}

	private ConferenceInformationMessage createConferenceInfoMessageForVisitors() {
		ConferenceInformationMessage confMessage = new ConferenceInformationMessage();
		confMessage.setSessionId(sessionId);

		int size = endPointList.size();
		for (int i = 0; i < size; i++) {
			ConferencedEndPoints conf = endPointList.get(i);
			if (conf.getInformation() == null) {
				continue;
			}

			EndPointInterface endpoint = conf.getEndpoint();
			if (endpoint == null) {
				continue;
			}

			CallPartyElement epInfo = (CallPartyElement) endpoint
					.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (epInfo != null && epInfo.isPrivateInfo()) {
				epInfo = new CallPartyElement(epInfo);
				TalkEndpoint.setPrivate(epInfo);
			}

			confMessage.getEndpointList().add(
					new ConferencePartyInfo(epInfo, conf.getStatus()));
		}
		return confMessage;
	}

	private ConferenceInformationMessage createConferenceInfoMessageForOperators() {
		ConferenceInformationMessage confMessage = new ConferenceInformationMessage();
		confMessage.setSessionId(sessionId);

		int size = endPointList.size();
		for (int i = 0; i < size; i++) {
			ConferencedEndPoints conf = endPointList.get(i);
			if (conf.getInformation() != null) {
				confMessage.getEndpointList().add(
						new ConferencePartyInfo(conf.getInformation(), conf
								.getStatus()));
			}
		}
		return confMessage;
	}

	public int numEndPoints() {
		synchronized (endPointList) {
			return endPointList.size();
		}
	}

	private void processMessageEvent(MessageEvent e) {
		if (e.getEventType() == MessageEvent.RTP_MESSAGE) {
			EndPointInterface from = e.getFrom();
			RTPMessage message = (RTPMessage) e.getMessage();

			RTPMessage fromPrivateRTPMessage = message;
			CallPartyElement fromInfo = (CallPartyElement) from
					.getParam(EndPointInterface.PARAM_SELF_INFO);
			if (fromInfo != null && fromInfo.isPrivateInfo()) {
				fromPrivateRTPMessage = new RTPMessage(message);
				TalkEndpoint.setPrivate(fromPrivateRTPMessage.getFrom());
			}

			synchronized (endPointList) {
				ListIterator<ConferencedEndPoints> iter = endPointList
						.listIterator();

				while (iter.hasNext()) {
					EndPointInterface endpoint = iter.next().getEndpoint();
					if (endpoint == from) {
						continue;
					}

					RTPMessage rtp = message;
					CallPartyElement toInfo = (CallPartyElement) endpoint
							.getParam(EndPointInterface.PARAM_SELF_INFO);
					if (toInfo != null && toInfo.getName() == null) {
						rtp = fromPrivateRTPMessage;
					}

					if (!endpoint.sendEvent(new MessageEvent(
							MessageEvent.RTP_MESSAGE, from, rtp, null))) {
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										Thread.currentThread().getName()
												+ "- ConferenceBridge.processMessageEvent() -- Could not send RTP message to the endpoint "
												+ endpoint);
						// and ignore
					}
				}
			}
		}
		// else, ignore
	}

	public boolean removeEndPoint(EndPointInterface ep) {
		synchronized (endPointList) {
			Iterator<ConferencedEndPoints> iterator = endPointList.iterator();
			while (iterator.hasNext()) {
				ConferencedEndPoints cep = iterator.next();
				if (cep.getEndpoint() == ep) {
					cep.setStatus(ConferencePartyInfo.STATUS_REMOVED);
					return true;
				}
			}
		}

		return false;
	}

	public void removeParam(String key) {
		synchronized (keyValuePair) {
			keyValuePair.remove(key);
		}
	}

	public void run() {
		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- ConferenceBridge.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());
				break;
			}

			if (message instanceof AceSignalMessage) {
				break;
			} else if (message instanceof MessageEvent) {
				processMessageEvent((MessageEvent) message);
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- ConferenceBridge.run() -- An unexpected event is received : "
										+ message.messageType());
			}
		}

		dispose();
	}

	public boolean sendEvent(AceMessageInterface message) {
		return sendMessage(message);
	}

	public void setParam(String key, Object value) {
		synchronized (keyValuePair) {
			keyValuePair.put(key, value);
		}
	}

	public void setSessionId(long sessionId) {
		this.sessionId = sessionId;
	}
}
