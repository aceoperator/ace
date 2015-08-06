/*
 * LogSearchForm.java
 *
 * Created on April 29, 2003, 3:18 PM
 */
package com.quikj.application.communicator.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author amit
 */
public class LogSearchForm extends ActionForm {

	private static final long serialVersionUID = 663506423097567924L;

	private String startDate;

	private String endDate;

	private String[] processNames;

	private Date startSearch;

	private Date endSearch;

	private String[] severityLevels;

	private String messageText;

	public LogSearchForm() {
		reset();
	}

	public String getEndDate() {
		return this.endDate;
	}

	public Date getEndSearch() {
		return this.endSearch;
	}

	public String getMessageText() {
		return this.messageText;
	}

	public String[] getProcessNames() {
		return this.processNames;
	}

	public String[] getSeverityLevels() {
		return this.severityLevels;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public Date getStartSearch() {
		return this.startSearch;
	}

	public void reset() {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Date today = new Date();
		endDate = formatter.format(new Date(today.getTime() + 24 * 3600 * 1000L));
		startDate = formatter.format(today);
		
		startSearch = null;
		endSearch = null;
		processNames = null;
		severityLevels = null;
		messageText = null;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void setEndSearch(Date endSearch) {
		this.endSearch = endSearch;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText.trim();
	}

	public void setProcessNames(String[] processNames) {
		this.processNames = processNames;
	}

	public void setSeverityLevels(String[] severityLevels) {
		this.severityLevels = severityLevels;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public void setStartSearch(Date startSearch) {
		this.startSearch = startSearch;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		Date date = DateUtility.processInputDate(startDate);
		if (date == null) {
			errors.add("startDate", new ActionError("error.date.invalid",
					"start"));
		} else {
			startSearch = date;
		}

		date = DateUtility.processInputDate(endDate, 23, 59, 59);
		if (date == null) {
			errors.add("endDate", new ActionError("error.date.invalid", "end"));
		} else {
			endSearch = date;
		}

		return errors;
	}
}
