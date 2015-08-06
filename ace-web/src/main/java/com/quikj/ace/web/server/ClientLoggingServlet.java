package com.quikj.ace.web.server;

import java.util.logging.LogRecord;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import com.google.gwt.logging.shared.RemoteLoggingService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.quikj.server.framework.AceLogger;

/**
 * Server side code for the remote log handler.
 */
public class ClientLoggingServlet extends RemoteServiceServlet implements
		RemoteLoggingService {
	private static final long serialVersionUID = 7079221031826198234L;

	public final String logOnServer(LogRecord lr) {
		HttpServletRequest request = getThreadLocalRequest();
		
		String cookieValue = null;
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (c.getName().equals(AceOperatorServiceImpl.ACE_ENDUSER_COOKIE_NAME)) {
					cookieValue = c.getValue();
					break;
				}
			}
		}
		
		String userAgent = request.getHeader("User-Agent");
		
		StringBuffer buffer = new StringBuffer("Log from client: ");
		buffer.append(cookieValue == null?"Unknown":cookieValue);
		
		buffer.append("\nUser Agent: ");
		buffer.append(userAgent == null?"Unknown":userAgent);
		
		buffer.append("\nLevel: ");
		buffer.append(lr.getLevel().getName());
		
		buffer.append("\nLogger: ");
		buffer.append(lr.getLoggerName());
		
		buffer.append("\nMessage: ");
		buffer.append(lr.getMessage());
		
		if (lr.getThrown() != null) {
			buffer.append("\nException: ");
			buffer.append(lr.getThrown().getClass().getName());
			buffer.append(" - ");
			buffer.append(lr.getThrown().getMessage());
		}
		
		AceLogger.Instance().log(AceLogger.INFORMATIONAL, AceLogger.SYSTEM_LOG,
				buffer.toString());

		return null;
	}
}