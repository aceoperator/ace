package com.quikj.application.web.talk.plugin;

import java.util.List;

import com.quikj.ace.messages.vo.talk.SetupResponseMessage;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.SQLParam;

public class DbUnavailableUserTransfer implements DbOperationInterface {
	private String username;

	private SessionInfo session;

	private EndPointInterface activeCalledParty;

	private SetupResponseMessage originalSetupRespMessage;

	private int originalSetupRespStatus;

	private String originalSetupRespReason;

	private long operationId;
	private String lastError = "";
	private ServiceController parent;
	private AceSQL database;

	public DbUnavailableUserTransfer(String username, SessionInfo session,
			EndPointInterface active_called_party,
			SetupResponseMessage original_setupresp_message,
			int original_setupresp_status, String original_setupresp_reason,
			ServiceController parent, AceSQL database) {
		this.parent = parent;
		this.database = database;
		this.username = username;
		this.session = session;
		activeCalledParty = active_called_party;
		originalSetupRespMessage = original_setupresp_message;
		originalSetupRespStatus = original_setupresp_status;
		originalSetupRespReason = original_setupresp_reason;
	}

	public void cancel() {
		database.cancelSQL(operationId, parent);
	}

	public boolean checkForTransfer() {
		SQLParam[] statements = UserTable
				.getTransferInfoQueryStatement(username);
		operationId = database.executeSQL(parent, this, statements);

		if (operationId == -1L) {
			lastError = parent.getErrorMessage();
			return false;
		}

		return true;
	}

	public EndPointInterface getEndPoint() {
		return session.getCallingEndPoint();
	}

	public String getLastError() {
		return lastError;
	}

	public boolean processResponse(AceSQLMessage message) // returns done or not
	{
		if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbUnavailableUserTransfer.processResponse() -- Database error result querying user name "
									+ username + ".");

			// send response to the client
			parent.finishSetupResponse(originalSetupRespMessage,
					originalSetupRespStatus, originalSetupRespReason,
					activeCalledParty);

			return true;
		}

		List<Object> results = message.getResults();

		if (results.size() == 0 || results.get(0) == null) {
			// no transfer to number provisioned

			// send response to the client
			parent.finishSetupResponse(originalSetupRespMessage,
					originalSetupRespStatus, originalSetupRespReason,
					activeCalledParty);

			return true;
		}

		UserElement userData = (UserElement) results.get(0);
		parent.transferUserUnavailableCall(session, userData,
				activeCalledParty, username);
		return true;
	}
}
