package com.quikj.server.app;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.quikj.ace.messages.vo.app.Message;
import com.quikj.ace.messages.vo.app.RequestMessage;
import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.app.WebMessage;
import com.quikj.server.app.adapter.AppServerAdapter;
import com.quikj.server.app.adapter.AppServerAdapterException;
import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;
import com.quikj.server.framework.AceTimerMessage;

public class RemoteEndPoint extends AceThread implements EndPointInterface {

	private static final String PART_FILE_EXTENSION = ".part";

	private static final String MESSAGE_TRACE_PROCESS_NAME = "APPLSERV-TRACE";

	public static final String PLAIN_TEXT = "text/plain";

	private static enum Direction {
		INBOUND, OUTBOUND
	}

	private String identifier;

	private PluginAppClientInterface plugin = null;

	private int pluginApplicationId;

	private String clientHost;

	private String endUserCookie;

	private Map<String, Object> keyValuePair = new HashMap<String, Object>();

	private static boolean trace = false;
	private static boolean traceMessage = false;

	private static ThreadGroup endpointThreadGroup = new ThreadGroup("Endpoint");

	private Map<String, Writer> openFiles = new HashMap<String, Writer>();

	static {
		String traceProp = System.getProperty("com.quikj.server.web.trace");
		if (traceProp != null) {
			if (traceProp.equals("true")) {
				trace = true;
			}
		}

		traceProp = System.getProperty("com.quikj.server.web.trace.message");
		if (traceProp != null) {
			if (traceProp.equals("true")) {
				traceMessage = true;
			}
		}
	}

	private String sessionId;

	private String disposeReasonText;

	private AppServerAdapter adapter;

	public RemoteEndPoint(String sessionId, String clientHost,
			String endUserCookie, AppServerAdapter adapter) throws IOException {
		super(endpointThreadGroup, "EndPoint");
		this.sessionId = sessionId;
		setName("EndPoint_" + sessionId);

		this.clientHost = clientHost;
		this.endUserCookie = endUserCookie;
		this.adapter = adapter;

		identifier = formulateIdentifier(sessionId, clientHost);
	}

	private String formulateIdentifier(String sessionId, String clientHost) {
		return ApplicationServer.getInstance().getHostName() + "_" + clientHost
				+ "_" + new Date().getTime() + "_" + sessionId;
	}

	private boolean connectToPlugin(Message msg) {
		boolean to_cont = true;

		String appl_id_s = msg.getHeaders().get(Message.PLUGIN_APP_ID);
		if (appl_id_s == null) {
			// the application id was not specified
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- EndPoint.connectToPlugin() -- Received message does not contain the header field: "
									+ Message.PLUGIN_APP_ID);

			to_cont = false;
		}

