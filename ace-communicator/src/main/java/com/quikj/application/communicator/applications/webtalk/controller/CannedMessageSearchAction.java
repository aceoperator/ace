package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.core.webtalk.vo.CannedMessage;
import com.quikj.ace.db.webtalk.model.CannedMessageBean;
import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class CannedMessageSearchAction extends Action {

	public CannedMessageSearchAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		CannedMessageSearchForm uform = (CannedMessageSearchForm) form;

		ActionErrors errors = new ActionErrors();
		ActionMessages messages = new ActionMessages();

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		ArrayList<LabelValueBean> groups = GroupManagementAction.populateGroups(groupBean, null);
		groups.add(0, new LabelValueBean("", URLEncoder.encode("", "UTF-8")));
		groups.add(1,
				new LabelValueBean("all", URLEncoder.encode("all", "UTF-8")));
		uform.setUserGroups(groups);
		
		CannedMessage canned = new CannedMessage();

		String field = uform.getDescription();
		if ((field != null) && (field.length() > 0)) {
			canned.setDescription(field);
		}

		field = URLDecoder.decode(uform.getGroup(), "UTF-8");
		if (field != null && field.length() > 0 && !field.equals("all")) {
			canned.setGroupName(field);
		}

		canned.setId(uform.getId());

		field = uform.getMessage();
		if ((field != null) && (field.trim().length() > 0)) {
			canned.setMessage(field.trim());
		}

		CannedMessageBean cannedBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), CannedMessageBean.class);
		List<CannedMessage> list = cannedBean.search(canned);

		if (list == null) {
			errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
					"error.db.failure"));

			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"CannedMessageSearchAction.execute()/Search/by-"
							+ request.getUserPrincipal().getName());
		} else {
			int num_items = list.size();

			if (num_items == 0) {
				messages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
						"message.search.empty"));
			} else {
				// store the search result items for the jsp
				ArrayList<HashMap<String, Object>> nameList = new ArrayList<HashMap<String, Object>>();
				for (CannedMessage ele : list) {
					HashMap<String, Object> map = new HashMap<String, Object>();
					map.put("id", ele.getId());
					map.put("submit", "Find");
					map.put("description", ele.getDescription());

					String group = ele.getGroupName();
					if (group == null) {
						group = "all";
					}

					map.put("group", group);
					nameList.add(map);
				}
				
				request.setAttribute("elements", nameList);

				// add related tasks to the navigation bar
				WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
				menu.addLink(new LinkAttribute("Search canned messages",
						"display_canned_message_search"));
				menu.addLink(new LinkAttribute("Administer canned messages",
						"display_canned_message_management"));
				request.setAttribute("menu", menu);

				// forward control to the search result screen
				return mapping.findForward("canned_message_search_result");
			}
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
		}

		if (messages.isEmpty() == false) {
			saveMessages(request, messages);
		}

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Search canned messages",
				"display_canned_message_search"));
		menu.addLink(new LinkAttribute("Administer canned messages",
				"display_canned_message_management"));
		request.setAttribute("menu", menu);

		return mapping.getInputForward();
	}
}
