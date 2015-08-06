/*
 * AceAppServerCom.java
 *
 * Created on May 29, 2003, 1:16 PM
 */

package com.quikj.client.raccess;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * 
 * @author amit
 */
public class RemoteAccessClient implements RemoteAccessClientInterface {

	private String lookupName;

	public RemoteAccessClient(String host, String service_name) {
		this("rmi://localhost:1099", service_name, host);
	}

	public RemoteAccessClient(String registry_url, String service_name,
			String host) {
		if (registry_url.endsWith("/") == false) {
			registry_url += "/";
		}

		if (service_name.endsWith("/") == false) {
			service_name += "/";
		}

		lookupName = registry_url + service_name + host;
		// System.out.println("Lookup: " + lookupName);
	}

	@Override
	public AceRMIInterface getRemoteAccess() throws NotBoundException,
			MalformedURLException, RemoteException {
		return (AceRMIInterface) Naming.lookup(lookupName);
	}
}
