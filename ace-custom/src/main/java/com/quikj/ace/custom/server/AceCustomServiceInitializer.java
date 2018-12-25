/**
 * 
 */
package com.quikj.ace.custom.server;

import java.io.IOException;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;

import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.AceTimer;
import com.quikj.server.framework.CaptchaService;

/**
 * @author amit
 * 
 */
public class AceCustomServiceInitializer {

	private String mailHost = "localhost";

	private int mailPort = 25;

	private boolean mailEncrypt = false;

	private boolean mailDebug = false;

	private String mailUserName;

	private String mailPassword;

	private String mailPendingDir;

	private String mailPendingFile;

	private String mailOverrideFrom;

	private String captchaSecret;

	private String processName;

	private int processGroup;
	
	@PreDestroy
	public void destroy() {
		AceMailService.getInstance().dispose();
		AceTimer.Instance().dispose();
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
		}

		AceLogger.Instance().dispose();
	}

	@PostConstruct
	public void init() throws UnknownHostException, IOException, ParserConfigurationException, AceException {
		new AceLogger(processName, processGroup).start();

		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG, "Timer service started");
		new AceTimer().start();

		new AceMailService(mailHost, mailPort, mailEncrypt, mailDebug, mailUserName, mailPassword, mailPendingDir,
				mailPendingFile, mailOverrideFrom).start();
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG, "Mail service started");

		CaptchaService captchaService = new CaptchaService();
		if (captchaSecret != null && !captchaSecret.trim().isEmpty()) {
			captchaService.setSecretKey(captchaSecret);
			AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG, "Captcha service initialized");
		}
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public void setMailPort(int mailPort) {
		this.mailPort = mailPort;
	}

	public void setMailEncrypt(boolean mailEncrypt) {
		this.mailEncrypt = mailEncrypt;
	}

	public void setMailDebug(boolean mailDebug) {
		this.mailDebug = mailDebug;
	}

	public void setMailUserName(String mailUserName) {
		this.mailUserName = mailUserName;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public void setMailPendingDir(String mailPendingDir) {
		this.mailPendingDir = mailPendingDir;
	}

	public void setMailPendingFile(String mailPendingFile) {
		this.mailPendingFile = mailPendingFile;
	}

	public void setMailOverrideFrom(String mailOverrideFrom) {
		this.mailOverrideFrom = mailOverrideFrom;
	}

	public void setCaptchaSecret(String captchaSecret) {
		this.captchaSecret = captchaSecret;
	}

	public void setProcessGroup(int processGroup) {
		this.processGroup = processGroup;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}	
}
