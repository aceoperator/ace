/*
 * DisplayWebTalkMenuAction.java
 *
 * Created on April 29, 2003, 9:01 PM
 */

package com.quikj.application.communicator.admin.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author Amit Chatterjee
 */
public class LogoffAction extends Action {

	public LogoffAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		
		AceLogger.Instance().log(AceLogger.INFORMATIONAL,
				AceLogger.USER_LOG,
				"User " + request.getUserPrincipal().getName() + " logged out");
		
		response.sendRedirect(request.getContextPath() + "/logout");
		
		return null;
	}
}
