/*
 * DisplayDeleteFileInputAction.java
 *
 * Created on June 12, 2003, 9:06 PM
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
public class DisplayDeleteFileInputAction extends Action {

	public DisplayDeleteFileInputAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		WebTalkRelatedTasks menu = new WebTalkRelatedTasks();
		menu.addLink(new LinkAttribute("Upload File", "upload_file_input"));
		menu.addLink(new LinkAttribute("Rename File", "rename_file_input"));

		request.setAttribute("menu", menu);
		return (new ActionForward(mapping.getInput()));
	}
}
