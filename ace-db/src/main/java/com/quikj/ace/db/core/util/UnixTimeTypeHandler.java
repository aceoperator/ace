package com.quikj.ace.db.core.util;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class UnixTimeTypeHandler implements TypeHandler<Long> {

	public UnixTimeTypeHandler() {
	}

	@Override
	public Long getResult(ResultSet rs, String columnName) throws SQLException {
		Timestamp ts = rs.getTimestamp(columnName);
		if (ts != null) {
			return ts.getTime();
		} else {
			return null;
		}
	}

	@Override
	public Long getResult(CallableStatement st, int index) throws SQLException {
		Timestamp ts = st.getTimestamp(index);
		if (ts != null) {
			return ts.getTime();
		} else {
			return null;
		}
	}

	@Override
	public void setParameter(PreparedStatement ps, int index, Long value,
			JdbcType jdbcType) throws SQLException {
		if (value != null) {
			ps.setTimestamp(index, new Timestamp(value));
		}
	}

	@Override
	public Long getResult(ResultSet rs, int index) throws SQLException {
		Timestamp ts = rs.getTimestamp(index);
		if (ts != null) {
			return ts.getTime();
		} else {
			return null;
		}
	}
}
