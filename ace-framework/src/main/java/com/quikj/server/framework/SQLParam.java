/**
 * 
 */
package com.quikj.server.framework;

/**
 * @author amit
 * 
 */
public class SQLParam {
	private String statement;
	private Object[] params = new Object[0];
	private MapResult resultHandler;

	public SQLParam(String statement, MapResult resultHandler, Object... params) {
		this.statement = statement;
		this.params = params;
		this.resultHandler = resultHandler;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public MapResult getResultHandler() {
		return resultHandler;
	}

	public void setResultHandler(MapResult resultHandler) {
		this.resultHandler = resultHandler;
	}
}
