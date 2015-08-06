/*
 * RelatedTasks.java
 *
 * Created on March 26, 2003, 5:45 PM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.ArrayList;

/**
 * 
 * @author amit
 */
public class RelatedTasks {

	/** Holds value of property links. */
	private ArrayList links = new ArrayList();

	/** Creates a new instance of RelatedTasks */
	public RelatedTasks() {
	}

	public void addLink(LinkAttribute link) {
		links.add(link);
	}

	/**
	 * Indexed getter for property links.
	 * 
	 * @param index
	 *            Index of the property.
	 * @return Value of the property at <CODE>index</CODE>.
	 * 
	 */
	public LinkAttribute getLink(int index) {
		return (LinkAttribute) this.links.get(index);
	}

	/**
	 * Getter for property links.
	 * 
	 * @return Value of property links.
	 * 
	 */
	public ArrayList getLinks() {
		return this.links;
	}

	/**
	 * Indexed setter for property links.
	 * 
	 * @param index
	 *            Index of the property.
	 * @param links
	 *            New value of the property at <CODE>index</CODE>.
	 * 
	 */
	public void setLink(int index, LinkAttribute link) {
		links.set(index, links);
	}

	/**
	 * Setter for property links.
	 * 
	 * @param links
	 *            New value of property links.
	 * 
	 */
	public void setLinks(ArrayList links) {
		this.links = links;
	}
}
