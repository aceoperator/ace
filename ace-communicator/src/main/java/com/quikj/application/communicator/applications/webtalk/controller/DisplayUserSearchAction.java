/*
 * DisplayUserSearchAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

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
 * @author bhm
 */
public class DisplayUserSearchAction extends Action {

	public DisplayUserSearchAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		UserSearchForm uform = (UserSearchForm) form;

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		ArrayList<LabelValueBean> list = GroupManagementAction.populateGroups(groupBean, null);
		list.add(0, new LabelValueBean("", URLEncoder.encode("", "UTF-8")));
		list.add(1,
				new LabelValueBean("all", URLEncoder.encode("all", "UTF-8")));
		uform.setUserGroups(list);
		
		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
