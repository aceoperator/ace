package com.quikj.server.app;

import java.util.Enumeration;
import java.util.Hashtable;

import com.quikj.server.framework.AceException;

public class PluginAppList {
	private Hashtable<Integer, PluginApplicationInfo> appList = new Hashtable<Integer, PluginApplicationInfo>();

	private static PluginAppList instance = null;

	private PluginAppList() {
	}

	public static PluginAppList Instance() {
		if (instance == null) {
			instance = new PluginAppList();
		}

		return instance;
	}

	public synchronized void add(PluginApplicationInfo plugin) throws AceException {
		try {
			// check if the application already exists in the list
			if (appList.get(plugin.getId()) != null) {
				throw new AceException("Could not add the application " + plugin.getClazz()
						+ " to the list because the application id " + plugin.getId()
						+ " is duplicate");
			}

			Class<?> appClass = Class.forName(plugin.getClazz());
			
			if (!PluginAppInterface.class.isAssignableFrom(appClass)) {
				throw new AceException("Class "
						+ plugin.getClazz()
						+ " does not implement the com.ace.server.app.PluginAppInterface");
			}

			// get a new instance of this class
			PluginAppInterface obj = (PluginAppInterface) appClass.newInstance();
			plugin.setObj(obj);

			// finally, add the object to the list
			appList.put(plugin.getId(), plugin);
		} catch (AceException e) {
			throw e;
		} catch (Exception e) {
			throw new AceException(e);
		}
	}

	public synchronized boolean delete(int id) {
		PluginApplicationInfo obj = appList
				.get(id);

		if (obj == null) {
			return false;
		}

		appList.remove(id);

		// call dispose
		obj.getObj().dispose();

		return true;
	}

	public void initApplication(int id) throws AceException {
		PluginApplicationInfo obj = (PluginApplicationInfo) appList
				.get(id);

		if (obj == null) {
			throw new AceException("Application Id: " + id + " not found in list");
		}

		obj.getObj().applicationInit(obj.getProperties());
	}
	
	public synchronized int[] listApplications() {
		int[] list = new int[appList.size()];

		Enumeration<Integer> e = appList.keys();
		int i = 0;
		while (e.hasMoreElements()) {
			list[i++] = ((Integer) e.nextElement()).intValue();
		}

		return list;
	}
	
	public synchronized PluginAppClientInterface newInstance(int id) throws AceException {
		PluginApplicationInfo obj = (PluginApplicationInfo) appList
				.get(id);

		if (obj == null) {
			throw new AceException("Application Id: " + id + " not found in list");
		}

		return obj.getObj().newEndpointInstance();
	}
}
