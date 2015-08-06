/*
 * GroupWizardCancelAction.java
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

/**
 * 
 * @author bhm
 */
public class GroupWizardCancelAction extends Action {

	public GroupWizardCancelAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		String submit = (String) ((DynaActionForm) form).get("submit");

		if (submit.equals("Exit") == true) {
			request.getSession().setAttribute("groupWizardLog", null);
			request.getSession().setAttribute("groupWizardDomain", null);
			request.getSession().setAttribute("groupWizardCompanyName", null);
			request.getSession().setAttribute("groupWizardCompanyUrl", null);
		}

		return mapping.findForward(submit);
	}
}
