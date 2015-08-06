package com.quikj.application.web.talk.plugin;

import com.quikj.server.app.EndPointInterface;
import com.quikj.server.framework.AceSQLMessage;

public interface DbOperationInterface {
	public abstract void cancel();

	public abstract EndPointInterface getEndPoint();

	public abstract boolean processResponse(AceSQLMessage message);
}
