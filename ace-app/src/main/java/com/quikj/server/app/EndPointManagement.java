package com.quikj.server.app;

import com.quikj.server.framework.AceLogger;

public class EndPointManagement implements EndPointManagementMBean {

	public EndPointManagement() {
	}

	@Override
	public void setTrace(String enabled) {
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"EndPointManagement.setTrace() --  " + enabled);
		RemoteEndPoint.setTrace(Boolean.valueOf(enabled));
	}

	@Override
	public String getTrace() {
		return Boolean.toString(RemoteEndPoint.isTrace());
	}

	@Override
	public void setTraceMessage(String enabled) {
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				"EndPointManagement.setTraceMessage() --  " + enabled);
		RemoteEndPoint.setTraceMessage(Boolean.valueOf(enabled));
	}

	@Override
	public String getTraceMessage() {
		return Boolean.toString(RemoteEndPoint.isTraceMessage());
	}

	@Override
	public String getEndpointCount() {
		return Integer.toString(RemoteEndPoint.getEndpointCount());
	}
}
