/*
 * AceNetworkAccess.java
 *
 * Created on July 5, 2002, 3:43 AM
 */

package com.quikj.server.framework;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * 
 * @author amit
 */
public class AceNetworkAccess {
	private HashMap list = new HashMap();
	private InetAddress[] networkList = new InetAddress[0];
	private InetAddress[] netmaskList = new InetAddress[0];

	/** Creates a new instance of AceNetworkAccess */
	public AceNetworkAccess() {
	}

	public boolean add(String network, String netmask) {
		try {
			list.put(InetAddress.getByName(network), InetAddress
					.getByName(netmask));

			// re-do the lists
			Set network_list = list.keySet();
			networkList = new InetAddress[network_list.size()];
			networkList = (InetAddress[]) network_list.toArray(networkList);

			Collection netmask_list = list.values();
			netmaskList = new InetAddress[netmask_list.size()];
			netmaskList = (InetAddress[]) netmask_list.toArray(netmaskList);

			return true;
		} catch (UnknownHostException ex) {
			return false;
		}
	}

	private byte[] byteArrayAnd(byte[] a, byte[] b) {
		byte[] c = new byte[a.length];

		for (int i = 0; i < a.length; i++) {
			c[i] = (byte) ((a[i]) & (b[i]));
		}

		return c;
	}

	private boolean byteArrayEquals(byte[] a, byte[] b) {
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i])
				return false;
		}

		return true;
	}

	public boolean match(String host) {
		try {
			byte[] addr = InetAddress.getByName(host).getAddress();

			for (int i = 0; i < networkList.length; i++) {
				byte[] anded = byteArrayAnd(addr, netmaskList[i].getAddress());
				if (byteArrayEquals(anded, networkList[i].getAddress()) == true) {
					return true;
				}
			}
		} catch (UnknownHostException ex) {
			return false;
		}

		return false;
	}
}
