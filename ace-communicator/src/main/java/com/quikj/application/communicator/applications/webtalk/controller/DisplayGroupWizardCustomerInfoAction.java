/*
 * DisplayGroupWizardCustomerInfoAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.application.communicator.admin.controller.LinkAttribute;

/**
 * 
 * @author bhm
 */
public class DisplayGroupWizardCustomerInfoAction extends Action {

	public DisplayGroupWizardCustomerInfoAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));

		return mapping.getInputForward();
	}
}
