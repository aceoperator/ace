/*
 * SessionCDR.java
 *
 * Created on June 29, 2002, 3:38 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;


/**
 * 
 * @author amit
 */
public class SessionLeaveCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_LEAVE_CDR_TABLE_NAME = "cdr_session_leave_tbl";

	private String session;
	private String endpoint;
	private Date timestamp;

	public SessionLeaveCDR(String session, String endpoint) {
		timestamp = new Date();
		this.session = session;
		this.endpoint = endpoint;
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + SESSION_LEAVE_CDR_TABLE_NAME
				+ " values (?, ?, ?)", null, session, endpoint, timestamp);
	}

	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}