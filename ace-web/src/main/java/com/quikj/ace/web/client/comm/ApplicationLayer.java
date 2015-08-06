package com.quikj.ace.web.client.comm;

import java.util.List;

import com.quikj.ace.messages.vo.app.Message;

public interface ApplicationLayer {

	public abstract void disconnected();

	public abstract void connected();

	public boolean isFastPoll();

	public void processIncomingMessages(List<Message> messages);

	public void setTransport(TransportLayer transport);

}