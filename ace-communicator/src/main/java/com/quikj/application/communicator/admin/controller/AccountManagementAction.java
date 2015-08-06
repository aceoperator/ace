/*
 * AccountManagementAction.java
 *
 * Created on May 4, 2003, 11:10 AM
 */

package com.quikj.application.communicator.admin.controller;

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

import com.quikj.ace.db.core.webtalk.vo.Account;
import com.quikj.ace.db.webtalk.model.AccountBean;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class AccountManagementAction extends Action {

	public AccountManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		AccountManagementForm aform = (AccountManagementForm) form;

		ActionErrors errors = new ActionErrors();

		AccountBean accountBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), AccountBean.class);

		if (aform.getSubmit().equals("Find")) {
			try {
				Account e = accountBean.findByUserName(aform.getName());

				if (e == null) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.no.such.user"));
				} else {
					aform.setAdditionalInfo(e.getAdditionalInfo());
				}
			} catch (Exception e) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.no.such.user"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"AccountManagementAction.execute()/Find/by-"
								+ aform.getName() + ": " + e.getMessage(), e);
			}

		} else if (aform.getSubmit().equals("Modify")) {
			Account e = new Account();
			e.setAdditionalInfo(aform.getAdditionalInfo());
			e.setUserName(aform.getName());

			String password = aform.getPassword();
			if ((password != null) && (password.length() > 0)) {
				e.setPassword(password);
			}

			try {
				accountBean.modify(e);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " modified account for user "
								+ aform.getName());

				// forward control to the main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.account.modified"));
				saveMessages(request, messages);
				return mapping.findForward("main_menu");
			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.no.such.user"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"AccountManagementAction.execute()/Modify/by-"
								+ request.getUserPrincipal().getName() + ": "
								+ ex.getMessage(), ex);
			}

		} else if (aform.getSubmit().equals("Create")) {
			Account e = new Account();
			e.setAdditionalInfo(aform.getAdditionalInfo());
			e.setUserName(aform.getName());
			e.setPassword(aform.getPassword());

			try {
				accountBean.create(e);

				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.USER_LOG,
						"User " + request.getUserPrincipal().getName()
								+ " created account for user "
								+ aform.getName());

				// forward control to the main menu
				ActionMessages messages = new ActionMessages();
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.account.created"));
				saveMessages(request, messages);
				return mapping.findForward("main_menu");

			} catch (Exception ex) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.account.create.failure"));

				AceLogger.Instance().log(
						AceLogger.ERROR,
						AceLogger.SYSTEM_LOG,
						"AccountManagementAction.execute()/Create/by-"
								+ request.getUserPrincipal().getName() + ": "
								+ ex.getMessage(), ex);
			}

		} else if (aform.getSubmit().equals("Delete")) {
			if (aform.getName().equals(request.getUserPrincipal().getName())) {
				errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
						"error.account.delete.self"));
			} else {
				try {
					accountBean.delete(aform.getName());
					
					AceLogger.Instance().log(
							AceLogger.INFORMATIONAL,
							AceLogger.USER_LOG,
							"User " + request.getUserPrincipal().getName()
									+ " deleted account for user "
									+ aform.getName());

					// forward control to the main menu
					ActionMessages messages = new ActionMessages();
					messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
							"message.account.deleted"));
					saveMessages(request, messages);
					return mapping.findForward("main_menu");
				
				} catch (Exception e) {
					errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
							"error.no.such.user"));
					
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"AccountManagementAction.execute()/Delete/by-"
									+ request.getUserPrincipal().getName() + ": "
									+ e.getMessage(), e);
				}
			}
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		return mapping.getInputForward();
	}
}
