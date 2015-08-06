/*
 * CDRInterface.java
 *
 * Created on June 28, 2002, 11:59 AM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.sql.SQLException;

import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public interface CDRInterface {
	public SQLParam generateSQLCDR() throws SQLException;

	public String generateXMLCDR();
}
