/*
 * RegUserReportAction.java
 *<%--@author Vinod Batra --%>
 * Created on December 26, 2002, 5:07 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.ace.db.core.webtalk.vo.UserStatistics;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author Vinod Batra
 */
public final class RegUserReportAction extends Action {

	private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy");

	public RegUserReportAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		ActionErrors errors = new ActionErrors();

		UserReportForm uform = (UserReportForm) form;

		UserBean userBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), UserBean.class);

		try {
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(REPORT_DATE_FORMAT.parse(uform.getStartDate()));

			Calendar endDate = Calendar.getInstance();
			endDate.setTime(REPORT_DATE_FORMAT.parse(uform.getEndDate()));

			List<UserStatistics> users = userBean.getUserReport(startDate,
					endDate, uform.getOrderBy().equals("name") ? false : true);

			if (users.size() > 0) {
				request.setAttribute("reg_user_list", users);
			}

		} catch (Exception e) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"RegUserReportAction.execute() : " + e.getMessage(), e);

			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));
			saveErrors(request, errors);

			return mapping.getInputForward();
		}

		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Visitor Report", "visitor_report_input"));
		menu.addLink(new LinkAttribute("Usage Report", "traffic_report_input"));

		request.setAttribute("menu", menu);
		request.setAttribute("reportDate", new java.util.Date().toString());

		return (mapping.findForward("show_report"));
	}
}
