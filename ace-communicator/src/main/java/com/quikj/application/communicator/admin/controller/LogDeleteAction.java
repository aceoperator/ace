/*
 * LogDeleteAction.java
 *
 * Created on June 4, 2003, 2:49 PM
 */

package com.quikj.application.communicator.admin.controller;

/**
 *
 * @author  bhm
 */

import java.util.Calendar;

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

import com.quikj.ace.db.webtalk.model.LogBean;
import com.quikj.server.framework.AceLogger;

public class LogDeleteAction extends Action {

	public LogDeleteAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		LogDeleteForm lform = (LogDeleteForm) form;

		ActionErrors errors = new ActionErrors();
		ActionMessages messages = new ActionMessages();

		LogBean log = SpringUtils.getBean(request.getSession()
				.getServletContext(), LogBean.class);
		Calendar beforeDate = Calendar.getInstance();
		beforeDate.setTime(lform.getPriorToDate());

		try {
			int count = log.delete(beforeDate);

			// Tell the user the number of logs which were deleted :
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"message.logs.deleted", count));
			saveMessages(request, messages);
		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"LogDeleteAction.execute()/Delete/by-" + request.getUserPrincipal().getName()
							+ ": " + e.getMessage(), e);

			saveErrors(request, errors);
			// Forward control back to the main menu :
			return mapping.findForward("main_menu");
		}

		// forward control to the main menu :
		return mapping.findForward("main_menu");

	} // execute

} // LogDeleteAction
