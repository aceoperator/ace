/**
 * 
 */
package com.quikj.ace.migration.server;

import java.net.InetAddress;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.quikj.client.raccess.RemoteAccessClient;
import com.quikj.server.framework.AceLogger;

/**
 * @author amit
 * 
 */
public class AceContextListener implements ServletContextListener {

	public AceContextListener() {
	}

	public void contextDestroyed(ServletContextEvent ev) {

		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"Ace Migration application shutdown");
		AceLogger.Instance().dispose();
	}

	public void contextInitialized(ServletContextEvent ev) {
		try {
			// set up logging
			new AceLogger("MIGRATION", 1).start();

			// create the remote access connection to the Ace application server
			RemoteAccessClient rmi = new RemoteAccessClient(
					"rmi://localhost:10999", "AceHTTPSRemoteAccess",
					InetAddress.getLocalHost().getHostName());

			// save it to the application scope
			ev.getServletContext().setAttribute("ace-migration.remoteAccess",
					rmi);

			AceLogger.Instance().log(AceLogger.INFORMATIONAL,
					AceLogger.SYSTEM_LOG, "Ace Migration application started");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
