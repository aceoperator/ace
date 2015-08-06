package com.quikj.server.framework;

import java.util.Date;

public class AceLogMessage implements AceLogMessageInterface {

	private String processName;

	private int severity;

	private int msgType;

	private String message;

	private String hostName = "";

	private long timeStamp = 0;
	
	private int logGroup = 0;
	
	private Throwable exception;
	
	public AceLogMessage(Date timestamp, int log_group, String host_name,
			String pname, int severity, int msg_type, String message,
			Throwable e) {
		if (timestamp == null) {
			timestamp = new Date();
		}

		logGroup = log_group;
		timeStamp = timestamp.getTime();
		hostName = host_name;
		processName = pname;
		this.severity = severity;
		msgType = msg_type;
		this.message = message;
		this.exception = e;
	}

	// formatter constructor
	public AceLogMessage(int log_group, String host_name, String pname,
		int severity, int msg_type, String message, Throwable e) {
		this(null, log_group, host_name, pname, severity, msg_type,
				message, e);
	}
	
	public String getHostName() {
		return hostName;
	}
	public String getMessage() {
		if (message != null) {
			return new String(message);
		} else {
			return null;
		}
	}
	public int getMessageType() {
		return msgType;
	}
	
	public String getProcessName() {
		if (processName != null) {
			return new String(processName);
		} else {
			return null;
		}
	}
	
	public int getSeverity() {
		return severity;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	
	public String messageType() {
		return "LOG_MESSAGE";
	}

	public int getLogGroup() {
		return logGroup;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
	}
}
