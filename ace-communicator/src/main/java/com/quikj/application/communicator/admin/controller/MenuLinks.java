/*
 * MenuLinks.java
 *
 * Created on March 24, 2003, 8:10 PM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @Source Vinod Batra
 */
public class MenuLinks {
	private String image;

	private String company;

	private String url;

	private List<Link> links = new ArrayList<Link>();;

	public void addLink(Link l) {
		links.add(l);
	}

	public String getCompany() {
		return company;
	}

	public String getImage() {
		return image;
	}

	public List<Link> getLinks() {
		return links;
	}

	public String getUrl() {
		return url;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
