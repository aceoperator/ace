/*
 * FeatureTableElement.java
 *
 * Created on September 7, 2003, 9:44 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.HashMap;

/**
 * 
 * @author bhm
 */
public class FeatureTableElement {

	/** Holds value of property name. */
	private String name;

	/** Holds value of property className. */
	private String className;

	/** Holds value of property active. */
	private boolean active;

	/** Holds value of property params. */
	private HashMap params;

	/** Holds value of property domain. */
	private String domain;

	/** Creates a new instance of FeatureTableElement */
	public FeatureTableElement() {
	}

	/**
	 * Getter for property className.
	 * 
	 * @return Value of property className.
	 * 
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Getter for property domain.
	 * 
	 * @return Value of property domain.
	 * 
	 */
	public String getDomain() {
		return this.domain;
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
	 * Getter for property params.
	 * 
	 * @return Value of property params.
	 * 
	 */
	public HashMap getParams() {
		return this.params;
	}

	/**
	 * Getter for property active.
	 * 
	 * @return Value of property active.
	 * 
	 */
	public boolean isActive() {
		return this.active;
	}

	/**
	 * Setter for property active.
	 * 
	 * @param active
	 *            New value of property active.
	 * 
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Setter for property className.
	 * 
	 * @param className
	 *            New value of property className.
	 * 
	 */
	public void setClassName(String className) {
		this.className = className;
	}

	/**
	 * Setter for property domain.
	 * 
	 * @param domain
	 *            New value of property domain.
	 * 
	 */
	public void setDomain(String domain) {
		this.domain = domain;
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

	/**
	 * Setter for property params.
	 * 
	 * @param params
	 *            New value of property params.
	 * 
	 */
	public void setParams(HashMap params) {
		this.params = params;
	}

}
