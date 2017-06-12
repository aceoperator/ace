/**
 * 
 */
package com.quikj.server.app.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.quikj.ace.messages.vo.adapter.GroupInfo;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.application.web.talk.plugin.LostUsernamePassword;
import com.quikj.application.web.talk.plugin.SynchronousDbOperations;
import com.quikj.application.web.talk.plugin.accounting.CDRHandler;
import com.quikj.application.web.talk.plugin.accounting.CDRInterface;
import com.quikj.client.raccess.RemoteAccessClient;
import com.quikj.server.app.ApplicationConfiguration;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.app.ClientMessage;
import com.quikj.server.app.RemoteEndPoint;
import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;

// TODO add a timer after the initial connect. The timer is canceled after the first request from the client. 
// If the timer expires, drop the session. This will prevent malicious clients from just keeping the session
// alive and wasting resources.

/**
 * @author amit
 * 
 */
public class PolledAppServerAdapter implements AppServerAdapter {

	private static final long SESSION_EXPIRY_TIMER = 2 * 60 * 1000L;
	private static final long MEASUREMENTS_TIMER = 12 * 60 * 60 * 1000L;

	private static Long nextSessionId = 0L;
	private static PolledAppServerAdapter instance = null;

	private Map<String, SessionInfo> sessionMap = new HashMap<String, SessionInfo>();

	private RemoteAccessClient rmi;

	private Timer expiryTimer;

	private Timer measurementsTimer;

	private AppServerAdapterMeasurements measurements = new AppServerAdapterMeasurements();

