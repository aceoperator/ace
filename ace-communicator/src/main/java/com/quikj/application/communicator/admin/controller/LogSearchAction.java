/*
 * LogSearchAction.java
 *
 */

package com.quikj.application.communicator.admin.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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

import com.quikj.ace.db.core.webtalk.vo.Log;
import com.quikj.ace.db.webtalk.model.LogBean;
import com.quikj.server.framework.AceLogger;

public class LogSearchAction extends Action {

	private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy");

	public LogSearchAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		LogSearchForm lform = (LogSearchForm) form;

		ActionErrors errors = new ActionErrors();
		ActionMessages messages = new ActionMessages();

		LogBean log = SpringUtils.getBean(request.getSession()
				.getServletContext(), LogBean.class);

		List<String> severityLevels = new ArrayList<String>();
		if (lform.getSeverityLevels() != null) {
			severityLevels = Arrays.asList(lform.getSeverityLevels());
		}

		List<String> processNames = new ArrayList<String>();
		if (lform.getProcessNames() != null) {
			processNames = Arrays.asList(lform.getProcessNames());
		}

		try {

			Calendar startDate = Calendar.getInstance();
			startDate.setTime(REPORT_DATE_FORMAT.parse(lform.getStartDate()));

			Calendar endDate = Calendar.getInstance();
			endDate.setTime(REPORT_DATE_FORMAT.parse(lform.getEndDate()));

			List<Log> list = log.search(startDate, endDate, severityLevels,
					processNames, lform.getMessageText());

			if (list.size() == 0) {
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.log.search.empty"));
			} else {
				request.setAttribute("logResultList", list);

				// add related tasks to the navigation bar
				RelatedTasks menu = new RelatedTasks();
				menu.addLink(new LinkAttribute("View logs",
						"display_log_search"));
				menu.addLink(new LinkAttribute("Delete logs",
						"display_log_delete"));
				request.setAttribute("menu", menu);

				// forward control to the search result screen
				return mapping.findForward("log_search_result");
			}

		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"LogSearchAction.execute()/Search/by-"
							+ request.getUserPrincipal().getName() + ": "
							+ e.getMessage(), e);
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}

		// add related tasks to the navigation bar
		RelatedTasks menu = new RelatedTasks();
		menu.addLink(new LinkAttribute("View logs", "display_log_search"));
		menu.addLink(new LinkAttribute("Delete logs", "display_log_delete"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}

}
