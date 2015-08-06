/**
 * 
 */
package com.quikj.ace.web.client.comm;

import com.quikj.ace.messages.vo.app.RequestMessage;

/**
 * @author amit
 * 
 */
public interface RequestListener {

	public void connected();
	
	public void requestReceived(int reqId, String contentType,
			RequestMessage req);
	
	public void disconnected();
}
