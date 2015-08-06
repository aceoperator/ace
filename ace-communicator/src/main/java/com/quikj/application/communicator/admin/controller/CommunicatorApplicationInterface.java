/*
 * CommunicatorApplicationInterface.java
 *
 * Created on April 17, 2003, 7:21 AM
 */

package com.quikj.application.communicator.admin.controller;

/**
 * 
 * @author Vinod Batra
 */
public interface CommunicatorApplicationInterface {

	public abstract void destroy();

	public abstract void init();

	public abstract void setParam(String name, String value);
}
