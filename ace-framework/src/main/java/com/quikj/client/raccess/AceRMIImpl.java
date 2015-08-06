/*
 * HTTPSRemoteService.java
 *
 * Created on May 26, 2003, 12:21 PM
 */

package com.quikj.client.raccess;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * 
 * @author amit
 */

public class AceRMIImpl extends UnicastRemoteObject implements AceRMIInterface {
	private HashMap map;
	private static AceRMIImpl instance = null;

	/** Creates a new instance of HTTPSRemoteService */
	public AceRMIImpl() throws RemoteException {
		super();

		map = new HashMap();
		instance = this;
	}

	public static AceRMIImpl getInstance() {
		return instance;
	}

	public String getParam(String object, String param) throws RemoteException {
		RemoteServiceInterface obj = (RemoteServiceInterface) map.get(object);
		if (obj != null) {
			return obj.getRMIParam(param);
		}
		return null;
	}

	public void registerService(String name, RemoteServiceInterface obj) {
		map.put(name, obj);
	}

	public boolean setParam(String object, String param, String value)
			throws RemoteException {
		RemoteServiceInterface obj = (RemoteServiceInterface) map.get(object);
		if (obj != null) {
			return obj.setRMIParam(param, value);
		}
		return false;
	}

	public void unregisterService(String name) {
		map.remove(name);
	}
}
