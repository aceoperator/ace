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
public class SessionSetupResponseCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_SETUP_RESP_CDR_TABLE_NAME = "cdr_session_setup_resp_tbl";

	private String session;
	private int status;
	private Date timestamp;
	private String called;

	public SessionSetupResponseCDR(String session, String calledName, int status) {
		timestamp = new Date();
		this.session = session;
		called = calledName;
		this.status = status;
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + SESSION_SETUP_RESP_CDR_TABLE_NAME
				+ " values (?, ?, ?, ?)", null, session, called, status,
				timestamp);
	}

	@Override
	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
