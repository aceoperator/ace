/*
 * CDRHandler.java
 *
 * Created on June 29, 2002, 12:19 PM
 */

package com.quikj.application.web.talk.plugin.accounting;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import javax.sql.DataSource;

import com.quikj.server.app.ApplicationServer;
import com.quikj.server.framework.AceConfigFileHelper;
import com.quikj.server.framework.AceLogger;
import com.quikj.server.framework.AceMessageInterface;
import com.quikj.server.framework.AceSignalMessage;
import com.quikj.server.framework.AceThread;
import com.quikj.server.framework.SQLParam;

/**
 * 
 * @author amit
 */
public class CDRHandler extends AceThread {
	private static CDRHandler instance = null;
	private String path;
	private FileWriter outputFile = null;

	class CDRMessage implements AceMessageInterface {
		private CDRInterface cdr;

		public CDRMessage(CDRInterface cdr) {
			this.cdr = cdr;
		}

		public CDRInterface getCDR() {
			return cdr;
		}

		public String messageType() {
			return "WebTalkCDRMessage";
		}
	}

	public CDRHandler(String dir, String file) throws IOException {
		super("WebTalkCDRHandler");
		path = AceConfigFileHelper.getAcePath(dir, file);
		outputFile = new FileWriter(path, true);
		instance = this;
	}

	public static String getDateString(java.util.Date timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);

		String date_string = cal.get(Calendar.YEAR) + "-"
				+ (cal.get(Calendar.MONTH) + 1) + "-"
				+ cal.get(Calendar.DAY_OF_MONTH) + " "
				+ cal.get(Calendar.HOUR_OF_DAY) + ":"
				+ cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

		return date_string;
	}

	public static CDRHandler getInstance() {
		return instance;
	}

	public void dispose() {
		// interrupt the wait (kill this thread)
		interruptWait(AceSignalMessage.SIGNAL_TERM, "disposed");
	}

	private void cleanup() {
		if (outputFile != null) {
			try {
				outputFile.close();
				outputFile = null;
			} catch (IOException ex) {
				;
			}
		}

		super.dispose();
		instance = null;
	}

	private void processCDR(CDRInterface cdr) {
		Connection c = null;
		try {
			c = ApplicationServer.getInstance().getBean(DataSource.class)
					.getConnection();

			SQLParam p = cdr.generateSQLCDR();
			PreparedStatement ps = c.prepareStatement(p.getStatement());

			int index = 1;
			for (Object param : p.getParams()) {
				if (param instanceof String) {
					ps.setString(index++, (String) param);
				} else if (param instanceof Integer) {
					ps.setInt(index++, (Integer) param);
				} else if (param instanceof Long) {
					ps.setLong(index++, (Long) param);
				} else if (param instanceof Double) {
					ps.setDouble(index++, (Double) param);
				} else if (param instanceof Float) {
					ps.setFloat(index++, (Float) param);
				} else if (param instanceof Date) {
					ps.setTimestamp(index++,
							new Timestamp(((Date) param).getTime()));
				} else {
					// print log message and continue (it will result in
					// an exception any way)
					AceLogger.Instance().log(
							AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							"AceSQL.AceSQLThread.run() : unsupported data type "
									+ param.getClass().getName());
					continue;
				}
			}

			int rowcount = ps.executeUpdate();
			ps.close();

			if (rowcount != 1) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- CDRHandler.processCDR() -- Couldn't store CDR record in the database");

				outputFile.write(cdr.generateXMLCDR());
				outputFile.flush();
			}

		} catch (IOException ex) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- CDRHandler.processCDR() -- IOException encountered, error: "
									+ ex.getMessage() + " while writing CDR "
									+ cdr.generateXMLCDR());
		} catch (SQLException ex) {
			AceLogger
					.Instance()
					.log(AceLogger.ERROR,
							AceLogger.SYSTEM_LOG,
							getName()
									+ "- CDRHandler.processCDR() -- SQLException encountered, error: "
									+ ex.getMessage());
			try {
				outputFile.write(cdr.generateXMLCDR());
				outputFile.flush();
			} catch (IOException e) {
				AceLogger
						.Instance()
						.log(AceLogger.ERROR,
								AceLogger.SYSTEM_LOG,
								getName()
										+ "- CDRHandler.processCDR() -- IOException encountered, error: "
										+ e.getMessage()
										+ " while writing CDR "
										+ cdr.generateXMLCDR());
			}
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									getName()
											+ "- CDRHandler.processCDR() -- SQLException encountered while closing connection"
											+ e);
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
										+ "- CDRHandler.run() -- A null message was received while waiting for a message - "
										+ getErrorMessage());

				break;
			}

			if (message instanceof AceSignalMessage) {
				// A signal message is received

				// print informational message
				AceLogger.Instance().log(
						AceLogger.INFORMATIONAL,
						AceLogger.SYSTEM_LOG,
						getName() + " - CDRHandler.run() --  A signal "
								+ ((AceSignalMessage) message).getSignalId()
								+ " is received : "
								+ ((AceSignalMessage) message).getMessage());
				break;
			} else if (message instanceof CDRHandler.CDRMessage) {
				processCDR(((CDRHandler.CDRMessage) message).getCDR());
			} else {
				AceLogger
						.Instance()
						.log(AceLogger.WARNING,
								AceLogger.SYSTEM_LOG,
								getName()
										+ " - CDRHandler.run() --  An unknow message of type "
										+ message.messageType()
										+ " is received");
			}
		}

		cleanup();
	}

	public boolean sendCDR(CDRInterface cdr) {
		return sendMessage(new CDRMessage(cdr));
	}
}
