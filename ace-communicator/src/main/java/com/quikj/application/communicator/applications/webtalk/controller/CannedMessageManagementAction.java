package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

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

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.webtalk.WebTalkException;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class CannedMessageManagementAction extends Action {

	public CannedMessageManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		CannedMessageManagementForm uform = (CannedMessageManagementForm) form;

		ActionErrors errors = new ActionErrors();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		ArrayList<LabelValueBean> list = GroupManagementAction.populateGroups(groupBean, null);
		list.add(0, new LabelValueBean("", URLEncoder.encode("", "UTF-8")));
		list.add(1,
				new LabelValueBean("all", URLEncoder.encode("all", "UTF-8")));
		uform.setUserGroups(list);

		CannedMessageBean cannedBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), CannedMessageBean.class);

		if (uform.getSubmit().equals("Find")) {
			CannedMessage canned = cannedBean.getById(uform.getId());

			if (canned == null) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.cannedmessage.not.exist"));
			} else {
				if (canned.getGroupName() != null) {
					uform.setGroup(URLEncoder.encode(canned.getGroupName(),
							"UTF-8"));
				} else {
					uform.setGroup("all");
				}

				uform.setDescription(canned.getDescription());
				uform.setMessage(canned.getMessage());
				uform.setId(canned.getId());
			}
		} else if (uform.getSubmit().equals("Modify")) {
			CannedMessage canned = new CannedMessage();

			if (uform.getGroup() == null || uform.getGroup().equals("all")) {
				canned.setGroupName(null);
			} else {
				canned.setGroupName(URLDecoder.decode(uform.getGroup(), "UTF-8"));
			}

			canned.setDescription(uform.getDescription());

			String message = uform.getMessage().trim();
			canned.setMessage(message);
			canned.setId(uform.getId());

			try {
				cannedBean.update(canned);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " modified webtalk canned message "
								+ uform.getId());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.cannedmessage.modified"));

				saveMessages(request, messages);
				return mapping.findForward("webtalk_main_menu");
			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.cannedmessage.not.exist"));
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " modified webtalk canned message "
								+ uform.getId());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.cannedmessage.modified"));

				saveMessages(request, messages);
				return mapping.findForward("webtalk_main_menu");
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.db.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"CannedMessageManagementAction.execute()/Modify/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage(), e);
			}
		} else if (uform.getSubmit().equals("Create")) {
			CannedMessage canned = new CannedMessage();

			if (uform.getGroup() == null || uform.getGroup().equals("all")) {
				canned.setGroupName(null);
			} else {
				canned.setGroupName(URLDecoder.decode(uform.getGroup(), "UTF-8"));
			}

			canned.setDescription(uform.getDescription());

			String message = uform.getMessage().trim();
			canned.setMessage(message);

			try {
				cannedBean.create(canned);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " created webtalk canned message "
								+ uform.getId());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.cannedmessage.created"));
				saveMessages(request, messages);

				return mapping.findForward("webtalk_main_menu");
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.cannedmessage.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"CannedMessageManagementAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage(), e);
			}
		} else if (uform.getSubmit().equals("Delete")) {
			try {
				cannedBean.delete(uform.getId());

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " deleted webtalk canned message "
								+ uform.getId());

				// forward control to the webtalk main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.cannedmessage.deleted"));
				saveMessages(request, messages);

				return mapping.findForward("webtalk_main_menu");

			} catch (WebTalkException e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.cannedmessage.not.exist"));
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.db.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"CannedMessageManagementAction.execute()/Delete/by-"
								+ request.getUserPrincipal().getName() + ": " + e.getMessage(), e);
			}
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search canned messages",
				"display_canned_message_search"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}

}
