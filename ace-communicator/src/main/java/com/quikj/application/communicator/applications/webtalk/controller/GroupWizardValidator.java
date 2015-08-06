/*
 * GroupWizardValidator.java
 *
 * Created on March 6, 2004, 11:05 PM
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
public class GroupWizardValidator {

	/** Creates a new instance of GroupWizardValidator */
	public GroupWizardValidator() {
	}

	public static boolean validateCompanyNickname(Object bean,
			ValidatorAction va, Field field, ActionErrors errors,
			HttpServletRequest request) {
		DynaValidatorForm form = (DynaValidatorForm) bean;

		// Get the submit value that requires this field to be validated
		String validate_if = field.getVarValue("submit");

		// See if the form submit equals that
		if (((String) form.get("submit")).equals(validate_if) == true) {
			// Get the name of the field being validated
			String name = field.getProperty();

			if (name.equals("companyNickname") == false) {
				return false;
			}

			// Get the data in the field
			String value = ((String) form.get(name)).trim();

			if (value.equals("all") == true) {
				// cannot have a group with name = 'all'
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}

			if (DataCheckUtility.followsTableIdRules(value) == false) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}
			
			if (DataCheckUtility.followsBlankSpaceRules(value) == false) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}
		}

		return true;
	}

	public static boolean validateCompanyUrl(Object bean, ValidatorAction va,
			Field field, ActionErrors errors, HttpServletRequest request) {
		DynaValidatorForm form = (DynaValidatorForm) bean;

		// Get the submit value that requires this field to be validated
		String validate_if = field.getVarValue("submit");

		// See if the form submit equals that
		if (((String) form.get("submit")).equals(validate_if) == true) {
			// Get the name of the field being validated
			String name = field.getProperty();

			if (name.equals("companyUrl") == false) {
				return false;
			}

			// Get the data in the field
			String value = ((String) form.get(name)).trim();

			if (value.length() < ("http://".length() + 5)) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}
		}

		return true;
	}

	public static boolean validateFeatureMaximums(Object bean,
			ValidatorAction va, Field field, ActionErrors errors,
			HttpServletRequest request) {
		DynaValidatorForm form = (DynaValidatorForm) bean;

		// Get the submit value that requires this field to be validated
		String validate_if = field.getVarValue("submit");

		// See if the form submit equals that
		if (((String) form.get("submit")).equals(validate_if) == true) {
			// Get the data in the field being checked
			Integer value = (Integer) form.get(field.getProperty());

			if (value == null) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));
				return false;
			}

			if (value.intValue() <= 0) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));
				return false;
			}

		}

		return true;
	}

	public static boolean validateGroupName(Object bean, ValidatorAction va,
			Field field, ActionErrors errors, HttpServletRequest request) {
		DynaValidatorForm form = (DynaValidatorForm) bean;

		// Get the submit value that requires this field to be validated
		String validate_if = field.getVarValue("submit");

		// See if the form submit equals that
		if (((String) form.get("submit")).equals(validate_if) == true) {
			// Get the name of the field being validated
			String name = field.getProperty();

			if (name.equals("groupName") == false) {
				return false;
			}

			// Get the data in the field
			String value = ((String) form.get(name)).trim();

			if (DataCheckUtility.followsTableIdRules(value) == false) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}
			
			if (DataCheckUtility.followsBlankSpaceRules(value) == false) {
				errors.add(field.getKey(), Resources.getActionError(request,
						va, field));

				return false;
			}
		}

		return true;
	}

}
