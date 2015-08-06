/*
 * DropCustomerSelectAction.java
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
import org.apache.struts.action.ActionErrors;
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
public class DropCustomerSelectAction extends Action {

	public DropCustomerSelectAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		DropCustomerSelectForm cform = (DropCustomerSelectForm) form;

		ActionErrors errors = new ActionErrors();

		String submit = cform.getSubmit();

		if (submit.startsWith("Cancel") == false) {
			String domain = cform.getDomain();

			GroupBean groupBean = SpringUtils.getBean(request.getSession()
					.getServletContext(), GroupBean.class);

			List<String> domains = groupBean.listDomains();
			ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();
			for (String dom: domains) {
				if (!dom.equals("ace")) {
					list.add(new LabelValueBean(dom, URLEncoder.encode(dom,
							"UTF-8")));
				}
			}

			cform.setDomains(list);

			request.setAttribute("domain", new String(domain));

			// add related tasks to the navigation bar
			WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
			request.setAttribute("menu", menu);

			menu.addLink(new LinkAttribute("List all groups", "list_groups"));
			menu.addLink(new LinkAttribute("List all features", "list_features"));
			menu.addLink(new LinkAttribute("Search users",
					"display_user_search"));
			menu.addLink(new LinkAttribute("Search canned messages",
					"display_canned_message_search"));
		}

		if (errors.isEmpty() == false) {
			saveErrors(request, errors);
			return mapping.getInputForward();
		}

		return mapping.findForward(submit);
	}

}
