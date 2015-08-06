/*
 * OPMHandler.java
 *
 * Created on March 3, 2003, 7:18 AM
 */

package com.quikj.application.web.talk.plugin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import com.quikj.server.app.ApplicationServer;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;

/**
 * 
 * @author bhm
 */
public class OPMHandler extends AceThread {
	private static OPMHandler instance = null;

	class SQLCommand implements AceMessageInterface {
		private String sql;

		public SQLCommand(String sql) {
			this.sql = sql;
		}

		public String getSQL() {
			return sql;
		}

		public String messageType() {
			return "WebTalkOPMSQLCommand";
		}
	}	
	
	public OPMHandler() throws IOException {
		super("WebTalkOPMHandler");
		instance = this;
	}

	public static OPMHandler getInstance() {
		return instance;
	}

	public void dispose() {
		// interrupt the wait (kill this thread)
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");
	}

	public boolean executeSQL(String sql) {
		return sendMessage(new SQLCommand(sql));
	}

	private void processSQLCommand(String cmd) {
		Connection connection = null;
		try {
			connection = ApplicationServer.getInstance().getBean(DataSource.class).getConnection();
			Statement s = connection.createStatement();
			int rowcount = s.executeUpdate(cmd);
			s.close();
			
			if (rowcount < 1) // not an accurate check
			{
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- OPMHandler.processSQLCommand() -- Couldn't store OPM record(s) in the database, SQL command = "
										+ cmd);
			}

			s.close();
		} catch (SQLException e) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- OPMHandler.processSQLCommand() -- SQLException encountered, error = "
									+ e.getMessage() + ", SQL command = "
									+ cmd);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- OPMHandler.processSQLCommand() -- SQLException closing connection", e);
				}
			}
		}
	}

	public void run() {
		while (true) {
			AceMessageInterface message = waitMessage();
			if (message == null) {
				// print error message
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- OPMHandler.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received

				// print informational message
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						getName() + " - OPMHandler.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());
				break;
			} else if (message instanceof OPMHandler.SQLCommand) {
				processSQLCommand(((OPMHandler.SQLCommand) message).getSQL());
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ " - OPMHandler.run() --  An unknown message of type "
										+ message.messageType()
										+ " is received");
			}
		}

		cleanup();
	}

	private void cleanup() {
		super.dispose();
		instance = null;
	}
}
