package com.quikj.ace.web.client.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.web.client.AceClientException;
import com.quikj.ace.web.client.AceOperatorService;
import com.quikj.ace.web.client.AceOperatorServiceAsync;
import com.quikj.ace.web.client.ClientProperties;
import com.quikj.ace.web.shared.AceServerException;

public class TransportLayerImpl implements TransportLayer {

	private ApplicationLayer application;
	private AceOperatorServiceAsync service;
	private Logger logger;
	private int maxReconnectRetry;
	private ServerMessageListener serverListener;
	private String sessionId = null;
	private Timer pollingTimer;
	private boolean connected = false;
	private int failureCount = 0;
	private Integer nextRequestId = 0;

	private List<Message> queuedMessages = new ArrayList<Message>();
	private List<Message> responsePendingMessages = new ArrayList<Message>();

	class ServerMessageListener implements AsyncCallback<List<Message>> {
		public ServerMessageListener() {
		}

		@Override
		public void onFailure(Throwable caught) {
			serverMessagingFailure(caught);
		}

		@Override
		public void onSuccess(List<Message> messages) {
			serverMessagingSuccess(messages);
		}
	}

	public TransportLayerImpl() {
		service = AceOperatorService.Util.getInstance();

		logger = Logger.getLogger(getClass().getName());

		maxReconnectRetry = ClientProperties.getInstance().getIntValue(
				ClientProperties.MAX_RECONNECT_RETRY,
				TransportLayer.DEFAULT_MAX_FAILURE_COUNT);
		serverListener = new ServerMessageListener();
	}

	@Override
	public void connect() {
		if (sessionId != null) {
			throw new AceClientException(
					"Trying to connect to a session that already has a session id");
		}

		RequestBuilder builder = service.connect(new AsyncCallback<String>() {

			@Override
			public void onFailure(Throwable caught) {
				logger.severe("Exception from server while connecting - "
						+ caught.getClass().getName() + " - "
						+ caught.getMessage());
				disconnect(false);
				application.disconnected();
			}

			@Override
			public void onSuccess(String sessionId) {
				TransportLayerImpl.this.sessionId = sessionId;
				application.connected();
			}
		});

		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}

