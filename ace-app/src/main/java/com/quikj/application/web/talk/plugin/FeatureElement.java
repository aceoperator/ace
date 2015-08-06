/*
 * FeatureElement.java
 *
 * Created on May 8, 2002, 3:09 AM
 */

package com.quikj.application.web.talk.plugin;

import java.util.Map;

/**
 * 
 * @author amit
 */
public class FeatureElement {
	private String name;

	private String className;

	private Map map;

	private FeatureInterface obj;

	/** Creates a new instance of FeatureElement */
	public FeatureElement() {
	}

	public FeatureElement(String name, String class_name, Map map) {
		this.name = name;
		className = class_name;
		this.map = map;
	}

	/**
	 * Getter for property className.
	 * 
	 * @return Value of property className.
	 */
	public java.lang.String getClassName() {
		return className;
	}

	/**
	 * Getter for property map.
	 * 
	 * @return Value of property map.
	 */
	public java.util.Map getMap() {
		return map;
	}

	/**
	 * Getter for property name.
	 * 
	 * @return Value of property name.
	 */
	public java.lang.String getName() {
		return name;
	}

	/**
	 * Getter for property obj.
	 * 
	 * @return Value of property obj.
	 */
	public com.quikj.application.web.talk.plugin.FeatureInterface getObj() {
		return obj;
	}

	/**
	 * Setter for property className.
	 * 
	 * @param className
	 *            New value of property className.
	 */
	public void setClassName(java.lang.String className) {
		this.className = className;
	}

	/**
	 * Setter for property map.
	 * 
	 * @param map
	 *            New value of property map.
	 */
	public void setMap(java.util.Map map) {
		this.map = map;
	}

	/**
	 * Setter for property name.
	 * 
	 * @param name
	 *            New value of property name.
	 */
	public void setName(java.lang.String name) {
		this.name = name;
	}

	/**
	 * Setter for property obj.
	 * 
	 * @param obj
	 *            New value of property obj.
	 */
	public void setObj(
			com.quikj.application.web.talk.plugin.FeatureInterface obj) {
		this.obj = obj;
	}

}
