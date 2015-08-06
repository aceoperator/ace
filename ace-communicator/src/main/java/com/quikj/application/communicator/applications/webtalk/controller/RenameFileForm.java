/*
 * RenameFileForm.java
 *
 * Created on June 14, 2003, 9:44 PM
 */

package com.quikj.application.communicator.applications.webtalk.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author Vinod Batra
 */
public class RenameFileForm extends ActionForm {
	private String fileName;
	private String renameTo;

	/** Creates a new instance of RenameFileForm */
	public RenameFileForm() {
	}

	/**
	 * Getter for property fileName.
	 * 
	 * @return Value of property fileName.
	 * 
	 */
	public java.lang.String getFileName() {
		return fileName;
	}

	/**
	 * Getter for property renameTo.
	 * 
	 * @return Value of property renameTo.
	 * 
	 */
	public java.lang.String getRenameTo() {
		return renameTo;
	}

	/**
	 * Setter for property fileName.
	 * 
	 * @param fileName
	 *            New value of property fileName.
	 * 
	 */
	public void setFileName(java.lang.String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Setter for property renameTo.
	 * 
	 * @param renameTo
	 *            New value of property renameTo.
	 * 
	 */
	public void setRenameTo(java.lang.String renameTo) {
		this.renameTo = renameTo;
	}

	/** Creates a new instance of DeleteRenameFileForm */

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
		if (fileName == null || fileName.length() == 0) {
			errors.add("fileName", new ActionError("file.name.enter"));
		}

		if (renameTo == null || renameTo.length() == 0) {
			errors.add("fileName", new ActionError("file.name.enter"));
		}

		return errors;

	}

}
