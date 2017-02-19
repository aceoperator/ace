/*
 * FeatureFactory.java
 *
 * Created on April 25, 2002, 2:52 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import com.quikj.client.raccess.AceRMIImpl;
import com.quikj.client.raccess.RemoteServiceInterface;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author amit
 */
public class FeatureFactory implements RemoteServiceInterface {
	private static FeatureFactory instance = null;

	private String errorMessage = "";

	private HashMap<String, FeatureElement> featureList = new HashMap<String, FeatureElement>(); 

	private static HashMap instantiatedClassList = new HashMap();

	public FeatureFactory() {
		setInstance(this);
	}

	public static FeatureFactory getInstance() {
		return instance;
	}

	public synchronized boolean activateFeature(String name) {
		FeatureElement feature = getActiveFeatureInfo(name);

		if (feature == null) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"FeatureFactory.activateFeature() -- Feature " + name
							+ " not started - " + getErrorMessage());

			return false;
		}

		try {
			// check if it is already running, for whatever reason...

			FeatureElement old = (FeatureElement) featureList.get(name);
			if (old != null) {
				featureList.remove(name);

				FeatureInterface obj = old.getObj();
				if (obj != null) {
					obj.dispose();

					AceLogger
							.Instance()
							.log(AceLogger.WARNING,
									AceLogger.SYSTEM_LOG,
									"Feature "
											+ name
											+ " being activated, but the feature was already running. Restarting the feature.");
				}
			}

			// start the feature

			featureList.put(new String(feature.getName()), feature);

			if (startFeature(feature) == false) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"FeatureFactory.activateFeature() -- Feature "
								+ feature.getName() + " not started - "
								+ getErrorMessage());

				featureList.remove(feature.getName());
			} else {
				AceLogger.Instance().log(AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						"Feature " + feature.getName() + " started");

				return true;
			}
		} catch (Exception ex) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"FeatureFactory.activateFeature() -- "
							+ ex.getClass().getName()
							+ " occurred while creating feature: "
							+ feature.getName());

			featureList.remove(feature.getName());
		}

		return false;
	}

	public synchronized boolean deactivateFeature(String name) {
		FeatureElement feature = (FeatureElement) featureList.get(name);
		if (feature != null) {
			featureList.remove(name);

			FeatureInterface obj = feature.getObj();
			if (obj != null) {
				obj.dispose();

				AceLogger.Instance().log(AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						"Feature " + feature.getName() + " stopped");

				return true;
			}
		}

		return true;
	}

	public void dispose() {
		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) { // if remote service has been started
			rs.unregisterService("com.quikj.application.web.talk.plugin.FeatureFactory");
		}

		Collection<FeatureElement> flist = featureList.values();
		for (FeatureElement f: flist) {
			f.getObj().dispose();
		}
		flist.clear();
		instantiatedClassList.clear();		

		setInstance(null);
	}

	private FeatureElement getActiveFeatureInfo(String name) {
		FeatureElement feature = null;
		FeatureTable tbl = new FeatureTable();		
		FeatureTableElement data = tbl.query(name);

		if (data == null) {
			if (tbl.getErrorMessage() == null) {
				errorMessage = "Feature name not found in database";
			} else {
				errorMessage = tbl.getErrorMessage();
			}
		} else {
			if (data.isActive()) {
				feature = new FeatureElement();
				feature.setName(data.getName());
				feature.setClassName(data.getClassName());
				feature.setMap(data.getParams());
			} else {
				errorMessage = "Feature information in database indicates feature not active";
			}
		}
		
		return feature;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String[] getFeatureNames() {
		String[] names = new String[featureList.size()];
		return ((String[]) (featureList.keySet().toArray(names)));
	}

	public String getRMIParam(String param) {
		return null;
	}

	public boolean init() {
		// Read all feature info from the database, build the featureList
		
		FeatureTable tbl = new FeatureTable();
		ArrayList list = tbl.queryAll();
		if (list == null) {
			errorMessage = "Error reading feature information from the database : "
					+ tbl.getErrorMessage();
			return false;
		}

		for (Iterator i = list.iterator(); i.hasNext();) {
			FeatureTableElement data = (FeatureTableElement) i.next();

			if (data.isActive()) {
				FeatureElement feature = new FeatureElement();
				feature.setName(data.getName());
				feature.setClassName(data.getClassName());
				feature.setMap(data.getParams());

				featureList.put(new String(feature.getName()), feature);
			}
		}
		return true;
	}

	public boolean isFeature(String name) {
		return featureList.containsKey(name);
	}

	public int numFeatures() {
		return featureList.size();
	}

	private void setInstance(FeatureFactory instance) {
		FeatureFactory.instance = instance;
	}

	public boolean setRMIParam(String param, String value) {
		// param = "feature:xxxx" where xxxx is the feature name
		// value = "activate", "deactivate", or "synch"

		String str = "feature:";
		if (param.startsWith(str)) {
			String name = param.substring(str.length());
			if (name.length() > 0) {
				if (value.equals("activate")) {
					return activateFeature(name);
				} else if (value.equals("deactivate")) {
					return deactivateFeature(name);
				} else if (value.equals("synch")) {
					return synchFeature(name);
				}
			}
		}

		AceLogger.Instance().log(
				AceLogger.ERROR,
				AceLogger.SYSTEM_LOG,
				"FeatureFactory.setRMIParam() -- Invalid param and/or value received. Param = "
						+ param + ", value = " + value);

		return false;
	}

	private boolean startFeature(FeatureElement element) {
		// check if the class exists in the instantiated class list
		Class app_class = (Class) instantiatedClassList.get(element
				.getClassName());
		if (app_class == null) { // does not exist
			try {
				app_class = Class.forName(element.getClassName());
			} catch (ClassNotFoundException ex) {
				errorMessage = "ClassNotFoundException : " + ex.getMessage();
				return false;
			}

			// check if the class implements the PluginAppInterface
			Class[] interfaces = app_class.getInterfaces();

			int found = 0;
			for (int i = 0; i < interfaces.length; i++) {
				if (interfaces[i]
						.getName()
						.equals("com.quikj.application.web.talk.plugin.FeatureInterface") == true) {
					found++;
				} else if (interfaces[i].getName().equals(
						"com.quikj.server.app.EndPointInterface") == true) {
					found++;
				}
			}

			if (found < 2) {
				errorMessage = "Class " + element.getClassName()
						+ " does not implement the necessary interfaces";
				return false;
			}

			instantiatedClassList.put(element.getClassName(), app_class);
		}

		// get a new instance of this class
		FeatureInterface obj = null;
		try {
			obj = (FeatureInterface) app_class.newInstance();
		} catch (InstantiationException ex1) {
			errorMessage = "InstantiationException : " + ex1.getMessage();
			return false;
		} catch (IllegalAccessException ex2) {
			errorMessage = "IllegalAccessException : " + ex2.getMessage();
			return false;
		}

		if (obj.init(element.getName(), element.getMap()) == false) {
			errorMessage = "Class " + element.getClassName()
					+ " returned error during init";
			return false;
		}

		// start the feature
		obj.start();
		element.setObj(obj);

		return true;
	}

	public void startUp() {
		// register for RMI

		AceRMIImpl rs = AceRMIImpl.getInstance();
		if (rs != null) // if remote service has been started
		{
			rs.registerService(
					"com.quikj.application.web.talk.plugin.FeatureFactory",
					getInstance());
		}

		// start all features

		Collection features = featureList.values();
		for (Iterator i = features.iterator(); i.hasNext();) {
			FeatureElement feature = (FeatureElement) i.next();
			try {
				if (startFeature(feature) == false) {
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"FeatureFactory.startUp() -- Feature "
									+ feature.getName() + " not started - "
									+ getErrorMessage());
				} else {
					AceLogger.Instance().log(AceLogger.INFORMATIONAL,
							AceLogger.SYSTEM_LOG,
							"Feature " + feature.getName() + " started");
				}
			} catch (Exception ex) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"FeatureFactory.startUp() -- "
								+ ex.getClass().getName()
								+ " occurred while creating feature: "
								+ feature.getName());
			}
		}
	}

	public synchronized boolean synchFeature(String name) {
		FeatureElement currentFeature = (FeatureElement) featureList.get(name);
		if (currentFeature != null) {
			FeatureInterface obj = currentFeature.getObj();
			if (obj != null) {
				// the feature is currently running
				FeatureElement newFeatureInfo = getActiveFeatureInfo(name);

				if (newFeatureInfo == null) {
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"FeatureFactory.synchFeature() -- Feature " + name
									+ " not updated - " + getErrorMessage());

					return false;
				}

				obj.resynchParam(newFeatureInfo.getMap());
				currentFeature.setMap(newFeatureInfo.getMap());

				AceLogger.Instance().log(AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						"Feature " + name + " parameters re-initialized");

				return true;
			}
		}

		AceLogger
				.Instance()
				.log(AceLogger.WARNING,
						AceLogger.SYSTEM_LOG,
						"Feature "
								+ name
								+ " being updated (synched), but the feature isn't running. Attempting to start the feature with the new data.");

		return activateFeature(name);
	}

}
