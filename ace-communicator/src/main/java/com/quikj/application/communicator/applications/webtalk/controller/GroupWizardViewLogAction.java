/*
 * GroupWizardViewLogAction.java
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

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
public class GroupWizardViewLogAction extends Action {

	public GroupWizardViewLogAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		return mapping.getInputForward();
	}
}
