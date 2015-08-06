/*
 * UnregisteredUserLoginCDR.java
 *
 * Created on June 29, 2002, 1:56 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class UnregisteredUserLoginCDR implements CDRInterface {
	// database table name constant
	private static final String UNREGISTERED_LOGIN_CDR_TABLE_NAME = "cdr_unreg_login_tbl";

	private String loginId;
	private String name;
	private String email;
	private String additional;
	private Date timestamp;
	private String environment;
	private String ip;
	private String cookie;

	public UnregisteredUserLoginCDR(String id, String name, String email,
			String additional, String environment, String ip, String cookie) {
		loginId = id;
		this.email = email;
		this.name = name;
		this.additional = additional;
		timestamp = new Date();
		this.environment = environment;
		this.ip = ip;
		this.cookie = cookie;
	}

	private String formatAdditionalInfo() {
		String addnl = additional;
		if (addnl == null || addnl.length() == 0) {
			addnl = (environment == null ? "" : environment);
		} else {
			addnl = addnl + (environment == null ? "" : " " + environment);
		}
		return addnl;
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam(
				"insert into "
						+ UNREGISTERED_LOGIN_CDR_TABLE_NAME
						+ "(loginid, name, address, time_stamp, addnl_info, ip_address, cookie)"
						+ " values (?, ?, ?, ?, ?, ?, ?)", null, loginId,
				name == null ? "" : name, email == null ? "" : email,
				timestamp, formatAdditionalInfo(), ip, cookie);
	}

	@Override
	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
