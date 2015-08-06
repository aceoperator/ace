/*
 * UserSearchAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.User;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.ace.db.webtalk.model.UserBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author bhm
 */
public class UserSearchAction extends Action {

	public UserSearchAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		UserSearchForm uform = (UserSearchForm) form;

		ActionErrors errors = new ActionErrors();
		ActionMessages messages = new ActionMessages();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);
		ArrayList<LabelValueBean> groupList = GroupManagementAction
				.populateGroups(groupBean, null);
		uform.setUserGroups(groupList);

		UserBean userBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), UserBean.class);

		User e = new User();
		e.setAdditionalInfo(uform.getAdditionalInfo());
		e.setEmail(uform.getAddress());
		e.setFullName(uform.getFullName());
		e.setUnavailableTransferTo(uform.getUnavailXferTo());
		e.setGatekeeper(uform.getGatekeeper());
		e.setUserName(uform.getName());
		e.setDomain(uform.getDomain());
		e.setAvatar(uform.getAvatar());

		if (uform.getLocked().equals("0")) {
			e.setSearchLocked(null);
		} else if (uform.getLocked().equals("1")) {
			e.setSearchLocked(true);
		} else {
			e.setSearchLocked(false);
		}

		if (uform.getChangePassword().equals("0")) {
			e.setSearchChangePassword(null);
		} else if (uform.getChangePassword().equals("1")) {
			e.setSearchChangePassword(true);
		} else {
			e.setSearchChangePassword(false);
		}

		Object[] ogroups = uform.getOwnsGroups();
		if (ogroups != null) {
			for (int i = 0; i < ogroups.length; i++) {
				if (ogroups[i] != null) {
					e.getOwnsGroups().add(
							URLDecoder.decode((String) ogroups[i], "UTF-8"));
				}
			}
		}

		ogroups = uform.getBelongsToGroups();
		if (ogroups != null) {
			for (int i = 0; i < ogroups.length; i++) {
				if (ogroups[i] != null) {
					e.getMemberOfGroups().add(
							URLDecoder.decode((String) ogroups[i], "UTF-8"));
				}
			}
		}

		List<String> userList = userBean.searchUser(e);

		int num_items = userList.size();
		if (num_items == 0) {
			messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
					"message.user.search.empty"));
		} else {
			// store the search result items
			ArrayList<HashMap<String, String>> nameList = new ArrayList<HashMap<String, String>>();
			for (String user : userList) {
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("name", user);
				map.put("submit", "Find");
				nameList.add(map);
			}
			request.setAttribute("users", nameList);

			// add related tasks to the navigation bar
			WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
			menu.addLink(new LinkAttribute("Search users",
					"display_user_search"));
			menu.addLink(new LinkAttribute("Administer users",
					"display_user_management"));
			menu.addLink(new LinkAttribute("List all groups", "list_groups"));
			menu.addLink(new LinkAttribute("Administer groups",
					"display_group_management"));
			request.setAttribute("menu", menu);

			// forward control to the search result screen
			return mapping.findForward("user_search_result");
		}

		if (!errors.isEmpty()) {
			saveErrors(request, errors);
		}

		if (!messages.isEmpty()) {
			saveMessages(request, messages);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search users", "display_user_search"));
		menu.addLink(new LinkAttribute("Administer users",
				"display_user_management"));
		menu.addLink(new LinkAttribute("List all groups", "list_groups"));
		menu.addLink(new LinkAttribute("Administer groups",
				"display_group_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
