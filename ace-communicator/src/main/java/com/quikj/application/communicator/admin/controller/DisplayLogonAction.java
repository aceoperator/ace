package com.quikj.application.communicator.admin.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * Implementation of <strong>Action</strong> that validates a user logon. Based
 * on Apache Struts framework.
 * 
 * @author Vinod Batra
 * @version $Revision: 1.6 $ $Date: 2004/05/03 11:09:17 $
 */

public final class DisplayLogonAction extends Action {
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		Date date = new Date();
		String today = date.toString();
		request.getSession().setAttribute("today", today);

		return (new ActionForward(mapping.getInput()));
	}
}