	public PolledAppServerAdapter() throws UnknownHostException {

		expiryTimer = new Timer();
		expiryTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				expireSessions();
			}
		}, new Date(new Date().getTime() + SESSION_EXPIRY_TIMER),
				SESSION_EXPIRY_TIMER);

		measurementsTimer = new Timer();
		expiryTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				logMeasurements();
			}
		}, new Date(new Date().getTime() + MEASUREMENTS_TIMER),
				MEASUREMENTS_TIMER);

		rmi = new RemoteAccessClient(ApplicationServer.getInstance()
				.getBean(ApplicationConfiguration.class).getRegistryURL(),
				ApplicationServer.getInstance()
						.getBean(ApplicationConfiguration.class)
						.getRegistryServiceName(), InetAddress.getLocalHost()
						.getHostName());

		ApplicationServer.getInstance().registerMbean(
				AppServerAdapterManagementMBean.MBEAN_NAME,
				new AppServerAdapterManagement());

		instance = this;
	}

	private void logMeasurements() {
		String text = formatMeasurementsOutput();
		resetMeasurements();
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"AppServerAdapter.logMeasurements() -- Measurements:\n" + text);
	}

	public void resetMeasurements() {
		synchronized (measurements) {
			measurements.reset();
		}
	}

	public String formatMeasurementsOutput() {
		int numSessions = 0;
		synchronized (sessionMap) {
			numSessions = sessionMap.size();
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("# sessions: ");
		buffer.append(numSessions);

		synchronized (measurements) {
			measurements.formatMeasurements(buffer);
		}
		return buffer.toString();
	}

	public void dispose() {
		expiryTimer.cancel();
		measurementsTimer.cancel();

		synchronized (sessionMap) {
			sessionMap.clear();
		}
		instance = null;
	}

	public static PolledAppServerAdapter getInstance() {
		return instance;
	}

	private void expireSessions() {
		long curTime = new Date().getTime();

		List<SessionInfo> expiredSessions = new ArrayList<SessionInfo>();
		int sessionCount = 0;
		int expiryCount = 0;
		synchronized (sessionMap) {
			sessionCount = sessionMap.size();
			Iterator<SessionInfo> i = sessionMap.values().iterator();
			while (i.hasNext()) {
				SessionInfo info = i.next();
				long last = info.getLastComm().getTime();
				if (last + SESSION_EXPIRY_TIMER <= curTime) {
					expiredSessions.add(info);
					i.remove();
					expiryCount++;
				}
			}
		}

		if (expiredSessions.size() > 0) {
			AceLogger.Instance().log(
					AceLogger.INFORMATIONAL,
					AceLogger.SYSTEM_LOG,
					"AppServerAdapter.expireSessions() -- " + expiryCount
							+ " sessions out of " + sessionCount
							+ " sessions will be closed because of inactivity");

			for (SessionInfo info : expiredSessions) {
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						"AppServerAdapter.expireSessions() -- Closing connection "
								+ info.getSessionId()
								+ " because of inactivity");
				info.getEndPoint()
						.dispose("disconnected because of inactivity");
			}
		}
	}

	public List<Message> exchangeMessages(Message incoming)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementIncomingMessageStartCount();
		}

		try {
			String sessionId = incoming.getHeaders().get(
					Message.SESSION_ID_HEADER);
			if (sessionId == null) {
				throw new AppServerAdapterException(
						"The incoming message does not contain a session id",
						false);
			} else {
				SessionInfo info = null;
				synchronized (sessionMap) {
					info = sessionMap.get(sessionId);
				}

				if (info == null) {
					throw new AppServerAdapterException(
							"Invalid or expired session id " + sessionId, false);
				} else {
					synchronized (info) {
						timestamp(info);

						if (incoming instanceof RequestMessage) {
							RequestMessage msg = (RequestMessage) incoming;

							if (msg.getMethod().equals(
									RequestMessage.PING_METHOD)) {
								List<Message> msgs = new ArrayList<Message>();
								boolean disconnect = generateResponse(info,
										msgs);
								if (disconnect) {
									synchronized (sessionMap) {
										sessionMap.remove(info.getSessionId());
									}
								}
								return msgs;
							}

							if (!msg.getMethod().equals(
									RequestMessage.APPLICATION_METHOD)) {
								AceLogger
										.Instance()
										.log(AceLogger.ERROR,
												AceLogger.SYSTEM_LOG,
												"AppServerAdapter.exchangeMessages() -- Message received with unsupported method: "
														+ msg.getMethod());
								throw new AppServerAdapterException(
										"Invalid method - " + msg.getMethod(),
										false);
							}
						}

						// send the message to the end point
						ClientMessage message = new ClientMessage(incoming);
						if (!info.getEndPoint().sendMessage(message)) {
							// Sending the message failed
							throw new AppServerAdapterException(
									"Failed to send message to the endpoint",
									false);
						}

						// send the response
						List<Message> msgs = new ArrayList<Message>();
						boolean disconnect = generateResponse(info, msgs);
						if (disconnect) {
							synchronized (sessionMap) {
								sessionMap.remove(info.getSessionId());
							}
						}
						return msgs;
					}
				}
			}
		} finally {
			synchronized (measurements) {
				measurements.incrementIncomingMessageEndCount();
			}
		}
	}

	private void timestamp(SessionInfo info) {
		info.setLastComm(new Date());
	}

	private boolean generateResponse(SessionInfo info, List<Message> msgs) {

		boolean disconnected = false;
		for (Message msg : info.getMessages()) {
			msgs.add(msg);

			if (msg instanceof RequestMessage) {
				RequestMessage req = (RequestMessage) msg;
				if (req.getMethod().equals(RequestMessage.DISCONNECT_METHOD)) {
					// Last message
					disconnected = true;
				}
			}
		}

		info.getMessages().clear();
		return disconnected;
	}

	@Override
	public void sendMessage(String sessionId, Message message)
			throws AppServerAdapterException {

		SessionInfo info = null;
		synchronized (sessionMap) {
			info = sessionMap.get(sessionId);
		}

		if (info == null) {
			throw new AppServerAdapterException("The session was not found",
					false);
		}

		synchronized (info) {
			info.getMessages().add(message);
		}
	}

	@Override
	public void endPointTerminated(String sessionId)
			throws AppServerAdapterException {
		SessionInfo info = null;
		synchronized (sessionMap) {
			info = sessionMap.get(sessionId);
		}

		if (info == null) {
			throw new AppServerAdapterException("The session was not found",
					false);
		}

		Map<String, String> headers = new HashMap<String, String>();
		headers.put(Message.SESSION_ID_HEADER, sessionId);
		RequestMessage req = new RequestMessage(
				RequestMessage.DISCONNECT_METHOD, "1.1", headers, null);

		synchronized (info) {
			info.getMessages().add(req);
		}
	}

	public String clientConnected(String ip, String endUserCookie)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementConnectStartCount();
		}

		try {
			String sessionId; // new session;
			synchronized (nextSessionId) {
				sessionId = nextSessionId.toString();
				nextSessionId++;
			}

			RemoteEndPoint ep = new RemoteEndPoint(sessionId, ip,
					endUserCookie, this);
			ep.start();

			SessionInfo info = new SessionInfo(sessionId, ep, new Date());
			timestamp(info);

			synchronized (sessionMap) {
				sessionMap.put(sessionId, info);
			}

			return sessionId;
		} catch (IOException e) {
			throw new AppServerAdapterException(e.getMessage(), false);
		} finally {
			synchronized (measurements) {
				measurements.incrementConnectEndCount();
			}
		}
	}

	public void clientDisconnected(String sessionId)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementDisconnectStartCount();
		}

		try {
			synchronized (sessionMap) {
				SessionInfo info = sessionMap.get(sessionId);
				if (info == null) {
					throw new AppServerAdapterException(
							"Invalid or expired session id " + sessionId, false);
				}

				info.getEndPoint().dispose(null);
				sessionMap.remove(info.getSessionId());
			}
		} finally {
			synchronized (measurements) {
				measurements.incrementDisconnectEndCount();
			}
		}
	}

	public CannedMessageElement[] listCannedMessages(String[] groups, boolean fetchContent)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementListCannedMsgStartCount();
		}

		try {
			CannedMessageElement[] cannedMessage = null;
			if (groups != null) {
				cannedMessage = SynchronousDbOperations.getInstance()
						.listCannedMessages(groups, fetchContent);
				if (cannedMessage == null) {
					throw new AppServerAdapterException(
							"Database returned error", true);
				}
			} else {
				throw new AppServerAdapterException(
						"The groups attribute has not been supplied", true);
			}

			return cannedMessage;
		} finally {
			synchronized (measurements) {
				measurements.incrementListCannedMsgEndCount();
			}
		}
	}

	public Properties getProfile(String profileName, String browserType)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementProfileRequestStartCount();
		}

		// AceLogger.Instance().log(
		// AceLogger.INFORMATIONAL,
		// AceLogger.SYSTEM_LOG,
		// "AppServerAdapter.getProfile() -- Received profile request for : "
		// + profileName + ", " + browserType);

		try {
			String path = AceConfigFileHelper.getAcePath("profiles",
					profileName + "-" + browserType + ".properties");
			File file = new File(path);
			if (!file.exists()) {
				path = AceConfigFileHelper.getAcePath("profiles", profileName
						+ ".properties");
				file = new File(path);
			}

			Properties p = new Properties();

			FileInputStream str = new FileInputStream(file);
			p.load(str);
			str.close();
			return p;
		} catch (IOException e) {
			throw new AppServerAdapterException(e.getMessage(), false, e);
		} finally {
			synchronized (measurements) {
				measurements.incrementProfileRequestEndCount();
			}
		}
	}

	public String getParam(String object, String paramName)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementGetParamStartCount();
		}
		try {
			return rmi.getRemoteAccess().getParam(object, paramName);
		} catch (Exception e) {
			throw new AppServerAdapterException(e.getMessage(), false, e);
		} finally {
			synchronized (measurements) {
				measurements.incrementGetParamEndCount();
			}
		}
	}

	public void writeCdr(CDRInterface cdr) {
		CDRHandler handler = CDRHandler.getInstance();
		if (handler != null) {
			if (!handler.sendCDR(cdr)) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"AppServerAdapter.writeCdr() -- Could not send CDR "
								+ cdr.getClass().getSimpleName());
			}
		}
	}

	public GroupInfo[] getGroupInfo(String user) {
		synchronized (measurements) {
			measurements.incrementGetGroupInfoStartCount();
		}

		try {
			List<String> owners = SynchronousDbOperations.getInstance()
					.getGroupOwners(user);
			List<GroupInfo> list = new ArrayList<GroupInfo>();
			if (owners != null) {
				for (String owner : owners) {
					GroupInfo groupInfo = new GroupInfo();
					groupInfo.setGroupName(owner);
					try {
						String key = "com.quikj.application.web.talk.feature.operator.Operator:"
								+ owner;
						String val = getParam(key, "subscriber-queue-size");
						if (val != null) {
							groupInfo.setQueueSize(Integer.parseInt(val));
						}						

						val = getParam(key, "all-operators-busy");
						if (val != null) {
							groupInfo
							.setAllOperatorsBusy(Boolean.parseBoolean(val));
						}

						val = getParam(key, "operator-queue-size");
						if (val != null) {
							groupInfo.setNumOperators(Integer.parseInt(val));
						}
						
						val = getParam(key, "operators-with-dnd-count");
						if (val != null) {
							groupInfo.setNumDND(Integer.parseInt(val));
						}
						
						val = getParam(key, "estimated-wait-time");
						if (val != null) {
							groupInfo.setWaitTime(val);
						}
						
						val = getParam(key, "paused-until");
						if (val != null) {
							groupInfo.setPausedUntil(Long.parseLong(val));
						}
						
						list.add(groupInfo);
					} catch (Exception e) {
						AceLogger.Instance().log(
								AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								"AppServerAdapter.getGroupInfo() -- Could not get group parameters. Error: "
										+ e.getMessage(), e);
					}
				}
			}
			return list.toArray(new GroupInfo[list.size()]);
		} finally {
			synchronized (measurements) {
				measurements.incrementGetGroupInfoEndCount();
			}
		}
	}

	public HashMap<Integer, String> getSecurityQuestions(String userid)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementGetSecurityQuestionsStartCount();
		}

		try {
			if (userid == null) {
				throw new AppServerAdapterException(
						"The userid attribute has not been supplied", true);
			}

			try {
				HashMap<Integer, String> questions = SynchronousDbOperations
						.getInstance().getSecurityQuestions(userid);
				return questions;
			} catch (Exception e) {
				throw new AppServerAdapterException("databaseError", true);
			}
		} finally {
			synchronized (measurements) {
				measurements.incrementGetSecurityQuestionsEndCount();
			}
		}
	}

	public void resetPassword(String userid,
			HashMap<Integer, String> securityAnswers, String locale)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementResetPasswordStartCount();
		}

		try {
			LostUsernamePassword.resetPassword(userid, securityAnswers, locale);
		} catch (AceException e) {
			throw new AppServerAdapterException(e.getMessage(),
					e.isRecoverable());
		} catch (Exception e) {
			throw new AppServerAdapterException(e.getMessage(), false, e);
		} finally {
			synchronized (measurements) {
				measurements.incrementResetPasswordEndCount();
			}
		}
	}

	public void recoverLostUsername(String address, String locale)
			throws AppServerAdapterException {
		synchronized (measurements) {
			measurements.incrementRecoverUsernameStartCount();
		}

		try {
			LostUsernamePassword.recoverLostUsername(address, locale);
		} catch (AceException e) {
			throw new AppServerAdapterException(e.getMessage(),
					e.isRecoverable());
		} catch (Exception e) {
			throw new AppServerAdapterException(e.getMessage(), false, e);
		} finally {
			synchronized (measurements) {
				measurements.incrementRecoverUsernameEndCount();
			}
		}
	}
}
