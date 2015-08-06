package com.quikj.server.app.adapter;

import com.quikj.ace.messages.vo.app.Message;

public interface AppServerAdapter {

	public void sendMessage(String sessionId, Message message)
			throws AppServerAdapterException;

	public void endPointTerminated(String sessionId)
			throws AppServerAdapterException;

}