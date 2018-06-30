package com.quikj.application.communicator.applications.webtalk.controller;

import java.net.URLEncoder;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.LabelValueBean;

import com.quikj.ace.db.webtalk.model.GroupBean;
import com.quikj.application.communicator.admin.controller.SpringUtils;
import com.quikj.server.framework.FormUtil;

/**
 * 
 * @author bhm
 */
public class CannedMessageManagementForm extends ActionForm {

	private static final long serialVersionUID = 1320944203940270365L;

	private long id;

	private String submit;

	private String group;

	private String description;

	private String message;

	private ArrayList<LabelValueBean> userGroups = new ArrayList<LabelValueBean>();

	public CannedMessageManagementForm() {
		reset();
	}

	public String getDescription() {
		return this.description;
	}

	public String getGroup() {
		return this.group;
	}

	public long getId() {
		return this.id;
	}

	public String getMessage() {
		return this.message;
	}

	public String getSubmit() {
		return this.submit;
	}

	public ArrayList<LabelValueBean> getUserGroups() {
		return this.userGroups;
	}

	public void reset() {
		id = 0L;
		group = null;
		description = null;
		message = null;
		submit = "Find";
	}

	public void setDescription(String description) {
		this.description = description.trim();
	}

	public void setGroup(String group) {
		this.group = group.trim();
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMessage(String message) {
		this.message = message.trim();
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public void setUserGroups(ArrayList<LabelValueBean> userGroups) {
		this.userGroups = userGroups;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		try {
			if (submit.equals("Modify") || submit.equals("Delete")) {
				if (id <= 0L) {
					errors.add("id", new ActionError(
							"error.cannedmessage.invalid.id"));
				}
			}

			// for create or modify
			if (submit.equals("Create") || submit.equals("Modify")) {
				if (group == null || group.isEmpty()) {
					errors.add("group", new ActionError(
							"error.cannedmessage.no.group"));
				}

				if (description == null || description.trim().isEmpty()) {
					errors.add("description", new ActionError(
							"error.cannedmessage.no.description"));
				}

				message = message.trim();
				if (message == null || message.trim().isEmpty()) {
					errors.add("message", new ActionError(
							"error.cannedmessage.no.content"));
				}
				
				String formError = FormUtil.validateForm(message);
				if (formError != null && !formError.isEmpty()) {
					errors.add("message", new ActionError(
							"error.cannedmessage.form.error", formError));
				}
			}

			if (errors.size() > 0) {
				GroupBean groupBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), GroupBean.class);
				ArrayList<LabelValueBean> list = GroupManagementAction
						.populateGroups(groupBean, null);
				list.add(0,
						new LabelValueBean("", URLEncoder.encode("", "UTF-8")));
				list.add(
						1,
						new LabelValueBean("all", URLEncoder.encode("all",
								"UTF-8")));
				setUserGroups(list);
			}
		} catch (Exception e) {
			errors.add("id", new ActionError("error.internal.error"));
		}
		return errors;
	}
}