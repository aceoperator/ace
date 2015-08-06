package com.quikj.ace.db.core.util;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class DateTypeHandler implements TypeHandler<Date> {

	public DateTypeHandler() {
	}

	@Override
	public Date getResult(ResultSet rs, String columnName)
			throws SQLException {
		Timestamp ts = rs.getTimestamp(columnName);
		if (ts != null) {
			return new Date(ts.getTime());
		} else {
			return null;
		}
	}

	@Override
	public Date getResult(CallableStatement st, int index)
			throws SQLException {
		Timestamp ts = st.getTimestamp(index);
		if (ts != null) {
			return new Date(ts.getTime());
		} else {
			return null;
		}
	}

	@Override
	public void setParameter(PreparedStatement ps, int index, Date value,
			JdbcType jdbcType) throws SQLException {
		Timestamp ms = null;
		if (value != null) {
			ms = new Timestamp(value.getTime());
		}
		
		ps.setTimestamp(index, ms);
	}

	@Override
	public Date getResult(ResultSet rs, int index) throws SQLException {
		Timestamp ts = rs.getTimestamp(index);
		if (ts != null) {
			return new Date(ts.getTime());
		} else {
			return null;
		}
	}
}
