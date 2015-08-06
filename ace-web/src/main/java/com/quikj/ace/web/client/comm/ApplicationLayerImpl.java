/**
 * 
 */
package com.quikj.ace.web.client.comm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Timer;
import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.app.WebMessage;
import com.quikj.ace.web.client.AceClientException;
import com.quikj.ace.web.client.ClientProperties;

// TODO messages may get lost if the server communication is temporarily disrupted. 
// Modify the retry algorithm so that the request/response message being sent is saved 
// in these cases and resent once the communication is established.

/**
 * @author amit
 * 
 */
public class ApplicationLayerImpl implements Server, ApplicationLayer {

	private static final int DEFAULT_APP_ID = 2;
	private static final String DEFAULT_CONTENT_TYPE = Message.CONTENT_TYPE_XML;
	private static final int REQUEST_TIMEOUT_CHECK_DURATION = 10 * 1000;

	private TransportLayer transport;
	private Timer timeoutTimer;
	private Logger logger;
	private RequestListener requestListener;
	private Map<Integer, RequestInfo> requestMap = new HashMap<Integer, RequestInfo>();
	private int appId;

	protected ApplicationLayerImpl() {
		logger = Logger.getLogger(getClass().getName());

		appId = ClientProperties.getInstance().getIntValue(
				ClientProperties.APP_ID, DEFAULT_APP_ID);
	}

	private void initTimeoutTimer() {
		timeoutTimer = new Timer() {

			@Override
			public void run() {
				if (logger.isLoggable(Level.FINEST)) {
					logger.finest("Timeout timer expired");
				}
				processTimeout();
			}
		};

		timeoutTimer.scheduleRepeating(REQUEST_TIMEOUT_CHECK_DURATION);
	}

