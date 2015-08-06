/*
 * FileAttribute.java
 *
 * Created on April 8, 2003, 3:46 AM
 */

package com.quikj.application.web.talk.plugin;

/**
 * 
 * @author amit
 */
public class FileAttribute {

	/** Holds value of property path. */
	private String path;

	/** Holds value of property checked. */
	private boolean checked = false;

	/** Creates a new instance of FileAttribute */
	public FileAttribute(String path) {
		setPath(path);
	}

	/**
	 * Getter for property path.
	 * 
	 * @return Value of property path.
	 * 
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Getter for property check.
	 * 
	 * @return Value of property check.
	 * 
	 */
	public boolean isChecked() {
		return this.checked;
	}

	/**
	 * Setter for property check.
	 * 
	 * @param check
	 *            New value of property check.
	 * 
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/**
	 * Setter for property path.
	 * 
	 * @param path
	 *            New value of property path.
	 * 
	 */
	public void setPath(String path) {
		this.path = path;
	}

}
