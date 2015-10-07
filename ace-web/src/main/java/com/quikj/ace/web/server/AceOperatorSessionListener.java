/**
 * 
 */
package com.quikj.ace.web.server;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.quikj.server.framework.AceLogger;

/**
 * @author amit
 *
 */
public class AceOperatorSessionListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent sessionEvent) {
		AceLogger
		.Instance()
		.log(AceLogger.INFORMATIONAL,
				AceLogger.SYSTEM_LOG,
				"Session created");

	}

	@Override
	public void sessionDestroyed(HttpSessionEvent sessionEvent) {
		AceLogger
		.Instance()
		.log(AceLogger.INFORMATIONAL,
				AceLogger.SYSTEM_LOG,
				"Session destroyed");
	}
}
