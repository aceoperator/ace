/*
 * DisplayRenameFileInputAction.java
 *
 * Created on June 14, 2003, 9:46 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.quikj.application.communicator.admin.controller.LinkAttribute;

/**
 * 
 * @author Vinod Batra
 */
public final class DisplayRenameFileInputAction extends Action {

	public DisplayRenameFileInputAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Upload File", "upload_file_input"));
		menu.addLink(new LinkAttribute("Delete File", "delete_file_input"));

		request.setAttribute("menu", menu);
		return (new ActionForward(mapping.getInput()));
	}
}
