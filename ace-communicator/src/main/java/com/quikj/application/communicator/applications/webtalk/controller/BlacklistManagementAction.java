package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;

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

import com.quikj.ace.db.core.webtalk.vo.Blacklist;
import com.quikj.ace.db.webtalk.model.BlacklistBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author amit
 */
public class BlacklistManagementAction extends Action {

	public BlacklistManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		BlacklistManagementForm bform = (BlacklistManagementForm) form;

		ActionErrors errors = new ActionErrors();

		BlacklistBean blacklist = SpringUtils.getBean(request.getSession()
				.getServletContext(), BlacklistBean.class);
		UserBean user = SpringUtils.getBean(request.getSession()
				.getServletContext(), UserBean.class);

		if (bform.getSubmit() == null) {
			try {
				if (bform.getId() > 0) {
					Blacklist bl = blacklist.get(bform.getId());
					if (bl != null) {
						bform.setIdentifier(bl.getIdentifier());
						bform.setLevel(bl.getLevel());
						bform.setType(bl.getType());
					} else {
						return gotoMainMenu(mapping, request,
								"message.blacklist.not.found");
					}
				} else {
					long userId = user.getUserId(bform.getUserName());
					bform.setUserId(userId);
				}
			} catch (Exception e) {
				AceLogger.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"BlacklistManagementAction.execute()/Find/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ " failed", e);
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.find.failed", getRootCause(e)));
			}
		} else if (bform.getSubmit().equals("Create")) {
			try {
				Blacklist bl = new Blacklist();
				bl.setIdentifier(bform.getIdentifier());
				bl.setType(bform.getType());
				bl.setUserId(bform.getUserId());
				bl.setLevel(bform.getLevel());
				blacklist.create(bl);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"Cookie " + bl.getIdentifier()
								+ " added to the blacklist for user ID "
								+ bform.getUserId() + " by "
								+ request.getUserPrincipal().getName());

				return gotoMainMenu(mapping, request,
						"message.blacklist.created");
			} catch (Exception e) {
				AceLogger.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"BlacklistManagementAction.execute()/Create/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ " failed", e);
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.create.failed", getRootCause(e)));
			}
		} else if (bform.getSubmit().equals("Delete")) {
			try {
				blacklist.delete(bform.getId());

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"Blacklist element with ID " + bform.getId()
								+ " deleted from the blacklist by "
								+ request.getUserPrincipal().getName());

				return gotoMainMenu(mapping, request,
						"message.blacklist.deleted");

			} catch (Exception e) {
				AceLogger.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"BlacklistManagementAction.execute()/Delete/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ " failed", e);

				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.delete.failed", getRootCause(e)));
			}
		} else if (bform.getSubmit().equals("Modify")) {
			try {
				Blacklist bl = new Blacklist();
				bl.setId(bform.getId());
				bl.setIdentifier(bform.getIdentifier());
				bl.setType(bform.getType());
				bl.setLevel(bform.getLevel());
				blacklist.modify(bl);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"Blacklist element with ID " + bform.getId()
								+ " modified by " + request.getUserPrincipal().getName());

				return gotoMainMenu(mapping, request,
						"message.blacklist.modified");
			} catch (Exception e) {
				AceLogger.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								"BlacklistManagementAction.execute()/Modify/by-"
										+ request.getUserPrincipal().getName() + ": "
										+ " failed", e);
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.blacklist.modify.failed", getRootCause(e)));
			}
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}

	private ActionForward gotoMainMenu(ActionMapping mapping,
			HttpServletRequest request, String message) {
		ActionMessages messages = new ActionMessages();
		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(message));

		saveMessages(request, messages);
		return mapping.findForward("webtalk_main_menu");
	}

	private String getRootCause(Throwable e) {
		String message = e.getMessage();
		if (e.getCause() != null) {
			message = getRootCause(e.getCause());
		}

		return message;
	}
}
