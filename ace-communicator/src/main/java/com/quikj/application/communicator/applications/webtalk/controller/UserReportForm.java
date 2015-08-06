/*
 * UserReportForm.java
 *@ author Vinod Batra
 * Created on December 23, 2002, 4:52 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author Vinod Batra
 */
public class UserReportForm extends ActionForm {

	private static final long serialVersionUID = -2930831696857468827L;
	private String startDate;
	private String endDate;
	private String orderBy = "name";

	public UserReportForm() {
		java.util.Date today = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		endDate = formatter.format(new Date(today.getTime() + 24 * 3600 * 1000L));
		startDate = formatter.format(today);
	}

	public java.lang.String getEndDate() {
		return endDate;

	}

	public java.lang.String getOrderBy() {
		return orderBy;
	}

	public java.lang.String getStartDate() {
		return startDate;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		Date today = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		
		startDate = formatter.format(today);
		endDate = formatter.format(new Date(today.getTime() + 24 * 3600 * 1000L));
		this.orderBy = "name";
	}


	public void setEndDate(java.lang.String endDate) {
		this.endDate = endDate;
	}

	public void setOrderBy(java.lang.String orderBy) {
		this.orderBy = orderBy;
	}

	public void setStartDate(java.lang.String startDate) {
		this.startDate = startDate;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		java.sql.Date sd = TrafficReportAction.convertDate(startDate);
		if (sd == null) {
			errors.add("startDate", new ActionError("error.Date.format"));
		}

		java.sql.Date ed = TrafficReportAction.convertDate(endDate);
		if (ed == null) {
			errors.add("endDate", new ActionError("error.Date.format"));
		}

		return errors;

	}
}