		if (to_cont) {
			try {
				pluginApplicationId = Integer.parseInt(appl_id_s);
			} catch (NumberFormatException ex) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- EndPoint.connectToPlugin() -- Received message does not contain a numeric plugin identifier: "
										+ appl_id_s);
				to_cont = false;
			}
		}

		if (to_cont) {
			try {
				plugin = PluginAppList.Instance().newInstance(
						pluginApplicationId);
			} catch (AceException e) {
				// the plugin was not found
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- EndPoint.connectToPlugin() -- Received message's request for plugin application: "
										+ appl_id_s
										+ " could not be instantiated", e);

				Map<String, String> headers = new HashMap<String, String>();
				ResponseMessage rsp = new ResponseMessage(404,
						"Plugin application: " + appl_id_s + " not found",
						headers, "1.1", null);
				headers.put(Message.PLUGIN_APP_ID, appl_id_s);

				if (msg.getHeaders().containsKey(Message.CORRELATION_ID)) {
					headers.put(Message.CORRELATION_ID,
							msg.getHeaders().get(Message.CORRELATION_ID));
				}

				sendMessageToClient(rsp);
				to_cont = false;
			}
		}

		if (to_cont) {
			// now that we have created a plugin, inform it of the new
			// connection
			to_cont = plugin.newConnection(clientHost, endUserCookie, this);
		}

		return to_cont;
	}

	public void dispose() {
		dispose(null);
	}

	public void dispose(String reasonText) {
		traceLog("End point being disposed");

		if (reasonText != null) {
			disposeReasonText = reasonText;
		}
		// kill the thread by sending a signal
		if (interruptWait(AceSignalMessage.SIGNAL_TERM,
				"Request to kill the thread received") == false) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ " EndPoint.dispose() -- Error occured while sending signal : "
									+ getErrorMessage());
		}

	}

	private void cleanup() {
		try {
			if (plugin != null) {
				plugin.connectionClosed(disposeReasonText);
				plugin = null;
			}

			for (Writer writer : openFiles.values()) {
				try {
					writer.close();
				} catch (IOException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ " EndPoint.dispose() -- Error occured while closing open file",
									e);
				}
			}
		} finally {
			super.dispose();
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public Object getParam(String key) {
		synchronized (keyValuePair) {
			return keyValuePair.get(key);
		}
	}

	public void removeParam(String key) {
		synchronized (keyValuePair) {
			keyValuePair.remove(key);
		}
	}

	private boolean processClientMessage(Message message) {
		boolean to_cont = true;

		if (plugin != null) {
			// if there is a plugin associated with this client
			to_cont = verifyPluginMessage(message);
			if (to_cont) {
				// send the message to the plugin
				to_cont = sendMessageToPlugin(message);
			}
		} else { // this is the first message from the client
			if (message instanceof RequestMessage) {
				RequestMessage req = (RequestMessage) message;
				String method = req.getMethod();

				// if the message is for a plugin
				if (method.equalsIgnoreCase(RequestMessage.APPLICATION_METHOD)) {
					to_cont = connectToPlugin(message);
					if (to_cont == true) {
						to_cont = sendMessageToPlugin(message);
					}
				} else {
					// Standard HTTP messages are not supported
					to_cont = false;
				}
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- EndPoint.processClientMessage() -- The first HTTP message received from the client side is not a request message");

				to_cont = false;
			}
		}

		return to_cont;
	}

	public void run() {
		boolean signal = false;
		boolean to_cont = true;
		while (to_cont) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- EndPoint.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if (message instanceof ClientMessage) {
				ClientMessage cmsg = (ClientMessage) message;
				Message msg = cmsg.getMessage();
				to_cont = processClientMessage(msg);
			} else if (message instanceof AceTimerMessage) {
				// this timer must have been started by a plugin, pass
				// the event
				if (plugin != null) {
					// send the message to the plugin
					to_cont = plugin.eventReceived(message);
				} else {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- EndPoint.run() -- An unexpected timer message is received");

				}
			} else if (message instanceof AceSignalMessage) {
				// A signal message is received
				signal = true;
				traceLog("A signal "
						+ ((AceSignalMessage) message).getSignalId()
						+ " is received : "
						+ ((AceSignalMessage) message).getMessage());
				break;
			} else {
				if (plugin != null) {
					// send the message to the plugin
					to_cont = plugin.eventReceived(message);
				} else {
					// print error message
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- EndPoint.run() -- An unexpected event is received : "
											+ message.messageType());
				}
			}
		} // forever

		// do the necessary clean-up
		cleanup();

		if (!signal) {
			try {
				adapter.endPointTerminated(sessionId);
			} catch (AppServerAdapterException e) {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- EndPoint.run() -- An exception was thrown while trying to terminate from the adapter : "
										+ e.getMessage(), e);
			}
		}
	}

	public boolean sendEvent(AceMessageInterface message) {
		return sendMessage(message);
	}

	private boolean sendMessageToClient(Message msg) {
		boolean to_cont = true;

		try {
			adapter.sendMessage(sessionId, msg);
		} catch (AppServerAdapterException e) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- EndPoint.sendMessageToClient() -- Error while sending message - "
									+ e.getMessage()
									+ ". Message was not sent.", e);
			to_cont = false;
		}
		return to_cont;
	}

	private boolean sendMessageToPlugin(Message msg) {
		boolean to_cont = true;

		int crl_id = Integer.parseInt(msg.getHeaders().get(
				Message.CORRELATION_ID));

		String content_type = msg.getHeaders().get(Message.CONTENT_TYPE);
		if (content_type == null) {
			content_type = PLAIN_TEXT;
		}

		if (msg instanceof RequestMessage) {
			RequestMessage req = (RequestMessage) msg;
			WebMessage body = msg.getMessage();
			traceMessage(body, Direction.INBOUND, null, req.getMethod());
			to_cont = plugin.requestReceived(crl_id, content_type, body);
		} else { // response message
			ResponseMessage rsp = (ResponseMessage) msg;
			int status = rsp.getStatus();
			String reason = rsp.getReason();
			WebMessage body = msg.getMessage();
			traceMessage(body, Direction.INBOUND, status, null);
			to_cont = plugin.responseReceived(crl_id, status, reason,
					content_type, body);
		}

		return to_cont;
	}

	public boolean sendRequestMessageToClient(int request_id,
			String content_type, WebMessage body) {

		Map<String, String> headers = new HashMap<String, String>();
		RequestMessage req = new RequestMessage(
				RequestMessage.APPLICATION_METHOD, "1.1", headers, body);

		headers.put(Message.PLUGIN_APP_ID,
				(new Integer(pluginApplicationId)).toString());
		headers.put(Message.CORRELATION_ID,
				(new Integer(request_id)).toString());

		if (content_type != null) {
			headers.put(Message.CONTENT_TYPE, content_type);
		}

		traceMessage(body, Direction.OUTBOUND, null,
				RequestMessage.APPLICATION_METHOD);

		return sendMessageToClient(req);
	}

	public boolean sendResponseMessageToClient(int request_id, int status,
			String reason, String content_type, WebMessage body) {

		Map<String, String> headers = new HashMap<String, String>();
		ResponseMessage rsp = new ResponseMessage(status, reason, headers,
				"1.1", body);

		headers.put(Message.PLUGIN_APP_ID,
				(new Integer(pluginApplicationId)).toString());
		headers.put(Message.CORRELATION_ID,
				(new Integer(request_id)).toString());

		if (content_type != null) {
			headers.put(Message.CONTENT_TYPE, content_type);
		}

		traceMessage(body, Direction.OUTBOUND, status, null);

		return sendMessageToClient(rsp);
	}

	public void setParam(String key, Object value) {
		synchronized (keyValuePair) {
			keyValuePair.put(key, value);
		}
	}

	public boolean verifyPluginMessage(Message msg) {
		boolean to_cont = true;

		// if the message is a request message
		if (msg instanceof RequestMessage) {
			// make sure that the method matches
			RequestMessage req = (RequestMessage) msg;
			if (!req.getMethod().equalsIgnoreCase(
					RequestMessage.APPLICATION_METHOD)) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
										+ "the request message does not use an application method");
				to_cont = false;
			}
		}

		String appl_id_s = msg.getHeaders().get(Message.PLUGIN_APP_ID);
		if (appl_id_s == null) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
									+ "the message does not have a plugin_app_id header");
			to_cont = false;
		}

		if (to_cont) {
			try {
				int appl_id = Integer.parseInt(appl_id_s);
				if (appl_id != pluginApplicationId) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
											+ "the plugin_app_id does not match ("
											+ appl_id + ")");
					to_cont = false;
				}
			} catch (NumberFormatException ex) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
										+ "the plugin_app_id is not numeric");
				to_cont = false;
			}
		}

		String crl_id_s = msg.getHeaders().get(Message.CORRELATION_ID);
		if (crl_id_s == null) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
									+ "the correlation_id header is missing");
			to_cont = false;
		}

		if (to_cont) {
			try {
				Integer.parseInt(crl_id_s);
			} catch (NumberFormatException ex) {
				to_cont = false;
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ " - EndPoint.verifyPluginMessage() -- A badly formatted service message is received from the endpoint: "
										+ "the correlation_id header is not numeric");

			}
		}

		return to_cont;
	}

	private void traceMessage(WebMessage body, Direction dir, Integer status,
			String method) {

		if (!trace) {
			return;
		}

		String message = null;
		if (body == null) {
			message = "[EMPTY]";
		} else if (traceMessage) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			XMLEncoder encoder = new XMLEncoder(stream);
			encoder.writeObject(body);
			encoder.close();
			message = stream.toString();
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			message = body.getClass().getName() + ": " + body.toString();
		}

		AceLogger.Instance().log(
				AceLogger.INFORMATIONAL,
				AceLogger.SYSTEM_LOG,
				getName()
						+ "- EndPoint.traceMessage() --  "
						+ getName()
						+ " - "
						+ dir.name()
						+ " - "
						+ (status == null ? ("REQUEST: " + method)
								: ("RESPONSE: " + status)) + "\n" + message,
				MESSAGE_TRACE_PROCESS_NAME);
	}

	private void traceLog(String message) {
		if (!trace) {
			return;
		}

		AceLogger.Instance().log(
				AceLogger.INFORMATIONAL,
				AceLogger.SYSTEM_LOG,
				getName() + "- EndPoint.traceMessage() --  " + getName()
						+ " - " + message, MESSAGE_TRACE_PROCESS_NAME);
	}

	public static boolean isTrace() {
		return trace;
	}

	public static void setTrace(boolean trace) {
		RemoteEndPoint.trace = trace;
	}

	public static boolean isTraceMessage() {
		return traceMessage;
	}

	public static void setTraceMessage(boolean traceMessage) {
		RemoteEndPoint.traceMessage = traceMessage;
	}

	public static int getEndpointCount() {
		return endpointThreadGroup.activeCount();
	}

	public synchronized void writeToFile(String path, String prefix,
			String message) {
		try {
			Writer writer = openFiles.get(path);
			if (writer == null) {
				File f = new File(path);
				if (f.exists()) {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Endpoint.writeToFile() -- Error opening file "
											+ path
											+ " because it already exists and has been closed previously");
					return;
				}

				writer = new FileWriter(path + PART_FILE_EXTENSION);
				writer.write(prefix);
				openFiles.put(path, writer);
			}

			writer.append(message);
			writer.flush();
		} catch (IOException e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							Thread.currentThread().getName()
									+ "- Endpoint.writeToFile() -- Error opening file ",
							e);
			return;
		}

		return;
	}

	public synchronized void closeFile(String path, String suffix) {
		try {
			Writer writer = openFiles.get(path);
			if (writer != null) {

				writer.write(suffix);
				writer.close();
				openFiles.remove(path);

				File closedFileName = new File(path);
				if (closedFileName.exists()) {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Endpoint.closeFile() -- Error closing file "
											+ path
											+ " because it already exists");
					return;
				}

				File f = new File(path + PART_FILE_EXTENSION);
				if (!f.renameTo(closedFileName)) {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Endpoint.closingFile() -- Error closing file "
											+ path
											+ " because the system returned an error");
				}
			}
		} catch (IOException e) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					Thread.currentThread().getName()
							+ "- Endpoint.closeFile() -- Error closing file ",
					e);

		}
	}

	public synchronized void renameFile(String oldPath, String newPath) {
		try {
			Writer writer = openFiles.get(oldPath);
			if (writer != null) {

				writer.close();
				openFiles.remove(oldPath);

				File f = new File(oldPath + PART_FILE_EXTENSION);
				File newFile = new File(newPath + PART_FILE_EXTENSION);
				if (newFile.exists()) {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Endpoint.renameFile() -- Error renaming file "
											+ oldPath
											+ " to "
											+ newPath
											+ " because the file already exists");
				}

				if (!f.renameTo(newFile)) {
					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									Thread.currentThread().getName()
											+ "- Endpoint.renameFile() -- Error renaming file "
											+ oldPath
											+ " to "
											+ newPath
											+ " because the system returned an error");
				}

				writer = new FileWriter(newPath + PART_FILE_EXTENSION, true);
				openFiles.put(newPath, writer);
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								Thread.currentThread().getName()
										+ "- Endpoint.renameFile() -- Error renaming file "
										+ oldPath + "  does not exist");
			}
		} catch (IOException e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							Thread.currentThread().getName()
									+ "- Endpoint.renameFile() -- Error renaming file ",
							e);
		}
	}
}
