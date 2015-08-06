package com.quikj.server.app;

import java.util.ArrayList;
import java.util.List;

public class ApplicationConfiguration {
	private int logGroup = 0;

	private String processName = "APP";

	private int processInstance = 0;

	private boolean registry = true;

	private int registryPort = 1099;

	private String registryURL = "rmi://localhost:1099";

	private String registryServiceName = "AceHTTPSRemoteAccess";
	
	private String mailHost = "localhost";
	
	private int mailPort = 25;
	
	private boolean mailEncrypt = false;
	
	private boolean mailDebug = false;
	
	private String mailUserName;
	
	private String mailPassword;
	
	private String mailPendingDir;
	
	private String mailPendingFile;
	
	private String mailOverrideFrom;

	private List<PluginApplicationInfo> plugins = new ArrayList<PluginApplicationInfo>();

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

	public boolean isRegistry() {
		return registry;
	}

	public void setRegistry(boolean registry) {
		this.registry = registry;
	}

	public int getRegistryPort() {
		return registryPort;
	}

	public void setRegistryPort(int registryPort) {
		this.registryPort = registryPort;
	}

	public String getRegistryURL() {
		return registryURL;
	}

	public void setRegistryURL(String registryURL) {
		this.registryURL = registryURL;
	}

	public String getRegistryServiceName() {
		return registryServiceName;
	}

	public void setRegistryServiceName(String registryServiceName) {
		this.registryServiceName = registryServiceName;
	}

	public List<PluginApplicationInfo> getPlugins() {
		return plugins;
	}

	public void setPlugins(List<PluginApplicationInfo> plugins) {
		this.plugins = plugins;
	}

	public String getMailHost() {
		return mailHost;
	}

	public void setMailHost(String mailHost) {
		this.mailHost = mailHost;
	}

	public int getMailPort() {
		return mailPort;
	}

	public void setMailPort(int mailPort) {
		this.mailPort = mailPort;
	}

	public boolean isMailEncrypt() {
		return mailEncrypt;
	}

	public void setMailEncrypt(boolean mailEncrypt) {
		this.mailEncrypt = mailEncrypt;
	}

	public boolean isMailDebug() {
		return mailDebug;
	}

	public void setMailDebug(boolean mailDebug) {
		this.mailDebug = mailDebug;
	}

	public String getMailUserName() {
		return mailUserName;
	}

	public void setMailUserName(String mailUserName) {
		this.mailUserName = mailUserName;
	}

	public String getMailPassword() {
		return mailPassword;
	}

	public void setMailPassword(String mailPassword) {
		this.mailPassword = mailPassword;
	}

	public String getMailPendingDir() {
		return mailPendingDir;
	}

	public void setMailPendingDir(String mailPendingDir) {
		this.mailPendingDir = mailPendingDir;
	}

	public String getMailPendingFile() {
		return mailPendingFile;
	}

	public void setMailPendingFile(String mailPendingFile) {
		this.mailPendingFile = mailPendingFile;
	}

	public String getMailOverrideFrom() {
		return mailOverrideFrom;
	}

	public void setMailOverrideFrom(String mailOverrideFrom) {
		this.mailOverrideFrom = mailOverrideFrom;
	}
}