	@Override
	public void connect() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Connecting to server");
		}

		if (transport.isConnected()) {
			logger.severe("Trying to connect to an already connected session");
			throw new AceClientException(
					"An internal error occured. See logs for details.");
		}

		transport.connect();
		initTimeoutTimer();
		requestMap.clear();
	}

	@Override
	public void setRequestListener(RequestListener requestListener) {
		this.requestListener = requestListener;
	}

	@Override
	public int sendRequest(WebMessage request, String contentType,
			boolean multipleResponse, long timeout,
			ResponseListener responseListener) {

		if (timeout > 0L && responseListener == null) {
			logger.severe("Invalid argument: timeout = " + timeout
					+ ", listener = " + responseListener);
			throw new AceClientException(
					"An internal error occured. See logs for details.");
		}

		RequestMessage req = new RequestMessage();
		req.setMethod(RequestMessage.APPLICATION_METHOD);
		req.setMessage(request);

		if (contentType == null) {
			contentType = DEFAULT_CONTENT_TYPE;
		}
		req.getHeaders().put(Message.CONTENT_TYPE, contentType);

		req.getHeaders().put(Message.PLUGIN_APP_ID, Integer.toString(appId));

		int requestId = transport.sendRequest(req);

		if (responseListener != null) {
			Date timeoutTime = computeTimeout(timeout);

			requestMap.put(requestId, new RequestInfo(requestId,
					multipleResponse, timeoutTime, responseListener));
		}

		return requestId;
	}

	private Date computeTimeout(long timeout) {
		if (timeout > 0L) {
			long time = new Date().getTime() + timeout;
			return new Date(time);
		} else {
			return null;
		}
	}

	@Override
	public void cancelRequest(int requestId) {
		RequestInfo info = requestMap.remove(requestId);
		if (info == null) {
			logger.severe("Request received to cancel request " + requestId
					+ " but it does not exist");
		}
	}

	@Override
	public void sendResponse(int requestId, int status, String reason,
			String contentType, WebMessage response) {
		ResponseMessage rsp = new ResponseMessage();
		rsp.setStatus(status);
		rsp.setReason(reason);
		rsp.setMessage(response);

		rsp.getHeaders().put(Message.PLUGIN_APP_ID, Integer.toString(appId));
		if (contentType == null) {
			contentType = DEFAULT_CONTENT_TYPE;
		}
		rsp.getHeaders().put(Message.CONTENT_TYPE, contentType);

		transport.sendResponse(requestId, rsp);
	}

	@Override
	public boolean isConnected() {
		return transport.isConnected();
	}

	private void processTimeout() {
		Date currentTime = new Date();
		Iterator<Entry<Integer, RequestInfo>> i = requestMap.entrySet()
				.iterator();

		List<RequestInfo> events = new ArrayList<RequestInfo>();
		while (i.hasNext()) {
			Entry<Integer, RequestInfo> element = i.next();
			RequestInfo req = element.getValue();

			if (req.getTimeout() != null && currentTime.after(req.getTimeout())) {
				events.add(req);
				i.remove();
			}
		}

		// Fire the timeout events
		for (RequestInfo event : events) {
			try {
				event.getListener().timeoutOccured(event.getRequestId());
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Exception occured in timeout listener : "
						+ e.getClass().getName() + " - " + e.getMessage());
			}
		}
	}

	@Override
	public void processIncomingMessages(List<Message> messages) {
		List<Message> events = new ArrayList<Message>();
		List<RequestInfo> rspRequests = new ArrayList<RequestInfo>();
		List<Integer> rspRequestIds = new ArrayList<Integer>();

		boolean serverDisconnected = false;
		for (Message msg : messages) {
			if (msg instanceof RequestMessage) {
				RequestMessage req = (RequestMessage) msg;
				if ((req.getMethod().equals(RequestMessage.DISCONNECT_METHOD))) {
					serverDisconnected = true;
					break;
				}

				if (requestListener == null) {
					logger.warning("Received a request but no listener is available. Going to toss the message");
					continue;
				}

				events.add(msg);
				continue;
			}

			// process response message
			assert (msg instanceof ResponseMessage);
			String crlIdStr = msg.getHeaders().get(Message.CORRELATION_ID);
			if (crlIdStr == null) {
				logger.warning("Received a response with no request id. Going to toss the message.");
				continue;
			}

			int reqId = Integer.parseInt(crlIdStr);
			RequestInfo reqElement = requestMap.get(reqId);
			if (reqElement == null) {
				logger.warning("Received a response with unmatched request id. Going to toss the message");
				continue;
			}

			if (!reqElement.isMultiple()) {
				requestMap.remove(reqId);
			}

			events.add(msg);
			rspRequestIds.add(reqId);
			rspRequests.add(reqElement);
		}

		// Fire the listeners
		int count = 0;
		for (Message event : events) {
			if (event instanceof RequestMessage) {
				if (requestListener != null) {
					String reqId = event.getHeaders().get(
							Message.CORRELATION_ID);
					try {
						requestListener.requestReceived(reqId == null ? -1
								: Integer.parseInt(reqId), event.getHeaders()
								.get(Message.CONTENT_TYPE),
								(RequestMessage) event);
					} catch (Exception e) {
						e.printStackTrace();
						logger.warning("Exception occured in request message listener : "
								+ e.getClass().getName()
								+ " - "
								+ e.getMessage());
					}
				}
			} else if (event instanceof ResponseMessage) {
				RequestInfo reqElement = rspRequests.get(count);
				int crlId = rspRequestIds.get(count);
				try {
					reqElement.getListener().responseReceived(crlId,
							event.getHeaders().get(Message.CONTENT_TYPE),
							(ResponseMessage) event);
				} catch (Exception e) {
					e.printStackTrace();
					logger.warning("Exception occured in response message listener : "
							+ e.getClass().getName() + " - " + e.getMessage());
				}
				count++;
			}
		}

		if (serverDisconnected) {
			logger.info("Server disconnected the session");
			cancelTimeoutTimer();

			transport.disconnect(false);

			if (requestListener != null) {
				requestListener.disconnected();
			}
		}
	}

	public void cancelTimeoutTimer() {
		if (timeoutTimer != null) {
			timeoutTimer.cancel();
		}
	}

	@Override
	public void changeTimeout(int requestId, long timeout) {
		RequestInfo req = requestMap.get(requestId);
		if (req == null) {
			return;
		}

		req.setTimeout(computeTimeout(timeout));
	}

	@Override
	public void disconnect() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Disconnecting from server");
		}

		cancelTimeoutTimer();
		transport.disconnect();
		requestMap.clear();
	}

	@Override
	public void disconnected() {
		logger.info("Server disconnected the session");
		if (requestListener != null) {
			try {
				requestListener.disconnected();
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Exception occured in server diconnect listener : "
						+ e.getClass().getName() + " - " + e.getMessage());
			}
		}

		cancelTimeoutTimer();
	}

	@Override
	public void connected() {
		logger.info("Server is connected the session");
		if (requestListener != null) {
			try {
				requestListener.connected();
			} catch (Exception e) {
				e.printStackTrace();
				logger.warning("Exception occured in server connect listener : "
						+ e.getClass().getName() + " - " + e.getMessage());
			}
		}
	}

	@Override
	public boolean isFastPoll() {
		return requestMap.size() > 0;
	}

	@Override
	public void setTransport(TransportLayer transport) {
		this.transport = transport;
	}
}
