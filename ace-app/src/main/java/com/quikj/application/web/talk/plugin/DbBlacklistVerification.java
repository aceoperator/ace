package com.quikj.application.web.talk.plugin;

import java.util.List;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.SetupRequestMessage;
import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.SQLParam;

public class DbBlacklistVerification implements DbOperationInterface {

	private EndPointInfo endpointInfo = new EndPointInfo();

	private long operationId;

	private String lastError = "";

	private ServiceController parent;

	private AceSQL database;

	private SetupRequestMessage setup;

	public DbBlacklistVerification(EndPointInterface endpoint,
			SetupRequestMessage setup, ServiceController parent, AceSQL database) {
		this.parent = parent;
		this.database = database;
		this.setup = setup;

		endpointInfo.setEndPoint(endpoint);
	}

	public void cancel() {
		database.cancelSQL(operationId, parent);
	}

	public EndPointInterface getEndPoint() {
		return endpointInfo.getEndPoint();
	}

	public String getLastError() {
		return lastError;
	}

	public boolean processResponse(AceSQLMessage message) {
		EndPointInterface endpoint = endpointInfo.getEndPoint();

		if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
			parent.adjustFailedTransferCallCount(setup);
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbBlacklistVerification.processResponse() -- Database error while checking the black list for user "
									+ setup.getCalledNameElement()
											.getCallParty().getName() + ".");
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(setup.getSessionId());
			parent.sendSetupResponse(
					endpoint,
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) endpoint
									.getParam("language"))).getString(
							"Database_error"), response, null);
			return true;
		}

		List<Object> results = message.getResults();
		if (results != null && results.size() > 0 && results.get(0) != null) {
			// If a result has been returned, the user is in the blacklist
			parent.adjustFailedTransferCallCount(setup);
			SetupResponseMessage response = new SetupResponseMessage();
			response.setSessionId(setup.getSessionId());
			parent.sendSetupResponse(
					endpoint,
					SetupResponseMessage.FORBIDDEN,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) endpoint
									.getParam("language"))).getString(
							"Chat_request_denied"), response, null);

			AceLogger
					.Instance()
					.log(AceLogger.INFORMATIONAL,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbBlacklistVerification.checkBlacklist() -- Blacklisted user with identifier : "
									+ results.get(0)
									+ " attempted to access the system. The access was denied");

			return true;
		}

		parent.finishChatSetup(setup, endpoint);
		return true;
	}

	public boolean checkBlacklist() {
		SQLParam[] statements = UserTable.getCookieIdentifierStatement(setup
				.getCalledNameElement().getCallParty().getName(), setup
				.getCallingNameElement().getCallParty().getEndUserCookie(),
				setup.getCallingNameElement().getCallParty().getIpAddress());
		operationId = database.executeSQL(parent, this, statements);
		if (operationId == -1L) {
			lastError = parent.getErrorMessage();
			return false;
		}

		return true;
	}
}
