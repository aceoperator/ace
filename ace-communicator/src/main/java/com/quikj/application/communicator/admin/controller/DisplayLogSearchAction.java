package com.quikj.application.communicator.admin.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.ace.db.webtalk.model.LogBean;

/**
 * @author Elizabeth Roman
 * @version $Revision: 1.2 $ $Date: 2004/05/03 11:09:17 $
 */
public final class DisplayLogSearchAction extends Action {
	
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		LogBean log = SpringUtils.getBean(request.getSession()
				.getServletContext(), LogBean.class);

		// get the list of unique process names
		request.getSession().setAttribute("logProcessNames",
				log.getUniqueProcessNames());

		// add related tasks to the navigation bar
		RelatedTasks menu = new RelatedTasks();
		menu.addLink(new LinkAttribute("Delete logs", "display_log_delete"));
		request.setAttribute("menu", menu);

		return (new ActionForward(mapping.getInput()));
	}
}