		connected = true;
		failureCount = 0;
		sessionId = null;
		initPollTimer();
	}

	private void initPollTimer() {
		pollingTimer = new Timer() {
			@Override
			public void run() {
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("Poll timer expired");
				}
				processPolling();
			}
		};

		startPollTimer();
	}

	private void startPollTimer() {
		int interval = ClientProperties.getInstance().getIntValue(
				ClientProperties.FAST_POLL_TIMER,
				TransportLayer.DEFAULT_FAST_POLL_TIMER);
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Starting poll timer, interval = " + interval);
		}

		pollingTimer.scheduleRepeating(interval);
	}

	private void cancelPollTimer() {
		if (logger.isLoggable(Level.FINEST)) {
			logger.finest("Polling timer is being cancelled");
		}
		pollingTimer.cancel();
	}

	@Override
	public void disconnect(boolean sendDisconnect) {

		if (!connected) {
			logger.severe("Trying to disconnect from an already disconnected session");
			throw new AceClientException(
					"Trying to disconnect from an already disconnected session");
		}

		connected = false;
		failureCount = 0;

		if (sendDisconnect && sessionId != null) {
			sendDisconnect();
		}

		sessionId = null;

		responsePendingMessages.clear();
		queuedMessages.clear();
		cancelPollTimer();
	}

	@Override
	public void disconnect() {
		disconnect(true);
	}

	private void sendDisconnect() {
		if (sessionId == null) {
			throw new AceClientException(
					"Trying to disconnect from a session that has not yet received a session id");
		}

		RequestBuilder builder = service.disconnect(sessionId,
				new AsyncCallback<Void>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.severe("Exception from server while disconnecting - "
								+ caught.getMessage());
					}

					@Override
					public void onSuccess(Void result) {
						application.disconnected();
					}
				});

		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	private void processPolling() {
		if (!connected) {
			// should not happen
			return;
		}

		if (sessionId == null) {
			logger.info("Going to skip polling since the session id has not been received");
			return;
		}

		if (responsePendingMessages.size() > 0) {
			// We have not yet received messages from the previous messages,
			// wait till we receive a response
			return;
		}

		if (queuedMessages.size() > 0) {
			sendMessage();
		} else {
			sendPing();
		}
	}

	private void sendPing() {
		RequestMessage req = new RequestMessage();
		req.setMethod(RequestMessage.PING_METHOD);

		req.getHeaders().put(Message.SESSION_ID_HEADER, sessionId);

		int requestId = nextRequestId++;
		req.getHeaders().put(Message.CORRELATION_ID,
				Integer.toString(requestId));

		RequestBuilder builder = service.exchangeMessages(req, serverListener);
		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
	}

	private void sendMessage() {
		RequestBuilder builder = service.exchangeMessages(queuedMessages,
				serverListener);
		try {
			CommunicationsFactory.sendMessageToServer(builder);
		} catch (RequestException e) {
			logger.severe("Error sending message to the server - "
					+ e.getMessage());
		}
		responsePendingMessages.addAll(queuedMessages);
		queuedMessages.clear();
	}

	private void serverMessagingSuccess(List<Message> messages) {
		failureCount = 0;

		responsePendingMessages.clear();

		if (logger.isLoggable(Level.FINE)) {
			if (messages.size() > 0) {
				logger.fine("Received response from the server. Number of messages received = "
						+ messages.size());
			}
		}

		if (sessionId == null) {
			// Should not happen
			return;
		}

		if (messages == null || messages.size() == 0) {
			return;
		}

		application.processIncomingMessages(messages);
		
		if (queuedMessages.size() > 0) {
			// Handle the queued up messages
			sendMessage();
		}
	}

	private void serverMessagingFailure(Throwable caught) {
		logger.severe("Exception from server while exchanging messages - "
				+ caught.getMessage());
		caught.printStackTrace();

		boolean remainConnected = true;
		boolean serverException = false;
		if (caught instanceof AceServerException) {
			AceServerException e = (AceServerException) caught;
			remainConnected = e.isRecoverable();
			serverException = true;
		} else {
			// Exceptions other than Ace exception
			failureCount++;
			if (failureCount >= maxReconnectRetry) {
				remainConnected = false;
			}
		}

		if (!remainConnected) {
			// Communication has failed with the server.
			failureCount = 0;
			logger.info("Comunication failed with server");
			disconnect(false);
			application.disconnected();
		} else if (!serverException) {
			// Add the response pending messages to the top of the queued
			// message so that we can send it again when the poll timer expires
			// again. This does not need to be done if the exception is thrown
			// from Ace Operator itself because the server has received the
			// message and there is no need for duplication.
			queuedMessages.addAll(0, responsePendingMessages);
			responsePendingMessages.clear();
		}
	}

	@Override
	public int sendRequest(RequestMessage message) {
		if (sessionId == null) {
			throw new AceClientException(
					"Cannot send a request before receiving a session id from the server");
		}

		message.setMethod(RequestMessage.APPLICATION_METHOD);
		message.getHeaders().put(Message.SESSION_ID_HEADER, sessionId);
		int requestId = nextRequestId++;
		message.getHeaders().put(Message.CORRELATION_ID,
				Integer.toString(requestId));
		queuedMessages.add(message);

		sendIfQueueEmpty();

		return requestId;
	}

	private void sendIfQueueEmpty() {
		if (responsePendingMessages.size() == 0 && queuedMessages.size() == 1) {
			// Send the message immediately if this is the only message in the
			// queue and no responses are pending
			sendMessage();
		}
	}

	@Override
	public void sendResponse(int requestId, ResponseMessage message) {
		if (sessionId == null) {
			throw new AceClientException(
					"Cannot send a response before receiving a session id from the server");
		}

		message.getHeaders().put(Message.SESSION_ID_HEADER, sessionId);
		message.getHeaders().put(Message.CORRELATION_ID,
				Integer.toString(requestId));
		queuedMessages.add(message);
		
		sendIfQueueEmpty();
	}

	@Override
	public void setApplication(ApplicationLayer application) {
		this.application = application;
	}
}
