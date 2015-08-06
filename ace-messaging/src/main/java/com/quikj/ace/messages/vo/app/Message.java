/**
 * 
 */
package com.quikj.ace.messages.vo.app;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author amit
 *
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 7642962220630265144L;
	
	public static final String SESSION_ID_HEADER = "session-id";
	
	public static final String PLUGIN_APP_ID = "Plugin-Id";

	public static final String CORRELATION_ID = "Correlation-Id";

	public static final String CONTENT_TYPE = "Content-Type";

	public static final String CONTENT_TYPE_XML = "application/xml";
	
	protected String version;
	
	protected Map<String, String> headers = new HashMap<String, String>();
	
	protected WebMessage message;
	
	public Message() {
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public WebMessage getMessage() {
		return message;
	}

	public void setMessage(WebMessage message) {
		this.message = message;
	}
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
