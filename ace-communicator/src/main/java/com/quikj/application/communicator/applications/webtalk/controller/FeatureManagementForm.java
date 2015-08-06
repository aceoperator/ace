/*
 * FeatureManagementForm.java
 *
 * Form Bean for generic, or 'Other' features
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author bhm
 */
public class FeatureManagementForm extends ActionForm {

	private static final long serialVersionUID = 6599580717588582564L;

	private String name;

	private String submit = "Find";

	private String className;

	private boolean active = false;

	private String params;

	private HashMap<String, String> paramsList;

	public FeatureManagementForm() {
	}

	public String getClassName() {
		return this.className;
	}

	public String getName() {
		return this.name;
	}

	public String getParams() {
		return this.params;
	}

	public HashMap<String, String> getParamsList() {
		return this.paramsList;
	}

	public String getSubmit() {
		return this.submit;
	}

	public boolean isActive() {
		return this.active;
	}

	private void processFeatureParams(String input, ActionErrors errors) {
		/*
		 * USER INPUT RULES:
		 * 
		 * zero or more pairs of: key = value (spaces around '=' doesn't matter)
		 * if > one pair, pairs must be separated by <CR> or newline <CR> or
		 * newline not allowed in any key or any value no duplicate keys allowed
		 * if key or value contains: =, it must be escaped as: &eq; if key or
		 * value contains: &, it must be escaped as: &amp;
		 */

		if (input != null) {
			input = input.trim();
			if (input.length() > 0) {
				StringTokenizer strtok = new StringTokenizer(input, "\n");
				int num_pairs = strtok.countTokens();

				paramsList = new HashMap<String, String>();

				for (int i = 0; i < num_pairs; i++) {
					String pair = strtok.nextToken();

					StringTokenizer pairtok = new StringTokenizer(pair, "=",
							true);
					int num_subparms = pairtok.countTokens();

					if (num_subparms != 3) {
						errors.add("params", new ActionError(
								"error.feature.params.pairtokens", new Integer(
										i + 1)));

						continue;
					}

					String input_key = pairtok.nextToken().trim();
					pairtok.nextToken();
					String input_value = pairtok.nextToken().trim();

					String key = Utilities.deEscapeEqual(input_key);
					String value = Utilities.deEscapeEqual(input_value);

					if (paramsList.containsKey(key) == true) {
						errors.add("params", new ActionError(
								"error.feature.params.duplicatekey",
								new Integer(i + 1)));

						continue;
					}

					paramsList.put(key, value);
				}
			}
		}
	}

	public void reset() {
		name = null;
		submit = "Find";
		className = null;
		active = false;
		params = null;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setName(String name) {
		this.name = name.trim();
	}

	public void setParams(String params) {
		this.params = params;
	}

	public void setParamsList(HashMap<String, String> paramsList) {
		this.paramsList = paramsList;
		params = null;

		if (paramsList != null) {
			StringBuilder buf = new StringBuilder();

			for (Entry<String, String> i : paramsList.entrySet()) {
				if (buf.length() > 0) {
					buf.append('\n');
				}

				String key = i.getKey();
				String value = i.getValue();
				buf.append(Utilities.escapeEqual(key) + "="
						+ Utilities.escapeEqual(value));
			}

			params = buf.toString();
		}
	}

	public void setSubmit(String submit) {
		this.submit = submit;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		// Check for mandatory data
		ActionErrors errors = new ActionErrors();

		if ((name == null) || (name.length() == 0)) {
			errors.add("name", new ActionError("error.feature.no.name"));
		}

		if (DataCheckUtility.followsTableIdRules(name) == false) {
			errors.add("name", new ActionError("error.feature.invalid.id"));
		}

		if (DataCheckUtility.followsBlankSpaceRules(name) == false) {
			errors.add("name", new ActionError("error.feature.invalid.id"));
		}

		// general checks for create/modify
		if ((submit.equals("Create") == true)
				|| (submit.equals("Modify") == true)) {
			if ((className == null) || (className.length() == 0)) {
				errors.add("className", new ActionError(
						"error.feature.no.className"));
			}

			// validate params input, convert to featureParams
			processFeatureParams(getParams(), errors);
		}

		return errors;
	}
}
