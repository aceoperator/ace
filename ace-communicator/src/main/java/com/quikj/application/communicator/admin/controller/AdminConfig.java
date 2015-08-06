/*
 * AdminConfig.java
 *
 * Created on April 13, 2003, 10:20 AM
 */

package com.quikj.application.communicator.admin.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author bhm
 */
public class AdminConfig {

	private int logGroup;

	private String processName;

	private int processInstance;

	private MenuLinks menuProperties;
	
	private String remoteUrl;
	
	private String remoteHost;
	
	private String remoteService;
	
	private List<ApplicationElement> applications = new ArrayList<ApplicationElement>();
	
	private Properties applicationProperties = new Properties();

	public AdminConfig() {
	}
	
	public MenuLinks getMenuProperties() {
		return menuProperties;
	}
	
	public void setMenuProperties(MenuLinks menuProperties) {
		this.menuProperties = menuProperties;
	}

	public int getLogGroup() {
		return logGroup;
	}

	public void setLogGroup(int logGroup) {
		this.logGroup = logGroup;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(int processInstance) {
		this.processInstance = processInstance;
	}

	public String getRemoteUrl() {
		return remoteUrl;
	}

	public void setRemoteUrl(String remoteUrl) {
		this.remoteUrl = remoteUrl;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public String getRemoteService() {
		return remoteService;
	}

	public void setRemoteService(String remoteService) {
		this.remoteService = remoteService;
	}

	public List<ApplicationElement> getApplications() {
		return applications;
	}

	public void setApplications(List<ApplicationElement> applications) {
		this.applications = applications;
	}

	public Properties getApplicationProperties() {
		return applicationProperties;
	}

	public void setApplicationProperties(Properties applicationProperties) {
		this.applicationProperties = applicationProperties;
	}
}
