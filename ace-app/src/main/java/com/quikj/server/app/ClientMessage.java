/**
 * 
 */
package com.quikj.server.app;

import com.quikj.ace.messages.vo.app.Message;
import com.quikj.server.framework.AceMessageInterface;

/**
 * @author amit
 *
 */
public class ClientMessage implements AceMessageInterface {

	private static final String MESSAGE_TYPE = "ClientMessage";

	private Message message;	
	
	public ClientMessage() {
	}
	
	public ClientMessage(Message message) {
		this.message = message;
	}

	public String messageType() {
		return MESSAGE_TYPE;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
