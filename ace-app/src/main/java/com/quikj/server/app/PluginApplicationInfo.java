package com.quikj.server.app;

import java.util.Properties;

class PluginApplicationInfo {
	
	private String clazz;
	private Properties properties;
	private int id;
	private PluginAppInterface obj;
	
	public String getClazz() {
		return clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public PluginAppInterface getObj() {
		return obj;
	}
	public void setObj(PluginAppInterface obj) {
		this.obj = obj;
	}
}