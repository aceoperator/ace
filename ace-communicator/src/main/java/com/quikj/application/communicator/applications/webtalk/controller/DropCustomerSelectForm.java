/*
 * DropCustomerSelectForm.java
 *
 * Created on April 24, 2004, 1:02 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

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
 * @author bhm
 */
public class DropCustomerSelectForm extends ActionForm {

	private static final long serialVersionUID = -1749004130003186161L;

	private String submit;

	private String domain;

	private ArrayList<LabelValueBean> domains = new ArrayList<LabelValueBean>();

	public DropCustomerSelectForm() {
	}

	public String getDomain() {
		return this.domain;
	}

	public ArrayList<LabelValueBean> getDomains() {
		return this.domains;
	}

	public String getSubmit() {
		return this.submit;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setDomains(ArrayList<LabelValueBean> domains) {
		this.domains = domains;
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		if (submit.startsWith("Cancel") == true) {
			return null;
		}

		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		try {
			if ((domain == null) || (domain.length() == 0)) {
				errors.add("domains", new ActionError(
						"error.customer.select.domain"));
			}

			if (!errors.isEmpty()) {
				GroupBean groupBean = SpringUtils.getBean(request.getSession()
						.getServletContext(), GroupBean.class);

				List<String> domains = groupBean.listDomains();
				ArrayList<LabelValueBean> list = new ArrayList<LabelValueBean>();
				for (String dom: domains) {
					if (!dom.equals("ace")) {
						list.add(new LabelValueBean(dom, URLEncoder.encode(dom,
								"UTF-8")));
					}
				}

				setDomains(list);
			}
		} catch (Exception e) {
			errors.add("domains", new ActionError("error.internal.error"));
		}
		return errors;
	}
}