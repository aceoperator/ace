/*
 * RemoteServiceInterface.java
 *
 * Created on May 26, 2003, 12:33 PM
 */

package com.quikj.client.raccess;

/**
 * 
 * @author amit
 */
public interface RemoteServiceInterface {
	public String getRMIParam(String param);

	public boolean setRMIParam(String param, String value);
}
