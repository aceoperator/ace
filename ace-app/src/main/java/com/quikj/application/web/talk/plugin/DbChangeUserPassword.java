package com.quikj.application.web.talk.plugin;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.ChangePasswordResponseMessage;
import com.quikj.ace.messages.vo.talk.TalkMessageInterface;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.SQLParam;

public class DbChangeUserPassword implements DbOperationInterface {
	private String userName;

	private String oldPassword;

	private String newPassword;

	private EndPointInterface endpoint;

	private ServiceController parent;

	private AceSQL database;

	private int requestId;

	private Object userParm;
	private long operationId;
	private String lastError = "";

	public DbChangeUserPassword(String username, String old_password,
			String new_password, EndPointInterface endpoint,
			ServiceController parent, AceSQL database, int request_id,
			Object user_parm) {
		userName = username;
		oldPassword = old_password;
		newPassword = new_password;
		this.endpoint = endpoint;
		this.parent = parent;
		this.database = database;
		requestId = request_id;
		userParm = user_parm;
	}

	public void cancel() {
		database.cancelSQL(operationId, parent);
	}

	public EndPointInterface getEndPoint() {
		return endpoint;
	}

	public String getLastError() {
		return lastError;
	}

	public boolean initiate() {
			SQLParam[] statements = UserTable.getChangePasswordStatement(userName, oldPassword,
					newPassword);
			operationId = database.executeSQL(parent, this, statements);
			if (operationId == -1L) {
				lastError = parent.getErrorMessage();
				return false;
			}

			return true;
	}

	public boolean processResponse(AceSQLMessage message) // returns done or not
	{
		if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbChangeUserPassword.processResponse() -- Database error result.");

			if (sendResponse(
					ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) endpoint
									.getParam("language"))).getString(
							"Database_error"),
					new ChangePasswordResponseMessage()) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								parent.getName()
										+ "- DbChangeUserPassword.processResponse() -- Could not send response message to the endpoint "
										+ endpoint);
			}

			return true;
		}

		if (message.getAffectedRows() != 1) {
			if (sendResponse(
					ResponseMessage.NOT_MODIFIED,
					java.util.ResourceBundle.getBundle(
							"com.quikj.application.web.talk.plugin.language",
							ServiceController.getLocale((String) endpoint
									.getParam("language"))).getString(
							"Password_not_modified"),
					new ChangePasswordResponseMessage()) == false) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								parent.getName()
										+ "- DbChangeUserPassword.processResponse() -- Could not send response message to the endpoint "
										+ endpoint);
			}

			return true;
		}

		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
				parent.getName() + "- Password changed for user " + userName);

		if (sendResponse(ResponseMessage.OK, "OK",
				new ChangePasswordResponseMessage()) == false) {
			// print error message
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbChangeUserPassword.processResponse() -- Could not send response message to the endpoint "
									+ endpoint);
		}

		EndPointInfo endpointInfo = RegisteredEndPointList.Instance()
				.findRegisteredEndPointInfo(endpoint);
		if (endpointInfo == null) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbChangeUserPassword.processResponse() -- Could not find endpoint "
									+ endpoint);
		}

		if (endpointInfo.getUserData().isChangePassword()) {
			endpointInfo.getUserData().setChangePassword(false);
			parent.groupNotifyOfAvailabilityChange(endpointInfo.getName(), true);
		}
		
		return true;
	}

	public boolean sendResponse(int response_status, String reason,
			TalkMessageInterface message) {
		if (endpoint.sendEvent(new MessageEvent(
				MessageEvent.CLIENT_RESPONSE_MESSAGE, null, response_status,
				reason, message, userParm, requestId)) == false) {
			return false;
		}

		return true;
	}
}
