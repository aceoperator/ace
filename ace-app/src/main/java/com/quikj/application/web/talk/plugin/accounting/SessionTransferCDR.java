/*
 * SessionCDR.java
 *
 * Created on June 29, 2002, 3:38 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;


/**
 * 
 * @author amit
 */
public class SessionTransferCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_TRANSFER_CDR_TABLE_NAME = "cdr_session_transfer_tbl";

	private String session;
	private String to;
	private Date timestamp;

	private String identifier;
	private static String hostName = null;
	private static int counter = 0;
	private static Object counterLock = new Object();

	public SessionTransferCDR(String session, String to) {
		timestamp = new Date();
		this.session = session;
		this.to = to;

		if (hostName == null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				hostName = "unknown";
			}
		}

		synchronized (counterLock) {
			identifier = hostName + ":transfer:" + timestamp.getTime() + ":"
					+ counter++;
		}
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + SESSION_TRANSFER_CDR_TABLE_NAME
				+ " values (?, ?, ?, ?)", null, session, to, identifier, timestamp);
	}

	@Override
	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}

	public String getIdentifier() {
		return identifier;
	}
}
