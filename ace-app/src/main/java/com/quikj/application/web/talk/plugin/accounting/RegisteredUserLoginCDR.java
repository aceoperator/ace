/*
 * RegisteredUserLoginCDR.java
 *
 * Created on June 28, 2002, 12:03 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class RegisteredUserLoginCDR implements CDRInterface {

	// database table name constant
	private static final String REGISTERED_LOGIN_CDR_TABLE_NAME = "cdr_reg_login_tbl";

	private String loginId;

	private String userName;
	private Date timestamp;
	
	public RegisteredUserLoginCDR(String id, String user_name) {
		loginId = id;
		userName = user_name;
		timestamp = new Date();
	}

	@Override
	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + REGISTERED_LOGIN_CDR_TABLE_NAME
				+ " values (?, ?, ?)", null, loginId, userName, timestamp);
	}
	
	public String generateXMLCDR() {		
		return XMLSerializer.getInstance().serialize(this);
	}
	
	public String getLoginId() {
		return loginId;
	}
}
