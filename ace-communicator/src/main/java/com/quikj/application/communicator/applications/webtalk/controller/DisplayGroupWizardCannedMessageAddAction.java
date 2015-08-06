/*
 * DisplayGroupWizardCannedMessageAddAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author bhm
 */
public class DisplayGroupWizardCannedMessageAddAction extends Action {

	public DisplayGroupWizardCannedMessageAddAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		GroupWizardCannedMessageForm cform = (GroupWizardCannedMessageForm) form;

		String domain = (String) request.getSession().getAttribute(
				"groupWizardDomain");
		cform.setDomain(domain);
		cform.setCounter(cform.getCounter() + 1);

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		List<Group> groups = groupBean.listGroups(domain);
		ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();

		for (Group group : groups) {
			if (!group.getName().equals(domain)) {
				list.add(new LabelValueBean(group.getName(), URLEncoder.encode(
						group.getName(), "UTF-8")));
			} else {
				list.add(0, new LabelValueBean("All of this customer's groups",
						URLEncoder.encode(group.getName(), "UTF-8")));
			}
		}

		cform.setUserGroups(list);

		// clear some of the fields for the next message to be added
		cform.setContent("");
		cform.setDescription("");

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("List all features", "list_features"));
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Search canned messages",
				"display_canned_message_search"));

		return mapping.getInputForward();
	}
}
