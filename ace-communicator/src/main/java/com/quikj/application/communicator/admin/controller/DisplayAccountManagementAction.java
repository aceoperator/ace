/*
 * DisplayAccountManagementAction.java
 *
 * Created on May 3, 2003, 8:05 AM
 */

package com.quikj.application.communicator.admin.controller;

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
public class DisplayAccountManagementAction extends Action {

	public DisplayAccountManagementAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return mapping.getInputForward();
	}
}
