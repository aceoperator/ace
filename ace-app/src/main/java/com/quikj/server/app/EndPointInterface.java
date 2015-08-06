package com.quikj.server.app;

import com.quikj.server.framework.AceMessageInterface;

public interface EndPointInterface {
	public static final String PARAM_SELF_INFO = "selfInfo";

	public String getIdentifier();

	public Object getParam(String key);

	public void removeParam(String key);

	public boolean sendEvent(AceMessageInterface message);

	public void setParam(String key, Object value);
}
