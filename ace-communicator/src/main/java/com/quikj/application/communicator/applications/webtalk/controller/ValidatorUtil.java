/*
 * ValidatorUtil.java
 *
 * Created on March 20, 2004, 3:13 PM
 *
 * This class contains generic validator methods to make up for current Struts release validator bugs/deficiences.
 *
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.validator.Field;
import org.apache.commons.validator.ValidatorAction;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.validator.DynaValidatorForm;
import org.apache.struts.validator.Resources;

/**
 * 
 * @author bhm
 */
public class ValidatorUtil {

	/** Creates a new instance of ValidatorUtil */
	public ValidatorUtil() {
	}

	public static boolean validateStringRequiredIf(
			// not needed starting Struts 1.2 (requiredif will be fixed and also
			// validwhen is available)
			Object bean, ValidatorAction va, Field field, ActionErrors errors,
			HttpServletRequest request) {
		DynaValidatorForm form = (DynaValidatorForm) bean;

		// Get the submit value that requires this field to be there
		String required_if = field.getVarValue("submit");

		// See if the form submit equals that
		if (((String) form.get("submit")).equals(required_if) == true) {
			// Get the data in the field being checked
			String value = (String) form.get(field.getProperty());

			if (value == null) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));
				return false;
			}

			if (value.trim().length() <= 0) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));
				return false;
			}
		}

		return true;
	}

}
