/*
 * RegisteredUserLogoutCDR.java
 *
 * Created on June 28, 2002, 12:38 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class LogoutCDR implements CDRInterface {

	// database table name constant
	private static final String LOGOUT_CDR_TABLE_NAME = "cdr_logout_tbl";

	private String loginId;

	private Date timestamp;

	public LogoutCDR(String loginid) {
		loginId = loginid;
		timestamp = new Date();
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + LOGOUT_CDR_TABLE_NAME
				+ " values (?, ?)", null, loginId, timestamp);
	}

	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
