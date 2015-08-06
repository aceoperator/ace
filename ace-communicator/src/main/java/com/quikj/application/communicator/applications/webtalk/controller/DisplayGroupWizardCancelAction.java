/*
 * DisplayGroupWizardCancelAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class DisplayGroupWizardCancelAction extends Action {

	public DisplayGroupWizardCancelAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		ArrayList log = (ArrayList) request.getSession().getAttribute(
				"groupWizardLog");

		if (log == null) {
			return mapping.findForward("Exit");
		}

		return mapping.getInputForward();
	}
}
