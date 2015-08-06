package com.quikj.server.framework;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

import javax.sql.DataSource;

public class AceSQL implements AceCompareMessageInterface {

	private boolean quit = false;

	private DataSource datasource = null;

	private Hashtable<Long, AceSQLThread> pendingOperations = new Hashtable<Long, AceSQLThread>();

	private static Object nextOperationIdLock = new Object();

	private static long nextOperationId = 0;

	class AceSQLThread extends AceThread {
		private boolean quitThread = false;

		private AceThread parent;
		private Object userParm;
		private long operationId;
		private SQLParam[] sqlStatements;

		public AceSQLThread(long operationId, SQLParam[] sqlStatements,
				AceThread parent, Object userParm) {
			this.operationId = operationId;
			this.sqlStatements = sqlStatements;
			this.parent = parent;
			this.userParm = userParm;
		}

		public void dispose() {
			if (!quitThread) {
				quitThread = true;
				// System.out.println("AceSQLThread.dispose() called, canceling the SQL statement");
			}

			super.dispose();
		}

		public Object getUserParm() {
			return userParm;
		}

		public void run() {
			// add this thread to the pending operations
			pendingOperations.put(operationId, this);

			int numExecuted = 0;
			Connection connection = null;
			PreparedStatement ps = null;
			try {
				connection = datasource.getConnection();
				int numAffectedRows = 0;
				ArrayList<Object> results = new ArrayList<Object>();

				for (SQLParam statement : sqlStatements) {
					ps = connection.prepareStatement(statement.getStatement());

					int index = 1;
					for (Object param : statement.getParams()) {
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
							ps.setDate(index++, new java.sql.Date(
									((Date) param).getTime()));
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

					if (ps.execute()) {
						ResultSet rs = ps.getResultSet();
						results.add(statement.getResultHandler().map(rs));
						rs.close();
					} else {
						numAffectedRows = ps.getUpdateCount();
					}

					ps.close();
					ps = null;
				}

				// send a message to the calling thread
				if (!quit && !quitThread) {
					if (!parent.sendMessage(new AceSQLMessage(
							AceSQLMessage.SQL_EXECUTED, operationId, results,
							numAffectedRows, numExecuted, userParm))) {
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										parent.getName()
												+ "- AceSQL.AceSQLThread.run() -- Error sending SQL executed message : "
												+ getErrorMessage());
					}
				}
			} catch (Exception e) {
				if (!quit && !quitThread) {
					AceLogger.Instance().log(
							AceLogger.WARNING,
							AceLogger.SYSTEM_LOG,
							parent.getName()
									+ " -- Unexpected database result : "
									+ e.getMessage(), e);

					// send a message to the calling thread
					if (!parent.sendMessage(new AceSQLMessage(
							AceSQLMessage.SQL_ERROR, operationId, null, 0, 0,
							userParm))) {
						AceLogger
								.Instance()
								.log(AceLogger.ERROR,
										AceLogger.SYSTEM_LOG,
										parent.getName()
												+ "- AceSQL.AceSQLThread.run() -- Error sending SQL error message : "
												+ getErrorMessage());
					}
				}
			} finally {
				try {
					if (ps != null && !ps.isClosed()) {
						ps.close();
					}

					if (connection != null) {
						connection.close();
					}
				} catch (SQLException e) {
					AceLogger
							.Instance()
							.log(AceLogger.ERROR,
									AceLogger.SYSTEM_LOG,
									"AceSQL.AceSQLThread.run() : SQL Exception while cleaning up",
									e);
				}

				// remove this thread to the pending operations
				pendingOperations.remove(operationId);

				dispose();
			}
		}
	}

	public AceSQL(DataSource datasource) {
		this.datasource = datasource;
	}

	public boolean cancelSQL(int id) {
		return cancelSQL(id, null);
	}

	public boolean cancelSQL(long id, AceThread cthread) {
		boolean ret = false;

		// find the entry in the pending operations
		AceSQLThread thr = (AceSQLThread) pendingOperations.get(id);

		if (thr != null) {
			// if the thread is still running
			thr.dispose(); // kill the thread
			ret = true;
		}

		// if the message has already been dispatched, to the calling thread,
		// remove it from the
		// queue
		if (cthread == null) {
			Thread t = Thread.currentThread();

			if (t instanceof AceThread) {
				cthread = (AceThread) t;
			} else {
				return ret;
			}
		}

		ret = cthread.removeMessage(
				new AceSQLMessage(AceSQLMessage.SQL_CANCELLED, id, null, 0, 0,
						thr.getUserParm()), this);

		return ret;
	}

	public void dispose() {
		if (!quit) {
			quit = true;
		}
	}

	public long executeSQL(AceThread cthread, Object userParam,
			String statement, MapResult resultHandler, Object... params) {
		return executeSQL(cthread, userParam, new SQLParam[] { new SQLParam(
				statement, resultHandler, params) });
	}

	public long executeSQL(AceThread cthread, Object userParam,
			SQLParam[] statements) {
		Thread caller = cthread;
		if (caller == null) {
			caller = Thread.currentThread();
		}

		if (!(caller instanceof AceThread)) {
			writeErrorMessage(
					"The calling thread must be an instance of AceThread", null);
			return -1;
		}

		long nextId;
		synchronized (nextOperationIdLock) {
			nextId = nextOperationId++;
		}

		AceSQLThread sql = new AceSQLThread(nextId, statements,
				(AceThread) caller, userParam);

		sql.start(); // start the thread

		return nextId;
	}

	public boolean same(AceMessageInterface obj1, AceMessageInterface obj2) {
		boolean ret = false;

		if (obj1 instanceof AceSQLMessage && obj2 instanceof AceSQLMessage) {
			if (((AceSQLMessage) obj1).getOperationId() == ((AceSQLMessage) obj2)
					.getOperationId()) {
				ret = true;
			}
		}

		return ret;
	}

	public AceMessageInterface waitSQLResult(int id) {
		Thread thr = Thread.currentThread();

		if ((thr instanceof AceThread) == false) {
			writeErrorMessage(
					"This method is not being called from an object which is a sub-class of type AceThread",
					null);
			return null;
		}

		// now wait for the response to the SQL statement
		AceThread cthread = (AceThread) thr;

		while (true) {
			AceMessageInterface msg = cthread.waitMessage();
			if (msg instanceof AceSQLMessage) {
				if (((AceSQLMessage) msg).getOperationId() == id) {
					return msg;
				}
			} else if ((msg instanceof AceSignalMessage) == true) {
				return msg;
			}
		}
	}

	private void writeErrorMessage(String error, Throwable e) {
		Thread cthread = Thread.currentThread();

		if (cthread instanceof AceThread) {
			((AceThread) cthread).dispatchErrorMessage(error, e);
		} else {
			AceLogger.Instance().log(AceLogger.ERROR, AceLogger.SYSTEM_LOG,
					"AceSQL.writeErrorMessage() : " + error, e);
		}
	}
}
