/*
 * CommunicatorClientList.java
 *
 * Created on June 1, 2003, 10:13 AM
 */

package com.quikj.server.app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import javax.sql.DataSource;

import com.quikj.client.raccess.AceRMIImpl;
import com.quikj.client.raccess.RemoteServiceInterface;
import com.quikj.server.framework.AceLogger;

/**
 * 
 * @author amit
 */
public class CommunicatorClientList implements RemoteServiceInterface {
	// database account table name constant
	public static final String ACCOUNT_TABLE_NAME = "account_tbl";

	// database account table column name constants
	public static final String NAME = "userid";

	public static final String PASSWORD = "password";

	public static final String ADDITIONAL_INFO = "addnl_info";

	private HashMap<String, AccountElement> list = new HashMap<String, AccountElement>();

	private Random random = new Random(new Date().getTime());

	public CommunicatorClientList() {
		AceRMIImpl.getInstance().registerService(getClass().getName(), this);
	}

	public void dispose() {
	}

	private String generateAuthCode() {
		while (true) {
			int rand = random.nextInt();
			rand = (rand >= 0) ? rand : 0 - rand;
			if (list.containsKey(new Integer(rand)) == false) {
				return String.valueOf(rand);
			}
		}
	}

	public String getRMIParam(String param) {
		StringTokenizer tokens = new StringTokenizer(param, ":");
		int num_tokens = tokens.countTokens();
		String command = tokens.nextToken();

		if (command.equals("register")) {
			if (num_tokens < 3) {
				return null;
			}

			return registerCommunicatorUser(tokens.nextToken(),
					tokens.nextToken());
		}

		return null;
	}

	private void processLoginResult(AccountElement userinfo, ResultSet result)
			throws SQLException {
		userinfo.setAdditionalInfo(result.getString(1));
	}

	public synchronized String registerCommunicatorUser(String user,
			String password) {
		Connection c = null;
		try {
			String sql = "select " + ADDITIONAL_INFO + " from "
					+ ACCOUNT_TABLE_NAME + " where " + NAME + " = ? and "
					+ PASSWORD + " = password(?)";

			c = ApplicationServer.getInstance().getBean(DataSource.class)
					.getConnection();
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, user);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();
			if (!rs.next()) {
				return null;
			}

			AccountElement e = new AccountElement();
			processLoginResult(e, rs);
			e.setName(user);

			String authcode = generateAuthCode();
			list.put(authcode, e);
			return authcode;
		} catch (SQLException ex) {
			AceLogger.Instance().log(
					AceLogger.ERROR,
					AceLogger.SYSTEM_LOG,
					"CommunicatorClientList.registerCommunicatorUser(): SQLException "
							+ ex.getMessage());
			return null;
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"CommunicatorClientList.registerCommunicatorUser(): SQLException while closing connection"
											+ e);
				}
			}
		}
	}

	public boolean setRMIParam(String param, String value) {
		StringTokenizer tokens = new StringTokenizer(param, ":");
		int num_tokens = tokens.countTokens();
		String command = tokens.nextToken();

		if (command.equals("unregister") == true) {
			if (num_tokens < 2) {
				return false;
			}

			unregisterCommunicatorUser(tokens.nextToken());
			return true;
		}

		return false;
	}

	public synchronized void unregisterCommunicatorUser(String authcode) {
		list.remove(authcode);
	}

}