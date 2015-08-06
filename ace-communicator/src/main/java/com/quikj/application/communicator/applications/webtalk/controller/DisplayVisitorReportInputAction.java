/*
 * DisplayVisitorReportAction.java
 *
 * Created on April 29, 2003, 9:59 PM
 */

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

/**
 * 
 * @author Vinod Batra
 */
public final class DisplayVisitorReportInputAction extends Action {

	public DisplayVisitorReportInputAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Registered User Report",
				"reg_report_input"));
		menu.addLink(new LinkAttribute("Usage Report", "traffic_report_input"));

		request.setAttribute("menu", menu);
		return (new ActionForward(mapping.getInput()));
	}
}
