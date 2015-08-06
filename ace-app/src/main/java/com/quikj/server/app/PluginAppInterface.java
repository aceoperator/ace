package com.quikj.server.app;

import java.util.Properties;

import com.quikj.server.framework.AceException;

public interface PluginAppInterface {
	void applicationInit(Properties params) throws AceException;
	void dispose();
	PluginAppClientInterface newEndpointInstance() throws AceException;
}
