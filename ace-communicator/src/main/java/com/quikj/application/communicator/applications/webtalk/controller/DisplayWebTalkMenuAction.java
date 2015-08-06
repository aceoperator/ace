/*
 * DisplayWebTalkMenuAction.java
 *
 * Created on April 29, 2003, 9:01 PM
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

/**
 * 
 * @author Vinod Batra
 */
public class DisplayWebTalkMenuAction extends Action {

	public DisplayWebTalkMenuAction() {
	}

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		return (new ActionForward(mapping.getInput()));
	}
}
