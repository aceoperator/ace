/*
 * DisplayDropCustomerSelectAction.java
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

import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.LinkAttribute;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author bhm
 */
public class DisplayDropCustomerSelectAction extends Action {

	public DisplayDropCustomerSelectAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws UnsupportedEncodingException {
		DropCustomerSelectForm cform = (DropCustomerSelectForm) form;

		GroupBean groupBean = SpringUtils.getBean(request.getSession()
				.getServletContext(), GroupBean.class);

		List<String> domains = groupBean.listDomains();
		ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();

		for (String domain: domains) {
			if (!domain.equals("ace")) {
				list.add(new LabelValueBean(domain, URLEncoder.encode(domain,
						"UTF-8")));
			}
		}

		cform.setDomains(list);

		// add related tasks to the navigation bar
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		request.setAttribute("menu", menu);

		menu.addLink(new LinkAttribute("List all groups", "list_groups"));

		return mapping.getInputForward();
	}
}
