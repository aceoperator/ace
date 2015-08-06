/*
 * DisplayDropCustomerAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

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
import org.apache.struts.action.DynaActionForm;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.core.webtalk.vo.Feature;
import com.quikj.ace.db.core.webtalk.vo.Group;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;
import com.quikj.ace.db.webtalk.model.FeatureBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class DisplayDropCustomerAction extends Action {

	public DisplayDropCustomerAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ActionErrors errors = new ActionErrors();

		String domain = (String) request.getAttribute("domain");

		((DynaActionForm) form).set("domain", new String(domain));

		// query the data and present it to user

		UserBean userBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), UserBean.class);

		List<String> operators = null;
		List<String> owners = null;
		try {
			// get the list of operators
			operators = userBean.findMembersByGroupDomain(domain);

			// get the list of group owners/features
			owners = userBean.findOwnersByGroupDomain(domain);
		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.customer.delete.finddata"));

			saveErrors(request, errors);

			// log the error(s)
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"DisplayDropCustomerAction.execute()/by-"
							+ request.getUserPrincipal().getName()
							+ ": Error finding data in domain " + domain + ": "
							+ e.getMessage(), e);

			return mapping.findForward("webtalk_main_menu");
		}

		// get a list of any stray users
		List<String> strayUsers = null;

		try {
			strayUsers = userBean.listUsersByDomain(domain);
			ArrayList<String> knownUsers = new ArrayList<String>(operators);
			knownUsers.addAll(owners);
			strayUsers.removeAll(knownUsers);

			if (strayUsers.size() <= 0) {
				strayUsers = null;
			}
		} catch (Exception e) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.customer.delete.strayusers"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"DisplayDropCustomerAction.execute()/by-"
							+ request.getUserPrincipal().getName()
							+ ": Error finding data in domain " + domain + ": "
							+ "stray users - " + e.getMessage(), e);
		}

		if (operators.size() <= 0) {
			operators = null;
		}

		if (owners.size() <= 0) {
			owners = null;
		}

		// get a list of all the groups
		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		List<Group> groups = groupBean.listGroups(domain);
		List<String> groupNames = new ArrayList<String>();
		for (Group group : groups) {
			groupNames.add(group.getName());
		}

		// get a list of the features

		FeatureBean featureBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), FeatureBean.class);

		List<Feature> features = featureBean.listFeatures(domain);

		// get a list of canned messages
		CannedMessageBean cannedBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), CannedMessageBean.class);
		CannedMessage search = new CannedMessage();
		search.setGroupName(domain);

		List<CannedMessage> list = cannedBean.search(search);
		if (list.size() == 0) {
			list = null;
		}

		request.setAttribute("operators", operators);
		request.setAttribute("owners", owners);
		request.setAttribute("strayUsers", strayUsers);
		request.setAttribute("groups", groupNames);
		request.setAttribute("features", features);
		request.setAttribute("cannedMessages", list);

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
