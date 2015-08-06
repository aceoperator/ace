/*
 * LogElementDisplay.java
 *
 * Created on June 3, 2003, 10:37 AM
 */

package com.quikj.application.communicator.admin.controller;

/**
 * 
 * @author bhm
 */
public class LogElementDisplay {

	/** Holds value of property datetime. */
	private String datetime;

	/** Holds value of property severity. */
	private String severity;

	/** Holds value of property hostName. */
	private String hostName;

	/** Holds value of property processName. */
	private String processName;

	/** Holds value of property message. */
	private String message;

	/** Creates a new instance of LogElementDisplay */
	public LogElementDisplay() {
	}

	/**
	 * Getter for property datetime.
	 * 
	 * @return Value of property datetime.
	 * 
	 */
	public String getDatetime() {
		return this.datetime;
	}

	/**
	 * Getter for property hostName.
	 * 
	 * @return Value of property hostName.
	 * 
	 */
	public String getHostName() {
		return this.hostName;
	}

	/**
	 * Getter for property message.
	 * 
	 * @return Value of property message.
	 * 
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Getter for property processName.
	 * 
	 * @return Value of property processName.
	 * 
	 */
	public String getProcessName() {
		return this.processName;
	}

	/**
	 * Getter for property severity.
	 * 
	 * @return Value of property severity.
	 * 
	 */
	public String getSeverity() {
		return this.severity;
	}

	/**
	 * Setter for property datetime.
	 * 
	 * @param datetime
	 *            New value of property datetime.
	 * 
	 */
	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	/**
	 * Setter for property hostName.
	 * 
	 * @param hostName
	 *            New value of property hostName.
	 * 
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	/**
	 * Setter for property message.
	 * 
	 * @param message
	 *            New value of property message.
	 * 
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Setter for property processName.
	 * 
	 * @param processName
	 *            New value of property processName.
	 * 
	 */
	public void setProcessName(String processName) {
		this.processName = processName;
	}

	/**
	 * Setter for property severity.
	 * 
	 * @param severity
	 *            New value of property severity.
	 * 
	 */
	public void setSeverity(String severity) {
		this.severity = severity;
	}

}
