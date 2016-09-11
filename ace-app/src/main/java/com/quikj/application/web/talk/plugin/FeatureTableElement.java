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

	private String name;

	private String className;

	private boolean active;

	private HashMap params;

	private String domain;

	public FeatureTableElement() {
	}

	public String getClassName() {
		return this.className;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getName() {
		return this.name;
	}

	public HashMap getParams() {
		return this.params;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setParams(HashMap params) {
		this.params = params;
	}
}
