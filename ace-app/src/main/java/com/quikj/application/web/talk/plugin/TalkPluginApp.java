package com.quikj.application.web.talk.plugin;

import java.io.IOException;
import java.util.Properties;

import com.quikj.application.web.talk.plugin.accounting.CDRHandler;
import com.quikj.server.app.ApplicationConfiguration;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.app.PluginAppClientInterface;
import com.quikj.server.app.PluginAppInterface;
import com.quikj.server.framework.AceException;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMailService;
import com.quikj.server.framework.AceNetworkAccess;

public class TalkPluginApp implements PluginAppInterface {
	private static TalkPluginApp instance = null;

	private ServiceController controller = null;

	private CDRHandler cdrHandler = null;

	private OPMHandler opmHandler = null;

	private AceMailService mailService = null;

	private boolean cdrRequired = false;

	private boolean opmRequired = false;

	private AceNetworkAccess accessInfo = null;

	public TalkPluginApp() {
		instance = this;
	}

	public static TalkPluginApp Instance() {
		return instance;
	}

	@Override
	public void applicationInit(Properties properties) throws AceException {
		String cdr_required_s = properties.getProperty("cdr-required");
		if (cdr_required_s != null) {
			if (cdr_required_s.equals("yes")) {
				cdrRequired = true;
				opmRequired = true;
			} else if (cdr_required_s.equals("no")) {
				cdrRequired = false;
				opmRequired = false;
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
								"TalkPluginApp.applicationInit() -- cdr-required contains invalid value.");

				cleanup();
				throw new AceException("Failed to initialization application");
			}
		}

		if (cdrRequired) {
			String backup_cdr_dir = properties.getProperty("backup-cdr-dir");
			if (backup_cdr_dir == null) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
								"TalkPluginApp.applicationInit() -- backup CDR directory not specified");

				cleanup();
				throw new AceException("Failed to initialization application");
			}

			String backup_cdr_file = properties.getProperty("backup-cdr-file");
			if (backup_cdr_file == null) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
								"TalkPluginApp.applicationInit() -- backup CDR file not specified");

				cleanup();
				throw new AceException("Failed to initialization application");
			}

			try {
				cdrHandler = new CDRHandler(backup_cdr_dir, backup_cdr_file);
				cdrHandler.start();
			} catch (IOException ex) {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								"TalkPluginApp.applicationInit() -- CDR handler returned IO Exception:  "
										+ ex.getMessage()
										+ " probably because the specified backup directory does not exist. "
										+ "No CDRs will be recorded", ex);
			}
		}

		if (opmRequired) {
			try {
				opmHandler = new OPMHandler();
				opmHandler.start();
			} catch (IOException ex) {
				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"TalkPluginApp.applicationInit() -- OPM handler returned IO Exception:  "
								+ ex.getMessage()
								+ ". No OPMs will be recorded", ex);
			}
		}

		ApplicationConfiguration appConfig = ApplicationServer.getInstance()
				.getBean(ApplicationConfiguration.class);
		mailService = new AceMailService(appConfig.getMailHost(), appConfig.getMailPort(),
				appConfig.isMailEncrypt(), appConfig.isMailDebug(),
				appConfig.getMailUserName(), appConfig.getMailPassword(),
				appConfig.getMailPendingDir(), appConfig.getMailPendingFile(),
				appConfig.getMailOverrideFrom());
		mailService.start();

		try {
			controller = new ServiceController();
			controller.start();
		} catch (IOException ex) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"TalkPluginApp.applicationInit() -- An IO error occured while trying to create the Service Controller thread - "
									+ ex.getMessage(), ex);

			cleanup();
			throw new AceException(ex);
		} catch (AceException ex) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"TalkPluginApp.applicationInit() -- An error occured while trying to create the Service Controller thread - "
									+ ex.getMessage(), ex);

			cleanup();
			throw ex;
		}

		SynchronousDbOperations.getInstance();
	}

	public void cleanup() {
		if (controller != null) {
			controller.dispose();
			controller = null;
		}

		if (cdrHandler != null) {
			cdrHandler.dispose();
			cdrHandler = null;
		}

		if (opmHandler != null) {
			opmHandler.dispose();
			opmHandler = null;
		}

		if (mailService != null) {
			mailService.dispose();
			mailService = null;
		}

		SynchronousDbOperations.getInstance().dispose();

		instance = null;
	}

	@Override
	public void dispose() {
		cleanup();
	}

	public AceNetworkAccess getAccessInfo() {
		return accessInfo;
	}

	public String getApplicationDescription() {
		return "Web-based instant messaging application";
	}

	public String getApplicationName() {
		return "TALK";
	}

	public boolean isCdrRequired() {
		return cdrRequired;
	}

	public boolean isOpmRequired() {
		return opmRequired;
	}

	@Override
	public PluginAppClientInterface newEndpointInstance() throws AceException {
		return new TalkEndpoint();
	}
}
