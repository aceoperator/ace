/*
 * ListGroupsAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class ListGroupsAction extends Action {

	public ListGroupsAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		try {
			List<Group> list = groupBean.listGroups(null);
			// store the list result items for the jsp
			ArrayList<HashMap<String, String>> name_list = new ArrayList<HashMap<String, String>>();
			for (Group group : list) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", group.getName());
				map.put("submit", "Find");
				map.put("domain", group.getDomain());
				name_list.add(map);
			}

			request.setAttribute("groups", name_list);

		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"ListGroupsAction.execute()/by-"
							+ request.getUserPrincipal().getName() + ": "
							+ e.getMessage());
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}