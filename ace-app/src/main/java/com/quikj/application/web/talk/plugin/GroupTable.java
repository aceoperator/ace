package com.quikj.application.web.talk.plugin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.quikj.server.framework.MapResult;
import com.quikj.server.framework.SQLParam;

public class GroupTable {
	// database group table name constant
	public static final String GROUP_TABLE_NAME = "group_tbl";

	// database group table column name constants
	public static final String GROUPNAME = "groupid";
	public static final String DOMAIN = "domain";
	public static final String FLAGS = "flags";
	public static final String MEMBERLOGIN_NOTIFICATION = "memberlogin_notif";
	public static final String MEMBERBUSY_NOTIFICATION = "memberbusy_notif";
	public static final String OWNERLOGIN_NOTIFICATION = "ownerlogin_notif";
	public static final String OWNERBUSY_NOTIFICATION = "ownerbusy_notif";

	// database group owner table name constant
	public static final String GROUP_OWNER_TABLE_NAME = "group_owner_tbl";

	// database group owner table column name constants
	public static final String USERNAME = UserTable.USERNAME;
	// public static final String GROUPNAME = "groupid";

	// database group member table name constant
	public static final String GROUP_MEMBER_TABLE_NAME = "group_member_tbl";

	public static SQLParam[] getGroupInfoByUserQueryStatements(String username) {
		// this returns the queries necessary to get all GroupInfos for groups
		// that the given user owns as well as belongs to

		SQLParam[] sqlStatements = new SQLParam[4];

		// get groups info by owner (group name, notification controls)
		sqlStatements[0] = new SQLParam("select g." + GROUPNAME + ",g."
				+ MEMBERLOGIN_NOTIFICATION + ",g." + MEMBERBUSY_NOTIFICATION
				+ ",g." + OWNERLOGIN_NOTIFICATION + ",g."
				+ OWNERBUSY_NOTIFICATION + " from " + GROUP_TABLE_NAME
				+ " as g inner join " + GROUP_OWNER_TABLE_NAME
				+ " as o using (" + GROUPNAME + ") where o." + USERNAME
				+ " = ?", new MapResult() {

			@Override
			public Object map(ResultSet rs) throws SQLException {
				List<GroupInfo> grouplist = new ArrayList<GroupInfo>();

				while (rs.next()) {
					GroupInfo info = mapGroupInfo(rs);
					grouplist.add(info);
				}

				return grouplist;
			}
		}, username);

		// get groups members by owner
		sqlStatements[1] = new SQLParam("select m." + GROUPNAME + ", m."
				+ USERNAME + " from " + GROUP_MEMBER_TABLE_NAME
				+ " as m inner join " + GROUP_OWNER_TABLE_NAME
				+ " as o using (" + GROUPNAME + ") where o." + USERNAME
				+ " = ?", new MapResult() {

			@Override
			public Object map(ResultSet rs) throws SQLException {
				List<GroupMember> grouplist = new ArrayList<GroupMember>();

				while (rs.next()) {
					GroupMember member = new GroupMember(rs.getString(1),
							rs.getString(2));
					grouplist.add(member);
				}

				return grouplist;
			}
		}, username);

		// get groups info by member (group name, notif, owner)
		sqlStatements[2] = new SQLParam("select g." + GROUPNAME + ", g."
				+ MEMBERLOGIN_NOTIFICATION + ", g." + MEMBERBUSY_NOTIFICATION
				+ ", g." + OWNERLOGIN_NOTIFICATION + ", g."
				+ OWNERBUSY_NOTIFICATION + ",o." + USERNAME + " from "
				+ GROUP_TABLE_NAME + " as g inner join "
				+ GROUP_OWNER_TABLE_NAME + " as o using (" + GROUPNAME
				+ ") inner join " + GROUP_MEMBER_TABLE_NAME + " as m using ("
				+ GROUPNAME + ") where m." + USERNAME + " = ?",
				new MapResult() {

					@Override
					public Object map(ResultSet rs) throws SQLException {
						List<GroupInfo> grouplist = new ArrayList<GroupInfo>();

						while (rs.next()) {
							GroupInfo info = mapGroupAndOwnerInfo(rs);
							grouplist.add(info);
						}

						return grouplist;
					}
				}, username);

		// get groups members by member
		sqlStatements[3] = new SQLParam("select m." + GROUPNAME + ", m."
				+ USERNAME + " from " + GROUP_MEMBER_TABLE_NAME
				+ " as m inner join " + GROUP_MEMBER_TABLE_NAME
				+ " as m1 using (" + GROUPNAME + ") where m1." + USERNAME
				+ " = ?", new MapResult() {

			@Override
			public Object map(ResultSet rs) throws SQLException {
				List<GroupMember> grouplist = new ArrayList<GroupMember>();

				while (rs.next()) {
					GroupMember member = new GroupMember(rs.getString(1),
							rs.getString(2));
					grouplist.add(member);
				}

				return grouplist;
			}
		}, username);

		return sqlStatements;
	}

	
	private static GroupInfo mapGroupInfo(ResultSet rs) throws SQLException {
		String groupname = rs.getString(1);

		GroupInfo info = new GroupInfo(groupname);
		info.getGroupData().setMemberLoginNotificationControl(rs.getInt(2));
		info.getGroupData().setMemberBusyNotificationControl(rs.getInt(3));
		info.getGroupData().setOwnerLoginNotificationControl(rs.getInt(4));
		info.getGroupData().setOwnerBusyNotificationControl(rs.getInt(5));
		return info;
	}
	
	private static GroupInfo mapGroupAndOwnerInfo(ResultSet rs) throws SQLException {
		String groupname = rs.getString(1);

		GroupInfo info = new GroupInfo(groupname);
		info.getGroupData().setMemberLoginNotificationControl(rs.getInt(2));
		info.getGroupData().setMemberBusyNotificationControl(rs.getInt(3));
		info.getGroupData().setOwnerLoginNotificationControl(rs.getInt(4));
		info.getGroupData().setOwnerBusyNotificationControl(rs.getInt(5));
				
		info.setOwner(rs.getString(6));
		return info;
	}

}
