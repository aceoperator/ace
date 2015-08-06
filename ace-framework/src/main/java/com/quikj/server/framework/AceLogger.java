package com.quikj.server.framework;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

public class AceLogger extends AceThread implements AceLoggerInterface {

	private static final int MAX_MESSAGE_LENGTH = 1000;

	class LogMessageEvent implements AceMessageInterface {
		private AceLogMessage message;

		public LogMessageEvent(AceLogMessage message) {
			this.message = message;
		}

		public AceLogMessage getMessage() {
			return message;
		}

		public String messageType() {
			return "AceLogMessage";
		}
	}

	// the following numnber must start with 0 and incerement by 1
	public static final int INFORMATIONAL = 0;
	public static final int WARNING = 1;
	public static final int ERROR = 2;
	public static final int FATAL = 3;

	public static final int NUM_MSG_TYPES = 4;

	// the following constants must have the same order as above
	public static final String[] SEVERITY_S = { "INFO", "WARN", "ERROR",
			"FATAL" };

	public static final int LOG_NONE = 0;
	public static final int SYSTEM_LOG = 1;
	public static final int USER_LOG = 2;

	private String hostName = "";

	private String processName;

	private int processGroup;

	private static AceLogger instance = null;

	public AceLogger(String process_name, int process_group)
			throws IOException, UnknownHostException,
			ParserConfigurationException, AceException {
		super("AceLogger");

		try {
			hostName = InetAddress.getLocalHost().getHostName();
			processName = process_name;
			processGroup = process_group;
		} catch (UnknownHostException e) {
			dispose();
			throw e;
		}

		// Initialize log4j
		PropertyConfigurator.configure(AceConfigFileHelper.getAcePath("config",
				"log4j.properties"));

		instance = this;
	}

	public static AceLogger Instance() {
		return instance;
	}

	public void dispose() {
		interruptWait(0, "disposed");
	}

	public boolean log(int severity, int msg_type, String message) {
		return sendLogMessage(severity, msg_type, message, null, null);
	}

	public boolean log(int severity, int msg_type, String message, Throwable e) {
		return sendLogMessage(severity, msg_type, message, null, e);
	}

	public boolean log(int severity, int msg_type, String message,
			String processName) {
		return sendLogMessage(severity, msg_type, message, processName, null);
	}

	private boolean sendLogMessage(int severity, int msg_type, String message,
			String processName, Throwable e) {
		AceLogMessage log_msg = new AceLogMessage(processGroup, hostName,
				processName == null ? this.processName : processName, severity,
				msg_type, message, e);
		return sendMessage(new LogMessageEvent(log_msg));
	}

	public void run() {
		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				// print error message
				log(AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						getName()
								+ " - AceLogger.run() -- A null message was received while waiting for a message - "
								+ getErrorMessage());

				dispose();
				break;
			}

			if ((message instanceof AceSignalMessage) == true) {
				// A signal message is received

				// print informational message
				log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
						getName() + " - AceLoggger.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());

				break;
			} else if ((message instanceof AceLogger.LogMessageEvent) == true) {
				try {
					AceLogMessage log = ((AceLogger.LogMessageEvent) message)
							.getMessage();

					StringBuffer buffer = new StringBuffer();
					buffer.append(log.getMessage());
					if (log.getException() != null) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						PrintStream stream = new PrintStream(bos);
						log.getException().printStackTrace(stream);
						buffer.append("\nException trace:\n");
						buffer.append(bos.toString());
						stream.close();
						bos.close();
					}

					String text = buffer.toString();
					if (text.length() >= MAX_MESSAGE_LENGTH) {
						text = text.substring(0, MAX_MESSAGE_LENGTH - 3)
								+ "...";
					}
					text = escape(text);

					switch (log.getSeverity()) {
					case INFORMATIONAL:
						LogFactory.getLog(log.getProcessName()).info(text);
						break;

					case WARNING:
						LogFactory.getLog(log.getProcessName()).warn(text);
						break;

					case ERROR:
						LogFactory.getLog(log.getProcessName()).error(text);
						break;

					case FATAL:
						LogFactory.getLog(log.getProcessName()).fatal(text);
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
					// continue
				}
			}
			// else, ignore
		}

		super.dispose();
	}

	private String escape(String s) {
		String ret = s;
		if (s.indexOf("'") != -1) {
			StringBuffer buffer = new StringBuffer();
			char c;
			for (int i = 0; i < s.length(); i++) {
				if ((c = s.charAt(i)) == '\'') {
					buffer.append("''");
				} else {
					buffer.append(c);
				}
			}
			ret = buffer.toString();
		}
		return ret;
	}
}
