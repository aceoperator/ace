/*
 * DisplayTrafficReportInputAction.java
 *
 * Created on May 3, 2003, 9:20 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author Vinod Batra
 */
public final class DisplayTrafficReportInputAction extends Action {
	
	public DisplayTrafficReportInputAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		TrafficReportForm tform = (TrafficReportForm) form;

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		ArrayList<LabelValueBean> list = GroupManagementAction.populateGroups(groupBean, null);
		tform.setUserGroups(list);
		
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu
				.addLink(new LinkAttribute("Visitor Report",
						"visitor_report_input"));
		menu.addLink(new LinkAttribute("Registered User Report",
				"reg_report_input"));
		request.setAttribute("menu", menu);

		return (new ActionForward(mapping.getInput()));
	}
}
