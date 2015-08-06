/**
 * 
 */
package com.quikj.ace.messages.vo.app;

import java.io.Serializable;
import java.util.Map;

/**
 * @author amit
 *
 */
public class RequestMessage extends Message implements Serializable {

	private static final long serialVersionUID = -6717989081558585603L;
	
	public static final String DISCONNECT_METHOD = "DISCONNECT";
	public static final String PING_METHOD = "PING";
	public static final String APPLICATION_METHOD = "APPLICATION"; 
	
	private String method;
	
	public RequestMessage() {
	}

	public RequestMessage(String method, String version,
			Map<String, String> headers, WebMessage message) {
		super();
		this.method = method;
		this.version = version;
		this.headers = headers;
		this.message = message;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}
}
