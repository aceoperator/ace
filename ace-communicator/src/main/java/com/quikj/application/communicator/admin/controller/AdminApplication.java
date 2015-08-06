/*
 * AdminApplication.java
 *
 * Created on April 13, 2003, 3:58 PM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.Map.Entry;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.PlugIn;
import org.apache.struts.config.ModuleConfig;

import com.quikj.client.raccess.RemoteAccessClient;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceNetworkAccess;
import com.quikj.server.framework.AceTimer;

/**
 * 
 * @author bhm
 */
public class AdminApplication implements PlugIn {

	private AdminConfig configuration;
	private String errMessage;

	public AdminApplication() {
	}

	public void destroy() {
		try {

			if (AceTimer.Instance() != null) {
				AceTimer.Instance().dispose();
			}

			if (AceLogger.Instance() != null) {
				AceLogger.Instance().dispose();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(ActionServlet actionServlet, ModuleConfig moduleConfig)
			throws javax.servlet.ServletException {
		// Init Spring beans
		SpringUtils.initSpringContext(actionServlet.getServletContext());

		configuration = SpringUtils.getBean(actionServlet.getServletContext(),
				AdminConfig.class);

		// save AdminConfig in the application scope
		actionServlet.getServletContext().setAttribute("adminConfig",
				configuration);

		if (configuration.getMenuProperties() != null) {
			// save MenuProperties in the application scope
			actionServlet.getServletContext().setAttribute("menuProperties",
					configuration.getMenuProperties());
		}

		// Start Ace Services
		try {
			new AceTimer().start();

			// start the log server
			new AceLogger(configuration.getProcessName(),
					configuration.getLogGroup());
			AceLogger.Instance().start();

			// store log severity level strings in application scope
			actionServlet.getServletContext().setAttribute(
					"logSeverityLevelStrings", AceLogger.SEVERITY_S);

		} catch (Exception ex) {
			throw new ServletException("System logger could not be started: "
					+ ex.getClass().getName() + ": " + ex.getMessage());
		}

		// load application classes and init them
		CommunicatorApplicationInterface application = null;
		for (ApplicationElement app : configuration.getApplications()) {
			if (app.getInitClass() != null) {
				application = loadClass(app.getInitClass());

				if (application == null) {
					throw new ServletException("Application class: "
							+ app.getInitClass() + " could not be loaded - "
							+ errMessage);
				}

				Properties params = app.getParams();

				for (Entry<Object, Object> entry : params.entrySet()) {
					String key = (String) entry.getKey();
					String value = (String) entry.getValue();
					application.setParam(key, value);
				}

				application.init();
			}
		}

		// create user access restriction list, if any
		AceNetworkAccess access_info = initUserAccessList();

		// save it in the application scope
		actionServlet.getServletContext().setAttribute("accessInfo",
				access_info);

		// create the remote access connection to the Ace application server
		RemoteAccessClient com = new RemoteAccessClient(
				configuration.getRemoteUrl(), configuration.getRemoteService(),
				configuration.getRemoteHost());

		// and save this to the application scope
		actionServlet.getServletContext().setAttribute("remoteAccess", com);

		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"Ace Communicator application started");
	}

	private AceNetworkAccess initUserAccessList() {
		AceNetworkAccess accessInfo = null;

		// if there is an access list, initialize it
		Properties params = configuration.getApplicationProperties();

		String value = params.getProperty("user-access");
		if (value != null) {
			StringTokenizer tokens = new StringTokenizer(value);
			if (tokens.countTokens() < 2) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"AdminApplication.initUserAccessList() -- Invalid value for user-access: "
								+ value + ", ignoring entry");
				return null;
			}

			boolean created = false;
			if (accessInfo == null) // one does not exist
			{
				accessInfo = new AceNetworkAccess();
				created = true;
			}

			if (!accessInfo.add(tokens.nextToken(), tokens.nextToken())) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"AdminApplication.initUserAccessList() -- Invalid IP addresses specified for user-access: "
										+ value + ", ignoring entry");

				if (created == true) // just created
				{
					accessInfo = null;
				}
			}

		}
		return accessInfo;
	}

	private CommunicatorApplicationInterface loadClass(String class_name) {
		Class<?> app_class;

		try {
			app_class = Class.forName(class_name);
		} catch (ClassNotFoundException ex) {
			errMessage = "Class " + class_name + " not found";
			return null;
		}

		// check if the class implements the CommunicatorApplicationInterface
		Class<?>[] interfaces = app_class.getInterfaces();

		boolean found = false;
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i]
					.getName()
					.equals("com.quikj.application.communicator.admin.controller.CommunicatorApplicationInterface") == true) {
				found = true;
				break;
			}
		}

		if (found == false) {
			errMessage = "Class "
					+ class_name
					+ " does not implement "
					+ "com.quikj.application.communicator.admin.controller.CommunicatorApplicationInterface";
			return null;
		}

		// get a new instance of this class
		CommunicatorApplicationInterface obj = null;
		try {
			obj = (CommunicatorApplicationInterface) app_class.newInstance();
		} catch (InstantiationException ex1) {
			errMessage = "InstantiationException : " + ex1.getMessage();
			return null;
		} catch (IllegalAccessException ex2) {
			errMessage = "IllegalAccessException : " + ex2.getMessage();
			return null;
		}

		return obj;
	}
}
