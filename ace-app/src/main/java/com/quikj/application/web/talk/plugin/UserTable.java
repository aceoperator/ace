package com.quikj.application.web.talk.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.quikj.server.framework.MapResult;
import com.quikj.server.framework.SQLParam;

public class UserTable {
	// database user table name constant
	public static final String USER_TABLE_NAME = "user_tbl";
	public static final String BLACKLIST_TABLE_NAME = "blacklist_tbl";

	// database user table column name constants
	public static final String USERNAME = "userid";
	public static final String PASSWORD = "password";
	public static final String DOMAIN = "domain";
	public static final String FLAGS = "flags";
	public static final String FULLNAME = "fullname";
	public static final String ADDRESS = "address";
	public static final String ADDITIONAL_INFO = "addnl_info";
	public static final String UNAVAIL_XFER = "unavail_xferto";
	public static final String GATEKEEPER = "gatekeeper";
	public static final String AVATAR = "avatar";
	public static final String LOCKED = "locked";
	public static final String CHANGE_PASSWORD = "change_password";
	public static final String PASSWORD_UPDATED = "password_updated";
	public static final String PRIVATE = "private";

	private static final String SQL_GET_GROUP_MEMBER_NAMES = "select "
			+ GroupTable.GROUPNAME + " from "
			+ GroupTable.GROUP_MEMBER_TABLE_NAME + " where "
			+ GroupTable.USERNAME + " = ?";

	private static final String SQL_GET_GROUP_OWNER_NAMES = "select "
			+ GroupTable.GROUPNAME + " from "
			+ GroupTable.GROUP_OWNER_TABLE_NAME + " where "
			+ GroupTable.USERNAME + " = ?";

	private static final String SQL_TRANSFER_INFO = "select t1." + USERNAME
			+ ", t1." + FULLNAME + ", t1." + ADDRESS + ", t1."
			+ ADDITIONAL_INFO + ", t1." + UNAVAIL_XFER + ", t1." + GATEKEEPER
			+ ", " + AVATAR + " from " + USER_TABLE_NAME + " as t1 left join "
			+ USER_TABLE_NAME + " as t2 on t1." + USERNAME + " = t2."
			+ UNAVAIL_XFER + " where t2." + USERNAME + " = ?";

	private static final String SQL_QUERY_USER = "select " + FULLNAME + ", "
			+ ADDRESS + ", " + ADDITIONAL_INFO + ", " + UNAVAIL_XFER + ", "
			+ GATEKEEPER + ", " + AVATAR + ", " + CHANGE_PASSWORD + ", "
			+ PRIVATE + " from " + USER_TABLE_NAME + " where " + USERNAME
			+ " = ?";

	private static final String SQL_AUTHENTICATE_USER = SQL_QUERY_USER
			+ " and " + PASSWORD + " = password(?) and " + LOCKED + " = 0";

	private static final String SQL_CHANGE_PASSWORD = "update "
			+ USER_TABLE_NAME + " set " + PASSWORD + "= password(?), "
			+ CHANGE_PASSWORD + "= 0, " + PASSWORD_UPDATED + "= ?"
			+ " where " + USERNAME + " = ? and "
			+ PASSWORD + "=password(?)";

	private static final String SQL_VALIDATE_IDENTIFIER = "select b.identifier from "
			+ BLACKLIST_TABLE_NAME
			+ " b, "
			+ USER_TABLE_NAME
			+ " u where b.user_id = u.id and u.userid = ? and "
			+ "((b.identifier = ? and b.type = 0) or (b.identifier = ? and b.type = 1))";

	public static SQLParam[] getChangePasswordStatement(String username,
			String oldPassword, String newPassword) {
		return new SQLParam[] { new SQLParam(SQL_CHANGE_PASSWORD, null,
				newPassword, new Timestamp(System.currentTimeMillis()),
				username, oldPassword) };
	}

	public static SQLParam getQueryStatement(String username) {
		return new SQLParam(SQL_QUERY_USER, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				UserElement userdata = null;
				if (result.next()) {
					userdata = mapUserElement(result);
				}

				return userdata;
			}
		}, username);
	}

	public static SQLParam getQueryStatement(String username, String password) {
		return new SQLParam(SQL_AUTHENTICATE_USER, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				UserElement userdata = null;
				if (result.next()) {
					userdata = mapUserElement(result);
				}

				return userdata;
			}
		}, username, password);
	}

	public static SQLParam[] getTransferInfoQueryStatement(String username) {
		return new SQLParam[] { new SQLParam(SQL_TRANSFER_INFO, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				UserElement userdata = null;
				if (result.next()) {
					userdata = mapTransferUserElement(result);
				}

				return userdata;
			}
		}, username) };
	}

	public static SQLParam[] getCookieIdentifierStatement(
			String registeredUserName, String visitorCookie, String ipAddress) {
		return new SQLParam[] { new SQLParam(SQL_VALIDATE_IDENTIFIER, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				return mapString(result);
			}
		}, registeredUserName, visitorCookie, ipAddress) };
	}

	public static SQLParam[] getUserElementQueryStatements(String username) {
		SQLParam[] sqlStatements = new SQLParam[3];

		// query for basic user data
		sqlStatements[0] = getQueryStatement(username);

		setGroupQuery(username, sqlStatements);

		return sqlStatements;
	}

	private static void setGroupQuery(String username, SQLParam[] sqlStatements) {
		// query names of groups owned by this user
		sqlStatements[1] = new SQLParam(SQL_GET_GROUP_OWNER_NAMES, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				return mapStringList(result);
			}
		}, username);

		// query names of groups that this user belongs to
		sqlStatements[2] = new SQLParam(SQL_GET_GROUP_MEMBER_NAMES, new MapResult() {
			@Override
			public Object map(ResultSet result) throws SQLException {
				return mapStringList(result);
			}
		}, username);
	}

	public static SQLParam[] getUserElementQueryStatements(String username,
			String password) {
		SQLParam[] sqlStatements = new SQLParam[3];

		// query for basic user data
		sqlStatements[0] = getQueryStatement(username, password);

		setGroupQuery(username, sqlStatements);

		return sqlStatements;
	}

	private static UserElement mapUserElement(ResultSet result)
			throws SQLException {
		UserElement userdata = new UserElement();
		userdata.setFullName(result.getString(1));
		userdata.setAddress(result.getString(2));
		userdata.setAdditionalInfo(result.getString(3));
		userdata.setUnavailXferTo(result.getString(4));
		userdata.setGatekeeper(result.getString(5));
		userdata.setAvatar(result.getString(6));
		userdata.setChangePassword(result.getBoolean(7));
		userdata.setPrivateInfo(result.getBoolean(8));
		return userdata;
	}
	
	private static UserElement mapTransferUserElement(ResultSet result) throws SQLException {
		UserElement userdata = new UserElement();
		userdata.setName(result.getString(1));
		userdata.setFullName(result.getString(2));
		userdata.setAddress(result.getString(3));
		userdata.setAdditionalInfo(result.getString(4));
		userdata.setUnavailXferTo(result.getString(5));
		userdata.setGatekeeper(result.getString(6));
		userdata.setAvatar(result.getString(7));
		return userdata;
	}

	private static List<String> mapStringList(ResultSet result) throws SQLException {
		List<String> list = new ArrayList<String>();
		while (result.next()) {
			list.add(result.getString(1));
		}

		return list;
	}

	private static Object mapString(ResultSet result) throws SQLException {
		String s = null;
		if (result.next()) {
			s = result.getString(1);
		}

		return s;
	}
}
