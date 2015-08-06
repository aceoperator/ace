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
public class ResponseMessage extends Message implements Serializable {
	
	private static final long serialVersionUID = 7179365322999174425L;
	
	public static final int NOT_ACCEPTABLE = 406;
	public static final int FORBIDDEN = 403;
	public static final int OK = 200;
	public static final int NO_CONTENT = 204;
	public static final int SERVICE_UNAVAILABLE = 503;
	public static final int INTERNAL_ERROR = 500;
	public static final int NOT_MODIFIED = 304;
	public static final int BAD_REQUEST = 400;
	
	private int status;
	private String reason;
	
	public ResponseMessage() {
	}

	public ResponseMessage(int status, String reason,
			Map<String, String> headers, String version, WebMessage message) {
		super();
		this.status = status;
		this.reason = reason;
		this.headers = headers;
		this.message = message;
		this.version = version;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
