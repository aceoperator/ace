/*
 * HTTPSRMIInterface.java
 *
 * Created on May 26, 2003, 12:12 PM
 */

package com.quikj.client.raccess;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * 
 * @author amit
 */
public interface AceRMIInterface extends Remote {
	public String getParam(String object, String param) throws RemoteException;

	public boolean setParam(String object, String param, String value)
			throws RemoteException;
}
