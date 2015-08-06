/*
 * AceLoggerInterface.java
 *
 * Created on March 7, 2003, 10:47 PM
 */

/**
 *
 * @author  Amit
 */

//////////////////////////////////////////////////
//      Packages
//////////////////////////////////////////////////
package com.quikj.server.framework;

public interface AceLoggerInterface {
	public boolean log(int severity, int msg_type, String message);

	public boolean log(int severity, int msg_type, String message,
			String processName);

	public boolean log(int severity, int msg_type, String message, Throwable e);
}
