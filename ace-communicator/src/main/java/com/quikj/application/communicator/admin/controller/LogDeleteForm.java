/*
 * LogDeleteForm.java
 *
 * Created on June 4, 2003, 11:06 AM
 */

package com.quikj.application.communicator.admin.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class LogDeleteForm extends ActionForm {

	private static final long serialVersionUID = 5913297754611845406L;

	private String priorToInput;

	private Date priorToDate;

	public LogDeleteForm() {
		reset();
	}

	public Date getPriorToDate() {
		return this.priorToDate;
	}

	public String getPriorToInput() {
		return this.priorToInput;
	}

	public void reset() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);

		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");

		priorToInput = formatter.format(cal.getTime());
		priorToDate = null;
	}

	public void setPriorToDate(Date priorToDate) {
		this.priorToDate = priorToDate;
	}

	public void setPriorToInput(String priorToInput) {
		this.priorToInput = priorToInput;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		Date date = DateUtility.processInputDate(priorToInput);
		if (date == null) {
			errors.add("priorToInput",
					new ActionError("error.date.invalid", ""));
		} else {
			priorToDate = date;
		}

		return errors;
	}
}
