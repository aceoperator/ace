/*
 * TrafficReportForm.java
 *
 * Created on March 14, 2003, 9:39 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.SpringUtils;

/**
 * 
 * @author Vinod Batra
 */
public class TrafficReportForm extends ActionForm {

	private static final long serialVersionUID = -9007025623863345042L;
	private String startDate;
	private String endDate;
	private String groupid;

	private ArrayList<LabelValueBean> userGroups = new ArrayList<LabelValueBean>();

	public TrafficReportForm() {
		java.util.Date today = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		endDate = formatter.format(new Date(today.getTime() + 24 * 3600 * 1000L));
		startDate = formatter.format(today);
	}

	public java.lang.String getEndDate() {
		return endDate;
	}

	public java.lang.String getGroupid() {
		return groupid;
	}

	public java.lang.String getStartDate() {
		return startDate;
	}

	public ArrayList<LabelValueBean> getUserGroups() {
		return this.userGroups;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		java.util.Date today = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		endDate = formatter.format(new Date(today.getTime() + 24 * 3600 * 1000L));
		startDate = formatter.format(today);
	}

	public void setEndDate(java.lang.String endDate) {
		this.endDate = endDate;
	}

	public void setGroupid(java.lang.String groupid) {
		this.groupid = groupid;
	}

	public void setStartDate(java.lang.String startDate) {
		this.startDate = startDate;
	}

	public void setUserGroups(ArrayList<LabelValueBean> userGroups) {
		this.userGroups = userGroups;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();
		try {
			java.sql.Date sd = TrafficReportAction.convertDate(startDate);
			if (sd == null) {
				errors.add("startDate", new ActionError("error.Date.format"));
			}

			java.sql.Date ed = TrafficReportAction.convertDate(endDate);
			if (ed == null) {
				errors.add("endDate", new ActionError("error.Date.format"));
			}

			if (!errors.isEmpty()) {
				GroupBean groupBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), GroupBean.class);
				ArrayList<LabelValueBean> list = GroupManagementAction
						.populateGroups(groupBean, null);
				userGroups = list;
			}
		} catch (Exception e) {
			errors.add("startDate", new ActionError("error.internal.error"));
		}
		return errors;
	}
}
