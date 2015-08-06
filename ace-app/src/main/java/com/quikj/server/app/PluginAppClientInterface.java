package com.quikj.server.app;

import com.quikj.ace.messages.vo.app.WebMessage;
import com.quikj.server.framework.AceMessageInterface;

public interface PluginAppClientInterface {
	public abstract void connectionClosed(String reasonText);

	public abstract boolean eventReceived(AceMessageInterface event);

	public abstract boolean newConnection(String host, String endUserCookie, RemoteEndPoint parent);

	public abstract boolean requestReceived(int request_id,
			String content_type, WebMessage body);

	public abstract boolean responseReceived(int request_id, int status,
			String reason, String content_type, WebMessage body);
}
