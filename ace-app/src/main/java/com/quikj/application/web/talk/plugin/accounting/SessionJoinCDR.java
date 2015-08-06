/*
 * SessionCDR.java
 *
 * Created on June 29, 2002, 3:38 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.util.ArrayList;
import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class SessionJoinCDR implements CDRInterface {
	// database table name constant
	private static final String SESSION_JOIN_CDR_TABLE_NAME = "cdr_session_join_tbl";

	private ArrayList<String> sessions = new ArrayList<String>();
	private Date timestamp;

	public SessionJoinCDR() {
		this(null);
	}

	public SessionJoinCDR(String session) {
		timestamp = new Date();

		if (session != null) {
			sessions.add(session);
		}
	}

	public void addSession(String session) {
		sessions.add(session);
	}

	@Override
	public SQLParam generateSQLCDR() {
		StringBuilder builder = new StringBuilder();
		String first = null;
		for (String session : sessions) {
			if (builder.length() > 0) {
				builder.append(',');
			} else {
				first = session;
			}

			builder.append(session);
		}

		return new SQLParam("insert into " + SESSION_JOIN_CDR_TABLE_NAME
				+ " values (?, ?, ?)", null, first, timestamp,
				builder.toString());
	}

	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
