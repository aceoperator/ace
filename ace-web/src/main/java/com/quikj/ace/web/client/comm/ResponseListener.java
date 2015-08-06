/**
 * 
 */
package com.quikj.ace.web.client.comm;

import com.quikj.ace.messages.vo.app.ResponseMessage;

/**
 * @author amit
 * 
 */
public interface ResponseListener {

	public void responseReceived(int requestId, String contentType,
			ResponseMessage message);

	public void timeoutOccured(int requestId);
}
