/*
 * ChangePasswordAction.java
 *
 * Created on May 8, 2003, 5:14 PM
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

import com.quikj.ace.db.webtalk.model.AccountBean;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class ChangePasswordAction extends Action {

	public ChangePasswordAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ChangePasswordForm f = (ChangePasswordForm) form; // get the form bean
		ActionErrors errors = new ActionErrors();

		AccountBean accountBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), AccountBean.class);

		try {
			accountBean.changePassword(request.getUserPrincipal().getName(),
					f.getOldPassword(), f.getNewPassword());
		} catch (Exception e) {
			AceLogger.Instance().log(
					AceLogger.WARNING,
					AceLogger.SYSTEM_LOG,
					"ChangePasswordAction.execute()/by-" + request.getUserPrincipal().getName()
							+ ": " + e.getMessage(), e);

			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.password.invalid"));
		}


		// if errors were encountered
		if (!errors.isEmpty()) {
			// Report any errors we have discovered back to the original form
			saveErrors(request, errors);
			return (new ActionForward(mapping.getInput()));
		}

		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.USER_LOG,
				"User " + request.getUserPrincipal().getName() + " changed his/her own password");

		// Remove the obsolete form bean
		if (mapping.getAttribute() != null) {
			if (mapping.getScope().equals("request")) {
				request.removeAttribute(mapping.getAttribute());
			} else {
				request.getSession().removeAttribute(mapping.getAttribute());
			}
		}

		// forward control to the main menu
		ActionMessages messages = new ActionMessages();

		messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
				"message.password.changed"));
		saveMessages(request, messages);
		return mapping.findForward("main_menu");
	}
}
