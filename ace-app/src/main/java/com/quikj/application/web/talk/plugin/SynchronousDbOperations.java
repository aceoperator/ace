/*
 * SynchronousDBOperations.java
 *
 * Created on June 6, 2003, 11:36 AM
 */

package com.quikj.application.web.talk.plugin;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.sql.DataSource;

import com.quikj.ace.messages.vo.talk.CannedMessageElement;
import com.quikj.server.app.ApplicationServer;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author amit
 */
public class SynchronousDbOperations {

	private static SynchronousDbOperations instance = null;

	private SynchronousDbOperations() {
		instance = this;
	}

	public static SynchronousDbOperations getInstance() {
		if (instance == null) {
			instance = new SynchronousDbOperations();
		}
		return instance;
	}

	public void dispose() {
		instance = null;
	}

	public synchronized CannedMessageElement[] listCannedMessages(String[] groups, boolean fetchContent) {
		Connection c = null;

		try {
			StringBuilder query = new StringBuilder("select id, description, grp");
			if (fetchContent) {
				query.append(", message");
			}

			query.append(" from canned_message_tbl where grp is null");

			if (groups.length > 0) {
				query.append(" or grp in (");

				for (int i = 0; i < groups.length; i++) {
					if (i > 0) {
						query.append(", ");
					}

					query.append("?");
				}

				query.append(") ");
			}

			query.append(" order by grp, description");

			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();
			PreparedStatement pstmt = c.prepareStatement(query.toString());
			for (int i = 0; i < groups.length; i++) {
				pstmt.setString(i + 1, groups[i]);
			}

			ResultSet rslt = pstmt.executeQuery();
			ArrayList<CannedMessageElement> lst = new ArrayList<CannedMessageElement>();
			while (rslt.next()) {
				int index = 1;
				CannedMessageElement el = new CannedMessageElement();
				el.setId(rslt.getLong(index++));
				el.setDescription(rslt.getString(index++));
				el.setGroup(rslt.getString(index++));

				if (fetchContent) {
					el.setMessage(rslt.getString(index++));
				}

				lst.add(el);
			}

			return lst.toArray(new CannedMessageElement[lst.size()]);
		} catch (Exception e) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.listCannedMessages() -- an exception occured while querying database", e);
			return null;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.listCannedMessages() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}

