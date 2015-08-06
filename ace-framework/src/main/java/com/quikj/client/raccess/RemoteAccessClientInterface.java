package com.quikj.client.raccess;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public interface RemoteAccessClientInterface {

	public AceRMIInterface getRemoteAccess() throws NotBoundException,
			MalformedURLException, RemoteException;

}