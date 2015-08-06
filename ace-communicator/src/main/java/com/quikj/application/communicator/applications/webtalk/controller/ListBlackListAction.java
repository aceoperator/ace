package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.ace.db.core.webtalk.vo.Blacklist;
import com.quikj.ace.db.webtalk.model.BlacklistBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author amit
 */
public class ListBlackListAction extends Action {

	public ListBlackListAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		ActionErrors errors = new ActionErrors();

		String userName = (String)request.getAttribute("userContext");
		BlacklistBean blacklist = SpringUtils.getBean(request.getSession()
				.getServletContext(), BlacklistBean.class);
		try {
			List<Blacklist> list = blacklist.list(userName);
			request.setAttribute("blacklist", list);
		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.no.such.user"));
			request.setAttribute("userContext", new ArrayList<Blacklist>());
			e.printStackTrace();
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}
		
		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
