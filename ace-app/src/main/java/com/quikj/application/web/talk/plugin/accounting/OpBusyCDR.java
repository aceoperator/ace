package com.quikj.application.web.talk.plugin.accounting;

import java.util.Date;

import com.quikj.application.web.talk.plugin.XMLSerializer;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class OpBusyCDR implements CDRInterface {

	private static final String OPBUSY_CDR_TABLE_NAME = "cdr_unreg_opbusy_tbl";

	private String group;

	private Date timestamp;

	public OpBusyCDR(String group) {
		this.group = group;
		timestamp = new Date();
	}

	public SQLParam generateSQLCDR() {
		return new SQLParam("insert into " + OPBUSY_CDR_TABLE_NAME
				+ " (groupid, time_stamp) values (?, ?)", null, group,
				timestamp);
	}

	public String generateXMLCDR() {
		return XMLSerializer.getInstance().serialize(this);
	}
}
