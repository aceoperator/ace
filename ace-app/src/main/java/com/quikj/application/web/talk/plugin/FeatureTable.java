/*
 * FeatureTable.java
 *
 * Created on September 7, 2003, 9:52 AM
 */

package com.quikj.application.web.talk.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import com.quikj.server.app.ApplicationServer;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author bhm
 */
public class FeatureTable {
	// database table name constants
	public static final String FEATURE_TABLE_NAME = "feature_tbl";
	public static final String PARAMS_TABLE_NAME = "feature_params_tbl";

	// feature table column name constants
	public static final String FEATURE_ID = "id";
	public static final String FEATURE_NAME = "fname";
	public static final String FEATURE_DOMAIN = "domain";
	public static final String FEATURE_CLASS = "class";
	public static final String FEATURE_ACTIVE = "active";

	// params table column name constants
	public static final String PARAM_FEATURE_ID = "feature_id";
	public static final String PARAM_NAME = "pname";
	public static final String PARAM_VALUE = "pvalue";

	private String errorMessage;

	public FeatureTable() {
	}

	public boolean activate(String name) {
		String cmd = "update " + FEATURE_TABLE_NAME + " set " + FEATURE_ACTIVE
				+ " = 1 where " + FEATURE_NAME + " = ?";

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();
			PreparedStatement pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, name);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				errorMessage = "Feature activate not performed: no rows affected";
				return false;
			}
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.activate() -- Failed to close database connection",
									e);
				}
			}
		}

		return true;
	}

	public boolean create(FeatureTableElement ele) {
		String cmd = "insert into " + FEATURE_TABLE_NAME
				+ " values (NULL, ?, ?, ?, ?)";

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, ele.getName());
			pstmt.setString(2, ele.getDomain() == null ? "" : ele.getDomain());
			pstmt.setString(3, ele.getClassName());
			pstmt.setBoolean(4, ele.isActive());

			int count = pstmt.executeUpdate();
			if (count == 0) {
				errorMessage = "Feature create failed: no rows affected, SQL command: "
						+ cmd;
				return false;
			}

			Map params = ele.getParams();
			if ((params != null) && (params.size() > 0)) {
				StringBuffer buffer = new StringBuffer("insert into "
						+ PARAMS_TABLE_NAME + " values ");

				int size = params.size();
				for (int i = 0; i < size; i++) {
					if (i > 0) {
						buffer.append(", ");
					}
					buffer.append("(LAST_INSERT_ID(), ?, ?)");
				}
				cmd = buffer.toString();

				pstmt = connection.prepareStatement(cmd);
				int index = 1;
				Set key_set = params.keySet();

				for (Iterator i = key_set.iterator(); i.hasNext();) {
					String key = (String) i.next();
					pstmt.setString(index++, key);
					String value = (String) params.get(key);
					pstmt.setString(index++, value);
				}

				pstmt.executeUpdate(); // need innoDB here
			}
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.create() -- Failed to close database connection",
									e);
				}
			}
		}

		return true;
	}

	public boolean deactivate(String name) {
		String cmd = "update " + FEATURE_TABLE_NAME + " set " + FEATURE_ACTIVE
				+ " = 0 where " + FEATURE_NAME + " = ?";

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, name);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				errorMessage = "Feature deactivate not performed: no rows affected";
				return false;
			}
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.deactivate() -- Failed to close database connection",
									e);
				}
			}
		}

		return true;
	}

	public boolean delete(String name) {
		return delete(name, null);
	}

	public boolean delete(String name, String domainConstraint) {
		StringBuilder cmd = new StringBuilder("select " + FEATURE_ID + " from "
				+ FEATURE_TABLE_NAME + " where ");

		if (domainConstraint != null) {
			cmd.append(FEATURE_DOMAIN + "= ? and ");
		}

		cmd.append(FEATURE_NAME + " = ?");

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd
					.toString());
			int index = 1;
			if (domainConstraint != null) {
				pstmt.setString(index++, domainConstraint);
			}
			pstmt.setString(index++, name);

			ResultSet rs = pstmt.executeQuery();
			if (!rs.first()) {
				// Doesn't exist
				errorMessage = null;
				return false;
			}

			int feature_id = rs.getInt(1);

			String sql = "delete from " + PARAMS_TABLE_NAME + " where "
					+ PARAM_FEATURE_ID + '=' + feature_id;

			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);

			sql = "delete from " + FEATURE_TABLE_NAME + " where " + FEATURE_ID
					+ '=' + feature_id;

			int count = stmt.executeUpdate(sql);
			if (count == 0) {
				errorMessage = "Feature delete failed: no rows affected, SQL command: "
						+ sql;
				return false;
			}

		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.delete() -- Failed to close database connection",
									e);
				}
			}
		}

		return true;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	public boolean isFeatureActive(String name) {
		String cmd = "select " + FEATURE_ACTIVE + " from " + FEATURE_TABLE_NAME
				+ " where " + FEATURE_NAME + " = ?";

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();
			PreparedStatement pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, name);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.first()) {
				return false;
			}

			return rs.getBoolean(1);
		} catch (SQLException ex) {
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.isFeatureActive() -- Failed to close database connection",
									e);
				}
			}
		}
	}

	public ArrayList list(String domainConstraint) {
		// returns list of 0 or more elements w/name, domain, classname & active
		// fields set, or null if error encountered

		StringBuffer cmd = new StringBuffer("select " + FEATURE_NAME + ", "
				+ FEATURE_DOMAIN + ", " + FEATURE_CLASS + ", " + FEATURE_ACTIVE
				+ " from " + FEATURE_TABLE_NAME);

		if (domainConstraint != null) {
			cmd.append(" where " + FEATURE_DOMAIN + "= ?");
		}

		cmd.append(" order by " + FEATURE_NAME);

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd
					.toString());
			if (domainConstraint != null) {
				pstmt.setString(1, domainConstraint);
			}
			ResultSet rs = pstmt.executeQuery();

			ArrayList list = new ArrayList();
			while (rs.next() == true) {
				FeatureTableElement ele = new FeatureTableElement();

				ele.setName(rs.getString(1));
				ele.setDomain(rs.getString(2));
				ele.setClassName(rs.getString(3));
				ele.setActive(rs.getBoolean(4));

				list.add(ele);
			}

			return list;
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.list() -- Failed to close database connection",
									e);
				}
			}
		}
	}

	public boolean modify(FeatureTableElement ele) {
		return modify(ele, null);
	}

	public boolean modify(FeatureTableElement ele, String domainConstraint) {
		StringBuffer cmd = new StringBuffer("select " + FEATURE_ID + " from "
				+ FEATURE_TABLE_NAME + " where ");

		if (domainConstraint != null) {
			cmd.append(FEATURE_DOMAIN + "= ? and ");
		}

		cmd.append(FEATURE_NAME + " = ?");

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd
					.toString());
			int index = 1;
			if (domainConstraint != null) {
				pstmt.setString(index++, domainConstraint);
			}
			pstmt.setString(index++, ele.getName());

			ResultSet rs = pstmt.executeQuery();
			if (!rs.first()) {
				// Doesn't exist
				errorMessage = null;
				return false;
			}

			int feature_id = rs.getInt(1);

			String sql = "update " + FEATURE_TABLE_NAME + " set "
					+ FEATURE_DOMAIN + "= ?, " + FEATURE_CLASS + "= ? where "
					+ FEATURE_ID + "= ? ";

			pstmt = connection.prepareStatement(sql);
			pstmt.setString(1, ele.getDomain() == null ? "" : ele.getDomain());
			pstmt.setString(
					2,
					ele.getClassName() == null ? FEATURE_CLASS : ele
							.getClassName());
			pstmt.setInt(3, feature_id);
			int count = pstmt.executeUpdate();
			if (count == 0) {
				errorMessage = "Feature modify not performed: no rows affected";
				return false;
			}

			// update params table
			sql = "delete from " + PARAMS_TABLE_NAME + " where "
					+ PARAM_FEATURE_ID + '=' + feature_id;

			Statement stmt = connection.createStatement();
			stmt.executeUpdate(sql);

			Map params = ele.getParams();
			if ((params != null) && (params.size() > 0)) {
				StringBuffer buffer = new StringBuffer("insert into "
						+ PARAMS_TABLE_NAME + " values ");

				int size = params.size();
				for (int i = 0; i < size; i++) {
					if (i > 0) {
						buffer.append(", ");
					}
					buffer.append("(?, ?, ?)");
				}

				sql = buffer.toString();
				pstmt = connection.prepareStatement(sql);
				index = 1;
				Set key_set = params.keySet();
				for (Iterator iter = key_set.iterator(); iter.hasNext();) {
					pstmt.setInt(index++, feature_id);
					String key = (String) iter.next();
					pstmt.setString(index++, key);
					String value = (String) params.get(key);
					pstmt.setString(index++, value);
				}

				pstmt.executeUpdate(); // need innoDB here
			}

		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return false;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.modify() -- Failed to close database connection",
									e);
				}
			}
		}

		return true;
	}

	public FeatureTableElement query(String featurename) {
		return query(featurename, null);
	}

	public FeatureTableElement query(String featurename, String domainConstraint) {
		String constraint = "";
		if (domainConstraint != null) {
			constraint = FEATURE_DOMAIN + "='" + domainConstraint + "' and ";
		}

		String cmd = "select " + FEATURE_DOMAIN + ", " + FEATURE_CLASS + ", "
				+ FEATURE_ACTIVE + " from " + FEATURE_TABLE_NAME + " where "
				+ constraint + FEATURE_NAME + " = ?";

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			PreparedStatement pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, featurename);
			ResultSet rs = pstmt.executeQuery();
			if (!rs.first()) {
				// Doesn't exist
				errorMessage = null;
				return null;
			}

			FeatureTableElement data = new FeatureTableElement();

			data.setDomain(rs.getString(1));
			data.setClassName(rs.getString(2));
			data.setActive(rs.getBoolean(3));
			data.setName(featurename);

			// query parameters for this feature
			cmd = "select " + PARAM_NAME + ',' + PARAM_VALUE + " from "
					+ PARAMS_TABLE_NAME + ',' + FEATURE_TABLE_NAME + " where "
					+ PARAM_FEATURE_ID + '=' + FEATURE_ID + " and "
					+ FEATURE_NAME + " = ?";

			pstmt = connection.prepareStatement(cmd);
			pstmt.setString(1, featurename);
			rs = pstmt.executeQuery();
			HashMap map = new HashMap();

			while (rs.next()) {
				map.put(rs.getString(1), rs.getString(2));
			}

			data.setParams(map);

			return data;
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.query() -- Failed to close database connection",
									e);
				}
			}
		}
	}

	public ArrayList queryAll() {
		// returns an arraylist of 0 or more FeatureTableElement objects, or
		// null if
		// error (call getErrorMessage()).
		// This is the full data query used by Ace Application Server
		// initialization

		// first get all of the data from the feature params table, build a map
		// of feature ID, feature params
		String cmd = "select " + PARAM_FEATURE_ID + ',' + PARAM_NAME + ','
				+ PARAM_VALUE + " from " + PARAMS_TABLE_NAME;

		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance()
					.getBean(DataSource.class).getConnection();

			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			HashMap feature_map = new HashMap(); // key = feature ID, value =
													// params map

			while (rs.next()) {
				Integer feature_id = new Integer(rs.getInt(1));

				Map param_map = (Map) feature_map.get(feature_id);
				if (param_map == null) {
					param_map = new HashMap();
					feature_map.put(feature_id, param_map);
				}

				param_map.put(rs.getString(2), rs.getString(3));
			}

			// now get all of the data from the feature table & build return
			// ArrayList
			cmd = "select " + FEATURE_ID + ", " + FEATURE_NAME + ", "
					+ FEATURE_DOMAIN + ", " + FEATURE_CLASS + ", "
					+ FEATURE_ACTIVE + " from " + FEATURE_TABLE_NAME;

			rs = stmt.executeQuery(cmd);

			ArrayList list = new ArrayList();

			while (rs.next()) {
				Integer feature_id = new Integer(rs.getInt(1));
				FeatureTableElement data = new FeatureTableElement();

				data.setName(rs.getString(2));
				data.setDomain(rs.getString(3));
				data.setClassName(rs.getString(4));
				data.setActive(rs.getBoolean(5));

				data.setParams((HashMap) feature_map.get(feature_id));

				list.add(data);
			}

			return list;
		} catch (SQLException ex) {
			errorMessage = "SQLException: " + ex.getMessage()
					+ ", SQL command: " + cmd;
			return null;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"FeatureTable.queryAll() -- Failed to close database connection",
									e);
				}
			}
		}
	}
}
