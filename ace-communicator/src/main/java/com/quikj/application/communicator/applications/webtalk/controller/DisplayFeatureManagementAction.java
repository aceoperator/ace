/*
 * DisplayFeatureManagementAction.java
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
public class DisplayFeatureManagementAction extends Action {

	public DisplayFeatureManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		menu.addLink(new LinkAttribute("List all features", "list_features"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
