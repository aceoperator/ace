package com.quikj.ace.db.core.util;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class CalendarTypeHandler implements TypeHandler<Calendar> {

	public CalendarTypeHandler() {
	}

	@Override
	public Calendar getResult(ResultSet rs, String columnName)
			throws SQLException {
		Timestamp ts = rs.getTimestamp(columnName);
		if (ts != null) {
			Calendar c = new GregorianCalendar();
			c.setTimeInMillis(ts.getTime());
			return c;
		} else {
			return null;
		}
	}

	@Override
	public Calendar getResult(CallableStatement st, int index)
			throws SQLException {
		Timestamp ts = st.getTimestamp(index);
		if (ts != null) {
			Calendar c = new GregorianCalendar();
			c.setTimeInMillis(ts.getTime());
			return c;
		} else {
			return null;
		}
	}

	@Override
	public void setParameter(PreparedStatement ps, int index, Calendar value,
			JdbcType jdbcType) throws SQLException {
		Timestamp ms = null;
		if (value != null) {
			ms = new Timestamp(value.getTimeInMillis());
		}

		ps.setTimestamp(index, ms);
	}

	@Override
	public Calendar getResult(ResultSet rs, int index) throws SQLException {
		Timestamp ts = rs.getTimestamp(index);
		if (ts != null) {
			Calendar c = new GregorianCalendar();
			c.setTimeInMillis(ts.getTime());
			return c;
		} else {
			return null;
		}
	}
}
