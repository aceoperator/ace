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
public class SessionDisconnectCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_DISC_CDR_TABLE_NAME = "cdr_session_disc_tbl";

	private String session;
	private int code;
	private Date timestamp;

	public SessionDisconnectCDR(String session, int code) {
		timestamp = new Date();
		this.session = session;
		this.code = code;
	}

	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + SESSION_DISC_CDR_TABLE_NAME
				+ " values (?, ?, ?)", null, session, code, timestamp);
	}

	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
