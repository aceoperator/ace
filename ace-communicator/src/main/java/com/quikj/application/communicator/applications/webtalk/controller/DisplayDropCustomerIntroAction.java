/*
 * DisplayDropCustomerIntroAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class DisplayDropCustomerIntroAction extends Action {

	public DisplayDropCustomerIntroAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