	public synchronized String queryCannedMessages(long id) {
		String query = "select message from canned_message_tbl where id = ?";

		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();
			PreparedStatement pstmt = c.prepareStatement(query);
			pstmt.setLong(1, id);
			ResultSet rslt = pstmt.executeQuery();

			if (rslt.next()) {
				Blob blob = rslt.getBlob(1);
				return new String(blob.getBytes(1, (int) blob.length()));
			}
		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.queryCannedMessages() -- an exception occured while querying database "
							+ ex.getMessage());
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.queryCannedMessages() -- Exception occured while closing connection",
							e);
				}
			}
		}

		return null;
	}

	public synchronized List<String> getGroupOwners(String userName) {
		List<String> ret = new ArrayList<String>();
		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			String query = "select go.userid" + " from group_member_tbl gm, group_owner_tbl go" + " where"
					+ " gm.groupid = go.groupid" + " and gm.userid = ?";

			PreparedStatement pstmt = c.prepareStatement(query);
			pstmt.setString(1, userName);
			ResultSet rslt = pstmt.executeQuery();

			while (rslt.next()) {
				ret.add(rslt.getString(1));
			}
		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.getGroupOwners() -- an exception occured while querying database "
							+ ex.getMessage());
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.getGroupOwners() -- Exception occured while closing connection",
							e);
				}
			}
		}

		return ret;
	}

	public synchronized HashMap<Integer, String> getSecurityQuestions(String userid) throws Exception {
		String query = "select question_id, question_value from user_tbl as u"
				+ " left join user_security_questions_tbl as q on u.id = q.user_id"
				+ " where address is not null and address != '' and u.userid = ?";
		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement(query);
			pstmt.setString(1, userid);
			ResultSet rslt = pstmt.executeQuery();

			if (!rslt.next()) {
				return null;
			}

			HashMap<Integer, String> questions = new HashMap<Integer, String>();
			do {
				Integer id = rslt.getInt(1);
				questions.put(id, rslt.getString(2));
			} while (rslt.next());

			return questions;

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.getSecurityQuestions() -- An exception occured while retrieving security questions for user '"
							+ userid + "': " + ex.getMessage());
			throw ex;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.getSecurityQuestions() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}

	public synchronized boolean resetPassword(String userid, String password, HashMap<Integer, String> securityAnswers)
			throws Exception {

		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement("select id from user_tbl where userid =?");
			pstmt.setString(1, userid);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			long id = rs.getLong(1);

			StringBuffer update = new StringBuffer("update user_tbl set password=password(?)" + " where id = ?"
					+ " and ? = (select count(*) from user_security_questions_tbl q where q.user_id = ?)");
			for (int i = 0; i < securityAnswers.size(); i++) {
				update.append(" and ? = (select answer_value from user_security_questions_tbl q where q.user_id = ?"
						+ " and question_id = ?)");
			}

			pstmt = c.prepareStatement(update.toString());
			int index = 1;
			pstmt.setString(index++, password);
			pstmt.setLong(index++, id);
			pstmt.setInt(index++, securityAnswers.size());
			pstmt.setLong(index++, id);

			for (Entry<Integer, String> answer : securityAnswers.entrySet()) {
				pstmt.setString(index++, answer.getValue());
				pstmt.setLong(index++, id);
				pstmt.setInt(index++, answer.getKey());
			}

			int rowcount = pstmt.executeUpdate();
			if (rowcount == 1) {
				return true;
			}

			return false;

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.resetPassword() -- An exception occured while resetting password for user '"
							+ userid + "': " + ex.getMessage());
			throw ex;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.resetPassword() -- Exception occured while closing connection", e);
				}
			}
		}
	}

	public synchronized String getEmailAddress(String userid) throws Exception {
		String query = "select address from user_tbl where userid = ?";

		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement(query);
			pstmt.setString(1, userid);
			ResultSet rslt = pstmt.executeQuery();

			if (!rslt.next()) {
				return null;
			}

			return rslt.getString(1);

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.getEmailAddress() --  An exception occured while retrieving email address for user '"
							+ userid + "': " + ex.getMessage());
			throw ex;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.getEmailAddress() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}

	public synchronized List<String> findUserByEmailAddress(String address) throws Exception {
		String query = "select userid from user_tbl where address = ?";

		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement(query);
			pstmt.setString(1, address);
			ResultSet rslt = pstmt.executeQuery();

			List<String> users = new ArrayList<String>();
			while (rslt.next()) {
				users.add(rslt.getString(1));
			}

			return users;

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.findUserByEmailAddress() -- An exception occured while retrieving user name for email address '"
							+ address + "': " + ex.getMessage());
			throw ex;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.findUserByEmailAddress() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}

	public long createFormRecord(long sessionId, long cannedMessageId) {
		Connection c = null;
		try {
			String statement = "insert into form_tbl(session, canned_msg_id) values(?, ?)";

			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
			pstmt.setLong(1, sessionId);
			pstmt.setLong(2, cannedMessageId);
			pstmt.executeUpdate();

			ResultSet rs = pstmt.getGeneratedKeys();
			rs.next();
			return rs.getLong(1);

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.getFormId() --  An exception occured while inserting form record for session '"
							+ sessionId + "': " + ex.getMessage());
			return -1L;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.getFormId() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}
	
	public long retrieveFormRecord(long formId) {
		Connection c = null;
		try {			
			String statement = "update form_tbl set status = 1 where status = 0 and id = ?";

			c = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = c.prepareStatement(statement, Statement.RETURN_GENERATED_KEYS);
			pstmt.setLong(1, formId);
			int affected = pstmt.executeUpdate();
			if (affected == 0) {
				return -1L;
			}

			statement = "select canned_msg_id from form_tbl where id = ?"; 
			pstmt = c.prepareStatement(statement);
			pstmt.setLong(1, formId);
			
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			return rs.getLong(1);

		} catch (Exception ex) {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"SynchronousDBOperations.retrieveFormRecord() --  An exception occured while form retrieving record '"
							+ formId + "': " + ex.getMessage());
			return -1;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
							"SynchronousDBOperations.retrieveFormRecord() -- Exception occured while closing connection",
							e);
				}
			}
		}
	}
}
