package com.quikj.server.framework;

import java.util.List;

public class AceSQLMessage implements AceMessageInterface {

	public static final int SQL_CANCELLED = 0;
	public static final int SQL_EXECUTED = 1;
	public static final int SQL_ERROR = 2;

	private int status;

	private Object userParm;

	private List<Object> results;

	private int affectedRows;

	private long operationId;

	private int numStatementsExecuted;

	protected AceSQLMessage(int status, long operation_id, List<Object> results,
			int affectedRows, int numStatementsExecuted, Object userParm) {
		this.status = status;
		this.results = results;
		this.operationId = operation_id;
		this.userParm = userParm;
		this.affectedRows = affectedRows;
		this.numStatementsExecuted = numStatementsExecuted;
	}

	public int getAffectedRows() {
		return affectedRows;
	}

	public List<Object> getResults() {
		return results;
	}

	public long getOperationId() {
		return operationId;
	}

	public int getStatus() {
		return status;
	}

	public Object getUserParm() {
		return userParm;
	}

	public String messageType() {
		return new String("AceSQLMessage");
	}

	public int getNumStatementsExecuted() {
		return numStatementsExecuted;
	}
}
