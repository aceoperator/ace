/**
 * 
 */
package com.quikj.ace.web.client.comm;

import java.util.Date;

/**
 * @author amit
 *
 */
public class RequestInfo {

	private int requestId;
	private boolean multiple;
	private Date timeout;
	private ResponseListener listener;
	
	public RequestInfo() {
	}

	public RequestInfo(int requestId, boolean multiple, Date timeout,
			ResponseListener listener) {
		super();
		this.requestId = requestId;
		this.multiple = multiple;
		this.timeout = timeout;
		this.listener = listener;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public boolean isMultiple() {
		return multiple;
	}

	public void setMultiple(boolean multiple) {
		this.multiple = multiple;
	}

	public Date getTimeout() {
		return timeout;
	}

	public void setTimeout(Date timeout) {
		this.timeout = timeout;
	}

	public ResponseListener getListener() {
		return listener;
	}

	public void setListener(ResponseListener listener) {
		this.listener = listener;
	}
}
