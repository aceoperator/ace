package com.quikj.ace.db.core.util;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.rowset.serial.SerialBlob;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

public class BlobTypeHandler implements TypeHandler<String> {

	public BlobTypeHandler() {
	}

	@Override
	public void setParameter(PreparedStatement ps, int i, String parameter,
			JdbcType jdbcType) throws SQLException {
		if (parameter != null) {
			SerialBlob blob = new SerialBlob(parameter.getBytes());
			ps.setBlob(i, blob);
		}
	}

	@Override
	public String getResult(ResultSet rs, String columnName)
			throws SQLException {
		Blob blob = rs.getBlob(columnName);
		if (blob != null) {
			String ret = new String(blob.getBytes(1L, (int) blob.length()));
			return ret;
		} else {
			return null;
		}
	}

	@Override
	public String getResult(ResultSet rs, int columnIndex) throws SQLException {
		Blob blob = rs.getBlob(columnIndex);
		if (blob != null) {
			String ret = new String(blob.getBytes(1L, (int) blob.length()));
			return ret;
		} else {
			return null;
		}
	}

	@Override
	public String getResult(CallableStatement cs, int columnIndex)
			throws SQLException {
		Blob blob = cs.getBlob(columnIndex);
		if (blob != null) {
			String ret = new String(blob.getBytes(1L, (int) blob.length()));
			return ret;
		} else {
			return null;
		}
	}
}
