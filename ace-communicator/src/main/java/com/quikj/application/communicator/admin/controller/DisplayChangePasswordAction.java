/*
 * DisplayChangePasswordAction.java
 *
 * Created on May 8, 2003, 5:11 PM
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
public class DisplayChangePasswordAction extends Action {

	public DisplayChangePasswordAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return mapping.getInputForward();
	}
}
