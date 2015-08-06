/*
 * LinkAttribute.java
 *
 * Created on March 26, 2003, 5:27 PM
 */

package com.quikj.application.communicator.admin.controller;

/**
 * 
 * @author amit
 */
public class LinkAttribute {

	/** Holds value of property name. */
	private String name;

	/** Holds value of property link. */
	private String link;

	/** Creates a new instance of LinkAttribute */
	public LinkAttribute() {
	}

	public LinkAttribute(String name, String link) {
		this.name = name;
		this.link = link;
	}

	/**
	 * Getter for property link.
	 * 
	 * @return Value of property link.
	 * 
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 * Getter for property name.
	 * 
	 * @return Value of property name.
	 * 
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Setter for property link.
	 * 
	 * @param link
	 *            New value of property link.
	 * 
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Setter for property name.
	 * 
	 * @param name
	 *            New value of property name.
	 * 
	 */
	public void setName(String name) {
		this.name = name;
	}

}
