/*
 * ApplicationElement.java
 *
 * Created on April 13, 2003, 9:58 AM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.Properties;

/**
 * 
 * @author bhm
 */
public class ApplicationElement {

	private String name;

	private String initClass;

	private String displayName;

	private String forwardName;

	private Properties params = new Properties();

	public String getDisplayName() {
		return this.displayName;
	}

	public String getForwardName() {
		return forwardName;
	}

	public String getInitClass() {
		return initClass;
	}

	public String getName() {
		return name;
	}

	public Properties getParams() {
		return params;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setForwardName(String forwardName) {
		this.forwardName = forwardName;
	}

	public void setInitClass(String initClass) {
		this.initClass = initClass;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParams(Properties params) {
		this.params = params;
	}
}
