package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.application.communicator.admin.controller.LinkAttribute;

public final class DisplayRegReportInputAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu
				.addLink(new LinkAttribute("Visitor Report",
						"visitor_report_input"));
		menu.addLink(new LinkAttribute("Usage Report", "traffic_report_input"));

		request.setAttribute("menu", menu);
		return (new ActionForward(mapping.getInput()));
	}
}
