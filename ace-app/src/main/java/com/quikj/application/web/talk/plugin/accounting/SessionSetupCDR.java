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
public class SessionSetupCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_SETUP_CDR_TABLE_NAME = "cdr_session_setup_tbl";

	private String identifier;
	private static String hostName = null;
	private static int counter = 0;
	private static Object counterLock = new Object();

	private Date timestamp;
	private String calling;
	private String called;
	private String transferId;

	public SessionSetupCDR(String callingId, String calledId, String transferId) {
		timestamp = new Date();
		calling = callingId;
		called = calledId;
		this.transferId = transferId;

		if (hostName == null) {
			try {
				hostName = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException ex) {
				hostName = "unknown";
			}
		}

		synchronized (counterLock) {
			identifier = hostName + ":session:" + timestamp.getTime() + ":"
					+ counter++;
		}
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + SESSION_SETUP_CDR_TABLE_NAME
				+ " values (?, ?, ?, ?, ?)", null, identifier, calling, called,
				transferId == null ? "" : transferId, timestamp);
	}

	@Override
	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}

	public String getIdentifier() {
		return identifier;
	}
}
