package com.quikj.application.web.talk.plugin;

import java.util.HashMap;
import java.util.List;

import com.quikj.ace.messages.vo.app.ResponseMessage;
import com.quikj.ace.messages.vo.talk.CallPartyElement;
import com.quikj.ace.messages.vo.talk.GroupMemberElement;
import com.quikj.ace.messages.vo.talk.HtmlElement;
import com.quikj.ace.messages.vo.talk.MediaElements;
import com.quikj.ace.messages.vo.talk.RegistrationResponseMessage;
import com.quikj.application.web.talk.plugin.accounting.RegisteredUserLoginCDR;
import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceSQL;
import com.quikj.server.framework.AceSQLMessage;
import com.quikj.server.framework.SQLParam;

public class DbEndPointRegistration implements DbOperationInterface {
	private EndPointInfo endpointInfo = new EndPointInfo();

	private GroupInfo[] groupInfos = null;

	private String username;

	private String password;

	private long operationId;

	private String lastError = "";

	private ServiceController parent;

	private AceSQL database;

	public DbEndPointRegistration(EndPointInterface endpoint, String username, String password,
			ServiceController parent, AceSQL database) {
		this.parent = parent;
		this.database = database;
		endpointInfo.setEndPoint(endpoint);
		this.username = username;
		this.password = password;
	}

	public void cancel() {
		// System.out.println("DbEndPointRegistration.cancel() called");
		database.cancelSQL(operationId, parent);
	}

	private void finished(EndPointInterface endpoint) {
		if (!(endpoint instanceof FeatureInterface)) {
			// not a feature

			// if a gatekeeper is specified
			if (endpointInfo.getUserData().getGatekeeper() != null
					&& endpointInfo.getUserData().getGatekeeper().length() > 0) {
				// find the gatekeeper endpoint from the registered endpoint
				// list
				EndPointInterface gk = RegisteredEndPointList.Instance()
						.findRegisteredEndPoint(endpointInfo.getUserData().getGatekeeper());
				if (gk != null) // present
				{
					if (gk instanceof GatekeeperInterface) {
						boolean allow = ((GatekeeperInterface) gk).allow(endpoint, endpointInfo);
						if (!allow) {
							parent.sendRegistrationResponse(endpoint, ResponseMessage.FORBIDDEN,
									java.util.ResourceBundle
											.getBundle("com.quikj.application.web.talk.plugin.language",
													ServiceController.getLocale((String) endpoint.getParam("language")))
											.getString(
													"We_are_unabled_to_process_the_login_request_because_of_license_limitations"),
									null, null);

							return;
						}
					}
				}
			}
		}
		// Check if the user is already registered, in that case, drop the
		// previous session
		// and remove the previous session from the registered user list
		EndPointInterface oldEndpoint = RegisteredEndPointList.Instance()
				.findRegisteredEndPoint(endpointInfo.getName());
		if (oldEndpoint != null) {
			// send a messsage to the end-point to instruct it to drop-out
			if (!oldEndpoint.sendEvent(new DropEndpointEvent())) {
				// print an error message
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"DbEndPointRegistration.finished() (TALK) -- Could not send drop endpoint event to the endpoint "
								+ oldEndpoint);
			}

			// unregister the end-point
			ServiceController.Instance().unregisterUser(endpointInfo.getName());
		}

		if (!RegisteredEndPointList.Instance().addRegisteredEndPoint(endpointInfo)) {
			// print error message
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					parent.getName() + "- DbEndPointRegistration.finished() -- Could not add the endpoint "
							+ endpointInfo.getName() + " to the registered endpoint list");

			parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) endpoint.getParam("language")))
							.getString("Failed_to_add_to_the_registered_user_list"),
					null, null);

			return;
		}

		if (groupInfos != null) {
			for (int i = 0; i < groupInfos.length; i++) {
				GroupList.Instance().addGroup(groupInfos[i]);
			}
		}

		RegistrationResponseMessage response = new RegistrationResponseMessage();

		response.setLoginDate(new java.util.Date());
		HtmlElement text = new HtmlElement();
		text.setHtml(
				java.util.ResourceBundle
						.getBundle("com.quikj.application.web.talk.plugin.language",
								ServiceController.getLocale((String) endpoint.getParam("language")))
						.getString(
								"Hello_")
						+ ' ' + endpointInfo.getName()
						+ java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.plugin.language",
										ServiceController.getLocale((String) endpoint.getParam("language")))
								.getString(",_welcome_to_the_Talk_Instant_Messaging_Server"));

		MediaElements elements = new MediaElements();
		elements.getElements().add(text);
		response.setMediaElements(elements);

		CallPartyElement callParty = new CallPartyElement();
		callParty.setName(endpointInfo.getName());
		callParty.setFullName(endpointInfo.getUserData().getFullName());
		callParty.setEmail(endpointInfo.getUserData().getAddress());
		callParty.setComment(endpointInfo.getUserData().getAdditionalInfo());
		callParty.setAvatar(endpointInfo.getUserData().getAvatar());
		callParty.setLanguage((String) endpoint.getParam("language"));
		callParty.setChangePassword(endpointInfo.getUserData().isChangePassword());
		callParty.setPrivateInfo(endpointInfo.getUserData().isPrivateInfo());

		response.setCallPartyInfo(callParty);

		// set Group(s) members status info in message
		String[] groupMembers = RegisteredEndPointList.Instance().getActiveMembers(endpointInfo.getName());
		if (groupMembers != null) {
			com.quikj.ace.messages.vo.talk.GroupElement group = new com.quikj.ace.messages.vo.talk.GroupElement();
			for (String groupMember : groupMembers) {
				EndPointInfo info = RegisteredEndPointList.Instance().findRegisteredEndPointInfo(groupMember);
				if (info == null) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							parent.getName()
									+ "- DbEndPointRegistration.finished() -- Could not find EndPointInfo for group member "
									+ groupMember);

					continue;
				}

				GroupMemberElement element = new GroupMemberElement();
				element.setOperation(GroupMemberElement.OPERATION_ADD_LIST);
				element.setUser(groupMember);
				element.setFullName(info.getUserData().getFullName());
				element.setCallCount(info.getCallCount());
				element.setAvatar(info.getUserData().getAvatar());
				element.setDnd(info.isDnd());
				group.addElement(element);
			}

			response.setGroup(group);
		}

		// add group info
		com.quikj.ace.messages.vo.talk.GroupList groups = new com.quikj.ace.messages.vo.talk.GroupList();

		// next, add canned elements specific to the group(s) this guy
		// owns/belongs
		if (groupInfos != null) {
			for (int i = 0; i < groupInfos.length; i++) {
				GroupInfo group = groupInfos[i];
				groups.addElement(group.getName());
			}
		}

		if (groups.numElements() > 0) {
			response.setGroupList(groups);
		}

		if (!parent.sendRegistrationResponse(endpoint, ResponseMessage.OK, "OK", response, null)) {
			// remove from the list
			RegisteredEndPointList.Instance().removeRegisteredEndPoint(endpointInfo.getName());
			return;
		}

		// create a login CDR
		RegisteredUserLoginCDR cdr = new RegisteredUserLoginCDR(endpointInfo.getEndPoint().getIdentifier(),
				endpointInfo.getName());

		// send the CDR to the CDR processing thread
		ServiceController.Instance().sendCDR(cdr);

		if (!endpointInfo.getUserData().isChangePassword()) {
			// Do not notify others till the user has changed his/her password
			parent.groupNotifyOfAvailabilityChange(endpointInfo.getName(), true);
		}
	}

	public EndPointInterface getEndPoint() {
		return endpointInfo.getEndPoint();
	}

	public String getLastError() {
		return lastError;
	}

	public boolean processResponse(AceSQLMessage message) {
		// returns done or not
		EndPointInterface endpoint = endpointInfo.getEndPoint();

		if (endpointInfo.getUserData() == null) {
			// first time around, we're validating the user
			if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						parent.getName()
								+ "- DbEndPointRegistration.processResponse() -- Database error result authenticating user name "
								+ username + ".");

				// send a error response to the client
				parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR,
						java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.plugin.language",
										ServiceController.getLocale((String) endpoint.getParam("language")))
								.getString("Authentication_error"),
						null, null);

				return true;
			}

			List<Object> results = message.getResults();
			if (results == null || results.size() < 3) {
				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG, parent.getName()
						+ "- DbEndPointRegistration.processResponse() -- Multiple results not returned on user query");

				// send a error response to the client
				parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR,
						java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.plugin.language",
										ServiceController.getLocale((String) endpoint.getParam("language")))
								.getString("Database_error"),
						null, null);

				return true;
			}

			boolean hasAssociatedGroups = false;

			if (results.get(0) == null) {
				// send error response to the client
				parent.sendRegistrationResponse(endpoint, ResponseMessage.FORBIDDEN,
						java.util.ResourceBundle
								.getBundle("com.quikj.application.web.talk.plugin.language",
										ServiceController.getLocale((String) endpoint.getParam("language")))
								.getString("User_authentication_failed"),
						null, null);

				return true;
			}

			UserElement userData = (UserElement) results.get(0);
			userData.setName(username);
			endpointInfo.setUserData(userData);

			// set group owner info
			if (results.get(1) != null) {
				List<?> groups = (List<?>) results.get(1);
				for (Object o : groups) {
					String group = (String) o;
					if (!userData.addOwnsGroup(group)) {
						AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
								username + " DbEndpointRegistration.processResponse() -- Couldn't add owned group "
										+ group + " to UserElement, probably duplicate error.");
					}
				}
			}

			// set group member info
			if (results.get(2) != null) {
				List<?> groups = (List<?>) results.get(2);
				for (Object o : groups) {
					String group = (String) o;
					if (!userData.addBelongsToGroup(group)) {
						AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
								username + " DbEndpointRegistration.processResponse() -- Couldn't add belongs to group "
										+ group + " to UserElement, probably duplicate error.");
					}
				}
			}

			if ((userData.numBelongsToGroups() > 0) || (userData.numOwnsGroups() > 0)) {
				hasAssociatedGroups = true;
			}

			if (!hasAssociatedGroups) {
				finished(endpoint);
				return true;
			}

			// get group info
			SQLParam[] statements = GroupTable.getGroupInfoByUserQueryStatements(username);

			operationId = database.executeSQL(parent, this, statements);

			if (operationId == -1L) {
				lastError = parent.getErrorMessage();

				AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
						"DbEndPointRegistration.processResponse() -- Failure getting group info for user " + username
								+ ", error : " + lastError);

				parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR, "Database error (group data)",
						null, null);

				return true;
			}

			return false;
		}

		// second time around, processing group info result

		if (message.getStatus() == AceSQLMessage.SQL_ERROR) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					parent.getName()
							+ "- DbEndPointRegistration.processResponse() -- Database error result getting group info for user "
							+ username + ".");

			// send a error response to the client
			parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) endpoint.getParam("language")))
							.getString("Database_error_(group_data)"),
					null, null);

			return true;
		}

		List<Object> results = message.getResults();
		if (results == null || results.size() < 4) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					parent.getName()
							+ "- DbEndPointRegistration.processResponse() -- Database result error getting group info for user "
							+ username + ".");

			// send a error response to the client
			parent.sendRegistrationResponse(endpoint, ResponseMessage.INTERNAL_ERROR,
					java.util.ResourceBundle
							.getBundle("com.quikj.application.web.talk.plugin.language",
									ServiceController.getLocale((String) endpoint.getParam("language")))
							.getString("Database_error_(group_data)"),
					null, null);

			return true;
		}

		groupInfos = processGroupInfoByUserQueryResult(username, results);

		finished(endpoint);
		return true;
	}

	public boolean registerEndPoint() {
		if (password == null && FeatureFactory.getInstance().isFeature(username)) {
			// allow feature login with no password
			SQLParam[] statements = UserTable.getUserElementQueryStatements(username);
			operationId = database.executeSQL(parent, this, statements);
		} else {
			SQLParam[] statements = UserTable.getUserElementQueryStatements(username, password);

			operationId = database.executeSQL(parent, this, statements);
		}

		if (operationId == -1L) {
			lastError = parent.getErrorMessage();
			return false;
		}

		return true;
	}

	private GroupInfo[] processGroupInfoByUserQueryResult(String username, List<Object> results) {
		GroupInfo[] list = null;

		HashMap<String, GroupInfo> grouplist = new HashMap<String, GroupInfo>();

		// process getting groups info by owner (group name, notification
		// controls)
		if (results.get(0) != null) {

			List<?> groupInfoList = (List<?>) results.get(0);
			for (Object o : groupInfoList) {
				GroupInfo info = (GroupInfo) o;
				info.setOwner(username);
				grouplist.put(info.getName(), info);
			}
		}

		if (results.get(1) != null) {
			// process getting groups members by owner
			List<?> groupMemberList = (List<?>) results.get(1);
			for (Object o : groupMemberList) {
				GroupMember member = (GroupMember) o;
				GroupInfo info = grouplist.get(member.getGroupName());

				if (info == null) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							" DbEndPointRegistration.processGroupInfoByUserQueryResult() -- Couldn't find group "
									+ member.getGroupName() + " in list, owned by user " + username
									+ ", trying to add member " + member);
				} else if (!info.addMember(member.getUserName())) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							" DbEndPointRegistration.processGroupInfoByUserQueryResult() -- Couldn't add member "
									+ member.getUserName() + " to group " + member.getGroupName() + " owned by "
									+ username + ", probably duplicate error.");
				}
			}
		}

		if (results.get(2) != null) {
			// process getting groups info by member (group name, notif, owner)
			List<?> groupInfoList = (List<?>) results.get(2);
			for (Object o : groupInfoList) {
				GroupInfo info = (GroupInfo) o;
				grouplist.put(info.getName(), info);
			}
		}

		if (results.get(3) != null) {
			// process getting groups members by member
			List<?> groupMemberList = (List<?>) results.get(3);
			for (Object o : groupMemberList) {
				GroupMember member = (GroupMember) o;
				GroupInfo info = grouplist.get(member.getGroupName());

				if (info == null) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							" DbEndPointRegistration.processGroupInfoByUserQueryResult() -- Couldn't find group "
									+ member.getGroupName() + " in list, referenced by member user " + username
									+ ", trying to add member " + member);
				} else if (!info.addMember(member.getUserName())) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							" DbEndPointRegistration.processGroupInfoByUserQueryResult() -- Couldn't add member "
									+ member.getUserName() + " to group " + member.getGroupName()
									+ " referenced by member " + username + ", probably duplicate error.");
				}
			}
		}

		if (grouplist.size() > 0) {
			GroupInfo[] temp = new GroupInfo[grouplist.size()];
			list = (grouplist.values().toArray(temp));
		}

		return list;
	}
}
