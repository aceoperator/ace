/**
 * 
 */
package com.quikj.ace.custom.server;

import java.io.FileInputStream;
import java.util.Properties;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.AceTimer;

/**
 * @author amit
 * 
 */
public class AceContextListener implements ServletContextListener {

	public AceContextListener() {
	}

	@Override
	public void contextDestroyed(ServletContextEvent ev) {
		AceMailService.getInstance().dispose();
		AceTimer.Instance().dispose();
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
		}

		AceLogger.Instance().dispose();
	}

	@Override
	public void contextInitialized(ServletContextEvent ev) {
		try {
			FileInputStream fis = new FileInputStream(
					AceConfigFileHelper.getAcePath("config/custom",
							"custom_cfg.properties"));
			Properties logProperties = new Properties();
			logProperties.load(fis);
			fis.close();

			new AceLogger(logProperties.getProperty(
					"com.quikj.ace.custom.log.processName", "CUSTOM"),
					Integer.parseInt(logProperties.getProperty(
							"com.quikj.ace.custom.log.logGroup", "1"))).start();

			AceLogger.Instance().log(AceLogger.INFORMATIONAL,
					AceLogger.SYSTEM_LOG, "Timer service started");
			new AceTimer().start();

			AceLogger.Instance().log(AceLogger.INFORMATIONAL,
					AceLogger.SYSTEM_LOG, "Mail service started");

			fis = new FileInputStream(AceConfigFileHelper.getAcePath(
					"config/properties", "mail.properties"));
			Properties mailProperties = new Properties();
			mailProperties.load(fis);
			fis.close();

			new AceMailService(
					mailProperties.getProperty("com.quikj.ace.mailHost",
							"localhost"),
					Integer.parseInt(mailProperties.getProperty(
							"com.quikj.ace.mailPort", "25")),
					Boolean.parseBoolean(mailProperties.getProperty(
							"com.quikj.ace.mailEncrypt", "false")),
					Boolean.parseBoolean(mailProperties.getProperty(
							"com.quikj.ace.mailDebug", "false")),
					mailProperties.getProperty("com.quikj.ace.mailUserName"),
					mailProperties.getProperty("com.quikj.ace.mailPassword"),
					mailProperties.getProperty("com.quikj.ace.mailPendingDir"),
					mailProperties.getProperty("com.quikj.ace.mailPendingFile"),
					mailProperties
							.getProperty("com.quikj.ace.mailOverrideFrom"))
					.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
