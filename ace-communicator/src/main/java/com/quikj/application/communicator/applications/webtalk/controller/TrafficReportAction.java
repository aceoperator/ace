/*
 * TrafficReportAction.java
 *
 * Created on March 14, 2003, 9:49 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.TrafficStatistics;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author Vinod Batra
 */
public final class TrafficReportAction extends Action {
	private static final SimpleDateFormat REPORT_DATE_FORMAT = new SimpleDateFormat(
			"MM/dd/yyyy");

	public TrafficReportAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		TrafficReportForm tform = (TrafficReportForm) form;

		ActionErrors errors = new ActionErrors();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);
		ArrayList<LabelValueBean> list = GroupManagementAction.populateGroups(
				groupBean, null);
		tform.setUserGroups(list);

		String decodedGroupId = URLDecoder.decode(tform.getGroupid(), "UTF-8");

		UserBean userBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), UserBean.class);

		try {
			Calendar startDate = Calendar.getInstance();
			startDate.setTime(REPORT_DATE_FORMAT.parse(tform.getStartDate()));

			Calendar endDate = Calendar.getInstance();
			endDate.setTime(REPORT_DATE_FORMAT.parse(tform.getEndDate()));

			List<TrafficStatistics> traffic = userBean.getTrafficReport(
					startDate, endDate, decodedGroupId);

			request.setAttribute("trafficData", traffic);
			request.setAttribute("today", (new java.util.Date()).toString());
			request.setAttribute("groupId", decodedGroupId);
		} catch (Exception e) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"TrafficReportAction.execute() : " + e.getMessage(), e);

			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));
			saveErrors(request, errors);

			return mapping.getInputForward();
		}

		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Visitor Report", "visitor_report_input"));
		menu.addLink(new LinkAttribute("Registered User Report",
				"reg_report_input"));
		request.setAttribute("menu", menu);

		return mapping.findForward("show_traffic_report");
	}

	public static java.sql.Date convertDate(String date) {
		StringTokenizer tokens = new StringTokenizer(date, "/");
		if (tokens.countTokens() != 3) {
			return null;
		}
	
		int m = 0;
		int d = 0;
		int y = 0;
	
		try {
			m = Integer.parseInt(tokens.nextToken()) - 1;
			d = Integer.parseInt(tokens.nextToken());
			y = Integer.parseInt(tokens.nextToken());
	
			Calendar cal = Calendar.getInstance();
			cal.setLenient(false);
	
			try {
				cal.set(y, m, d, 0, 0, 0);
			} catch (Exception ex) {
				return null;
			}
	
			return new java.sql.Date(cal.getTime().getTime());
		} catch (NumberFormatException ex) {
			return null;
		}
	}
}
