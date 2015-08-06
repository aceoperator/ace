/*
 * GroupManagementAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class GroupManagementAction extends Action {

	/** Creates a new instance of GroupManagementAction */
	public GroupManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		GroupManagementForm gform = (GroupManagementForm) form;

		ActionErrors errors = new ActionErrors();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		if (gform.getSubmit().equals("Find")) {
			try {
				Group e = groupBean.findByName(gform.getName());

				gform.setDomain(e.getDomain());

				switch (e.getMemberBusyNotification()) {
				case GroupBean.NOTIFY_NONE: {
					gform.setMemberCallCountNotifyMembers(false);
					gform.setMemberCallCountNotifyOwner(false);
				}
					break;
				case GroupBean.NOTIFY_OWNER: {
					gform.setMemberCallCountNotifyMembers(false);
					gform.setMemberCallCountNotifyOwner(true);
				}
					break;
				case GroupBean.NOTIFY_MEMBERS: {
					gform.setMemberCallCountNotifyMembers(true);
					gform.setMemberCallCountNotifyOwner(false);
				}
					break;
				case GroupBean.NOTIFY_ALL: {
					gform.setMemberCallCountNotifyMembers(true);
					gform.setMemberCallCountNotifyOwner(true);
				}
					break;
				default:
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"GroupManagementAction.execute()/Find/by-"
											+ request.getUserPrincipal().getName()
											+ ": "
											+ "Invalid DB value for MemberBusyNotificationControl : "
											+ e.getMemberBusyNotification()
											+ ", group = " + e.getName());

					break;
				}

				switch (e.getMemberLoginNotification()) {
				case GroupBean.NOTIFY_NONE: {
					gform.setMemberLoginNotifyMembers(false);
					gform.setMemberLoginNotifyOwner(false);
				}
					break;
				case GroupBean.NOTIFY_OWNER: {
					gform.setMemberLoginNotifyMembers(false);
					gform.setMemberLoginNotifyOwner(true);
				}
					break;
				case GroupBean.NOTIFY_MEMBERS: {
					gform.setMemberLoginNotifyMembers(true);
					gform.setMemberLoginNotifyOwner(false);
				}
					break;
				case GroupBean.NOTIFY_ALL: {
					gform.setMemberLoginNotifyMembers(true);
					gform.setMemberLoginNotifyOwner(true);
				}
					break;
				default:
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));

					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"GroupManagementAction.execute()/Find/by-"
											+ request.getUserPrincipal().getName()
											+ ": "
											+ "Invalid DB value for MemberLoginNotificationControl : "
											+ e.getMemberLoginNotification()
											+ ", group = " + e.getName());

					break;
				}

				switch (e.getOwnerBusyNotification()) {
				case GroupBean.NOTIFY_NONE: {
					gform.setOwnerCallCountNotifyMembers(false);
				}
					break;
				case GroupBean.NOTIFY_MEMBERS: {
					gform.setOwnerCallCountNotifyMembers(true);
				}
					break;
				case GroupBean.NOTIFY_ALL: {
					gform.setOwnerCallCountNotifyMembers(true);
				}
					break;
				default:
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));

					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"GroupManagementAction.execute()/Find/by-"
											+ request.getUserPrincipal().getName()
											+ ": "
											+ "Invalid DB value for OwnerBusyNotificationControl : "
											+ e.getOwnerBusyNotification()
											+ ", group = " + e.getName());

					break;
				}

				switch (e.getOwnerLoginNotification()) {
				case GroupBean.NOTIFY_NONE: {
					gform.setOwnerLoginNotifyMembers(false);
				}
					break;
				case GroupBean.NOTIFY_MEMBERS: {
					gform.setOwnerLoginNotifyMembers(true);
				}
					break;
				case GroupBean.NOTIFY_ALL: {
					gform.setOwnerLoginNotifyMembers(true);
				}
					break;
				default:
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.db.failure"));

					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"GroupManagementAction.execute()/Find/by-"
											+ request.getUserPrincipal().getName()
											+ ": "
											+ "Invalid DB value for OwnerLoginNotificationControl() : "
											+ e.getOwnerLoginNotification()
											+ ", group = " + e.getName());

					break;
				}
			} catch (WebTalkException e) {

				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.group.not.exist"));
			}
		} else if (gform.getSubmit().equals("Modify")) {
			Group e = new Group();
			setNotificationControls(gform, e);
			e.setName(gform.getName());
			e.setDomain(gform.getDomain());

			try {
				groupBean.modifyGroup(e);

				AceLogger.Instance()
						.log(AceLogger.INFORMATIONAL,
								AceLogger.USER_LOG,
								"User " + request.getUserPrincipal().getName()
										+ " modified data for group "
										+ gform.getName());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.group.modified"));
				saveMessages(request, messages);

				return mapping.findForward("webtalk_main_menu");

			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.group.not.exist"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupManagementAction.execute()/Modify/by-"
								+ request.getUserPrincipal().getName() + ": " + ex.getMessage());
			}

		} else if (gform.getSubmit().equals("Create")) {
			Group e = new Group();

			setNotificationControls(gform, e);
			e.setName(gform.getName());
			e.setDomain(gform.getDomain());

			try {
				groupBean.createGroup(e);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " created new group "
								+ gform.getName());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.group.created"));
				saveMessages(request, messages);

				return mapping.findForward("webtalk_main_menu");
			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.group.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupManagementAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": " + ex.getMessage());
			}
		} else if (gform.getSubmit().equals("Delete")) {
			try {
				groupBean.deleteGroup(gform.getName());

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName() + " deleted group "
								+ gform.getName());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.group.deleted"));
				saveMessages(request, messages);

				return mapping.findForward("webtalk_main_menu");

			} catch (Exception ex) {

				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.db.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"GroupManagementAction.execute()/Delete/by-"
								+ request.getUserPrincipal().getName() + ": " + ex.getMessage());
			}
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}

	private void setNotificationControls(GroupManagementForm gform, Group e) {
		// set member busy notification control
		if (gform.isMemberCallCountNotifyMembers()) {
			if (gform.isMemberCallCountNotifyOwner()) {
				e.setMemberBusyNotification(GroupBean.NOTIFY_ALL);
			} else {
				e.setMemberBusyNotification(GroupBean.NOTIFY_MEMBERS);
			}
		} else {
			if (gform.isMemberCallCountNotifyOwner()) {
				e.setMemberBusyNotification(GroupBean.NOTIFY_OWNER);
			} else {
				e.setMemberBusyNotification(GroupBean.NOTIFY_NONE);
			}
		}

		// set member login notification control
		if (gform.isMemberLoginNotifyMembers()) {
			if (gform.isMemberLoginNotifyOwner()) {
				e.setMemberLoginNotification(GroupBean.NOTIFY_ALL);
			} else {
				e.setMemberLoginNotification(GroupBean.NOTIFY_MEMBERS);
			}
		} else {
			if (gform.isMemberLoginNotifyOwner()) {
				e.setMemberLoginNotification(GroupBean.NOTIFY_OWNER);
			} else {
				e.setMemberLoginNotification(GroupBean.NOTIFY_NONE);
			}
		}

		// set owner busy notification control
		if (gform.isOwnerCallCountNotifyMembers()) {
			e.setOwnerBusyNotification(GroupBean.NOTIFY_MEMBERS);
		} else {
			e.setOwnerBusyNotification(GroupBean.NOTIFY_NONE);
		}

		// set owner login notification control
		if (gform.isOwnerLoginNotifyMembers()) {
			e.setOwnerLoginNotification(GroupBean.NOTIFY_MEMBERS);
		} else {
			e.setOwnerLoginNotification(GroupBean.NOTIFY_NONE);
		}
	}

	protected static ArrayList<LabelValueBean> populateGroups(
			GroupBean groupBean, String domain)
			throws UnsupportedEncodingException {
		List<Group> groups = groupBean.listGroups(domain);
		ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>(
				groups.size() + 2);
		for (Group group : groups) {
			list.add(new LabelValueBean(group.getName(), URLEncoder.encode(
					group.getName(), "UTF-8")));
		}
		return list;
	}
}
