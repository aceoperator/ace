/*
 * DisplayGroupWizardGroupAddAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;

import com.quikj.application.communicator.admin.controller.LinkAttribute;

/**
 * 
 * @author bhm
 */
public class DisplayGroupWizardGroupAddAction extends Action {

	public DisplayGroupWizardGroupAddAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		// clear the group name field for the next one
		((DynaActionForm) form).set("groupName", (Object) null);

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("List all features", "list_features"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));

		return mapping.getInputForward();
	}
}
